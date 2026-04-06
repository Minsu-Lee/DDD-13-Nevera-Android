package com.anddd.nevera.data.datasource

import com.anddd.nevera.domain.model.Session
import javax.inject.Inject

internal class FakeSessionDataSourceImpl @Inject constructor() : SessionDataSource {

    private var accessToken: String? = null
    private var refreshToken: String? = null
    private var userId: String? = null

    override suspend fun saveSession(accessToken: String, refreshToken: String, userId: String) {
        this.accessToken = accessToken
        this.refreshToken = refreshToken
        this.userId = userId
    }

    override suspend fun saveTokens(accessToken: String, refreshToken: String) {
        this.accessToken = accessToken
        this.refreshToken = refreshToken
    }

    override suspend fun getSession(): Session =
        Session(accessToken = accessToken, refreshToken = refreshToken, userId = userId)

    override suspend fun getToken(): String? = accessToken

    override suspend fun getRefreshToken(): String? = refreshToken

    override suspend fun getUserId(): String? = userId

    override suspend fun clearSession() {
        accessToken = null
        refreshToken = null
        userId = null
    }
}
