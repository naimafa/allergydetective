package com.nfar.allergydetective.ui.profile

import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.goal_custom
import allergydetective.shared.generated.resources.goal_custom_days
import allergydetective.shared.generated.resources.goal_days
import allergydetective.shared.generated.resources.goal_focus
import allergydetective.shared.generated.resources.goal_focus_hint
import allergydetective.shared.generated.resources.goal_hint
import allergydetective.shared.generated.resources.goal_title
import allergydetective.shared.generated.resources.goal_unit
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.GpsFixed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nfar.allergydetective.domain.reminder.GoalSettings
import com.nfar.allergydetective.domain.reminder.SettingsStore
import com.nfar.allergydetective.ui.common.ChoiceRow
import com.nfar.allergydetective.ui.theme.AppColors
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

/**
 * "Auswertungs-Intervall" (user 23.11.2026, picture of the profile): after how many confirmed days a stretch is
 * done – 7, 14, 21 or an own number – and an optional focus. Saved right away like the other settings; the start
 * screen counts towards it and offers a new stretch once it is reached.
 */
@Composable
fun GoalSection(store: SettingsStore = koinInject()) {
    var goal by remember { mutableStateOf(store.goal()) }
    val save: (GoalSettings) -> Unit = {
        goal = it
        store.saveGoal(it)
    }
    var custom by remember { mutableStateOf(goal.days !in GoalSettings.PRESETS) }
    var customText by remember { mutableStateOf(if (custom) goal.days.toString() else "") }
    val primary = MaterialTheme.colorScheme.primary
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Eco, contentDescription = null, tint = primary, modifier = Modifier.size(22.dp))
            Text(stringResource(Res.string.goal_title), style = MaterialTheme.typography.titleMedium, color = primary, modifier = Modifier.padding(start = 8.dp))
        }
        Text(stringResource(Res.string.goal_hint), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        val labels = GoalSettings.PRESETS.map { stringResource(Res.string.goal_days, it) } + stringResource(Res.string.goal_custom)
        val selected = if (custom) GoalSettings.PRESETS.size else GoalSettings.PRESETS.indexOf(goal.days)
        ChoiceRow(labels, selected, { index ->
            if (index < GoalSettings.PRESETS.size) {
                custom = false
                save(goal.copy(days = GoalSettings.PRESETS[index]))
            } else {
                custom = true
                if (customText.isBlank()) customText = goal.days.toString()
            }
        })
        if (custom) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(Res.string.goal_custom_days), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                OutlinedTextField(
                    value = customText,
                    onValueChange = { text ->
                        customText = text.filter(Char::isDigit).take(2)
                        customText.toIntOrNull()?.takeIf { it in GoalSettings.MIN_DAYS..GoalSettings.MAX_DAYS }?.let { save(goal.copy(days = it)) }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = MaterialTheme.typography.titleMedium.copy(textAlign = TextAlign.Center),
                    isError = customText.toIntOrNull()?.let { it !in GoalSettings.MIN_DAYS..GoalSettings.MAX_DAYS } ?: true,
                    modifier = Modifier.width(84.dp),
                )
                Text(stringResource(Res.string.goal_unit), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 10.dp))
            }
        }
        HorizontalDivider(color = AppColors.extra.home.cardBorder)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.GpsFixed, contentDescription = null, tint = primary, modifier = Modifier.size(22.dp))
            Text(stringResource(Res.string.goal_focus), style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(start = 8.dp))
        }
        OutlinedTextField(
            value = goal.focus,
            onValueChange = { save(goal.copy(focus = it.take(80))) },
            placeholder = { Text(stringResource(Res.string.goal_focus_hint)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
