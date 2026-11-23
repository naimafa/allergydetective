package com.nfar.allergydetective.domain.diary

/**
 * Self-test 19.11.: with 15 or 30 dishes the list gets long. A dish matches when its name or one of
 * its ingredients contains [query] (case, umlauts and accents ignored). A blank query matches all.
 */
fun FavoriteInfo.matches(query: String, ingredientNames: Map<Long, String>): Boolean {
    val needle = normalizeForSearch(query.trim())
    if (needle.isEmpty()) return true
    if (needle in normalizeForSearch(name)) return true
    return items.any { item -> ingredientNames[item.ingredientId]?.let { needle in normalizeForSearch(it) } == true }
}

/**
 * Why a dish was found (user feedback 21.11.: "Linsensuppe" appeared for "to" without a visible reason):
 * null when its name matches, otherwise the name of the first ingredient that contains [query].
 */
fun FavoriteInfo.matchingIngredient(query: String, ingredientNames: Map<Long, String>): String? {
    val needle = normalizeForSearch(query.trim())
    if (needle.isEmpty() || needle in normalizeForSearch(name)) return null
    return items.firstNotNullOfOrNull { item -> ingredientNames[item.ingredientId]?.takeIf { needle in normalizeForSearch(it) } }
}
