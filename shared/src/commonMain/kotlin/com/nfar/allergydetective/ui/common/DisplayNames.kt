package com.nfar.allergydetective.ui.common

import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.allStringResources
import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

/**
 * Display names of seed entries. The database only stores stable keys (CODING_RICHTLINIEN 6),
 * the names come from string resources named "<prefix>_<key>", e.g. "ingredient_cow_milk".
 */
enum class NamePrefix(val value: String) {
    INGREDIENT("ingredient"),
    PROPERTY("property"),
    SYMPTOM("symptom"),
    SYMPTOM_GROUP("symptom_group"),
    FACTOR("factor"),
    CATEGORY("category"),
}

@Composable
fun displayName(prefix: NamePrefix, key: String): String =
    Res.allStringResources["${prefix.value}_$key"]?.let { stringResource(it) } ?: key

/** Resolves names outside of composition, e.g. in a ViewModel for the search. */
fun interface DisplayNameResolver {
    suspend fun name(prefix: NamePrefix, key: String): String
}

class ResourceDisplayNameResolver : DisplayNameResolver {
    override suspend fun name(prefix: NamePrefix, key: String): String =
        Res.allStringResources["${prefix.value}_$key"]?.let { getString(it) } ?: key
}
