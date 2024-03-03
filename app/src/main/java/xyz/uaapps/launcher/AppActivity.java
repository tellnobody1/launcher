package xyz.uaapps.launcher;

import android.content.ComponentName;
import android.content.Intent;

import java.util.Set;

public interface AppActivity {
    String getActivityLabel();
    String getIconKey();
}

interface IntentAppActivity extends AppActivity {
    Intent getLaunchIntent();
}

interface RegularAppActivity extends AppActivity {
    ComponentName getComponent();

    void setFavorite(boolean priority);
    boolean isFavorite();
    String getId();

    Set<String> getLabels();
}

interface RegularUserAppActivity extends RegularAppActivity {
    long getUserSerial();
}

interface RegularIntentAppActivity extends RegularAppActivity, IntentAppActivity {
}
