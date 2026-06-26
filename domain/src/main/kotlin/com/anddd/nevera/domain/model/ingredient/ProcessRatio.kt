package com.anddd.nevera.domain.model.ingredient

/**
 * 식재료 처리 비율 도메인 모델
 *
 * 서버가 허용하는 비율 값(25 / 50 / 75 / 100)만 표현한다.
 */
sealed interface ProcessRatio {
    val value: Int

    data object Quarter : ProcessRatio {
        override val value: Int = 25
    }

    data object Half : ProcessRatio {
        override val value: Int = 50
    }

    data object ThreeQuarters : ProcessRatio {
        override val value: Int = 75
    }

    data object Full : ProcessRatio {
        override val value: Int = 100
    }

    companion object {
        val entries: List<ProcessRatio> = listOf(Quarter, Half, ThreeQuarters, Full)
    }
}
