
package com.G2Preview.iGsd;




import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.G2Preview.iGsd.core.LayoutDir;
import com.G2Preview.iGsd.corner.AbsoluteCornerSize;
import com.G2Preview.iGsd.corner.CornerSmoothness;
import com.G2Preview.iGsd.shape.G2RoundedCornerShape;
import com.G2Preview.iGsd.shape.G2ShapeDrawable;
import com.G2Preview.iGsd.ui.AspectRatioLayout;
import com.G2Preview.iGsd.ui.BaselineOverlay;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;
public class MainActivity extends AppCompatActivity {

    private View preview;
    private AspectRatioLayout container;

    // 初始状态从 CornerSmoothness.DEFAULT 读取
    private float circleFraction = CornerSmoothness.DEFAULT.circleFraction;
    private float extendedFraction = CornerSmoothness.DEFAULT.extendedFraction;

    // 半径以 dp 存储&控制
    private float radiusDp = 120f; // dp
    private float aspect   = 1f;

    private CornerSmoothness smoothness = CornerSmoothness.DEFAULT;

    private G2RoundedCornerShape shape;
    private G2ShapeDrawable drawable;

    private TextView tvCircle, tvRadius, tvExtended, tvAspect;

    // Material3 Sliders
    private Slider sbCircle, sbRadius, sbExtended, sbAspect;

