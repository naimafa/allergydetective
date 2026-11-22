package com.nfar.allergydetective.ui.theme

import com.nfar.allergydetective.domain.reminder.SettingsStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * The chosen app language (user note 21.11.2026: German, English, French, Arabic, changeable in the profile),
 * read by the app root and changed in the profile. null = like the phone.
 */
class LanguageController(private val store: SettingsStore) {
    private val _language = MutableStateFlow(store.appLanguage())
    val language: StateFlow<String?> = _language.asStateFlow()

    fun onLanguageChange(language: String?) {
        store.saveAppLanguage(language)
        _language.value = language
    }

    companion object {
        /** Languages the app has texts for, in the order of the profile. */
        val SUPPORTED = listOf("de", "en", "fr", "ar")

        /** Written right to left. */
        fun isRtl(language: String): Boolean = language == "ar"
    }
}
