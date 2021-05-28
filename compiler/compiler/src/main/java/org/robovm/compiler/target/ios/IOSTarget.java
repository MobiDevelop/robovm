/*
 * Copyright (C) 2012 RoboVM AB
 * Copyright (C) 2018 Daniel Thommes, NeverNull GmbH, <daniel.thommes@nevernull.io>
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

import com.dd.plist.NSArray;
import com.dd.plist.NSDictionary;
import com.dd.plist.NSNumber;
import com.dd.plist.NSObject;
import com.dd.plist.NSString;
import com.dd.plist.PropertyListFormatException;
import com.dd.plist.PropertyListParser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.PrefixFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang3.tuple.Pair;
import org.robovm.compiler.CompilerException;
import org.robovm.compiler.config.*;
import org.robovm.compiler.log.Logger;
import org.robovm.compiler.target.AbstractTarget;
import org.robovm.compiler.target.LaunchParameters;
import org.robovm.compiler.target.Launcher;
import org.robovm.compiler.target.ios.ProvisioningProfile.Type;
import org.robovm.compiler.util.Executor;
import org.robovm.compiler.util.PList;
import org.robovm.compiler.util.ToolchainUtil;
import org.robovm.compiler.util.io.OpenOnWriteFileOutputStream;
import org.robovm.libimobiledevice.AfcClient.UploadProgressCallback;
import org.robovm.libimobiledevice.IDevice;
import org.robovm.libimobiledevice.InstallationProxyClient.StatusCallback;
import org.robovm.libimobiledevice.util.AppLauncher;
import org.robovm.libimobiledevice.util.AppLauncherCallback;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author niklas
 *
 */
public class IOSTarget extends AbstractTarget {
    final List<String> excludedKeys = Arrays.asList(
        "com.apple.developer.icloud-container-development-container-identifiers",
        "com.apple.developer.icloud-container-environment",
        "com.apple.developer.icloud-container-identifiers",
        "com.apple.developer.icloud-services",
        "com.apple.developer.restricted-resource-mode",
        "com.apple.developer.ubiquity-container-identifiers",
        "com.apple.developer.ubiquity-kvstore-identifier",
        "inter-app-audio",
        "com.apple.developer.homekit",
        "com.apple.developer.healthkit",
        "com.apple.developer.in-app-payments",
        "com.apple.developer.associated-domains",
        "com.apple.security.application-groups",
        "com.apple.developer.maps",
        "com.apple.developer.networking.vpn.api",
        "com.apple.external-accessory.wireless-configuration"
    );

    public static final String TYPE = "ios";

    private Arch arch;
    private Environment env;
    private SDK sdk;
    private File entitlementsPList;
    private SigningIdentity signIdentity;
    private ProvisioningProfile provisioningProfile;
    private IDevice device;
    private File partialPListDir;

    public IOSTarget() {}

    public String getType() {
        return TYPE;
    }

    @Override
    public Arch getArch() {
        return arch;
    }

    @Override
    public Environment getEnv() {
        return env;
    }

    @Override
    public LaunchParameters createLaunchParameters() {
        if (isSimulatorArch(arch, env)) {
            return new IOSSimulatorLaunchParameters();
        }
        return new IOSDeviceLaunchParameters();
    }

    public static boolean isSimulatorArch(Arch arch, Environment env) {
        return env == Environment.Simulator && (arch == Arch.x86 || arch == Arch.x86_64 || arch == Arch.arm64);
    }

    public static boolean isDeviceArch(Arch arch, Environment env) {
        return env == Environment.Native &&  (arch == Arch.thumbv7 || arch == Arch.arm64);
    }

    /**
     * Late initialization as we cannot assume that the tmp dir is available at configuration creation
     *
     * @return
     */
    private File getPartialPListDir() {
        if (!partialPListDir.exists()) {
            partialPListDir.mkdirs();
        }
        return partialPListDir;
    }

    public List<SDK> getSDKs() {
        if (isSimulatorArch(arch, env)) {
            return SDK.listSimulatorSDKs();
        } else {
            return SDK.listDeviceSDKs();
        }
    }

    /**
     * Returns the {@link IDevice} when an app has been launched on a device.
     * Returns {@code null} before {@link #launch(LaunchParameters)} has been
     * called or if the app was launched in the simulator.
     */
    public IDevice getDevice() {
        return device;
    }

    @Override
    protected Launcher createLauncher(LaunchParameters launchParameters) throws IOException {
        if (isSimulatorArch(arch, env)) {
            return createIOSSimLauncher(launchParameters);
        } else {
            return createIOSDevLauncher(launchParameters);
        }
    }

    private Launcher createIOSSimLauncher(LaunchParameters launchParameters) throws IOException {
        return new SimLauncherProcess(config.getLogger(), getAppDir(), getBundleId(), (IOSSimulatorLaunchParameters) launchParameters);
    }

    private Launcher createIOSDevLauncher(LaunchParameters launchParameters)
            throws IOException {

        IOSDeviceLaunchParameters deviceLaunchParameters = (IOSDeviceLaunchParameters) launchParameters;
        String deviceId = deviceLaunchParameters.getDeviceId();
        int forwardPort = deviceLaunchParameters.getForwardPort();
        AppLauncherCallback callback = deviceLaunchParameters.getAppPathCallback();
        if (deviceId == null) {
            String[] udids = IDevice.listUdids();
            if (udids.length == 0) {
                throw new RuntimeException("No devices connected");
            }
            if (udids.length > 1) {
                config.getLogger().warn("More than 1 device connected (%s). "
                        + "Using %s.", Arrays.asList(udids), udids[0]);
            }
            deviceId = udids[0];
        }
        device = new IDevice(deviceId);

        OutputStream out = null;
        if (launchParameters.getStdoutFifo() != null) {
            out = new OpenOnWriteFileOutputStream(launchParameters.getStdoutFifo());
        } else {
            out = System.out;
        }

        Map<String, String> env = launchParameters.getEnvironment();
        if (env == null) {
            env = new HashMap<>();
        }
        //Fix for #71, see http://stackoverflow.com/questions/37800790/hide-strange-unwanted-xcode-8-logs
        env.put("OS_ACTIVITY_DT_MODE", "");

        AppLauncher launcher = new AppLauncher(device, getAppDir()) {
            protected void log(String s, Object... args) {
                config.getLogger().info(s, args);
            }
        }
                .stdout(out)
                .closeOutOnExit(true)
                .args(launchParameters.getArguments(true).toArray(new String[0]))
                .env(env)
                .forward(forwardPort)
                .appLauncherCallback(callback)
                .xcodePath(ToolchainUtil.findXcodePath())
                .uploadProgressCallback(new UploadProgressCallback() {
                    boolean first = true;

                    public void success() {
                        config.getLogger().info("[100%%] Upload complete");
                    }

                    public void progress(File path, int percentComplete) {
                        if (first) {
                            config.getLogger().info("[  0%%] Beginning upload...");
                        }
                        first = false;
                        config.getLogger().info("[%3d%%] Uploading %s...", percentComplete, path);
                    }

                    public void error(String message) {}
                })
                .installStatusCallback(new StatusCallback() {
                    boolean first = true;

                    public void success() {
                        config.getLogger().info("[100%%] Install complete");
                    }

                    public void progress(String status, int percentComplete) {
                        if (first) {
                            config.getLogger().info("[  0%%] Beginning installation...");
                        }
                        first = false;
                        config.getLogger().info("[%3d%%] %s", percentComplete, status);
                    }

                    public void error(String message) {}
                });

        return new AppLauncherProcess(config.getLogger(), launcher, launchParameters);
    }

