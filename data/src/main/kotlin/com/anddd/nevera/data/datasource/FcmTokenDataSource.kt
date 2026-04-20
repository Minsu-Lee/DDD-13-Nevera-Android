package com.anddd.nevera.data.datasource

internal interface FcmTokenDataSource {
    suspend fun getFcmToken(): String?
    suspend fun saveFcmToken(token: String)
    suspend fun needsSync(): Boolean
    suspend fun setNeedsSync(value: Boolean)
    suspend fun markTokenForSync(token: String)
}
