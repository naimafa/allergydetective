package com.nfar.allergydetective.platform

import android.content.ClipData
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

private const val MIME_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
private const val EXPORT_DIR = "export"

@Composable
actual fun rememberShareSpreadsheet(): (title: String, fileName: String, sheets: List<Sheet>) -> Unit {
    val context = LocalContext.current
    return { title, fileName, sheets ->
        // Cache only, like the PDF: the previous file is replaced.
        val dir = File(context.cacheDir, EXPORT_DIR).apply { mkdirs() }
        dir.listFiles()?.forEach { it.delete() }
        val file = File(dir, fileName)
        file.outputStream().use { writeXlsx(it, sheets) }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = MIME_XLSX
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_STREAM, uri)
            clipData = ClipData.newRawUri(fileName, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(send, title))
    }
}

/**
 * A minimal Office Open XML workbook without a library: one worksheet per [Sheet], text cells as inline
 * strings, the first row bold and frozen, sensible column widths. Opens in Excel, LibreOffice and Google Sheets.
 */
internal fun writeXlsx(out: OutputStream, sheets: List<Sheet>) {
    ZipOutputStream(out).use { zip ->
        fun put(name: String, xml: String) {
            zip.putNextEntry(ZipEntry(name))
            zip.write(xml.toByteArray(Charsets.UTF_8))
            zip.closeEntry()
        }
        val names = uniqueSheetNames(sheets.map { it.name })
        put(
            "[Content_Types].xml",
            """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
<Default Extension="xml" ContentType="application/xml"/>
<Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
<Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
${sheets.indices.joinToString("\n") { """<Override PartName="/xl/worksheets/sheet${it + 1}.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>""" }}
</Types>""",
        )
        put(
            "_rels/.rels",
            """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>""",
        )
        put(
            "xl/workbook.xml",
            """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
<sheets>
${names.indices.joinToString("\n") { """<sheet name="${escape(names[it])}" sheetId="${it + 1}" r:id="rId${it + 1}"/>""" }}
</sheets>
</workbook>""",
        )
        put(
            "xl/_rels/workbook.xml.rels",
            """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
${sheets.indices.joinToString("\n") { """<Relationship Id="rId${it + 1}" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet${it + 1}.xml"/>""" }}
<Relationship Id="rId${sheets.size + 1}" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
</Relationships>""",
        )
        // Style 0 = normal, 1 = bold (header), 2 = bold on a tinted band (day rows), all wrapping text.
        put(
            "xl/styles.xml",
            """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
<fonts count="2"><font><sz val="11"/><name val="Calibri"/></font><font><b/><sz val="11"/><name val="Calibri"/></font></fonts>
<fills count="3"><fill><patternFill patternType="none"/></fill><fill><patternFill patternType="gray125"/></fill><fill><patternFill patternType="solid"><fgColor rgb="FFDDE8D5"/><bgColor indexed="64"/></patternFill></fill></fills>
<borders count="1"><border><left/><right/><top/><bottom/><diagonal/></border></borders>
<cellStyleXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0"/></cellStyleXfs>
<cellXfs count="3">
<xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0" applyAlignment="1"><alignment vertical="top" wrapText="1"/></xf>
<xf numFmtId="0" fontId="1" fillId="0" borderId="0" xfId="0" applyFont="1" applyAlignment="1"><alignment vertical="top" wrapText="1"/></xf>
<xf numFmtId="0" fontId="1" fillId="2" borderId="0" xfId="0" applyFont="1" applyFill="1" applyAlignment="1"><alignment vertical="top" wrapText="1"/></xf>
</cellXfs>
</styleSheet>""",
        )
        sheets.forEachIndexed { index, sheet -> put("xl/worksheets/sheet${index + 1}.xml", worksheet(sheet)) }
    }
}

private fun worksheet(sheet: Sheet): String {
    val columns = sheet.rows.maxOfOrNull { it.size } ?: 0
    // Width from the longest text in the column, between 8 and 60 characters.
    val widths = (0 until columns).map { col ->
        (sheet.rows.maxOfOrNull { it.getOrNull(col)?.length ?: 0 } ?: 0).coerceIn(8, 60) + 2
    }
    val cols = widths.mapIndexed { i, w -> """<col min="${i + 1}" max="${i + 1}" width="$w" customWidth="1"/>""" }.joinToString("")
    val rows = sheet.rows.mapIndexed { r, row ->
        val style = if (r == 0) 1 else if (r in sheet.emphasisRows) 2 else 0
        val cells = row.mapIndexed { c, value ->
            """<c r="${columnName(c)}${r + 1}" t="inlineStr" s="$style"><is><t xml:space="preserve">${escape(value)}</t></is></c>"""
        }.joinToString("")
        """<row r="${r + 1}">$cells</row>"""
    }.joinToString("\n")
    return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
<sheetViews><sheetView workbookViewId="0"><pane ySplit="1" topLeftCell="A2" activePane="bottomLeft" state="frozen"/></sheetView></sheetViews>
${if (cols.isNotEmpty()) "<cols>$cols</cols>" else ""}
<sheetData>
$rows
</sheetData>
</worksheet>"""
}

/** A, B, …, Z, AA, AB, … */
internal fun columnName(index: Int): String {
    var n = index + 1
    val name = StringBuilder()
    while (n > 0) {
        val rem = (n - 1) % 26
        name.insert(0, 'A' + rem)
        n = (n - 1) / 26
    }
    return name.toString()
}

/** Excel sheet names: at most 31 characters, none of []:*?/\ and unique. */
internal fun uniqueSheetNames(names: List<String>): List<String> {
    val used = mutableSetOf<String>()
    return names.map { raw ->
        val base = raw.replace(Regex("""[\[\]:*?/\\]"""), " ").take(31).ifBlank { "Sheet" }
        var name = base
        var i = 2
        while (!used.add(name.lowercase())) name = base.take(28) + " " + i++
        name
    }
}

private fun escape(text: String): String = buildString(text.length) {
    for (ch in text) {
        when {
            ch == '&' -> append("&amp;")
            ch == '<' -> append("&lt;")
            ch == '>' -> append("&gt;")
            ch == '"' -> append("&quot;")
            // Control characters are not allowed in XML 1.0.
            ch < ' ' && ch != '\n' && ch != '\t' -> append(' ')
            else -> append(ch)
        }
    }
}
