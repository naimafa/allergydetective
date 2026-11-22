package com.nfar.allergydetective.ui

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Languages (user note 21.11.2026): every translation has every text of the English base,
 * with the same placeholders, so a missing translation or a broken %1$d shows up in the tests.
 */
class TranslationsTest {

    private val resourcesDir: File = listOf("src/commonMain/composeResources", "shared/src/commonMain/composeResources")
        .map(::File)
        .first { it.isDirectory }

    /** Old JSON export texts, no longer shown anywhere. */
    private val unused = setOf("export_title", "export_hint", "export_button")

    private val entry = Regex("""<(string|plurals|string-array) name="([a-z0-9_]+)"[^>]*>(.*?)</\1>""", RegexOption.DOT_MATCHES_ALL)
    private val placeholder = Regex("""%\d\$[ds]""")

    private fun read(folder: String, file: String): Map<String, Set<String>> =
        entry.findAll(File(resourcesDir, "$folder/$file").readText())
            .associate { it.groupValues[2] to placeholder.findAll(it.groupValues[3]).map { p -> p.value }.toSet() }

    @Test
    fun everyLanguageHasEveryText() {
        listOf("ui_strings.xml", "seed_strings.xml", "quotes.xml").forEach { file ->
            val base = read("values", file).filterKeys { it !in unused }
            listOf("values-de", "values-fr", "values-ar").forEach { folder ->
                val translated = read(folder, file)
                val missing = base.keys - translated.keys
                assertTrue(missing.isEmpty(), "$folder/$file is missing $missing")
                base.forEach { (name, args) ->
                    // Arabic plurals may drop the number in "one"/"two" forms, so only check that no unknown placeholder appears.
                    val got = translated.getValue(name)
                    if (folder == "values-ar") {
                        assertTrue(args.containsAll(got), "$folder/$file $name has placeholders $got, expected $args")
                    } else {
                        assertEquals(args, got, "$folder/$file $name placeholders")
                    }
                }
            }
        }
    }
}
