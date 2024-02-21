package com.anpmech.launcher;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;

public class LetterIconView extends View {

    private Paint paint;
    private String letter = String.valueOf('•');
    private int hash = letter.hashCode();

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
        //todo 24?
        paint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 24, getResources().getDisplayMetrics()));
    }

    public void set(String name) {
        if (name.length() > 0) {
            this.letter = String.valueOf(name.charAt(0));
            this.hash = name.hashCode();
            invalidate();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 56, getResources().getDisplayMetrics());
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        var radius = getWidth() / 2f;
        var centerY = getHeight() / 2f;

        var backgroundColor = Color.rgb(
            (int) ((hash & 0xFF0000) >> 16) / 2 + 128,
            (int) ((hash & 0x00FF00) >> 8) / 2 + 128,
            (int) (hash & 0x0000FF) / 2 + 128);

        paint.setColor(backgroundColor);
        paint.setAlpha(200);
        canvas.drawCircle(radius, centerY, radius, paint);

        var centerY2 = centerY - (paint.ascent() + paint.descent()) / 2f;

        int onBackground = isColorDark(backgroundColor) ? Color.WHITE : Color.BLACK;

        paint.setColor(onBackground);
        paint.setAlpha(200);
        canvas.drawText(letter, radius, centerY2, paint);

    }

    private static boolean isColorDark(int color) {
        double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        return darkness >= 0.5;
    }
}