    // ★ 基准线
    private BaselineOverlay baselineOverlay;
    private MaterialButton btnToggleBaseline;
    private boolean showBaseline = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.Theme_App);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // 绑定视图
        container = findViewById(R.id.aspect_container);
        preview   = findViewById(R.id.preview_shape);

        tvCircle   = findViewById(R.id.tv_circle);
        tvRadius   = findViewById(R.id.tv_radius);
        tvExtended = findViewById(R.id.tv_extended);
        tvAspect   = findViewById(R.id.tv_aspect);
		
        // ★ 基准线与按钮
        baselineOverlay   = findViewById(R.id.baseline_overlay);
        btnToggleBaseline = findViewById(R.id.btn_toggle_baseline);
        if (btnToggleBaseline != null) {
            btnToggleBaseline.setText(showBaseline ? "Hide baseline" : "Show baseline");
            btnToggleBaseline.setOnClickListener(v -> {
                showBaseline = !showBaseline;
                if (baselineOverlay != null) {
                    baselineOverlay.setVisibility(showBaseline ? View.VISIBLE : View.GONE);
                }
                btnToggleBaseline.setText(showBaseline ? "Hide baseline" : "Show baseline");
            });
        }

        // ====== Sliders 初始化（先设区间/步进，再设值；并吸附到刻度） ======

        // circleFraction: 0 ~ 1, step 0.01
        sbCircle = findViewById(R.id.sb_circle);
        sbCircle.setValueFrom(0f);
        sbCircle.setValueTo(1f);
        sbCircle.setStepSize(0.01f);
        circleFraction = snap(circleFraction, 0f, 1f, 0.01f);
        sbCircle.setValue(circleFraction);
        sbCircle.addOnChangeListener((s, v, fromUser) -> {
            float nv = snap(v, 0f, 1f, 0.01f);
            if (Math.abs(nv - v) > 1e-6) s.setValue(nv);
            circleFraction = nv;
            smoothness = new CornerSmoothness(circleFraction, extendedFraction);
            applyState();
        });

        // radiusDp: 0 ~ 170 dp, step 1
        sbRadius = findViewById(R.id.sb_radius);
        sbRadius.setValueFrom(0f);
        sbRadius.setValueTo(170f);
        sbRadius.setStepSize(1f);
        radiusDp = snap(radiusDp, 0f, 170f, 1f);
        sbRadius.setValue(radiusDp);
        sbRadius.addOnChangeListener((s, v, fromUser) -> {
            float nv = snap(v, 0f, 170f, 1f);
            if (Math.abs(nv - v) > 1e-6) s.setValue(nv);
            radiusDp = nv;   // 以 dp 存储
            applyState();
        });

        // extendedFraction: 0 ~ 2, step 0.01
        sbExtended = findViewById(R.id.sb_extended);
        sbExtended.setValueFrom(0f);
        sbExtended.setValueTo(2f);
        sbExtended.setStepSize(0.01f);
        extendedFraction = snap(extendedFraction, 0f, 2f, 0.01f);
        sbExtended.setValue(extendedFraction);
        sbExtended.addOnChangeListener((s, v, fromUser) -> {
            float nv = snap(v, 0f, 2f, 0.01f);
            if (Math.abs(nv - v) > 1e-6) s.setValue(nv);
            extendedFraction = nv;
            smoothness = new CornerSmoothness(circleFraction, extendedFraction);
            applyState();
        });

        // aspectRatio: 1.00 ~ 2.00, step 0.01
        sbAspect = findViewById(R.id.sb_aspect);
        sbAspect.setValueFrom(1f);
        sbAspect.setValueTo(2f);
        sbAspect.setStepSize(0.01f);
        aspect = snap(Math.max(1f, aspect), 1f, 2f, 0.01f);
        sbAspect.setValue(aspect);
        sbAspect.addOnChangeListener((s, v, fromUser) -> {
            float nv = snap(v, 1f, 2f, 0.01f);
            if (Math.abs(nv - v) > 1e-6) s.setValue(nv);
            aspect = nv;
            container.setAspect(aspect);
            tvAspect.setText(String.format("aspectRatio=%.2f", aspect));
        });

        // 初始化 shape & drawable
        applyState();

        // 重置按钮
        MaterialButton btnReset = findViewById(R.id.btn_reset);
        if (btnReset != null) {
            btnReset.setOnClickListener(v -> {
                circleFraction   = CornerSmoothness.DEFAULT.circleFraction;
                extendedFraction = CornerSmoothness.DEFAULT.extendedFraction;
                radiusDp = 120f;   // dp
                aspect   = 1f;

                // 吸附并同步到 UI
                circleFraction   = snap(circleFraction, 0f, 1f, 0.01f);
                extendedFraction = snap(extendedFraction, 0f, 2f, 0.01f);
                radiusDp         = snap(radiusDp, 0f, 170f, 1f);
                aspect           = snap(aspect, 1f, 2f, 0.01f);

                sbCircle.setValue(circleFraction);
                sbExtended.setValue(extendedFraction);
                sbRadius.setValue(radiusDp);
                sbAspect.setValue(aspect);

                container.setAspect(aspect);
                smoothness = new CornerSmoothness(circleFraction, extendedFraction);
                applyState();
            });
        }

        // 首次文案
        tvAspect.setText(String.format("aspectRatio=%.2f", aspect));
    }

    /** 将值按步进吸附，并在[min,max]内夹紧 */
    private static float snap(float v, float min, float max, float step) {
        if (step <= 0f) return Math.max(min, Math.min(max, v));
        float snapped = Math.round((v - min) / step) * step + min;
        snapped = (float) Math.round(snapped * 1_000_000d) / 1_000_000f; // 抑制浮点毛刺
        if (snapped < min) snapped = min;
        if (snapped > max) snapped = max;
        return snapped;
    }

    /** dp → px */
    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    /** 根据当前状态刷新 shape/drawable/UI 文案 */
    private void applyState() {
        float radiusPx = dpToPx(radiusDp); // 绘制前转换为 px

        shape = new G2RoundedCornerShape(
			new AbsoluteCornerSize(radiusPx),
			new AbsoluteCornerSize(radiusPx),
			new AbsoluteCornerSize(radiusPx),
			new AbsoluteCornerSize(radiusPx),
			smoothness
        );

        drawable = new G2ShapeDrawable(Color.parseColor("#4F46E5"), shape);
        drawable.setLayoutDir(LayoutDir.LTR); // 如需 RTL，切换为 LayoutDir.RTL
        preview.setBackground(drawable);

        // 同步基准线圆角
        if (baselineOverlay != null) {
            baselineOverlay.setCornerRadiusPx(radiusPx);
        }

        if (tvCircle != null) {
            tvCircle.setText(String.format("circleFraction=%.2f", circleFraction));
        }
        if (tvRadius != null) {
            tvRadius.setText(String.format("cornerRadius=%.0fdp", radiusDp)); // 文案显示 dp
        }
        if (tvExtended != null) {
            tvExtended.setText(String.format("extendedFraction=%.2f", extendedFraction));
        }
        if (tvAspect != null) {
            tvAspect.setText(String.format("aspectRatio=%.2f", aspect));
        }
    }
}

