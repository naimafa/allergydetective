package com.nfar.allergydetective.data.profile

import com.nfar.allergydetective.data.db.dao.ProfileDao
import com.nfar.allergydetective.data.db.entity.KnownAllergyEntity
import com.nfar.allergydetective.data.db.entity.UserProfileEntity
import com.nfar.allergydetective.domain.profile.ProfileRepository

class RoomProfileRepository(private val dao: ProfileDao) : ProfileRepository {

    override suspend fun isOnboardingCompleted(): Boolean = dao.profile()?.onboardingCompletedAt != null

    override suspend fun completeOnboarding(atMillis: Long) {
        dao.upsertProfile(UserProfileEntity(onboardingCompletedAt = atMillis))
    }

    override suspend fun knownAllergyPropertyIds(): Set<Long> = dao.knownAllergyPropertyIds().toSet()

    override suspend fun saveKnownAllergies(propertyIds: Set<Long>) {
        dao.replaceKnownAllergies(propertyIds.sorted().map { KnownAllergyEntity(it) })
    }
}
