package com.nfar.allergydetective.data.seed

import allergydetective.shared.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi

/** Provides the raw seed files. An interface so tests can feed their own data. */
fun interface SeedSource {
    suspend fun read(): RawSeed
}

/** Reads the seed JSON shipped in composeResources/files/seed. */
class ComposeResourceSeedSource : SeedSource {
    override suspend fun read(): RawSeed = RawSeed(
        categories = file("categories.json"),
        properties = file("properties.json"),
        ingredients = file("ingredients.json"),
        symptoms = file("symptoms.json"),
        lifestyleFactors = file("lifestyle_factors.json"),
    )

    @OptIn(ExperimentalResourceApi::class)
    private suspend fun file(name: String): String = Res.readBytes("files/seed/$name").decodeToString()
}
