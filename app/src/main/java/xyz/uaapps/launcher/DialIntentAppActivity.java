package xyz.uaapps.launcher;

import android.content.Intent;
import android.net.Uri;
import static android.content.Intent.ACTION_DIAL;

public class DialIntentAppActivity implements IntentAppActivity {
    private final Intent intent;
    private final String label;

    public DialIntentAppActivity(String phoneNumber, String label) {
        this.intent = new Intent(ACTION_DIAL);
        this.intent.setData(Uri.parse("tel:" + phoneNumber));
        this.label = label;
    }

    public String getActivityLabel() {
        return label;
    }

    public String getIconKey() {
        return "Phone";
    }

    public Intent getLaunchIntent() {
        return intent;
    }
}
