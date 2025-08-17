
package com.G2Preview.iGsd.corner;

import android.util.SizeF;

/**
 * 对应 Compose CornerSize 接口。
 * 根据控件尺寸 (SizeF) 返回最终的圆角半径（像素）。
 */
public interface CornerSize {
    /**
     * @param size 当前组件的宽高（像素）
     * @return 圆角半径（像素）
     */
    float toPx(SizeF size);
}

