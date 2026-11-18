package com.nfar.allergydetective.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfar.allergydetective.domain.diary.CustomIngredientRepository
import com.nfar.allergydetective.domain.diary.PropertyInfo
import com.nfar.allergydetective.domain.profile.ProfileRepository
import com.nfar.allergydetective.domain.time.AppClock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class KnownAllergiesState(
    val loading: Boolean = true,
    val properties: List<PropertyInfo> = emptyList(),
    val selected: Set<Long> = emptySet(),
    /** Set after saving, the screen moves on. */
    val saved: Boolean = false,
)

/**
 * Known allergies (TRD 3.1), used by the onboarding and later by the profile.
 * Saving also marks the onboarding as completed.
 */
class KnownAllergiesViewModel(
    private val profile: ProfileRepository,
    private val catalog: CustomIngredientRepository,
    private val clock: AppClock,
) : ViewModel() {

    private val _state = MutableStateFlow(KnownAllergiesState())
    val state: StateFlow<KnownAllergiesState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val properties = catalog.properties()
            val selected = profile.knownAllergyPropertyIds()
            _state.update { it.copy(loading = false, properties = properties, selected = selected) }
        }
    }

    fun onToggle(propertyId: Long) = _state.update {
        it.copy(selected = if (propertyId in it.selected) it.selected - propertyId else it.selected + propertyId)
    }

    fun onSave() {
        val selected = _state.value.selected
        viewModelScope.launch {
            profile.saveKnownAllergies(selected)
            if (!profile.isOnboardingCompleted()) profile.completeOnboarding(clock.nowMillis())
            _state.update { it.copy(saved = true) }
        }
    }

    fun onSavedHandled() = _state.update { it.copy(saved = false) }
}
