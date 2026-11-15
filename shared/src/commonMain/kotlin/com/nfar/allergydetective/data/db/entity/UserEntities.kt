package com.nfar.allergydetective.data.db.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.PrimaryKey

/** Single-row table (id = 1) with onboarding state (TRD 1.2). */
@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Long = SINGLE_ROW_ID,
    /** UTC epoch millis, null while onboarding is not finished. */
    val onboardingCompletedAt: Long?,
) {
    companion object {
        const val SINGLE_ROW_ID = 1L
    }
}

/** Known allergies are excluded from the ranking and only trigger a separate notice (TRD 2.6). */
@Entity(
    tableName = "user_known_allergies",
    foreignKeys = [
        ForeignKey(PropertyEntity::class, ["id"], ["propertyId"], onDelete = ForeignKey.CASCADE),
    ],
)
data class KnownAllergyEntity(
    @PrimaryKey val propertyId: Long,
)
