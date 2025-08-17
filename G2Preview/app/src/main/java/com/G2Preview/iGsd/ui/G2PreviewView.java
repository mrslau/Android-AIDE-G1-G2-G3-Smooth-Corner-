
package com.G2Preview.iGsd.ui;



/**
 * 纯代码预览控件：
 * - 内部：AspectRatioLayout（等比容器） -> preview(着色背景) + BaselineOverlay(可见/隐藏)
 * - 对外：四个参数的 setter（circleFraction / cornerRadiusDp / extendedFraction / aspectRatio）
 * - 附带：resetDefaults()、setBaselineVisible()/toggleBaseline()、setLayoutDir()
 * - 不包含任何 TextView 或 Slider；不依赖 XML
 */
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import androidx.annotation.Nullable;
import com.G2Preview.iGsd.core.LayoutDir;
import com.G2Preview.iGsd.corner.AbsoluteCornerSize;
import com.G2Preview.iGsd.corner.CornerSmoothness;
import com.G2Preview.iGsd.shape.G2RoundedCornerShape;
import com.G2Preview.iGsd.shape.G2ShapeDrawable;

public class G2PreviewView extends FrameLayout {

    // ---- 子视图 ----
    private AspectRatioLayout container;
    private View preview;
    private BaselineOverlay baselineOverlay;

    // ---- 参数状态（默认等同你之前的默认）----
    private float circleFraction   = CornerSmoothness.DEFAULT.circleFraction; // [0,1]
    private float extendedFraction = CornerSmoothness.DEFAULT.extendedFraction; // >=0
    private float cornerRadiusDp   = 120f; // 以 dp 存储
    private float aspectRatio      = 1f;   // 等比缩放

    private boolean showBaseline   = false;
    private LayoutDir layoutDir    = LayoutDir.LTR;

    private CornerSmoothness smoothness = CornerSmoothness.DEFAULT;

    // 绘制对象
    private G2RoundedCornerShape shape;
    private G2ShapeDrawable drawable;

    public G2PreviewView(Context context) {
        super(context);
        init(context);
    }

