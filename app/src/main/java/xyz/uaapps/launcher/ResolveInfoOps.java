package xyz.uaapps.launcher;

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;

import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ResolveInfoOps {
    private final ResolveInfo info;
    private final PackageManager pm;

    public ResolveInfoOps(ResolveInfo info, PackageManager pm) {
        this.info = info;
        this.pm = pm;
    }

    public Map<Locale, String> getLabels(Set<Locale> locales) {
        cacheDefaultLabel();
        var appInfo = info.activityInfo.applicationInfo;
        try {
            var res = pm.getResourcesForApplication(appInfo);
            var cfg = res.getConfiguration();
            var labels = new HashMap<Locale, String>();
            for (Locale locale : locales) {
                cfg.locale = locale;
                var res2 = new Resources(res.getAssets(), res.getDisplayMetrics(), cfg);
                CharSequence label;
                try {
                    label = res2.getText(info.activityInfo.labelRes);
                } catch (Resources.NotFoundException ignored) {
                    try {
                        label = res2.getText(appInfo.labelRes);
                    } catch (Resources.NotFoundException ignored2) {
                        label = info.loadLabel(pm);
                    }
                }
                labels.put(locale, label.toString());
            }
            return unmodifiableMap(labels);
        } catch (PackageManager.NameNotFoundException ignored) {}
        return emptyMap();
    }

    private void cacheDefaultLabel() {
        info.loadLabel(pm);
    }
}
