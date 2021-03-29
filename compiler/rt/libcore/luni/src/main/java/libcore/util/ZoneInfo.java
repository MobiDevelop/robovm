/*
 * Copyright (C) 2007 The Android Open Source Project
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
/*
 * Elements of the WallTime class are a port of Bionic's localtime.c to Java. That code had the
 * following header:
 *
 * This file is in the public domain, so clarified as of
 * 1996-06-05 by Arthur David Olson.
 */
package libcore.util;

import dalvik.annotation.compat.UnsupportedAppUsage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import libcore.io.BufferIterator;
import libcore.timezone.ZoneInfoDB;

/**
 * Our concrete TimeZone implementation, backed by zoneinfo data.
 *
 * <p>This reads time zone information from a binary file stored on the platform. The binary file
 * is essentially a single file containing compacted versions of all the tzfiles produced by the
 * zone info compiler (zic) tool (see {@code man 5 tzfile} for details of the format and
 * {@code man 8 zic}) and an index by long name, e.g. Europe/London.
 *
 * <p>The compacted form is created by {@code external/icu/tools/ZoneCompactor.java} and is used
 * by both this and Bionic. {@link ZoneInfoDB} is responsible for mapping the binary file, and
 * reading the index and creating a {@link BufferIterator} that provides access to an entry for a
 * specific file. This class is responsible for reading the data from that {@link BufferIterator}
 * and storing it a representation to support the {@link TimeZone} and {@link GregorianCalendar}
 * implementations. See {@link ZoneInfo#readTimeZone(String, BufferIterator, long)}.
 *
 * <p>The main difference between {@code tzfile} and the compacted form is that the
 * {@code struct ttinfo} only uses a single byte for {@code tt_isdst} and {@code tt_abbrind}.
 *
 * <p>This class does not use all the information from the {@code tzfile}; it uses:
 * {@code tzh_timecnt} and the associated transition times and type information. For each type
 * (described by {@code struct ttinfo}) it uses {@code tt_gmtoff} and {@code tt_isdst}. Note, that
 * the definition of {@code struct ttinfo} uses {@code long}, and {@code int} but they do not have
 * the same meaning as Java. The prose following the definition makes it clear that the {@code long}
 * is 4 bytes and the {@code int} fields are 1 byte.
 *
 * <p>As the data uses 32 bits to store the time in seconds the time range is limited to roughly
 * 69 years either side of the epoch (1st Jan 1970 00:00:00) that means that it cannot handle any
 * dates before 1900 and after 2038. There is an extended version of the table that uses 64 bits
 * to store the data but that information is not used by this.
 *
 * <p>This class should be in libcore.timezone but this class is Serializable so cannot
 * be moved there without breaking apps that have (for some reason) serialized TimeZone objects.
 *
 * @hide - used to implement TimeZone
 */
@libcore.api.CorePlatformApi
public final class ZoneInfo extends TimeZone {
    private static final long MILLISECONDS_PER_DAY = 24 * 60 * 60 * 1000;
    private static final long MILLISECONDS_PER_400_YEARS =
            MILLISECONDS_PER_DAY * (400 * 365 + 100 - 3);

    private static final long UNIX_OFFSET = 62167219200000L;

    private static final int[] NORMAL = new int[] {
        0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334,
    };

    private static final int[] LEAP = new int[] {
        0, 31, 60, 91, 121, 152, 182, 213, 244, 274, 305, 335,
    };

    // Proclaim serialization compatibility with pre-OpenJDK AOSP
    static final long serialVersionUID = -4598738130123921552L;

    /**
     * The (best guess) non-DST offset used "today". It is stored in milliseconds.
     * See also {@link #mOffsets} which holds values relative to this value, albeit in seconds.
     */
    private int mRawOffset;

    /**
     * The earliest non-DST offset for the zone. It is stored in milliseconds and is absolute, i.e.
     * it is not relative to mRawOffset.
     */
    private final int mEarliestRawOffset;

    /**
     * Implements {@link #useDaylightTime()}
     *
     * <p>True if the transition active at the time this instance was created, or future
     * transitions support DST. It is possible that caching this value at construction time and
     * using it for the lifetime of the instance does not match the contract of the
     * {@link TimeZone#useDaylightTime()} method but it appears to be what the RI does and that
     * method is not particularly useful when it comes to historical or future times as it does not
     * allow the time to be specified.
     *
     * <p>When this is false then {@link #mDstSavings} will be 0.
     *
     * @see #mDstSavings
     */
    private final boolean mUseDst;

    /**
     * Implements {@link #getDSTSavings()}
     *
     * <p>This should be final but is not because it may need to be fixed up by
     * {@link #readObject(ObjectInputStream)} to correct an inconsistency in the previous version
     * of the code whereby this was set to a non-zero value even though DST was not actually used.
     *
     * @see #mUseDst
     */
    private int mDstSavings;

    /**
     * The times (in seconds) at which the offsets changes for any reason, whether that is a change
     * in the offset from UTC or a change in the DST.
     *
     * <p>These times are pre-calculated externally from a set of rules (both historical and
     * future) and stored in a file from which {@link ZoneInfo#readTimeZone(String, BufferIterator,
     * long)} reads the data. That is quite different to {@link java.util.SimpleTimeZone}, which has
     * essentially human readable rules (e.g. DST starts at 01:00 on the first Sunday in March and
     * ends at 01:00 on the last Sunday in October) that can be used to determine the DST transition
     * times across a number of years
     *
     * <p>In terms of {@link ZoneInfo tzfile} structure this array is of length {@code tzh_timecnt}
     * and contains the times in seconds converted to long to make them safer to use.
     *
     * <p>They are stored in order from earliest (lowest) time to latest (highest). A transition is
     * identified by its index within this array. A transition {@code T} is active at a specific
     * time {@code X} if {@code T} is the highest transition whose time is less than or equal to
     * {@code X}.
     *
     * @see #mTypes
     */
    @UnsupportedAppUsage
    private final long[] mTransitions;

    /**
     * The type of the transition, where type is a pair consisting of the offset and whether the
     * offset includes DST or not.
     *
     * <p>Each transition in {@link #mTransitions} has an associated type in this array at the same
     * index. The type is an index into the arrays {@link #mOffsets} and {@link #mIsDsts} that each
     * contain one part of the pair.
     *
     * <p>In the {@link ZoneInfo tzfile} structure the type array only contains unique instances of
     * the {@code struct ttinfo} to save space and each type may be referenced by multiple
     * transitions. However, the type pairs stored in this class are not guaranteed unique because
     * they do not include the {@code tt_abbrind}, which is the abbreviated identifier to use for
     * the time zone after the transition.
     *
     * @see #mTransitions
     * @see #mOffsets
     * @see #mIsDsts
     */
    private final byte[] mTypes;

