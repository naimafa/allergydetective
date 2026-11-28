package com.nfar.allergydetective.platform

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
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
actual fun rememberPackageScan(report: (text: String?, blurry: Boolean) -> Unit): PackageScanStarters? {
    val context = LocalContext.current
    fun onResult(text: String?, blurry: Boolean = false) = report(text, blurry)
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
                        // User 28.11.2026: unsure reading (blurry or unclear photo) is reported with the text, the form warns.
                        val confidence = meanConfidence(it)
                        Log.i("PackageScan", "confidence=$confidence (limit $CONFIDENCE_LIMIT)")
                        onResult(it.text, confidence < CONFIDENCE_LIMIT)
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

    fun crop() {
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

    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { taken ->
        val file = photoFile()
        Log.i("PackageScan", "camera result: taken=$taken, size=${file.length()}")
        if (!taken) {
            file.delete()
            onResult(null)
            return@rememberLauncherForActivityResult
        }
        crop()
    }

    // User 28.11.2026: a picture already in the gallery. The system photo picker needs no permission; the picture is
    // copied to the same cache file as a camera photo, so crop, reading and clean-up work the same way.
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { picked ->
        if (picked == null) {
            onResult(null)
            return@rememberLauncherForActivityResult
        }
        val copied = runCatching {
            context.contentResolver.openInputStream(picked)?.use { input ->
                photoFile().outputStream().use { input.copyTo(it) }
            } != null
        }.getOrDefault(false)
        if (copied) {
            crop()
        } else {
            Log.w("PackageScan", "picture could not be copied")
            photoFile().delete()
            onResult("")
        }
    }
    return PackageScanStarters(
        takePhoto = {
            runCatching { camera.launch(photoUri()) }.onFailure {
                Log.w("PackageScan", "camera could not be started", it)
                onResult(null)
            }
        },
        pickFromGallery = {
            runCatching { gallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }.onFailure {
                Log.w("PackageScan", "gallery could not be started", it)
                onResult(null)
            }
        },
    )
}

/** Mean confidence of the words ML Kit read; below this the picture is too blurry or unclear to trust (phone test 28.11.2026: sharp photo 0.81, blurry one 0.39). */
private const val CONFIDENCE_LIMIT = 0.6f

/** How sure ML Kit is about the words it read, 0..1; 1 when nothing was read (the form says so on its own). */
private fun meanConfidence(text: com.google.mlkit.vision.text.Text): Float {
    val values = text.textBlocks.flatMap { block -> block.lines.flatMap { line -> line.elements.map { it.confidence } } }
    return if (values.isEmpty()) 1f else values.average().toFloat()
}
