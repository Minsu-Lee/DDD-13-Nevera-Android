package com.anddd.nevera.domain.repository

import com.anddd.nevera.domain.model.Session

interface TokenRepository {
    suspend fun saveSession(accessToken: String, refreshToken: String, userId: String)
    suspend fun saveTokens(accessToken: String, refreshToken: String)
    suspend fun getSession(): Session
    suspend fun getToken(): String?
    suspend fun getRefreshToken(): String?
    suspend fun getUserId(): String?
    suspend fun clearSession()
}
