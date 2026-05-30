package com.anddd.nevera.domain.model.ingredient

/**
 * 식재료 카테고리 도메인 모델
 *
 * UI 표시 문자열·아이콘 등 표현 관련 속성은 UI 레이어의 확장 프로퍼티로 분리됩니다.
 * @see com.anddd.nevera.feature.ingredient.main.displayName
 * @see com.anddd.nevera.feature.ingredient.main.iconRes
 */
sealed interface FoodCategory {
    data object Veg : FoodCategory
    data object Fruit : FoodCategory
    data object MeatEggs : FoodCategory
    data object Sea : FoodCategory
    data object Dairy : FoodCategory
    data object Sauce : FoodCategory
    data object Drink : FoodCategory
    data object Processed : FoodCategory
    data object Etc : FoodCategory

    companion object {
        /** 화면에 표시할 모든 카테고리 목록 (선언 순서 유지) */
        val entries: List<FoodCategory> = listOf(
            Veg, Fruit, MeatEggs, Sea, Dairy, Sauce, Drink, Processed, Etc,
        )
    }
}
