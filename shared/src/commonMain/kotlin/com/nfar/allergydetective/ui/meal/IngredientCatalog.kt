package com.nfar.allergydetective.ui.meal

import com.nfar.allergydetective.domain.diary.IngredientInfo
import com.nfar.allergydetective.domain.diary.IngredientSearch
import com.nfar.allergydetective.domain.diary.LanguageProvider
import com.nfar.allergydetective.domain.diary.MealRepository
import com.nfar.allergydetective.domain.diary.SearchEntry
import com.nfar.allergydetective.ui.common.DisplayNameResolver
import com.nfar.allergydetective.ui.common.NamePrefix

/** All ingredients (seed and custom) with the search over names and synonyms of the app language. */
class IngredientCatalog(
    val byId: Map<Long, IngredientInfo>,
    private val search: IngredientSearch,
) {
    fun search(query: String, limit: Int = MAX_RESULTS): List<IngredientInfo> =
        search.search(query, limit).mapNotNull(byId::get)

    /** Closest ingredient to [name] and its score: 0 = same name or synonym, 1–3 = part of a name, 4 = typo. */
    fun bestMatch(name: String): Pair<IngredientInfo, Int>? =
        search.bestMatch(name)?.let { (id, score) -> byId[id]?.let { it to score } }

    companion object {
        const val MAX_RESULTS = 6
        val EMPTY = IngredientCatalog(emptyMap(), IngredientSearch(emptyList()))

        suspend fun load(meals: MealRepository, names: DisplayNameResolver, language: LanguageProvider): IngredientCatalog {
            val ingredients = meals.ingredients()
            val synonyms = meals.synonyms(language.language())
            val search = IngredientSearch(
                ingredients.map { ingredient ->
                    val name = ingredient.key?.let { names.name(NamePrefix.INGREDIENT, it) } ?: ingredient.customName.orEmpty()
                    SearchEntry(ingredient.id, name, synonyms[ingredient.id].orEmpty())
                },
            )
            return IngredientCatalog(ingredients.associateBy { it.id }, search)
        }
    }
}
