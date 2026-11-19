package com.nfar.allergydetective.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/*
 * Design language (UX 1, palette of 19.11.2026): warm white ground, soft pastel areas and
 * forest green as the one accent for buttons, active navigation and important icons.
 * Rough share: 60 % warm white, 25 % pastels, 10 % sage / grey-green, 5 % forest green.
 * Food = sage, symptoms = apricot, day check = lavender, results = grey-green.
 * Secondary text is #5F6A63 instead of the proposed #818A82, which reaches only ~3.3 : 1 (WCAG needs 4.5 : 1).
 */

private val LightColors = lightColorScheme(
    primary = Color(0xFF385B50),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE8DF),
    onPrimaryContainer = Color(0xFF12271F),
    secondary = Color(0xFF4A5F55),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE5EBE7),
    onSecondaryContainer = Color(0xFF1E2B25),
    tertiary = Color(0xFF9A4A33),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF6E4DC),
    onTertiaryContainer = Color(0xFF3A160B),
    background = Color(0xFFF6F7F3),
    onBackground = Color(0xFF293831),
    surface = Color(0xFFF6F7F3),
    onSurface = Color(0xFF293831),
    surfaceVariant = Color(0xFFDDE3DC),
    onSurfaceVariant = Color(0xFF5F6A63),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF0F2EC),
    surfaceContainer = Color(0xFFEAEDE6),
    surfaceContainerHigh = Color(0xFFE4E8E0),
    surfaceContainerHighest = Color(0xFFDEE3DA),
    outline = Color(0xFF737D75),
    outlineVariant = Color(0xFFC3CBC2),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9CC7B6),
    onPrimary = Color(0xFF0B3328),
    primaryContainer = Color(0xFF2A4A3F),
    onPrimaryContainer = Color(0xFFDCE8DF),
    secondary = Color(0xFFB4C7BC),
    onSecondary = Color(0xFF1F2E27),
    secondaryContainer = Color(0xFF34433C),
    onSecondaryContainer = Color(0xFFE5EBE7),
    tertiary = Color(0xFFF0B9A3),
    onTertiary = Color(0xFF55200F),
    tertiaryContainer = Color(0xFF6E3524),
    onTertiaryContainer = Color(0xFFF6E4DC),
    background = Color(0xFF121512),
    onBackground = Color(0xFFE2E4DE),
    surface = Color(0xFF121512),
    onSurface = Color(0xFFE2E4DE),
    surfaceVariant = Color(0xFF424A44),
    onSurfaceVariant = Color(0xFFC2C9C1),
    surfaceContainerLowest = Color(0xFF0D100D),
    surfaceContainerLow = Color(0xFF1A1D1A),
    surfaceContainer = Color(0xFF1E221E),
    surfaceContainerHigh = Color(0xFF282C28),
    surfaceContainerHighest = Color(0xFF333733),
    outline = Color(0xFF8C948C),
    outlineVariant = Color(0xFF424A44),
)

/** Colors outside the Material roles, e.g. the four symptom groups (CONTENT_Symptome 1). */
@Immutable
data class ExtraColors(
    val digestive: Color,
    val skin: Color,
    val head: Color,
    val airways: Color,
    val other: Color,
    /** Lavender, so the four home tiles have four distinct colours. */
    val clues: Color,
    val onClues: Color,
    /** Strong accent on lavender, e.g. its icon. */
    val cluesAccent: Color,
    /** Emergency symptoms and the notice, always clearly visible. */
    val redFlag: Color,
)

private val LightExtra = ExtraColors(
    digestive = Color(0xFFF8E8D2),
    skin = Color(0xFFF6E4DC),
    head = Color(0xFFE8E2F0),
    airways = Color(0xFFDDEAEE),
    other = Color(0xFFE5EBE7),
    clues = Color(0xFFE8E2F0),
    onClues = Color(0xFF2B2238),
    cluesAccent = Color(0xFF5E4E86),
    redFlag = Color(0xFFFFCDC8),
)

private val DarkExtra = ExtraColors(
    digestive = Color(0xFF4A3D28),
    skin = Color(0xFF4D3329),
    head = Color(0xFF3A3349),
    airways = Color(0xFF2A3D44),
    other = Color(0xFF34433C),
    clues = Color(0xFF3A3349),
    onClues = Color(0xFFE8E2F0),
    cluesAccent = Color(0xFFCDBFE8),
    redFlag = Color(0xFF7E2B25),
)

val LocalExtraColors = staticCompositionLocalOf { LightExtra }

/** Slightly larger and calmer than the Material defaults; system sans-serif (Inter would need a font file). */
private val AppTypography = Typography().run {
    copy(
        headlineMedium = headlineMedium.copy(fontWeight = FontWeight.Bold),
        titleLarge = titleLarge.copy(fontWeight = FontWeight.SemiBold),
        titleMedium = titleMedium.copy(fontWeight = FontWeight.SemiBold),
        bodyLarge = TextStyle(fontSize = 17.sp, lineHeight = 24.sp),
        bodyMedium = TextStyle(fontSize = 15.sp, lineHeight = 21.sp),
    )
}

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun AppTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalExtraColors provides if (darkTheme) DarkExtra else LightExtra) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = AppTypography,
            shapes = AppShapes,
            content = content,
        )
    }
}

object AppColors {
    val extra: ExtraColors
        @Composable get() = LocalExtraColors.current
}
