package com.anddd.nevera.domain.model.ingredient

/**
 * 식재료 처리 유형 도메인 모델
 *
 * | API 값       | 의미 |
 * |--------------|------|
 * | `CONSUMED`   | 구조 |
 * | `WASTED`     | 폐기 |
 */
sealed interface ProcessType {
    /** 구조 */
    data object Consumed : ProcessType

    /** 폐기 */
    data object Wasted : ProcessType
}
