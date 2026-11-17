package com.nfar.allergydetective.ui.home

import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.home_progress_collecting
import allergydetective.shared.generated.resources.home_progress_ready
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/** "Day 4 of 7 – collecting clues" (UX 2). Only confirmed days count. */
@Composable
fun HomeProgress(confirmedDays: Int) {
    val total = HomeViewModel.COLLECTION_DAYS
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            if (confirmedDays >= total) pluralStringResource(Res.plurals.home_progress_ready, confirmedDays, confirmedDays)
            else stringResource(Res.string.home_progress_collecting, confirmedDays, total),
            style = MaterialTheme.typography.bodyLarge,
        )
        LinearProgressIndicator(
            progress = { (confirmedDays.toFloat() / total).coerceAtMost(1f) },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        )
    }
}
