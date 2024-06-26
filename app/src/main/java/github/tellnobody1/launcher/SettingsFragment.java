/*
 * Copyright 2015-2017 Hayai Software
 * Copyright 2018-2022 The KeikaiLauncher Project
 * Copyright 2024 tellnobody1
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
package github.tellnobody1.launcher;

import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.*;
import android.view.*;
import static android.os.Build.VERSION_CODES.HONEYCOMB;

@TargetApi(HONEYCOMB)
public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        findPreference(R.string.pref_key_source_code).setOnPreferenceClickListener(preference -> {
            var intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(getString(R.string.source_code)));
            startActivity(intent);
            return true;
        });
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private Preference findPreference(int prefKey) {
        return findPreference(getString(prefKey));
    }
}
