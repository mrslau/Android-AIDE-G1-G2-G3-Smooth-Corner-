
package com.G2Preview.iGsd.shape;

import android.graphics.Path;
import android.util.SizeF;
import com.G2Preview.iGsd.core.LayoutDir;
import com.G2Preview.iGsd.corner.AbsoluteCornerSize;
import com.G2Preview.iGsd.corner.CornerSize;
import com.G2Preview.iGsd.corner.CornerSmoothness;
import com.G2Preview.iGsd.corner.RelativeCornerSize;

public class G2RoundedCornerShape {

    private final CornerSize topStart;
    private final CornerSize topEnd;
    private final CornerSize bottomEnd;
    private final CornerSize bottomStart;
    private final CornerSmoothness smooth;

    public G2RoundedCornerShape(CornerSize topStart,
                                CornerSize topEnd,
                                CornerSize bottomEnd,
                                CornerSize bottomStart,
                                CornerSmoothness smooth) {
        this.topStart = topStart;
        this.topEnd = topEnd;
        this.bottomEnd = bottomEnd;
        this.bottomStart = bottomStart;
        this.smooth = smooth;
    }

    // 工厂：等价 Compose 各种重载
    public static G2RoundedCornerShape allPx(float px, CornerSmoothness s) {
        CornerSize c = new AbsoluteCornerSize(px);
        return new G2RoundedCornerShape(c, c, c, c, s);
    }
    public static G2RoundedCornerShape allPercent(int percent, CornerSmoothness s) {
        CornerSize c = new RelativeCornerSize(percent);
        return new G2RoundedCornerShape(c, c, c, c, s);
    }
    public static G2RoundedCornerShape capsule(CornerSmoothness s) {
        return allPercent(50, s);
    }

    /**
     * 生成 Path（会考虑 LTR/RTL，把 start/end 映射到 left/right）
     */
    public Path createPath(float width, float height, LayoutDir dir) {
        SizeF size = new SizeF(width, height);
        float centerX = width / 2f, centerY = height / 2f;
        float maxR = Math.min(centerX, centerY);

        // 先把四个 CornerSize 转为像素
        float ts = clamp(topStart.toPx(size), 0, maxR);
        float te = clamp(topEnd.toPx(size), 0, maxR);
        float be = clamp(bottomEnd.toPx(size), 0, maxR);
        float bs = clamp(bottomStart.toPx(size), 0, maxR);

        // 根据方向映射
        float topLeft, topRight, bottomRight, bottomLeft;
        if (dir == LayoutDir.LTR) {
            topLeft = ts; topRight = te; bottomRight = be; bottomLeft = bs;
        } else {
            topLeft = te; topRight = ts; bottomRight = bs; bottomLeft = be;
        }

        // 圆角全为 0：矩形
        if ((topLeft + topRight + bottomRight + bottomLeft) == 0f) {
            Path p = new Path();
            p.addRect(0, 0, width, height, Path.Direction.CW);
            return p;
        }

        // circleFraction=1 或者完美圆形四分之一：退回平台 RoundedRect（圆角）
        if (smooth.circleFraction >= 1f ||
            (width == height && topLeft == centerX &&
			topLeft == topRight && bottomLeft == bottomRight)) {
            Path p = new Path();
            // 平台圆角：用 addRoundRect
            float[] radii = new float[]{
				topLeft, topLeft,     // TL x,y
				topRight, topRight,   // TR
				bottomRight, bottomRight, // BR
				bottomLeft, bottomLeft    // BL
            };
            p.addRoundRect(0, 0, width, height, radii, Path.Direction.CW);
            return p;
        }

        // 平滑 G2 路径
        return smooth.createRoundedRectanglePath(
			width, height, topRight, topLeft, bottomLeft, bottomRight
        );
    }

    private static float clamp(float v, float lo, float hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    public CornerSmoothness getCornerSmoothness(){ return smooth; }
}