    /**
     * The offset parts of the transition types, in seconds.
     *
     * <p>These are actually a delta to the {@link #mRawOffset}. So, if the offset is say +7200
     * seconds and {@link #mRawOffset} is say +3600 then this will have a value of +3600.
     *
     * <p>The offset in milliseconds can be computed using:
     * {@code mRawOffset + mOffsets[type] * 1000}
     *
     * @see #mTypes
     * @see #mIsDsts
     */
    private final int[] mOffsets;

    /**
     * Specifies whether an associated offset includes DST or not.
     *
     * <p>Each entry in here is 1 if the offset at the same index in {@link #mOffsets} includes DST
     * and 0 otherwise.
     *
     * @see #mTypes
     * @see #mOffsets
     */
    private final byte[] mIsDsts;

    public static ZoneInfo readTimeZone(String id, BufferIterator it, long currentTimeMillis)
            throws IOException {
        // Variable names beginning tzh_ correspond to those in "tzfile.h".

        // Check tzh_magic.
        int tzh_magic = it.readInt();
        if (tzh_magic != 0x545a6966) { // "TZif"
            throw new IOException("Timezone id=" + id + " has an invalid header=" + tzh_magic);
        }

        // Skip the uninteresting part of the header.
        it.skip(28);

        // Read the sizes of the arrays we're about to read.
        int tzh_timecnt = it.readInt();
        // Arbitrary ceiling to prevent allocating memory for corrupt data.
        // 2 per year with 2^32 seconds would give ~272 transitions.
        final int MAX_TRANSITIONS = 2000;
        if (tzh_timecnt < 0 || tzh_timecnt > MAX_TRANSITIONS) {
            throw new IOException(
                    "Timezone id=" + id + " has an invalid number of transitions=" + tzh_timecnt);
        }

        int tzh_typecnt = it.readInt();
        final int MAX_TYPES = 256;
        if (tzh_typecnt < 1) {
            throw new IOException("ZoneInfo requires at least one type "
                    + "to be provided for each timezone but could not find one for '" + id + "'");
        } else if (tzh_typecnt > MAX_TYPES) {
            throw new IOException(
                    "Timezone with id " + id + " has too many types=" + tzh_typecnt);
        }

        it.skip(4); // Skip tzh_charcnt.

        // Transitions are signed 32 bit integers, but we store them as signed 64 bit
        // integers since it's easier to compare them against 64 bit inputs (see getOffset
        // and isDaylightTime) with much less risk of an overflow in our calculations.
        //
        // The alternative of checking the input against the first and last transition in
        // the array is far more awkward and error prone.
        int[] transitions32 = new int[tzh_timecnt];
        it.readIntArray(transitions32, 0, transitions32.length);

        long[] transitions64 = new long[tzh_timecnt];
        for (int i = 0; i < tzh_timecnt; ++i) {
            transitions64[i] = transitions32[i];
            if (i > 0 && transitions64[i] <= transitions64[i - 1]) {
                throw new IOException(
                        id + " transition at " + i + " is not sorted correctly, is "
                                + transitions64[i] + ", previous is " + transitions64[i - 1]);
            }
        }

        byte[] type = new byte[tzh_timecnt];
        it.readByteArray(type, 0, type.length);
        for (int i = 0; i < type.length; i++) {
            int typeIndex = type[i] & 0xff;
            if (typeIndex >= tzh_typecnt) {
                throw new IOException(
                        id + " type at " + i + " is not < " + tzh_typecnt + ", is " + typeIndex);
            }
        }

        int[] gmtOffsets = new int[tzh_typecnt];
        byte[] isDsts = new byte[tzh_typecnt];
        for (int i = 0; i < tzh_typecnt; ++i) {
            gmtOffsets[i] = it.readInt();
            byte isDst = it.readByte();
            if (isDst != 0 && isDst != 1) {
                throw new IOException(id + " dst at " + i + " is not 0 or 1, is " + isDst);
            }
            isDsts[i] = isDst;
            // We skip the abbreviation index. This would let us provide historically-accurate
            // time zone abbreviations (such as "AHST", "YST", and "AKST" for standard time in
            // America/Anchorage in 1982, 1983, and 1984 respectively). ICU only knows the current
            // names, though, so even if we did use this data to provide the correct abbreviations
            // for en_US, we wouldn't be able to provide correct abbreviations for other locales,
            // nor would we be able to provide correct long forms (such as "Yukon Standard Time")
            // for any locale. (The RI doesn't do any better than us here either.)
            it.skip(1);
        }

        return new ZoneInfo(id, transitions64, type, gmtOffsets, isDsts, currentTimeMillis);
    }

