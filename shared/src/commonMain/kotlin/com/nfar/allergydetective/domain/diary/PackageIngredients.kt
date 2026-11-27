package com.nfar.allergydetective.domain.diary

/**
 * Cuts the ingredient list out of the text read from a package or a recipe (user 27.11.2026: "Zutaten aus der
 * Verpackung lesen"). The text of a photo is messy: line breaks inside words, percentages, quantities, brackets, the
 * nutrition table, "kann Spuren von ... enthalten" or the preparation steps around the list. What comes back is plain
 * words for [VoiceMatcher].
 */
object PackageIngredients {

    private val STARTS = listOf("zutaten", "zutat", "ingredients", "ingrédients", "ingredientes", "içindekiler", "icindekiler", "المكونات")

    /** Where the list ends: the nutrition table, traces, storage, the preparation steps and the like. */
    private val ENDS = listOf(
        "nährwert", "naehrwert", "durchschnittliche", "kann spuren", "kann ", "enthält spuren", "spuren von", "mindestens haltbar", "mindesthaltbar",
        "aufbewahren", "hergestellt", "zubereitung", "anleitung", "notiz", "nutrition", "may contain", "traces", "best before", "store ",
        "preparation", "instructions", "directions", "valeurs nutritionnelles", "peut contenir", "préparation", "informaci", "puede contener",
        "preparación", "besin değerleri", "iz miktarda", "hazırlanışı", "abgefüllt", "vor wärme",
    )

    /** Quantities and labels in front of or between the ingredients that are no ingredients themselves. */
    private val NOISE_WORDS = setOf(
        "el", "tl", "bund", "prise", "stück", "stk", "tasse", "becher", "päckchen", "dose", "zehe", "zehen", "scheibe", "scheiben", "handvoll",
        "glutenfrei", "vegetarisch", "vegan", "laktosefrei", "min", "portionen", "portion", "bio",
        "tbsp", "tsp", "cup", "cups", "pinch", "bunch", "slice", "slices", "cloves", "clove",
    )

    private val hyphenBreak = Regex("""-\s*\n\s*""")
    private val whitespace = Regex("""\s+""")
    private val quantity = Regex("""\d+([.,]\d+)?\s*(%|g|mg|kg|ml|l)(?![\p{L}])""")
    private val number = Regex("""\d+([.,]\d+)?""")
    private val separators = Regex("""[()\[\]{}:;.*/"•·+¹²³]""")
    private val sentenceEnd = Regex("""[\p{L})¹²³]\.(?=\s|$)""")

    fun clean(raw: String): String {
        // "Voll-\nmilch" is one word; any other line break is a space.
        var text = raw.replace(hyphenBreak, "")
        // "Zutaten:" on a package, or a "Zutaten" heading on a line of its own in a recipe.
        val start = STARTS.mapNotNull { marker ->
            Regex("""(?<!\p{L})""" + Regex.escape(marker) + """(?!\p{L})\s*[:：]?""", RegexOption.IGNORE_CASE).find(text)?.range?.last?.plus(1)
        }.minOrNull()
        if (start != null) text = text.substring(start)
        text = text.replace(whitespace, " ").trim()
        // An ingredient list ends with a full stop (not a decimal point): what follows is traces, storage, advice.
        val end = (ENDS.mapNotNull { marker -> text.indexOf(marker, ignoreCase = true).takeIf { it > 0 } } +
            listOfNotNull(sentenceEnd.find(text)?.range?.first?.plus(1)?.takeIf { it > 1 })).minOrNull()
        if (end != null) text = text.substring(0, end)
        return text
            // Percentages and quantities: "Weizenmehl (62 %)", "12,5%", "250 g".
            .replace(quantity, " ")
            .replace(number, " ")
            .replace(separators, ",")
            .split(',')
            .map { segment -> segment.split(' ').filter { it.isNotBlank() && it.lowercase() !in NOISE_WORDS }.joinToString(" ") }
            .filter { it.length >= 2 }
            .joinToString(", ")
    }
}
