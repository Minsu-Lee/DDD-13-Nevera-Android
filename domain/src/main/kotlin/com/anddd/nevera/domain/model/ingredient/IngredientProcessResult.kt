package com.anddd.nevera.domain.model.ingredient

/**
 * 식재료 구조·폐기 처리 결과 도메인 모델
 *
 * @property inventoryId      처리된 식재료 ID
 * @property processedStatus  이번 처리 유형 (구조/폐기)
 * @property processedRatio   이번 처리 비율 (%)
 * @property processedAmount  이번에 구조·폐기된 금액
 * @property consumedRatio    누적 구조 비율 (%)
 * @property wastedRatio      누적 폐기 비율 (%)
 * @property remainingRatio   남은 비율 (%)
 * @property remainingAmount  현재 남은 금액
 * @property inventoryStatus  부분 처리 시 "ACTIVE", 완료 시 최종 상태값
 * @property completed        누적 처리율 100% 여부
 */
data class IngredientProcessResult(
    val inventoryId: Long,
    val processedStatus: ProcessType,
    val processedRatio: Int,
    val processedAmount: Int,
    val consumedRatio: Int,
    val wastedRatio: Int,
    val remainingRatio: Int,
    val remainingAmount: Int,
    val inventoryStatus: String,
    val completed: Boolean,
)
