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

import android.content.*;
import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class SharedAppPrefs {
    private final Context ctx;
    private final SharedPreferences prefs;

    public SharedAppPrefs(Context ctx) {
        this.ctx = ctx;
        this.prefs = getDefaultSharedPreferences(ctx);
    }

    public boolean isShowSearchButton() {
        return prefs.getBoolean(ctx.getString(R.string.pref_key_search_button), true);
    }

}
