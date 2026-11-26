package com.nfar.allergydetective.ui.results

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfar.allergydetective.domain.analysis.AnalysisRepository
import com.nfar.allergydetective.domain.analysis.localEpochDay
import com.nfar.allergydetective.domain.analysis.model.DiaryData
import com.nfar.allergydetective.domain.diary.IngredientInfo
import com.nfar.allergydetective.domain.diary.LanguageProvider
import com.nfar.allergydetective.domain.diary.MealRepository
import com.nfar.allergydetective.domain.experiment.Experiment
import com.nfar.allergydetective.domain.experiment.ExperimentResult
import com.nfar.allergydetective.domain.experiment.evaluateExperiment
import com.nfar.allergydetective.domain.reminder.SettingsStore
import com.nfar.allergydetective.domain.time.AppClock
import com.nfar.allergydetective.ui.common.DisplayNameResolver
import com.nfar.allergydetective.ui.meal.IngredientCatalog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Choosing a new Weglass-Test: what, how long, since when. */
data class ExperimentSetup(
    val query: String = "",
    val results: List<IngredientInfo> = emptyList(),
    val chosen: IngredientInfo? = null,
    val days: Int = Experiment.DEFAULT_DAYS,
    /** Days ago the food was left out first: 0 = from today, up to [MAX_START_DAYS_BACK] (user 25.11.2026). */
    val sinceDaysAgo: Int = 0,
    /** Finished days before that start: the comparison needs at least 5. */
    val beforeDays: Int = 0,
)

data class ExperimentState(
    val loading: Boolean = true,
    val experiment: Experiment? = null,
    val ingredient: IngredientInfo? = null,
    val result: ExperimentResult? = null,
    /** What the user ate on most days lately: the usual candidates to leave out. */
    val suggestions: List<IngredientInfo> = emptyList(),
    val setup: ExperimentSetup? = null,
    val today: Long = 0,
)

/**
 * Weglass-Test (user 25.11.2026): leave one ingredient out, keep logging, then compare the days before with the days
 * without it. Starting one also sets the focus and the stretch on the start screen.
 */
class ExperimentViewModel(
    private val analysis: AnalysisRepository,
    private val meals: MealRepository,
    private val names: DisplayNameResolver,
    private val language: LanguageProvider,
    private val settings: SettingsStore,
    private val clock: AppClock,
) : ViewModel() {

    private val _state = MutableStateFlow(ExperimentState())
    val state: StateFlow<ExperimentState> = _state.asStateFlow()

    private var catalog = IngredientCatalog.EMPTY
    private var diary: DiaryData? = null

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            catalog = IngredientCatalog.load(meals, names, language)
            val now = clock.nowMillis()
            val diary = analysis.loadInput(now, clock.offsetMinAt(now)).diary
            this@ExperimentViewModel.diary = diary
            val today = clock.today()
            val experiment = settings.experiment()
            // Most eaten in the last two weeks, counted per day.
            val suggestions = diary.meals
                .filter { localEpochDay(it.eatenAt, it.tzOffsetMin) > today - SUGGESTION_DAYS }
                .flatMap { meal -> meal.items.map { it.ingredientId to localEpochDay(meal.eatenAt, meal.tzOffsetMin) } }
                .distinct()
                .groupingBy { it.first }.eachCount()
                .entries.sortedByDescending { it.value }
                .mapNotNull { catalog.byId[it.key] }
                .take(MAX_SUGGESTIONS)
            _state.update {
                it.copy(
                    loading = false,
                    experiment = experiment,
                    ingredient = experiment?.let { e -> catalog.byId[e.ingredientId] },
                    result = experiment?.let { e -> evaluateExperiment(e, diary, today) },
                    suggestions = suggestions,
                    today = today,
                )
            }
        }
    }

    fun onOpenSetup() = _state.update { it.copy(setup = withBeforeDays(ExperimentSetup())) }

    fun onCancelSetup() = _state.update { it.copy(setup = null) }

    fun onQuery(query: String) = updateSetup { it.copy(query = query, results = catalog.search(query)) }

    fun onChoose(ingredient: IngredientInfo) = updateSetup { it.copy(chosen = ingredient, query = "", results = emptyList()) }

    fun onDays(days: Int) = updateSetup { withBeforeDays(it.copy(days = days)) }

    fun onSince(daysAgo: Int) = updateSetup { withBeforeDays(it.copy(sinceDaysAgo = daysAgo.coerceIn(0, MAX_START_DAYS_BACK))) }

    /** The same days the test compares with: finished, not sick, before the start. */
    private fun withBeforeDays(setup: ExperimentSetup): ExperimentSetup {
        val start = clock.today() - setup.sinceDaysAgo
        val count = diary?.dayChecks.orEmpty().count { it.confirmed && !it.sick && it.epochDay in (start - setup.days) until start }
        return setup.copy(beforeDays = count)
    }

    /** [focus] is the text for the start screen, e.g. "Ohne Hartkäse" (resolved in the UI's language). */
    fun onBegin(focus: String) {
        val setup = _state.value.setup ?: return
        val chosen = setup.chosen ?: return
        val experiment = Experiment(chosen.id, startDay = clock.today() - setup.sinceDaysAgo, days = setup.days)
        settings.saveExperiment(experiment)
        settings.saveGoal(settings.goal().copy(days = experiment.days, focus = focus, startDay = experiment.startDay))
        _state.update { it.copy(setup = null) }
        refresh()
    }

    fun onStop() {
        val experiment = _state.value.experiment ?: return
        settings.saveExperiment(experiment.copy(stoppedDay = clock.today()))
        refresh()
    }

    /** After a finished test: clear it and choose the next one. */
    fun onNew() {
        settings.saveExperiment(null)
        _state.update { it.copy(experiment = null, ingredient = null, result = null, setup = withBeforeDays(ExperimentSetup())) }
    }

    private fun updateSetup(change: (ExperimentSetup) -> ExperimentSetup) =
        _state.update { state -> state.setup?.let { state.copy(setup = change(it)) } ?: state }

    companion object {
        /** An earlier start leaves too few days before it to compare with. */
        const val MAX_START_DAYS_BACK = 14
        private const val SUGGESTION_DAYS = 14
        private const val MAX_SUGGESTIONS = 5
    }
}
