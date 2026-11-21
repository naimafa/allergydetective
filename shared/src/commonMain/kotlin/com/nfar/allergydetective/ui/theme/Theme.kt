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
    // Design of 21.11.2026: warm cream instead of the cool grey, the neutrals below follow.
    background = Color(0xFFF9F6E8),
    onBackground = Color(0xFF293831),
    surface = Color(0xFFF9F6E8),
    onSurface = Color(0xFF293831),
    surfaceVariant = Color(0xFFDDE3DC),
    onSurfaceVariant = Color(0xFF5F6A63),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF4F1E3),
    surfaceContainer = Color(0xFFFBF8EC),
    surfaceContainerHigh = Color(0xFFEAE7D8),
    surfaceContainerHighest = Color(0xFFE3E0D0),
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
    // Dark mode feedback 19.11.: terracotta a little less saturated.
    tertiary = Color(0xFFE6BFAE),
    onTertiary = Color(0xFF55200F),
    tertiaryContainer = Color(0xFF5A3B31),
    onTertiaryContainer = Color(0xFFF6E4DC),
    background = Color(0xFF121512),
    onBackground = Color(0xFFE2E4DE),
    surface = Color(0xFF121512),
    onSurface = Color(0xFFE2E4DE),
    surfaceVariant = Color(0xFF424A44),
    // Dark mode feedback 19.11.: secondary and status texts a little lighter.
    onSurfaceVariant = Color(0xFFD2D8D1),
    surfaceContainerLowest = Color(0xFF0D100D),
    surfaceContainerLow = Color(0xFF1A1D1A),
    // Bottom navigation: clearly lighter than the page.
    surfaceContainer = Color(0xFF242924),
    surfaceContainerHigh = Color(0xFF282C28),
    surfaceContainerHighest = Color(0xFF333733),
    outline = Color(0xFF8C948C),
    outlineVariant = Color(0xFF424A44),
)

/** A home tile: light corner top left, deeper corner bottom right, and the soft wave along the bottom. */
@Immutable
data class TileGradient(val start: Color, val end: Color, val wave: Color)

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
    /** Soft blue for "new entries" badges (StatusBadge). */
    val info: Color,
    val onInfo: Color,
    /** Start screen (design of 21.11.2026). */
    val home: HomeColors,
)

/**
 * Colours of the start screen, measured from the design picture of 21.11.2026: warm cream page,
 * deep forest green for text, bar and icons, sage and apricot tiles.
 */
@Immutable
data class HomeColors(
    /** Page: cream, with a peach tint towards the right edge. */
    val page: Color,
    val pageTint: Color,
    /** Headings, numbers, the progress bar. */
    val ink: Color,
    /** Tagline and secondary text. */
    val inkSoft: Color,
    /** Icons on the sage cards. */
    val icon: Color,
    /** Logo: dark and light leaf. */
    val logoDark: Color,
    val logoLight: Color,
    /** Branch top right: leaves and stem. */
    val leaf: Color,
    val leafLight: Color,
    val progressCard: Color,
    val progressTrack: Color,
    val badge: Color,
    val todayCard: Color,
    val mealTile: TileGradient,
    val symptomTile: TileGradient,
    val dayCheckTile: TileGradient,
    val resultsTile: TileGradient,
    /** Text on the sage and on the apricot tiles. */
    val onSageTile: Color,
    val onWarmTile: Color,
    val heart: Color,
    val moon: Color,
    /** "Jetzt abschließen". */
    val button: Color,
    val onButton: Color,
    /** Red dot on the bell. */
    val dot: Color,
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
    info = Color(0xFFDDEAEE),
    onInfo = Color(0xFF24505C),
    home = HomeColors(
        page = Color(0xFFF9F6E8),
        pageTint = Color(0xFFF8E6D6),
        ink = Color(0xFF1B4B2E),
        inkSoft = Color(0xFF36543C),
        icon = Color(0xFF2E6140),
        logoDark = Color(0xFF3F7049),
        logoLight = Color(0xFF6E9C72),
        leaf = Color(0xFFA8BF98),
        leafLight = Color(0xFFD3DDC4),
        progressCard = Color(0xFFFDFCF5),
        progressTrack = Color(0xFFE6E5D3),
        badge = Color(0xFFE2E7D5),
        todayCard = Color(0xFFE4E9D6),
        mealTile = TileGradient(Color(0xFFEDF0DF), Color(0xFFD9E2CA), Color(0xFFBDCFB0)),
        symptomTile = TileGradient(Color(0xFFFCE7D3), Color(0xFFFBD5B6), Color(0xFFF8C298)),
        dayCheckTile = TileGradient(Color(0xFFFBEFD9), Color(0xFFF8DCBA), Color(0xFFF4C493)),
        resultsTile = TileGradient(Color(0xFFEDEEDE), Color(0xFFDAE3CC), Color(0xFFA4BF98)),
        onSageTile = Color(0xFF152812),
        onWarmTile = Color(0xFF361A04),
        heart = Color(0xFFD16F2E),
        moon = Color(0xFFE58A36),
        // White text on it reaches about 4.7 : 1.
        button = Color(0xFFB15626),
        onButton = Color.White,
        dot = Color(0xFFD9483B),
    ),
)

private val DarkExtra = ExtraColors(
    digestive = Color(0xFF4A3D28),
    skin = Color(0xFF4A3830),
    head = Color(0xFF3A3349),
    airways = Color(0xFF2A3D44),
    other = Color(0xFF34433C),
    clues = Color(0xFF3A3349),
    onClues = Color(0xFFE8E2F0),
    cluesAccent = Color(0xFFCDBFE8),
    redFlag = Color(0xFF7E2B25),
    info = Color(0xFF2A3D44),
    onInfo = Color(0xFFCBE3EA),
    home = HomeColors(
        page = Color(0xFF121512),
        pageTint = Color(0xFF1E1A15),
        ink = Color(0xFFB4DCC0),
        inkSoft = Color(0xFFCAD6C6),
        icon = Color(0xFF9CC7A8),
        logoDark = Color(0xFF6FA57A),
        logoLight = Color(0xFF9CC7A0),
        leaf = Color(0xFF3F5A44),
        leafLight = Color(0xFF2E3F31),
        progressCard = Color(0xFF1C201C),
        progressTrack = Color(0xFF333733),
        badge = Color(0xFF2A4A3F),
        todayCard = Color(0xFF243A2E),
        mealTile = TileGradient(Color(0xFF34544A), Color(0xFF22362D), Color(0xFF3E6254)),
        symptomTile = TileGradient(Color(0xFF65453A), Color(0xFF402A21), Color(0xFF77503F)),
        dayCheckTile = TileGradient(Color(0xFF5E4732), Color(0xFF3E2D1F), Color(0xFF725638)),
        resultsTile = TileGradient(Color(0xFF3A4D44), Color(0xFF25332C), Color(0xFF465C51)),
        onSageTile = Color(0xFFDCE8DF),
        onWarmTile = Color(0xFFF6E4DC),
        heart = Color(0xFFF0A27A),
        moon = Color(0xFFF2C083),
        button = Color(0xFFF0A27A),
        onButton = Color(0xFF4A1E08),
        dot = Color(0xFFFF8A80),
    ),
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
