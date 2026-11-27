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
        "gemahlene", "gemahlener", "gemahlenes", "gemahlen", "eine", "weizenart", "aus", "kontrolliert", "biologischem", "anbau", "enthält", "enthaelt", "enthalt", "enthalten", "aroma", "aromen",
    )

    /** "Säuerungsmittel (Kaliumtartrate)": the class of an additive and its chemical name, nothing to eat. */
    private val additiveClass = Regex(
        """(säuerungsmittel|backtriebmittel|triebmittel|emulgator|konservierungsstoff|farbstoff|antioxidationsmittel|verdickungsmittel|geschmacksverstärker|stabilisator|festigungsmittel|überzugsmittel|trennmittel|feuchthaltemittel|acidity regulator|raising agents?|emulsifiers?|preservatives?|colou?rs?|antioxidants?|thickeners?|stabili[sz]ers?)\s*\((?:[^()]|\([^()]*\))*\)""",
        RegexOption.IGNORE_CASE,
    )

    private val additiveStem = Regex(
        """(?<!\p{L})(backtrieb|triebmittel|säuerungsm|sauerungsm|emulgator|konservierungsst|farbstoff|antioxidationsm|verdickungsm|geschmacksverst|stabilisator|festigungsm|überzugsm|trennmittel|feuchthaltem)\p{L}*""",
        RegexOption.IGNORE_CASE,
    )
    /** Chemical names of additives, also as the camera garbles them ("Natrumcartnate"): nothing to eat, no new ingredient. */
    private val chemical = Regex(
        """(?<!\p{L})[\p{L}-]*(natr[iu]m|kalium|calcium|magnesium|carbon|carbnat|cartnat|tartrat|phosphat|citrat|sorbat|benzoat|lacton|glutamat|säure|saeure|sulfit|nitrit)[\p{L}-]*""",
        RegexOption.IGNORE_CASE,
    )
    private val hyphenBreak = Regex("""-\s*\n\s*""")
    private val whitespace = Regex("""\s+""")
    private val quantity = Regex("""\d+([.,]\d+)?\s*(%|g|mg|kg|ml|l)(?![\p{L}])""")
    private val number = Regex("""\d+([.,]\d+)?""")
    // The footnote mark "¹" is often read as an apostrophe.
    private val separators = Regex("""[()\[\]{}:;.*/"'’‘´`•·+¹²³]""")
    private val sentenceEnd = Regex("""[\p{L})¹²³]\.(?=\s|$)""")

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
        text = text.replace(whitespace, " ").trim()
        // An ingredient list ends with a full stop (not a decimal point): what follows is traces, storage, advice.
        val end = (ENDS.mapNotNull { marker -> text.indexOf(marker, ignoreCase = true).takeIf { it > 0 } } +
            listOfNotNull(sentenceEnd.find(text)?.range?.first?.plus(1)?.takeIf { it > 1 })).minOrNull()
        if (end != null) text = text.substring(0, end)
        return text
            // Percentages and quantities: "Weizenmehl (62 %)", "12,5%", "250 g".
            .replace(additiveClass, " ")
            // The camera often cuts the end of a word at the edge: "Backtriebmit" is still the class of an additive.
            .replace(additiveStem, " ")
            .replace(chemical, " ")
            .replace(quantity, " ")
            .replace(number, " ")
            .replace(separators, ",")
            .split(',')
            .map { segment -> segment.split(' ').filter { it.isNotBlank() && it.lowercase() !in NOISE_WORDS }.joinToString(" ") }
            .filter { it.length >= 2 }
            .joinToString(", ")
    }
}
