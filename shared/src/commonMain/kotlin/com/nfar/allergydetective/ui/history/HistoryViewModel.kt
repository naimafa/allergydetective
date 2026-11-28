package com.nfar.allergydetective.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfar.allergydetective.domain.analysis.model.IllnessRelation
import com.nfar.allergydetective.domain.diary.DayCheckData
import com.nfar.allergydetective.domain.diary.DayCheckRepository
import com.nfar.allergydetective.domain.diary.DayCheckStatus
import com.nfar.allergydetective.domain.diary.HistoryEntry
import com.nfar.allergydetective.domain.diary.HistoryRepository
import com.nfar.allergydetective.domain.diary.MealRepository
import com.nfar.allergydetective.domain.diary.SymptomRepository
import com.nfar.allergydetective.domain.time.AppClock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HistoryDay(
    val epochDay: Long,
    val entries: List<HistoryEntry>,
    /** The day check with sleep, stress and factors, null if the day was never checked. */
    val check: DayCheckData? = null,
) {
    val status: DayCheckStatus? get() = check?.status
}

/**
 * Self-test 19.11.: one timeline keeps food and symptoms side by side (that is the point of the
 * diary); the chips narrow it down instead of separate tabs.
 */
enum class HistoryFilter { ALL, FOOD, SYMPTOMS, DAYS }

data class HistoryState(
    val loading: Boolean = true,
    /** Newest day first. Days without entries and without a check are left out. */
    val days: List<HistoryDay> = emptyList(),
    val filter: HistoryFilter = HistoryFilter.ALL,
    /** User 28.11.2026: search in the history (dishes, ingredients, complaints, notes); blank = no search. */
    val query: String = "",
    /** Lifestyle factor keys by id, to name the factors of a day check. */
    val factorKeys: Map<Long, String> = emptyMap(),
) {
    /**
     * Days and entries that match [filter]; a day without matching content is left out.
     * [sickDays]: "Days" also shows an ill day without a day check (phone test 19.11.: it was
     * missing there but shown under "All").
     */
    fun visibleDays(sickDays: Set<Long> = emptySet(), searchText: (HistoryEntry) -> String = { "" }): List<HistoryDay> {
        val byFilter = filteredDays(sickDays)
        if (!searching) return byFilter
        // Searching shows only the entries that match; days without a match (and the day checks) are left out.
        return byFilter.map { day -> day.copy(entries = day.entries.filter { matchesSearch(searchText(it), query) }) }
            .filter { it.entries.isNotEmpty() }
    }

    val searching: Boolean get() = query.isNotBlank()

    private fun filteredDays(sickDays: Set<Long>): List<HistoryDay> = when (filter) {
        HistoryFilter.ALL -> days
        HistoryFilter.DAYS -> days.filter { it.check != null || it.epochDay in sickDays }.map { it.copy(entries = emptyList()) }
        HistoryFilter.FOOD -> days.map { day -> day.copy(entries = day.entries.filterIsInstance<HistoryEntry.MealEntry>()) }
            .filter { it.entries.isNotEmpty() }
        HistoryFilter.SYMPTOMS -> days.map { day -> day.copy(entries = day.entries.filterIsInstance<HistoryEntry.SymptomEntry>()) }
            .filter { it.entries.isNotEmpty() }
    }

    /** The day check is shown for "all" and "days". */
    val showsDayChecks: Boolean get() = !searching && (filter == HistoryFilter.ALL || filter == HistoryFilter.DAYS)
}

/** Every word of [query] must occur in [text], upper and lower case ignored ("linsen tomate" finds "Linsensuppe · Tomaten"). */
fun matchesSearch(text: String, query: String): Boolean {
    val words = query.lowercase().split(Regex("\\s+")).filter { it.isNotEmpty() }
    val haystack = text.lowercase()
    return words.all { it in haystack }
}

/** History of the last [DAYS] days with editing and deletion of single entries. */
class HistoryViewModel(
    private val history: HistoryRepository,
    private val meals: MealRepository,
    private val symptoms: SymptomRepository,
    private val dayChecks: DayCheckRepository,
    private val clock: AppClock,
) : ViewModel() {

    private val _state = MutableStateFlow(HistoryState())
    val state: StateFlow<HistoryState> = _state.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            val today = clock.today()
            val fromDay = today - DAYS + 1
            val entries = history.entriesSince(clock.startOfDayMillis(fromDay))
            val checks = (fromDay..today).mapNotNull { dayChecks.load(it) }.associateBy { it.epochDay }
            val byDay = entries.groupBy { clock.epochDayOf(it.time) }
            val days = (today downTo fromDay)
                .filter { it in byDay || it in checks }
                // User note 21.11.2026: newest first in every filter, also within a day (latest time, then latest saved).
                .map { HistoryDay(it, byDay[it].orEmpty().sortedWith(compareByDescending<HistoryEntry> { e -> e.time }.thenByDescending { e -> e.id }), checks[it]) }
            val factorKeys = dayChecks.lifestyleFactors().associate { it.id to it.key }
            _state.update { it.copy(loading = false, days = days, factorKeys = factorKeys) }
        }
    }

    fun onFilterChange(filter: HistoryFilter) {
        _state.update { it.copy(filter = filter) }
    }

    fun onQueryChange(query: String) {
        _state.update { it.copy(query = query) }
    }

    /**
     * Self-test 19.11.: a logged meal becomes a dish with one tap. The meal is linked to the new dish,
     * so the history shows its name.
     */
    fun onSaveAsFavorite(mealId: Long, name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            val meal = meals.meal(mealId) ?: return@launch
            val favoriteId = meals.saveFavorite(trimmed, meal.items)
            meals.updateMeal(meal.id, meal.eatenAt, meal.tzOffsetMin, meal.items, favoriteId)
            refresh()
        }
    }

    /** A complaint on a sick day: part of the illness or not (self-test 19.11.). */
    fun onIllnessRelationChange(entry: HistoryEntry.SymptomEntry, relation: IllnessRelation) {
        viewModelScope.launch {
            symptoms.setIllnessRelation(entry.id, relation)
            refresh()
        }
    }

    /** Permanently removes the entry; the screen asks first. */
    fun onDelete(entry: HistoryEntry) {
        viewModelScope.launch {
            when (entry) {
                is HistoryEntry.MealEntry -> meals.deleteMeal(entry.id)
                is HistoryEntry.SymptomEntry -> symptoms.deleteEntry(entry.id)
            }
            refresh()
        }
    }

    companion object {
        const val DAYS = 14
    }
}

/** Identifies an entry across meals and complaints (their ids are separate counters). */
fun HistoryEntry.key(): Pair<Boolean, Long> = (this is HistoryEntry.MealEntry) to id
