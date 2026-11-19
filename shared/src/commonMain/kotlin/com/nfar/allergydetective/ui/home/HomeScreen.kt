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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
import com.nfar.allergydetective.ui.theme.AppColors
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
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(stringResource(Res.string.home_heading), style = MaterialTheme.typography.headlineMedium)
            DailyQuote(state.today)
        }
        banner()
        state.confirmedDays?.let { HomeProgress(it) }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(Res.string.home_actions_title), style = MaterialTheme.typography.titleMedium)
            HomeAction.entries.chunked(2).forEach { row ->
                // Same height in a row, also when one title wraps.
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.height(IntrinsicSize.Min)) {
                    row.forEach { action ->
                        ActionTile(
                            action,
                            open = action == HomeAction.DAY_CHECK && state.todayOpen,
                            onClick = { onAction(action) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyQuote(epochDay: Long) {
    val quotes = stringArrayResource(Res.array.daily_quotes)
    if (quotes.isEmpty()) return
    Text(
        quotes[HomeViewModel.quoteIndex(epochDay, quotes.size)],
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

/**
 * Self-test 19.11.: "Tages-Abschluss" was cut off although there was room. Less inner padding,
 * the icon without its circle, and the title may wrap to a second line instead of being clipped.
 */
@Composable
private fun ActionTile(action: HomeAction, open: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val colors = tileColors(action)
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = colors.container, contentColor = colors.content),
        modifier = modifier.fillMaxHeight().heightIn(min = TILE_HEIGHT),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(action.icon, contentDescription = null, tint = colors.accent, modifier = Modifier.size(28.dp))
                Spacer(Modifier.weight(1f))
                if (open) {
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceContainerLowest, contentColor = colors.accent) {
                        Text(
                            stringResource(Res.string.home_day_open),
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                        )
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.padding(top = 12.dp)) {
                Text(stringResource(action.label), style = MaterialTheme.typography.titleMedium, maxLines = 2)
                Text(
                    stringResource(action.hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.content.copy(alpha = 0.85f),
                    maxLines = 2,
                )
            }
        }
    }
}

private val TILE_HEIGHT = 136.dp

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
