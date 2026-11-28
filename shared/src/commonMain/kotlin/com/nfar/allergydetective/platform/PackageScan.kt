package com.nfar.allergydetective.platform

import androidx.compose.runtime.Composable

/** The two ways to get the picture of an ingredient list: a new photo, or one already in the gallery (user 28.11.2026). */
class PackageScanStarters(val takePhoto: () -> Unit, val pickFromGallery: () -> Unit)

/**
 * Reads the ingredient list on a package from a photo and reports the text read from it (user 27.11.2026), or null when
 * the user cancelled or nothing was read. The photo never leaves the phone. Returns null where this is not available:
 * the camera button is hidden then.
 */
@Composable
expect fun rememberPackageScan(onResult: (text: String?) -> Unit): PackageScanStarters?
