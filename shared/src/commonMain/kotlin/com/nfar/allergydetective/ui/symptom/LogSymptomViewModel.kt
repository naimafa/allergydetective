package com.nfar.allergydetective.ui.symptom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfar.allergydetective.domain.analysis.model.IllnessRelation
import com.nfar.allergydetective.domain.diary.SymptomDuration
import com.nfar.allergydetective.domain.diary.SymptomInfo
import com.nfar.allergydetective.domain.diary.SymptomRepository
import com.nfar.allergydetective.domain.time.AppClock
import com.nfar.allergydetective.domain.time.SymptomDayPart
import com.nfar.allergydetective.domain.time.timeOf
import com.nfar.allergydetective.ui.common.ExactTime
import com.nfar.allergydetective.ui.common.MAX_DAYS_BACK
import com.nfar.allergydetective.ui.common.timeOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** The detail overlay for one symptom (CONTENT_Symptome 2). */
data class SymptomDraft(
    val symptom: SymptomInfo,
    val intensity: Int = LogSymptomViewModel.DEFAULT_INTENSITY,
    val dayPart: SymptomDayPart = SymptomDayPart.NOW,
    /** Picked in the time dialog, overrides [dayPart]. */
    val exactTime: ExactTime? = null,
    val note: String = "",
    /** Only asked on a sick day (self-test 19.11.). */
    val illnessRelation: IllnessRelation = IllnessRelation.UNKNOWN,
    /** Optional (feedback 20.11.): how long it lasted. */
    val duration: SymptomDuration? = null,
)

data class LogSymptomState(
    val loading: Boolean = true,
    /** Symptoms per group in catalog order, groups in [LogSymptomViewModel.GROUP_ORDER]. */
    val groups: List<Pair<String, List<SymptomInfo>>> = emptyList(),
    val favorites: List<SymptomInfo> = emptyList(),
    /** Emergency notice for this symptom is shown before anything else (TRD 3.4). */
    val emergencyNoticeFor: SymptomInfo? = null,
    val draft: SymptomDraft? = null,
    /** Time of the last logged meal, for "after the meal". */
    val lastMealAt: Long? = null,
    val savedEntryId: Long? = null,
    /** Today and the days that can be picked, as far as they are sick days. */
    val sickDays: Set<Long> = emptySet(),
    val today: Long = 0,
    /** Correcting a logged complaint from the history (feedback 19.11.): saving replaces it. */
    val editingEntryId: Long? = null,
    /** Day row: 0 = today, up to MAX_DAYS_BACK (feedback 19.11.). */
    val day: Int = 0,
    /** Edit saved or dismissed: the screen closes. */
    val finished: Boolean = false,
) {
    /** The draft lies on a sick day: the overlay asks whether it belongs to the illness. */
    val draftOnSickDay: Boolean get() = draft != null && (today - day) in sickDays
}

