package com.nfar.allergydetective.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfar.allergydetective.domain.profile.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Decides the first screen: onboarding on the very first start, home otherwise. */
class StartViewModel(private val profile: ProfileRepository) : ViewModel() {

    /** Null while loading. */
    private val _onboardingCompleted = MutableStateFlow<Boolean?>(null)
    val onboardingCompleted: StateFlow<Boolean?> = _onboardingCompleted.asStateFlow()

    init {
        viewModelScope.launch { _onboardingCompleted.value = profile.isOnboardingCompleted() }
    }
}
