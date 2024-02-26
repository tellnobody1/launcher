package xyz.uaapps.launcher;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.N;
import static java.util.Locale.ENGLISH;

import android.content.res.Configuration;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

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

    static Locale getDefault(Configuration configuration) {
        return SDK_INT >= N ? configuration.getLocales().get(0) : Locale.getDefault();
    }
}
