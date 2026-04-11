package com.anddd.nevera.domain.usecase

import com.anddd.nevera.core.common.ApiResult
import com.anddd.nevera.domain.model.MessageResult
import com.anddd.nevera.domain.repository.UserRepository
import javax.inject.Inject

class EmailVerifyUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(email: String, authCode: String): ApiResult<MessageResult> =
        userRepository.emailVerify(email, authCode)
}
