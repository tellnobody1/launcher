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
package xyz.uaapps.launcher;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.StringRes;

public class SettingsFragment extends PreferenceFragment {

    private Preference findPreference(@StringRes final int prefKey) {
        return findPreference(getString(prefKey));
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        findPreference(R.string.pref_key_version).setSummary(BuildConfig.VERSION_NAME);

        var source_code = findPreference(R.string.pref_key_source_code);
        source_code.setOnPreferenceClickListener(new LaunchPreferenceSummary(getString(R.string.source_code)));

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private final class LaunchPreferenceSummary implements Preference.OnPreferenceClickListener {
        private final String url;

        public LaunchPreferenceSummary(String url) {
            this.url = url;
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            final Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
            return true;
        }
    }
}