    private ZoneInfo(String name, long[] transitions, byte[] types, int[] gmtOffsets, byte[] isDsts,
            long currentTimeMillis) {
        if (gmtOffsets.length == 0) {
            throw new IllegalArgumentException("ZoneInfo requires at least one offset "
                    + "to be provided for each timezone but could not find one for '" + name + "'");
        }
        mTransitions = transitions;
        mTypes = types;
        mIsDsts = isDsts;
        setID(name);

        // Find the latest daylight and standard offsets (if any).
        int lastStdTransitionIndex = -1;
        int lastDstTransitionIndex = -1;
        for (int i = mTransitions.length - 1;
                (lastStdTransitionIndex == -1 || lastDstTransitionIndex == -1) && i >= 0; --i) {
            int typeIndex = mTypes[i] & 0xff;
            if (lastStdTransitionIndex == -1 && mIsDsts[typeIndex] == 0) {
                lastStdTransitionIndex = i;
            }
            if (lastDstTransitionIndex == -1 && mIsDsts[typeIndex] != 0) {
                lastDstTransitionIndex = i;
            }
        }

        // Use the latest non-daylight offset (if any) as the raw offset.
        if (mTransitions.length == 0) {
            // This case is no longer expected to occur in the data used on Android after changes
            // made in zic version 2014c. It is kept as a fallback.
            // If there are no transitions then use the first GMT offset.
            mRawOffset = gmtOffsets[0];
        } else {
            if (lastStdTransitionIndex == -1) {
                throw new IllegalStateException( "ZoneInfo requires at least one non-DST "
                        + "transition to be provided for each timezone that has at least one "
                        + "transition but could not find one for '" + name + "'");
            }
            mRawOffset = gmtOffsets[mTypes[lastStdTransitionIndex] & 0xff];
        }

        if (lastDstTransitionIndex != -1) {
            // Check to see if the last DST transition is in the future or the past. If it is in
            // the past then we treat it as if it doesn't exist, at least for the purposes of
            // setting mDstSavings and mUseDst.
            long lastDSTTransitionTime = mTransitions[lastDstTransitionIndex];

            // Convert the current time in millis into seconds. Unlike other places that convert
            // time in milliseconds into seconds in order to compare with transition time this
            // rounds up rather than down. It does that because this is interested in what
            // transitions apply in future
            long currentUnixTimeSeconds = roundUpMillisToSeconds(currentTimeMillis);

            // Is this zone observing DST currently or in the future?
            // We don't care if they've historically used it: most places have at least once.
            // See http://code.google.com/p/android/issues/detail?id=877.
            // This test means that for somewhere like Morocco, which tried DST in 2009 but has
            // no future plans (and thus no future schedule info) will report "true" from
            // useDaylightTime at the start of 2009 but "false" at the end. This seems appropriate.
            if (lastDSTTransitionTime < currentUnixTimeSeconds) {
                // The last DST transition is before now so treat it as if it doesn't exist.
                lastDstTransitionIndex = -1;
            }
        }

        if (lastDstTransitionIndex == -1) {
            // There were no DST transitions or at least no future DST transitions so DST is not
            // used.
            mDstSavings = 0;
            mUseDst = false;
        } else {
            // Use the latest transition's pair of offsets to compute the DST savings.
            // This isn't generally useful, but it's exposed by TimeZone.getDSTSavings.
            int lastGmtOffset = gmtOffsets[mTypes[lastStdTransitionIndex] & 0xff];
            int lastDstOffset = gmtOffsets[mTypes[lastDstTransitionIndex] & 0xff];
            mDstSavings = (lastDstOffset - lastGmtOffset) * 1000;
            mUseDst = true;
        }

        // From the tzfile docs (Jan 2019):
        // The localtime(3) function uses the first standard-time ttinfo structure
        // in the file (or simply the first ttinfo structure in the absence of a
        // standard-time structure) if either tzh_timecnt is zero or the time
        // argument is less than the first transition time recorded in the file.
        //
        // Cache the raw offset associated with the first nonDst type, in case we're asked about
        // times that predate our transition data. Android falls back to mRawOffset if there are
        // only DST ttinfo structures (assumed rare).
        int firstStdTypeIndex = -1;
        for (int i = 0; i < mIsDsts.length; ++i) {
            if (mIsDsts[i] == 0) {
                firstStdTypeIndex = i;
                break;
            }
        }

        int earliestRawOffset = (firstStdTypeIndex != -1)
                ? gmtOffsets[firstStdTypeIndex] : mRawOffset;

        // Rather than keep offsets from UTC, we use offsets from local time, so the raw offset
        // can be changed and automatically affect all the offsets.
        mOffsets = gmtOffsets;
        for (int i = 0; i < mOffsets.length; i++) {
            mOffsets[i] -= mRawOffset;
        }

        // tzdata uses seconds, but Java uses milliseconds.
        mRawOffset *= 1000;
        mEarliestRawOffset = earliestRawOffset * 1000;
    }

    /**
     * Ensure that when deserializing an instance that {@link #mDstSavings} is always 0 when
     * {@link #mUseDst} is false.
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        if (!mUseDst && mDstSavings != 0) {
            mDstSavings = 0;
        }
    }

    @Override
    public int getOffset(int era, int year, int month, int day, int dayOfWeek, int millis) {
        // XXX This assumes Gregorian always; Calendar switches from
        // Julian to Gregorian in 1582.  What calendar system are the
        // arguments supposed to come from?

        long calc = (year / 400) * MILLISECONDS_PER_400_YEARS;
        year %= 400;

        calc += year * (365 * MILLISECONDS_PER_DAY);
        calc += ((year + 3) / 4) * MILLISECONDS_PER_DAY;

        if (year > 0) {
            calc -= ((year - 1) / 100) * MILLISECONDS_PER_DAY;
        }

        boolean isLeap = (year == 0 || (year % 4 == 0 && year % 100 != 0));
        int[] mlen = isLeap ? LEAP : NORMAL;

        calc += mlen[month] * MILLISECONDS_PER_DAY;
        calc += (day - 1) * MILLISECONDS_PER_DAY;
        calc += millis;

        calc -= mRawOffset;
        calc -= UNIX_OFFSET;

        return getOffset(calc);
    }

    /**
     * Find the transition in the {@code timezone} in effect at {@code seconds}.
     *
     * <p>Returns an index in the range -1..timeZone.mTransitions.length - 1. -1 is used to
     * indicate the time is before the first transition. Other values are an index into
     * timeZone.mTransitions.
     */
    public int findTransitionIndex(long seconds) {
        int transition = Arrays.binarySearch(mTransitions, seconds);
        if (transition < 0) {
            transition = ~transition - 1;
            if (transition < 0) {
                return -1;
            }
        }

        return transition;
    }

    /**
     * Finds the index within the {@link #mOffsets}/{@link #mIsDsts} arrays for the specified time
     * in seconds, since 1st Jan 1970 00:00:00.
     * @param seconds the time in seconds.
     * @return -1 if the time is before the first transition, or [0..{@code mOffsets}-1] for the
     * active offset.
     */
    int findOffsetIndexForTimeInSeconds(long seconds) {
        int transition = findTransitionIndex(seconds);
        if (transition < 0) {
            return -1;
        }

        return mTypes[transition] & 0xff;
    }

    /**
     * Finds the index within the {@link #mOffsets}/{@link #mIsDsts} arrays for the specified time
     * in milliseconds, since 1st Jan 1970 00:00:00.000.
     * @param millis the time in milliseconds.
     * @return -1 if the time is before the first transition, or [0..{@code mOffsets}-1] for the
     * active offset.
     */
    int findOffsetIndexForTimeInMilliseconds(long millis) {
        // This rounds the time in milliseconds down to the time in seconds.
        //
        // It can't just divide a timestamp in millis by 1000 to obtain a transition time in
        // seconds because / (div) in Java rounds towards zero. Times before 1970 are negative and
        // if they have a millisecond component then div would result in obtaining a time that is
        // one second after what we need.
        //
        // e.g. dividing -12,001 milliseconds by 1000 would result in -12 seconds. If there was a
        //      transition at -12 seconds then that would be incorrectly treated as being active
        //      for a time of -12,001 milliseconds even though that time is before the transition
        //      should occur.

        return findOffsetIndexForTimeInSeconds(roundDownMillisToSeconds(millis));
    }

    /**
     * Converts time in milliseconds into a time in seconds, rounding down to the closest time
     * in seconds before the time in milliseconds.
     *
     * <p>It's not sufficient to simply divide by 1000 because that rounds towards 0 and so while
     * for positive numbers it produces a time in seconds that precedes the time in milliseconds
     * for negative numbers it can produce a time in seconds that follows the time in milliseconds.
     *
     * <p>This basically does the same as {@code (long) Math.floor(millis / 1000.0)} but should be
     * faster.
     *
     * @param millis the time in milliseconds, may be negative.
     * @return the time in seconds.
     */
    static long roundDownMillisToSeconds(long millis) {
        if (millis < 0) {
            // If the time is less than zero then subtract 999 and then divide by 1000 rounding
            // towards 0 as usual, e.g.
            // -12345 -> -13344 / 1000 = -13
            // -12000 -> -12999 / 1000 = -12
            // -12001 -> -13000 / 1000 = -13
            return (millis - 999) / 1000;
        } else {
            return millis / 1000;
        }
    }