    @Override
    protected void doBuild(File outFile, List<String> ccArgs,
            List<File> objectFiles, List<String> libArgs)
            throws IOException {

        // Always link against UIKit or else it will not be initialized properly
        // causing problems with UIAlertView and maybe other classes on iOS 7
        // (#195)
        if (!config.getFrameworks().contains("UIKit")) {
            libArgs.add("-framework");
            libArgs.add("UIKit");
        }

        String minVersion = getMinimumOSVersion();

        int majorVersionNumber = -1;
        try {
            majorVersionNumber = Integer.parseInt(minVersion.substring(0, minVersion.indexOf('.')));
            int minMajorSupportedVersion = Integer.parseInt(config.getOs().getMinVersion().substring(0, config.getOs().getMinVersion().indexOf('.')));

            if (majorVersionNumber < minMajorSupportedVersion) {
                throw new CompilerException("MinimumOSVersion of " + minVersion + " is not supported. "
                    + "The minimum version for this platform is " + config.getOs().getMinVersion());
            }

        } catch (NumberFormatException e) {
            throw new CompilerException("Failed to get major version number from "
                    + "MinimumOSVersion string '" + minVersion + "'");
        }

        ccArgs.add("--target=" + config.getClangTriple(getMinimumOSVersion()));

        if (isDeviceArch(arch, env)) {
            if (config.isDebug()) {
                ccArgs.add("-Wl,-no_pie");
            }
            if (config.isEnableBitcode()) {
                // tells clang to keep bitcode while linking
                ccArgs.add("-fembed-bitcode");
            }
        } else {
            if (config.getArch() == Arch.x86 || config.isDebug()) {
                ccArgs.add("-Wl,-no_pie");
            }
        }
        ccArgs.add("-isysroot");
        ccArgs.add(sdk.getRoot().getAbsolutePath());

        // specify sdk version for linker
        libArgs.add("-Xlinker");
        libArgs.add("-sdk_version");
        libArgs.add("-Xlinker");
        libArgs.add(sdk.getVersion());

        // add runtime path to swift libs first to support swift-5 libs location
        libArgs.add("-Xlinker");
        libArgs.add("-rpath");
        libArgs.add("-Xlinker");
        libArgs.add("/usr/lib/swift");
        // specify dynamic library loading path
        libArgs.add("-Xlinker");
        libArgs.add("-rpath");
        libArgs.add("-Xlinker");
        libArgs.add("@executable_path/Frameworks");
        libArgs.add("-Xlinker");
        libArgs.add("-rpath");
        libArgs.add("-Xlinker");
        libArgs.add("@loader_path/Frameworks");

        if (!isDeviceArch(arch, env)) {
            // add simulated entitlement to allow Security framework to work on simulator
            File simEntitlement = createSimulatedEntitlementsPList(getBundleId());
            ccArgs.add("-Xlinker");
            ccArgs.add("-sectcreate");
            ccArgs.add("-Xlinker");
            ccArgs.add("__TEXT");
            ccArgs.add("-Xlinker");
            ccArgs.add("__entitlements");
            ccArgs.add("-Xlinker");
            ccArgs.add(simEntitlement.getAbsolutePath());
        }

        super.doBuild(outFile, ccArgs, objectFiles, libArgs);
    }

    protected void prepareInstall(File installDir) throws IOException {
        createInfoPList(installDir);
        generateDsym(installDir, getExecutable(), false);

        if (isDeviceArch(arch, env)) {
            // strip local symbols
            strip(installDir, getExecutable());

            // remove bitcode to minimize binary size if not required
            if (!config.isEnableBitcode()) {
                config.getLogger().info("Striping bitcode from binary: %s", new File(installDir, getExecutable()));
                stripBitcode(new File(installDir, getExecutable()));
            }
            if (config.isIosSkipSigning()) {
                config.getLogger().warn("Skipping code signing. The resulting app will "
                        + "be unsigned and will not run on unjailbroken devices");
                codesignApp(SigningIdentity.ADHOC, getOrCreateEntitlementsPList(false, getBundleId()), installDir);
            } else {
                String appIdPrefix = provisioningProfile.getAppIdPrefix();

                // Copy the provisioning profile
                copyProvisioningProfile(provisioningProfile, installDir);
                boolean getTaskAllow = provisioningProfile.getType() == Type.Development;
                signFrameworks(signIdentity, installDir);
                // app extensions
                provisionAppExtensions(config.getAppExtensions(), signIdentity, installDir);
                signAppExtensions(signIdentity, installDir, appIdPrefix, getTaskAllow);
                // watch app
                if (config.getWatchKitApp() != null) {
                    provisionWatchApp(signIdentity, installDir);
                    signWatchApp(signIdentity, installDir, appIdPrefix, getTaskAllow);
                }
                // app
                codesignApp(signIdentity, getOrCreateEntitlementsPList(getTaskAllow, getBundleId()), installDir);
            }
        }
    }

    private void copyProvisioningProfile(ProvisioningProfile profile, File destDir) throws IOException {
        config.getLogger().info("Copying %s provisioning profile: %s (%s)",
                profile.getType(),
                profile.getName(),
                profile.getEntitlements().objectForKey("application-identifier"));
        FileUtils.copyFile(profile.getFile(), new File(destDir, "embedded.mobileprovision"));
    }

