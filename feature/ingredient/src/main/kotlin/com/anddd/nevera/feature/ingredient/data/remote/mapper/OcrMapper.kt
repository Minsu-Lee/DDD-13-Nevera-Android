package com.anddd.nevera.feature.ingredient.data.remote.mapper

import com.anddd.nevera.domain.model.ingredient.FoodCategory
import com.anddd.nevera.domain.model.ingredient.StorageLocation
import com.anddd.nevera.feature.ingredient.data.remote.dto.OcrIngredientDto
import com.anddd.nevera.feature.ingredient.main.model.IngredientUiModel

/**
 * OCR API DTO → UI 모델 변환 매퍼
 *
 * DTO는 순수 데이터 홀더이므로 변환 로직은 이 클래스에서 담당합니다.
 */
object OcrMapper {

    fun OcrIngredientDto.toUiModel(): IngredientUiModel = IngredientUiModel(
        name     = name,
        category = toFoodCategory(category),
        location = toStorageLocation(location),
        quantity = quantity,
        cost     = cost,
    )

    /**
     * API category 문자열 → [FoodCategory] 도메인 모델
     *
     * 확인된 API 값: "GRAINS", "CANDRY"
     * 알 수 없는 값은 null 반환 — UI에서 "선택" placeholder로 표시됩니다.
     *
     * TODO: 서버 API enum 전체 목록 확정 후 매핑 보완
     */
    private fun toFoodCategory(value: String): FoodCategory? = when (value.uppercase()) {
        "VEGETABLE"  -> FoodCategory.Vegetable
        "FRUIT"      -> FoodCategory.Fruit
        "MEAT_EGG"   -> FoodCategory.MeatEgg
        "SEAFOOD"    -> FoodCategory.Seafood
        "DAIRY"      -> FoodCategory.Dairy
        "SAUCE"      -> FoodCategory.Sauce
        "BEVERAGE"   -> FoodCategory.Beverage
        "GRAINS",
        "CANDRY",
        "PROCESSED"  -> FoodCategory.Processed
        else         -> null
    }

    /**
     * API location 문자열 → [StorageLocation] 도메인 모델
     *
     * 확인된 API 값: "PANTRY", "FRIDGE"
     * 알 수 없는 값은 null 반환 — UI에서 "선택" placeholder로 표시됩니다.
     */
    private fun toStorageLocation(value: String): StorageLocation? = when (value.uppercase()) {
        "FRIDGE"  -> StorageLocation.Fridge
        "FREEZER" -> StorageLocation.Freezer
        "PANTRY"  -> StorageLocation.Pantry
        else      -> null
    }
}
