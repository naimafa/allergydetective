package com.nfar.allergydetective.ui.home

import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.today_entries
import allergydetective.shared.generated.resources.today_meals
import allergydetective.shared.generated.resources.today_symptoms
import allergydetective.shared.generated.resources.today_title
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nfar.allergydetective.ui.theme.AppColors
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/**
 * "Your day today", home design of 21.11.2026: three columns with icon and a large number
 * (meals, symptoms, entries in total), separated by fine lines. A tap opens the history.
 */
@Composable
fun HomeToday(state: HomeState, onClick: () -> Unit) {
    val home = AppColors.extra.home
    val total = state.mealsToday + state.symptomsToday
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = home.todayCard, contentColor = home.onSageTile),
        border = BorderStroke(1.dp, home.cardBorder),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(stringResource(Res.string.today_title), style = MaterialTheme.typography.titleMedium)
            // Top-aligned: at a large font "Einträge gesamt" wraps, the numbers still stay on one line.
            Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min), verticalAlignment = Alignment.Top) {
                Stat({ Icon(Icons.Filled.Restaurant, contentDescription = null, tint = AppColors.extra.home.icon, modifier = it) }, state.mealsToday, pluralStringResource(Res.plurals.today_meals, state.mealsToday), Modifier.weight(1f))
                Separator()
                Stat({ HeartPulseIcon(AppColors.extra.home.icon, it) }, state.symptomsToday, pluralStringResource(Res.plurals.today_symptoms, state.symptomsToday), Modifier.weight(1f))
                Separator()
                Stat({ Icon(Icons.Outlined.Checklist, contentDescription = null, tint = AppColors.extra.home.icon, modifier = it) }, total, pluralStringResource(Res.plurals.today_entries, total), Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun Separator() {
    VerticalDivider(
        color = AppColors.extra.home.onSageTile.copy(alpha = 0.15f),
        modifier = Modifier.fillMaxHeight().width(1.dp).padding(vertical = 4.dp),
    )
}

@Composable
private fun Stat(icon: @Composable (Modifier) -> Unit, count: Int, label: String, modifier: Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            icon(Modifier.size(26.dp))
            Text(
                count.toString(),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 26.sp),
                color = AppColors.extra.home.ink,
            )
        }
        Text(
            label,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 15.sp, letterSpacing = 0.sp),
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,

        )
    }
}
