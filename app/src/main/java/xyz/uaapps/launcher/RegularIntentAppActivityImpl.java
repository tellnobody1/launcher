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

import android.annotation.TargetApi;
import android.content.*;
import android.content.pm.*;
import java.util.*;
import static android.content.Intent.*;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.*;

@TargetApi(BASE)
public class RegularIntentAppActivityImpl implements RegularIntentAppActivity {
    private final String mActivityLabel;
    private final Set<String> labels;
    private final String iconKey;
    private final Intent mLaunchIntent;
    private boolean favorite;
    private final String id;

    public RegularIntentAppActivityImpl(
            ResolveInfo info,
            SharedPreferences prefs,
            PackageManager manager,
            Set<String> labels,
            String iconKey) {
        var activityInfo = info.activityInfo;
        var name = new ComponentName(activityInfo.packageName, activityInfo.name);
        mLaunchIntent = getLaunchableIntent(name);
        /* Returns the actual label from the info and stores it locally, or retrieve it locally. */
        if (prefs.contains(activityInfo.packageName) && manager != null) {
            mActivityLabel = prefs.getString(activityInfo.packageName, null);
        } else {
            mActivityLabel = info.loadLabel(manager).toString();
            if (SDK_INT >= GINGERBREAD) {
                prefs.edit().putString(activityInfo.packageName, mActivityLabel).apply();
            } else {
                var result = prefs.edit().putString(activityInfo.packageName, mActivityLabel).commit();
            }
        }
        this.labels = labels;
        this.iconKey = iconKey;
        this.id = activityInfo.name;
    }

    private static Intent getLaunchableIntent(ComponentName componentName) {
        Intent launchIntent;
        if (SDK_INT >= HONEYCOMB) {
            launchIntent = Intent.makeMainActivity(componentName);
        } else {
            launchIntent = new Intent(ACTION_MAIN);
            launchIntent.addCategory(CATEGORY_LAUNCHER);
            launchIntent.setComponent(componentName);
        }
        launchIntent.setFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        return launchIntent;
    }

    public ComponentName getComponent() {
        return mLaunchIntent.getComponent();
    }

    public Intent getLaunchIntent() {
        return mLaunchIntent;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean priority) {
        favorite = priority;
    }

    public Set<String> getLabels() {
        return Collections.unmodifiableSet(labels);
    }

    public String getActivityLabel() {
        return mActivityLabel;
    }

    public String getIconKey() {
        return iconKey;
    }

    public String getId() {
        return id;
    }
}
