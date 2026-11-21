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
import kotlin.math.atan2

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
 * Branch measured from the design picture (21.11.2026, second phone test): a stem coming in from the
 * lower right edge and four broad, smooth leaves – a large one up to the left, a long one to the left
 * just above the progress card, two up to the right edge. Darker at the stem, pale at the tips, a fine
 * light midrib, no side veins. Coordinates in a 200 × 200 dp box at the top right of the content.
 */
@Composable
fun LeafBranch(modifier: Modifier) {
    val colors = AppColors.extra.home
    Canvas(modifier) {
        val u = size.width / 200f
        fun p(x: Float, y: Float) = Offset(x * u, y * u)
        val stem = Path().apply {
            moveTo(205 * u, 200 * u)
            cubicTo(192 * u, 186 * u, 178 * u, 160 * u, 164 * u, 122 * u)
        }
        drawPath(stem, colors.leaf, style = Stroke(width = 2.2f * u, cap = StrokeCap.Round))
        // Back to front: long leaf left, right edge, up right, large leaf up left.
        broadLeaf(p(175f, 158f), p(33f, 169f), 0.32f, colors.leaf, colors.leafLight)
        broadLeaf(p(183f, 147f), p(232f, 78f), 0.38f, colors.leaf, colors.leafLight)
        broadLeaf(p(168f, 125f), p(206f, 28f), 0.36f, colors.leaf, colors.leafLight)
        broadLeaf(p(164f, 122f), p(67f, 57f), 0.42f, colors.leaf, colors.leafLight)
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
            drawPath(shape, Brush.horizontalGradient(listOf(dark, lerp(dark, light, 0.55f), light), startX = 0f, endX = length), alpha = 0.92f)
            // Soft light along the upper half, as in the picture.
            drawPath(shape, Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.22f), Color.Transparent), startY = -w * 0.5f, endY = w * 0.1f))
            val rib = Path().apply {
                moveTo(length * 0.03f, 0f)
                quadraticTo(length * 0.5f, -w * 0.1f, length * 0.9f, -w * 0.02f)
            }
            drawPath(rib, Color.White.copy(alpha = 0.45f), style = Stroke(width = w * 0.035f, cap = StrokeCap.Round))
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

private fun Float.toRad() = (this * PI / 180).toFloat()
