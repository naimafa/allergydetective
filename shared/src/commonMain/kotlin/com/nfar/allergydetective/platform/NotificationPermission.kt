package com.nfar.allergydetective.platform

import androidx.compose.runtime.Composable

/**
 * Returns a function that asks for the notification permission where the platform needs it
 * and reports the answer to [onResult]. Where no permission is needed it reports true at once.
 */
@Composable
expect fun rememberNotificationPermissionRequest(onResult: (granted: Boolean) -> Unit): () -> Unit
