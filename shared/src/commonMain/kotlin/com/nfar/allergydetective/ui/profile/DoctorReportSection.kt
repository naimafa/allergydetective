package com.nfar.allergydetective.ui.profile

import androidx.compose.foundation.layout.padding
import allergydetective.shared.generated.resources.report_entry_count
import allergydetective.shared.generated.resources.report_to
import allergydetective.shared.generated.resources.report_from
import allergydetective.shared.generated.resources.report_period_custom
import allergydetective.shared.generated.resources.report_hint_short
import allergydetective.shared.generated.resources.common_cancel
import org.jetbrains.compose.resources.pluralStringResource
import kotlinx.datetime.LocalDate
import com.nfar.allergydetective.ui.theme.AppColors
import com.nfar.allergydetective.ui.common.formatDate
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.Alignment
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Icon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
 * "Bericht & Export" (user picture 23.11.2026): period 14 / 30 days or an own period with from / to, how many
 * entries it holds, the content, PDF or Excel, then "Erstellen und teilen".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorReportSection(viewModel: DoctorReportViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val sharePdf = rememberSharePdf()
    val shareExcel = rememberShareSpreadsheet()
    val title = stringResource(Res.string.report_section_title)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionTitle(title)
        Text(stringResource(Res.string.report_hint_short), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        val periods = DoctorReportViewModel.PERIODS
        ChoiceRow(
            periods.map { stringResource(Res.string.report_period_days, it) } + stringResource(Res.string.report_period_custom),
            state.days?.let { periods.indexOf(it) } ?: periods.size,
            { if (it < periods.size) viewModel.onDaysChange(periods[it]) else viewModel.onCustomPeriod() },
        )
        if (state.days == null) {
            Surface(shape = MaterialTheme.shapes.medium, color = AppColors.extra.home.page, border = BorderStroke(1.dp, AppColors.extra.home.cardBorder)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DateField(stringResource(Res.string.report_from), state.customFrom, latest = state.customTo, onPick = viewModel::onCustomFromChange)
                    DateField(stringResource(Res.string.report_to), state.customTo, onPick = viewModel::onCustomToChange)
                }
            }
        }
        // How much the period holds, so an empty report is no surprise.
        Surface(shape = MaterialTheme.shapes.medium, color = lerp(AppColors.extra.home.page, AppColors.extra.home.badge, 0.45f), modifier = Modifier.fillMaxWidth()) {
            Text(
                state.entryCount?.let { pluralStringResource(Res.plurals.report_entry_count, it, it) } ?: "…",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            )
        }
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
            shape = CircleShape,
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
        ) {
            Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(20.dp))
            Text(stringResource(Res.string.report_create), modifier = Modifier.padding(start = 10.dp))
        }
    }
}

/** "Von: 01.05.2026" with a calendar icon: a tap opens the calendar; days after today or after [latest] are not offered. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateField(label: String, day: Long, latest: Long? = null, onPick: (Long) -> Unit) {
    var open by remember { mutableStateOf(false) }
    val today = remember { kotlin.time.Clock.System.now().toEpochMilliseconds() / DAY_MILLIS }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.width(56.dp))
        Surface(
            onClick = { open = true },
            shape = CircleShape,
            color = AppColors.extra.home.card,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.weight(1f),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.heightIn(min = 44.dp).padding(horizontal = 14.dp)) {
                Text(formatDate(LocalDate.fromEpochDays(day), Locale.current.language), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                Icon(Icons.Outlined.CalendarMonth, contentDescription = null, modifier = Modifier.size(20.dp))
            }
        }
    }
    if (open) {
        val last = minOf(latest ?: today, today)
        val picker = rememberDatePickerState(
            initialSelectedDateMillis = day * DAY_MILLIS,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis / DAY_MILLIS <= last
            },
        )
        DatePickerDialog(
            onDismissRequest = { open = false },
            confirmButton = {
                Button(onClick = {
                    picker.selectedDateMillis?.let { onPick(it / DAY_MILLIS) }
                    open = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { open = false }) { Text(stringResource(Res.string.common_cancel)) } },
        ) { DatePicker(state = picker, showModeToggle = false) }
    }
}

private const val DAY_MILLIS = 86_400_000L
