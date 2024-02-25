package xyz.uaapps.launcher;

import static android.content.Intent.ACTION_DIAL;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;

import java.util.Set;

public class DialLaunchableActivity implements LaunchableActivity {
    private final Intent intent;

    public DialLaunchableActivity(String phoneNumber) {
        this.intent = new Intent(ACTION_DIAL);
        this.intent.setData(Uri.parse("tel:" + phoneNumber));
    }

    public ComponentName getComponent() {
        return null;
    }

    public int getPriority() {
        return 1;
    }

    public boolean isUserKnown() {
        return false;
    }

    public long getUserSerial() {
        return 0;
    }

    public Intent getLaunchIntent() {
        return intent;
    }

    public String getLabelEn() {
        return null;
    }

    public Set<String> getLabels() {
        return null;
    }
}