    public G2PreviewView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public G2PreviewView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    
    private void init(Context ctx) {
        setClipToPadding(false);
        setClipChildren(false);

        container = new AspectRatioLayout(ctx);
        container.setLayoutParams(new LayoutParams(
									  LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        container.setAspect(aspectRatio);

        preview = new View(ctx);
        preview.setLayoutParams(new LayoutParams(
									LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        baselineOverlay = new BaselineOverlay(ctx);
        baselineOverlay.setLayoutParams(new LayoutParams(
											LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        baselineOverlay.setVisibility(showBaseline ? VISIBLE : GONE);

        // 组装：container 里放 preview 与 overlay；container 作为根子视图加入自身
        container.addView(preview);
        container.addView(baselineOverlay);
        addView(container);

        // 初次绘制
        applyState();
    }

    /* =========================
     * 对外 API
     * ========================= */

    /** 一把梭设置四个参数（dp 半径 & 其余 float） */
    public void setParams(float circleFraction, float cornerRadiusDp,
                          float extendedFraction, float aspectRatio) {
        boolean needApply = false;

        float cf = clamp(circleFraction, 0f, 1f);
        if (Math.abs(cf - this.circleFraction) > 1e-6f) {
            this.circleFraction = cf;
            needApply = true;
        }

        float ef = Math.max(0f, extendedFraction);
        if (Math.abs(ef - this.extendedFraction) > 1e-6f) {
            this.extendedFraction = ef;
            needApply = true;
        }

        float cr = Math.max(0f, cornerRadiusDp);
        if (Math.abs(cr - this.cornerRadiusDp) > 1e-6f) {
            this.cornerRadiusDp = cr;
            needApply = true;
        }

        float ar = Math.max(0.01f, aspectRatio);
        if (Math.abs(ar - this.aspectRatio) > 1e-6f) {
            this.aspectRatio = ar;
            container.setAspect(this.aspectRatio);
            needApply = true;
        }

        if (needApply) {
            // 重建平滑度对象
            this.smoothness = new CornerSmoothness(this.circleFraction, this.extendedFraction);
            applyState();
        }
    }

    public void setCircleFraction(float v) {
        float nv = clamp(v, 0f, 1f);
        if (Math.abs(nv - this.circleFraction) > 1e-6f) {
            this.circleFraction = nv;
            this.smoothness = new CornerSmoothness(this.circleFraction, this.extendedFraction);
            applyState();
        }
    }

    /** 以 dp 设置圆角半径 */
    public void setCornerRadiusDp(float dp) {
        float nv = Math.max(0f, dp);
        if (Math.abs(nv - this.cornerRadiusDp) > 1e-6f) {
            this.cornerRadiusDp = nv;
            applyState();
        }
    }

    public void setExtendedFraction(float v) {
        float nv = Math.max(0f, v);
        if (Math.abs(nv - this.extendedFraction) > 1e-6f) {
            this.extendedFraction = nv;
            this.smoothness = new CornerSmoothness(this.circleFraction, this.extendedFraction);
            applyState();
        }
    }

    public void setAspectRatio(float ratio) {
        float nv = Math.max(0.01f, ratio);
        if (Math.abs(nv - this.aspectRatio) > 1e-6f) {
            this.aspectRatio = nv;
            container.setAspect(this.aspectRatio);
            applyState();
        }
    }

    /** 重置为默认值（不涉及文本） */
    public void resetDefaults() {
        circleFraction   = CornerSmoothness.DEFAULT.circleFraction;
        extendedFraction = CornerSmoothness.DEFAULT.extendedFraction;
        cornerRadiusDp   = 120f;
        aspectRatio      = 1f;
        container.setAspect(aspectRatio);
        smoothness = new CornerSmoothness(circleFraction, extendedFraction);
        applyState();
    }

    /** 显示/隐藏基线覆盖层（不含文本） */
    public void setBaselineVisible(boolean visible) {
        if (showBaseline != visible) {
            showBaseline = visible;
            if (baselineOverlay != null) {
                baselineOverlay.setVisibility(showBaseline ? VISIBLE : GONE);
            }
        }
    }

    /** 取当前基线可见性 */
    public boolean isBaselineVisible() {
        return showBaseline;
    }

    /** 切换基线可见性 */
    public void toggleBaseline() {
        setBaselineVisible(!showBaseline);
    }

    /** 设置布局方向（LTR/RTL） */
    public void setLayoutDir(LayoutDir dir) {
        if (dir != null && dir != this.layoutDir) {
            this.layoutDir = dir;
            applyState();
        }
    }

    /* =========================
     * 内部绘制
     * ========================= */
    private void applyState() {
        float radiusPx = dpToPx(cornerRadiusDp);

        // 生成 G2 shape 与 drawable
        shape = new G2RoundedCornerShape(
			new AbsoluteCornerSize(radiusPx),
			new AbsoluteCornerSize(radiusPx),
			new AbsoluteCornerSize(radiusPx),
			new AbsoluteCornerSize(radiusPx),
			smoothness
        );

        if (drawable == null) {
            // 首次创建：随便给个漂亮一点的色（可对外提供 setter）
            drawable = new G2ShapeDrawable(Color.parseColor("#4F46E5"), shape);
        } else {
           // drawable.setShape(shape);
        }
        drawable.setLayoutDir(layoutDir);

        // 应用到预览
        preview.setBackground(drawable);

        // 同步基线圆角（以 px；你的 BaselineOverlay 会按圆角填充/描边）
        if (baselineOverlay != null) {
            baselineOverlay.setCornerRadiusPx(radiusPx);
        }

        // 请求重绘
        invalidate();
    }

    /* =========================
     * 工具
     * ========================= */
    private static float clamp(float v, float lo, float hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    /* =========================
     * 可选 Getter
     * ========================= */
    public float getCircleFraction()   { return circleFraction; }
    public float getExtendedFraction() { return extendedFraction; }
    public float getCornerRadiusDp()   { return cornerRadiusDp; }
    public float getAspectRatio()      { return aspectRatio; }
    public LayoutDir getLayoutDirEnum(){ return layoutDir; }
}
