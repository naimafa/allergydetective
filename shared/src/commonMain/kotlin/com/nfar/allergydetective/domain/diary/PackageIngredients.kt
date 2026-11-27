package com.nfar.allergydetective.domain.diary

/**
 * Cuts the ingredient list out of the text read from a package (user 27.11.2026: "Zutaten aus der Verpackung lesen").
 * The text of a photo is messy: line breaks inside words, percentages, brackets, the nutrition table and
 * "kann Spuren von ... enthalten" around the list. What comes back is plain words for [VoiceMatcher].
 */
object PackageIngredients {

    private val STARTS = listOf("zutaten", "zutat", "ingredients", "ingrédients", "ingredientes", "içindekiler", "icindekiler", "المكونات")

    /** Where the list ends: the nutrition table, traces, storage and the like. */
    private val ENDS = listOf(
        "nährwert", "naehrwert", "durchschnittliche", "kann spuren", "kann ", "enthält spuren", "spuren von", "mindestens haltbar", "mindesthaltbar",
        "aufbewahren", "hergestellt", "nutrition", "may contain", "traces", "best before", "store ", "valeurs nutritionnelles", "peut contenir",
        "informaci", "puede contener", "besin değerleri", "iz miktarda", "abgefüllt", "vor wärme",
    )

    private val hyphenBreak = Regex("""-\s*\n\s*""")
    private val whitespace = Regex("""\s+""")
    private val quantity = Regex("""\d+([.,]\d+)?\s*(%|g|mg|kg|ml|l)(?![\p{L}])""")
    private val number = Regex("""\d+([.,]\d+)?""")
    private val separators = Regex("""[()\[\]{}:;.*/"]""")

    fun clean(raw: String): String {
        // "Voll-\nmilch" is one word; any other line break is a space.
        var text = raw.replace(hyphenBreak, "").replace(whitespace, " ").trim()
        val lower = text.lowercase()
        val start = STARTS.mapNotNull { marker -> Regex(Regex.escape(marker) + """\s*[:：]""").find(lower)?.let { it.range.last + 1 } }.minOrNull()
        if (start != null) text = text.substring(start)
        val end = ENDS.mapNotNull { marker -> text.lowercase().indexOf(marker).takeIf { it > 0 } }.minOrNull()
        if (end != null) text = text.substring(0, end)
        return text
            // Percentages and quantities: "Weizenmehl (62 %)", "12,5%", "250 g".
            .replace(quantity, " ")
            .replace(number, " ")
            .replace(separators, ",")
            .split(',')
            .map { it.trim() }
            .filter { it.length >= 2 }
            .joinToString(", ")
    }
}
