package com.nfar.allergydetective.platform

import androidx.compose.runtime.Composable

/** Opens the system share sheet for [text]; the user picks where it goes (mail, drive, …). */
@Composable
expect fun rememberShareText(): (title: String, text: String) -> Unit