    /**
     * Converts time in milliseconds into a time in seconds, rounding up to the closest time
     * in seconds before the time in milliseconds.
     *
     * <p>It's not sufficient to simply divide by 1000 because that rounds towards 0 and so while
     * for negative numbers it produces a time in seconds that follows the time in milliseconds
     * for positive numbers it can produce a time in seconds that precedes the time in milliseconds.
     *
     * <p>This basically does the same as {@code (long) Math.ceil(millis / 1000.0)} but should be
     * faster.
     *
     * @param millis the time in milliseconds, may be negative.
     * @return the time in seconds.
     */
    static long roundUpMillisToSeconds(long millis) {
        if (millis > 0) {
            // If the time is greater than zero then add 999 and then divide by 1000 rounding
            // towards 0 as usual, e.g.
            // 12345 -> 13344 / 1000 = 13
            // 12000 -> 12999 / 1000 = 12
            // 12001 -> 13000 / 1000 = 13
            return (millis + 999) / 1000;
        } else {
            return millis / 1000;
        }
    }

    /**
     * Get the raw and DST offsets for the specified time in milliseconds since
     * 1st Jan 1970 00:00:00.000 UTC.
     *
     * <p>The raw offset, i.e. that part of the total offset which is not due to DST, is stored at
     * index 0 of the {@code offsets} array and the DST offset, i.e. that part of the offset which
     * is due to DST is stored at index 1.
     *
     * @param utcTimeInMillis the UTC time in milliseconds.
     * @param offsets the array whose length must be greater than or equal to 2.
     * @return the total offset which is the sum of the raw and DST offsets.
     */
    public int getOffsetsByUtcTime(long utcTimeInMillis, int[] offsets) {
        int transitionIndex = findTransitionIndex(roundDownMillisToSeconds(utcTimeInMillis));
        int totalOffset;
        int rawOffset;
        int dstOffset;
        if (transitionIndex == -1) {
            // See getOffset(long) and inDaylightTime(Date) for an explanation as to why these
            // values are used for times before the first transition.
            rawOffset = mEarliestRawOffset;
            dstOffset = 0;
            totalOffset = rawOffset;
        } else {
            int type = mTypes[transitionIndex] & 0xff;

            // Get the total offset used for the transition.
            totalOffset = mRawOffset + mOffsets[type] * 1000;
            if (mIsDsts[type] == 0) {
                // Offset does not include DST so DST is 0 and the raw offset is the total offset.
                rawOffset = totalOffset;
                dstOffset = 0;
            } else {
                // Offset does include DST, we need to find the preceding transition that did not
                // include the DST offset so that we can calculate the DST offset.
                rawOffset = -1;
                for (transitionIndex -= 1; transitionIndex >= 0; --transitionIndex) {
                    type = mTypes[transitionIndex] & 0xff;
                    if (mIsDsts[type] == 0) {
                        rawOffset = mRawOffset + mOffsets[type] * 1000;
                        break;
                    }
                }
                // If no previous transition was found then use the earliest raw offset.
                if (rawOffset == -1) {
                    rawOffset = mEarliestRawOffset;
                }

                // The DST offset is the difference between the total and the raw offset.
                dstOffset = totalOffset - rawOffset;
            }
        }

        offsets[0] = rawOffset;
        offsets[1] = dstOffset;

        return totalOffset;
    }

    @Override
    public int getOffset(long when) {
        int offsetIndex = findOffsetIndexForTimeInMilliseconds(when);
        if (offsetIndex == -1) {
            // Assume that all times before our first transition correspond to the
            // oldest-known non-daylight offset. The obvious alternative would be to
            // use the current raw offset, but that seems like a greater leap of faith.
            return mEarliestRawOffset;
        }
        return mRawOffset + mOffsets[offsetIndex] * 1000;
    }

    @Override public boolean inDaylightTime(Date time) {
        long when = time.getTime();
        int offsetIndex = findOffsetIndexForTimeInMilliseconds(when);
        if (offsetIndex == -1) {
            // Assume that all times before our first transition are non-daylight.
            // Transition data tends to start with a transition to daylight, so just
            // copying the first transition would assume the opposite.
            // http://code.google.com/p/android/issues/detail?id=14395
            return false;
        }
        return mIsDsts[offsetIndex] == 1;
    }

    @Override public int getRawOffset() {
        return mRawOffset;
    }

    @Override public void setRawOffset(int off) {
        mRawOffset = off;
    }

    @Override public int getDSTSavings() {
        return mDstSavings;
    }

    @Override public boolean useDaylightTime() {
        return mUseDst;
    }

    @Override public boolean hasSameRules(TimeZone timeZone) {
        if (!(timeZone instanceof ZoneInfo)) {
            return false;
        }
        ZoneInfo other = (ZoneInfo) timeZone;
        if (mUseDst != other.mUseDst) {
            return false;
        }
        if (!mUseDst) {
            return mRawOffset == other.mRawOffset;
        }
        return mRawOffset == other.mRawOffset
                // Arrays.equals returns true if both arrays are null
                && Arrays.equals(mOffsets, other.mOffsets)
                && Arrays.equals(mIsDsts, other.mIsDsts)
                && Arrays.equals(mTypes, other.mTypes)
                && Arrays.equals(mTransitions, other.mTransitions);
    }

    @Override public boolean equals(Object obj) {
        if (!(obj instanceof ZoneInfo)) {
            return false;
        }
        ZoneInfo other = (ZoneInfo) obj;
        return getID().equals(other.getID()) && hasSameRules(other);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + getID().hashCode();
        result = prime * result + Arrays.hashCode(mOffsets);
        result = prime * result + Arrays.hashCode(mIsDsts);
        result = prime * result + mRawOffset;
        result = prime * result + Arrays.hashCode(mTransitions);
        result = prime * result + Arrays.hashCode(mTypes);
        result = prime * result + (mUseDst ? 1231 : 1237);
        return result;
    }

    @Override
    public String toString() {
        return getClass().getName() + "[id=\"" + getID() + "\"" +
            ",mRawOffset=" + mRawOffset +
            ",mEarliestRawOffset=" + mEarliestRawOffset +
            ",mUseDst=" + mUseDst +
            ",mDstSavings=" + mDstSavings +
            ",transitions=" + mTransitions.length +
            "]";
    }

    @Override
    public Object clone() {
        // Overridden for documentation. The default clone() behavior is exactly what we want.
        // Though mutable, the arrays of offset data are treated as immutable. Only ID and
        // mRawOffset are mutable in this class, and those are an immutable object and a primitive
        // respectively.
        return super.clone();
    }

