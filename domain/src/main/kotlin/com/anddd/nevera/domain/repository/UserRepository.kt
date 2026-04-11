package com.anddd.nevera.domain.repository

import com.anddd.nevera.core.common.ApiResult
import com.anddd.nevera.domain.model.LoginResult
import com.anddd.nevera.domain.model.MessageResult

interface UserRepository {
    suspend fun loginWithEmail(email: String, password: String): ApiResult<LoginResult>
    suspend fun signup(
        email: String,
        password: String,
        name: String
    ): ApiResult<MessageResult>
    suspend fun loginWithGoogle(idToken: String): ApiResult<LoginResult>
    suspend fun emailRequest(email: String): ApiResult<MessageResult>
    suspend fun emailVerify(email: String, authCode: String): ApiResult<MessageResult>
    suspend fun logout(): ApiResult<MessageResult>
    suspend fun withdraw(): ApiResult<MessageResult>
}
