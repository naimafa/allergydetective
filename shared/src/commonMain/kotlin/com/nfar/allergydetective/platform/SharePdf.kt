package com.nfar.allergydetective.platform

import androidx.compose.runtime.Composable

/** A simple document the platform turns into a PDF page by page: text blocks, tables and bars. */
sealed interface PdfBlock {
    data class Title(val text: String) : PdfBlock
    data class Heading(val text: String) : PdfBlock
    data class Paragraph(val text: String, val small: Boolean = false) : PdfBlock

    /** First row is the header; [weights] are the relative column widths. */
    data class Table(val rows: List<List<String>>, val weights: List<Float>) : PdfBlock

    /** Horizontal bars, e.g. complaints per part of the day; [value] relative to [max]. */
    data class Bars(val rows: List<BarRow>, val max: Double) : PdfBlock
}

data class BarRow(val label: String, val value: Double, val valueText: String)

/** [footer] is printed on every page together with the page number. */
data class PdfDoc(val blocks: List<PdfBlock>, val footer: String)

/**
 * Renders [PdfDoc] as a PDF named fileName and opens the share sheet (feedback 20.11.:
 * a report the user can take to the doctor). The file stays in the app cache.
 */
@Composable
expect fun rememberSharePdf(): (title: String, fileName: String, doc: PdfDoc) -> Unit
