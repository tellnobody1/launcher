package xyz.uaapps.launcher;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;

public class IconVectorDrawable {
    private final VectorDrawable drawable;

    public static IconVectorDrawable apply(Context ctx, int icon) {
        var d = ctx.getDrawable(icon);
        return d != null ? new IconVectorDrawable(d) : null;
    }

    public IconVectorDrawable(Drawable drawable) {
        this.drawable = (VectorDrawable) drawable;
    }

    public void setBounds(int left, int top, int right, int bottom) {
        drawable.setBounds(left, top, right, bottom);
    }

    public int getIntrinsicWidth() {
        return drawable.getIntrinsicWidth();
    }

    public int getIntrinsicHeight() {
        return drawable.getIntrinsicHeight();
    }

    public void draw(Canvas canvas) {
        drawable.draw(canvas);
    }
}
