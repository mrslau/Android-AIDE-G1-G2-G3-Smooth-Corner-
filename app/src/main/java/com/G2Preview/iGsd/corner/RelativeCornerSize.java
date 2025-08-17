
package com.G2Preview.iGsd.corner;


import android.util.SizeF;

public final class RelativeCornerSize implements CornerSize {
    private final int percent; // 0..100
    public RelativeCornerSize(int percent) {
        this.percent = Math.max(0, Math.min(100, percent));
    }
    @Override public float toPx(SizeF size) {
        float minSide = Math.min(size.getWidth(), size.getHeight());
        return (percent / 100f) * (minSide / 2f); // 和 Compose 一样：相对于短边的一半
    }
    @Override public String toString(){ return "RelativeCornerSize(" + percent + "%)"; }
}

