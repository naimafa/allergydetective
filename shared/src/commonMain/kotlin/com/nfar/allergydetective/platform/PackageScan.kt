package com.nfar.allergydetective.platform

import androidx.compose.runtime.Composable

/**
 * Takes a photo of the ingredient list on a package and reports the text read from it (user 27.11.2026), or null when
 * the user cancelled or nothing was read. The photo never leaves the phone. Returns null where this is not available:
 * the camera button is hidden then.
 */
@Composable
expect fun rememberPackageScan(onResult: (text: String?) -> Unit): (() -> Unit)?
