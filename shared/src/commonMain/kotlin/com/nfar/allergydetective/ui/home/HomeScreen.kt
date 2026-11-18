package com.nfar.allergydetective.ui.home

import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.daily_quotes
import allergydetective.shared.generated.resources.home_day_check
import allergydetective.shared.generated.resources.home_day_check_hint
import allergydetective.shared.generated.resources.home_log_meal_hint
import allergydetective.shared.generated.resources.home_log_symptom_hint
import allergydetective.shared.generated.resources.home_results_hint
import allergydetective.shared.generated.resources.home_log_meal
import allergydetective.shared.generated.resources.home_log_symptom
import allergydetective.shared.generated.resources.home_results
import allergydetective.shared.generated.resources.home_title
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import com.nfar.allergydetective.ui.theme.AppColors
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource

enum class HomeAction(val label: StringResource, val hint: StringResource, val icon: ImageVector) {
    LOG_MEAL(Res.string.home_log_meal, Res.string.home_log_meal_hint, Icons.Outlined.Restaurant),
    LOG_SYMPTOM(Res.string.home_log_symptom, Res.string.home_log_symptom_hint, Icons.Outlined.Bolt),
    DAY_CHECK(Res.string.home_day_check, Res.string.home_day_check_hint, Icons.Outlined.Bedtime),
    RESULTS(Res.string.home_results, Res.string.home_results_hint, Icons.Outlined.TravelExplore),
}

/** Dashboard (UX 2): quote of the day, detective progress and the four quick actions. */
@Composable
fun HomeScreen(
    onAction: (HomeAction) -> Unit,
    modifier: Modifier = Modifier,
    state: HomeState = HomeState(),
    /** Optional card on top, e.g. "ill since …" (self-test 18.11.). */
    banner: @Composable () -> Unit = {},
) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(Res.string.home_title), style = MaterialTheme.typography.headlineMedium)
        banner()
        DailyQuote(state.today)
        state.confirmedDays?.let { HomeProgress(it) }
        HomeAction.entries.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { action ->
                    ActionTile(action, onClick = { onAction(action) }, modifier = Modifier.weight(1f))
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
        style = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

/**
 * Self-test 18.11.: tiles looked plain and uneven. All four have the same height, an icon in a
 * strongly coloured circle, the title and a short hint what happens.
 */
@Composable
private fun ActionTile(action: HomeAction, onClick: () -> Unit, modifier: Modifier) {
    val colors = tileColors(action)
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = colors.container, contentColor = colors.content),
        modifier = modifier.height(TILE_HEIGHT),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Surface(shape = CircleShape, color = colors.accent, contentColor = colors.onAccent, modifier = Modifier.size(48.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(action.icon, contentDescription = null, modifier = Modifier.size(26.dp))
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(stringResource(action.label), style = MaterialTheme.typography.titleMedium, maxLines = 1)
                Text(
                    stringResource(action.hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.content.copy(alpha = 0.8f),
                    maxLines = 2,
                )
            }
        }
    }
}

private val TILE_HEIGHT = 156.dp

private class TileColors(val container: Color, val content: Color, val accent: Color, val onAccent: Color)

/** Food = petrol, symptoms = coral, day check = sage, results = lavender (UX 1.1). */
@Composable
private fun tileColors(action: HomeAction): TileColors {
    val scheme = MaterialTheme.colorScheme
    val extra = AppColors.extra
    return when (action) {
        HomeAction.LOG_MEAL -> TileColors(scheme.primaryContainer, scheme.onPrimaryContainer, scheme.primary, scheme.onPrimary)
        HomeAction.LOG_SYMPTOM -> TileColors(scheme.tertiaryContainer, scheme.onTertiaryContainer, scheme.tertiary, scheme.onTertiary)
        HomeAction.DAY_CHECK -> TileColors(scheme.secondaryContainer, scheme.onSecondaryContainer, scheme.secondary, scheme.onSecondary)
        HomeAction.RESULTS -> TileColors(extra.clues, extra.onClues, extra.cluesAccent, extra.clues)
    }
}
