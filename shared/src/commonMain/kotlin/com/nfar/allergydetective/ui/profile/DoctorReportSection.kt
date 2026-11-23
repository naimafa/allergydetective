package com.nfar.allergydetective.ui.profile

import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.report_content_all
import allergydetective.shared.generated.resources.report_content_analysis
import allergydetective.shared.generated.resources.report_create
import allergydetective.shared.generated.resources.report_format_excel
import allergydetective.shared.generated.resources.report_format_pdf
import allergydetective.shared.generated.resources.report_hint
import allergydetective.shared.generated.resources.report_period_days
import allergydetective.shared.generated.resources.report_section_title
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nfar.allergydetective.platform.rememberSharePdf
import com.nfar.allergydetective.platform.rememberShareSpreadsheet
import com.nfar.allergydetective.ui.common.ChoiceRow
import com.nfar.allergydetective.ui.common.ChoicePill
import com.nfar.allergydetective.ui.common.SectionTitle
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Profile: report for the doctor via the share sheet (feedback 20.11.). User note 21.11.2026: choose the
 * content (analysis only, or with all entries) and the format (PDF or Excel) – this replaces the JSON export.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DoctorReportSection(viewModel: DoctorReportViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val sharePdf = rememberSharePdf()
    val shareExcel = rememberShareSpreadsheet()
    val title = stringResource(Res.string.report_section_title)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle(title)
        Text(stringResource(Res.string.report_hint), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        // User note 22.11.2026: "14 · 30 · 90 Tage" in one line; the same pills for content and format.
        val periods = DoctorReportViewModel.PERIODS
        ChoiceRow(
            periods.map { stringResource(Res.string.report_period_days, it) },
            periods.indexOf(state.days),
            { viewModel.onDaysChange(periods[it]) },
        )
        // The two contents have long names: each pill takes the full width.
        ChoicePill(stringResource(Res.string.report_content_analysis), !state.includeEntries, { viewModel.onIncludeEntriesChange(false) }, Modifier.fillMaxWidth())
        ChoicePill(stringResource(Res.string.report_content_all), state.includeEntries, { viewModel.onIncludeEntriesChange(true) }, Modifier.fillMaxWidth())
        ChoiceRow(
            listOf(stringResource(Res.string.report_format_pdf), stringResource(Res.string.report_format_excel)),
            if (state.format == ReportFormat.PDF) 0 else 1,
            { viewModel.onFormatChange(if (it == 0) ReportFormat.PDF else ReportFormat.EXCEL) },
        )
        Button(
            onClick = {
                viewModel.create(
                    onPdf = { fileName, doc -> sharePdf(title, fileName, doc) },
                    onExcel = { fileName, sheets -> shareExcel(title, fileName, sheets) },
                )
            },
            enabled = !state.busy,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
        ) { Text(stringResource(Res.string.report_create)) }
    }
}
