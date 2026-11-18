package com.nfar.allergydetective.platform

import androidx.compose.runtime.Composable

// Not built yet (no Mac). UIActivityViewController follows with the iOS build.
@Composable
actual fun rememberShareFile(): (title: String, fileName: String, content: String) -> Unit = { _, _, _ -> }
