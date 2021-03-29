/*
 * Copyright (C) 2010 The Android Open Source Project
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

package libcore.net;

import dalvik.annotation.compat.UnsupportedAppUsage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Utilities for dealing with MIME types.
 * Used to implement java.net.URLConnection and android.webkit.MimeTypeMap.
 * @hide
 */
@libcore.api.CorePlatformApi
public final class MimeUtils {

    private static final Pattern splitPattern = Pattern.compile("\\s+");

    /**
     * Note: These maps only contain lowercase keys/values, regarded as the
     * {@link #canonicalize(String) canonical form}.
     *
     * <p>This is the case for both extensions and MIME types. The mime.types
     * data file contains examples of mixed-case MIME types, but some applications
     * use the lowercase version of these same types. RFC 2045 section 2 states
     * that MIME types are case insensitive.
     */
    private static final Map<String, String> mimeTypeToExtensionMap = new HashMap<>();
    private static final Map<String, String> extensionToMimeTypeMap = new HashMap<>();

    static {
        parseTypes("mime.types");
        parseTypes("android.mime.types");
    }

    private static void parseTypes(String resource) {
        try (BufferedReader r = new BufferedReader(
                new InputStreamReader(MimeUtils.class.getResourceAsStream(resource)))) {
            String line;
            while ((line = r.readLine()) != null) {
                int commentPos = line.indexOf('#');
                if (commentPos >= 0) {
                    line = line.substring(0, commentPos);
                }
                line = line.trim();
                if (line.equals("")) {
                    continue;
                }

                final String[] split = splitPattern.split(line);
                final String mimeType = canonicalize(split[0]);
                if (!allowedInMap(mimeType)) {
                    throw new IllegalArgumentException(
                            "Invalid mimeType " + mimeType + " in: " + line);
                }
                for (int i = 1; i < split.length; i++) {
                    String extension = canonicalize(split[i]);
                    if (!allowedInMap(extension)) {
                        throw new IllegalArgumentException(
                                "Invalid extension " + extension + " in: " + line);
                    }

                    // Normally the first MIME type definition wins, and the
                    // last extension definition wins. However, a file can
                    // override a MIME type definition by adding the "!" suffix
                    // to an extension.

                    if (extension.endsWith("!")) {
                        extension = extension.substring(0, extension.length() - 1);

                        // Overriding MIME definition wins
                        mimeTypeToExtensionMap.put(mimeType, extension);
                    } else {
                        // First MIME definition wins
                        if (!mimeTypeToExtensionMap.containsKey(mimeType)) {
                            mimeTypeToExtensionMap.put(mimeType, extension);
                        }
                    }

                    // Last extension definition wins
                    extensionToMimeTypeMap.put(extension, mimeType);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse " + resource, e);
        }
    }

    private MimeUtils() {
    }

    /**
     * Returns the canonical (lowercase) form of the given extension or MIME type.
     */
    private static String canonicalize(String s) {
        return s.toLowerCase(Locale.ROOT);
    }

    /**
     * Checks whether the given extension or MIME type might be valid and
     * therefore may appear in the mimeType <-> extension maps.
     */
    private static boolean allowedInMap(String s) {
        return s != null && !s.isEmpty();
    }

    /**
     * Returns true if the given case insensitive MIME type has an entry in the map.
     * @param mimeType A MIME type (i.e. text/plain)
     * @return True if a extension has been registered for
     * the given case insensitive MIME type.
     */
    @libcore.api.CorePlatformApi
    public static boolean hasMimeType(String mimeType) {
        return (guessExtensionFromMimeType(mimeType) != null);
    }

    /**
     * Returns the MIME type for the given case insensitive file extension.
     * @param extension A file extension without the leading '.'
     * @return The MIME type has been registered for
     * the given case insensitive file extension or null if there is none.
     */
    @UnsupportedAppUsage
    @libcore.api.CorePlatformApi
    public static String guessMimeTypeFromExtension(String extension) {
        if (!allowedInMap(extension)) {
            return null;
        }
        extension = canonicalize(extension);
        return extensionToMimeTypeMap.get(extension);
    }

    /**
     * Returns true if the given case insensitive extension has a registered MIME type.
     * @param extension A file extension without the leading '.'
     * @return True if a MIME type has been registered for
     * the given case insensitive file extension.
     */
    @libcore.api.CorePlatformApi
    public static boolean hasExtension(String extension) {
        return (guessMimeTypeFromExtension(extension) != null);
    }

    /**
     * Returns the registered extension for the given case insensitive MIME type. Note that some
     * MIME types map to multiple extensions. This call will return the most
     * common extension for the given MIME type.
     * @param mimeType A MIME type (i.e. text/plain)
     * @return The extension has been registered for
     * the given case insensitive MIME type or null if there is none.
     */
    @UnsupportedAppUsage
    @libcore.api.CorePlatformApi
    public static String guessExtensionFromMimeType(String mimeType) {
        if (!allowedInMap(mimeType)) {
            return null;
        }
        mimeType = canonicalize(mimeType);
        return mimeTypeToExtensionMap.get(mimeType);
    }
}
