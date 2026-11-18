package com.nfar.allergydetective.platform

import androidx.compose.runtime.Composable

// Not built yet (no Mac). SFSpeechRecognizer follows with the iOS build; until then the mic is hidden.
@Composable
actual fun rememberSpeechInput(language: String, onResult: (text: String?) -> Unit): (() -> Unit)? = null
