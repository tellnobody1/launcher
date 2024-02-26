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

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.LOLLIPOP;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.LauncherActivityInfo;
import android.os.UserManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.util.Collections;
import java.util.Set;

public class RegularUserLaunchableActivityImpl implements RegularUserLaunchableActivity {

    private final String mActivityLabel;
    private final Set<String> labels;
    @Nullable private final String iconKey;

    private final Intent mLaunchIntent;

    /**
     * The user serial, to be used to retrieve a {@link android.os.UserHandle} as necessary.
     * Defined as {@code Long.MIN_VALUE} if there is no user serial assigned to this object.
     */
    private final long mUserSerial;

    private int mPriority;

    /**
     * This is the constructor for LaunchableActivities, used in a {@link LaunchableAdapter}
     *
     * @param info Information to derive the LaunchableActivity from.
     * @param manager The service to retrieve user information about the activity from.
     */
    @RequiresApi(api = LOLLIPOP)
    public RegularUserLaunchableActivityImpl(
            @NonNull LauncherActivityInfo info,
            UserManager manager,
            Set<String> labels,
            @Nullable String iconKey) {
        mLaunchIntent = getLaunchableIntent(info.getComponentName());
        mActivityLabel = info.getLabel().toString();
        this.iconKey = iconKey;
        mUserSerial = manager.getSerialNumberForUser(info.getUser());
        this.labels = labels;
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
    @RequiresApi(api = JELLY_BEAN_MR1)
    public long getUserSerial() {
        return mUserSerial;
    }

    public ComponentName getComponent() {
        return mLaunchIntent.getComponent();
    }

    public int getPriority() {
        return mPriority;
    }

    public void setPriority(int priority) {
        mPriority = priority;
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
