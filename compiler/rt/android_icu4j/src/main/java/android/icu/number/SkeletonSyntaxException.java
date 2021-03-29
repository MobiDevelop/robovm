/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2018 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
package android.icu.number;

/**
 * Exception used for illegal number skeleton strings.
 *
 * @see NumberFormatter
 * @hide Only a subset of ICU is exposed in Android
 * @hide draft / provisional / internal are hidden on Android
 */
public class SkeletonSyntaxException extends IllegalArgumentException {
    private static final long serialVersionUID = 7733971331648360554L;

    /**
     * Construct a new SkeletonSyntaxException with information about the token at the point of failure.
     *
     * @see NumberFormatter
     * @hide draft / provisional / internal are hidden on Android
     */
    public SkeletonSyntaxException(String message, CharSequence token) {
        super("Syntax error in skeleton string: " + message + ": " + token);
    }

    /**
     * Construct a new SkeletonSyntaxException with information about the token at the point of failure.
     *
     * @see NumberFormatter
     * @hide draft / provisional / internal are hidden on Android
     */
    public SkeletonSyntaxException(String message, CharSequence token, Throwable cause) {
        super("Syntax error in skeleton string: " + message + ": " + token, cause);
    }
}
