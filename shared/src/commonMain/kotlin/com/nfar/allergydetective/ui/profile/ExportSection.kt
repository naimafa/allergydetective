package com.nfar.allergydetective.ui.profile

import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.export_button
import allergydetective.shared.generated.resources.export_hint
import allergydetective.shared.generated.resources.export_title
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nfar.allergydetective.platform.rememberShareText
import com.nfar.allergydetective.ui.common.SectionTitle
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Self-test export (ROADMAP Phase 4): diary + analysis numbers as JSON via the share sheet. */
@Composable
fun ExportSection(viewModel: ExportViewModel = koinViewModel()) {
    val busy by viewModel.busy.collectAsStateWithLifecycle()
    val share = rememberShareText()
    val title = stringResource(Res.string.export_title)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle(title)
        Text(stringResource(Res.string.export_hint), style = MaterialTheme.typography.bodyMedium)
        OutlinedButton(
            onClick = { viewModel.export { json -> share(title, json) } },
            enabled = !busy,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
        ) { Text(stringResource(Res.string.export_button)) }
    }
}
