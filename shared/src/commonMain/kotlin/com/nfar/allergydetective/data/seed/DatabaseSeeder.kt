package com.nfar.allergydetective.data.seed

import com.nfar.allergydetective.data.db.dao.SeedDao

sealed interface SeedOutcome {
    data object AlreadySeeded : SeedOutcome
    data class Seeded(val ingredients: Int, val symptoms: Int) : SeedOutcome
}

/**
 * Fills the reference tables on first start (empty database), in one transaction.
 * Later changes to the seed data are shipped as Room migrations.
 */
class DatabaseSeeder(
    private val dao: SeedDao,
    private val source: SeedSource = ComposeResourceSeedSource(),
) {
    suspend fun seedIfEmpty(): SeedOutcome {
        if (dao.countCategories() > 0) return SeedOutcome.AlreadySeeded
        val entities = SeedParser.parse(source.read()).toEntities()
        dao.insertAll(entities)
        return SeedOutcome.Seeded(ingredients = entities.ingredients.size, symptoms = entities.symptoms.size)
    }
}
