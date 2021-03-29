/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2018 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
package android.icu.impl.number;

import android.icu.impl.StandardPlural;

/**
 * This is *not* a modifier; rather, it is an object that can return modifiers
 * based on given parameters.
 *
 * @author sffc
 * @hide Only a subset of ICU is exposed in Android
 */
public interface ModifierStore {
    /**
     * Returns a Modifier with the given parameters (best-effort).
     */
    Modifier getModifier(int signum, StandardPlural plural);
}
