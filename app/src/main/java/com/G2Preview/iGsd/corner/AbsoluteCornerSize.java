
package com.G2Preview.iGsd.corner;

import android.util.SizeF;

public final class AbsoluteCornerSize implements CornerSize {
    private final float px;
    public AbsoluteCornerSize(float px) { this.px = Math.max(0f, px); }
    @Override public float toPx(SizeF size) { return px; }
    @Override public String toString(){ return "AbsoluteCornerSize(" + px + "px)"; }
}

