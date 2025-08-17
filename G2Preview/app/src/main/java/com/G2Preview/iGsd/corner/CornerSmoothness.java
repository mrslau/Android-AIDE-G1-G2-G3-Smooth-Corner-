
package com.G2Preview.iGsd.corner;
import android.graphics.Path;
import android.graphics.RectF;

public final class CornerSmoothness {

    public final float circleFraction;   // [0,1]
    public final float extendedFraction; // >= 0

    private static final float HALF_PI = (float) (Math.PI / 2.0f);

    private final float circleRadians;   // = HALF_PI * circleFraction
    private final float bezierRadians;   // = (HALF_PI - circleRadians)/2

    // 由 bezierRadians 推导
    private final float sin;
    private final float cos;
    private final float a;   // = 1 - sin/(1+cos)
    private final float d;   // = 1.5 * sin/(1+cos)^2
    private final float ad;  // = a + d

    public CornerSmoothness(float circleFraction, float extendedFraction) {
        this.circleFraction   = clamp01(circleFraction);
        this.extendedFraction = Math.max(0f, extendedFraction);

        circleRadians = HALF_PI * this.circleFraction;
        bezierRadians = (HALF_PI - circleRadians) / 2f;

        sin = (float) Math.sin(bezierRadians);
        cos = (float) Math.cos(bezierRadians);
        a   = 1f - sin / (1f + cos);
        d   = 1.5f * sin / (1f + cos) / (1f + cos);
        ad  = a + d;
    }

    // ---------- 公用：弧（弧度） ----------
    private static void arcToRad(Path p, float cx, float cy, float r,
                                 float startRad, float sweepRad, boolean forceMoveTo) {
        RectF oval = new RectF(cx - r, cy - r, cx + r, cy + r);
        p.arcTo(oval, (float) Math.toDegrees(startRad), (float) Math.toDegrees(sweepRad), forceMoveTo);
    }

    // ---------- 角段（与 Kotlin 对齐） ----------
    private void topRightCorner0(Path p, float w, float r, float dy) {
        p.cubicTo(w, r * ad, w, r * a, w - r * (1f - cos), r * (1f - sin));
    }
    private void topRightCircle(Path p, float w, float r) {
        if (circleRadians > 0f) arcToRad(p, w - r, r, r, -bezierRadians, -circleRadians, false);
    }
    private void topRightCorner1(Path p, float w, float r, float dx) {
        p.cubicTo(w - r * a, 0f, w - r * ad, 0f, w - r - dx, 0f);
    }

    private void topLeftCorner1(Path p, float r, float dx) {
        p.cubicTo(r * ad, 0f, r * a, 0f, r * (1f - sin), r * (1f - cos));
    }
    private void topLeftCircle(Path p, float r) {
        if (circleRadians > 0f) arcToRad(p, r, r, r, -(HALF_PI + bezierRadians), -circleRadians, false);
    }
    private void topLeftCorner0(Path p, float r, float dy) {
        p.cubicTo(0f, r * a, 0f, r * ad, 0f, r + dy);
    }

    private void bottomLeftCorner0(Path p, float h, float r, float dy) {
		p.cubicTo(
			0f,
			h - r * ad,
			0f,
			h - r * a,
			r * (1f - cos),
			h - r * (1f - sin)
		);
	}

	private void bottomLeftCircle(Path p, float h, float r) {
		if (circleRadians > 0f) {
			arcToRad(
				p,
				r,
				h - r,
				r,
				-(HALF_PI * 2f + bezierRadians),
				-circleRadians,
				false
			);
		}
	}

	private void bottomLeftCorner1(Path p, float h, float r, float dx) {
		p.cubicTo(
			r * a,
			h,
			r * ad,
			h,
			r - dx,
			h
		);
	}
	

    private void bottomRightCorner1(Path p, float w, float h, float r, float dx) {
        p.cubicTo(w - r * ad, h, w - r * a, h, w - r * (1f - sin), h - r * (1f - cos));
    }
    private void bottomRightCircle(Path p, float w, float h, float r) {
        if (circleRadians > 0f) arcToRad(p, w - r, h - r, r, -(HALF_PI * 3f + bezierRadians), -circleRadians, false);
    }
    private void bottomRightCorner0(Path p, float w, float h, float r, float dy) {
        p.cubicTo(w, h - r * a, w, h - r * ad, w, h - r + dy);
    }

    // ---------- 整半圆（保持几何一致） ----------
    private void rightCircle(Path p, float w, float r, boolean moveToStart) {
        arcToRad(p, w - r, r, r, bezierRadians + circleRadians,
				 -(bezierRadians + circleRadians) * 2f, moveToStart);
    }
    private void leftCircle(Path p, float r, float h, boolean moveToStart) {
        arcToRad(p, r, h - r, r, -(HALF_PI + bezierRadians),
				 -(bezierRadians + circleRadians) * 2f, moveToStart);
    }
    private void topCircle(Path p, float r, boolean moveToStart) {
        arcToRad(p, r, r, r, -bezierRadians, -(bezierRadians + circleRadians) * 2f, moveToStart);
    }
    private void bottomCircle(Path p, float h, float r, boolean moveToStart) {
        arcToRad(p, r, h - r, r, -(HALF_PI * 2f + bezierRadians),
				 -(bezierRadians + circleRadians) * 2f, moveToStart);
    }

