package com.nfar.allergydetective.ui.home

import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotateRad
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp
import com.nfar.allergydetective.ui.theme.AppColors
import kotlin.math.atan2

/*
 * Drawings of the start screen after the design picture of 21.11.2026: the broad-leaved branch
 * top right (dark mode; light mode uses the leaf picture) and the tile icons (heart with pulse line, moon with stars,
 * bars with a magnifier). Drawn in code, so they follow light and dark mode and stay sharp.
 */

/**
 * Branch measured from the design picture (21.11.2026, compared side by side with the phone): a stem
 * coming in from the lower right edge below the bell, five broad, overlapping leaves – a large one up
 * to the left, a long one to the left just above the progress card, one in between, one up along the
 * right edge, a small one low. Pale sage, slightly out of focus like the picture, a faint midrib.
 * Coordinates in a 200 × 200 unit box at the top right of the content.
 */
@Composable
fun LeafBranch(modifier: Modifier) {
    val colors = AppColors.extra.home
    // Soft focus as in the picture (blur works from Android 12; older phones show the leaves sharp).
    Canvas(modifier.blur(1.5.dp, BlurredEdgeTreatment.Unbounded)) {
        val u = size.width / 200f
        fun p(x: Float, y: Float) = Offset(x * u, y * u)
        val stem = Path().apply {
            moveTo(205 * u, 150 * u)
            cubicTo(200 * u, 136 * u, 196 * u, 124 * u, 194 * u, 111 * u)
        }
        drawPath(stem, colors.leaf, style = Stroke(width = 2.2f * u, cap = StrokeCap.Round))
        // Back to front: up the right edge, long one left, small one low, middle one, large one up left.
        // Positions taken from the picture relative to the top of the progress card, so all leaves end above it.
        broadLeaf(p(194f, 106f), p(200f, 38f), 0.46f, colors.leaf, colors.leafLight)
        broadLeaf(p(194f, 127f), p(71f, 133f), 0.36f, colors.leaf, colors.leafLight)
        broadLeaf(p(197f, 132f), p(166f, 141f), 0.5f, colors.leaf, colors.leafLight)
        broadLeaf(p(188f, 119f), p(134f, 85f), 0.5f, colors.leaf, colors.leafLight)
        broadLeaf(p(194f, 112f), p(98f, 49f), 0.46f, colors.leaf, colors.leafLight)
    }
}

/** Smooth lanceolate leaf from [base] to [tip], widest at about 40 %, gently curved. */
private fun DrawScope.broadLeaf(base: Offset, tip: Offset, widthRatio: Float, dark: Color, light: Color) {
    val d = tip - base
    val length = d.getDistance()
    val w = length * widthRatio
    translate(base.x, base.y) {
        rotateRad(atan2(d.y, d.x), pivot = Offset.Zero) {
            val shape = Path().apply {
                moveTo(0f, 0f)
                cubicTo(length * 0.22f, -w * 0.62f, length * 0.66f, -w * 0.66f, length, 0f)
                cubicTo(length * 0.7f, w * 0.34f, length * 0.3f, w * 0.48f, 0f, 0f)
                close()
            }
            drawPath(shape, Brush.horizontalGradient(listOf(dark, lerp(dark, light, 0.55f), light), startX = 0f, endX = length))
            // Soft light along the upper half, as in the picture.
            drawPath(shape, Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.22f), Color.Transparent), startY = -w * 0.5f, endY = w * 0.1f))
            val rib = Path().apply {
                moveTo(length * 0.03f, 0f)
                quadraticTo(length * 0.5f, -w * 0.1f, length * 0.9f, -w * 0.02f)
            }
            drawPath(rib, Color.White.copy(alpha = 0.3f), style = Stroke(width = w * 0.03f, cap = StrokeCap.Round))
        }
    }
}