    @Override
    public void prepareLaunch() throws IOException {
        prepareLaunch(getAppDir());
    }


    protected void prepareLaunch(File appDir) throws IOException {
        super.doInstall(appDir, getExecutable(), appDir);
        createInfoPList(appDir);
        generateDsym(appDir, getExecutable(), true);

        // strip symbols to reduce application size, all debugger symbols converted into globals
        strip(appDir, getExecutable());

        if (isDeviceArch(arch, env)) {
            if (config.isIosSkipSigning()) {
                config.getLogger().warn("Skiping code signing. The resulting app will "
                        + "be unsigned and will not run on unjailbroken devices");
                codesignApp(SigningIdentity.ADHOC, getOrCreateEntitlementsPList(true, getBundleId()), appDir);
            } else {
                String appIdPrefix = provisioningProfile.getAppIdPrefix();

                copyProvisioningProfile(provisioningProfile, appDir);
                boolean getTaskAllow = provisioningProfile.getType() == Type.Development;
                signFrameworks(signIdentity, appDir);
                // app extensions
                provisionAppExtensions(config.getAppExtensions(), signIdentity, appDir);
                signAppExtensions(signIdentity, appDir, appIdPrefix, getTaskAllow);
                // watch app
                if (config.getWatchKitApp() != null) {
                    provisionWatchApp(signIdentity, appDir);
                    signWatchApp(signIdentity, appDir, appIdPrefix, getTaskAllow);
                }
                // sign the app
                codesignApp(signIdentity, getOrCreateEntitlementsPList(getTaskAllow, getBundleId()), appDir);
            }
        } else { // simulator
            if (sdk.getVersionCode() >= 0x0B0300) {
                // code signing of frameworks and app extensions are required since iOS 11.3
                signFrameworks(SigningIdentity.ADHOC, appDir);
                signAppExtensions(SigningIdentity.ADHOC, appDir, null, true);
                if (config.getWatchKitApp() != null)
                    signWatchApp(SigningIdentity.ADHOC, appDir, null, true);
            }
            // sign the app
            // NB: it is not required as app can run without it but Xcode does this
            codesignApp(SigningIdentity.ADHOC, createEntitlementsPList(true), appDir);
        }
    }

    private void signFrameworks(SigningIdentity identity, File appDir) throws IOException {
        // sign dynamic frameworks first
        File frameworksDir = new File(appDir, "Frameworks");
        if (frameworksDir.exists() && frameworksDir.isDirectory()) {
            // Sign swift rt libs
            for (File swiftLib : frameworksDir.listFiles()) {
                if (swiftLib.getName().endsWith(".dylib")) {
                    codesignSwiftLib(identity, swiftLib);
                }
            }

            // sign embedded frameworks
            for (File framework : frameworksDir.listFiles()) {
                if (framework.isDirectory() && framework.getName().endsWith(".framework")) {
                    codesignCustomFramework(identity, framework);
                }
            }
        }
    }

    private void signAppExtensions(SigningIdentity identity, File appDir, String appIdPrefix, boolean getTaskAllow) throws IOException {
        // sign dynamic frameworks first
        File extensionsDir = new File(appDir, "PlugIns");
        if (extensionsDir.exists() && extensionsDir.isDirectory()) {
            // sign embedded app-extensions
            for (File extension : extensionsDir.listFiles()) {
                if (extension.isDirectory() && extension.getName().endsWith(".appex"))
                    signSingleAppExtension(identity, extension, appIdPrefix, getTaskAllow);
            }
        }
    }

    private void signSingleAppExtension(SigningIdentity identity, File extDir, String appIdPrefix, boolean getTaskAllow) throws IOException {
        String appExBundleId = null;
        // NB: appIdPrefix is null for simulator build
        if (appIdPrefix != null) {
            // read extension bundle id from plist (id was generated during copy phase up to robovm.xml config)
            File infoPlistFile = new File(extDir, "Info.plist");
            try {
                NSDictionary infoPlist = (NSDictionary) PropertyListParser.parse(infoPlistFile);
                appExBundleId = infoPlist.get("CFBundleIdentifier").toString();
            } catch (PropertyListFormatException | SAXException | ParserConfigurationException | ParseException e) {
                throw new IOException(e);
            }
            appExBundleId = appIdPrefix + "." + appExBundleId;
        }
        // create entitlements
        File entitlements = createEntitlementForAppEx(getTaskAllow, appExBundleId);

        // now sign
        codesignAppExtension(identity, entitlements, extDir);
    }

    private void signWatchApp(SigningIdentity identity, File appDir, String appIdPrefix, boolean getTaskAllow) throws IOException {
        // sign watch app
        WatchKitApp waConfig = config.getWatchKitApp();

        String waName = waConfig.getWatchAppName();
        File waDir = new File(appDir, "Watch/" + waName);
        if (!waDir.exists() || !waDir.isDirectory())
            throw new IllegalStateException("Error while signing WatchApp, watch directory doesn't exist: " + waDir.getAbsolutePath());

        // sign all extension first
        signAppExtensions(identity, waDir, appIdPrefix, getTaskAllow);

        // sign watch app (same way as app extension)
        signSingleAppExtension(identity, waDir, appIdPrefix, getTaskAllow);
    }