/** Report a symptom (UX 3.3): grid, overlay with intensity and time, quick log, emergency notice. */
class LogSymptomViewModel(
    private val repository: SymptomRepository,
    private val clock: AppClock,
) : ViewModel() {

    private val _state = MutableStateFlow(LogSymptomState())
    val state: StateFlow<LogSymptomState> = _state.asStateFlow()

    private var symptomsById: Map<Long, SymptomInfo> = emptyMap()

    /** Declared after the fields it fills; editing and adding for a past day wait for it. */
    private val initJob = viewModelScope.launch {
            val symptoms = repository.symptoms()
            symptomsById = symptoms.associateBy { it.id }
            val groups = symptoms.groupBy { it.groupKey }
                .toList()
                .sortedBy { (group, _) -> GROUP_ORDER.indexOf(group).let { if (it < 0) GROUP_ORDER.size else it } }
            val lastMeal = repository.lastMealAt(clock.nowMillis())
            val today = clock.today()
            val sickDays = (0..MAX_DAYS_BACK).map { today - it }.filter { repository.isSickDay(it) }.toSet()
            _state.update {
                it.copy(loading = false, groups = groups, favorites = favorites(), lastMealAt = lastMeal, today = today, sickDays = sickDays)
            }
        }

    private suspend fun favorites(): List<SymptomInfo> =
        repository.mostUsedSymptomIds(FAVORITE_COUNT).mapNotNull { symptomsById[it] }

    /** "+ Symptom" on a day in the history: the day row starts on that day (feedback 19.11.). */
    fun startForDay(daysAgo: Int) = onDayChange(daysAgo)

    /**
     * The day row (feedback 19.11.): today or up to five days back. Day parts like "morning" then mean
     * that day; "now" and "after the meal" only exist for today.
     */
    fun onDayChange(daysAgo: Int) {
        val day = daysAgo.coerceAtLeast(0)
        _state.update { state ->
            state.copy(
                day = day,
                draft = state.draft?.let { draft ->
                    val part = if (day > 0 && draft.dayPart in TODAY_ONLY_PARTS) SymptomDayPart.MIDDAY else draft.dayPart
                    draft.copy(dayPart = part, exactTime = draft.exactTime?.copy(daysAgo = day))
                },
            )
        }
    }

    /** "Edit" on a complaint in the history: the overlay opens with its values. */
    fun startEditing(entryId: Long) {
        if (_state.value.editingEntryId == entryId) return
        viewModelScope.launch {
            initJob.join()
            val entry = repository.entry(entryId) ?: return@launch
            val symptom = symptomsById[entry.symptomId] ?: return@launch
            val (hour, minute) = clock.localTime(entry.startedAt)
            val day = clock.epochDayOf(entry.startedAt)
            val sick = repository.isSickDay(day)
            _state.update {
                it.copy(
                    editingEntryId = entryId,
                    day = (it.today - day).toInt(),
                    sickDays = if (sick) it.sickDays + day else it.sickDays,
                    draft = SymptomDraft(
                        symptom = symptom,
                        intensity = entry.intensity,
                        exactTime = ExactTime((it.today - day).toInt(), hour, minute),
                        note = entry.note.orEmpty(),
                        illnessRelation = entry.illnessRelation ?: IllnessRelation.UNKNOWN,
                        duration = entry.duration,
                    ),
                )
            }
        }
    }

    /** A new draft: "now" today, "midday" on an earlier day of the day row. */
    private fun newDraft(symptom: SymptomInfo): SymptomDraft =
        SymptomDraft(symptom, dayPart = if (_state.value.day > 0) SymptomDayPart.MIDDAY else SymptomDayPart.NOW)

    /** Tap on a tile: overlay, or the emergency notice first. */
    fun onSymptomClick(symptom: SymptomInfo) {
        _state.update {
            if (symptom.isRedFlag) it.copy(emergencyNoticeFor = symptom)
            else it.copy(draft = newDraft(symptom))
        }
    }

    /** "Continue logging" on the emergency notice. */
    fun onEmergencyNoticeContinue() {
        val current = _state.value
        val symptom = current.emergencyNoticeFor ?: return
        _state.update { it.copy(emergencyNoticeFor = null) }
        _state.update { it.copy(draft = newDraft(symptom)) }
    }

    fun onEmergencyNoticeDismiss() {
        _state.update { it.copy(emergencyNoticeFor = null) }
    }

    fun onIntensityChange(intensity: Int) = updateDraft { it.copy(intensity = intensity.coerceIn(1, MAX_INTENSITY)) }

    fun onDayPartChange(part: SymptomDayPart) = updateDraft { it.copy(dayPart = part, exactTime = null) }

    /**
     * Today: day parts that have begun, "after the meal" only with a meal today.
     * An earlier day: every part of the day except "now" and "after the meal".
     */
    fun dayParts(): List<SymptomDayPart> =
        if (_state.value.day == 0) {
            SymptomDayPart.entries.filter { clock.timeOf(it, _state.value.lastMealAt) != null }
        } else {
            SymptomDayPart.entries - TODAY_ONLY_PARTS
        }

    /** The time dialog only changes the clock time; the day comes from the day row. */
    fun onExactTimeChange(time: ExactTime) {
        val day = _state.value.day
        updateDraft { it.copy(exactTime = time.copy(daysAgo = day)) }
    }

    fun onNoteChange(note: String) = updateDraft { it.copy(note = note) }

    /** Tapping the chosen duration again removes it: the duration stays optional. */
    fun onDurationChange(duration: SymptomDuration) = updateDraft { it.copy(duration = duration.takeIf { d -> d != it.duration }) }

    fun onIllnessRelationChange(relation: IllnessRelation) = updateDraft { it.copy(illnessRelation = relation) }

    fun onDraftDismiss() {
        // Editing from the history: closing the overlay goes back there.
        _state.update { it.copy(draft = null, finished = it.editingEntryId != null) }
    }

    fun onSaveDraft() {
        val draft = _state.value.draft ?: return
        _state.update { it.copy(draft = null) }
        save(draft)
    }

    private fun save(draft: SymptomDraft) {
        viewModelScope.launch {
            val day = _state.value.day
            val startedAt = draft.exactTime?.let { clock.timeOf(it, 0) }
                ?: if (day > 0 && draft.dayPart !in TODAY_ONLY_PARTS) {
                    clock.millisAt(clock.today() - day, draft.dayPart.hour, draft.dayPart.minute)
                } else {
                    clock.timeOf(draft.dayPart, repository.lastMealAt(clock.nowMillis()))
                }
                ?: clock.nowMillis()
            _state.value.editingEntryId?.let { entryId ->
                repository.updateEntry(entryId, startedAt, clock.offsetMinAt(startedAt), draft.intensity, draft.note.trim().ifEmpty { null })
                if (clock.epochDayOf(startedAt) in _state.value.sickDays || repository.isSickDay(clock.epochDayOf(startedAt))) {
                    repository.setIllnessRelation(entryId, draft.illnessRelation)
                }
                repository.setDuration(entryId, draft.duration)
                _state.update { it.copy(finished = true) }
                return@launch
            }
            val id = repository.saveEntry(
                symptomId = draft.symptom.id,
                startedAt = startedAt,
                tzOffsetMin = clock.offsetMinAt(startedAt),
                intensity = draft.intensity,
                note = draft.note.trim().ifEmpty { null },
            )
            if (clock.epochDayOf(startedAt) in _state.value.sickDays) repository.setIllnessRelation(id, draft.illnessRelation)
            draft.duration?.let { repository.setDuration(id, it) }
            _state.update { it.copy(savedEntryId = id, favorites = favorites(), finished = true) }
        }
    }

    fun onUndo(entryId: Long) {
        viewModelScope.launch {
            repository.deleteEntry(entryId)
            _state.update { it.copy(favorites = favorites()) }
        }
    }

    fun onSavedShown() {
        _state.update { it.copy(savedEntryId = null) }
    }

    private fun updateDraft(change: (SymptomDraft) -> SymptomDraft) {
        _state.update { state -> state.copy(draft = state.draft?.let(change)) }
    }

    companion object {
        const val DEFAULT_INTENSITY = 3


        /** Only meaningful for today. */
        val TODAY_ONLY_PARTS = setOf(SymptomDayPart.NOW, SymptomDayPart.AFTER_MEAL)
        const val MAX_INTENSITY = 5
        const val FAVORITE_COUNT = 3
        val GROUP_ORDER = listOf("digestive", "skin", "head", "airways", "other")
    }
}
