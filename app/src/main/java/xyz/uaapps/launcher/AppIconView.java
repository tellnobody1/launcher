package xyz.uaapps.launcher;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static xyz.uaapps.launcher.AppIcons.getIcons;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.VectorDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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

    public void set(CharSequence label, @Nullable String iconKey) {
        if (iconKey != null && getIcons().containsKey(iconKey) && SDK_INT >= LOLLIPOP) {
            vectorDrawable = (VectorDrawable) getContext().getDrawable(getIcons().get(iconKey));
            vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
            letter = null;
        } else if (label.length() > 0) {
            vectorDrawable = null;
            letter = String.valueOf(label.charAt(0)).toUpperCase();
        }
        hash = label.hashCode();
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
}
