/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2017 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
package android.icu.impl.number.parse;

import android.icu.impl.StringSegment;

/**
 * A Matcher used only for post-process validation, not for consuming characters at runtime.
 * @hide Only a subset of ICU is exposed in Android
 */
public abstract class ValidationMatcher implements NumberParseMatcher {

    @Override
    public boolean match(StringSegment segment, ParsedNumber result) {
        return false;
    }

    @Override
    public boolean smokeTest(StringSegment segment) {
        return false;
    }

}
