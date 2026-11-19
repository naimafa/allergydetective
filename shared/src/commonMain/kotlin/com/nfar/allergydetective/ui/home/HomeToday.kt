package com.nfar.allergydetective.ui.home

import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.today_done
import allergydetective.shared.generated.resources.today_meals
import allergydetective.shared.generated.resources.today_nothing_yet
import allergydetective.shared.generated.resources.today_open
import allergydetective.shared.generated.resources.today_symptoms
import allergydetective.shared.generated.resources.today_title
import allergydetective.shared.generated.resources.weekdays_short
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import com.nfar.allergydetective.ui.common.formatDate
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource

/**
 * "Your day today" (feedback 19.11.), compact: date, today's meals and symptoms in one line and a
 * hint with the next step. Replaces the "2 today" badges on the tiles; a tap opens the history.
 */
@Composable
fun HomeToday(state: HomeState, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val date = LocalDate.fromEpochDays(state.today)
    val weekday = stringArrayResource(Res.array.weekdays_short).getOrNull(date.dayOfWeek.ordinal).orEmpty()
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = scheme.secondaryContainer, contentColor = scheme.onSecondaryContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(Res.string.today_title), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Text(
                    "$weekday, ${formatDate(date, Locale.current.language)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Count(Icons.Outlined.Restaurant, pluralStringResource(Res.plurals.today_meals, state.mealsToday, state.mealsToday))
                Count(Icons.Outlined.Bolt, pluralStringResource(Res.plurals.today_symptoms, state.symptomsToday, state.symptomsToday))
            }
            HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.5f))
            val (icon, hint) = when {
                !state.todayOpen -> Icons.Outlined.CheckCircle to stringResource(Res.string.today_done)
                state.mealsToday + state.symptomsToday == 0 -> Icons.Outlined.Info to stringResource(Res.string.today_nothing_yet)
                else -> Icons.Outlined.Info to stringResource(Res.string.today_open)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(icon, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(18.dp))
                Text(hint, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun Count(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}
