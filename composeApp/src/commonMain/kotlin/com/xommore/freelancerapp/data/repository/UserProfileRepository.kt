package com.xommore.freelancerapp.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.xommore.freelancerapp.data.model.UserProfile
import com.xommore.freelancerapp.db.FreelancerDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserProfileRepository(private val db: FreelancerDatabase) {

    private val queries = db.freelancerDatabaseQueries

    fun getProfile(userId: String): Flow<UserProfile?> {
        return queries.getProfile(userId)
            .asFlow()
            .mapToOneOrNull(Dispatchers.Default)
            .map { it?.toUserProfile() }
    }

    fun saveProfile(profile: UserProfile) {
        queries.saveProfile(
            id = profile.id,
            userId = profile.userId,
            name = profile.name,
            phone = profile.phone,
            email = profile.email,
            bankName = profile.bankName,
            accountNumber = profile.accountNumber,
            accountHolder = profile.accountHolder,
            businessNumber = profile.businessNumber,
            address = profile.address,
            taxRate = profile.taxRate,
            updatedAt = profile.updatedAt
        )
    }

    fun deleteProfile(userId: String) {
        queries.deleteProfile(userId)
    }
}

private fun com.xommore.freelancerapp.db.User_profile.toUserProfile(): UserProfile {
    return UserProfile(
        id = id,
        userId = userId,
        name = name,
        phone = phone,
        email = email,
        bankName = bankName,
        accountNumber = accountNumber,
        accountHolder = accountHolder,
        businessNumber = businessNumber,
        address = address,
        taxRate = taxRate,
        updatedAt = updatedAt
    )
}