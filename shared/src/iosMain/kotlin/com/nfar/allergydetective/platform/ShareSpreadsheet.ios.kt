package com.nfar.allergydetective.platform

import androidx.compose.runtime.Composable

// Not built yet (no Mac). Follows with the iOS build, like the PDF and file sharing.
@Composable
actual fun rememberShareSpreadsheet(): (title: String, fileName: String, sheets: List<Sheet>) -> Unit = { _, _, _ -> }
