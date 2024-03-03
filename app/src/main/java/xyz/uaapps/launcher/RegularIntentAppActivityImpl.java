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

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.content.Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.GINGERBREAD;

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collections;
import java.util.Set;

public class RegularIntentAppActivityImpl implements RegularIntentAppActivity {

    private final String mActivityLabel;
    private final Set<String> labels;
    @Nullable private final String iconKey;

    private final Intent mLaunchIntent;

    private boolean favorite;

    /**
     * This is the constructor for LaunchableActivities, used in a {@link AppsAdapter}, for
     * APIs 15-20.
     *
     * @param info    Information to derive the LaunchableActivity from.
     * @param prefs   The {@link SharedPreferences} to load the label for this from.
     * @param manager The {@link PackageManager} to load the label for this from. If null, the
     *                local store will not cache the label.
     */
    public RegularIntentAppActivityImpl(
            @NonNull ResolveInfo info,
            @NonNull SharedPreferences prefs,
            @Nullable PackageManager manager,
            Set<String> labels,
            @Nullable String iconKey) {
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
    }

    private static Intent getLaunchableIntent(ComponentName componentName) {
        var launchIntent = Intent.makeMainActivity(componentName);
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

    @Nullable
    public String getIconKey() {
        return iconKey;
    }

    public String getName() {
        return getComponent() == null ? mActivityLabel : getComponent().getClassName();
    }
}
