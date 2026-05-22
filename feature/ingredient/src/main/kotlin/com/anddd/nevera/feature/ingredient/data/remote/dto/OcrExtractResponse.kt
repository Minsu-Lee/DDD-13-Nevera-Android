package com.anddd.nevera.feature.ingredient.data.remote.dto

/**
 * OCR 추출 API 응답 (GET /api/v1/ocr/extract)
 *
 * @param result OCR로 추출된 식재료 목록
 * @param error  오류 발생 시 에러 정보 (정상 응답 시 null)
 */
data class OcrExtractResponse(
    val result: List<OcrIngredientDto>,
    val error: OcrErrorDto?,
)
