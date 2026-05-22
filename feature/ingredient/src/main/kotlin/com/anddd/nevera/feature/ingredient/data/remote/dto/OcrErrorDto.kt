package com.anddd.nevera.feature.ingredient.data.remote.dto

/**
 * OCR API 에러 응답 DTO
 *
 * @param code    에러 코드 ([OcrErrorCode] 참조)
 * @param message 서버 에러 메시지 키 (예: "error.ocr.empty_image_file")
 */
data class OcrErrorDto(
    val code: Int,
    val message: String,
)

/**
 * OCR API 에러 코드 상수
 *
 * | code | status | 상황 |
 * |------|--------|------|
 * | 3004 | 400    | 이미지 파일이 비어있는 경우 |
 * | 3001 | 500    | OCR 처리 중 오류 |
 * | 3003 | 500    | Google Vision API 오류 |
 */
object OcrErrorCode {
    const val EMPTY_IMAGE         = 3004
    const val PROCESS_ERROR       = 3001
    const val GOOGLE_VISION_ERROR = 3003

    fun toUserMessage(code: Int): String = when (code) {
        EMPTY_IMAGE         -> "이미지 파일이 비어있어요. 다시 시도해주세요."
        PROCESS_ERROR       -> "OCR 처리 중 오류가 발생했어요."
        GOOGLE_VISION_ERROR -> "이미지 인식에 실패했어요."
        else                -> "알 수 없는 오류가 발생했어요."
    }
}
