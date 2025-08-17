
package com.G2Preview.iGsd.ui;


import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class AspectRatioLayout extends FrameLayout {
    private float aspect = 1f; // width/height
    public AspectRatioLayout(Context c) { 
	super(c); 
	}
    public AspectRatioLayout(Context c, AttributeSet a) { 
	super(c, a);
	}
    public AspectRatioLayout(Context c, AttributeSet a, int s) {
		super(c, a, s);
		}
    public void setAspect(float a) { 
	this.aspect = Math.max(0.1f, a); requestLayout();
	}

    @Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w = MeasureSpec.getSize(widthMeasureSpec);
        int h = (int) (w / aspect);
        int hSpec = MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, hSpec);
    }
}

