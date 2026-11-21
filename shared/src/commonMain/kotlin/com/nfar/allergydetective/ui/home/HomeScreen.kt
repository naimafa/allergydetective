package com.nfar.allergydetective.ui.home

import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.home_brand
import allergydetective.shared.generated.resources.home_day_check_hint
import allergydetective.shared.generated.resources.home_day_check_now
import allergydetective.shared.generated.resources.home_day_done
import allergydetective.shared.generated.resources.home_greeting_day
import allergydetective.shared.generated.resources.home_greeting_evening
import allergydetective.shared.generated.resources.home_greeting_morning
import allergydetective.shared.generated.resources.home_log_meal_hint
import allergydetective.shared.generated.resources.home_log_symptom_hint
import allergydetective.shared.generated.resources.home_results_hint
import allergydetective.shared.generated.resources.home_tagline
import allergydetective.shared.generated.resources.tile_day_check
import allergydetective.shared.generated.resources.tile_meal
import allergydetective.shared.generated.resources.tile_results
import allergydetective.shared.generated.resources.tile_symptom
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nfar.allergydetective.ui.common.StatusBadge
import com.nfar.allergydetective.ui.common.StatusKind
import com.nfar.allergydetective.ui.theme.AppColors
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

enum class HomeAction(val label: StringResource, val hint: StringResource, val icon: ImageVector) {
    LOG_MEAL(Res.string.tile_meal, Res.string.home_log_meal_hint, Icons.Filled.Restaurant),
    LOG_SYMPTOM(Res.string.tile_symptom, Res.string.home_log_symptom_hint, Icons.Filled.MonitorHeart),
    DAY_CHECK(Res.string.tile_day_check, Res.string.home_day_check_hint, Icons.Filled.Bedtime),
    RESULTS(Res.string.tile_results, Res.string.home_results_hint, Icons.Filled.QueryStats),
}

/**
 * Dashboard, home design of 21.11.2026: logo and name, a greeting by time of day with the tagline,
 * the progress card, "your day today" with three numbers and the four tiles with soft gradients.
 * The day check tile carries a "finish now" button while today is open.
 */
