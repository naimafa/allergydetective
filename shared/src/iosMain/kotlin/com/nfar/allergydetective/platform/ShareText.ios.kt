package com.nfar.allergydetective.platform

import androidx.compose.runtime.Composable

// Not built yet (no Mac). UIActivityViewController follows with the iOS build.
@Composable
actual fun rememberShareText(): (title: String, text: String) -> Unit = { _, _ -> }
