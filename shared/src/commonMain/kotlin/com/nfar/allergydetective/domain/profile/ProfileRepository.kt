package com.nfar.allergydetective.domain.profile

/** Onboarding state and known allergies (TRD 1.2, 3.1). */
interface ProfileRepository {
    suspend fun isOnboardingCompleted(): Boolean

    /** Marks the onboarding as done at [atMillis] (UTC). */
    suspend fun completeOnboarding(atMillis: Long)

    suspend fun knownAllergyPropertyIds(): Set<Long>

    /** Replaces all known allergies. They leave the ranking and only cause a notice (TRD 2.6). */
    suspend fun saveKnownAllergies(propertyIds: Set<Long>)
}
