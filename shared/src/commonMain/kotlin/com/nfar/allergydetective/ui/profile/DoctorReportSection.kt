package com.nfar.allergydetective.ui.profile

import androidx.compose.material.icons.outlined.Description
import androidx.compose.ui.semantics.Role
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.RadioButton
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.layout.padding
import allergydetective.shared.generated.resources.report_entry_count
import allergydetective.shared.generated.resources.report_period_custom
import allergydetective.shared.generated.resources.report_hint_short
import allergydetective.shared.generated.resources.common_cancel
import org.jetbrains.compose.resources.pluralStringResource
import kotlinx.datetime.LocalDate
import com.nfar.allergydetective.ui.theme.AppColors
import androidx.compose.ui.Alignment
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Icon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.BorderStroke
import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.report_content_all
import allergydetective.shared.generated.resources.report_content_analysis
import allergydetective.shared.generated.resources.report_create
import allergydetective.shared.generated.resources.report_format_excel
import allergydetective.shared.generated.resources.report_format_pdf
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
 * "Bericht & Export" (user pictures 23.11.2026, second round: "etwas überladen"): period 14 / 30 days or an own
 * period, content as two small radio rows, one quiet summary line, PDF or Excel, then "Erstellen und teilen".
 * The own period is chosen in a calendar that only shows while choosing; afterwards the pill shows the dates.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorReportSection(viewModel: DoctorReportViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val sharePdf = rememberSharePdf()
    val shareExcel = rememberShareSpreadsheet()
    val title = stringResource(Res.string.report_section_title)
    var picking by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionTitle(title, Icons.Outlined.Description)
        Text(stringResource(Res.string.report_hint_short), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        val periods = DoctorReportViewModel.PERIODS
        val customLabel = if (state.days == null) shortRange(state.customFrom, state.customTo) else stringResource(Res.string.report_period_custom)
        ChoiceRow(
            periods.map { stringResource(Res.string.report_period_days, it) } + customLabel,
            state.days?.let { periods.indexOf(it) } ?: periods.size,
            { if (it < periods.size) viewModel.onDaysChange(periods[it]) else picking = true },
        )
        // User 23.11.2026: the count right under the period it belongs to.
        Text(
            state.entryCount?.let { pluralStringResource(Res.plurals.report_entry_count, it, it) } ?: "…",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp),
        )
        HorizontalDivider(color = AppColors.extra.home.cardBorder)
        // Two small radio rows instead of two full-width pills, close together (user 23.11.2026).
        Column {
            ContentOption(stringResource(Res.string.report_content_analysis), !state.includeEntries) { viewModel.onIncludeEntriesChange(false) }
            ContentOption(stringResource(Res.string.report_content_all), state.includeEntries) { viewModel.onIncludeEntriesChange(true) }
        }
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
            shape = CircleShape,
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
        ) {
            Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(20.dp))
            Text(stringResource(Res.string.report_create), modifier = Modifier.padding(start = 10.dp))
        }
    }
    if (picking) {
        PeriodDialog(
            from = state.customFrom,
            to = state.customTo,
            onPicked = { from, to ->
                picking = false
                viewModel.onCustomPeriod(from, to)
            },
            onDismiss = { picking = false },
        )
    }
}

@Composable
private fun ContentOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().heightIn(min = 40.dp).selectable(selected = selected, onClick = onClick, role = Role.RadioButton),
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 12.dp))
    }
}

/** "01.05.–31.05." for the pill of the own period. */
private fun shortRange(from: Long, to: Long): String {
    fun d(day: Long) = LocalDate.fromEpochDays(day).let { "${it.day.toString().padStart(2, '0')}.${(it.month.ordinal + 1).toString().padStart(2, '0')}." }
    // User 23.11.2026: within one month shorter ("10.–23.11."), so the pill keeps some room at its edges.
    val a = LocalDate.fromEpochDays(from)
    val b = LocalDate.fromEpochDays(to)
    if (a.year == b.year && a.month == b.month) return "${a.day.toString().padStart(2, '0')}.–${d(to)}"
    return "${d(from)}–${d(to)}"
}

/** Start and end day in one calendar; no day after today. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PeriodDialog(from: Long, to: Long, onPicked: (Long, Long) -> Unit, onDismiss: () -> Unit) {
    val today = remember { kotlin.time.Clock.System.now().toEpochMilliseconds() / DAY_MILLIS }
    val picker = rememberDateRangePickerState(
        initialSelectedStartDateMillis = from * DAY_MILLIS,
        initialSelectedEndDateMillis = to * DAY_MILLIS,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis / DAY_MILLIS <= today
        },
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                enabled = picker.selectedStartDateMillis != null,
                onClick = {
                    val start = picker.selectedStartDateMillis ?: return@Button
                    val end = picker.selectedEndDateMillis ?: start
                    onPicked(start / DAY_MILLIS, end / DAY_MILLIS)
                },
            ) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.common_cancel)) } },
    ) {
        DateRangePicker(state = picker, showModeToggle = false, modifier = Modifier.heightIn(max = 520.dp))
    }
}

private const val DAY_MILLIS = 86_400_000L
