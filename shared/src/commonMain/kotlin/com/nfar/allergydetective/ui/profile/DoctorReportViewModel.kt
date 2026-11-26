package com.nfar.allergydetective.ui.profile

import allergydetective.shared.generated.resources.illness_unknown
import allergydetective.shared.generated.resources.illness_unrelated
import allergydetective.shared.generated.resources.illness_related
import allergydetective.shared.generated.resources.meal_slot_drink
import allergydetective.shared.generated.resources.meal_slot_snack
import allergydetective.shared.generated.resources.meal_slot_dinner
import allergydetective.shared.generated.resources.meal_slot_lunch
import allergydetective.shared.generated.resources.meal_slot_breakfast
import allergydetective.shared.generated.resources.meal_portion_large
import allergydetective.shared.generated.resources.meal_portion_medium
import allergydetective.shared.generated.resources.meal_portion_small
import allergydetective.shared.generated.resources.report_status_complete
import allergydetective.shared.generated.resources.report_status_no_symptoms
import allergydetective.shared.generated.resources.report_status_open
import allergydetective.shared.generated.resources.report_h_days
import allergydetective.shared.generated.resources.report_h_complaints
import allergydetective.shared.generated.resources.report_h_meals
import allergydetective.shared.generated.resources.report_col_factors
import allergydetective.shared.generated.resources.report_col_stress
import allergydetective.shared.generated.resources.report_col_sleep_quality
import allergydetective.shared.generated.resources.report_col_sleep
import allergydetective.shared.generated.resources.report_col_status
import allergydetective.shared.generated.resources.report_col_illness
import allergydetective.shared.generated.resources.report_col_duration
import allergydetective.shared.generated.resources.report_col_intensity
import allergydetective.shared.generated.resources.report_col_ingredients
import allergydetective.shared.generated.resources.report_col_dish
import allergydetective.shared.generated.resources.report_col_meal
import allergydetective.shared.generated.resources.report_col_time
import allergydetective.shared.generated.resources.report_sheet_analysis
import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.report_col_avg
import allergydetective.shared.generated.resources.report_col_candidate
import allergydetective.shared.generated.resources.report_col_count
import allergydetective.shared.generated.resources.report_col_date
import allergydetective.shared.generated.resources.report_h_diary
import allergydetective.shared.generated.resources.report_diary_type
import allergydetective.shared.generated.resources.report_diary_entry
import allergydetective.shared.generated.resources.report_diary_details
import allergydetective.shared.generated.resources.report_diary_meal
import allergydetective.shared.generated.resources.report_diary_symptom
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfar.allergydetective.platform.Sheet
import com.nfar.allergydetective.domain.time.MealSlot
import com.nfar.allergydetective.domain.analysis.model.IllnessRelation
import com.nfar.allergydetective.domain.analysis.model.Portion
import com.nfar.allergydetective.domain.diary.HistoryEntry
import com.nfar.allergydetective.domain.diary.DayCheckStatus
import com.nfar.allergydetective.domain.diary.DayCheckRepository
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
import com.nfar.allergydetective.ui.symptom.durationLabelRes
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
    /** 14 or 30 days back from today; null = the own period [customFrom]–[customTo] (user picture 23.11.2026). */
    val days: Int? = DoctorReportViewModel.PERIODS.first(),
    /** Local epoch days of the own period, both included. */
    val customFrom: Long = 0,
    val customTo: Long = 0,
    /** Meals and complaints in the chosen period: "48 Einträge im Zeitraum"; null while counting. */
    val entryCount: Int? = null,
    val busy: Boolean = false,
    /** User note 21.11.2026: only the analysis, or together with all meals, complaints and day checks. */
    val includeEntries: Boolean = false,
    val format: ReportFormat = ReportFormat.PDF,
)

/** PDF for reading and printing, Excel for working with the entries (instead of the developer JSON). */
enum class ReportFormat { PDF, EXCEL }

/**
 * Report for the doctor (feedback 20.11.): the diary of the chosen period as a PDF.
 * The patterns come from the analysis of the whole diary, like the results screen.
 */
