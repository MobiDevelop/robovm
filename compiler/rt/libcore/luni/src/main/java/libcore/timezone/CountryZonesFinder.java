/*
 * Copyright (C) 2018 The Android Open Source Project
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import libcore.timezone.CountryTimeZones.TimeZoneMapping;

/**
 * An in-memory representation of country &lt;-&gt; time zone mapping data.
 * @hide
 */
@libcore.api.CorePlatformApi
public final class CountryZonesFinder {

    private final List<CountryTimeZones> countryTimeZonesList;

    CountryZonesFinder(List<CountryTimeZones> countryTimeZonesList) {
        this.countryTimeZonesList = new ArrayList<>(countryTimeZonesList);
    }

    // VisibleForTesting
    public static CountryZonesFinder createForTests(List<CountryTimeZones> countryTimeZonesList) {
        return new CountryZonesFinder(countryTimeZonesList);
    }

    /**
     * Returns an immutable list of country ISO codes with time zones. The codes can be passed to
     * {@link #lookupCountryTimeZones(String)} and similar methods.
     */
    @libcore.api.CorePlatformApi
    public List<String> lookupAllCountryIsoCodes() {
        List<String> isoCodes = new ArrayList<>(countryTimeZonesList.size());
        for (CountryTimeZones countryTimeZones : countryTimeZonesList) {
            isoCodes.add(countryTimeZones.getCountryIso());
        }
        return Collections.unmodifiableList(isoCodes);
    }

    /**
     * Returns an immutable list of {@link CountryTimeZones} for countries that use the specified
     * time zone. An exact, case-sensitive match is performed on the zone ID. This method never
     * returns null.
     */
    @libcore.api.CorePlatformApi
    public List<CountryTimeZones> lookupCountryTimeZonesForZoneId(String zoneId) {
        List<CountryTimeZones> matches = new ArrayList<>(2);
        for (CountryTimeZones countryTimeZones : countryTimeZonesList) {
            boolean match = TimeZoneMapping.containsTimeZoneId(
                    countryTimeZones.getTimeZoneMappings(), zoneId);
            if (match) {
                matches.add(countryTimeZones);
            }
        }
        return Collections.unmodifiableList(matches);
    }

    /**
     * Returns a {@link CountryTimeZones} object associated with the specified country code. If one
     * cannot be found this method returns {@code null}.
     */
    @libcore.api.CorePlatformApi
    public CountryTimeZones lookupCountryTimeZones(String countryIso) {
        String normalizedCountryIso = TimeZoneFinder.normalizeCountryIso(countryIso);
        for (CountryTimeZones countryTimeZones : countryTimeZonesList) {
            if (countryTimeZones.getCountryIso().equals(normalizedCountryIso)) {
                return countryTimeZones;
            }
        }
        return null;
    }
}
