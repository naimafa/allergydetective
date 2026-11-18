package com.nfar.allergydetective.platform

import androidx.compose.runtime.Composable

/**
 * Opens the system share sheet for a file named [fileName] with [content] (JSON).
 * Sent as an attachment, not as message text: messengers like WhatsApp reject
 * JSON text, and large exports exceed the intent size limit.
 */
@Composable
expect fun rememberShareFile(): (title: String, fileName: String, content: String) -> Unit
