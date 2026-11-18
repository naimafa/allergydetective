package com.nfar.allergydetective.ui.home

import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.daily_quotes
import allergydetective.shared.generated.resources.home_day_check
import allergydetective.shared.generated.resources.home_log_meal
import allergydetective.shared.generated.resources.home_log_symptom
import allergydetective.shared.generated.resources.home_results
import allergydetective.shared.generated.resources.home_title
import androidx.compose.foundation.layout.Arrangement
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

enum class HomeAction(val label: StringResource, val icon: ImageVector) {
    LOG_MEAL(Res.string.home_log_meal, Icons.Outlined.Restaurant),
    LOG_SYMPTOM(Res.string.home_log_symptom, Icons.Outlined.Bolt),
    DAY_CHECK(Res.string.home_day_check, Icons.Outlined.Bedtime),
    RESULTS(Res.string.home_results, Icons.Outlined.TravelExplore),
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

@Composable
private fun ActionTile(action: HomeAction, onClick: () -> Unit, modifier: Modifier) {
    val (container, content) = tileColors(action)
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = container, contentColor = content),
        modifier = modifier.heightIn(min = 120.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(action.icon, contentDescription = null, modifier = Modifier.size(32.dp))
            Text(stringResource(action.label), style = MaterialTheme.typography.titleMedium)
        }
    }
}

/** Food = petrol, symptoms = coral, day check = sage, results = neutral (UX 1.1). */
@Composable
private fun tileColors(action: HomeAction): Pair<Color, Color> {
    val scheme = MaterialTheme.colorScheme
    return when (action) {
        HomeAction.LOG_MEAL -> scheme.primaryContainer to scheme.onPrimaryContainer
        HomeAction.LOG_SYMPTOM -> scheme.tertiaryContainer to scheme.onTertiaryContainer
        HomeAction.DAY_CHECK -> scheme.secondaryContainer to scheme.onSecondaryContainer
        HomeAction.RESULTS -> scheme.surfaceContainerHigh to scheme.onSurface
    }
}
