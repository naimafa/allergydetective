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
import com.nfar.allergydetective.ui.theme.AppColors
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
    val home = AppColors.extra.home
    Card(
        colors = CardDefaults.cardColors(containerColor = home.progressCard, contentColor = home.ink),
        // Home design of 21.11.2026: white card with a soft shadow, thicker bar.
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth(),
    ) {
        // Design of 19.11.: compact; title row, bar, then the two short lines close together.
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(Res.string.home_progress_title),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                if (!ready) {
                    Surface(shape = CircleShape, color = home.badge, contentColor = home.ink) {
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
                modifier = Modifier.fillMaxWidth().height(10.dp),
                color = home.ink,
                trackColor = home.progressTrack,
                strokeCap = StrokeCap.Round,
                drawStopIndicator = {},
            )
            // Compact design of 19.11.: one short line below the bar.
            Text(
                if (ready) {
                    pluralStringResource(Res.plurals.home_progress_ready, confirmedDays, confirmedDays)
                } else {
                    pluralStringResource(Res.plurals.home_progress_remaining, total - confirmedDays, total - confirmedDays)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = home.inkSoft,
            )
        }
    }
}
