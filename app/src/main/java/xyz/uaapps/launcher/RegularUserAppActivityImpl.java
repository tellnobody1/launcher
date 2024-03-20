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
import android.content.pm.LauncherActivityInfo;
import android.os.UserManager;
import java.util.*;
import xyz.uaapps.launcher.AppActivity.RegularUserAppActivity;
import static android.content.Intent.*;
import static android.os.Build.VERSION_CODES.*;

public class RegularUserAppActivityImpl implements RegularUserAppActivity {
    private final String activityLabel;
    private final Set<String> labels;
    private final String iconKey;
    private final Intent launchIntent;
    /**
     * The user serial, to be used to retrieve a {@link android.os.UserHandle} as necessary.
     * Defined as {@code Long.MIN_VALUE} if there is no user serial assigned to this object.
     */
    private final long userSerial;
    private final String id;

    private boolean favorite;

    /**
     * This is the constructor for LaunchableActivities, used in a {@link AppsAdapter}
     *
     * @param info Information to derive the LaunchableActivity from.
     * @param manager The service to retrieve user information about the activity from.
     */
    @TargetApi(LOLLIPOP)
    public RegularUserAppActivityImpl(
            LauncherActivityInfo info,
            UserManager manager,
            Set<String> labels,
            String iconKey) {
        launchIntent = getLaunchableIntent(info.getComponentName());
        activityLabel = info.getLabel().toString();
        this.iconKey = iconKey;
        userSerial = manager.getSerialNumberForUser(info.getUser());
        id = String.format("%s@%s", info.getName(), userSerial);
        this.labels = labels;
    }

    @TargetApi(HONEYCOMB)
    private static Intent getLaunchableIntent(ComponentName componentName) {
        var launchIntent = Intent.makeMainActivity(componentName);
        launchIntent.setFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        return launchIntent;
    }

    /**
     * The user serial, to be used to retrieve a {@link android.os.UserHandle} as necessary.
     *
     * @return A user serial, {@code Long.MIN_VALUE} if there is no user serial assigned to this
     * object.
     */
    @TargetApi(JELLY_BEAN_MR1)
    public long getUserSerial() {
        return userSerial;
    }

    public ComponentName getComponent() {
        return launchIntent.getComponent();
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
        return activityLabel;
    }

    public String getIconKey() {
        return iconKey;
    }

    public String getId() {
        return id;
    }
}
