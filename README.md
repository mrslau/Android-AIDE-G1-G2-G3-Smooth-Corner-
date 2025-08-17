
---

# Android-AIDE+ \[G1 / G2 / G3 Smooth Corner]

**An Android custom library supporting G1/G2/G3 smooth corner algorithms, helping developers achieve fluid rounded rectangles / capsules.**

Features:

* ✅ G1, G2, G3 smooth corner algorithms
* ✅ `G2PreviewView`: Quickly preview corner effects with adjustable parameters
* ✅ `AspectRatioLayout`: A container View that enforces a fixed aspect ratio
* ✅ Four adjustable parameters:

  * `circleFraction`: Proportion of circular arc in the corner
  * `extendedFraction`: Extension factor along edges
  * `cornerRadius`: Corner radius
  * `aspectRatio`: Aspect ratio control

---

## 🚀 Highlights

* **Geometrically precise**: Matches Jetpack Compose G2 corner algorithm, but works without Compose.
* **Flexible**: Fully controllable with 4 parameters for a wide range of corner effects.
* **Preview-friendly**: Built-in `G2PreviewView` to adjust and preview in real time.
* **Highly compatible**: Usable as a `Drawable` background for any View.

---

## 🧮 Algorithm Overview

### 🔹 CornerSmoothness

`CornerSmoothness` defines **how a corner is smoothed**, with two core parameters:

* `circleFraction ∈ [0,1]`:
  Determines how much of a 90° corner is preserved as a circular arc.

  * `1.0` → full rounded corner (pure arc)
  * `0.0` → fully smoothed with Bezier curve
  * `0.3–0.7` → hybrid mix for natural transitions

* `extendedFraction ≥ 0`:
  Controls **how far the corner extends along edges**, capsule-like.

  * `0` → standard rounded corner
  * `>1.0` → elongated capsule effect

Algorithm principle:

1. Split a 90° corner into **arc segment + Bezier transition**.
2. Use cubic Bezier curves (`cubicTo`) for smooth blending.

---

### 🔹 G2RoundedCornerShape

`G2RoundedCornerShape` generates a **full rounded rectangle Path/Outline**:

1. Converts each corner size (`topLeft`, `topRight`, `bottomLeft`, `bottomRight`) into valid pixels (never exceeding half the shape size).
2. Simplifies cases:

   * `circleFraction == 1.0` → draws a platform `RoundRect`.
   * Equal width/height + radius equals center → draws a circle/capsule.
3. General case:

   * Calls `CornerSmoothness.createRoundedRectanglePath()`.
   * Each corner is drawn as: **straight edge → Bezier transition → arc**.
   * Produces a continuous closed Path.

Math formula for Bezier control points:

```
a = 1 - sin(θ) / (1 + cos(θ))
d = 1.5 * sin(θ) / (1 + cos(θ))
```

where θ = `(90° - circleFraction * 90°) / 2`.

Guarantees **C² continuity** (smooth curvature, no sharp spikes or beveled edges).

---

## 🖼️ Usage

### 1. In Layout XML

#### **Preview View**

```xml
<com.g2corner.G2PreviewView
    android:id="@+id/preview"
    android:layout_width="match_parent"
    android:layout_height="300dp"
    app:circleFraction="0.5"
    app:extendedFraction="0.75"
    app:cornerRadius="120dp"
    app:aspectRatio="1.0" />
```

#### **Fixed Aspect Ratio Container**

```xml
<com.g2corner.AspectRatioLayout
    android:id="@+id/container"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:aspectRatio="1.0">

    <ImageView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:scaleType="centerCrop"
        android:src="@drawable/sample" />

</com.g2corner.AspectRatioLayout>
```

---

### 2. In Java Code

```java
// Create corner smoothness parameters
CornerSmoothness smooth = new CornerSmoothness(0.6f, 0.75f);

// Create a rounded corner background
Drawable bg = new G2RoundedCornerDrawable(
    Color.RED,
    G2RoundedCornerShape.allPx(120f, smooth),
    LayoutDir.LTR
);

// Apply to any View
view.setBackground(bg);
```

---

## 🎛️ Parameters

| Parameter          | Description                                         |
| ------------------ | --------------------------------------------------- |
| `circleFraction`   | ∈ \[0,1], proportion of arc preserved in 90° corner |
| `extendedFraction` | ≥ 0, extension factor along edges                   |
| `cornerRadius`     | Corner radius (px/dp)                               |
| `aspectRatio`      | Aspect ratio (0.25–2.0 adjustable)                  |

---

## 📷 Preview



---

## 🤝 Contributing

* Issues and feature requests are welcome
* Feedback mail:3106875994@qq.com
* 


