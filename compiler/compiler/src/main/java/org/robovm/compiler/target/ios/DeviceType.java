/*
 * Copyright (C) 2013 RoboVM AB
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/gpl-2.0.html>.
 */
package org.robovm.compiler.target.ios;

import org.apache.commons.exec.util.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.robovm.compiler.config.Arch;
import org.robovm.compiler.log.Logger;
import org.robovm.compiler.util.Executor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Simulator device types, consisting of the device type id and SDK version as
 * listed by xcrun simctl devices -j list.
 */
public class DeviceType implements Comparable<DeviceType> {
    public static final String IOS_VERSION_PREFIX = "com.apple.CoreSimulator.SimRuntime.iOS-";
    public static final String PREFERRED_IPHONE_SIM_NAME = "iPhone 8";
    public static final String PREFERRED_IPAD_SIM_NAME = "iPad Air";

    public static final String[] ONLY_32BIT_DEVICES = {"iPhone 4", "iPhone 4s", "iPhone 5", "iPhone 5c", "iPad 2"};
    public static final Version ONLY_64BIT_IOS_VERSION = new Version(11, 0, 0);
    public static final Version ARM64_IOS_VERSION = new Version(14, 0, 0);

    public enum DeviceFamily {
        iPhone,
        iPad
    }

    // depending on x86_64 or m1 CPU use different host simulator arches
    public final static Arch DEFAULT_HOST_ARCH;
    static {
        String archProp = System.getProperty("os.arch").toLowerCase();
        if (archProp.matches("aarch64|arm64")) {
            DEFAULT_HOST_ARCH = Arch.arm64;
        } else {
            DEFAULT_HOST_ARCH = Arch.x86_64;
        }
    }

    private final String deviceName;
    private final String udid;
    private final String state;
    private final Version version;
    private final Set<Arch> archs;
    private final DeviceType pair;

