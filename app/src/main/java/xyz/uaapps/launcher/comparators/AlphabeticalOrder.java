/*
 * Copyright 2015-2017 Hayai Software
 * Copyright 2018-2022 The KeikaiLauncher Project
 * Copyright 2024 uaapps
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND
 * either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xyz.uaapps.launcher.comparators;

import xyz.uaapps.launcher.LaunchableActivity;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

public class AlphabeticalOrder implements Comparator<LaunchableActivity> {
    private final Collator collator;

    public AlphabeticalOrder(Locale locale) {
        this.collator = Collator.getInstance(locale);
        this.collator.setStrength(Collator.PRIMARY);
    }

    @Override
    public int compare(final LaunchableActivity lhs, final LaunchableActivity rhs) {
        return collator.compare(lhs.toString(), rhs.toString());
    }
}
