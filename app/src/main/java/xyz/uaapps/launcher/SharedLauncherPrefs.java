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

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.StringRes;

/**
 * This class is used to retrieve shared preferences used by this package.
 */
public class SharedLauncherPrefs {

    /**
     * The current Context.
     */
    private final Context mContext;

    /**
     * A SharedPreferences object.
     */
    private final SharedPreferences mPreferences;

    /**
     * The sole constructor.
     *
     * @param context The current context.
     */
    public SharedLauncherPrefs(final Context context) {
        mContext = context;
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * The used {@link SharedPreferences} object.
     *
     * @return The used SharedPreferences object.
     */
    public SharedPreferences getPreferences() {
        return mPreferences;
    }

    private String getString(@StringRes int resId) {
        return mContext.getString(resId);
    }

    public boolean isActionBarEnabled() {
        return isPrefEnabled(R.string.pref_key_actionbar, true);
    }

    public boolean isSwipeEnabled() {
        return isPrefEnabled(R.string.pref_key_swipe, true);
    }

    /**
     * This method checks if a {@code boolean} preference is enabled by {@link StringRes} key.
     *
     * @param keyRes         The {@code StringRes} key.
     * @param defaultBoolean The default if the key does not exist.
     * @return {@code true} if the value of the {@code StringRes} boolean value is true,
     * {@code false} otherwise.
     */
    private boolean isPrefEnabled(@StringRes final int keyRes, final boolean defaultBoolean) {
        return mPreferences.getBoolean(getString(keyRes), defaultBoolean);
    }

    /**
     * This method returns if screen rotation should be allowed.
     *
     * @return {@code true} if screen rotation should be permitted, {@code false} otherwise.
     */
    public boolean isRotationAllowed() {
        return isPrefEnabled(R.string.pref_key_allow_rotation, true);
    }
}
