package com.anddd.nevera.domain.usecase

import com.anddd.nevera.core.common.NeveraResult
import com.anddd.nevera.core.common.mapFailure
import com.anddd.nevera.core.common.onSuccess
import com.anddd.nevera.domain.model.common.CommonError
import com.anddd.nevera.domain.model.notification.FcmTokenError
import com.anddd.nevera.domain.repository.FcmTokenProvider
import com.anddd.nevera.domain.repository.FcmTokenRepository
import com.anddd.nevera.domain.repository.TokenRepository
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class SyncDeviceTokenUseCase @Inject constructor(
    private val fcmTokenRepository: FcmTokenRepository,
    private val fcmTokenProvider: FcmTokenProvider,
    private val tokenRepository: TokenRepository,
) {

    private sealed interface SyncAction {
        data class Register(val token: String) : SyncAction
        object Skip : SyncAction
    }

    suspend operator fun invoke(token: String?): NeveraResult<Unit, CommonError> {
        return when (val action = determineSyncAction(token)) {
            is SyncAction.Skip -> NeveraResult.Success(Unit)
            is SyncAction.Register -> if (isLoggedIn()) register(action.token) else NeveraResult.Success(Unit)
        }
    }

    private suspend fun determineSyncAction(token: String?): SyncAction {
        if (!token.isNullOrEmpty()) return determineSyncActionFromInput(token)

        val storedToken = fcmTokenRepository.getFcmToken()
        if (!storedToken.isNullOrEmpty()) {
            return if (fcmTokenRepository.isSyncNeeded()) SyncAction.Register(storedToken) else SyncAction.Skip
        }

        val fetchedToken = fetchAndPersistToken() ?: return SyncAction.Skip
        return SyncAction.Register(fetchedToken)
    }

    private suspend fun determineSyncActionFromInput(token: String): SyncAction {
        val stored = fcmTokenRepository.getFcmToken()
        if (stored != token) {
            fcmTokenRepository.saveTokenPendingSync(token)
            return SyncAction.Register(token)
        }
        if (!fcmTokenRepository.isSyncNeeded()) return SyncAction.Skip
        return SyncAction.Register(token)
    }

    private suspend fun fetchAndPersistToken(): String? {
        val token = runCatching {
            fcmTokenProvider.getToken()
        }.onFailure {
            if (it is CancellationException) throw it
        }.getOrNull()
        if (!token.isNullOrEmpty()) fcmTokenRepository.saveTokenPendingSync(token)
        return token
    }

    private suspend fun isLoggedIn(): Boolean {
        return !tokenRepository.getAccessToken().isNullOrEmpty()
    }

    private suspend fun register(token: String): NeveraResult<Unit, CommonError> {
        return fcmTokenRepository.registerFcmToken(token)
            .onSuccess { fcmTokenRepository.clearSyncNeeded() }
            .mapFailure { error ->
                when (error) {
                    FcmTokenError.MemberNotFound -> CommonError.Unknown
                    is FcmTokenError.Common -> error.error
                }
            }
    }
}
