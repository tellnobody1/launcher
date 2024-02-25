package xyz.uaapps.launcher;

import android.content.ComponentName;
import android.content.Intent;

import java.util.Set;

public interface LaunchableActivity {
    ComponentName getComponent();

    void setPriority(int priority);

    int getPriority();

    boolean isUserKnown();

    long getUserSerial();

    Intent getLaunchIntent();

    String getLabelEn();

    Set<String> getLabels();
}
