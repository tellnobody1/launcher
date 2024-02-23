package xyz.uaapps.launcher;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;

import android.content.pm.LauncherActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;

import androidx.annotation.RequiresApi;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class LauncherActivityInfoOps {
    private final LauncherActivityInfo info;

    public LauncherActivityInfoOps(LauncherActivityInfo info) {
        this.info = info;
    }

    @RequiresApi(api = LOLLIPOP)
    public Map<Locale, String> getLabels(Set<Locale> locales, PackageManager pm) {
        cacheDefaultLabel();
        var appInfo = info.getApplicationInfo();
        try {
            var res = pm.getResourcesForApplication(appInfo);
            var cfg = res.getConfiguration();
            var labels = new HashMap<Locale, String>();
            for (Locale locale : locales) {
                cfg.setLocale(locale);
                var res2 = new Resources(res.getAssets(), res.getDisplayMetrics(), cfg);
                CharSequence label;
                try {
                    label = res2.getText(appInfo.labelRes);
                } catch (Resources.NotFoundException ignored) {
                    label = info.getLabel();
                }
                labels.put(locale, label.toString());
            }
            return unmodifiableMap(labels);
        } catch (NameNotFoundException ignored) {}
        return emptyMap();
    }

    @RequiresApi(api = LOLLIPOP)
    private void cacheDefaultLabel() {
        info.getLabel();
    }
}
