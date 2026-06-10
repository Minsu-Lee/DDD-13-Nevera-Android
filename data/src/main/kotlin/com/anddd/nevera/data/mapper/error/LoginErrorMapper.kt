package com.anddd.nevera.data.mapper.error

import com.anddd.nevera.core.common.NetworkError
import com.anddd.nevera.domain.model.auth.LoginError

private object LoginErrorCode {
    const val LOGIN_FAILED = 2008
}

internal fun NetworkError.toLoginError(): LoginError = when (this) {
    is NetworkError.HttpError -> when (code) {
        LoginErrorCode.LOGIN_FAILED -> LoginError.InvalidCredentials
        else -> LoginError.Common(toCommonError())
    }
    else -> LoginError.Common(toCommonError())
}
