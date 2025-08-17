
package com.G2Preview.iGsd.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

public class SquareLinearLayout extends LinearLayout {

    // 正方形边长 = parentHeight * sideFraction，再与 parentWidth 取 min，保证不越界仍是正方形
    private float sideFraction = 0.8f; // 0~1，默认占父高度的80%

    public SquareLinearLayout(Context context) { super(context); init(); }
    public SquareLinearLayout(Context context, AttributeSet attrs) { super(context, attrs); init(); }
    public SquareLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); init(); }

    private void init() {
		setGravity(android.view.Gravity.CENTER);
		setBackgroundColor(0xffffff); // 纯红色（ARGB）
	}
	
    /** 设置以父高度为基准的边长比例（0~1） */
    public void setSideFraction(float fraction) {
        if (fraction <= 0f) fraction = 0.01f;
        if (fraction > 1f) fraction = 1f;
        if (this.sideFraction != fraction) {
            this.sideFraction = fraction;
            requestLayout();
        }
    }

    /** 读取当前比例 */
    public float getSideFraction() {
        return sideFraction;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int parentW = MeasureSpec.getSize(widthMeasureSpec);
        int parentH = MeasureSpec.getSize(heightMeasureSpec);

        // 以父高度为主决定边长，再确保不超过父宽度
        int sizeByHeight = (int) (parentH * sideFraction);
        int size = Math.min(sizeByHeight, parentW);

        int squareSpec = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);
        super.onMeasure(squareSpec, squareSpec);

        // 子视图填满正方形
        int childW = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);
        int childH = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            child.measure(childW, childH);
        }
        setMeasuredDimension(size, size);
    }
}

