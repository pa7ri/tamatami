package com.mobile.tamatami.data.repository

import com.mobile.tamatami.data.db.dao.UserProfileDao
import com.mobile.tamatami.data.db.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

class UserRepository(
    private val dao: UserProfileDao,
) {
    fun observeProfile(): Flow<UserProfileEntity?> = dao.observe()

    suspend fun getProfile(): UserProfileEntity? = dao.get()

    suspend fun saveProfile(profile: UserProfileEntity) = dao.upsert(profile)
}
