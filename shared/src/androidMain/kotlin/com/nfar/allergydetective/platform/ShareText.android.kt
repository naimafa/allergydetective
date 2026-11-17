package com.nfar.allergydetective.platform

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberShareText(): (title: String, text: String) -> Unit {
    val context = LocalContext.current
    return { title, text ->
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(send, title))
    }
}