/**
 * Logo next to "Allergy Detective" (user's code of 21.11.2026 calls it; drawn after the user's logo
 * picture, docs/design/home-logo-source.webp): a smaller light leaf to the left and a larger dark leaf
 * to the upper right, growing from the same point, each with a white midrib and side veins.
 * Coordinates measured from the picture in a 100 × 100 box.
 */
@Composable
fun AllergyDetectiveLogo(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val k = size.minDimension / 100f
        fun o(x: Float, y: Float) = Offset(x * k, y * k)
        fun path(block: Path.() -> Unit) = Path().apply(block)
        fun Path.m(x: Float, y: Float) = moveTo(x * k, y * k)
        fun Path.c(x1: Float, y1: Float, x2: Float, y2: Float, x: Float, y: Float) = cubicTo(x1 * k, y1 * k, x2 * k, y2 * k, x * k, y * k)
        fun Path.q(x1: Float, y1: Float, x: Float, y: Float) = quadraticTo(x1 * k, y1 * k, x * k, y * k)

        val left = path {
            m(40.8f, 87.4f)
            c(25f, 80f, 6f, 72f, 2f, 50f)
            c(-1f, 35f, 3f, 22f, 4.9f, 14.3f)
            c(18f, 20f, 32f, 30f, 38f, 47f)
            c(34f, 60f, 35f, 75f, 40.8f, 87.4f)
            close()
        }
        val right = path {
            m(41.9f, 87.9f)
            c(33f, 70f, 36f, 45f, 48f, 33f)
            c(62f, 20f, 85f, 16f, 99.7f, 11.7f)
            c(97f, 30f, 95f, 50f, 82f, 66f)
            c(70f, 80f, 52f, 80f, 41.9f, 87.9f)
            close()
        }
        drawPath(left, Brush.linearGradient(listOf(Color(0xFFA6DB8B), Color(0xFF3D8A55)), start = o(4f, 14f), end = o(40f, 87f)))
        drawPath(right, Brush.linearGradient(listOf(Color(0xFF7DBA79), Color(0xFF0D4A2A)), start = o(100f, 12f), end = o(42f, 88f)))

        val vein = Color(0xFFF3F7E8)
        val thick = Stroke(width = 1.6f * k, cap = StrokeCap.Round)
        val thin = Stroke(width = 1.1f * k, cap = StrokeCap.Round)
        // Left leaf: midrib and two side veins.
        drawPath(path { m(6.3f, 17.8f); q(24f, 50f, 40.8f, 87.4f) }, vein, style = thick)
        drawPath(path { m(5.2f, 45.5f); q(17f, 49f, 23f, 54.4f) }, vein, style = thin)
        drawPath(path { m(25.7f, 30.3f); q(26f, 40f, 22.2f, 48.7f) }, vein, style = thin)
        // Right leaf: midrib and four side veins.
        drawPath(path { m(99.3f, 12f); c(80f, 32f, 60f, 52f, 52.4f, 61.2f); q(42f, 72f, 41.9f, 87.9f) }, vein, style = thick)
        drawPath(path { m(60.2f, 31.9f); q(57f, 45f, 56f, 57f) }, vein, style = thin)
        drawPath(path { m(79f, 24f); q(75f, 32f, 73.8f, 38.7f) }, vein, style = thin)
        drawPath(path { m(91.6f, 38.4f); q(80f, 43f, 69.4f, 46.1f) }, vein, style = thin)
        drawPath(path { m(82.2f, 55.5f); q(66f, 56f, 54.5f, 60.1f) }, vein, style = thin)
    }
}

