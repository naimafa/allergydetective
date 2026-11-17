package com.nfar.allergydetective.ui.symptom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfar.allergydetective.domain.diary.SymptomInfo
import com.nfar.allergydetective.domain.diary.SymptomRepository
import com.nfar.allergydetective.domain.time.AppClock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** The detail overlay for one symptom (CONTENT_Symptome 2). */
data class SymptomDraft(
    val symptom: SymptomInfo,
    val intensity: Int = LogSymptomViewModel.DEFAULT_INTENSITY,
    val minutesAgo: Int = 0,
    val note: String = "",
)

data class LogSymptomState(
    val loading: Boolean = true,
    /** Symptoms per group in catalog order, groups in [LogSymptomViewModel.GROUP_ORDER]. */
    val groups: List<Pair<String, List<SymptomInfo>>> = emptyList(),
    val favorites: List<SymptomInfo> = emptyList(),
    /** Emergency notice for this symptom is shown before anything else (TRD 3.4). */
    val emergencyNoticeFor: SymptomInfo? = null,
    val draft: SymptomDraft? = null,
    val savedEntryId: Long? = null,
    /** True when the notice was opened by a long press: continuing saves directly. */
    internal val quickLogAfterNotice: Boolean = false,
)

/** Report a symptom (UX 3.3): grid, overlay with intensity and time, quick log, emergency notice. */
class LogSymptomViewModel(
    private val repository: SymptomRepository,
    private val clock: AppClock,
) : ViewModel() {

    private val _state = MutableStateFlow(LogSymptomState())
    val state: StateFlow<LogSymptomState> = _state.asStateFlow()

    private var symptomsById: Map<Long, SymptomInfo> = emptyMap()

    init {
        viewModelScope.launch {
            val symptoms = repository.symptoms()
            symptomsById = symptoms.associateBy { it.id }
            val groups = symptoms.groupBy { it.groupKey }
                .toList()
                .sortedBy { (group, _) -> GROUP_ORDER.indexOf(group).let { if (it < 0) GROUP_ORDER.size else it } }
            _state.update { it.copy(loading = false, groups = groups, favorites = favorites()) }
        }
    }

    private suspend fun favorites(): List<SymptomInfo> =
        repository.mostUsedSymptomIds(FAVORITE_COUNT).mapNotNull { symptomsById[it] }

    /** Tap on a tile: overlay, or the emergency notice first. */
    fun onSymptomClick(symptom: SymptomInfo) {
        _state.update {
            if (symptom.isRedFlag) it.copy(emergencyNoticeFor = symptom, quickLogAfterNotice = false)
            else it.copy(draft = SymptomDraft(symptom))
        }
    }

    /** Long press: save at once with default intensity and "now" (CONTENT_Symptome 5.3). */
    fun onSymptomLongClick(symptom: SymptomInfo) {
        if (symptom.isRedFlag) {
            _state.update { it.copy(emergencyNoticeFor = symptom, quickLogAfterNotice = true) }
        } else {
            save(SymptomDraft(symptom))
        }
    }

    /** "Continue logging" on the emergency notice. */
    fun onEmergencyNoticeContinue() {
        val current = _state.value
        val symptom = current.emergencyNoticeFor ?: return
        _state.update { it.copy(emergencyNoticeFor = null, quickLogAfterNotice = false) }
        if (current.quickLogAfterNotice) save(SymptomDraft(symptom)) else _state.update { it.copy(draft = SymptomDraft(symptom)) }
    }

    fun onEmergencyNoticeDismiss() {
        _state.update { it.copy(emergencyNoticeFor = null, quickLogAfterNotice = false) }
    }

    fun onIntensityChange(intensity: Int) = updateDraft { it.copy(intensity = intensity.coerceIn(1, MAX_INTENSITY)) }

    fun onMinutesAgoChange(minutes: Int) = updateDraft { it.copy(minutesAgo = minutes) }

    fun onNoteChange(note: String) = updateDraft { it.copy(note = note) }

    fun onDraftDismiss() {
        _state.update { it.copy(draft = null) }
    }

    fun onSaveDraft() {
        val draft = _state.value.draft ?: return
        _state.update { it.copy(draft = null) }
        save(draft)
    }

    private fun save(draft: SymptomDraft) {
        viewModelScope.launch {
            val startedAt = clock.nowMillis() - draft.minutesAgo * MILLIS_PER_MINUTE
            val id = repository.saveEntry(
                symptomId = draft.symptom.id,
                startedAt = startedAt,
                tzOffsetMin = clock.offsetMinAt(startedAt),
                intensity = draft.intensity,
                note = draft.note.trim().ifEmpty { null },
            )
            _state.update { it.copy(savedEntryId = id, favorites = favorites()) }
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
        const val MAX_INTENSITY = 5
        const val FAVORITE_COUNT = 3

        /** Time chips of the overlay: now, 30 min, 1 h, 2 h, 3 h ago. */
        val MINUTES_AGO_OPTIONS = listOf(0, 30, 60, 120, 180)
        val GROUP_ORDER = listOf("digestive", "skin", "head", "airways", "other")
        private const val MILLIS_PER_MINUTE = 60_000L
    }
}
