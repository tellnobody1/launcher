package github.tellnobody1.launcher;

import android.annotation.TargetApi;
import android.content.pm.*;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.*;
import java.util.*;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.*;
import static java.util.Collections.*;

public abstract class InfoOps<Info> {
    protected final Info info;
    protected final PackageManager pm;

    public InfoOps(Info info, PackageManager pm) {
        this.info = info;
        this.pm = pm;
    }

    public final Map<Locale, String> getLabels(Set<Locale> locales) {
        cacheDefaultLabel();
        Resources res = null;
        Locale defaultLocale = null;
        try {
            res = pm.getResourcesForApplication(getAppInfo());
            var cfg = res.getConfiguration();
            defaultLocale = cfg.locale;
            var labels = new HashMap<Locale, String>();
            for (var locale : locales) {
                setLocale(locale, cfg);
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
        } catch (NameNotFoundException ignored) {} finally {
            restoreLocale(defaultLocale, res);
        }
        return emptyMap();
    }

    private void restoreLocale(Locale locale, Resources res) {
        if (locale != null) {
            var cfg = res.getConfiguration();
            setLocale(locale, cfg);
            res.updateConfiguration(cfg, res.getDisplayMetrics());
        }
    }

    private void setLocale(Locale locale, Configuration cfg) {
        if (SDK_INT >= JELLY_BEAN_MR1) cfg.setLocale(locale);
        else cfg.locale = locale;
    }

    protected abstract void cacheDefaultLabel();
    protected abstract ApplicationInfo getAppInfo();
    protected abstract int getLabelRes();
    protected abstract CharSequence fallback(Resources res);

    private static final CharSequence LRM = "\u200E";
    private static final CharSequence RLM = "\u200F";
}

@TargetApi(LOLLIPOP)
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
