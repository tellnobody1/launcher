package github.tellnobody1.launcher;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

public class IconVectorDrawable {
    private final VectorDrawableCompat drawable;

    public static IconVectorDrawable apply(Context ctx, int icon) {
        var d = AppCompatResources.getDrawable(ctx, icon);
        return d != null ? new IconVectorDrawable(d) : null;
    }

    public IconVectorDrawable(Drawable drawable) {
        this.drawable = (VectorDrawableCompat) drawable;
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
