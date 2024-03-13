package xyz.uaapps.launcher;

import android.content.Context;
import android.graphics.*;
import android.util.*;
import android.view.View;
import static xyz.uaapps.launcher.AppIcons.getIcons;

public class AppIconView extends View {
    private Paint paint;
    private String letter;
    private Integer hash;
    private IconVectorDrawable vectorDrawable;

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

    public void set(CharSequence label, String iconKey) {
        if (iconKey != null && getIcons().containsKey(iconKey)) {
            vectorDrawable = IconVectorDrawable.apply(getContext(), getIcons().get(iconKey));
            if (vectorDrawable != null) {
                letter = null;
                vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
            } else {
                setLetter(label);
            }
        } else {
            setLetter(label);
        }
        hash = label.hashCode();
        invalidate();
    }

    private void setLetter(CharSequence label) {
        vectorDrawable = null;
        if (label.length() > 0)
            letter = String.valueOf(label.charAt(0)).toUpperCase();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 56, getResources().getDisplayMetrics());
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
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
