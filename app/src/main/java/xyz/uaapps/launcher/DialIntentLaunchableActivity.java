package xyz.uaapps.launcher;

import static android.content.Intent.ACTION_DIAL;

import android.content.Intent;
import android.net.Uri;

import java.util.Collections;
import java.util.Set;

public class DialIntentLaunchableActivity implements IntentLaunchableActivity {
    private final Intent intent;

    public DialIntentLaunchableActivity(String phoneNumber) {
        this.intent = new Intent(ACTION_DIAL);
        this.intent.setData(Uri.parse("tel:" + phoneNumber));
    }

    public String getActivityLabel() {
        return "Dial..."; //todo
    }

    public String getIconKey() {
        return "Phone"; //todo
    }

    public Set<String> getLabels() {
        return Collections.emptySet(); //todo
    }

    public Intent getLaunchIntent() {
        return intent;
    }
}
