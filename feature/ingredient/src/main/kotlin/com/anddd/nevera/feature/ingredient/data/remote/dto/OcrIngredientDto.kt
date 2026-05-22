package com.anddd.nevera.feature.ingredient.data.remote.dto

/**
 * OCR 추출 식재료 항목 DTO
 *
 * 확인된 API 값:
 * - category: "GRAINS", "CANDRY" (서버 API enum 전체 목록 확정 후 OcrMapper 보완 필요)
 * - location: "PANTRY", "FRIDGE", "FREEZER"
 * - unit: "EA", "PACK"
 *
 * UI 변환은 [com.anddd.nevera.feature.ingredient.data.remote.mapper.OcrMapper]에서 담당합니다.
 */
data class OcrIngredientDto(
    val name: String,
    val category: String,
    val location: String,
    val quantity: Int,
    val unit: String,
    val cost: Int,
)
