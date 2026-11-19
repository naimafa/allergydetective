package com.nfar.allergydetective.ui.home

import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.daily_quotes
import allergydetective.shared.generated.resources.home_actions_title
import allergydetective.shared.generated.resources.home_day_check
import allergydetective.shared.generated.resources.home_day_check_hint
import allergydetective.shared.generated.resources.home_day_open
import allergydetective.shared.generated.resources.home_heading
import allergydetective.shared.generated.resources.home_log_meal
import allergydetective.shared.generated.resources.home_log_meal_hint
import allergydetective.shared.generated.resources.home_log_symptom
import allergydetective.shared.generated.resources.home_log_symptom_hint
import allergydetective.shared.generated.resources.home_results
import allergydetective.shared.generated.resources.home_results_hint
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.outlined.TravelExplore
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
import allergydetective.shared.generated.resources.home_today_count
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource

enum class HomeAction(val label: StringResource, val hint: StringResource, val icon: ImageVector) {
    LOG_MEAL(Res.string.home_log_meal, Res.string.home_log_meal_hint, Icons.Outlined.Restaurant),
    LOG_SYMPTOM(Res.string.home_log_symptom, Res.string.home_log_symptom_hint, Icons.Outlined.Bolt),
    DAY_CHECK(Res.string.home_day_check, Res.string.home_day_check_hint, Icons.Outlined.Bedtime),
    RESULTS(Res.string.home_results, Res.string.home_results_hint, Icons.Outlined.TravelExplore),
}

/** Dashboard (UX 2, design of 19.11.2026): heading, quote of the day, progress card and the four quick actions. */
@Composable
fun HomeScreen(
    onAction: (HomeAction) -> Unit,
    modifier: Modifier = Modifier,
    state: HomeState = HomeState(),
    /** Optional card on top, e.g. "ill since ..." (self-test 18.11.). */
    banner: @Composable () -> Unit = {},
) {
    // Design of 19.11.: a slimmer top, the four tiles get about half of the screen (47 %),
    // all of them exactly the same size. Spacing on an 8 dp grid (8 / 16 / 24).
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val tileHeight = ((maxHeight * TILES_SHARE - GRID) / 2).coerceIn(MIN_TILE_HEIGHT, MAX_TILE_HEIGHT)
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(Res.string.home_heading), style = MaterialTheme.typography.headlineSmall)
                DailyQuote(state.today)
            }
            banner()
            state.confirmedDays?.let { HomeProgress(it) }
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
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Part of the screen height for the four tiles (design of 19.11.). */
private const val TILES_SHARE = 0.47f
private val GRID = 12.dp
private val MIN_TILE_HEIGHT = 128.dp
private val MAX_TILE_HEIGHT = 184.dp

/** Status of today on the tile (self-test 19.11.): "2 today" in blue, day check done in green or open in grey. */
@Composable
private fun TileBadge(action: HomeAction, state: HomeState) {
    when (action) {
        HomeAction.LOG_MEAL -> if (state.mealsToday > 0) {
            StatusBadge(stringResource(Res.string.home_today_count, state.mealsToday), StatusKind.NEW)
        }
        HomeAction.LOG_SYMPTOM -> if (state.symptomsToday > 0) {
            StatusBadge(stringResource(Res.string.home_today_count, state.symptomsToday), StatusKind.NEW)
        }
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
 * status top right, title in the lower third with the description right below it. The description
 * always takes two lines, so every title sits on the same line; a long title grows upwards.
 */
@Composable
private fun ActionTile(action: HomeAction, badge: @Composable () -> Unit, onClick: () -> Unit, modifier: Modifier) {
    val colors = tileColors(action)
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = colors.container, contentColor = colors.content),
        modifier = modifier,
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
                Icon(action.icon, contentDescription = null, tint = colors.accent, modifier = Modifier.size(28.dp))
                Spacer(Modifier.weight(1f))
                badge()
            }
            Spacer(Modifier.weight(1f))
            Text(stringResource(action.label), style = MaterialTheme.typography.titleMedium, maxLines = 2)
            Text(
                stringResource(action.hint),
                style = MaterialTheme.typography.bodySmall,
                color = colors.content.copy(alpha = 0.85f),
                minLines = 2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

private class TileColors(val container: Color, val content: Color, val accent: Color)

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
