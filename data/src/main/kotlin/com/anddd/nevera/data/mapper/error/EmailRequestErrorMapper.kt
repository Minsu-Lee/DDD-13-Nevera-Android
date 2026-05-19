package com.anddd.nevera.data.mapper.error

import com.anddd.nevera.core.common.NetworkError
import com.anddd.nevera.domain.model.auth.EmailRequestError

private object EmailRequestErrorCode {
    const val DUPLICATE_EMAIL = 2006
    const val MAIL_SEND_ERROR = 2007
}

internal fun NetworkError.toEmailRequestError(): EmailRequestError = when (this) {
    is NetworkError.HttpError -> when (code) {
        EmailRequestErrorCode.DUPLICATE_EMAIL -> EmailRequestError.DuplicateEmail(serverMessage = message)
        EmailRequestErrorCode.MAIL_SEND_ERROR -> EmailRequestError.MailSendError(serverMessage = message)
        else -> EmailRequestError.Common(toCommonError())
    }
    else -> EmailRequestError.Common(toCommonError())
}
