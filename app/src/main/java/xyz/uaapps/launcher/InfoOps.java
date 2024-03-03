package xyz.uaapps.launcher;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;

import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;

import androidx.annotation.RequiresApi;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public abstract class InfoOps<Info> {
    protected final Info info;
    protected final PackageManager pm;

    public InfoOps(Info info, PackageManager pm) {
        this.info = info;
        this.pm = pm;
    }

    public final Map<Locale, String> getLabels(Set<Locale> locales) {
        cacheDefaultLabel();
        try {
            var res = pm.getResourcesForApplication(getAppInfo());
            var cfg = res.getConfiguration();
            var labels = new HashMap<Locale, String>();
            for (var locale : locales) {
                if (SDK_INT >= JELLY_BEAN_MR1) cfg.setLocale(locale);
                else cfg.locale = locale;
                var res2 = new Resources(res.getAssets(), res.getDisplayMetrics(), cfg);
                CharSequence label;
                try {
                    label = res2.getText(getLabelRes());
                } catch (Resources.NotFoundException ignored) {
                    label = fallback(res2);
                }
                labels.put(locale, label.toString().replace(LRM, "").replace(RLM, ""));
            }
            return unmodifiableMap(labels);
        } catch (NameNotFoundException ignored) {}
        return emptyMap();
    }

    protected abstract void cacheDefaultLabel();
    protected abstract ApplicationInfo getAppInfo();
    protected abstract int getLabelRes();
    protected abstract CharSequence fallback(Resources res);

    private static final CharSequence LRM = "\u200E";
    private static final CharSequence RLM = "\u200F";
}

@RequiresApi(api = LOLLIPOP)
class LauncherActivityInfoOps extends InfoOps<LauncherActivityInfo> {
    public LauncherActivityInfoOps(LauncherActivityInfo info, PackageManager pm) {
        super(info, pm);
    }

    protected void cacheDefaultLabel() {
        info.getLabel();
    }

    protected ApplicationInfo getAppInfo() {
        return info.getApplicationInfo();
    }

    protected int getLabelRes() {
        return info.getApplicationInfo().labelRes;
    }

    protected CharSequence fallback(Resources res) {
        return info.getLabel();
    }
}

class ResolveInfoOps extends InfoOps<ResolveInfo> {
    public ResolveInfoOps(ResolveInfo info, PackageManager pm) {
        super(info, pm);
    }

    protected void cacheDefaultLabel() {
        info.loadLabel(pm);
    }

    protected ApplicationInfo getAppInfo() {
        return info.activityInfo.applicationInfo;
    }

    protected int getLabelRes() {
        return info.activityInfo.labelRes;
    }

    protected CharSequence fallback(Resources res) {
        try {
            return res.getText(info.activityInfo.applicationInfo.labelRes);
        } catch (Resources.NotFoundException ignored) {
            return info.loadLabel(pm);
        }
    }
}
