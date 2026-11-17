package com.nfar.allergydetective.data.db.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
import com.nfar.allergydetective.data.db.entity.KnownAllergyEntity
import com.nfar.allergydetective.data.db.entity.UserProfileEntity

@Dao
interface ProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun profile(): UserProfileEntity?

    @Upsert
    suspend fun upsertProfile(profile: UserProfileEntity)

    @Query("SELECT propertyId FROM user_known_allergies ORDER BY propertyId")
    suspend fun knownAllergyPropertyIds(): List<Long>

    @Query("DELETE FROM user_known_allergies")
    suspend fun deleteKnownAllergies()

    @Insert
    suspend fun insertKnownAllergies(items: List<KnownAllergyEntity>)

    @Transaction
    suspend fun replaceKnownAllergies(items: List<KnownAllergyEntity>) {
        deleteKnownAllergies()
        insertKnownAllergies(items)
    }
}