class DoctorReportViewModel(
    private val history: HistoryRepository,
    private val dayChecks: DayCheckRepository,
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

    init {
        val today = clock.today()
        _state.update { it.copy(customFrom = today - PERIODS.first() + 1, customTo = today) }
        countEntries()
    }

    fun onDaysChange(days: Int) {
        _state.update { it.copy(days = days) }
        countEntries()
    }

    /** Own period from the calendar (user 23.11.2026); the days are put in order and end at today at the latest. */
    fun onCustomPeriod(from: Long, to: Long) {
        val today = clock.today()
        val start = minOf(from, to).coerceAtMost(today)
        val end = maxOf(from, to).coerceAtMost(today)
        _state.update { it.copy(days = null, customFrom = start, customTo = end) }
        countEntries()
    }

    /** First and last local epoch day of the chosen period. */
    private fun range(options: DoctorReportState): Pair<Long, Long> {
        val today = clock.today()
        return options.days?.let { today - it + 1 to today } ?: (options.customFrom to options.customTo)
    }

    /** Meals and complaints from [fromDay] to [toDay], both days included. */
    private suspend fun entriesIn(fromDay: Long, toDay: Long): List<HistoryEntry> {
        val end = clock.millisAt(toDay + 1, 0, 0)
        return history.entriesSince(clock.millisAt(fromDay, 0, 0)).filter { it.time < end }
    }

    private fun countEntries() {
        val (fromDay, toDay) = range(_state.value)
        _state.update { it.copy(entryCount = null) }
        viewModelScope.launch {
            val count = entriesIn(fromDay, toDay).size
            _state.update { if (range(it) == fromDay to toDay) it.copy(entryCount = count) else it }
        }
    }

    fun onIncludeEntriesChange(include: Boolean) {
        _state.update { it.copy(includeEntries = include) }
    }

    fun onFormatChange(format: ReportFormat) {
        _state.update { it.copy(format = format) }
    }

    /**
     * Builds the report in the chosen content and format; the platform renders and shares it.
     * PDF: [onPdf] with the document. Excel: [onExcel] with one sheet for the analysis and, with entries,
     * one each for meals, complaints and day checks.
     */
    fun create(onPdf: (fileName: String, doc: PdfDoc) -> Unit, onExcel: (fileName: String, sheets: List<Sheet>) -> Unit) {
        if (_state.value.busy) return
        val options = _state.value
        _state.update { it.copy(busy = true) }
        viewModelScope.launch {
            try {
                val (fromDay, today) = range(options)
                val entries = entriesIn(fromDay, today)
                val report = buildDoctorReport(entries, fromDay, today, clock)
                val now = clock.nowMillis()
                val input = analysis.loadInput(now, clock.offsetMinAt(now))
                val result = withContext(computeContext) { engine.analyze(input) }
                val doc = document(report, result, catalog.keys())
                val tables = if (options.includeEntries) entryTables(entries, fromDay, today) else emptyList()
                val base = "allergy-detective-${clock.localDate(today)}"
                when (options.format) {
                    ReportFormat.PDF -> {
                        val blocks = doc.blocks + tables.filter { it.inPdf }.flatMap { listOf(PdfBlock.Heading(it.title), PdfBlock.Table(it.rows, it.weights)) }
                        onPdf("$base.pdf", doc.copy(blocks = blocks))
                    }
                    ReportFormat.EXCEL -> {
                        val analysisSheet = Sheet(getString(Res.string.report_sheet_analysis), sheetRows(doc.blocks))
                        onExcel("$base.xlsx", listOf(analysisSheet) + tables.map { Sheet(it.title, it.rows, it.emphasis) })
                    }
                }
            } finally {
                _state.update { it.copy(busy = false) }
            }
        }
    }

    /** One table of entries: title, rows with the header first, column weights for the PDF. */
    private class EntryTable(
        val title: String,
        val rows: List<List<String>>,
        val weights: List<Float>,
        /** Rows shown bold on a tinted band in Excel (the day rows of the diary). */
        val emphasis: Set<Int> = emptySet(),
        /** The diary by day is for Excel; the PDF keeps the three plain tables. */
        val inPdf: Boolean = true,
    )

    /** Meals, complaints and day checks of the period, oldest first (like a diary). */
    private suspend fun entryTables(entries: List<HistoryEntry>, fromDay: Long, today: Long): List<EntryTable> {
        val meals = entries.filterIsInstance<HistoryEntry.MealEntry>().sortedBy { it.time }
        val symptoms = entries.filterIsInstance<HistoryEntry.SymptomEntry>().sortedBy { it.time }
        val mealRows = listOf(
            listOf(
                getString(Res.string.report_col_date), getString(Res.string.report_col_time), getString(Res.string.report_col_meal),
                getString(Res.string.report_col_dish), getString(Res.string.report_col_ingredients),
            ),
        ) + meals.map { meal ->
            val items = meal.items.map { (ingredient, portion) ->
                val name = ingredient.key?.let { names.name(NamePrefix.INGREDIENT, it) } ?: ingredient.customName.orEmpty()
                "$name (${portionLabel(portion)})"
            }
            listOf(
                date(clock.epochDayOf(meal.time)), time(meal.time), meal.slot?.let { slotLabel(it) }.orEmpty(),
                meal.dishName.orEmpty(), items.joinToString(", "),
            )
        }
        val symptomRows = listOf(
            listOf(
                getString(Res.string.report_col_date), getString(Res.string.report_col_time), getString(Res.string.report_col_symptom),
                getString(Res.string.report_col_intensity), getString(Res.string.report_col_duration),
                getString(Res.string.report_col_illness), getString(Res.string.report_col_note),
            ),
        ) + symptoms.map { symptom ->
            listOf(
                date(clock.epochDayOf(symptom.time)), time(symptom.time), symptomName(symptom.symptomKey), symptom.intensity.toString(),
                symptom.duration?.let { durationLabel(it) }.orEmpty(), symptom.illnessRelation?.let { illnessLabel(it) }.orEmpty(),
                symptom.note.orEmpty(),
            )
        }
        val factorNames = dayChecks.lifestyleFactors().associate { it.id to names.name(NamePrefix.FACTOR, it.key) }
        val dayRows = listOf(
            listOf(
                getString(Res.string.report_col_date), getString(Res.string.report_col_status), getString(Res.string.report_col_sleep),
                getString(Res.string.report_col_sleep_quality), getString(Res.string.report_col_stress), getString(Res.string.report_col_factors),
            ),
        ) + (fromDay..today).mapNotNull { day -> dayChecks.load(day) }.map { check ->
            listOf(
                date(check.epochDay), statusLabel(check.status), check.sleepHours?.let { formatDecimal(it, language.language()) }.orEmpty(),
                check.sleepQuality?.toString().orEmpty(), check.stress?.toString().orEmpty(),
                check.factorIds.mapNotNull(factorNames::get).joinToString(", "),
            )
        }
        val diary = diaryTable(meals, symptoms, fromDay, today)
        return listOf(
            diary,
            EntryTable(getString(Res.string.report_h_meals), mealRows, listOf(1.1f, 0.7f, 1f, 1.2f, 3f)),
            EntryTable(getString(Res.string.report_h_complaints), symptomRows, listOf(1.1f, 0.7f, 1.4f, 0.7f, 1f, 1f, 1.6f)),
            EntryTable(getString(Res.string.report_h_days), dayRows, listOf(1.1f, 1.2f, 0.8f, 0.9f, 0.7f, 1.8f)),
        )
    }

    /**
     * User 24.11.2026: in the three plain tables it was not clear which entries belong to one day. This sheet lists every
     * day once as a bold band (date, finished or not, sleep, stress, factors) with its meals and complaints below, by time.
     */
    private suspend fun diaryTable(
        meals: List<HistoryEntry.MealEntry>,
        symptoms: List<HistoryEntry.SymptomEntry>,
        fromDay: Long,
        today: Long,
    ): EntryTable {
        val factorNames = dayChecks.lifestyleFactors().associate { it.id to names.name(NamePrefix.FACTOR, it.key) }
        val mealLabel = getString(Res.string.report_diary_meal)
        val symptomLabel = getString(Res.string.report_diary_symptom)
        val rows = mutableListOf(
            listOf(
                getString(Res.string.report_col_date), getString(Res.string.report_col_time), getString(Res.string.report_diary_type),
                getString(Res.string.report_diary_entry), getString(Res.string.report_diary_details),
            ),
        )
        val emphasis = mutableSetOf<Int>()
        for (day in fromDay..today) {
            val dayMeals = meals.filter { clock.epochDayOf(it.time) == day }
            val daySymptoms = symptoms.filter { clock.epochDayOf(it.time) == day }
            val check = dayChecks.load(day)
            if (dayMeals.isEmpty() && daySymptoms.isEmpty() && check == null) continue
            val summary = buildList {
                check?.let { c ->
                    add(statusLabel(c.status))
                    c.sleepHours?.let { add(getString(Res.string.report_col_sleep) + " " + formatDecimal(it, language.language())) }
                    c.stress?.let { add(getString(Res.string.report_col_stress) + " " + it) }
                    c.factorIds.mapNotNull(factorNames::get).takeIf { it.isNotEmpty() }?.let { add(it.joinToString(", ")) }
                }
            }
            emphasis += rows.size
            rows += listOf(date(day), "", "", summary.joinToString(" · "), "")
            val entries = dayMeals.map { it.time to it as HistoryEntry } + daySymptoms.map { it.time to it as HistoryEntry }
            for ((_, entry) in entries.sortedBy { it.first }) {
                when (entry) {
                    is HistoryEntry.MealEntry -> {
                        val items = entry.items.map { (ingredient, portion) ->
                            val name = ingredient.key?.let { names.name(NamePrefix.INGREDIENT, it) } ?: ingredient.customName.orEmpty()
                            "$name (${portionLabel(portion)})"
                        }
                        val kind = entry.slot?.let { slotLabel(it) }
                        rows += listOf("", time(entry.time), listOfNotNull(mealLabel, kind).joinToString(" · "), entry.dishName.orEmpty(), items.joinToString(", "))
                    }
                    is HistoryEntry.SymptomEntry -> {
                        val details = listOfNotNull(
                            entry.duration?.let { durationLabel(it) },
                            entry.illnessRelation?.let { illnessLabel(it) },
                            entry.note?.takeIf { it.isNotBlank() },
                        ).joinToString(" · ")
                        rows += listOf("", time(entry.time), symptomLabel, symptomName(entry.symptomKey) + " (" + entry.intensity + ")", details)
                    }
                    else -> Unit
                }
            }
        }
        return EntryTable(getString(Res.string.report_h_diary), rows, listOf(1.1f, 0.7f, 1.3f, 2f, 3f), emphasis, inPdf = false)
    }

    /** The PDF blocks as rows of one sheet: titles and texts in column A, tables as they are, a blank row between. */
    private fun sheetRows(blocks: List<PdfBlock>): List<List<String>> = buildList {
        blocks.forEach { block ->
            when (block) {
                is PdfBlock.Title -> add(listOf(block.text))
                is PdfBlock.Heading -> { add(emptyList()); add(listOf(block.text)) }
                is PdfBlock.Paragraph -> add(listOf(block.text))
                is PdfBlock.Table -> addAll(block.rows)
                is PdfBlock.Bars -> block.rows.forEach { add(listOf(it.label, it.valueText)) }
            }
        }
    }

    private fun time(millis: Long): String = clock.localTime(millis).let { (h, m) -> "${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}" }

    private suspend fun portionLabel(portion: Portion): String = getString(
        when (portion) {
            Portion.SMALL -> Res.string.meal_portion_small
            Portion.MEDIUM -> Res.string.meal_portion_medium
            Portion.LARGE -> Res.string.meal_portion_large
        },
    )

    private suspend fun slotLabel(slot: MealSlot): String = getString(
        when (slot) {
            MealSlot.BREAKFAST -> Res.string.meal_slot_breakfast
            MealSlot.LUNCH -> Res.string.meal_slot_lunch
            MealSlot.DINNER -> Res.string.meal_slot_dinner
            MealSlot.SNACK -> Res.string.meal_slot_snack
            MealSlot.DRINK -> Res.string.meal_slot_drink
        },
    )

    private suspend fun illnessLabel(relation: IllnessRelation): String = getString(
        when (relation) {
            IllnessRelation.RELATED -> Res.string.illness_related
            IllnessRelation.UNRELATED -> Res.string.illness_unrelated
            IllnessRelation.UNKNOWN -> Res.string.illness_unknown
        },
    )

    private suspend fun statusLabel(status: DayCheckStatus): String = getString(
        when (status) {
            DayCheckStatus.OPEN -> Res.string.report_status_open
            DayCheckStatus.NO_SYMPTOMS -> Res.string.report_status_no_symptoms
            DayCheckStatus.COMPLETE -> Res.string.report_status_complete
        },
    )

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
                // The old step "3–12 h" only when entries still carry it.
                rows = SymptomDuration.entries.filter { it in SymptomDuration.offered || it in report.durations }.map { duration ->
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

    private suspend fun durationLabel(duration: SymptomDuration): String = getString(durationLabelRes(duration))

    companion object {
        /** Periods to choose from, in days. */
        /** User picture 23.11.2026: 14 and 30 days, everything else as an own period. */
        val PERIODS = listOf(14, 30)
        private const val MAX_INTENSITY = 5.0
    }
}
