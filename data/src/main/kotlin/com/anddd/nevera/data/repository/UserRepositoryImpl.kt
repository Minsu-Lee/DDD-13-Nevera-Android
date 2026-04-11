package com.anddd.nevera.data.repository

import com.anddd.nevera.core.common.ApiResult
import com.anddd.nevera.core.common.mapSuccess
import com.anddd.nevera.core.network.auth.ApiCallExecutor
import com.anddd.nevera.data.datasource.UserDataSource
import com.anddd.nevera.data.mapper.toDomain
import com.anddd.nevera.domain.model.LoginResult
import com.anddd.nevera.domain.model.MessageResult
import com.anddd.nevera.domain.repository.UserRepository
import javax.inject.Inject

internal class UserRepositoryImpl @Inject constructor(
    private val authDataSource: UserDataSource,
    private val apiCall: ApiCallExecutor
) : UserRepository {

    override suspend fun loginWithEmail(email: String, password: String): ApiResult<LoginResult> {
        return apiCall { authDataSource.loginWithEmail(email, password) }
            .mapSuccess { it.toDomain() }
    }

    override suspend fun signup(
        email: String,
        password: String,
        name: String
    ): ApiResult<MessageResult> {
        return apiCall { authDataSource.signup(email, password, name) }
            .mapSuccess { it.toDomain() }
    }

    override suspend fun loginWithGoogle(idToken: String): ApiResult<LoginResult> {
        return apiCall { authDataSource.loginWithGoogle(idToken) }
            .mapSuccess { it.toDomain() }
    }

    override suspend fun emailRequest(email: String): ApiResult<MessageResult> {
        return apiCall { authDataSource.emailRequest(email) }
            .mapSuccess { it.toDomain() }
    }

    override suspend fun emailVerify(email: String, authCode: String): ApiResult<MessageResult> {
        return apiCall { authDataSource.emailVerify(email, authCode) }
            .mapSuccess { it.toDomain() }
    }

    override suspend fun logout(): ApiResult<MessageResult> {
        return apiCall { authDataSource.logout() }
            .mapSuccess { it.toDomain() }
    }

    override suspend fun withdraw(): ApiResult<MessageResult> {
        return apiCall { authDataSource.withdraw() }
            .mapSuccess { it.toDomain() }
    }
}
