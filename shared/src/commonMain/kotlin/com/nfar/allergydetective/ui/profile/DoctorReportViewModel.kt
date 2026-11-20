package com.nfar.allergydetective.ui.profile

import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.report_col_avg
import allergydetective.shared.generated.resources.report_col_candidate
import allergydetective.shared.generated.resources.report_col_count
import allergydetective.shared.generated.resources.report_col_date
import allergydetective.shared.generated.resources.report_col_level
import allergydetective.shared.generated.resources.report_col_max
import allergydetective.shared.generated.resources.report_col_note
import allergydetective.shared.generated.resources.report_col_reason
import allergydetective.shared.generated.resources.report_col_symptom
import allergydetective.shared.generated.resources.report_daytime_afternoon
import allergydetective.shared.generated.resources.report_daytime_evening
import allergydetective.shared.generated.resources.report_daytime_midday
import allergydetective.shared.generated.resources.report_daytime_morning
import allergydetective.shared.generated.resources.report_daytime_night
import allergydetective.shared.generated.resources.report_disclaimer
import allergydetective.shared.generated.resources.report_footer
import allergydetective.shared.generated.resources.report_h_course
import allergydetective.shared.generated.resources.report_h_daytime
import allergydetective.shared.generated.resources.report_h_duration
import allergydetective.shared.generated.resources.report_h_frequency
import allergydetective.shared.generated.resources.report_h_notes
import allergydetective.shared.generated.resources.report_h_patterns
import allergydetective.shared.generated.resources.report_level_strong
import allergydetective.shared.generated.resources.report_level_suspicion
import allergydetective.shared.generated.resources.report_level_trace
import allergydetective.shared.generated.resources.report_no_patterns
import allergydetective.shared.generated.resources.report_none
import allergydetective.shared.generated.resources.report_patterns_hint
import allergydetective.shared.generated.resources.report_period
import allergydetective.shared.generated.resources.report_summary
import allergydetective.shared.generated.resources.report_title
import allergydetective.shared.generated.resources.report_week
import allergydetective.shared.generated.resources.report_week_value
import allergydetective.shared.generated.resources.results_bad_sleep
import allergydetective.shared.generated.resources.results_high_stress
import allergydetective.shared.generated.resources.results_reason
import allergydetective.shared.generated.resources.symptom_duration_h1_to_3
import allergydetective.shared.generated.resources.symptom_duration_h3_to_12
import allergydetective.shared.generated.resources.symptom_duration_over_12h
import allergydetective.shared.generated.resources.symptom_duration_under_1h
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfar.allergydetective.domain.analysis.AnalysisEngine
import com.nfar.allergydetective.domain.analysis.AnalysisRepository
import com.nfar.allergydetective.domain.analysis.CandidateCatalog
import com.nfar.allergydetective.domain.analysis.CandidateKeys
import com.nfar.allergydetective.domain.analysis.model.AnalysisResult
import com.nfar.allergydetective.domain.analysis.model.Candidate
import com.nfar.allergydetective.domain.analysis.model.EvidenceLevel
import com.nfar.allergydetective.domain.diary.HistoryRepository
import com.nfar.allergydetective.domain.diary.LanguageProvider
import com.nfar.allergydetective.domain.diary.SymptomDuration
import com.nfar.allergydetective.domain.report.DoctorReport
import com.nfar.allergydetective.domain.report.ReportDayTime
import com.nfar.allergydetective.domain.report.buildDoctorReport
import com.nfar.allergydetective.domain.time.AppClock
import com.nfar.allergydetective.platform.BarRow
import com.nfar.allergydetective.platform.PdfBlock
import com.nfar.allergydetective.platform.PdfDoc
import com.nfar.allergydetective.ui.common.DisplayNameResolver
import com.nfar.allergydetective.ui.common.NamePrefix
import com.nfar.allergydetective.ui.common.formatDate
import com.nfar.allergydetective.ui.common.formatDecimal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.getString
import kotlin.coroutines.CoroutineContext

data class DoctorReportState(
    val days: Int = DoctorReportViewModel.PERIODS.first(),
    val busy: Boolean = false,
)

/**
 * Report for the doctor (feedback 20.11.): the diary of the chosen period as a PDF.
 * The patterns come from the analysis of the whole diary, like the results screen.
 */
