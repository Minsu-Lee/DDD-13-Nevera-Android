package com.anddd.nevera.data.mapper.error

import com.anddd.nevera.core.common.NetworkError
import com.anddd.nevera.domain.model.notification.FcmTokenError

/**
 * 네트워크 레이어의 [NetworkError]를 FCM 등록 도메인 에러로 변환한다.
 *
 * 서버 비즈니스 에러 코드는 FCM 전용 에러로 매핑하고,
 * 나머지 인프라성 실패는 공통 에러로 변환해 [FcmTokenError.Common]에 담는다.
 */
internal fun NetworkError.toFcmTokenError(): FcmTokenError = when (this) {
    is NetworkError.HttpError -> when (code) {
        2041 -> FcmTokenError.MemberNotFound
        2051 -> FcmTokenError.TokenNotFound
        2052 -> FcmTokenError.InvalidToken
        2053 -> FcmTokenError.SendError
        else -> FcmTokenError.Common(toCommonError())
    }
    else -> FcmTokenError.Common(toCommonError())
}
