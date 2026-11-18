package com.nfar.allergydetective.platform

import android.content.ClipData
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File

private const val MIME_JSON = "application/json"
private const val EXPORT_DIR = "export"

@Composable
actual fun rememberShareFile(): (title: String, fileName: String, content: String) -> Unit {
    val context = LocalContext.current
    return { title, fileName, content ->
        // Cache only: the old export is replaced each time and never leaves the app on its own.
        val dir = File(context.cacheDir, EXPORT_DIR).apply { mkdirs() }
        dir.listFiles()?.forEach { it.delete() }
        val file = File(dir, fileName).apply { writeText(content) }
        // Authority must match the <provider> in the app manifest.
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = MIME_JSON
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_STREAM, uri)
            clipData = ClipData.newRawUri(fileName, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(send, title))
    }
}
