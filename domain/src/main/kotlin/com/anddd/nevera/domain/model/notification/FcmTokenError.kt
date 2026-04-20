package com.anddd.nevera.domain.model.notification

import com.anddd.nevera.domain.model.common.CommonError

/**
 * FCM 토큰 등록/동기화 과정에서 발생할 수 있는 비즈니스 에러.
 *
 * 서버가 내려주는 FCM 관련 에러 코드는 이 타입으로 끌어올리고,
 * 네트워크/타임아웃 같은 공통 인프라 에러는 [Common]으로 래핑한다.
 */
sealed interface FcmTokenError {
    // status: 404, code: 2041, 존재하지 않는 사용자인 경우
    data object MemberNotFound : FcmTokenError
    // status: 404, code: 2051, FCM 토큰이 등록되지 않은 경우
    data object TokenNotFound : FcmTokenError
    // status: 400, code: 2052, FCM 토큰이 만료되거나 무효화된 경우 (앱 재설치 등)
    data object InvalidToken : FcmTokenError
    // status: 500, code: 2053, Firebase 서버 오류 등 전송 실패 시
    data object SendError : FcmTokenError
    data class Common(val error: CommonError) : FcmTokenError
}