    /**
     * A class that represents a "wall time". This class is modeled on the C tm struct and
     * is used to support android.text.format.Time behavior. Unlike the tm struct the year is
     * represented as the full year, not the years since 1900.
     *
     * <p>This class contains a rewrite of various native functions that android.text.format.Time
     * once relied on such as mktime_tz and localtime_tz. This replacement does not support leap
     * seconds but does try to preserve behavior around ambiguous date/times found in the BSD
     * version of mktime that was previously used.
     *
     * <p>The original native code used a 32-bit value for time_t on 32-bit Android, which
     * was the only variant of Android available at the time. To preserve old behavior this code
     * deliberately uses {@code int} rather than {@code long} for most things and performs
     * calculations in seconds. This creates deliberate truncation issues for date / times before
     * 1901 and after 2038. This is intentional but might be fixed in future if all the knock-ons
     * can be resolved: Application code may have come to rely on the range so previously values
     * like zero for year could indicate an invalid date but if we move to long the year zero would
     * be valid.
     *
     * <p>All offsets are considered to be safe for addition / subtraction / multiplication without
     * worrying about overflow. All absolute time arithmetic is checked for overflow / underflow.
     *
     * @hide
     */
    @libcore.api.CorePlatformApi
    public static class WallTime {

        // We use a GregorianCalendar (set to UTC) to handle all the date/time normalization logic
        // and to convert from a broken-down date/time to a millis value.
        // Unfortunately, it cannot represent an initial state with a zero day and would
        // automatically normalize it, so we must copy values into and out of it as needed.
        private final GregorianCalendar calendar;

        private int year;
        private int month;
        private int monthDay;
        private int hour;
        private int minute;
        private int second;
        private int weekDay;
        private int yearDay;
        private int isDst;
        private int gmtOffsetSeconds;

        @libcore.api.CorePlatformApi
        public WallTime() {
            this.calendar = new GregorianCalendar(0, 0, 0, 0, 0, 0);
            calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        }

        /**
         * Sets the wall time to a point in time using the time zone information provided. This
         * is a replacement for the old native localtime_tz() function.
         *
         * <p>When going from an instant to a wall time it is always unambiguous because there
         * is only one offset rule acting at any given instant. We do not consider leap seconds.
         */
        @libcore.api.CorePlatformApi
        public void localtime(int timeSeconds, ZoneInfo zoneInfo) {
            try {
                int offsetSeconds = zoneInfo.mRawOffset / 1000;

                // Find out the timezone DST state and adjustment.
                byte isDst;
                if (zoneInfo.mTransitions.length == 0) {
                    isDst = 0;
                } else {
                    // offsetIndex can be in the range -1..zoneInfo.mOffsets.length - 1
                    int offsetIndex = zoneInfo.findOffsetIndexForTimeInSeconds(timeSeconds);
                    if (offsetIndex == -1) {
                        // -1 means timeSeconds is "before the first recorded transition". The first
                        // recorded transition is treated as a transition from non-DST and the
                        // earliest known raw offset.
                        offsetSeconds = zoneInfo.mEarliestRawOffset / 1000;
                        isDst = 0;
                    } else {
                        offsetSeconds += zoneInfo.mOffsets[offsetIndex];
                        isDst = zoneInfo.mIsDsts[offsetIndex];
                    }
                }

                // Perform arithmetic that might underflow before setting fields.
                int wallTimeSeconds = checked32BitAdd(timeSeconds, offsetSeconds);

                // Set fields.
                calendar.setTimeInMillis(wallTimeSeconds * 1000L);
                copyFieldsFromCalendar();
                this.isDst = isDst;
                this.gmtOffsetSeconds = offsetSeconds;
            } catch (CheckedArithmeticException e) {
                // Just stop, leaving fields untouched.
            }
        }

        /**
         * Returns the time in seconds since beginning of the Unix epoch for the wall time using the
         * time zone information provided. This is a replacement for an old native mktime_tz() C
         * function.
         *
         * <p>When going from a wall time to an instant the answer can be ambiguous. A wall
         * time can map to zero, one or two instants given sane date/time transitions. Sane
         * in this case means that transitions occur less frequently than the offset
         * differences between them (which could cause all sorts of craziness like the
         * skipping out of transitions).
         *
         * <p>For example, this is not fully supported:
         * <ul>
         *     <li>t1 { time = 1, offset = 0 }
         *     <li>t2 { time = 2, offset = -1 }
         *     <li>t3 { time = 3, offset = -2 }
         * </ul>
         * A wall time in this case might map to t1, t2 or t3.
         *
         * <p>We do not handle leap seconds.
         * <p>We assume that no timezone offset transition has an absolute offset > 24 hours.
         * <p>We do not assume that adjacent transitions modify the DST state; adjustments can
         * occur for other reasons such as when a zone changes its raw offset.
         */
        @libcore.api.CorePlatformApi
        public int mktime(ZoneInfo zoneInfo) {
            // Normalize isDst to -1, 0 or 1 to simplify isDst equality checks below.
            this.isDst = this.isDst > 0 ? this.isDst = 1 : this.isDst < 0 ? this.isDst = -1 : 0;

            copyFieldsToCalendar();
            final long longWallTimeSeconds = calendar.getTimeInMillis() / 1000;
            if (Integer.MIN_VALUE > longWallTimeSeconds
                    || longWallTimeSeconds > Integer.MAX_VALUE) {
                // For compatibility with the old native 32-bit implementation we must treat
                // this as an error. Note: -1 could be confused with a real time.
                return -1;
            }

            try {
                final int wallTimeSeconds =  (int) longWallTimeSeconds;
                final int rawOffsetSeconds = zoneInfo.mRawOffset / 1000;
                final int rawTimeSeconds = checked32BitSubtract(wallTimeSeconds, rawOffsetSeconds);

                if (zoneInfo.mTransitions.length == 0) {
                    // There is no transition information. There is just a raw offset for all time.
                    if (this.isDst > 0) {
                        // Caller has asserted DST, but there is no DST information available.
                        return -1;
                    }
                    copyFieldsFromCalendar();
                    this.isDst = 0;
                    this.gmtOffsetSeconds = rawOffsetSeconds;
                    return rawTimeSeconds;
                }

                // We cannot know for sure what instant the wall time will map to. Unfortunately, in
                // order to know for sure we need the timezone information, but to get the timezone
                // information we need an instant. To resolve this we use the raw offset to find an
                // OffsetInterval; this will get us the OffsetInterval we need or very close.

                // The initialTransition can be between -1 and (zoneInfo.mTransitions - 1). -1
                // indicates the rawTime is before the first transition and is handled gracefully by
                // createOffsetInterval().
                final int initialTransitionIndex = zoneInfo.findTransitionIndex(rawTimeSeconds);

                if (isDst < 0) {
                    // This is treated as a special case to get it out of the way:
                    // When a caller has set isDst == -1 it means we can return the first match for
                    // the wall time we find. If the caller has specified a wall time that cannot
                    // exist this always returns -1.

                    Integer result = doWallTimeSearch(zoneInfo, initialTransitionIndex,
                            wallTimeSeconds, true /* mustMatchDst */);
                    return result == null ? -1 : result;
                }

                // If the wall time asserts a DST (isDst == 0 or 1) the search is performed twice:
                // 1) The first attempts to find a DST offset that matches isDst exactly.
                // 2) If it fails, isDst is assumed to be incorrect and adjustments are made to see
                // if a valid wall time can be created. The result can be somewhat arbitrary.

                Integer result = doWallTimeSearch(zoneInfo, initialTransitionIndex, wallTimeSeconds,
                        true /* mustMatchDst */);
                if (result == null) {
                    result = doWallTimeSearch(zoneInfo, initialTransitionIndex, wallTimeSeconds,
                            false /* mustMatchDst */);
                }
                if (result == null) {
                    result = -1;
                }
                return result;
            } catch (CheckedArithmeticException e) {
                return -1;
            }
        }