class DoctorReportViewModel(
    private val history: HistoryRepository,
    private val analysis: AnalysisRepository,
    private val engine: AnalysisEngine,
    private val catalog: CandidateCatalog,
    private val names: DisplayNameResolver,
    private val language: LanguageProvider,
    private val clock: AppClock,
    private val computeContext: CoroutineContext,
) : ViewModel() {

    private val _state = MutableStateFlow(DoctorReportState())
    val state: StateFlow<DoctorReportState> = _state.asStateFlow()

    fun onDaysChange(days: Int) {
        _state.update { it.copy(days = days) }
    }

    /** [onReady] gets the file name and the document; the platform renders and shares it. */
    fun create(onReady: (fileName: String, doc: PdfDoc) -> Unit) {
        if (_state.value.busy) return
        _state.update { it.copy(busy = true) }
        viewModelScope.launch {
            try {
                val today = clock.today()
                val fromDay = today - _state.value.days + 1
                val entries = history.entriesSince(clock.millisAt(fromDay, 0, 0))
                val report = buildDoctorReport(entries, fromDay, today, clock)
                val now = clock.nowMillis()
                val input = analysis.loadInput(now, clock.offsetMinAt(now))
                val result = withContext(computeContext) { engine.analyze(input) }
                val doc = document(report, result, catalog.keys())
                onReady("symptom-report-${clock.localDate(today)}.pdf", doc)
            } finally {
                _state.update { it.copy(busy = false) }
            }
        }
    }

    private fun date(epochDay: Long) = formatDate(clock.localDate(epochDay), language.language())

    private fun decimal(value: Double) = formatDecimal(value.toFloat(), language.language())

    private suspend fun symptomName(key: String) = names.name(NamePrefix.SYMPTOM, key)

    private suspend fun document(report: DoctorReport, result: AnalysisResult, keys: CandidateKeys): PdfDoc {
        val blocks = mutableListOf<PdfBlock>()
        blocks += PdfBlock.Title(getString(Res.string.report_title))
        blocks += PdfBlock.Paragraph(getString(Res.string.report_period, date(report.fromDay), date(report.toDay), report.days))
        blocks += PdfBlock.Paragraph(
            getString(Res.string.report_summary, report.documentedDays, report.mealCount, report.drinkCount, report.symptomCount),
        )
        blocks += PdfBlock.Paragraph(getString(Res.string.report_disclaimer), small = true)

        if (report.symptomCount == 0) {
            blocks += PdfBlock.Paragraph(getString(Res.string.report_none))
        } else {
            blocks += symptomBlocks(report)
        }
        blocks += patternBlocks(result, keys)
        return PdfDoc(blocks, getString(Res.string.report_footer, date(clock.today())))
    }

    private suspend fun symptomBlocks(report: DoctorReport): List<PdfBlock> {
        val blocks = mutableListOf<PdfBlock>()
        blocks += PdfBlock.Heading(getString(Res.string.report_h_frequency))
        blocks += PdfBlock.Table(
            rows = listOf(
                listOf(
                    getString(Res.string.report_col_symptom),
                    getString(Res.string.report_col_count),
                    getString(Res.string.report_col_avg),
                    getString(Res.string.report_col_max),
                ),
            ) + report.symptoms.map { listOf(symptomName(it.symptomKey), it.count.toString(), decimal(it.averageIntensity), it.maxIntensity.toString()) },
            weights = listOf(3f, 1f, 1.4f, 1f),
        )

        blocks += PdfBlock.Heading(getString(Res.string.report_h_course))
        blocks += PdfBlock.Bars(
            rows = report.weeks.map { week ->
                BarRow(
                    label = getString(Res.string.report_week, date(week.weekStart)),
                    value = week.averageIntensity,
                    valueText = getString(Res.string.report_week_value, decimal(week.averageIntensity), week.count),
                )
            },
            max = MAX_INTENSITY,
        )

        blocks += PdfBlock.Heading(getString(Res.string.report_h_daytime))
        val maxCount = report.dayTimes.values.maxOrNull()?.toDouble() ?: 0.0
        blocks += PdfBlock.Bars(
            rows = ReportDayTime.entries.map { part ->
                val count = report.dayTimes[part] ?: 0
                BarRow(dayTimeLabel(part), count.toDouble(), count.toString())
            },
            max = maxCount,
        )

        if (report.durations.isNotEmpty()) {
            blocks += PdfBlock.Heading(getString(Res.string.report_h_duration))
            val maxDuration = report.durations.values.max().toDouble()
            blocks += PdfBlock.Bars(
                rows = SymptomDuration.entries.map { duration ->
                    val count = report.durations[duration] ?: 0
                    BarRow(durationLabel(duration), count.toDouble(), count.toString())
                },
                max = maxDuration,
            )
        }

        if (report.notes.isNotEmpty()) {
            blocks += PdfBlock.Heading(getString(Res.string.report_h_notes))
            blocks += PdfBlock.Table(
                rows = listOf(
                    listOf(getString(Res.string.report_col_date), getString(Res.string.report_col_symptom), getString(Res.string.report_col_note)),
                ) + report.notes.map { note ->
                    val (hour, minute) = clock.localTime(note.time)
                    val time = "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
                    listOf("${date(clock.epochDayOf(note.time))} $time", "${symptomName(note.symptomKey)} (${note.intensity})", note.note)
                },
                weights = listOf(1.3f, 1.5f, 3.2f),
            )
        }
        return blocks
    }

    private suspend fun patternBlocks(result: AnalysisResult, keys: CandidateKeys): List<PdfBlock> {
        val blocks = mutableListOf<PdfBlock>(PdfBlock.Heading(getString(Res.string.report_h_patterns)))
        val rows = result.ranking
            .filter { it.level >= EvidenceLevel.TRACE }
            .mapNotNull { r ->
                val name = candidateName(r.candidate, keys) ?: return@mapNotNull null
                val stats = r.stats
                listOf(
                    name,
                    levelLabel(r.level),
                    getString(Res.string.results_reason, stats.hitCount, stats.exposureCount, stats.unexposedHitCount, stats.unexposedCount),
                )
            }
        if (rows.isEmpty()) {
            blocks += PdfBlock.Paragraph(getString(Res.string.report_no_patterns))
        } else {
            blocks += PdfBlock.Paragraph(getString(Res.string.report_patterns_hint), small = true)
            blocks += PdfBlock.Table(
                rows = listOf(
                    listOf(getString(Res.string.report_col_candidate), getString(Res.string.report_col_level), getString(Res.string.report_col_reason)),
                ) + rows,
                weights = listOf(2f, 1.4f, 3f),
            )
        }
        return blocks
    }

    private suspend fun candidateName(candidate: Candidate, keys: CandidateKeys): String? = when (candidate) {
        is Candidate.Ingredient -> keys.ingredients[candidate.id]?.let { it.key?.let { key -> names.name(NamePrefix.INGREDIENT, key) } ?: it.customName }
        is Candidate.Property -> keys.properties[candidate.id]?.let { names.name(NamePrefix.PROPERTY, it) }
        is Candidate.DayFactor -> keys.factors[candidate.id]?.let { names.name(NamePrefix.FACTOR, it) }
        Candidate.BadSleep -> getString(Res.string.results_bad_sleep)
        Candidate.HighStress -> getString(Res.string.results_high_stress)
    }

    private suspend fun levelLabel(level: EvidenceLevel): String = getString(
        when (level) {
            EvidenceLevel.STRONG_SUSPICION -> Res.string.report_level_strong
            EvidenceLevel.SUSPICION -> Res.string.report_level_suspicion
            else -> Res.string.report_level_trace
        },
    )

    private suspend fun dayTimeLabel(part: ReportDayTime): String = getString(
        when (part) {
            ReportDayTime.NIGHT -> Res.string.report_daytime_night
            ReportDayTime.MORNING -> Res.string.report_daytime_morning
            ReportDayTime.MIDDAY -> Res.string.report_daytime_midday
            ReportDayTime.AFTERNOON -> Res.string.report_daytime_afternoon
            ReportDayTime.EVENING -> Res.string.report_daytime_evening
        },
    )

    private suspend fun durationLabel(duration: SymptomDuration): String = getString(
        when (duration) {
            SymptomDuration.UNDER_1H -> Res.string.symptom_duration_under_1h
            SymptomDuration.H1_TO_3 -> Res.string.symptom_duration_h1_to_3
            SymptomDuration.H3_TO_12 -> Res.string.symptom_duration_h3_to_12
            SymptomDuration.OVER_12H -> Res.string.symptom_duration_over_12h
        },
    )

    companion object {
        /** Periods to choose from, in days. */
        val PERIODS = listOf(14, 30, 90)
        private const val MAX_INTENSITY = 5.0
    }
}
