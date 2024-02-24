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

package xyz.uaapps.launcher.fragments;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.StringRes;

import xyz.uaapps.launcher.BuildConfig;
import xyz.uaapps.launcher.R;

public class SettingsFragment extends PreferenceFragment {

    /**
     * This field generates a ContentObserver to enable or disable the orientation locked setting depending if
     * rotation is locked or unlocked.
     */
    private final ContentObserver mAccSettingObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            final Preference orientationPreference = findPreference(R.string.pref_key_allow_rotation);

            orientationPreference.setEnabled(isOrientationLocked());

            super.onChange(selfChange);
        }
    };

    /**
     * This method returns whether the system orientation is locked.
     *
     * @return True if orientation is locked, false otherwise.
     */
    private boolean isOrientationLocked() {
        return Settings.System.getInt(getPreferenceScreen().getContext().getContentResolver(),
                Settings.System.ACCELEROMETER_ROTATION, 0) == 1;
    }

    private Preference findPreference(@StringRes final int prefKey) {
        return findPreference(getString(prefKey));
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        findPreference(getString(R.string.pref_key_swipe)).setEnabled(SDK_INT >= ICE_CREAM_SANDWICH);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        // Set the version
        findPreference("about_version").setSummary(BuildConfig.VERSION_NAME);

        final LaunchPreferenceSummary listener = new LaunchPreferenceSummary(getString(R.string.source_code));

        final Preference about_project = findPreference("about_project_website");
        about_project.setOnPreferenceClickListener(listener);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onPause() {
        final PreferenceScreen prefs = getPreferenceScreen();

        prefs.getContext().getContentResolver().unregisterContentObserver(mAccSettingObserver);

        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        final Preference orientationPreference = findPreference(R.string.pref_key_allow_rotation);
        final Context context = orientationPreference.getContext();
        final Uri accUri = Settings.System.getUriFor(Settings.System.ACCELEROMETER_ROTATION);

        context.getContentResolver().registerContentObserver(accUri, false, mAccSettingObserver);
        orientationPreference.setEnabled(isOrientationLocked());
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
