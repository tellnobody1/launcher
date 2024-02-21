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

package com.anpmech.launcher;

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.UserManager;

import androidx.annotation.DeprecatedSinceApi;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

public class LaunchableActivity {

    private final String mActivityLabel;

    private final Intent mLaunchIntent;

    /**
     * The user serial, to be used to retrieve a {@link android.os.UserHandle} as necessary.
     * Defined as {@code Long.MIN_VALUE} if there is no user serial assigned to this object.
     */
    private final long mUserSerial;

    private int mPriority;

    /**
     * This is the constructor for LaunchableActivities, used in a {@link LaunchableAdapter}, for
     * API 21+.
     *
     * @param info           Information to derive the LaunchableActivity from.
     * @param manager        The service to retrieve user information about the activity from.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public LaunchableActivity(@NonNull final LauncherActivityInfo info, final UserManager manager) {
        mLaunchIntent = getLaunchableIntent(info.getComponentName());
        mActivityLabel = info.getLabel().toString();
        mUserSerial = manager.getSerialNumberForUser(info.getUser());
    }

    /**
     * This is a constructor used for manual {@code LaunchableActivity} creation.
     *
     * @param intent The {@link Intent} to create this from.
     * @param label  The label to construct this object with.
     * @param icon   The icon to use for this object. If null, the Android icon will be loaded.
     */
    public LaunchableActivity(@NonNull final Intent intent, @NonNull final String label,
                              @DrawableRes final int icon) {
        mLaunchIntent = intent;
        mActivityLabel = label;
        mUserSerial = Long.MIN_VALUE;
    }

    /**
     * This is the constructor for LaunchableActivities, used in a {@link LaunchableAdapter}, for
     * APIs 15-20.
     *
     * @param info    Information to derive the LaunchableActivity from.
     * @param prefs   The {@link SharedPreferences} to load the label for this from.
     * @param manager The {@link PackageManager} to load the label for this from. If null, the
     *                local store will not cache the label.
     */
    @DeprecatedSinceApi(api = Build.VERSION_CODES.N, message = "Later APIs use addToAdapter24()")
    public LaunchableActivity(@NonNull final ResolveInfo info,
                              @NonNull final SharedPreferences prefs,
                              @Nullable final PackageManager manager) {
        final ActivityInfo activityInfo = info.activityInfo;
        final ComponentName name =
                new ComponentName(activityInfo.packageName, activityInfo.name);
        mLaunchIntent = getLaunchableIntent(name);

        /* Returns the actual label from the info and stores it locally, or retrieve it locally. */
        if (prefs.contains(activityInfo.packageName) && manager != null) {
            mActivityLabel = prefs.getString(activityInfo.packageName, null);
        } else {
            mActivityLabel = info.loadLabel(manager).toString();
            prefs.edit().putString(activityInfo.packageName, mActivityLabel).apply();
        }

        mUserSerial = Long.MIN_VALUE;
    }

    private static Intent getLaunchableIntent(final ComponentName componentName) {
        final Intent launchIntent = Intent.makeMainActivity(componentName);

        launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

        return launchIntent;
    }

    public boolean isUserKnown() {
        return mUserSerial != Long.MIN_VALUE;
    }

    /**
     * The user serial, to be used to retrieve a {@link android.os.UserHandle} as necessary.
     *
     * @return A user serial, {@code Long.MIN_VALUE} if there is no user serial assigned to this
     * object.
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public long getUserSerial() {
        return mUserSerial;
    }

    public ComponentName getComponent() {
        return mLaunchIntent.getComponent();
    }

    public Intent getLaunchIntent() {
        return mLaunchIntent;
    }

    public int getPriority() {
        return mPriority;
    }

    public void setPriority(final int priority) {
        mPriority = priority;
    }

    @Override
    public String toString() {
        return mActivityLabel;
    }
}