        /**
         * Attempt to apply DST adjustments to {@code oldWallTimeSeconds} to create a wall time in
         * {@code targetInterval}.
         *
         * <p>This is used when a caller has made an assertion about standard time / DST that cannot
         * be matched to any offset interval that exists. We must therefore assume that the isDst
         * assertion is incorrect and the invalid wall time is the result of some modification the
         * caller made to a valid wall time that pushed them outside of the offset interval they
         * were in. We must correct for any DST change that should have been applied when they did
         * so.
         *
         * <p>Unfortunately, we have no information about what adjustment they made and so cannot
         * know which offset interval they were previously in. For example, they may have added a
         * second or a year to a valid time to arrive at what they have.
         *
         * <p>We try all offset types that are not the same as the isDst the caller asserted. For
         * each possible offset we work out the offset difference between that and
         * {@code targetInterval}, apply it, and see if we are still in {@code targetInterval}. If
         * we are, then we have found an adjustment.
         */
        private Integer tryOffsetAdjustments(ZoneInfo zoneInfo, int oldWallTimeSeconds,
                OffsetInterval targetInterval, int transitionIndex, int isDstToFind)
                throws CheckedArithmeticException {

            int[] offsetsToTry = getOffsetsOfType(zoneInfo, transitionIndex, isDstToFind);
            for (int j = 0; j < offsetsToTry.length; j++) {
                int rawOffsetSeconds = zoneInfo.mRawOffset / 1000;
                int jOffsetSeconds = rawOffsetSeconds + offsetsToTry[j];
                int targetIntervalOffsetSeconds = targetInterval.getTotalOffsetSeconds();
                int adjustmentSeconds = targetIntervalOffsetSeconds - jOffsetSeconds;
                int adjustedWallTimeSeconds =
                        checked32BitAdd(oldWallTimeSeconds, adjustmentSeconds);
                if (targetInterval.containsWallTime(adjustedWallTimeSeconds)) {
                    // Perform any arithmetic that might overflow.
                    int returnValue = checked32BitSubtract(adjustedWallTimeSeconds,
                            targetIntervalOffsetSeconds);

                    // Modify field state and return the result.
                    calendar.setTimeInMillis(adjustedWallTimeSeconds * 1000L);
                    copyFieldsFromCalendar();
                    this.isDst = targetInterval.getIsDst();
                    this.gmtOffsetSeconds = targetIntervalOffsetSeconds;
                    return returnValue;
                }
            }
            return null;
        }

        /**
         * Return an array of offsets that have the requested {@code isDst} value.
         * The {@code startIndex} is used as a starting point so transitions nearest
         * to that index are returned first.
         */
        private static int[] getOffsetsOfType(ZoneInfo zoneInfo, int startIndex, int isDst) {
            // +1 to account for the synthetic transition we invent before the first recorded one.
            int[] offsets = new int[zoneInfo.mOffsets.length + 1];
            boolean[] seen = new boolean[zoneInfo.mOffsets.length];
            int numFound = 0;

            int delta = 0;
            boolean clampTop = false;
            boolean clampBottom = false;
            do {
                // delta = { 1, -1, 2, -2, 3, -3...}
                delta *= -1;
                if (delta >= 0) {
                    delta++;
                }

                int transitionIndex = startIndex + delta;
                if (delta < 0 && transitionIndex < -1) {
                    clampBottom = true;
                    continue;
                } else if (delta > 0 && transitionIndex >=  zoneInfo.mTypes.length) {
                    clampTop = true;
                    continue;
                }

                if (transitionIndex == -1) {
                    if (isDst == 0) {
                        // Synthesize a non-DST transition before the first transition we have
                        // data for.
                        offsets[numFound++] = 0; // offset of 0 from raw offset
                    }
                    continue;
                }
                int type = zoneInfo.mTypes[transitionIndex] & 0xff;
                if (!seen[type]) {
                    if (zoneInfo.mIsDsts[type] == isDst) {
                        offsets[numFound++] = zoneInfo.mOffsets[type];
                    }
                    seen[type] = true;
                }
            } while (!(clampTop && clampBottom));

            int[] toReturn = new int[numFound];
            System.arraycopy(offsets, 0, toReturn, 0, numFound);
            return toReturn;
        }

