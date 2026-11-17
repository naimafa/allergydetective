package com.nfar.allergydetective.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfar.allergydetective.domain.diary.DayCheckRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Detective progress on the home screen (UX 2): confirmed days of the collection phase. */
class HomeViewModel(private val dayChecks: DayCheckRepository) : ViewModel() {

    private val _confirmedDays = MutableStateFlow<Int?>(null)
    val confirmedDays: StateFlow<Int?> = _confirmedDays.asStateFlow()

    /** Called whenever the home screen becomes visible, e.g. after a day check. */
    fun refresh() {
        viewModelScope.launch { _confirmedDays.value = dayChecks.confirmedDayCount() }
    }

    companion object {
        /** Days until the first evaluation (UX 5). */
        const val COLLECTION_DAYS = 7
    }
}
