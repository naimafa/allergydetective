package com.nfar.allergydetective.data.db.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

/*
 * Static reference data shipped with the app (TRD 1.1), filled from seed JSON.
 * Only stable English keys are stored; display names come from string resources (i18n).
 */

@Entity(tableName = "categories", indices = [Index(value = ["key"], unique = true)])
data class CategoryEntity(
    @PrimaryKey val id: Long,
    val key: String,
)

/** Trigger property, e.g. key "histamine", group "intolerance" or "allergen_eu14". */
@Entity(tableName = "properties", indices = [Index(value = ["key"], unique = true)])
data class PropertyEntity(
    @PrimaryKey val id: Long,
    val key: String,
    val groupKey: String,
)

/** Reaction window per trigger in minutes after eating. Windows may overlap. */
@Entity(
    tableName = "property_windows",
    foreignKeys = [
        ForeignKey(PropertyEntity::class, ["id"], ["propertyId"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index("propertyId")],
)
data class PropertyWindowEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val propertyId: Long,
    val startMin: Int,
    val endMin: Int,
)

/** Symptom groups that plausibly match a trigger. */
@Entity(
    tableName = "property_symptom_groups",
    primaryKeys = ["propertyId", "symptomGroupKey"],
    foreignKeys = [
        ForeignKey(PropertyEntity::class, ["id"], ["propertyId"], onDelete = ForeignKey.CASCADE),
    ],
)
data class PropertySymptomGroupEntity(
    val propertyId: Long,
    val symptomGroupKey: String,
)

/**
 * Predefined ingredients have a [key] (display name from resources).
 * Custom ingredients have no key but a user-entered [customName].
 */
@Entity(
    tableName = "ingredients",
    foreignKeys = [
        ForeignKey(CategoryEntity::class, ["id"], ["categoryId"]),
        ForeignKey(IngredientEntity::class, ["id"], ["baseIngredientId"], onDelete = ForeignKey.SET_NULL),
    ],
    indices = [
        Index(value = ["key"], unique = true),
        Index("categoryId"),
        Index("baseIngredientId"),
    ],
)
data class IngredientEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val key: String?,
    val customName: String?,
    val categoryId: Long,
    val isCustom: Boolean,
    val baseIngredientId: Long?,
)

@Entity(
    tableName = "ingredient_properties",
    primaryKeys = ["ingredientId", "propertyId"],
    foreignKeys = [
        ForeignKey(IngredientEntity::class, ["id"], ["ingredientId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(PropertyEntity::class, ["id"], ["propertyId"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index("propertyId")],
)
data class IngredientPropertyEntity(
    val ingredientId: Long,
    val propertyId: Long,
)

/** Search and voice synonyms per language, e.g. ("de", "semmel") for bread roll. */
@Entity(
    tableName = "ingredient_synonyms",
    foreignKeys = [
        ForeignKey(IngredientEntity::class, ["id"], ["ingredientId"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index("ingredientId"), Index(value = ["locale", "synonym"])],
)
data class IngredientSynonymEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ingredientId: Long,
    val locale: String,
    val synonym: String,
)

@Entity(tableName = "symptoms", indices = [Index(value = ["key"], unique = true), Index("groupKey")])
data class SymptomEntity(
    @PrimaryKey val id: Long,
    val key: String,
    val groupKey: String,
    val isRedFlag: Boolean,
)

/** Non-food candidates such as alcohol, sport, cycle, medication. */
@Entity(tableName = "lifestyle_factors", indices = [Index(value = ["key"], unique = true)])
data class LifestyleFactorEntity(
    @PrimaryKey val id: Long,
    val key: String,
)
