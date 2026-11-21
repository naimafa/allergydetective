package com.nfar.allergydetective.ui.home

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
import kotlin.math.PI

/*
 * Drawings of the start screen after the design picture of 21.11.2026: the two-leaf logo, the
 * broad-leaved branch top right, and the tile icons (heart with pulse line, moon with stars,
 * bars with a magnifier). Drawn in code, so they follow light and dark mode and stay sharp.
 */

/** Leaf lying on the x axis from (0, 0) to (length, 0), broad in the first half, pointed at the tip. */
private fun leafPath(length: Float, width: Float) = Path().apply {
    moveTo(0f, 0f)
    cubicTo(length * 0.18f, -width * 0.62f, length * 0.62f, -width * 0.6f, length, 0f)
    cubicTo(length * 0.62f, width * 0.6f, length * 0.18f, width * 0.62f, 0f, 0f)
    close()
}

private fun DrawScope.leaf(at: Offset, angle: Float, length: Float, width: Float, dark: Color, light: Color, alpha: Float) {
    translate(at.x, at.y) {
        rotateRad(angle, pivot = Offset.Zero) {
            drawPath(
                leafPath(length, width),
                Brush.linearGradient(listOf(dark, light), start = Offset(0f, width / 2), end = Offset(length, -width / 2)),
                alpha = alpha,
            )
            // Midrib and a few side veins, lighter than the leaf.
            val vein = lerp(light, Color.White, 0.35f)
            drawLine(vein, Offset(length * 0.04f, 0f), Offset(length * 0.9f, 0f), strokeWidth = width * 0.045f, cap = StrokeCap.Round, alpha = alpha * 0.8f)
            for (k in 1..3) {
                val x = length * (0.18f + k * 0.17f)
                for (side in listOf(-1f, 1f)) {
                    drawLine(vein, Offset(x, 0f), Offset(x + length * 0.12f, side * width * 0.28f), strokeWidth = width * 0.025f, cap = StrokeCap.Round, alpha = alpha * 0.5f)
                }
            }
        }
    }
}

/** Logo: a large dark leaf and a smaller light one growing from the same point, as in the picture. */
@Composable
fun LeafLogo(modifier: Modifier) {
    val colors = AppColors.extra.home
    Canvas(modifier) {
        val s = size.minDimension
        val root = Offset(s * 0.22f, s * 0.92f)
        leaf(root, (-100f).toRad(), s * 0.7f, s * 0.42f, colors.logoLight, colors.logoLight, 1f)
        leaf(root, (-38f).toRad(), s * 0.95f, s * 0.5f, colors.logoDark, colors.logoLight, 1f)
    }
}

/**
 * Branch coming in from the right edge (picture: broad leaves behind the greeting, reaching up and
 * to the left). Soft, so the text in front stays readable.
 */
@Composable
fun LeafBranch(modifier: Modifier) {
    val colors = AppColors.extra.home
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        // Stem from the lower right edge, bending up to the upper middle.
        val p0 = Offset(w * 1.04f, h * 0.98f)
        val p1 = Offset(w * 0.86f, h * 0.7f)
        val p2 = Offset(w * 0.72f, h * 0.45f)
        val p3 = Offset(w * 0.6f, h * 0.16f)
        fun point(t: Float): Offset {
            val u = 1 - t
            return p0 * (u * u * u) + p1 * (3 * u * u * t) + p2 * (3 * u * t * t) + p3 * (t * t * t)
        }
        val stem = Path().apply {
            moveTo(p0.x, p0.y)
            cubicTo(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y)
        }
        drawPath(stem, colors.leaf, style = Stroke(width = w * 0.016f, cap = StrokeCap.Round), alpha = 0.6f)
        // (position on the stem, angle in degrees, length as part of the width)
        val leaves = listOf(
            Triple(0.18f, -168f, 0.62f), // long leaf to the left, lowest
            Triple(0.3f, -35f, 0.42f), // up to the right edge
            Triple(0.5f, -150f, 0.58f), // left, a little up
            Triple(0.62f, -60f, 0.4f), // up
            Triple(0.82f, -128f, 0.5f), // up-left
            Triple(1f, -95f, 0.36f), // tip
        )
        for ((t, deg, len) in leaves) {
            val length = w * len
            leaf(point(t), deg.toRad(), length, length * 0.5f, colors.leaf, colors.leafLight, 0.72f)
        }
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
        val full = Path().apply { addOval(androidx.compose.ui.geometry.Rect(Offset(s * 0.42f, s * 0.56f), s * 0.38f)) }
        val cut = Path().apply { addOval(androidx.compose.ui.geometry.Rect(Offset(s * 0.62f, s * 0.42f), s * 0.32f)) }
        val moon = Path().apply { op(full, cut, PathOperation.Difference) }
        drawPath(moon, Brush.linearGradient(listOf(lerp(color, Color.White, 0.35f), color), start = Offset(s * 0.1f, s * 0.2f), end = Offset(s * 0.6f, s * 0.95f)))
        star(Offset(s * 0.76f, s * 0.22f), s * 0.11f, color)
        star(Offset(s * 0.9f, s * 0.42f), s * 0.065f, color)
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

private fun Float.toRad() = (this * PI / 180).toFloat()
