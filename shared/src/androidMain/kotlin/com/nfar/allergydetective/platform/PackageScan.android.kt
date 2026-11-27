package com.nfar.allergydetective.platform

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File

/**
 * The system camera takes the photo into the cache (no camera permission needed), ML Kit reads the text on the phone
 * (bundled Latin model, works offline), then the photo is deleted.
 */
@Composable
actual fun rememberPackageScan(onResult: (text: String?) -> Unit): (() -> Unit)? {
    val context = LocalContext.current
    val photo = remember { mutableStateOf<Pair<File, Uri>?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { taken ->
        val (file, uri) = photo.value ?: return@rememberLauncherForActivityResult
        photo.value = null
        if (!taken) {
            file.delete()
            onResult(null)
            return@rememberLauncherForActivityResult
        }
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        runCatching { InputImage.fromFilePath(context, uri) }
            .onSuccess { image ->
                recognizer.process(image)
                    .addOnSuccessListener { onResult(it.text.takeIf { text -> text.isNotBlank() }) }
                    .addOnFailureListener { onResult(null) }
                    .addOnCompleteListener {
                        recognizer.close()
                        file.delete()
                    }
            }
            .onFailure {
                recognizer.close()
                file.delete()
                onResult(null)
            }
    }
    return {
        val file = File(File(context.cacheDir, "scan").apply { mkdirs() }, "package.jpg")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        photo.value = file to uri
        runCatching { launcher.launch(uri) }.onFailure {
            photo.value = null
            onResult(null)
        }
    }
}
