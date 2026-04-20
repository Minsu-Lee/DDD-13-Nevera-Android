package com.anddd.nevera.domain.usecase.notification

import com.anddd.nevera.core.common.onSuccess
import com.anddd.nevera.domain.repository.FcmTokenRepository
import com.anddd.nevera.domain.repository.TokenRepository
import javax.inject.Inject

class UpdateFcmTokenUseCase @Inject constructor(
    private val fcmTokenRepository: FcmTokenRepository,
    private val tokenRepository: TokenRepository,
) {

    suspend operator fun invoke(newToken: String) {
        val storedToken = fcmTokenRepository.getFcmToken()
        if (newToken == storedToken) return

        fcmTokenRepository.markTokenForSync(newToken)

        tokenRepository.getAccessToken() ?: return  // 미로그인이면 여기서 종료

        fcmTokenRepository.registerFcmToken(newToken)
            .onSuccess { fcmTokenRepository.setNeedsSync(false) }
    }
}