    // ---------- 对外：生成 Path ----------
    public Path createRoundedRectanglePath(float width, float height,
                                           float topRight, float topLeft,
                                           float bottomLeft, float bottomRight) {
        float centerX = width / 2f, centerY = height / 2f;
        float maxR = Math.min(centerX, centerY);

        float topRightDy    = fastCoerceAtMost(topRight    * extendedFraction, centerY - topRight);
        float topRightDx    = fastCoerceAtMost(topRight    * extendedFraction, centerX - topRight);
        float topLeftDx     = fastCoerceAtMost(topLeft     * extendedFraction, centerX - topLeft);
        float topLeftDy     = fastCoerceAtMost(topLeft     * extendedFraction, centerY - topLeft);
        float bottomLeftDy  = fastCoerceAtMost(bottomLeft  * extendedFraction, centerY - bottomLeft);
        float bottomLeftDx  = fastCoerceAtMost(bottomLeft  * extendedFraction, centerX - bottomLeft);
        float bottomRightDx = fastCoerceAtMost(bottomRight * extendedFraction, centerX - bottomRight);
        float bottomRightDy = fastCoerceAtMost(bottomRight * extendedFraction, centerY - bottomRight);

        // 轻微内缩（特别对左下角），抑制极端参数回勾/自交
        final float EPS = 0.5f;
        topRightDx = Math.max(0f, topRightDx - EPS);
        topRightDy = Math.max(0f, topRightDy - EPS);
        topLeftDx  = Math.max(0f, topLeftDx  - EPS);
        topLeftDy  = Math.max(0f, topLeftDy  - EPS);
        bottomLeftDx  = Math.max(0f, bottomLeftDx  - EPS);
        bottomLeftDy  = Math.max(0f, bottomLeftDy  - EPS);
        bottomRightDx = Math.max(0f, bottomRightDx - EPS);
        bottomRightDy = Math.max(0f, bottomRightDy - EPS);

        Path p = new Path();
        p.setFillType(Path.FillType.WINDING); // 非零环绕：避免内部“挖空”

        // 胶囊：四角等于 maxR
        if (topRight == maxR && topLeft == maxR && bottomLeft == maxR && bottomRight == maxR) {
            if (width > height) {
                rightCircle(p, width, maxR, true); // 首段弧 moveTo 起笔
                topRightCorner1(p, width, topRight, topRightDx);
                p.lineTo(topLeft + topLeftDx, 0f);
                topLeftCorner1(p, topLeft, topLeftDx);
                leftCircle(p, maxR, height, false);
                bottomLeftCorner1(p, height, bottomLeft, -bottomLeftDx);
                p.lineTo(width - bottomRight - bottomRightDx, height);
                bottomRightCorner1(p, width, height, bottomRight, -bottomRightDx);
            } else {
                p.moveTo(width, height - bottomRight - bottomRightDy);
                p.lineTo(width, topRight + topRightDy);
                topRightCorner0(p, width, topRight, -topRightDy);
                topCircle(p, maxR, false);
                topLeftCorner0(p, topLeft, topLeftDy);
                p.lineTo(0f, height - bottomLeft - bottomLeftDy);
                bottomLeftCorner0(p, height, bottomLeft, bottomLeftDy);
                bottomCircle(p, height, maxR, false);
                bottomRightCorner0(p, width, height, bottomRight, -bottomRightDy);
            }
            p.close();
            return p;
        }

        // 通用：顺时针
        p.moveTo(width, height - bottomRight - bottomRightDy);
        p.lineTo(width, topRight + topRightDy);

        if (topRight > 0f) {
            topRightCorner0(p, width, topRight, -topRightDy);
            topRightCircle(p, width, topRight);
            topRightCorner1(p, width, topRight, topRightDx);
        }

        p.lineTo(topLeft + topLeftDx, 0f);

        if (topLeft > 0f) {
            topLeftCorner1(p, topLeft, topLeftDx);
            topLeftCircle(p, topLeft);
            topLeftCorner0(p, topLeft, topLeftDy);
        }

        p.lineTo(0f, height - bottomLeft - bottomLeftDy);

        if (bottomLeft > 0f) {
            bottomLeftCorner0(p, height, bottomLeft, bottomLeftDy);
            bottomLeftCircle(p, height, bottomLeft);
            bottomLeftCorner1(p, height, bottomLeft, -bottomLeftDx);
        }

        p.lineTo(width - bottomRight - bottomRightDx, height);

        if (bottomRight > 0f) {
            bottomRightCorner1(p, width, height, bottomRight, -bottomRightDx);
            bottomRightCircle(p, width, height, bottomRight);
            bottomRightCorner0(p, width, height, bottomRight, -bottomRightDy);
        }

        p.close();
        return p;
    }

    // --------- 辅助 ----------
    private static float clamp01(float v) { return v < 0f ? 0f : (v > 1f ? 1f : v); }
    private static float fastCoerceAtMost(float value, float max) { return value > max ? max : value; }

    // --------- 预设 ---------
    public static final CornerSmoothness DEFAULT = new CornerSmoothness(
		1f - 2f * (float) Math.asin(0.6f) / HALF_PI, // ~= 16.26°
		0.75f
    );
    public static final CornerSmoothness None = new CornerSmoothness(1f, 0f);
}

