package com.nfar.allergydetective.platform

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File

private const val MIME_PDF = "application/pdf"
private const val EXPORT_DIR = "export"

@Composable
actual fun rememberSharePdf(): (title: String, fileName: String, doc: PdfDoc) -> Unit {
    val context = LocalContext.current
    return { title, fileName, doc ->
        val file = writePdf(context, fileName, doc)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = MIME_PDF
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_STREAM, uri)
            clipData = ClipData.newRawUri(fileName, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(send, title))
    }
}

/** Cache only, like the JSON export: the previous file is replaced. */
private fun writePdf(context: Context, fileName: String, doc: PdfDoc): File {
    val dir = File(context.cacheDir, EXPORT_DIR).apply { mkdirs() }
    dir.listFiles()?.forEach { it.delete() }
    val file = File(dir, fileName)
    val pdf = PdfDocument()
    try {
        PdfWriter(pdf, doc.footer).apply {
            doc.blocks.forEach(::draw)
            finish()
        }
        file.outputStream().use(pdf::writeTo)
    } finally {
        pdf.close()
    }
    return file
}

/** Lays the blocks out on A4 pages (points), top to bottom, with a new page when one is full. */
private class PdfWriter(private val pdf: PdfDocument, private val footer: String) {
    private val pageWidth = 595
    private val pageHeight = 842
    private val margin = 48f
    private val contentWidth = pageWidth - 2 * margin
    private val bottom = pageHeight - margin - 20f

    private val green = Color.rgb(0x2F, 0x5D, 0x4A)
    private val light = Color.rgb(0xA8, 0xCB, 0xB4)
    private val stripe = Color.rgb(0xF2, 0xF0, 0xE8)
    private val text = Color.rgb(0x29, 0x38, 0x31)
    private val muted = Color.rgb(0x5E, 0x6B, 0x64)

    private val titlePaint = paint(20f, bold = true, color = green)
    private val headingPaint = paint(13f, bold = true, color = green)
    private val bodyPaint = paint(10.5f)
    private val smallPaint = paint(9f, color = muted)
    private val boldPaint = paint(10f, bold = true)
    private val cellPaint = paint(10f)
    private val fill = Paint().apply { isAntiAlias = true }

    private var pageNumber = 0
    private var page: PdfDocument.Page? = null
    private lateinit var canvas: Canvas
    private var y = 0f

    init {
        newPage()
    }

    private fun paint(size: Float, bold: Boolean = false, color: Int = text) = TextPaint().apply {
        isAntiAlias = true
        textSize = size
        this.color = color
        typeface = Typeface.create(Typeface.SANS_SERIF, if (bold) Typeface.BOLD else Typeface.NORMAL)
    }

    private fun newPage() {
        page?.let(::closePage)
        pageNumber++
        page = pdf.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
        canvas = page!!.canvas
        y = margin
    }

    private fun closePage(current: PdfDocument.Page) {
        val line = "$footer · $pageNumber"
        current.canvas.drawText(line, margin, pageHeight - margin + 8f, smallPaint)
        pdf.finishPage(current)
    }

    fun finish() {
        page?.let(::closePage)
        page = null
    }

    private fun layout(value: String, paint: TextPaint, width: Float): StaticLayout =
        StaticLayout.Builder.obtain(value, 0, value.length, paint, width.toInt().coerceAtLeast(1))
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(1.5f, 1f)
            .build()

    private fun ensure(height: Float) {
        if (y + height > bottom) newPage()
    }

    private fun drawLayout(layout: StaticLayout, x: Float, top: Float) {
        canvas.save()
        canvas.translate(x, top)
        layout.draw(canvas)
        canvas.restore()
    }

    private fun text(value: String, paint: TextPaint, spaceBefore: Float, spaceAfter: Float) {
        val l = layout(value, paint, contentWidth)
        ensure(spaceBefore + l.height + spaceAfter)
        y += spaceBefore
        drawLayout(l, margin, y)
        y += l.height + spaceAfter
    }

    fun draw(block: PdfBlock) {
        when (block) {
            is PdfBlock.Title -> {
                text(block.text, titlePaint, 0f, 6f)
                fill.color = light
                canvas.drawRect(margin, y, margin + contentWidth, y + 2f, fill)
                y += 10f
            }
            // A heading never stays alone at the bottom of a page.
            is PdfBlock.Heading -> {
                ensure(60f)
                text(block.text, headingPaint, 12f, 6f)
            }
            is PdfBlock.Paragraph -> text(block.text, if (block.small) smallPaint else bodyPaint, 0f, 6f)
            is PdfBlock.Table -> table(block)
            is PdfBlock.Bars -> bars(block)
        }
    }

    private fun table(table: PdfBlock.Table) {
        val total = table.weights.sum()
        val widths = table.weights.map { it / total * contentWidth }
        val pad = 4f
        table.rows.forEachIndexed { index, row ->
            val header = index == 0
            val layouts = row.mapIndexed { column, cell ->
                layout(cell, if (header) boldPaint else cellPaint, widths[column] - 2 * pad)
            }
            val height = layouts.maxOf { it.height } + 2 * pad
            ensure(height)
            if (header || index % 2 == 0) {
                fill.color = if (header) light else stripe
                canvas.drawRect(margin, y, margin + contentWidth, y + height, fill)
            }
            var x = margin
            layouts.forEachIndexed { column, l ->
                drawLayout(l, x + pad, y + pad)
                x += widths[column]
            }
            y += height
        }
        y += 8f
    }

    private fun bars(block: PdfBlock.Bars) {
        val labelWidth = contentWidth * 0.32f
        val valueWidth = contentWidth * 0.18f
        val barWidth = contentWidth - labelWidth - valueWidth - 8f
        val rowHeight = 18f
        block.rows.forEach { row ->
            ensure(rowHeight)
            val baseline = y + 12f
            canvas.drawText(row.label, margin, baseline, cellPaint)
            fill.color = stripe
            canvas.drawRect(margin + labelWidth, y + 3f, margin + labelWidth + barWidth, y + rowHeight - 3f, fill)
            val share = if (block.max > 0) (row.value / block.max).coerceIn(0.0, 1.0).toFloat() else 0f
            fill.color = green
            canvas.drawRect(margin + labelWidth, y + 3f, margin + labelWidth + barWidth * share, y + rowHeight - 3f, fill)
            canvas.drawText(row.valueText, margin + labelWidth + barWidth + 8f, baseline, cellPaint)
            y += rowHeight
        }
        y += 8f
    }
}
