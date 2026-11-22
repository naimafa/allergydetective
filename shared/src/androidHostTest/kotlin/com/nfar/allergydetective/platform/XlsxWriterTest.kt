package com.nfar.allergydetective.platform

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipInputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** The Excel export (user note 21.11.2026) without a library: a valid workbook with one worksheet per sheet. */
class XlsxWriterTest {

    private fun entries(bytes: ByteArray): Map<String, String> {
        val result = mutableMapOf<String, String>()
        ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            generateSequence { zip.nextEntry }.forEach { result[it.name] = zip.readBytes().toString(Charsets.UTF_8) }
        }
        return result
    }

    @Test
    fun workbookHasOneWorksheetPerSheetAndEscapesText() {
        val out = ByteArrayOutputStream()
        writeXlsx(
            out,
            listOf(
                Sheet("Mahlzeiten", listOf(listOf("Datum", "Zutaten"), listOf("21.11.2026", "Brot & Butter <frisch>"))),
                Sheet("Beschwerden", listOf(listOf("Datum"))),
            ),
        )
        val files = entries(out.toByteArray())

        assertTrue("[Content_Types].xml" in files)
        assertTrue("xl/workbook.xml" in files)
        assertTrue("xl/worksheets/sheet1.xml" in files)
        assertTrue("xl/worksheets/sheet2.xml" in files)
        assertTrue("name=\"Mahlzeiten\"" in files.getValue("xl/workbook.xml"))
        assertTrue("Brot &amp; Butter &lt;frisch&gt;" in files.getValue("xl/worksheets/sheet1.xml"))
        assertTrue("r=\"B2\"" in files.getValue("xl/worksheets/sheet1.xml"))
    }

    @Test
    fun columnNamesAndSheetNames() {
        assertEquals("A", columnName(0))
        assertEquals("Z", columnName(25))
        assertEquals("AA", columnName(26))
        assertEquals(listOf("Auswertung", "Auswertung 2", "a b"), uniqueSheetNames(listOf("Auswertung", "Auswertung", "a/b")))
    }
}