        /**
         * Find a time <em>in seconds</em> the same or close to {@code wallTimeSeconds} that
         * satisfies {@code mustMatchDst}. The search begins around the timezone offset transition
         * with {@code initialTransitionIndex}.
         *
         * <p>If {@code mustMatchDst} is {@code true} the method can only return times that
         * use timezone offsets that satisfy the {@code this.isDst} requirements.
         * If {@code this.isDst == -1} it means that any offset can be used.
         *
         * <p>If {@code mustMatchDst} is {@code false} any offset that covers the
         * currently set time is acceptable. That is: if {@code this.isDst} == -1, any offset
         * transition can be used, if it is 0 or 1 the offset used must match {@code this.isDst}.
         *
         * <p>Note: This method both uses and can modify field state. It returns the matching time
         * in seconds if a match has been found and modifies fields, or it returns {@code null} and
         * leaves the field state unmodified.
         */
        private Integer doWallTimeSearch(ZoneInfo zoneInfo, int initialTransitionIndex,
                int wallTimeSeconds, boolean mustMatchDst) throws CheckedArithmeticException {

            // The loop below starts at the initialTransitionIndex and radiates out from that point
            // up to 24 hours in either direction by applying transitionIndexDelta to inspect
            // adjacent transitions (0, -1, +1, -2, +2). 24 hours is used because we assume that no
            // total offset from UTC is ever > 24 hours. clampTop and clampBottom are used to
            // indicate whether the search has either searched > 24 hours or exhausted the
            // transition data in that direction. The search stops when a match is found or if
            // clampTop and clampBottom are both true.
            // The match logic employed is determined by the mustMatchDst parameter.
            final int MAX_SEARCH_SECONDS = 24 * 60 * 60;
            boolean clampTop = false, clampBottom = false;
            int loop = 0;
            do {
                // transitionIndexDelta = { 0, -1, 1, -2, 2,..}
                int transitionIndexDelta = (loop + 1) / 2;
                if (loop % 2 == 1) {
                    transitionIndexDelta *= -1;
                }
                loop++;

                // Only do any work in this iteration if we need to.
                if (transitionIndexDelta > 0 && clampTop
                        || transitionIndexDelta < 0 && clampBottom) {
                    continue;
                }

                // Obtain the OffsetInterval to use.
                int currentTransitionIndex = initialTransitionIndex + transitionIndexDelta;
                OffsetInterval offsetInterval =
                        OffsetInterval.create(zoneInfo, currentTransitionIndex);
                if (offsetInterval == null) {
                    // No transition exists with the index we tried: Stop searching in the
                    // current direction.
                    clampTop |= (transitionIndexDelta > 0);
                    clampBottom |= (transitionIndexDelta < 0);
                    continue;
                }

                // Match the wallTimeSeconds against the OffsetInterval.
                if (mustMatchDst) {
                    // Work out if the interval contains the wall time the caller specified and
                    // matches their isDst value.
                    if (offsetInterval.containsWallTime(wallTimeSeconds)) {
                        if (this.isDst == -1 || offsetInterval.getIsDst() == this.isDst) {
                            // This always returns the first OffsetInterval it finds that matches
                            // the wall time and isDst requirements. If this.isDst == -1 this means
                            // the result might be a DST or a non-DST answer for wall times that can
                            // exist in two OffsetIntervals.
                            int totalOffsetSeconds = offsetInterval.getTotalOffsetSeconds();
                            int returnValue =
                                    checked32BitSubtract(wallTimeSeconds, totalOffsetSeconds);

                            copyFieldsFromCalendar();
                            this.isDst = offsetInterval.getIsDst();
                            this.gmtOffsetSeconds = totalOffsetSeconds;
                            return returnValue;
                        }
                    }
                } else {
                    // To retain similar behavior to the old native implementation: if the caller is
                    // asserting the same isDst value as the OffsetInterval we are looking at we do
                    // not try to find an adjustment from another OffsetInterval of the same isDst
                    // type. If you remove this you get different results in situations like a
                    // DST -> DST transition or STD -> STD transition that results in an interval of
                    // "skipped" wall time. For example: if 01:30 (DST) is invalid and between two
                    // DST intervals, and the caller has passed isDst == 1, this results in a -1
                    // being returned.
                    if (isDst != offsetInterval.getIsDst()) {
                        final int isDstToFind = isDst;
                        Integer returnValue = tryOffsetAdjustments(zoneInfo, wallTimeSeconds,
                                offsetInterval, currentTransitionIndex, isDstToFind);
                        if (returnValue != null) {
                            return returnValue;
                        }
                    }
                }

                // See if we can avoid another loop in the current direction.
                if (transitionIndexDelta > 0) {
                    // If we are searching forward and the OffsetInterval we have ends
                    // > MAX_SEARCH_SECONDS after the wall time, we don't need to look any further
                    // forward.
                    boolean endSearch = offsetInterval.getEndWallTimeSeconds() - wallTimeSeconds
                            > MAX_SEARCH_SECONDS;
                    if (endSearch) {
                        clampTop = true;
                    }
                } else if (transitionIndexDelta < 0) {
                    boolean endSearch = wallTimeSeconds - offsetInterval.getStartWallTimeSeconds()
                            >= MAX_SEARCH_SECONDS;
                    if (endSearch) {
                        // If we are searching backward and the OffsetInterval starts
                        // > MAX_SEARCH_SECONDS before the wall time, we don't need to look any
                        // further backwards.
                        clampBottom = true;
                    }
                }
            } while (!(clampTop && clampBottom));
            return null;
        }

        @libcore.api.CorePlatformApi
        public void setYear(int year) {
            this.year = year;
        }

        @libcore.api.CorePlatformApi
        public void setMonth(int month) {
            this.month = month;
        }

        @libcore.api.CorePlatformApi
        public void setMonthDay(int monthDay) {
            this.monthDay = monthDay;
        }

        @libcore.api.CorePlatformApi
        public void setHour(int hour) {
            this.hour = hour;
        }

        @libcore.api.CorePlatformApi
        public void setMinute(int minute) {
            this.minute = minute;
        }

        @libcore.api.CorePlatformApi
        public void setSecond(int second) {
            this.second = second;
        }

        @libcore.api.CorePlatformApi
        public void setWeekDay(int weekDay) {
            this.weekDay = weekDay;
        }

        @libcore.api.CorePlatformApi
        public void setYearDay(int yearDay) {
            this.yearDay = yearDay;
        }

        @libcore.api.CorePlatformApi
        public void setIsDst(int isDst) {
            this.isDst = isDst;
        }

        @libcore.api.CorePlatformApi
        public void setGmtOffset(int gmtoff) {
            this.gmtOffsetSeconds = gmtoff;
        }

        @libcore.api.CorePlatformApi
        public int getYear() {
            return year;
        }

        @libcore.api.CorePlatformApi
        public int getMonth() {
            return month;
        }

        @libcore.api.CorePlatformApi
        public int getMonthDay() {
            return monthDay;
        }

        @libcore.api.CorePlatformApi
        public int getHour() {
            return hour;
        }

        @libcore.api.CorePlatformApi
        public int getMinute() {
            return minute;
        }

        @libcore.api.CorePlatformApi
        public int getSecond() {
            return second;
        }

        @libcore.api.CorePlatformApi
        public int getWeekDay() {
            return weekDay;
        }

        @libcore.api.CorePlatformApi
        public int getYearDay() {
            return yearDay;
        }

        @libcore.api.CorePlatformApi
        public int getGmtOffset() {
            return gmtOffsetSeconds;
        }

        @libcore.api.CorePlatformApi
        public int getIsDst() {
            return isDst;
        }

