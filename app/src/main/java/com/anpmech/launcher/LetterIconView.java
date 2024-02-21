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
    private String letter;
    private int backgroundColor;

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
        letter = "•";
        backgroundColor = Color.GRAY;
    }

    public void setLetter(String letter) {
        this.letter = letter;
        invalidate();
    }

    public void setBackgroundColor(int color) {
        this.backgroundColor = color;
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
        float radius = getWidth() / 2f;
        float centerY = getHeight() / 2f;

        paint.setColor(backgroundColor);
        canvas.drawCircle(radius, centerY, radius, paint);

        float centerY2 = centerY - (paint.ascent() + paint.descent()) / 2f;

        paint.setColor(Color.WHITE);
        canvas.drawText(letter, radius, centerY2, paint);
    }
}