    DeviceType(String deviceName, String udid, String state, Version version, Set<Arch> archs, DeviceType pair) {
        this.deviceName = deviceName;
        this.udid = udid;
        this.state = state;
        this.version = version;
        this.archs = archs;
        this.pair = pair;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public Version getVersion() {
        return version;
    }

    public Set<Arch> getArchs() {
        return Collections.unmodifiableSet(archs);
    }

    public DeviceType getPair() {
        return pair;
    }

    /**
     * @return id as understood by the AppCompiler -simdevicetype flag
     */
    public String getSimpleDeviceTypeId() {
        return getSimpleDeviceName() + ", " + getVersion();
    }

    public String getSimpleDeviceName() {
        return deviceName + " (" + udid + ")";
    }

    public DeviceFamily getFamily() {
        if (deviceName.contains("iPhone")) {
            return DeviceFamily.iPhone;
        } else {
            return DeviceFamily.iPad;
        }
    }

    public String getUdid() {
        return udid;
    }

    public String getState() {
        return state;
    }

    /**
     * @return fresh copy -- to receive fresh device state (and paired state)
     */
    public DeviceType refresh() {
        for (DeviceType t : listDeviceTypes()) {
            if (udid.equals(t.udid))
                return t;
        }

        return null;
    }

    public static List<DeviceType> listDeviceTypes() {
        try {
            String capture = new Executor(Logger.NULL_LOGGER, "xcrun").args(
                    "simctl", "list", "devices", "pairs", "-j").execCapture();
            List<DeviceType> types = new ArrayList<>();

            JSONParser parser = new JSONParser();
            JSONObject root = (JSONObject) parser.parse(capture);

            // parse watch pairs to
            Map<String, DeviceType> pairs = new HashMap<>();
            JSONObject pairList = (JSONObject) root.get("pairs");
            if (pairList != null) {
                for (Object e : pairList.values()) {
                    JSONObject entry = (JSONObject) e;
                    if (entry.containsKey("state") && entry.get("state").toString().contains("unavailable"))
                        continue;
                    JSONObject watchEntry = (JSONObject) entry.get("watch");
                    JSONObject phoneEntry = (JSONObject) entry.get("phone");
                    if (watchEntry != null && phoneEntry != null) {
                        String phoneUdid = phoneEntry.get("udid").toString();
                        String watchUdid = watchEntry.get("udid").toString();
                        String watchName = watchEntry.get("name").toString();
                        String watchState = watchEntry.get("state").toString();
                        if (watchState.contains("unavailable"))
                            continue;
                        DeviceType simpleWatch = new DeviceType(watchName, watchUdid, watchState,
                                new Version(0, 0, 0), Collections.emptySet(), null);
                        pairs.put(phoneUdid, simpleWatch);
                    }
                }
            }

            JSONObject deviceList = (JSONObject) root.get("devices");
            for (Object value : deviceList.entrySet()) {
                //noinspection rawtypes
                Map.Entry entry = (Map.Entry) value;
                String versionKey = entry.getKey().toString();
                if (versionKey.startsWith(IOS_VERSION_PREFIX)) {
                    // com.apple.CoreSimulator.SimRuntime.iOS-
                    versionKey = versionKey.replace(IOS_VERSION_PREFIX, "").replace('-', '.');
                } else if (versionKey.startsWith("iOS ")) {
                    versionKey = versionKey.replace("iOS ", "");
                } else {
                    // not iOS
                    continue;
                }
                JSONArray devices = (JSONArray) entry.getValue();
                for (Object obj : devices) {
                    JSONObject device = (JSONObject) obj;
                    boolean isAvailable = false;
                    if (device.containsKey("isAvailable")) {
                        Object o = device.get("isAvailable");
                        isAvailable = o instanceof Boolean ? (Boolean) o : "true".equals(o.toString());
                    } else if (device.containsKey("availability"))
                        isAvailable = !device.get("availability").toString().contains("unavailable");

                    if (isAvailable) {
                        final String deviceName = device.get("name").toString();
                        final Version version = Version.parse(versionKey);
                        Set<Arch> archs = new HashSet<>();
                        if (!Arrays.asList(ONLY_32BIT_DEVICES).contains(deviceName)) {
                            // This is assumption that on M1 ios versions starting from ios14 can run arm64 target
                            if (DEFAULT_HOST_ARCH == Arch.arm64 && version.isSameOrBetter(ARM64_IOS_VERSION))
                                archs.add(Arch.arm64);
                            archs.add(Arch.x86_64);
                        }
                        if (DEFAULT_HOST_ARCH != Arch.arm64 && !version.isSameOrBetter(ONLY_64BIT_IOS_VERSION)) {
                            // 32 bit device. not supported on Arm
                            archs.add(Arch.x86);
                        }

                        String udid = device.get("udid").toString();
                        DeviceType watchPair = pairs.get(udid);
                        types.add(new DeviceType(deviceName, udid, device.get("state").toString(), version, archs, watchPair));
                    }
                }
            }

            // Sort. Make sure that devices that have an id which is a prefix of
            // another id comes before in the list.
            Collections.sort(types);
            return types;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int compareTo(DeviceType that) {
        int c = this.version.compareTo(that.version);
        if (c == 0) {
            c = this.getFamily().compareTo(that.getFamily());
            if (c == 0) {
                c = this.deviceName.compareToIgnoreCase(that.deviceName);
            }
        }
        return c;
    }

    private static List<DeviceType> filter(List<DeviceType> deviceTypes, Arch arch,
                                           DeviceFamily family, String deviceName, Version simVersion) {

        deviceName = deviceName == null ? null : deviceName.toLowerCase();

        List<DeviceType> result = new ArrayList<>();
        for (DeviceType type : deviceTypes) {
            if (arch == null || type.getArchs().contains(arch)) {
                if (family == null || family == type.getFamily()) {
                    if (deviceName == null || type.getDeviceName().toLowerCase().contains(deviceName)) {
                        if (simVersion == null || type.version.isSameOrBetter(simVersion)) {
                            result.add(type);
                        }
                    }
                }
            }
        }
        return result;
    }

    public static List<String> getSimpleDeviceTypeIds() {
        List<String> result = new ArrayList<>();
        for (DeviceType type : listDeviceTypes()) {
            result.add(type.getSimpleDeviceTypeId());
        }
        return result;
    }

    public static DeviceType getDeviceType(String displayName) {
        List<DeviceType> types = listDeviceTypes();
        if (displayName == null) {
            return null;
        }
        for (DeviceType type : types) {
            if (displayName.equals(type.getSimpleDeviceTypeId())) {
                return type;
            }
        }
        return null;
    }

    public static DeviceType getBestDeviceType() {
        return getBestDeviceType(null, null, null, null);
    }

    public static DeviceType getBestDeviceType(DeviceFamily family) {
        return getBestDeviceType(null, family, null, null);
    }

    /**
     * Returns the best {@link DeviceType} matching the parameters. If multiple
     * device types match the parameters the first one with the highest SDK
     * number will be returned. If no device name and no {@link DeviceFamily} is
     * specified this method will default to {@link DeviceFamily#iPhone}.
     */
    public static DeviceType getBestDeviceType(Arch arch, DeviceFamily family,
                                               String deviceName, String deviceVersion) {

        if (deviceName == null && family == null) {
            family = DeviceFamily.iPhone;
        }
        String preferredDeviceName = PREFERRED_IPHONE_SIM_NAME;
        if (family == DeviceFamily.iPad) {
            preferredDeviceName = PREFERRED_IPAD_SIM_NAME;
        }


        // set simulator types and find one that matches version and name
        DeviceType exact = null;
        DeviceType bestDefault = null;
        DeviceType bestAny = null;
        Version version = deviceVersion != null ? Version.parse(deviceVersion) : null;
        List<DeviceType> devices = filter(listDeviceTypes(), arch, family, deviceName, version);
        for (DeviceType type : devices) {
            if (type.getDeviceName().equals(deviceName)) {
                // match for specified device
                if (exact == null || (version == null && type.version.versionCode > exact.version.versionCode))
                    exact = type;
            } else if (deviceName == null && type.getDeviceName().equals(preferredDeviceName)) {
                // match for preferable device
                if (bestDefault == null || (version == null && type.version.versionCode > bestDefault.version.versionCode))
                    bestDefault = type;
            } else {
                // just pick one with best version
                if (bestAny == null || (version == null && type.version.versionCode > bestAny.version.versionCode))
                    bestAny = type;
            }
        }

        DeviceType best = exact;
        if (best == null) {
            best = bestDefault != null ? bestDefault : bestAny;
        }
        if (best == null) {
            throw new IllegalArgumentException("Unable to find a matching device "
                    + "[arch=" + arch + ", family=" + family
                    + ", name=" + deviceName + ", version=" + version + "]");
        }
        return best;
    }

    @Override
    public String toString() {
        return "DeviceType [deviceName=" + deviceName + ", version=" + version + ", archs=" + archs + "]";
    }

    /**
     * simulator version
     */
    public static class Version {
        public final int major, minor, revision, versionCode;

        public Version(int major, int minor, int revision) {
            this.major = major;
            this.minor = minor;
            this.revision = revision;
            this.versionCode = (major << 16) | (minor << 8) | revision;
        }

        static Version parse(String v) {
            String[] parts = StringUtils.split(v, ".");
            int major = Integer.parseInt(parts[0]);
            int minor = parts.length >= 2 ? Integer.parseInt(parts[1]) : 0;
            int revision = parts.length >= 3 ? Integer.parseInt(parts[2]) : 0;
            return new Version(major, minor, revision);
        }

        @Override
        public String toString() {
            return major + "." + minor + (revision != 0 ? ("." + revision) : "");
        }

        public int compareTo(Version other) {
            return versionCode - other.versionCode;
        }

        public boolean isSameOrBetter(Version other) {
            return versionCode >= other.versionCode;
        }
    }
}
