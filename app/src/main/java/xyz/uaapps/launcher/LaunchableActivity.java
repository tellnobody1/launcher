package xyz.uaapps.launcher;

import android.content.ComponentName;
import android.content.Intent;

import java.util.Set;

public interface LaunchableActivity {
    String getActivityLabel();
    String getIconKey();
    Set<String> getLabels();
}

interface IntentLaunchableActivity extends LaunchableActivity {
    Intent getLaunchIntent();
}

interface RegularLaunchableActivity extends LaunchableActivity {
    ComponentName getComponent();

    void setPriority(int priority);
    int getPriority();
    String getName();
}

interface RegularUserLaunchableActivity extends RegularLaunchableActivity {
    long getUserSerial();
}

interface RegularIntentLaunchableActivity extends RegularLaunchableActivity, IntentLaunchableActivity {
}
