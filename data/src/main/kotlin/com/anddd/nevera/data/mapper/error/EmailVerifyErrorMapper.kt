package com.anddd.nevera.data.mapper.error

import com.anddd.nevera.core.common.NetworkError
import com.anddd.nevera.domain.model.auth.EmailVerifyError

private object EmailVerifyErrorCode {
    const val INVALID_CODE = 2001
    const val EXPIRED_CODE = 2002
    const val NOT_FOUND = 2005
}

internal fun NetworkError.toEmailVerifyError(): EmailVerifyError = when (this) {
    is NetworkError.HttpError -> when (code) {
        EmailVerifyErrorCode.INVALID_CODE -> EmailVerifyError.InvalidCode(serverMessage = message)
        EmailVerifyErrorCode.EXPIRED_CODE -> EmailVerifyError.ExpiredCode(serverMessage = message)
        EmailVerifyErrorCode.NOT_FOUND -> EmailVerifyError.NotFound(serverMessage = message)
        else -> EmailVerifyError.Common(toCommonError())
    }
    else -> EmailVerifyError.Common(toCommonError())
}
