package com.anddd.nevera.domain.model.ingredient

import com.anddd.nevera.domain.model.common.CommonError

/**
 * 식재료 구조·폐기 처리 API 에러 도메인 모델
 *
 * | 에러 코드 | HTTP | 설명 |
 * |-----------|------|------|
 * | 4001 | 404 | 식재료가 존재하지 않음 |
 * | 4002 | 403 | 다른 사용자의 식재료 |
 * | 4003 | 409 | 이미 100% 처리 완료 |
 * | 4004 | 400 | CONSUMED, WASTED 이외 상태 |
 * | 4005 | 400 | 허용되지 않은 비율 |
 * | 4006 | 400 | 누적 처리율이 100% 초과 |
 */
sealed interface ProcessIngredientError {
    /** 4001 — 식재료가 존재하지 않음 */
    data object InventoryNotFound : ProcessIngredientError

    /** 4002 — 다른 사용자의 식재료 */
    data object InventoryForbidden : ProcessIngredientError

    /** 4003 — 이미 100% 처리 완료된 식재료 */
    data object AlreadyCompleted : ProcessIngredientError

    /** 4004 — CONSUMED, WASTED 이외의 상태 */
    data object InvalidProcessStatus : ProcessIngredientError

    /** 4005 — 허용되지 않은 비율 (25/50/75/100 이외) */
    data object InvalidProcessRatio : ProcessIngredientError

    /** 4006 — 누적 처리율이 100% 초과 */
    data object ProcessRatioExceeded : ProcessIngredientError

    /** 인프라/공통 에러 래핑 */
    data class Common(val error: CommonError) : ProcessIngredientError
}
