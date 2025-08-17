
package com.G2Preview.iGsd.ui;



import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class BaselineOverlay extends View {

    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();

    /** 圆角半径（px，普通平台圆角用） */
    private float cornerRadiusPx = 0f;

    /** 内容内缩（px），给红色块四周留一点空隙，可选 */
    private float insetPx = 0f;

    public BaselineOverlay(Context c) { super(c); init(); }
    public BaselineOverlay(Context c, AttributeSet a) { super(c, a); init(); }
    public BaselineOverlay(Context c, AttributeSet a, int d) { super(c, a, d); init(); }

    private void init() {
        setWillNotDraw(false);
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(0xFFFF0000); // 红色填充
    }

    /** dp -> px */
    private float dp(float v) {
        return v * getResources().getDisplayMetrics().density;
    }

    /** 设置圆角半径（px） */
    public void setCornerRadiusPx(float r) {
        if (r < 0) r = 0;
        if (Math.abs(cornerRadiusPx - r) > 0.5f) {
            cornerRadiusPx = r;
            invalidate();
        }
    }

    /** 设置圆角半径（dp） */
    public void setCornerRadiusDp(float rDp) {
        setCornerRadiusPx(dp(rDp));
    }

    /** 设置内缩（px），让红色块不贴边，可选 */
    public void setInsetPx(float insetPx) {
        if (insetPx < 0) insetPx = 0;
        if (Math.abs(this.insetPx - insetPx) > 0.5f) {
            this.insetPx = insetPx;
            invalidate();
        }
    }

    /** 设置内缩（dp） */
    public void setInsetDp(float insetDp) {
        setInsetPx(dp(insetDp));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        final int w = getWidth(), h = getHeight();
        if (w <= 0 || h <= 0) return;

        // 计算绘制区域（留出内缩）
        rect.set(insetPx, insetPx, w - insetPx, h - insetPx);

        // 平台普通圆角
        float r = Math.min(cornerRadiusPx, Math.min(rect.width(), rect.height()) / 2f);
        canvas.drawRoundRect(rect, r, r, fillPaint);
    }
}

