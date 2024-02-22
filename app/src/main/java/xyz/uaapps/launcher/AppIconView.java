package xyz.uaapps.launcher;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.VectorDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

public class AppIconView extends View {
    private Paint paint;
    private String letter;
    private Integer hash;
    private VectorDrawable vectorDrawable;

    public AppIconView(Context context) {
        super(context);
        init();
    }

    public AppIconView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AppIconView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 24, getResources().getDisplayMetrics()));
    }

    public void set(String name) {
        if (icons.containsKey(name) && SDK_INT >= LOLLIPOP) {
            vectorDrawable = (VectorDrawable) getContext().getDrawable(icons.get(name));
            vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
            letter = null;
        } else if (name.length() > 0) {
            vectorDrawable = null;
            letter = String.valueOf(name.charAt(0)).toUpperCase();
        }
        hash = name.hashCode();
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 56, getResources().getDisplayMetrics());
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        var centerX = getWidth() / 2f;
        var centerY = getHeight() / 2f;

        if (hash != null) {
            var radius = getWidth() / 2f;
            var backgroundColor = Color.rgb(
                    (int) ((hash & 0xFF0000) >> 16) / 2 + 128,
                    (int) ((hash & 0x00FF00) >> 8) / 2 + 128,
                    (int) (hash & 0x0000FF) / 2 + 128);

            paint.setColor(backgroundColor);
            paint.setAlpha(200);
            canvas.drawCircle(centerX, centerY, radius, paint);

            if (vectorDrawable != null) {
                int drawableWidth = vectorDrawable.getIntrinsicWidth();
                int drawableHeight = vectorDrawable.getIntrinsicHeight();
                int left = (int) centerX - drawableWidth / 2;
                int top = (int) centerY - drawableHeight / 2;
                canvas.save();
                canvas.translate(left, top);
                vectorDrawable.draw(canvas);
                canvas.restore();
            } else if (letter != null) {
                var centerY2 = centerY - (paint.ascent() + paint.descent()) / 2f;
                int onBackground = isColorDark(backgroundColor) ? Color.WHITE : Color.BLACK;
                paint.setColor(onBackground);
                paint.setAlpha(200);
                canvas.drawText(letter, centerX, centerY2, paint);
            }
        }
    }

    private static boolean isColorDark(int color) {
        double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        return darkness >= 0.5;
    }

    private static final Map<String, Integer> icons = new HashMap<>();
    static {
        icons.put("AmicPay", R.drawable.ic_local_gas_station);
        icons.put("Aurora Store", R.drawable.ic_storefront);
        icons.put("Book Reader", R.drawable.ic_book);
        icons.put("ChatGPT", R.drawable.ic_smart_toy);
        icons.put("Chrome", R.drawable.ic_public);
        icons.put("F-Droid", R.drawable.ic_storefront);
        icons.put("Fennec", R.drawable.ic_public);
        icons.put("Files", R.drawable.ic_folder);
        icons.put("Gmail", R.drawable.ic_mail);
        icons.put("Google Фото", R.drawable.ic_photo_library);
        icons.put("HELSI", R.drawable.ic_local_hospital);
        icons.put("IBKR", R.drawable.ic_timeline);
        icons.put("Joplin", R.drawable.ic_notes);
        icons.put("Medikom", R.drawable.ic_local_hospital);
        icons.put("NewPipe", R.drawable.ic_live_tv);
        icons.put("Organic Maps", R.drawable.ic_map);
        icons.put("Play Маркет", R.drawable.ic_storefront);
        icons.put("ProCredit Ukraine", R.drawable.ic_account_balance);
        icons.put("QR & Barcode Scanner", R.drawable.ic_qr_code_scanner);
        icons.put("Sense SuperApp", R.drawable.ic_account_balance);
        icons.put("Spotify", R.drawable.ic_music_note);
        icons.put("Tabletki.ua", R.drawable.ic_local_pharmacy);
        icons.put("Weather", R.drawable.ic_partly_cloudy_day);
        icons.put("WhatsApp", R.drawable.ic_chat);
        icons.put("monobank", R.drawable.ic_account_balance);
        icons.put("Беббо", R.drawable.ic_child_care);
        icons.put("Годинник", R.drawable.ic_alarm);
        icons.put("Диктофон", R.drawable.ic_graphic_eq);
        icons.put("Диск", R.drawable.ic_folder);
        icons.put("Дія", R.drawable.ic_badge);
        icons.put("Змійка", R.drawable.ic_videogame_asset);
        icons.put("Календар", R.drawable.ic_calendar_month);
        icons.put("Калькулятор", R.drawable.ic_calculate);
        icons.put("Камера", R.drawable.ic_photo_camera);
        icons.put("Карти", R.drawable.ic_map);
        icons.put("Контакти", R.drawable.ic_contacts_product);
        icons.put("Мапа тривог України", R.drawable.ic_destruction);
        icons.put("Менеджер Файлів", R.drawable.ic_folder);
        icons.put("Налаштування", R.drawable.ic_settings);
        icons.put("Об’єктив", R.drawable.ic_filter_center_focus);
        icons.put("Повідомлення", R.drawable.ic_sms);
        icons.put("Сільпо", R.drawable.ic_grocery);
        icons.put("Телефон", R.drawable.ic_call);
        icons.put("Фора", R.drawable.ic_grocery);
    }
}
