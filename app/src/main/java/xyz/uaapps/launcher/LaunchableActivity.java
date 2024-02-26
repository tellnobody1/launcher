package xyz.uaapps.launcher;

import android.content.ComponentName;
import android.content.Intent;

import java.util.Set;

public interface LaunchableActivity {
    String getActivityLabel();
    String getLabelEn(); //todo getIconLabel
    Set<String> getLabels();
}

interface UserLaunchableActivity extends RegularLaunchableActivity {
    long getUserSerial();
}

interface IntentLaunchableActivity extends RegularLaunchableActivity, HasIntent {
}

interface VirtualLaunchableActivity extends LaunchableActivity, HasIntent {
}

interface Pinnable {
    void setPriority(int priority);
    int getPriority();
    String getName();
}

interface Regular extends Pinnable {
    ComponentName getComponent();
}

interface RegularLaunchableActivity extends Regular, LaunchableActivity {
}

interface HasIntent {
    Intent getLaunchIntent();
}
