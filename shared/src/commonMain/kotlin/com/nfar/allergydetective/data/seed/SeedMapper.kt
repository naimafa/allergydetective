package com.nfar.allergydetective.data.seed

import com.nfar.allergydetective.data.db.entity.CategoryEntity
import com.nfar.allergydetective.data.db.entity.IngredientEntity
import com.nfar.allergydetective.data.db.entity.IngredientPropertyEntity
import com.nfar.allergydetective.data.db.entity.IngredientSynonymEntity
import com.nfar.allergydetective.data.db.entity.LifestyleFactorEntity
import com.nfar.allergydetective.data.db.entity.PropertyEntity
import com.nfar.allergydetective.data.db.entity.PropertySymptomGroupEntity
import com.nfar.allergydetective.data.db.entity.PropertyWindowEntity
import com.nfar.allergydetective.data.db.entity.SymptomEntity

/** All rows of the reference tables, ready to insert. */
data class SeedEntities(
    val categories: List<CategoryEntity>,
    val properties: List<PropertyEntity>,
    val propertyWindows: List<PropertyWindowEntity>,
    val propertySymptomGroups: List<PropertySymptomGroupEntity>,
    val ingredients: List<IngredientEntity>,
    val ingredientProperties: List<IngredientPropertyEntity>,
    val ingredientSynonyms: List<IngredientSynonymEntity>,
    val symptoms: List<SymptomEntity>,
    val lifestyleFactors: List<LifestyleFactorEntity>,
)

/**
 * Resolves key references to ids and validates the data.
 * Seed data ships with the app, so an invalid file is a programming error: fail fast.
 */
fun SeedData.toEntities(): SeedEntities {
    requireUnique("category", categories.map { it.id }, categories.map { it.key })
    requireUnique("property", properties.map { it.id }, properties.map { it.key })
    requireUnique("ingredient", ingredients.map { it.id }, ingredients.map { it.key })
    requireUnique("symptom", symptoms.map { it.id }, symptoms.map { it.key })
    requireUnique("lifestyle factor", lifestyleFactors.map { it.id }, lifestyleFactors.map { it.key })

    val categoryIds = categories.associate { it.key to it.id }
    val propertyIds = properties.associate { it.key to it.id }
    val symptomGroups = symptoms.map { it.group }.toSet()

    properties.forEach { p ->
        require(p.windows.isNotEmpty()) { "Property '${p.key}' has no window" }
        p.windows.forEach { w ->
            require(w.startMin in 0..<w.endMin) { "Property '${p.key}' has invalid window $w" }
        }
        p.symptomGroups.forEach { g ->
            require(g in symptomGroups) { "Property '${p.key}' references unknown symptom group '$g'" }
        }
    }

    return SeedEntities(
        categories = categories.map { CategoryEntity(id = it.id, key = it.key) },
        properties = properties.map { PropertyEntity(id = it.id, key = it.key, groupKey = it.group) },
        propertyWindows = properties.flatMap { p ->
            p.windows.map { PropertyWindowEntity(propertyId = p.id, startMin = it.startMin, endMin = it.endMin) }
        },
        propertySymptomGroups = properties.flatMap { p ->
            p.symptomGroups.map { PropertySymptomGroupEntity(propertyId = p.id, symptomGroupKey = it) }
        },
        ingredients = ingredients.map {
            IngredientEntity(
                id = it.id,
                key = it.key,
                customName = null,
                categoryId = categoryIds.lookup(it.category, "category", it.key),
                isCustom = false,
                baseIngredientId = null,
            )
        },
        ingredientProperties = ingredients.flatMap { i ->
            i.properties.map { IngredientPropertyEntity(ingredientId = i.id, propertyId = propertyIds.lookup(it, "property", i.key)) }
        },
        ingredientSynonyms = ingredients.flatMap { i ->
            i.synonyms.flatMap { (locale, words) ->
                words.map { IngredientSynonymEntity(ingredientId = i.id, locale = locale, synonym = it) }
            }
        },
        symptoms = symptoms.map { SymptomEntity(id = it.id, key = it.key, groupKey = it.group, isRedFlag = it.isRedFlag) },
        lifestyleFactors = lifestyleFactors.map { LifestyleFactorEntity(id = it.id, key = it.key) },
    )
}

private fun requireUnique(name: String, ids: List<Long>, keys: List<String>) {
    val duplicateIds = ids.groupingBy { it }.eachCount().filterValues { it > 1 }.keys
    val duplicateKeys = keys.groupingBy { it }.eachCount().filterValues { it > 1 }.keys
    require(duplicateIds.isEmpty()) { "Duplicate $name ids: $duplicateIds" }
    require(duplicateKeys.isEmpty()) { "Duplicate $name keys: $duplicateKeys" }
}

private fun Map<String, Long>.lookup(key: String, type: String, owner: String): Long =
    requireNotNull(this[key]) { "Ingredient '$owner' references unknown $type '$key'" }
