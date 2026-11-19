package com.nfar.allergydetective.ui.home

import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.daily_quotes
import allergydetective.shared.generated.resources.home_actions_title
import allergydetective.shared.generated.resources.home_day_check_hint
import allergydetective.shared.generated.resources.home_day_open
import allergydetective.shared.generated.resources.home_heading
import allergydetective.shared.generated.resources.tile_meal
import allergydetective.shared.generated.resources.tile_symptom
import allergydetective.shared.generated.resources.tile_day_check
import allergydetective.shared.generated.resources.tile_results
import allergydetective.shared.generated.resources.home_log_meal_hint
import allergydetective.shared.generated.resources.home_log_symptom_hint
import allergydetective.shared.generated.resources.home_results_hint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.QueryStats
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.nfar.allergydetective.ui.common.StatusBadge
import com.nfar.allergydetective.ui.common.StatusKind
import com.nfar.allergydetective.ui.theme.AppColors
import allergydetective.shared.generated.resources.home_day_done
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource

enum class HomeAction(val label: StringResource, val hint: StringResource, val icon: ImageVector) {
    LOG_MEAL(Res.string.tile_meal, Res.string.home_log_meal_hint, Icons.Outlined.Restaurant),
    LOG_SYMPTOM(Res.string.tile_symptom, Res.string.home_log_symptom_hint, Icons.Outlined.Bolt),
    DAY_CHECK(Res.string.tile_day_check, Res.string.home_day_check_hint, Icons.Outlined.Bedtime),
    RESULTS(Res.string.tile_results, Res.string.home_results_hint, Icons.Outlined.QueryStats),
}

/** Dashboard (UX 2, design of 19.11.2026): heading, quote of the day, progress card and the four quick actions. */
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
    // Design of 19.11.: a slimmer top, the four tiles get about 40 % of the screen height (47 % left too much room between icon and text),
    // all of them exactly the same size. Spacing on an 8 dp grid (8 / 16 / 24).
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val tileHeight = ((maxHeight * TILES_SHARE - GRID) / 2).coerceIn(MIN_TILE_HEIGHT, MAX_TILE_HEIGHT)
        // Text width inside a tile: screen padding 2 × 16, gap, tile padding on both sides, a small safety margin.
        val textWidth = (maxWidth - 32.dp - GRID) / 2 - TILE_PADDING * 2 - SAFETY_MARGIN
        val tileText = sharedTileStyles(textWidth)
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(stringResource(Res.string.home_heading), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                DailyQuote(state.today)
            }
            banner()
            state.confirmedDays?.let {
                HomeProgress(it)
                HomeToday(state, onClick = onOpenToday)
            }
            Column(verticalArrangement = Arrangement.spacedBy(GRID), modifier = Modifier.padding(top = 8.dp)) {
                Text(stringResource(Res.string.home_actions_title), style = MaterialTheme.typography.titleMedium)
                HomeAction.entries.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(GRID)) {
                        row.forEach { action ->
                            ActionTile(
                                action,
                                badge = { TileBadge(action, state) },
                                onClick = { onAction(action) },
                                modifier = Modifier.weight(1f).height(tileHeight),
                                text = tileText,
                            )
                        }
                    }
                }
            }
        }
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

/** Part of the screen height for the four tiles (design of 19.11.). */
private const val TILES_SHARE = 0.40f
private val GRID = 12.dp
private val MIN_TILE_HEIGHT = 120.dp
private val MAX_TILE_HEIGHT = 160.dp
private val TILE_PADDING = 18.dp
private val SAFETY_MARGIN = 6.dp

/** Status of today on the tile (self-test 19.11.): day check done in green or open in grey. */
@Composable
private fun TileBadge(action: HomeAction, state: HomeState) {
    when (action) {
        // Counts of today are in the "Your day today" card, not on the tiles.
        HomeAction.LOG_MEAL, HomeAction.LOG_SYMPTOM -> Unit
        HomeAction.DAY_CHECK -> if (state.confirmedDays != null) {
            if (state.todayOpen) {
                StatusBadge(stringResource(Res.string.home_day_open), StatusKind.OPEN)
            } else {
                StatusBadge(stringResource(Res.string.home_day_done), StatusKind.DONE)
            }
        }
        HomeAction.RESULTS -> Unit
    }
}

@Composable
private fun DailyQuote(epochDay: Long) {
    val quotes = stringArrayResource(Res.array.daily_quotes)
    if (quotes.isEmpty()) return
    // One or two short lines.
    Text(
        quotes[HomeViewModel.quoteIndex(epochDay, quotes.size)],
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
    )
}

/**
 * Design of 19.11.: all four tiles follow the same rules. Same size and padding, icon top left,
 * status top right, title in the lower third with the description (up to two lines) right below it.
 * Title 18 sp semibold, description 15 sp regular, icon 32 dp, padding 18 dp; with a large system font all four shrink together.
 */
@Composable
private fun ActionTile(
    action: HomeAction,
    badge: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier,
    text: TileTextStyles,
) {
    val colors = tileColors(action)
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = colors.container, contentColor = colors.content),
        modifier = modifier,
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(TILE_PADDING)) {
            Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
                Icon(action.icon, contentDescription = null, tint = colors.accent, modifier = Modifier.size(32.dp))
                Spacer(Modifier.weight(1f))
                badge()
            }
            Spacer(Modifier.weight(1f))
            // Sizes shared by all four tiles (sharedTileStyles). The title never wraps; the description
            // always reserves two lines, so the text area has the same height and every title sits on the same line.
            Text(stringResource(action.label), style = text.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                stringResource(action.hint),
                style = text.hint,
                color = colors.content.copy(alpha = 0.85f),
                minLines = text.hintLines,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

private class TileColors(val container: Color, val content: Color, val accent: Color)

/** Text sizes shared by the four tiles, and how many lines every description reserves. */
private class TileTextStyles(val title: TextStyle, val hint: TextStyle, val hintLines: Int)

/** Food = sage, symptoms = apricot, day check = lavender, results = grey-green (palette of 19.11.2026). */
@Composable
private fun tileColors(action: HomeAction): TileColors {
    val scheme = MaterialTheme.colorScheme
    val extra = AppColors.extra
    return when (action) {
        HomeAction.LOG_MEAL -> TileColors(scheme.primaryContainer, scheme.onPrimaryContainer, scheme.primary)
        HomeAction.LOG_SYMPTOM -> TileColors(scheme.tertiaryContainer, scheme.onTertiaryContainer, scheme.tertiary)
        HomeAction.DAY_CHECK -> TileColors(extra.clues, extra.onClues, extra.cluesAccent)
        HomeAction.RESULTS -> TileColors(scheme.secondaryContainer, scheme.onSecondaryContainer, scheme.secondary)
    }
}
