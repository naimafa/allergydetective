package com.nfar.allergydetective.platform

import androidx.compose.runtime.Composable

/**
 * Starts the system speech recognition (TRD 3.2, offline preferred) and reports the text,
 * or null when the user cancelled. Returns null when recognition is not available on the
 * device: the microphone is then hidden and the keyboard search stays (UX 3.2).
 */
@Composable
expect fun rememberSpeechInput(language: String, onResult: (text: String?) -> Unit): (() -> Unit)?
