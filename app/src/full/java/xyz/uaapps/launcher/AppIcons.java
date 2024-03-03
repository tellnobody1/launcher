package xyz.uaapps.launcher;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AppIcons {
    private static final Map<String, Integer> icons = new HashMap<>();

    public static Map<String, Integer> getIcons() {
        return Collections.unmodifiableMap(icons);
    }

    static {
        icons.put("AmicPay", R.drawable.ic_local_gas_station);
        icons.put("Aurora Store", R.drawable.ic_storefront);
        icons.put("Authenticator", R.drawable.ic_shield_lock);
        icons.put("Bebbo", R.drawable.ic_child_care);
        icons.put("Bitwarden", R.drawable.ic_password);
        icons.put("Book Reader", R.drawable.ic_book);
        icons.put("Browser", R.drawable.ic_public);
        icons.put("Calculator", R.drawable.ic_calculate);
        icons.put("Calendar", R.drawable.ic_calendar_month);
        icons.put("Camera", R.drawable.ic_photo_camera);
        icons.put("ChatGPT", R.drawable.ic_smart_toy);
        icons.put("Chrome", R.drawable.ic_public);
        icons.put("Clock", R.drawable.ic_alarm);
        icons.put("Contacts", R.drawable.ic_contacts_product);
        icons.put("Drive", R.drawable.ic_folder);
        icons.put("F-Droid Basic", R.drawable.ic_storefront);
        icons.put("F-Droid", R.drawable.ic_storefront);
        icons.put("Fennec", R.drawable.ic_public);
        icons.put("File Manager", R.drawable.ic_folder);
        icons.put("Files by Google", R.drawable.ic_folder);
        icons.put("Files", R.drawable.ic_folder);
        icons.put("Gallery", R.drawable.ic_photo_library);
        icons.put("Gmail", R.drawable.ic_mail);
        icons.put("Google Play Store", R.drawable.ic_storefront);
        icons.put("HELSI", R.drawable.ic_local_hospital);
        icons.put("IBKR", R.drawable.ic_timeline);
        icons.put("Joplin", R.drawable.ic_notes);
        icons.put("Kindle", R.drawable.ic_book);
        icons.put("Lens", R.drawable.ic_filter_center_focus);
        icons.put("Maps", R.drawable.ic_map);
        icons.put("Medikom", R.drawable.ic_local_hospital);
        icons.put("Messages", R.drawable.ic_sms);
        icons.put("Music", R.drawable.ic_music_note);
        icons.put("NewPipe", R.drawable.ic_live_tv);
        icons.put("Openreads", R.drawable.ic_book);
        icons.put("Organic Maps", R.drawable.ic_map);
        icons.put("Phone", R.drawable.ic_call);
        icons.put("Photos", R.drawable.ic_photo_library);
        icons.put("ProCredit Ukraine", R.drawable.ic_account_balance);
        icons.put("QR & Barcode Scanner", R.drawable.ic_qr_code_scanner);
        icons.put("Recorder", R.drawable.ic_graphic_eq);
        icons.put("Sense SuperApp", R.drawable.ic_account_balance);
        icons.put("Settings", R.drawable.ic_settings);
        icons.put("Snake", R.drawable.ic_videogame_asset);
        icons.put("Spotify", R.drawable.ic_music_note);
        icons.put("Tabletki.ua", R.drawable.ic_local_pharmacy);
        icons.put("Weather", R.drawable.ic_partly_cloudy_day);
        icons.put("WhatsApp", R.drawable.ic_chat);
        icons.put("monobank", R.drawable.ic_account_balance);
        icons.put("Дія", R.drawable.ic_badge);
        icons.put("Мапа тривог України", R.drawable.ic_destruction);
        icons.put("Сільпо", R.drawable.ic_grocery);
        icons.put("Фора", R.drawable.ic_grocery);
    }
}
