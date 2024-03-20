package github.tellnobody1.launcher;

import android.content.Context;
import android.content.res.Configuration;
import java.util.*;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.*;
import static java.util.Locale.ENGLISH;

public class AppLocales {
    /** @return english + defaults + assets */
    static Set<Locale> getLabelLocales(Configuration configuration) {
        var locales = new HashSet<>(List.of(ENGLISH));
        // add default locales
        if (SDK_INT >= N) {
            var defaults = configuration.getLocales();
            for (var i = 0; i < defaults.size(); i++) locales.add(defaults.get(i));
        } else {
            locales.add(Locale.getDefault());
        }
        // add assets locales
        for (var asset : LocaleConfig.LOCALES)
            if (SDK_INT >= LOLLIPOP) {
                locales.add(Locale.forLanguageTag(asset));
            } else {
                var parts = asset.split("-");
                locales.add(parts.length >= 2 ? new Locale(parts[0], parts[1]) : new Locale(parts[0]));
            }
        return locales;
    }

    static Locale getDefault(Context ctx) {
        return SDK_INT >= N ? ctx.getResources().getConfiguration().getLocales().get(0) : Locale.getDefault();
    }
}
