/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package libcore.timezone;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Constants and logic associated with the time zone data version file.
 * @hide
 */
@libcore.api.CorePlatformApi
public class TzDataSetVersion {

    // Remove from CorePlatformApi when all users in platform code are removed. http://b/123398797
    /**
     * The name typically given to the {@link TzDataSetVersion} file. See
     * {@link TzDataSetVersion#readFromFile(File)}.
     */
    @libcore.api.CorePlatformApi
    public static final String DEFAULT_FILE_NAME = "tz_version";

    /**
     * The major tz data format version supported by this device.
     * Increment this for non-backwards compatible changes to the tz data format. Reset the minor
     * version to 1 when doing so.
     */
    // @VisibleForTesting : Keep this inline-able: it is used from CTS tests.
    public static final int CURRENT_FORMAT_MAJOR_VERSION = 3; // Android Q

    /**
     * Returns the major tz data format version supported by this device.
     */
    @libcore.api.CorePlatformApi
    public static int currentFormatMajorVersion() {
        return CURRENT_FORMAT_MAJOR_VERSION;
    }

    /**
     * The minor tz data format version supported by this device. Increment this for
     * backwards-compatible changes to the tz data format.
     */
    // @VisibleForTesting : Keep this inline-able: it is used from CTS tests.
    public static final int CURRENT_FORMAT_MINOR_VERSION = 1;

    /**
     * Returns the minor tz data format version supported by this device.
     */
    @libcore.api.CorePlatformApi
    public static int currentFormatMinorVersion() {
        return CURRENT_FORMAT_MINOR_VERSION;
    }

    /** The full major + minor tz data format version for this device. */
    private static final String FULL_CURRENT_FORMAT_VERSION_STRING =
            toFormatVersionString(CURRENT_FORMAT_MAJOR_VERSION, CURRENT_FORMAT_MINOR_VERSION);

    private static final int FORMAT_VERSION_STRING_LENGTH =
            FULL_CURRENT_FORMAT_VERSION_STRING.length();
    private static final Pattern FORMAT_VERSION_PATTERN = Pattern.compile("(\\d{3})\\.(\\d{3})");

    /** A pattern that matches the IANA rules value of a rules update. e.g. "2016g" */
    private static final Pattern RULES_VERSION_PATTERN = Pattern.compile("(\\d{4}\\w)");

    private static final int RULES_VERSION_LENGTH = 5;

    /** A pattern that matches the revision of a rules update. e.g. "001" */
    private static final Pattern REVISION_PATTERN = Pattern.compile("(\\d{3})");

    private static final int REVISION_LENGTH = 3;

    /**
     * The length of a well-formed tz data set version file:
     * {Format version}|{Rule version}|{Revision}
     */
    private static final int TZ_DATA_VERSION_FILE_LENGTH = FORMAT_VERSION_STRING_LENGTH + 1
            + RULES_VERSION_LENGTH
            + 1 + REVISION_LENGTH;

    private static final Pattern TZ_DATA_VERSION_FILE_PATTERN = Pattern.compile(
            FORMAT_VERSION_PATTERN.pattern() + "\\|"
                    + RULES_VERSION_PATTERN.pattern() + "\\|"
                    + REVISION_PATTERN.pattern()
                    + ".*" /* ignore trailing */);

    public final int formatMajorVersion;
    public final int formatMinorVersion;

    // Remove from CorePlatformApi when all users in platform code are removed. http://b/123398797
    @libcore.api.CorePlatformApi
    public final String rulesVersion;

    public final int revision;

    @libcore.api.CorePlatformApi
    public TzDataSetVersion(int formatMajorVersion, int formatMinorVersion, String rulesVersion,
            int revision) throws TzDataSetException {
        this.formatMajorVersion = validate3DigitVersion(formatMajorVersion);
        this.formatMinorVersion = validate3DigitVersion(formatMinorVersion);
        if (!RULES_VERSION_PATTERN.matcher(rulesVersion).matches()) {
            throw new TzDataSetException("Invalid rulesVersion: " + rulesVersion);
        }
        this.rulesVersion = rulesVersion;
        this.revision = validate3DigitVersion(revision);
    }

    // VisibleForTesting
    public static TzDataSetVersion fromBytes(byte[] bytes) throws TzDataSetException {
        String tzDataVersion = new String(bytes, StandardCharsets.US_ASCII);
        try {
            Matcher matcher = TZ_DATA_VERSION_FILE_PATTERN.matcher(tzDataVersion);
            if (!matcher.matches()) {
                throw new TzDataSetException(
                        "Invalid tz data version string: \"" + tzDataVersion + "\"");
            }
            String formatMajorVersion = matcher.group(1);
            String formatMinorVersion = matcher.group(2);
            String rulesVersion = matcher.group(3);
            String revision = matcher.group(4);
            return new TzDataSetVersion(
                    from3DigitVersionString(formatMajorVersion),
                    from3DigitVersionString(formatMinorVersion),
                    rulesVersion,
                    from3DigitVersionString(revision));
        } catch (IndexOutOfBoundsException e) {
            // The use of the regexp above should make this impossible.
            throw new TzDataSetException(
                    "tz data version string too short: \"" + tzDataVersion + "\"");
        }
    }

