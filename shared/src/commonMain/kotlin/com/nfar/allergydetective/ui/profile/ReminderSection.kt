package com.nfar.allergydetective.ui.profile

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsNone
import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.reminder_denied
import allergydetective.shared.generated.resources.reminder_setting
import allergydetective.shared.generated.resources.reminder_setting_hint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import com.nfar.allergydetective.ui.common.AppFilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nfar.allergydetective.platform.rememberNotificationPermissionRequest
import com.nfar.allergydetective.ui.common.SectionTitle
import com.nfar.allergydetective.ui.common.formatClock
import com.nfar.allergydetective.ui.common.ChoiceRow
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Evening reminder in the profile (UX 4): on/off and the time. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReminderSection(viewModel: ReminderViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val requestPermission = rememberNotificationPermissionRequest { granted -> viewModel.onEnabledChange(true, granted) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle(stringResource(Res.string.reminder_setting), Icons.Outlined.NotificationsNone)
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(Res.string.reminder_setting_hint), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Switch(
                checked = state.settings.enabled,
                onCheckedChange = { enabled -> if (enabled) requestPermission() else viewModel.onEnabledChange(false) },
            )
        }
        if (state.permissionDenied) {
            Text(stringResource(Res.string.reminder_denied), color = MaterialTheme.colorScheme.tertiary, style = MaterialTheme.typography.bodySmall)
        }
        if (state.settings.enabled) {
            // User note 22.11.2026: all four times in one line ("22" had wrapped into a line of its own).
            val hours = ReminderViewModel.HOURS
            ChoiceRow(
                hours.map { formatClock(it, 0) },
                hours.indexOfFirst { state.settings.hour == it && state.settings.minute == 0 },
                { viewModel.onTimeChange(hours[it], 0) },
            )
        }
    }
}