/** Heart with a white pulse line ("Symptome"). */
@Composable
fun HeartPulseIcon(color: Color, modifier: Modifier) {
    Canvas(modifier) {
        val s = size.minDimension
        val heart = Path().apply {
            moveTo(s * 0.5f, s * 0.92f)
            cubicTo(s * 0.12f, s * 0.66f, -s * 0.02f, s * 0.42f, s * 0.08f, s * 0.24f)
            cubicTo(s * 0.18f, s * 0.06f, s * 0.42f, s * 0.04f, s * 0.5f, s * 0.24f)
            cubicTo(s * 0.58f, s * 0.04f, s * 0.82f, s * 0.06f, s * 0.92f, s * 0.24f)
            cubicTo(s * 1.02f, s * 0.42f, s * 0.88f, s * 0.66f, s * 0.5f, s * 0.92f)
            close()
        }
        drawPath(heart, Brush.verticalGradient(listOf(lerp(color, Color.White, 0.18f), color)))
        val pulse = Path().apply {
            moveTo(s * 0.1f, s * 0.48f)
            lineTo(s * 0.34f, s * 0.48f)
            lineTo(s * 0.42f, s * 0.32f)
            lineTo(s * 0.53f, s * 0.66f)
            lineTo(s * 0.61f, s * 0.42f)
            lineTo(s * 0.67f, s * 0.48f)
            lineTo(s * 0.9f, s * 0.48f)
        }
        drawPath(pulse, Color.White, style = Stroke(width = s * 0.065f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

/** Crescent moon with two small stars ("Tagesabschluss"). */
@Composable
fun MoonIcon(color: Color, modifier: Modifier) {
    Canvas(modifier) {
        val s = size.minDimension
        val full = Path().apply { addOval(androidx.compose.ui.geometry.Rect(Offset(s * 0.42f, s * 0.56f), s * 0.42f)) }
        val cut = Path().apply { addOval(androidx.compose.ui.geometry.Rect(Offset(s * 0.66f, s * 0.4f), s * 0.33f)) }
        val moon = Path().apply { op(full, cut, PathOperation.Difference) }
        drawPath(moon, Brush.linearGradient(listOf(lerp(color, Color.White, 0.35f), color), start = Offset(s * 0.1f, s * 0.2f), end = Offset(s * 0.6f, s * 0.95f)))
        star(Offset(s * 0.8f, s * 0.2f), s * 0.12f, color)
        star(Offset(s * 0.93f, s * 0.42f), s * 0.07f, color)
    }
}

private fun DrawScope.star(center: Offset, r: Float, color: Color) {
    val p = Path().apply {
        moveTo(center.x, center.y - r)
        quadraticTo(center.x, center.y, center.x + r, center.y)
        quadraticTo(center.x, center.y, center.x, center.y + r)
        quadraticTo(center.x, center.y, center.x - r, center.y)
        quadraticTo(center.x, center.y, center.x, center.y - r)
        close()
    }
    drawPath(p, color)
}

/** Rising bars ("Einträge gesamt"). */
@Composable
fun BarsIcon(color: Color, modifier: Modifier) {
    Canvas(modifier) { bars(size.minDimension, color, listOf(0.38f, 0.58f, 0.82f), 0.26f) }
}

private fun DrawScope.bars(s: Float, color: Color, heights: List<Float>, barWidth: Float) {
    val gap = s * 0.06f
    heights.forEachIndexed { i, hgt ->
        val x = i * (s * barWidth + gap)
        drawRoundRect(color, Offset(x, s * (0.92f - hgt)), Size(s * barWidth, s * hgt), CornerRadius(s * 0.03f))
    }
}

/** Bars with a magnifier in front ("Muster entdecken"). */
@Composable
fun PatternIcon(color: Color, background: Color, modifier: Modifier) {
    Canvas(modifier) {
        val s = size.minDimension
        bars(s, color.copy(alpha = 0.85f), listOf(0.3f, 0.46f, 0.62f, 0.86f), 0.15f)
        val c = Offset(s * 0.7f, s * 0.6f)
        val r = s * 0.19f
        drawCircle(background, radius = r, center = c)
        drawCircle(color, radius = r, center = c, style = Stroke(width = s * 0.07f))
        drawLine(color, Offset(c.x + r * 0.72f, c.y + r * 0.72f), Offset(s * 0.98f, s * 0.95f), strokeWidth = s * 0.09f, cap = StrokeCap.Round)
    }
}
