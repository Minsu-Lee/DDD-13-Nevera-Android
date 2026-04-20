package com.anddd.nevera.feature.login.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anddd.nevera.core.common.onFailure
import com.anddd.nevera.core.common.onSuccess
import com.anddd.nevera.domain.model.common.CommonError
import com.anddd.nevera.domain.model.notification.FcmTokenError
import com.anddd.nevera.domain.model.validation.EmailValidationResult
import com.anddd.nevera.domain.model.validation.PasswordValidationResult
import com.anddd.nevera.domain.usecase.auth.EmailLoginUseCase
import com.anddd.nevera.domain.usecase.auth.GoogleLoginUseCase
import com.anddd.nevera.domain.usecase.notification.SyncFcmTokenUseCase
import com.anddd.nevera.domain.usecase.validation.ValidateEmailUseCase
import com.anddd.nevera.domain.usecase.validation.ValidatePasswordUseCase
import com.anddd.nevera.feature.login.BuildConfig
import com.anddd.nevera.feature.login.main.model.LoginSideEffect
import com.anddd.nevera.feature.login.main.model.LoginStatus
import com.anddd.nevera.feature.login.main.model.LoginUiState
import com.anddd.nevera.feature.login.main.model.toUiModel
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val emailLoginUseCase: EmailLoginUseCase,
    private val googleLoginUseCase: GoogleLoginUseCase,
    private val validateEmailUseCase: ValidateEmailUseCase,
    private val validatePasswordUseCase: ValidatePasswordUseCase,
    private val syncFcmTokenUseCase: SyncFcmTokenUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    private val _sideEffect = Channel<LoginSideEffect>(Channel.BUFFERED)
    val sideEffect = _sideEffect.receiveAsFlow()

    fun onEmailChange(email: String) {
        _uiState.update {
            it.copy(
                email = email,
                emailValidation = validateEmailUseCase(email)
            )
        }
    }

    fun onPasswordChange(password: String) {
        _uiState.update {
            it.copy(
                password = password,
                passwordValidation = validatePasswordUseCase(password)
            )
        }
    }

    fun loginWithEmail(email: String, password: String) {
        if (!validateInputs(email, password)) return

        viewModelScope.launch {
            _uiState.update { it.copy(status = LoginStatus.Loading) }
            emailLoginUseCase(email, password)
                .onSuccess {
                    // 로그인 성공 후 FCM 토큰 동기화 시도 (실패해도 화면 이동 진행)
                    syncFcmToken()
                    _uiState.update { it.copy(status = LoginStatus.Success) }
                    _sideEffect.send(LoginSideEffect.MoveToHomeScreen)
                }.onFailure { cause ->
                    _uiState.update { it.copy(status = LoginStatus.Idle) }
                    _sideEffect.send(LoginSideEffect.ShowErrorToast(cause.toUiModel().message))
                }
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        val emailResult = validateEmailUseCase(email)
        val passwordResult = validatePasswordUseCase(password)
        _uiState.update { it.copy(emailValidation = emailResult, passwordValidation = passwordResult) }
        return emailResult == EmailValidationResult.Valid && passwordResult is PasswordValidationResult.Valid
    }

    fun loginWithGoogle(token: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(status = LoginStatus.Loading) }
            googleLoginUseCase(token)
                .onSuccess {
                    // 로그인 성공 후 FCM 토큰 동기화 시도 (실패해도 화면 이동 진행)
                    syncFcmToken()
                    _uiState.update { it.copy(status = LoginStatus.Success) }
                    _sideEffect.send(LoginSideEffect.MoveToHomeScreen)
                }.onFailure { cause ->
                    _uiState.update { it.copy(status = LoginStatus.Idle) }
                    _sideEffect.send(LoginSideEffect.ShowErrorToast(cause.toUiModel().message))
                }
        }
    }

    private suspend fun syncFcmToken() {
        runCatching {
            syncFcmTokenUseCase {
                Firebase.messaging.token.await()
            }
        }.onSuccess { result ->
            result.onFailure(::logFcmSyncFailure)
        }.onFailure { throwable ->
            if (BuildConfig.DEBUG) {
                Log.e(TAG, throwable.message, throwable)
            }
        }
    }

    private fun logFcmSyncFailure(error: FcmTokenError) {
        if (!BuildConfig.DEBUG) return

        val message = when (error) {
            FcmTokenError.MemberNotFound -> "FCM 동기화 실패: MEMBER_NOT_FOUND(status=404, code=2041, message=존재하지 않는 사용자입니다.)"
            FcmTokenError.TokenNotFound -> "FCM 동기화 실패: FCM_TOKEN_NOT_FOUND(status=404, code=2051, message=FCM 토큰이 등록되지 않은 사용자입니다.)"
            FcmTokenError.InvalidToken -> "FCM 동기화 실패: FCM_TOKEN_INVALID(status=400, code=2052, message=유효하지 않은 FCM 토큰입니다.)"
            FcmTokenError.SendError -> "FCM 동기화 실패: FCM_SEND_ERROR(status=500, code=2053, message=푸시 알림 전송 중 오류가 발생했습니다.)"
            is FcmTokenError.Common -> when (val commonError = error.error) {
                CommonError.NetworkUnavailable -> "FCM 동기화 실패: 네트워크 연결을 확인해주세요."
                CommonError.Timeout -> "FCM 동기화 실패: 요청 시간이 초과되었습니다."
                CommonError.Unauthorized -> "FCM 동기화 실패: 인증되지 않은 요청입니다."
                is CommonError.ServerError -> "FCM 동기화 실패: 서버 오류(${commonError.message})"
                CommonError.Unknown -> "FCM 동기화 실패: 알 수 없는 오류가 발생했습니다."
            }
        }
        Log.w(TAG, message)
    }

    fun handleGoogleLoginFailure(throwable: Throwable) {
        viewModelScope.launch {
            if (BuildConfig.DEBUG) throwable.printStackTrace()
            _uiState.update { it.copy(status = LoginStatus.Idle) }
            _sideEffect.send(LoginSideEffect.ShowErrorToast("SNS 로그인에 실패했습니다."))
        }
    }

    companion object {
        private const val TAG = "LoginViewModel"
    }
}
