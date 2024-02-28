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

public class SharedLauncherPrefs {
    private final Context mContext;
    private final SharedPreferences mPreferences;

    public SharedLauncherPrefs(Context context) {
        mContext = context;
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    private String getString(@StringRes int resId) {
        return mContext.getString(resId);
    }

    public boolean isShowSearchButton() {
        return isPrefEnabled(R.string.pref_key_search_button, true);
    }

    public boolean isSwipeEnabled() {
        return isPrefEnabled(R.string.pref_key_swipe, true);
    }

    private boolean isPrefEnabled(@StringRes int keyRes, boolean defaultBoolean) {
        return mPreferences.getBoolean(getString(keyRes), defaultBoolean);
    }
}
