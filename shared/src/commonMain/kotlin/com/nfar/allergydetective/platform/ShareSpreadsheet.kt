package com.nfar.allergydetective.platform

import androidx.compose.runtime.Composable

/** One sheet of an Excel file: a name for the tab and its rows (first row = header, shown bold; [emphasisRows] are bold on a tinted band). */
data class Sheet(val name: String, val rows: List<List<String>>, val emphasisRows: Set<Int> = emptySet())

/**
 * Writes the sheets as an Excel file (.xlsx) named fileName and opens the share sheet (user note 21.11.2026:
 * "für den User eher Excel" instead of JSON). The file stays in the app cache, like the PDF.
 */
@Composable
expect fun rememberShareSpreadsheet(): (title: String, fileName: String, sheets: List<Sheet>) -> Unit
