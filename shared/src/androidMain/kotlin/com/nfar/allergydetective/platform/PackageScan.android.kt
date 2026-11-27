package com.nfar.allergydetective.platform

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File

/**
 * The system camera takes the photo into the cache (no camera permission needed), ML Kit reads the text on the phone
 * (bundled Latin model, works offline), then the photo is deleted.
 *
 * The file is always the same one: the activity can be recreated while the camera is open (phone test 27.11.2026:
 * the result was lost then), so nothing may be kept in remembered state between the launch and the result.
 */
@Composable
actual fun rememberPackageScan(onResult: (text: String?) -> Unit): (() -> Unit)? {
    val context = LocalContext.current
    fun photoFile() = File(File(context.cacheDir, "scan").apply { mkdirs() }, "package.jpg")
    fun photoUri() = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", photoFile())

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { taken ->
        val file = photoFile()
        Log.i("PackageScan", "camera result: taken=$taken, size=${file.length()}")
        if (!taken) {
            file.delete()
            onResult(null)
            return@rememberLauncherForActivityResult
        }
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        runCatching { InputImage.fromFilePath(context, photoUri()) }
            .onSuccess { image ->
                recognizer.process(image)
                    .addOnSuccessListener {
                        Log.i("PackageScan", "read ${it.text.length} characters: ${it.text.lines().joinToString("|")}")
                        // Empty text (not null) tells the form that the photo was taken but nothing could be read.
                        onResult(it.text)
                    }
                    .addOnFailureListener {
                        Log.w("PackageScan", "text recognition failed", it)
                        onResult("")
                    }
                    .addOnCompleteListener {
                        recognizer.close()
                        file.delete()
                    }
            }
            .onFailure {
                Log.w("PackageScan", "photo could not be opened", it)
                recognizer.close()
                file.delete()
                onResult("")
            }
    }
    return {
        runCatching { launcher.launch(photoUri()) }.onFailure {
            Log.w("PackageScan", "camera could not be started", it)
            onResult(null)
        }
    }
}
