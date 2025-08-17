
package com.G2Preview.iGsd.shape;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import com.G2Preview.iGsd.core.LayoutDir;

public class G2ShapeDrawable extends Drawable {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final G2RoundedCornerShape shape;
    private LayoutDir layoutDir = LayoutDir.LTR;
    private Path path;

    public G2ShapeDrawable(int color, G2RoundedCornerShape shape) {
        this.shape = shape;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
    }

    public void setColor(int color){ paint.setColor(color); invalidateSelf(); }
    public void setLayoutDir(LayoutDir dir){ this.layoutDir = dir; updatePath(getBounds()); invalidateSelf(); }

    @Override protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        updatePath(bounds);
    }

    private void updatePath(Rect b) {
        float w = Math.max(0, b.width());
        float h = Math.max(0, b.height());
        this.path = shape.createPath(w, h, layoutDir);
        if (this.path != null) this.path.offset(b.left, b.top);
    }

    @Override public void draw(Canvas canvas) {
        if (path != null) canvas.drawPath(path, paint);
    }

    @Override public void setAlpha(int alpha) { paint.setAlpha(alpha); invalidateSelf(); }
    @Override public void setColorFilter(android.graphics.ColorFilter cf) { paint.setColorFilter(cf); invalidateSelf(); }
    @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
}

