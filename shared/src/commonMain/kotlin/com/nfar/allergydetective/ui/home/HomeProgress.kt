package com.nfar.allergydetective.ui.home

import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.home_progress_badge
import allergydetective.shared.generated.resources.home_progress_ready
import allergydetective.shared.generated.resources.home_progress_remaining
import allergydetective.shared.generated.resources.home_progress_title
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/**
 * "Your progress" card (UX 2, design of 19.11.2026): title, a "Day 3 of 7" badge, a progress bar and
 * how many days are left. Only confirmed days count.
 */
@Composable
fun HomeProgress(confirmedDays: Int) {
    val total = HomeViewModel.COLLECTION_DAYS
    val ready = confirmedDays >= total
    val scheme = MaterialTheme.colorScheme
    Card(
        colors = CardDefaults.cardColors(containerColor = scheme.surfaceContainerLowest),
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(Res.string.home_progress_title),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                if (!ready) {
                    Surface(shape = CircleShape, color = scheme.primaryContainer, contentColor = scheme.primary) {
                        Text(
                            stringResource(Res.string.home_progress_badge, confirmedDays, total),
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        )
                    }
                }
            }
            LinearProgressIndicator(
                progress = { (confirmedDays.toFloat() / total).coerceAtMost(1f) },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = scheme.primary,
                trackColor = scheme.surfaceContainerHigh,
                strokeCap = StrokeCap.Round,
                gapSize = 0.dp,
                drawStopIndicator = {},
            )
            Text(
                if (ready) {
                    pluralStringResource(Res.plurals.home_progress_ready, confirmedDays, confirmedDays)
                } else {
                    pluralStringResource(Res.plurals.home_progress_remaining, total - confirmedDays, total - confirmedDays)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant,
            )
        }
    }
}
