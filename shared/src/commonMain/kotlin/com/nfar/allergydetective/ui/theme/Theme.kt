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
 * Design language (UX 1): calm pastel surfaces, darker variants for text and icons (WCAG ≥ 4.5 : 1).
 * Petrol = trust (primary), sage = "tolerated well" (secondary), coral = symptoms (tertiary).
 */

private val LightColors = lightColorScheme(
    primary = Color(0xFF2E6371),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCFE8EE),
    onPrimaryContainer = Color(0xFF0B2F37),
    secondary = Color(0xFF4F6F67),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD7E8E2),
    onSecondaryContainer = Color(0xFF15302A),
    tertiary = Color(0xFFB5453F),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDAD7),
    onTertiaryContainer = Color(0xFF3F0705),
    background = Color(0xFFF7FAFA),
    onBackground = Color(0xFF181C1D),
    surface = Color(0xFFF7FAFA),
    onSurface = Color(0xFF181C1D),
    surfaceVariant = Color(0xFFDDE4E5),
    onSurfaceVariant = Color(0xFF3F4849),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF1F5F5),
    surfaceContainer = Color(0xFFEBF0F0),
    surfaceContainerHigh = Color(0xFFE5EBEB),
    surfaceContainerHighest = Color(0xFFDFE5E5),
    outline = Color(0xFF6F797A),
    outlineVariant = Color(0xFFBFC8C9),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8FCFDE),
    onPrimary = Color(0xFF00363F),
    primaryContainer = Color(0xFF1F4D58),
    onPrimaryContainer = Color(0xFFCFE8EE),
    secondary = Color(0xFFB2CDC5),
    onSecondary = Color(0xFF1E352F),
    secondaryContainer = Color(0xFF354D46),
    onSecondaryContainer = Color(0xFFD7E8E2),
    tertiary = Color(0xFFFFB4AC),
    onTertiary = Color(0xFF5F1410),
    tertiaryContainer = Color(0xFF7E2B25),
    onTertiaryContainer = Color(0xFFFFDAD7),
    background = Color(0xFF101415),
    onBackground = Color(0xFFDFE3E4),
    surface = Color(0xFF101415),
    onSurface = Color(0xFFDFE3E4),
    surfaceVariant = Color(0xFF3F4849),
    onSurfaceVariant = Color(0xFFBFC8C9),
    surfaceContainerLowest = Color(0xFF0B0F10),
    surfaceContainerLow = Color(0xFF181C1D),
    surfaceContainer = Color(0xFF1C2021),
    surfaceContainerHigh = Color(0xFF262B2C),
    surfaceContainerHighest = Color(0xFF313637),
    outline = Color(0xFF899294),
    outlineVariant = Color(0xFF3F4849),
)

/** Colors outside the Material roles, e.g. the four symptom groups (CONTENT_Symptome 1). */
@Immutable
data class ExtraColors(
    val digestive: Color,
    val skin: Color,
    val head: Color,
    val airways: Color,
    val other: Color,
    /** Emergency symptoms and the notice, always clearly visible. */
    val redFlag: Color,
)

private val LightExtra = ExtraColors(
    digestive = Color(0xFFFCE9D4),
    skin = Color(0xFFFDE1E1),
    head = Color(0xFFE6E3F7),
    airways = Color(0xFFDCEEF7),
    other = Color(0xFFE8ECEC),
    redFlag = Color(0xFFFFCDC8),
)

private val DarkExtra = ExtraColors(
    digestive = Color(0xFF4A3A26),
    skin = Color(0xFF4D3030),
    head = Color(0xFF39354F),
    airways = Color(0xFF283F4B),
    other = Color(0xFF2F3536),
    redFlag = Color(0xFF7E2B25),
)

val LocalExtraColors = staticCompositionLocalOf { LightExtra }

/** Slightly larger and calmer than the Material defaults; system sans-serif (Inter would need a font file). */
private val AppTypography = Typography().run {
    copy(
        headlineMedium = headlineMedium.copy(fontWeight = FontWeight.SemiBold),
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
    large = RoundedCornerShape(24.dp),
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