@Composable
fun HomeScreen(
    onAction: (HomeAction) -> Unit,
    /** "Your day today" opens the history. */
    onOpenToday: () -> Unit = {},
    modifier: Modifier = Modifier,
    state: HomeState = HomeState(),
    /** Optional card on top, e.g. "ill since ..." (self-test 18.11.). */
    banner: @Composable () -> Unit = {},
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        // Text width inside a tile: screen padding 2 × 16, gap, tile padding on both sides, a small safety margin.
        val textWidth = (maxWidth - 32.dp - GRID) / 2 - TILE_PADDING * 2 - SAFETY_MARGIN
        val tileText = sharedTileStyles(textWidth)
        Box(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Leaves(modifier = Modifier.align(Alignment.TopEnd))
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Header(state.greeting)
                banner()
                state.confirmedDays?.let {
                    HomeProgress(it)
                    HomeToday(state, onClick = onOpenToday)
                }
                Column(verticalArrangement = Arrangement.spacedBy(GRID)) {
                    HomeAction.entries.chunked(2).forEach { row ->
                        // Both tiles of a row get the height of the taller one (the button on the day check tile).
                        Row(horizontalArrangement = Arrangement.spacedBy(GRID), modifier = Modifier.height(IntrinsicSize.Min)) {
                            row.forEach { action ->
                                ActionTile(
                                    action,
                                    state = state,
                                    onClick = { onAction(action) },
                                    modifier = Modifier.weight(1f).fillMaxHeight().heightIn(min = MIN_TILE_HEIGHT),
                                    text = tileText,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Header(greeting: Greeting) {
    val scheme = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(Icons.Filled.Eco, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(36.dp))
            Text(
                stringResource(Res.string.home_brand),
                style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Medium),
                color = scheme.primary,
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(
                when (greeting) {
                    Greeting.MORNING -> Res.string.home_greeting_morning
                    Greeting.DAY -> Res.string.home_greeting_day
                    Greeting.EVENING -> Res.string.home_greeting_evening
                },
            ),
            style = MaterialTheme.typography.headlineLarge.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold),
            color = scheme.primary,
        )
        Text(
            stringResource(Res.string.home_tagline),
            style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Serif),
            color = scheme.onSurfaceVariant,
        )
    }
}

/** Soft leaves top right behind the greeting, only decoration. */
@Composable
private fun Leaves(modifier: Modifier) {
    val tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
    Box(modifier = modifier.size(190.dp)) {
        Icon(
            Icons.Filled.Eco,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(170.dp).offset(x = 50.dp, y = 10.dp).rotate(-20f),
        )
        Icon(
            Icons.Filled.Eco,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(110.dp).offset(x = 10.dp, y = 90.dp).rotate(35f),
        )
    }
}

/**
 * Phone test 19.11.: at font scale 1.25 the tiles shrank their texts one by one and looked uneven.
 * One size for all four titles and one for all four descriptions, the largest that fits:
 * titles 18 → 13 sp in one line (never wrapped), descriptions 15 → 11 sp in at most two lines
 * without breaking a word. Steps of 0.5 sp.
 */
@Composable
private fun sharedTileStyles(textWidth: Dp): TileTextStyles {
    val measurer = rememberTextMeasurer()
    val widthPx = with(LocalDensity.current) { textWidth.toPx() }
    val safetyPx = with(LocalDensity.current) { SAFETY_MARGIN.toPx() }
    val titles = HomeAction.entries.map { stringResource(it.label) }
    val hints = HomeAction.entries.map { stringResource(it.hint) }
    val title = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold)
    val hint = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp, lineHeight = 20.sp, letterSpacing = 0.sp, fontWeight = FontWeight.Normal)
    return remember(titles, hints, widthPx, safetyPx, title, hint) {
        fun oneLine(text: String, style: TextStyle) =
            measurer.measure(text, style, maxLines = 1, softWrap = false).size.width <= widthPx

        fun twoLines(text: String, style: TextStyle) =
            text.split(' ').all { oneLine(it, style) } &&
                !measurer.measure(text, style, maxLines = 2, constraints = Constraints(maxWidth = widthPx.toInt())).hasVisualOverflow

        fun fitting(texts: List<String>, style: TextStyle, minSize: Float, fits: (String, TextStyle) -> Boolean): TextStyle {
            var size = style.fontSize.value
            while (size > minSize) {
                // Line height shrinks along with the font.
                val candidate = style.copy(fontSize = size.sp, lineHeight = (size * style.lineHeight.value / style.fontSize.value).sp)
                if (texts.all { fits(it, candidate) }) return candidate
                size -= 0.5f
            }
            return style.copy(fontSize = minSize.sp, lineHeight = (minSize * style.lineHeight.value / style.fontSize.value).sp)
        }
        val titleStyle = fitting(titles, title, 13f, ::oneLine)
        // The description stays clearly smaller than the title (phone test 19.11.: 15 sp hint under a shrunk title).
        val hintStart = minOf(hint.fontSize.value, titleStyle.fontSize.value - 3f)
        val hintStyle = fitting(hints, hint.copy(fontSize = hintStart.sp, lineHeight = (hintStart * 4f / 3f).sp), 11f, ::twoLines)
        // Two lines are only reserved when one of the four descriptions needs them.
        // Lines against the real text width (without the safety margin used for the size).
        val lines = hints.maxOf { measurer.measure(it, hintStyle, maxLines = 2, constraints = Constraints(maxWidth = (widthPx + safetyPx).toInt())).lineCount }
        TileTextStyles(titleStyle, hintStyle, lines)
    }
}

private val GRID = 12.dp
private val MIN_TILE_HEIGHT = 150.dp
private val TILE_PADDING = 16.dp
private val SAFETY_MARGIN = 6.dp

/**
 * Design of 21.11.2026: soft gradient, large icon top left, a chevron top right, title and
 * description at the bottom. The day check tile adds "finish now" while today is open, "done" after.
 */
@Composable
private fun ActionTile(
    action: HomeAction,
    state: HomeState,
    onClick: () -> Unit,
    modifier: Modifier,
    text: TileTextStyles,
) {
    val colors = tileColors(action)
    val shape = MaterialTheme.shapes.large
    Surface(
        onClick = onClick,
        shape = shape,
        color = Color.Transparent,
        contentColor = colors.content,
        modifier = modifier.shadow(elevation = 2.dp, shape = shape, clip = false),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(listOf(colors.gradient.first, colors.gradient.second)), shape)
                .padding(TILE_PADDING),
        ) {
            Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
                Icon(action.icon, contentDescription = null, tint = colors.accent, modifier = Modifier.size(44.dp))
                Spacer(Modifier.weight(1f))
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = colors.content.copy(alpha = 0.8f),
                    modifier = Modifier.size(24.dp),
                )
            }
            Spacer(Modifier.weight(1f).heightIn(min = 12.dp))
            // Sizes shared by all four tiles (sharedTileStyles). The title never wraps; the description
            // reserves the same number of lines everywhere, so every title sits on the same line.
            Text(stringResource(action.label), style = text.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                stringResource(action.hint),
                style = text.hint,
                color = colors.content.copy(alpha = 0.85f),
                minLines = text.hintLines,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp),
            )
            if (action == HomeAction.DAY_CHECK && state.confirmedDays != null) {
                Spacer(Modifier.height(10.dp))
                if (state.todayOpen) FinishNowButton(onClick) else StatusBadge(stringResource(Res.string.home_day_done), StatusKind.DONE)
            }
        }
    }
}

@Composable
private fun FinishNowButton(onClick: () -> Unit) {
    val extra = AppColors.extra
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = extra.warmAccent, contentColor = extra.onWarmAccent),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        modifier = Modifier.fillMaxWidth().heightIn(min = 40.dp),
    ) {
        Text(
            stringResource(Res.string.home_day_check_now),
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        Spacer(Modifier.size(6.dp))
        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
    }
}

private class TileColors(val gradient: Pair<Color, Color>, val content: Color, val accent: Color)

/** Text sizes shared by the four tiles, and how many lines every description reserves. */
private class TileTextStyles(val title: TextStyle, val hint: TextStyle, val hintLines: Int)

/** Food and results = sage, symptoms and day check = apricot (design of 21.11.2026). */
@Composable
private fun tileColors(action: HomeAction): TileColors {
    val scheme = MaterialTheme.colorScheme
    val extra = AppColors.extra
    return when (action) {
        HomeAction.LOG_MEAL -> TileColors(extra.mealTile, scheme.onPrimaryContainer, scheme.primary)
        HomeAction.LOG_SYMPTOM -> TileColors(extra.symptomTile, extra.onWarmTile, extra.warmAccent)
        HomeAction.DAY_CHECK -> TileColors(extra.dayCheckTile, extra.onWarmTile, extra.moon)
        HomeAction.RESULTS -> TileColors(extra.resultsTile, scheme.onPrimaryContainer, scheme.primary)
    }
}
