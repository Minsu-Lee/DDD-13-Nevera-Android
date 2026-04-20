package com.anddd.nevera.domain.model.notification

import com.anddd.nevera.domain.model.common.CommonError
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FcmTokenErrorLogMessageTest {

    @Test
    fun `member not found 에러를 서버 코드 포함 메시지로 변환한다`() {
        assertEquals(
            "FCM 동기화 실패: MEMBER_NOT_FOUND(status=404, code=2041, message=존재하지 않는 사용자입니다.)",
            FcmTokenError.MemberNotFound.toLogMessage(),
        )
    }

    @Test
    fun `server error 를 서버 오류 메시지로 변환한다`() {
        assertEquals(
            "FCM 동기화 실패: 서버 오류(boom)",
            FcmTokenError.Common(CommonError.ServerError("boom")).toLogMessage(),
        )
    }
}
