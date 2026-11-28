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
        // User 28.11.2026: a blurry picture is reported with the text, the form warns.
        val blurry = isBlurry(context, uri)
        runCatching { InputImage.fromFilePath(context, uri) }
            .onSuccess { image ->
                recognizer.process(image)
                    .addOnSuccessListener {
                        Log.i("PackageScan", "read ${it.text.length} characters: ${it.text.lines().joinToString("|")}")
                        // Empty text (not null) tells the form that the photo was taken but nothing could be read.
                        onResult(it.text, blurry)
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

/** Below this the picture is too soft for a reliable reading. Logged with every picture, to tune it on real photos. */
private const val BLUR_LIMIT = 40.0
private const val SHARPNESS_SIDE = 1000

/**
 * Sharpness of the picture as the variance of its Laplacian (grey values, picture scaled to about 1000 px): sharp text
 * has many strong edges, a blurry one hardly any. Null result (not readable) counts as not blurry.
 */
private fun isBlurry(context: android.content.Context, uri: Uri): Boolean {
    val variance = runCatching { laplacianVariance(context, uri) }.getOrNull() ?: return false
    Log.i("PackageScan", "sharpness=$variance (limit $BLUR_LIMIT)")
    return variance < BLUR_LIMIT
}

private fun laplacianVariance(context: android.content.Context, uri: Uri): Double? {
    val resolver = context.contentResolver
    val bounds = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
    resolver.openInputStream(uri)?.use { android.graphics.BitmapFactory.decodeStream(it, null, bounds) }
    val longSide = maxOf(bounds.outWidth, bounds.outHeight)
    if (longSide <= 0) return null
    val options = android.graphics.BitmapFactory.Options().apply { inSampleSize = (longSide / SHARPNESS_SIDE).coerceAtLeast(1) }
    val bitmap = resolver.openInputStream(uri)?.use { android.graphics.BitmapFactory.decodeStream(it, null, options) } ?: return null
    val w = bitmap.width
    val h = bitmap.height
    if (w < 3 || h < 3) return null
    val pixels = IntArray(w * h)
    bitmap.getPixels(pixels, 0, w, 0, 0, w, h)
    bitmap.recycle()
    val grey = DoubleArray(w * h) { i ->
        val p = pixels[i]
        0.299 * ((p shr 16) and 0xFF) + 0.587 * ((p shr 8) and 0xFF) + 0.114 * (p and 0xFF)
    }
    var sum = 0.0
    var sumSquares = 0.0
    var count = 0
    for (y in 1 until h - 1) {
        for (x in 1 until w - 1) {
            val i = y * w + x
            val laplacian = grey[i - 1] + grey[i + 1] + grey[i - w] + grey[i + w] - 4 * grey[i]
            sum += laplacian
            sumSquares += laplacian * laplacian
            count++
        }
    }
    val mean = sum / count
    return sumSquares / count - mean * mean
}
