package com.nfar.allergydetective.ui.results

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfar.allergydetective.domain.analysis.AnalysisEngine
import com.nfar.allergydetective.domain.analysis.AnalysisRepository
import com.nfar.allergydetective.domain.analysis.CandidateCatalog
import com.nfar.allergydetective.domain.analysis.CandidateKeys
import com.nfar.allergydetective.domain.analysis.model.Candidate
import com.nfar.allergydetective.domain.analysis.model.EvidenceLevel
import com.nfar.allergydetective.domain.diary.IngredientInfo
import com.nfar.allergydetective.domain.time.AppClock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

/** What the screen needs to name a candidate. Display names come from resources. */
sealed interface CandidateLabel {
    data class Ingredient(val ingredient: IngredientInfo) : CandidateLabel
    data class Property(val key: String) : CandidateLabel
    data class Factor(val key: String) : CandidateLabel
    data object BadSleep : CandidateLabel
    data object HighStress : CandidateLabel
}

data class ResultItem(
    val label: CandidateLabel,
    val level: EvidenceLevel,
    /** Unweighted counts for the one-sentence reason (UX 5). */
    val exposures: Int,
    val withComplaints: Int,
    /** The same for meals or days without the candidate. */
    val otherExposures: Int,
    val otherWithComplaints: Int,
    /** How many times as often complaints came with the candidate than without (TRD 2.4); 0 = unknown. */
    val lift: Double = 0.0,
    /** Food: typical delay in days, 0 = same day (user 24.11.2026); null for lifestyle or without complaints. */
    val lagDays: Int? = null,
    /** Found as a fast reaction within 2 hours after a meal; counts are meals then, not days. */
    val fast: Boolean = false,
)

data class ResultsState(
    val loading: Boolean = true,
    val confirmedDays: Int = 0,
    /** Candidates with at least a trace, strongest first. */
    val items: List<ResultItem> = emptyList(),
    val groups: List<List<CandidateLabel>> = emptyList(),
    val safeIngredients: List<CandidateLabel> = emptyList(),
    /** Ingredients eaten almost every day: they cannot be compared, only tested by leaving them out. */
    val dailyFoods: List<CandidateLabel> = emptyList(),
    /** Ingredients with a known allergy that were eaten (TRD 2.6). */
    val knownAllergyIngredients: List<CandidateLabel> = emptyList(),
    /** Why there is nothing to show yet (only read when [items] is empty). */
    val noPatternReason: NoPatternReason = NoPatternReason.TOO_FEW_DAYS,
)

/** Simple result screen (UX 5): ranking by evidence level, groups, safe list. */
class ResultsViewModel(
    private val repository: AnalysisRepository,
    private val engine: AnalysisEngine,
    private val catalog: CandidateCatalog,
    private val clock: AppClock,
    private val computeContext: CoroutineContext,
) : ViewModel() {

    private val _state = MutableStateFlow(ResultsState())
    val state: StateFlow<ResultsState> = _state.asStateFlow()

    init {
        viewModelScope.launch { _state.value = load() }
    }

    private suspend fun load(): ResultsState {
        val now = clock.nowMillis()
        val input = repository.loadInput(now, clock.offsetMinAt(now))
        val result = withContext(computeContext) { engine.analyze(input) }
        val keys = catalog.keys()
        fun label(candidate: Candidate) = labelOf(candidate, keys)
        return ResultsState(
            loading = false,
            confirmedDays = input.diary.dayChecks.count { it.confirmed },
            items = result.ranking
                .filter { it.level >= EvidenceLevel.TRACE }
                .mapNotNull { r -> label(r.candidate)?.let { ResultItem(it, r.level, r.stats.exposureCount, r.stats.hitCount, r.stats.unexposedCount, r.stats.unexposedHitCount, r.lift, r.lagDays, r.fast) } },
            groups = result.groups.map { group -> group.members.mapNotNull(::label) },
            safeIngredients = result.safeIngredientIds.mapNotNull { label(Candidate.Ingredient(it)) },
            // Eaten almost every day: no days without it to compare with (user 24.11.2026).
            dailyFoods = result.ranking.filter { it.tooOften && it.candidate is Candidate.Ingredient }.mapNotNull { label(it.candidate) },
            knownAllergyIngredients = result.knownAllergyExposures
                .map { it.ingredientId }
                .distinct()
                .mapNotNull { label(Candidate.Ingredient(it)) },
            noPatternReason = noPatternReason(input.diary, FIRST_EVALUATION_DAYS),
        )
    }

    fun refresh() {
        _state.update { it.copy(loading = true) }
        viewModelScope.launch { _state.value = load() }
    }

    private fun labelOf(candidate: Candidate, keys: CandidateKeys): CandidateLabel? = when (candidate) {
        is Candidate.Ingredient -> keys.ingredients[candidate.id]?.let { CandidateLabel.Ingredient(it) }
        is Candidate.Property -> keys.properties[candidate.id]?.let { CandidateLabel.Property(it) }
        is Candidate.DayFactor -> keys.factors[candidate.id]?.let { CandidateLabel.Factor(it) }
        Candidate.BadSleep -> CandidateLabel.BadSleep
        Candidate.HighStress -> CandidateLabel.HighStress
    }

    companion object {
        /** First evaluation after this many confirmed days (UX 5). */
        const val FIRST_EVALUATION_DAYS = 7
    }
}
