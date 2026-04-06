package com.anddd.nevera.data.datasource

import com.anddd.nevera.domain.model.Session

internal interface SessionDataSource {
    suspend fun saveSession(accessToken: String, refreshToken: String, userId: String)
    suspend fun saveTokens(accessToken: String, refreshToken: String)
    suspend fun getSession(): Session
    suspend fun getToken(): String?
    suspend fun getRefreshToken(): String?
    suspend fun getUserId(): String?
    suspend fun clearSession()
}
