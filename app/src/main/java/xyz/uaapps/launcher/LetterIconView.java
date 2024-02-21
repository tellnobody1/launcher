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

public class LetterIconView extends View {

    private Paint paint;
    private String letter;
    private Integer hash;
    private VectorDrawable vectorDrawable;

    public LetterIconView(Context context) {
        super(context);
        init();
    }

    public LetterIconView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LetterIconView(Context context, AttributeSet attrs, int defStyleAttr) {
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
        icons.put("AmicPay", R.drawable.local_gas_station);
        icons.put("Aurora Store", R.drawable.storefront);
        icons.put("Book Reader", R.drawable.book);
        icons.put("F-Droid", R.drawable.storefront);
        icons.put("Files", R.drawable.folder);
        icons.put("Gmail", R.drawable.mail);
        icons.put("Google Фото", R.drawable.photo_library);
        icons.put("HELSI", R.drawable.local_hospital);
        icons.put("IBKR", R.drawable.timeline);
        icons.put("Joplin", R.drawable.notes);
        icons.put("Medikom", R.drawable.local_hospital);
        icons.put("NewPipe", R.drawable.live_tv);
        icons.put("Organic Maps", R.drawable.map);
        icons.put("Play Маркет", R.drawable.storefront);
        icons.put("ProCredit Ukraine", R.drawable.account_balance);
        icons.put("QR & Barcode Scanner", R.drawable.qr_code_scanner);
        icons.put("Sense SuperApp", R.drawable.account_balance);
        icons.put("Spotify", R.drawable.music_note);
        icons.put("Tabletki.ua", R.drawable.local_pharmacy);
        icons.put("Weather", R.drawable.partly_cloudy_day);
        icons.put("WhatsApp", R.drawable.chat);
        icons.put("monobank", R.drawable.account_balance);
        icons.put("Беббо", R.drawable.child_care);
        icons.put("Годинник", R.drawable.alarm);
        icons.put("Диктофон", R.drawable.graphic_eq);
        icons.put("Диск", R.drawable.folder);
        icons.put("Дія", R.drawable.badge);
        icons.put("Змійка", R.drawable.videogame_asset);
        icons.put("Календар", R.drawable.calendar_month);
        icons.put("Калькулятор", R.drawable.calculate);
        icons.put("Камера", R.drawable.photo_camera);
        icons.put("Карти", R.drawable.map);
        icons.put("Контакти", R.drawable.contacts_product);
        icons.put("Мапа тривог України", R.drawable.destruction);
        icons.put("Менеджер Файлів", R.drawable.folder);
        icons.put("Налаштування", R.drawable.settings);
        icons.put("Об’єктив", R.drawable.filter_center_focus);
        icons.put("Повідомлення", R.drawable.sms);
        icons.put("Сільпо", R.drawable.grocery);
        icons.put("Телефон", R.drawable.call);
        icons.put("Фора", R.drawable.grocery);
    }
}
