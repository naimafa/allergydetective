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
        // "gemahlene Bourbon-Vanille" is not "gemahlene Mandeln"; "eine Weizenart" says what the flour is, not another one.
        "gemahlene", "gemahlener", "gemahlenes", "gemahlen", "eine", "weizenart", "aus", "kontrolliert", "biologischem", "anbau", "enthält", "enthaelt", "enthalt", "enthalten",
    )

    /** The innermost brackets: "Backtriebmittel (Natriumcarbonate)", "Aroma (enthält Gerste)". */
    private val innermostBrackets = Regex("""\([^()]*\)""")
    /** Words of the producer's line: company forms and distribution. */
    private val COMPANY = listOf("nahrungsmittel", "gmbh", " kg ", "vertrieb", "inverkehrbringer", "importiert", "www.", "hotline", "kundenservice")
    private val postalCode = Regex("""(?<!\d)\d{5}(?!\d)""")
    private val hyphenBreak = Regex("""-\s*\n\s*""")
    private val whitespace = Regex("""\s+""")
    private val quantity = Regex("""\d+([.,]\d+)?\s*(%|g|mg|kg|ml|l)(?![\p{L}])""")
    private val number = Regex("""\d+([.,]\d+)?""")
    // The footnote mark "¹" is often read as an apostrophe.
    private val separators = Regex("""[()\[\]{}:;.*/"'’‘´`•·+¹²³]""")
    private val sentenceEnd = Regex("""[\p{L})¹²³]\.(?=\s|$)""")

    /**
     * "Backtriebmittel (Natriumcarbonate)" is the entry "Backtriebmittel"; "Aroma (enthält Gerste)" is "Aroma" (user
     * 27.11.2026: the entries as the package lists them). A bracket without its partner, as the camera loses them,
     * only separates.
     */
    private fun withoutBrackets(text: String): String {
        var result = text
        while (true) {
            val next = result.replace(innermostBrackets, " ")
            if (next == result) break
            result = next
        }
        return result.replace('(', ',').replace(')', ' ')
    }

    fun clean(raw: String): String {
        // "Voll-\nmilch" is one word; any other line break is a space.
        var text = raw.replace(hyphenBreak, "")
        // "Zutaten:" on a package, or a "Zutaten" heading on a line of its own in a recipe.
        // A marketing text can say "...wertvolle Zutaten." earlier on: the colon, or a line of its own, makes it the heading.
        fun startAfter(tail: String) = STARTS.mapNotNull { marker ->
            Regex("""(?<!\p{L})""" + Regex.escape(marker) + tail, RegexOption.IGNORE_CASE).find(text)?.range?.last?.plus(1)
        }.minOrNull()
        val start = startAfter("""(?!\p{L})[ \t]*[:：]""") ?: startAfter("""[ \t]*(?=\n)""")
        // No "Zutaten:" and nothing that looks like a list (commas): the front of a package or a text, no guessing.
        if (start == null && text.count { it == ',' } < 2) return ""
        if (start != null) text = text.substring(start)
        // The line of the producer's address ends the list as well, even when "Kann enthalten" was not read.
        val addressAt = (COMPANY.mapNotNull { text.indexOf(it, ignoreCase = true).takeIf { at -> at > 0 } } +
            listOfNotNull(postalCode.find(text)?.range?.first?.takeIf { it > 0 })).minOrNull()
        if (addressAt != null) text = text.substring(0, text.lastIndexOf('\n', addressAt).coerceAtLeast(0))
        text = text.replace(whitespace, " ").trim()
        // An ingredient list ends with a full stop (not a decimal point): what follows is traces, storage, advice.
        val end = (ENDS.mapNotNull { marker -> text.indexOf(marker, ignoreCase = true).takeIf { it > 0 } } +
            listOfNotNull(sentenceEnd.find(text)?.range?.first?.plus(1)?.takeIf { it > 1 })).minOrNull()
        if (end != null) text = text.substring(0, end)
        return text
            // What stands in brackets only says more about the entry in front of it, the entry is what the package lists.
            .let(::withoutBrackets)
            // Percentages and quantities: "12,5%", "250 g".
            .replace(quantity, " ")
            .replace(number, " ")
            .replace(separators, ",")
            .split(',')
            .map { segment -> segment.split(' ').filter { it.isNotBlank() && it.lowercase() !in NOISE_WORDS }.joinToString(" ") }
            .filter { it.length >= 2 }
            .joinToString(", ")
    }
}
