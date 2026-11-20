package com.nfar.allergydetective.platform

import androidx.compose.runtime.Composable

// Not built yet (no Mac). UIGraphicsPDFRenderer + UIActivityViewController follow with the iOS build.
@Composable
actual fun rememberSharePdf(): (title: String, fileName: String, doc: PdfDoc) -> Unit = { _, _, _ -> }
