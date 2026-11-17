package com.nfar.allergydetective.ui.home

import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.home_day_check
import allergydetective.shared.generated.resources.home_log_meal
import allergydetective.shared.generated.resources.home_log_symptom
import allergydetective.shared.generated.resources.home_results
import allergydetective.shared.generated.resources.home_title
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

enum class HomeAction(val label: StringResource) {
    LOG_MEAL(Res.string.home_log_meal),
    LOG_SYMPTOM(Res.string.home_log_symptom),
    DAY_CHECK(Res.string.home_day_check),
    RESULTS(Res.string.home_results),
}

/** Dashboard with the quick actions (UX 2). [header] shows the progress once day checks exist. */
@Composable
fun HomeScreen(
    onAction: (HomeAction) -> Unit,
    modifier: Modifier = Modifier,
    header: @Composable () -> Unit = {},
) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(stringResource(Res.string.home_title), style = MaterialTheme.typography.headlineMedium)
        header()
        HomeAction.entries.forEach { action ->
            FilledTonalButton(
                onClick = { onAction(action) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp),
            ) {
                Text(stringResource(action.label), style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
