package com.anddd.nevera.data.repository

import com.anddd.nevera.data.datasource.LocalSessionDataSource
import com.anddd.nevera.data.datasource.SessionDataSource
import com.anddd.nevera.domain.model.Session
import com.anddd.nevera.domain.repository.TokenRepository
import javax.inject.Inject

internal class TokenRepositoryImpl @Inject constructor(
    @param:LocalSessionDataSource private val sessionDataSource: SessionDataSource
) : TokenRepository {

    override suspend fun saveSession(accessToken: String, refreshToken: String, userId: String) =
        sessionDataSource.saveSession(accessToken, refreshToken, userId)

    override suspend fun saveTokens(accessToken: String, refreshToken: String) =
        sessionDataSource.saveTokens(accessToken, refreshToken)

    override suspend fun getSession(): Session =
        sessionDataSource.getSession()

    override suspend fun getToken(): String? =
        sessionDataSource.getToken()

    override suspend fun getRefreshToken(): String? =
        sessionDataSource.getRefreshToken()

    override suspend fun getUserId(): String? =
        sessionDataSource.getUserId()

    override suspend fun clearSession() =
        sessionDataSource.clearSession()
}