        private void copyFieldsToCalendar() {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, monthDay);
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, second);
            calendar.set(Calendar.MILLISECOND, 0);
        }

        private void copyFieldsFromCalendar() {
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH);
            monthDay = calendar.get(Calendar.DAY_OF_MONTH);
            hour = calendar.get(Calendar.HOUR_OF_DAY);
            minute = calendar.get(Calendar.MINUTE);
            second =  calendar.get(Calendar.SECOND);

            // Calendar uses Sunday == 1. Android Time uses Sunday = 0.
            weekDay = calendar.get(Calendar.DAY_OF_WEEK) - 1;
            // Calendar enumerates from 1, Android Time enumerates from 0.
            yearDay = calendar.get(Calendar.DAY_OF_YEAR) - 1;
        }
    }

    /**
     * A wall-time representation of a timezone offset interval.
     *
     * <p>Wall-time means "as it would appear locally in the timezone in which it applies".
     * For example in 2007:
     * PST was a -8:00 offset that ran until Mar 11, 2:00 AM.
     * PDT was a -7:00 offset and ran from Mar 11, 3:00 AM to Nov 4, 2:00 AM.
     * PST was a -8:00 offset and ran from Nov 4, 1:00 AM.
     * Crucially this means that there was a "gap" after PST when PDT started, and an overlap when
     * PDT ended and PST began.
     *
     * <p>Although wall-time means "local time", for convenience all wall-time values are stored in
     * the number of seconds since the beginning of the Unix epoch to get that time <em>in UTC</em>.
     * To convert from a wall-time to the actual UTC time it is necessary to <em>subtract</em> the
     * {@code totalOffsetSeconds}.
     * For example: If the offset in PST is -07:00 hours, then:
     * timeInPstSeconds = wallTimeUtcSeconds - offsetSeconds
     * i.e. 13:00 UTC - (-07:00) = 20:00 UTC = 13:00 PST
     */
    static class OffsetInterval {

        /** The time the interval starts in seconds since start of epoch, inclusive. */
        private final int startWallTimeSeconds;
        /** The time the interval ends in seconds since start of epoch, exclusive. */
        private final int endWallTimeSeconds;
        private final int isDst;
        private final int totalOffsetSeconds;

        /**
         * Creates an {@link OffsetInterval}.
         *
         * <p>If {@code transitionIndex} is -1, where possible the transition is synthesized to run
         * from the beginning of 32-bit time until the first transition in {@code timeZone} with
         * offset information based on the first type defined. If {@code transitionIndex} is the
         * last transition, that transition is considered to run until the end of 32-bit time.
         * Otherwise, the information is extracted from {@code timeZone.mTransitions},
         * {@code timeZone.mOffsets} and {@code timeZone.mIsDsts}.
         *
         * <p>This method can return null when:
         * <ol>
         * <li>the {@code transitionIndex} is outside the allowed range, i.e.
         *   {@code transitionIndex < -1 || transitionIndex >= [the number of transitions]}.</li>
         * <li>when calculations result in a zero-length interval. This is only expected to occur
         *   when dealing with transitions close to (or exactly at) {@code Integer.MIN_VALUE} and
         *   {@code Integer.MAX_VALUE} and where it's difficult to convert from UTC to local times.
         *   </li>
         * </ol>
         */
        public static OffsetInterval create(ZoneInfo timeZone, int transitionIndex) {
            if (transitionIndex < -1 || transitionIndex >= timeZone.mTransitions.length) {
                return null;
            }

            if (transitionIndex == -1) {
                int totalOffsetSeconds = timeZone.mEarliestRawOffset / 1000;
                int isDst = 0;

                int startWallTimeSeconds = Integer.MIN_VALUE;
                int endWallTimeSeconds =
                        saturated32BitAdd(timeZone.mTransitions[0], totalOffsetSeconds);
                if (startWallTimeSeconds == endWallTimeSeconds) {
                    // There's no point in returning an OffsetInterval that lasts 0 seconds.
                    return null;
                }
                return new OffsetInterval(startWallTimeSeconds, endWallTimeSeconds, isDst,
                        totalOffsetSeconds);
            }

            int rawOffsetSeconds = timeZone.mRawOffset / 1000;
            int type = timeZone.mTypes[transitionIndex] & 0xff;
            int totalOffsetSeconds = timeZone.mOffsets[type] + rawOffsetSeconds;
            int endWallTimeSeconds;
            if (transitionIndex == timeZone.mTransitions.length - 1) {
                endWallTimeSeconds = Integer.MAX_VALUE;
            } else {
                endWallTimeSeconds = saturated32BitAdd(
                        timeZone.mTransitions[transitionIndex + 1], totalOffsetSeconds);
            }
            int isDst = timeZone.mIsDsts[type];
            int startWallTimeSeconds =
                    saturated32BitAdd(timeZone.mTransitions[transitionIndex], totalOffsetSeconds);
            if (startWallTimeSeconds == endWallTimeSeconds) {
                // There's no point in returning an OffsetInterval that lasts 0 seconds.
                return null;
            }
            return new OffsetInterval(
                    startWallTimeSeconds, endWallTimeSeconds, isDst, totalOffsetSeconds);
        }

        private OffsetInterval(int startWallTimeSeconds, int endWallTimeSeconds, int isDst,
                int totalOffsetSeconds) {
            this.startWallTimeSeconds = startWallTimeSeconds;
            this.endWallTimeSeconds = endWallTimeSeconds;
            this.isDst = isDst;
            this.totalOffsetSeconds = totalOffsetSeconds;
        }

        public boolean containsWallTime(long wallTimeSeconds) {
            return wallTimeSeconds >= startWallTimeSeconds && wallTimeSeconds < endWallTimeSeconds;
        }

        public int getIsDst() {
            return isDst;
        }

        public int getTotalOffsetSeconds() {
            return totalOffsetSeconds;
        }

        public long getEndWallTimeSeconds() {
            return endWallTimeSeconds;
        }

        public long getStartWallTimeSeconds() {
            return startWallTimeSeconds;
        }
    }

    /**
     * An exception used to indicate an arithmetic overflow or underflow.
     */
    private static class CheckedArithmeticException extends Exception {
    }

    /**
     * Calculate (a + b). The result must be in the Integer range otherwise an exception is thrown.
     *
     * @throws CheckedArithmeticException if overflow or underflow occurs
     */
    private static int checked32BitAdd(long a, int b) throws CheckedArithmeticException {
        // Adapted from Guava IntMath.checkedAdd();
        long result = a + b;
        if (result != (int) result) {
            throw new CheckedArithmeticException();
        }
        return (int) result;
    }

    /**
     * Calculate (a - b). The result must be in the Integer range otherwise an exception is thrown.
     *
     * @throws CheckedArithmeticException if overflow or underflow occurs
     */
    private static int checked32BitSubtract(long a, int b) throws CheckedArithmeticException {
        // Adapted from Guava IntMath.checkedSubtract();
        long result = a - b;
        if (result != (int) result) {
            throw new CheckedArithmeticException();
        }
        return (int) result;
    }

    /**
     * Calculate (a + b). If the result would overflow or underflow outside of the Integer range
     * Integer.MAX_VALUE or Integer.MIN_VALUE will be returned, respectively.
     */
    private static int saturated32BitAdd(long a, int b) {
        long result = a + b;
        if (result > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        } else if (result < Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        return (int) result;
    }
}
