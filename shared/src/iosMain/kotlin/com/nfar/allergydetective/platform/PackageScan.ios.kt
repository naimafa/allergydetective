package com.nfar.allergydetective.platform

import androidx.compose.runtime.Composable

// Not built yet (no Mac). Vision text recognition follows with the iOS build; until then the camera button is hidden.
@Composable
actual fun rememberPackageScan(onResult: (text: String?) -> Unit): PackageScanStarters? = null
