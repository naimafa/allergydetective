package com.nfar.allergydetective.platform

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File

/**
 * The system camera takes the photo into the cache (no camera permission needed), a crop screen lets the user pick the
 * ingredient list (user 27.11.2026), ML Kit reads the text on the phone (bundled Latin model, works offline), then the
 * photos are deleted.
 *
 * The file is always the same one: the activity can be recreated while the camera is open (phone test 27.11.2026:
 * the result was lost then), so nothing may be kept in remembered state between the launch and the result.
 */
@Composable
actual fun rememberPackageScan(onResult: (text: String?) -> Unit): (() -> Unit)? {
    val context = LocalContext.current
    fun photoFile() = File(File(context.cacheDir, "scan").apply { mkdirs() }, "package.jpg")
    fun photoUri() = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", photoFile())

    /** Reads the text of the (cropped) picture, then deletes the photos. */
    fun read(uri: Uri, cropped: File?) {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        fun cleanUp() {
            recognizer.close()
            photoFile().delete()
            cropped?.delete()
        }
        runCatching { InputImage.fromFilePath(context, uri) }
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
                    .addOnCompleteListener { cleanUp() }
            }
            .onFailure {
                Log.w("PackageScan", "photo could not be opened", it)
                cleanUp()
                onResult("")
            }
    }

    val cropper = rememberLauncherForActivityResult(CropImageContract()) { result ->
        val uri = result.uriContent
        Log.i("PackageScan", "crop result: successful=${result.isSuccessful}")
        if (!result.isSuccessful || uri == null) {
            photoFile().delete()
            onResult(null)
        } else {
            read(uri, result.getUriFilePath(context)?.let(::File))
        }
    }

    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { taken ->
        val file = photoFile()
        Log.i("PackageScan", "camera result: taken=$taken, size=${file.length()}")
        if (!taken) {
            file.delete()
            onResult(null)
            return@rememberLauncherForActivityResult
        }
        cropper.launch(
            CropImageContractOptions(
                uri = photoUri(),
                cropImageOptions = CropImageOptions(
                    guidelines = CropImageView.Guidelines.ON,
                    // Where the ingredient list sits is the user's call; free corners, no fixed shape.
                    fixAspectRatio = false,
                    outputCompressQuality = 95,
                ),
            ),
        )
    }
    return {
        runCatching { camera.launch(photoUri()) }.onFailure {
            Log.w("PackageScan", "camera could not be started", it)
            onResult(null)
        }
    }
}