    /**
     * generates simple entitlement plist which is required for AppEx during submit to app store
     * @param bundleId -- might be null for simulator
     */
    private File createEntitlementForAppEx(boolean getTaskAllow, String bundleId) throws IOException {
        try {
            File destFile = new File(config.getTmpDir(), "AppExtEntitlements.plist");
            NSDictionary dict = new NSDictionary();
            if (bundleId != null)
                dict.put("application-identifier", bundleId);
            // xcode uses prefix for simulators entitlements
            String prefix = isDeviceArch(arch, env) ? "" : "com.apple.security.";
            dict.put(prefix + "get-task-allow", getTaskAllow);
            PropertyListParser.saveAsXML(dict, destFile);
            return destFile;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * finds and copies provisioning profiles for AppExtensions
     *
     * @param extensions   extension information from robovm.xml
     * @param signIdentity ideantity used to sign application
     * @param installDir   of application
     */
    private void provisionAppExtensions(List<AppExtension> extensions, SigningIdentity signIdentity, File installDir) throws IOException {
        File pluginsDir = new File(installDir, "PlugIns");
        if (pluginsDir.exists() && pluginsDir.isDirectory()) {
            Map<String, AppExtension> extensionsMap = new HashMap<>();
            extensions.forEach(e -> extensionsMap.put(e.getNameWithExt(".appex"), e));

            // move through all extensions in directory
            for (File extPath : pluginsDir.listFiles()) {
                if (!extPath.isDirectory() || !extPath.getName().endsWith(".appex")) {
                    config.getLogger().info("Skipping not expected file/dir '%s' in PlugIns folder: %s",
                            extPath.getName(), pluginsDir.getAbsolutePath());
                    continue;
                }
                String name = extPath.getName();
                AppExtension extension = extensionsMap.get(name);
                if (extension == null) {
                    extension = AppExtension.DEFAULT_RULE;
                    config.getLogger().info("Using default signing rules for app extension " + name);
                }

                provisionSingleAppExtension(extension, signIdentity, extPath);
            }
        }
    }

    /**
     * provision single app extension:
     * - finds and copies profile for it
     */
    private void provisionSingleAppExtension(AppExtension extension, SigningIdentity signIdentity, File extPath) throws IOException {
        if (extension.skipSigning())
            return;

        // read extension bundle id from plist (id was generated during copy phase up to robovm.xml config)
        File infoPlistFile = new File(extPath, "Info.plist");
        String appExBundleId;
        try {
            NSDictionary infoPlist = (NSDictionary) PropertyListParser.parse(infoPlistFile);
            appExBundleId = infoPlist.get("CFBundleIdentifier").toString();
        } catch (PropertyListFormatException | SAXException | ParserConfigurationException | ParseException e) {
            throw new IOException(e);
        }

        ProvisioningProfile appExtProfile;
        String profileName = extension.getProfile();
        if (profileName != null) {
            // profile is specified in robovm.xml
            appExtProfile = ProvisioningProfile.find(ProvisioningProfile.list(), profileName);
        } else {
            // find profile that matches app ext bundle id
            appExtProfile = ProvisioningProfile.find(ProvisioningProfile.list(), signIdentity, appExBundleId);
        }

        if (appExtProfile != null) {
            config.getLogger().info("Copying %s provisioning profile for : %s (%s)",
                    appExtProfile.getType(),
                    appExtProfile.getName(),
                    appExtProfile.getEntitlements().objectForKey("application-identifier"));
            FileUtils.copyFile(appExtProfile.getFile(), new File(extPath, "embedded.mobileprovision"));
        } else {
            throw new RuntimeException("Failed to locate provisioning profile for " + extPath.getName());
        }
    }

    /**
     * provision WatchApplication and its extensions
     * @param signIdentity used for signing the application
     * @param installDir of application
     */
    private void provisionWatchApp(SigningIdentity signIdentity, File installDir) throws IOException {
        WatchKitApp waConfig = config.getWatchKitApp();

        // provision app itself
        File waDir = new File(installDir, "Watch/" + waConfig.getWatchAppName());
        provisionSingleAppExtension(waConfig.getApp(), signIdentity, waDir);

        // provision plugins
        provisionAppExtensions(waConfig.getExtensions(), signIdentity, new File(waDir, "/PlugIns" ));
    }

    private void codesignApp(SigningIdentity identity, File entitlementsPList, File appDir) throws IOException {
        config.getLogger().info("Code signing app using identity '%s' with fingerprint %s", identity.getName(),
                identity.getFingerprint());
        codesign(identity, entitlementsPList, false, false, true, appDir);
    }

    private void codesignSwiftLib(SigningIdentity identity, File swiftLib) throws IOException {
        config.getLogger().info("Code signing swift dylib '%s' using identity '%s' with fingerprint %s", swiftLib.getName(), identity.getName(),
                identity.getFingerprint());
        codesign(identity, null, false, true, false, swiftLib);
    }

    private void codesignCustomFramework(SigningIdentity identity, File frameworkDir) throws IOException {
        config.getLogger().info("Code signing framework '%s' using identity '%s' with fingerprint %s", frameworkDir.getName(), identity.getName(),
                identity.getFingerprint());
        codesign(identity, null, true, false, true, frameworkDir);
    }

    private void codesignAppExtension(SigningIdentity identity, File entitlementsPList, File extensionDir) throws IOException {
        config.getLogger().info("Code signing app-extension '%s' using identity '%s' with fingerprint %s", extensionDir.getName(), identity.getName(),
                identity.getFingerprint());
        codesign(identity, entitlementsPList, false, false, true, extensionDir);
    }

    private void codesign(SigningIdentity identity, File entitlementsPList, boolean preserveMetadata, boolean verbose, boolean allocate, File target) throws IOException {
        List<Object> args = new ArrayList<>();
        args.add("-f");
        args.add("-s");
        args.add(identity.getFingerprint());
        if (entitlementsPList != null) {
            args.add("--entitlements");
            args.add(entitlementsPList);
        }
        if (preserveMetadata) {
            args.add("--preserve-metadata=identifier,entitlements");
        }
        if (verbose) {
            args.add("--verbose");
        }
        if (identity == SigningIdentity.ADHOC) {
            // don't contact time server in case of adhoc signing to save time
            args.add("--timestamp=none");
        }
        args.add(target);
        Executor executor = new Executor(config.getLogger(), "codesign");
        if (allocate) {
            executor.addEnv("CODESIGN_ALLOCATE", ToolchainUtil.findXcodeCommand("codesign_allocate", "iphoneos"));
        }
        executor.args(args);
        executor.exec();
    }

    /**
     * creates simple entitlement plist from scratch that only contains get-task-allow
     * used to sign binary for simulator
     */
    private File createEntitlementsPList(boolean getTaskAllow) throws IOException {
        try {
            File destFile = new File(config.getTmpDir(), "Entitlements.plist");
            NSDictionary dict = new NSDictionary();
            dict.put("com.apple.security.get-task-allow", getTaskAllow);
            PropertyListParser.saveAsXML(dict, destFile);
            return destFile;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates simulated entitlements for simulator run. These are not used for signing
     * but just put as __TEXT __entitlements section
     */
    private File createSimulatedEntitlementsPList(String bundleId) throws IOException {
        try {
            // there is no provisioning profile in simulator to pick teamId. check if user have provided in properties
            String teamID = config.getProperties().getProperty("teamID");
            if (teamID == null) {
                // generate team ID from bundle id. use MD5 hash from bundle, then convert it to base-36
                teamID = new BigInteger(MessageDigest.getInstance("MD5").digest(bundleId.getBytes())).toString(36).toUpperCase();
                if (teamID.length() > 10)
                    teamID = teamID.substring(0, 10);
            }

            File destFile = new File(config.getTmpDir(), "Entitlements-Simulated.plist");
            NSDictionary dict;
            if (entitlementsPList != null) {
                // use properties. this allows teamID (and other placeholders) to be used in entitlements
                Properties properties = new Properties(config.getProperties());
                properties.setProperty("teamID", teamID);
                dict = new PList(entitlementsPList).parse(properties).getDictionary();
            } else {
                dict = new NSDictionary();
            }

            dict.put("application-identifier", teamID + "." + bundleId);
            PropertyListParser.saveAsXML(dict, destFile);
            return destFile;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private File getOrCreateEntitlementsPList(boolean getTaskAllow, String bundleId) throws IOException {
        try {
            File destFile = new File(config.getTmpDir(), "Entitlements.plist");
            NSDictionary dict = null;
            if (provisioningProfile != null) {
                String teamID = provisioningProfile.getAppIdPrefix();
                if (entitlementsPList != null) {
                    // use properties. this allows teamID (and other placeholders) to be used in entitlements
                    Properties properties = new Properties(config.getProperties());
                    properties.setProperty("teamID", teamID);
                    dict = new PList(entitlementsPList).parse(properties).getDictionary();
                } else {
                    dict = new NSDictionary();
                }

                NSDictionary profileEntitlements = provisioningProfile.getEntitlements();
                for (String key : profileEntitlements.allKeys()) {
                    if (dict.objectForKey(key) == null && !excludedKeys.contains(key)) {
                        dict.put(key, profileEntitlements.objectForKey(key));
                    }
                }
                dict.put("application-identifier", teamID + "." + bundleId);
            } else {
                // should not happen
                dict = entitlementsPList == null ? new NSDictionary() : (NSDictionary) PropertyListParser.parse(entitlementsPList);
            }
            dict.put("get-task-allow", getTaskAllow);
            PropertyListParser.saveAsXML(dict, destFile);
            return destFile;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void generateDsym(final File dir, final String executable, boolean copyToIndexedDir) throws IOException {
        final File dsymDir = new File(dir.getParentFile(), dir.getName() + ".dSYM");
        final File exePath = new File(dir, executable);
        FileUtils.deleteDirectory(dsymDir);
        final Process process = new Executor(config.getLogger(), "xcrun")
                .args("dsymutil", "-o", dsymDir, exePath)
                .execAsync();
        if (copyToIndexedDir) {
            new Thread() {
                public void run() {
                    try {
                        process.waitFor();
                    } catch (InterruptedException e) {
                        return;
                    }
                    copyToIndexedDir(dir, executable, dsymDir, exePath);
                }
            }.start();
        }
    }

    private void strip(File dir, String executable) throws IOException {
        new Executor(config.getLogger(), "xcrun")
                .args("strip", "-x", new File(dir, executable))
                .exec();
    }

    @Override
    protected void doInstall(File installDir, String executable, File resourcesDir) throws IOException {
        super.doInstall(installDir, getExecutable(), resourcesDir);
        prepareInstall(installDir);
    }

    @Override
    protected Process doLaunch(LaunchParameters launchParameters) throws IOException {
        // in IDEA prepare for launch is happening during build phase to not block calling thread
        // all other plugins will prepare here
        if (!config.isManuallyPreparedForLaunch())
            prepareLaunch();
        Process process = super.doLaunch(launchParameters);
        return process;
    }

    @Override
    public List<Arch> getDefaultArchs() {
        return Arrays.asList(Arch.thumbv7, Arch.arm64);
    }

    public void archive() throws IOException {
        config.getLogger().info("Creating IPA in %s", config.getInstallDir());
        config.getInstallDir().mkdirs();
        File tmpDir = new File(config.getInstallDir(), getExecutable() + ".app");
        FileUtils.deleteDirectory(tmpDir);
        tmpDir.mkdirs();
        super.doInstall(tmpDir, getExecutable(), tmpDir);
        prepareInstall(tmpDir);
        packageApplication(tmpDir);
    }

    private void packageApplication(File appDir) throws IOException {
        File ipaFile = new File(config.getInstallDir(), getExecutable() + ".ipa");
        config.getLogger().info("Packaging IPA %s from %s", ipaFile.getName(), appDir.getName());

        File tmpDir = new File(config.getInstallDir(), "ipabuild");
        FileUtils.deleteDirectory(tmpDir);
        tmpDir.mkdirs();

        File payloadDir = new File(tmpDir, "Payload");
        payloadDir.mkdir();
        config.getLogger().info("Copying %s to %s", appDir.getName(), payloadDir);
        new Executor(config.getLogger(), "cp")
                .args("-Rp", appDir, payloadDir)
                .exec();

        File frameworksDir = new File(appDir, "Frameworks");
        if (frameworksDir.exists()){
            String[] swiftLibs = frameworksDir.list(new AndFileFilter(
                    new PrefixFileFilter("libswift"),
                    new SuffixFileFilter(".dylib")));

            if (swiftLibs != null && swiftLibs.length > 0){
                File swiftSupportDir = new File(tmpDir, "SwiftSupport");

                // append OS name subfolder same as XCode does
                if (config.getOs() == OS.ios) {
                    if (config.getArch().isArm()) {
                        swiftSupportDir = new File(swiftSupportDir, "iphoneos");
                    }
                } else {
                    swiftSupportDir = new File(swiftSupportDir, "mac");
                }

                swiftSupportDir.mkdirs();
                copySwiftLibs(Arrays.asList(swiftLibs), swiftSupportDir, false);
            }
        }

        // check app extension and add suport files if needed
        File pluginsDir = new File(appDir, "PlugIns");
        if (pluginsDir.exists()){
            String[] plugins = pluginsDir.list();
            if (plugins != null && plugins.length > 0) {
                final String iStickersExtId = "com.apple.message-payload-provider";
                boolean hasStickers = false;
                for (String p : plugins) {
                    File infoPlistFile = new File(new File(pluginsDir, p), "Info.plist");
                    if (!infoPlistFile.exists())
                        continue;
                    try {
                        NSDictionary infoPlist = (NSDictionary)PropertyListParser.parse(infoPlistFile);
                        NSDictionary extensionDict = (NSDictionary) infoPlist.get("NSExtension");
                        if (extensionDict != null) {
                            hasStickers |= iStickersExtId.equals(extensionDict.get("NSExtensionPointIdentifier").toJavaObject());
                            // other ext types should go here if requried
                        }
                    } catch (Exception ignored) {
                    }
                }

                // provide support
                if (hasStickers) {
                    // for stickers extension
                    config.getLogger().info("Copying support files for Stickers app extension");
                    File xcodePath = new File(ToolchainUtil.findXcodePath());
                    File stickersExtSupportStub = new File(xcodePath, "Platforms/iPhoneOS.platform/Library/" +
                            "Application Support/MessagesApplicationExtensionStub/MessagesApplicationExtensionStub");
                    if (!stickersExtSupportStub.exists()) {
                        throw new FileNotFoundException("Stickers support: MessagesApplicationExtensionStub not found in "
                                + stickersExtSupportStub.getAbsolutePath());
                    }

                    File stickersExtSupportDestDir = new File(tmpDir, "MessagesApplicationExtensionSupport");

                    stickersExtSupportDestDir.mkdir();
                    Files.copy(stickersExtSupportStub.toPath(), new File(stickersExtSupportDestDir, stickersExtSupportStub.getName()).toPath(),
                            StandardCopyOption.COPY_ATTRIBUTES);
                }
            }
        }

        if (config.getWatchKitApp() != null) {
            // copy "WatchKitSupport2/WK: for watch kit app
            config.getLogger().info("Copying support files for WatchKit app extension");
            File xcodePath = new File(ToolchainUtil.findXcodePath());
            File wk = new File(xcodePath, "Platforms/WatchOS.platform/Developer/SDKs/WatchOS.sdk/" +
                    "Library/Application Support/WatchKit/WK");
            if (!wk.exists()) {
                throw new FileNotFoundException("WatchKitSupport support not found in " + wk.getAbsolutePath());
            }

            File watchKitSupport2 = new File(tmpDir, "WatchKitSupport2");
            watchKitSupport2.mkdir();
            Files.copy(wk.toPath(), new File(watchKitSupport2, wk.getName()).toPath(), StandardCopyOption.COPY_ATTRIBUTES);
        }

        config.getLogger().info("Zipping %s to %s", tmpDir, ipaFile);
        new Executor(Logger.NULL_LOGGER, "zip")
                .wd(tmpDir)
                .args("--symlinks", "--recurse-paths", ipaFile, ".")
                .exec();

        config.getLogger().info("Deleting temp dir %s", tmpDir);
        FileUtils.deleteDirectory(tmpDir);
    }

    @Override
    protected void copyResources(File destDir) throws IOException {
        // all xcassets should be processed in one call. otherwise each call will produce Assets.car
        // that will override results of previous one
        List<File> xcassets = new ArrayList<>();
        Resource.Walker walker = new Resource.Walker() {
            @Override
            public boolean processDir(Resource resource, File dir, File destDir) throws IOException {
                if (dir.getName().endsWith(".atlas")) {
                    destDir.mkdirs();

                    ToolchainUtil.textureatlas(config, dir, destDir);
                    return false;
                } else if (dir.getName().endsWith(".xcassets")) {
                    xcassets.add(dir);
                    return false;
                } else {
                    return true;
                }
            }

            @Override
            public void processFile(Resource resource, File file, File destDir) throws IOException {
                if (isDeviceArch(arch, env) && !resource.isSkipPngCrush()
                        && file.getName().toLowerCase().endsWith(".png")) {
                    destDir.mkdirs();
                    File outFile = new File(destDir, file.getName());
                    ToolchainUtil.pngcrush(config, file, outFile);
                } else if (file.getName().toLowerCase().endsWith(".strings")) {
                    destDir.mkdirs();
                    File outFile = new File(destDir, file.getName());
                    ToolchainUtil.compileStrings(config, file, outFile);
                } else if (file.getName().toLowerCase().endsWith(".storyboard")) {
                    destDir.mkdirs();
                    ToolchainUtil.ibtool(config, createPartialInfoPlistFile(file), file, destDir);
                } else if (file.getName().toLowerCase().endsWith(".xib")) {
                    destDir.mkdirs();
                    String fileName = file.getName();
                    fileName = fileName.substring(0, fileName.lastIndexOf('.')) + ".nib";
                    File outFile = new File(destDir, fileName);
                    ToolchainUtil.ibtool(config, createPartialInfoPlistFile(file), file, outFile);
                } else {
                    copyFile(resource, file, destDir);
                }
            }
        };

        for (Resource res : config.getResources()) {
            res.walk(walker, destDir);
        }

        // process all collected xcassets
        if (!xcassets.isEmpty()) {
            ToolchainUtil.actool(config, createPartialInfoPlistFile(xcassets.get(0)), getAppDir(), xcassets);
        }
    }

    private File createPartialInfoPlistFile(File f) throws IOException {
        File tmpFile = File.createTempFile(f.getName() + "_", ".plist", getPartialPListDir());
        tmpFile.delete();
        return tmpFile;
    }

    @Override
    protected File getAppDir() {
        File dir = null;
        if (!config.isSkipInstall()) {
            dir = new File(config.getInstallDir(), getExecutable() + ".app");
            if (!dir.exists()) {
                dir = config.getInstallDir();
            }
        } else {
            dir = new File(config.getTmpDir(), getExecutable() + ".app");
            dir.mkdirs();
        }
        return dir;
    }

    @Override
    protected String getExecutable() {
        if (config.getIosInfoPList() != null) {
            String bundleExecutable = config.getIosInfoPList().getBundleExecutable();
            if (bundleExecutable != null) {
                return bundleExecutable;
            }
        }
        return config.getExecutableName();
    }

    @Override
    protected String getBundleId() {
        if (config.getIosInfoPList() != null) {
            String bundleIdentifier = config.getIosInfoPList().getBundleIdentifier();
            if (bundleIdentifier != null) {
                return bundleIdentifier;
            }
        }
        return config.getMainClass() != null ? config.getMainClass() : config.getExecutableName();
    }

    protected String getMinimumOSVersion() {
        if (config.getIosInfoPList() != null) {
            String minVersion = config.getIosInfoPList().getMinimumOSVersion();
            if (minVersion != null) {
                return minVersion;
            }
        }
        return config.getOs().getMinVersion();
    }

    private void putIfAbsent(NSDictionary dict, String key, String value) {
        if (dict.objectForKey(key) == null) {
            dict.put(key, value);
        }
    }

    protected void customizeInfoPList(NSDictionary dict) {
        if (isSimulatorArch(arch, env)) {
            dict.put("CFBundleSupportedPlatforms", new NSArray(new NSString("iPhoneSimulator")));
        } else {
            dict.put("CFBundleSupportedPlatforms", new NSArray(new NSString("iPhoneOS")));
            dict.put("DTPlatformVersion", sdk.getPlatformVersion());
            dict.put("DTPlatformBuild", sdk.getPlatformBuild());
            dict.put("DTSDKBuild", sdk.getBuild());

            // Validation fails without DTXcode and DTXcodeBuild. Try to read
            // them from the installed Xcode.
            try {
                File versionPListFile = new File(new File(ToolchainUtil.findXcodePath()).getParentFile(),
                        "version.plist");
                NSDictionary versionPList = (NSDictionary) PropertyListParser.parse(versionPListFile);
                File xcodeInfoPListFile = new File(new File(ToolchainUtil.findXcodePath()).getParentFile(),
                        "Info.plist");
                NSDictionary xcodeInfoPList = (NSDictionary) PropertyListParser.parse(xcodeInfoPListFile);
                NSString dtXcodeBuild = (NSString) versionPList.objectForKey("ProductBuildVersion");
                if (dtXcodeBuild == null) {
                    throw new NoSuchElementException("No ProductBuildVersion in " + versionPListFile.getAbsolutePath());
                }
                NSString dtXcode = (NSString) xcodeInfoPList.objectForKey("DTXcode");
                if (dtXcode == null) {
                    throw new NoSuchElementException("No DTXcode in " + xcodeInfoPListFile.getAbsolutePath());
                }
                putIfAbsent(dict, "DTXcode", dtXcode.toString());
                putIfAbsent(dict, "DTXcodeBuild", dtXcodeBuild.toString());
            } catch (Exception e) {
                config.getLogger()
                        .warn("Failed to read DTXcodeBuild/DTXcode from current Xcode install. Will use fake values. (%s: %s)",
                                e.getClass().getName(), e.getMessage());
            }
            // Fake Xcode 6.1.1 values if the above fails.
            putIfAbsent(dict, "DTXcode", "0611");
            putIfAbsent(dict, "DTXcodeBuild", "6A2008a");
        }
    }

    protected void createInfoPList(File dir) throws IOException {
        NSDictionary dict = new NSDictionary();
        if (config.getIosInfoPList() != null && config.getIosInfoPList().getDictionary() != null) {
            NSDictionary infoPListDict = config.getIosInfoPList().getDictionary();
            for (String key : infoPListDict.allKeys()) {
                dict.put(key, infoPListDict.objectForKey(key));
            }
        } else {
            dict.put("CFBundleVersion", "1.0");
            dict.put("CFBundleExecutable", config.getExecutableName());
            dict.put("CFBundleName", config.getExecutableName());
            dict.put("CFBundleIdentifier", getBundleId());
            dict.put("CFBundlePackageType", "APPL");
            dict.put("LSRequiresIPhoneOS", true);
            NSObject supportedDeviceFamilies = sdk.getDefaultProperty("SUPPORTED_DEVICE_FAMILIES");
            if (supportedDeviceFamilies != null) {
                // SUPPORTED_DEVICE_FAMILIES is either a NSString of comma
                // separated numbers
                // or an NSArray with NSStrings. UIDeviceFamily values should be
                // NSNumbers.
                NSArray families = null;
                if (supportedDeviceFamilies instanceof NSString) {
                    NSString defFamilies = (NSString) supportedDeviceFamilies;
                    String[] parts = defFamilies.toString().split(",");
                    families = new NSArray(parts.length);
                    for (int i = 0; i < families.count(); i++) {
                        families.setValue(i, new NSNumber(parts[i].trim()));
                    }
                } else {
                    NSArray defFamilies = (NSArray) supportedDeviceFamilies;
                    families = new NSArray(defFamilies.count());
                    for (int i = 0; i < families.count(); i++) {
                        families.setValue(i, new NSNumber(defFamilies.objectAtIndex(i).toString()));
                    }
                }
                dict.put("UIDeviceFamily", families);
            }
            dict.put("UISupportedInterfaceOrientations", new NSArray(
                    new NSString("UIInterfaceOrientationPortrait"),
                    new NSString("UIInterfaceOrientationLandscapeLeft"),
                    new NSString("UIInterfaceOrientationLandscapeRight"),
                    new NSString("UIInterfaceOrientationPortraitUpsideDown")
                    ));
            dict.put("UISupportedInterfaceOrientations~ipad", new NSArray(
                    new NSString("UIInterfaceOrientationPortrait"),
                    new NSString("UIInterfaceOrientationLandscapeLeft"),
                    new NSString("UIInterfaceOrientationLandscapeRight"),
                    new NSString("UIInterfaceOrientationPortraitUpsideDown")
                    ));
            dict.put("UIRequiredDeviceCapabilities", new NSArray(new NSString("armv7")));
        }

        dict.put("DTPlatformName", sdk.getPlatformName());
        dict.put("DTSDKName", sdk.getCanonicalName());

        for (File f : FileUtils.listFiles(getPartialPListDir(), new String[] {"plist"}, false)) {
            try {
                NSDictionary d = (NSDictionary) PropertyListParser.parse(f);
                dict.putAll(d);
            } catch (Exception e) {
                throw new CompilerException(e);
            }
        }

        if (dict.objectForKey("MinimumOSVersion") == null) {
            // This is required
            dict.put("MinimumOSVersion", config.getOs().getMinVersion());
        }

        customizeInfoPList(dict);

        /*
         * Make sure CFBundleShortVersionString and CFBundleVersion are at the
         * top of the Info.plist file to avoid the "Could not hardlink copy"
         * problem when launching on the simulator. com.dd.plist maintains the
         * insertion order of keys so we rebuild the dictionary here and make
         * sure those two keys are inserted first. See #771.
         */
        NSDictionary newDict = new NSDictionary();
        if (dict.objectForKey("CFBundleShortVersionString") != null) {
            newDict.put("CFBundleShortVersionString", dict.objectForKey("CFBundleShortVersionString"));
            dict.remove("CFBundleShortVersionString");
        }
        if (dict.objectForKey("CFBundleVersion") != null) {
            newDict.put("CFBundleVersion", dict.objectForKey("CFBundleVersion"));
            dict.remove("CFBundleVersion");
        }
        for (String key : dict.allKeys()) {
            newDict.put(key, dict.objectForKey(key));
        }

        File tmpInfoPlist = new File(config.getTmpDir(), "Info.plist");
        PropertyListParser.saveAsBinary(newDict, tmpInfoPlist);

        config.getLogger().info("Installing Info.plist to %s", dir);
        FileUtils.copyFile(tmpInfoPlist, new File(dir, tmpInfoPlist.getName()));
    }

    public void init(Config config) {
        super.init(config);

        if (config.getArch() == null) {
            arch = Arch.thumbv7;
        } else {
            if (!isSimulatorArch(config.getArch(), config.getEnv()) && !isDeviceArch(config.getArch(), config.getEnv())) {
                throw new IllegalArgumentException("Arch '" + config.getArch()
                        + "' is unsupported for iOS target");
            }
            arch = config.getArch();
        }
        env = config.getEnv();

        if (config.getIosInfoPList() != null) {
            config.getIosInfoPList().parse(config.getProperties());
        }

        if (isDeviceArch(arch, env)) {
            if (!config.isSkipLinking() && !config.isIosSkipSigning()) {
                signIdentity = config.getIosSignIdentity();
                provisioningProfile = config.getIosProvisioningProfile();
                String bundleId = "*";
                if (config.getIosInfoPList() != null && config.getIosInfoPList().getBundleIdentifier() != null) {
                    bundleId = config.getIosInfoPList().getBundleIdentifier();
                }

                if (signIdentity == null && provisioningProfile == null) {
                    // both identity and provisioningProfile are set to auto, start with picking profile s
                    Pair<SigningIdentity, ProvisioningProfile> pair = ProvisioningProfile.find(ProvisioningProfile.list(), SigningIdentity.list(SigningIdentity.REGEX_MATCH_FOR_IOS), bundleId);
                    signIdentity = pair.getLeft();
                    provisioningProfile = pair.getRight();
                } else if (signIdentity == null) {
                    // provisioning profile was specified, need to find a signing identity that matches it
                    signIdentity = SigningIdentity.find(SigningIdentity.list(), SigningIdentity.REGEX_MATCH_FOR_IOS, provisioningProfile);
                } else if (provisioningProfile == null) {
                    // find profile that matches identity and bundle id
                    provisioningProfile = ProvisioningProfile.find(ProvisioningProfile.list(), signIdentity, bundleId);
                }
            }
        }

        String sdkVersion = config.getIosSdkVersion();
        List<SDK> sdks = getSDKs();
        if (sdkVersion == null) {
            if (sdks.isEmpty()) {
                throw new IllegalArgumentException("No " + (isDeviceArch(arch, env) ? "device" : "simulator")
                        + " SDKs installed");
            }
            Collections.sort(sdks);
            this.sdk = sdks.get(sdks.size() - 1);
        } else {
            for (SDK sdk : sdks) {
                if (sdk.getVersion().equals(sdkVersion)) {
                    this.sdk = sdk;
                    break;
                }
            }
            if (sdk == null) {
                throw new IllegalArgumentException("No SDK found matching version string " + sdkVersion);
            }
        }

        entitlementsPList = config.getIosEntitlementsPList();

        partialPListDir = new File(config.getTmpDir(), "partial-plists");
        try {
            //It may not exist, as we might clean the tmp dir depending on the configuration
            if(partialPListDir.exists()){
                FileUtils.cleanDirectory(partialPListDir);
            }
        } catch (IOException e) {
            throw new CompilerException(e);
        }
    }

    @Override
    public OS getOs() {
        return OS.ios;
    }

    @Override
    public boolean canLaunchInPlace() {
        return false;
    }

    /**
     * Copies the dSYM and the executable to {@code ~/Library/Developer/Xcode/
     * DerivedData/RoboVM/Build/Products/<appname>_<timestamp>/}.
     */
    private void copyToIndexedDir(File dir, String executable, File dsymDir, File exePath) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        final File indexedDir = new File(System.getProperty("user.home"),
                "Library/Developer/Xcode/DerivedData/RoboVM/Build/Products/"
                        + FilenameUtils.removeExtension(dir.getName()) + "_"
                        + sdf.format(new Date()));
        indexedDir.mkdirs();
        File indexedDSymDir = new File(indexedDir, dsymDir.getName());
        File indexedAppDir = new File(indexedDir, dir.getName());
        indexedAppDir.mkdirs();

        try {
            // No need to copy the whole .app folder. Just the exe
            // is enough to make symbolication happy.
            FileUtils.copyFile(exePath, new File(indexedAppDir, executable));
        } catch (IOException e) {
            config.getLogger().error("Failed to copy %s to indexed dir %s: %s",
                    exePath.getAbsolutePath(),
                    indexedAppDir.getAbsolutePath(), e.getMessage());
        }

        try {
            FileUtils.copyDirectory(dsymDir, indexedDSymDir);
        } catch (IOException e) {
            config.getLogger().error("Failed to copy %s to indexed dir %s: %s",
                    dsymDir.getAbsolutePath(),
                    indexedDir.getAbsolutePath(), e.getMessage());
        }

        // Now do some cleanup and delete all but the 3 most recent dirs
        List<File> dirs = new ArrayList<>(Arrays.asList(indexedDir.getParentFile().listFiles((FileFilter)
                new AndFileFilter(
                        new PrefixFileFilter(FilenameUtils.removeExtension(dir.getName())),
                        new RegexFileFilter(".*_\\d{14}")))));
        Collections.sort(dirs, new Comparator<File>() {
            public int compare(File o1, File o2) {
                return Long.compare(o1.lastModified(), o2.lastModified());
            }
        });
        if (dirs.size() > 3) {
            for (File f : dirs.subList(0, dirs.size() - 3)) {
                try {
                    FileUtils.deleteDirectory(f);
                } catch (IOException e) {
                    config.getLogger().error("Failed to delete diretcory %s",
                            f.getAbsolutePath(), e.getMessage());
                }
            }
        }
    }
}
