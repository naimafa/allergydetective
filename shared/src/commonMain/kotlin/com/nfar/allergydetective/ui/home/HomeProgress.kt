package com.nfar.allergydetective.ui.home

import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.home_progress_collecting
import allergydetective.shared.generated.resources.home_progress_ready
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/** "Day 4 of 7 – collecting clues" with a progress ring (UX 2). Only confirmed days count. */
@Composable
fun HomeProgress(confirmedDays: Int) {
    val total = HomeViewModel.COLLECTION_DAYS
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { (confirmedDays.toFloat() / total).coerceAtMost(1f) },
                    modifier = Modifier.size(64.dp),
                    strokeWidth = 6.dp,
                    trackColor = MaterialTheme.colorScheme.secondaryContainer,
                )
                Text(
                    "${confirmedDays.coerceAtMost(99)}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Text(
                if (confirmedDays >= total) pluralStringResource(Res.plurals.home_progress_ready, confirmedDays, confirmedDays)
                else stringResource(Res.string.home_progress_collecting, confirmedDays, total),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}