    // Remove from CorePlatformApi when all users in platform code are removed. http://b/123398797
    @libcore.api.CorePlatformApi
    public static TzDataSetVersion readFromFile(File file) throws IOException, TzDataSetException {
        byte[] versionBytes = readBytes(file, TzDataSetVersion.TZ_DATA_VERSION_FILE_LENGTH);
        return fromBytes(versionBytes);
    }

    // Remove from CorePlatformApi when all users in platform code are removed. http://b/123398797
    @libcore.api.CorePlatformApi
    public byte[] toBytes() {
        return toBytes(formatMajorVersion, formatMinorVersion, rulesVersion, revision);
    }

    private static byte[] toBytes(
            int majorFormatVersion, int minorFormatVerison, String rulesVersion, int revision) {
        return (toFormatVersionString(majorFormatVersion, minorFormatVerison)
                + "|" + rulesVersion + "|" + to3DigitVersionString(revision))
                .getBytes(StandardCharsets.US_ASCII);
    }

    @libcore.api.CorePlatformApi
    public static boolean isCompatibleWithThisDevice(TzDataSetVersion tzDataVersion) {
        return (CURRENT_FORMAT_MAJOR_VERSION == tzDataVersion.formatMajorVersion)
                && (CURRENT_FORMAT_MINOR_VERSION <= tzDataVersion.formatMinorVersion);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TzDataSetVersion that = (TzDataSetVersion) o;

        if (formatMajorVersion != that.formatMajorVersion) {
            return false;
        }
        if (formatMinorVersion != that.formatMinorVersion) {
            return false;
        }
        if (revision != that.revision) {
            return false;
        }
        return rulesVersion.equals(that.rulesVersion);
    }

    @Override
    public int hashCode() {
        int result = formatMajorVersion;
        result = 31 * result + formatMinorVersion;
        result = 31 * result + rulesVersion.hashCode();
        result = 31 * result + revision;
        return result;
    }

    @Override
    public String toString() {
        return "TzDataSetVersion{" +
                "formatMajorVersion=" + formatMajorVersion +
                ", formatMinorVersion=" + formatMinorVersion +
                ", rulesVersion='" + rulesVersion + '\'' +
                ", revision=" + revision +
                '}';
    }

    /**
     * Returns a version as a zero-padded three-digit String value.
     */
    private static String to3DigitVersionString(int version) {
        try {
            return String.format(Locale.ROOT, "%03d", validate3DigitVersion(version));
        } catch (TzDataSetException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Validates and parses a zero-padded three-digit String value.
     */
    private static int from3DigitVersionString(String versionString) throws TzDataSetException {
        final String parseErrorMessage = "versionString must be a zero padded, 3 digit, positive"
                + " decimal integer";
        if (versionString.length() != 3) {
            throw new TzDataSetException(parseErrorMessage);
        }
        try {
            int version = Integer.parseInt(versionString);
            return validate3DigitVersion(version);
        } catch (NumberFormatException e) {
            throw new TzDataSetException(parseErrorMessage, e);
        }
    }

    private static int validate3DigitVersion(int value) throws TzDataSetException {
        // 0 is allowed but is reserved for testing.
        if (value < 0 || value > 999) {
            throw new TzDataSetException("Expected 0 <= value <= 999, was " + value);
        }
        return value;
    }

    private static String toFormatVersionString(int majorFormatVersion, int minorFormatVersion) {
        return to3DigitVersionString(majorFormatVersion)
                + "." + to3DigitVersionString(minorFormatVersion);
    }

    /**
     * Reads up to {@code maxBytes} bytes from the specified file. The returned array can be
     * shorter than {@code maxBytes} if the file is shorter.
     */
    private static byte[] readBytes(File file, int maxBytes) throws IOException {
        if (maxBytes <= 0) {
            throw new IllegalArgumentException("maxBytes ==" + maxBytes);
        }

        try (FileInputStream in = new FileInputStream(file)) {
            byte[] max = new byte[maxBytes];
            int bytesRead = in.read(max, 0, maxBytes);
            byte[] toReturn = new byte[bytesRead];
            System.arraycopy(max, 0, toReturn, 0, bytesRead);
            return toReturn;
        }
    }

    /**
     * A checked exception used in connection with time zone data sets.
     */
    @libcore.api.CorePlatformApi
    public static class TzDataSetException extends Exception {

        @libcore.api.CorePlatformApi
        public TzDataSetException(String message) {
            super(message);
        }

        @libcore.api.CorePlatformApi
        public TzDataSetException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
