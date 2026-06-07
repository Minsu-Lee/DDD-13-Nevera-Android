package com.anddd.nevera.data.repository

import com.anddd.nevera.core.common.NeveraResult
import com.anddd.nevera.domain.model.common.CommonError
import com.anddd.nevera.domain.model.ingredient.FoodCategory
import com.anddd.nevera.domain.model.ingredient.FridgeIngredient
import com.anddd.nevera.domain.model.ingredient.StorageLocation
import com.anddd.nevera.domain.repository.FridgeRepository
import java.time.LocalDate
import javax.inject.Inject

// TODO: 실제 API 연동 시 제거
internal class FakeFridgeRepository @Inject constructor() : FridgeRepository {

    override suspend fun getFridgeIngredients(
        storageLocation: StorageLocation?,
        category: FoodCategory?,
    ): NeveraResult<List<FridgeIngredient>, CommonError> {
        val filtered = mockFridgeIngredients
            .let { list -> if (storageLocation != null) list.filter { it.storageLocation == storageLocation } else list }
            .let { list -> if (category != null) list.filter { it.category == category } else list }
        return NeveraResult.Success(filtered)
    }
}

private val today: LocalDate = LocalDate.now()

private val mockFridgeIngredients = listOf(
    FridgeIngredient(id = 1L, name = "제주 햇당근", category = FoodCategory.Veg, storageLocation = StorageLocation.Fridge, quantity = 1, cost = 6500, expiryDate = today.plusDays(28)),
    FridgeIngredient(id = 2L, name = "한돈 삼겹살", category = FoodCategory.MeatEggs, storageLocation = StorageLocation.Fridge, quantity = 1, cost = 24990, expiryDate = today.minusDays(3)),
    FridgeIngredient(id = 3L, name = "서울우유 1L", category = FoodCategory.Dairy, storageLocation = StorageLocation.Fridge, quantity = 2, cost = 3570, expiryDate = today.plusDays(1)),
    FridgeIngredient(id = 4L, name = "새우", category = FoodCategory.Sea, storageLocation = StorageLocation.Freezer, quantity = 3, cost = 12000, expiryDate = today.plusDays(30)),
    FridgeIngredient(id = 5L, name = "청포도", category = FoodCategory.Fruit, storageLocation = StorageLocation.Fridge, quantity = 1, cost = 4500, expiryDate = today.plusDays(14)),
    FridgeIngredient(id = 6L, name = "간장", category = FoodCategory.Sauce, storageLocation = StorageLocation.Pantry, quantity = 1, cost = 2800, expiryDate = today.plusDays(180)),
    FridgeIngredient(id = 7L, name = "콜라 1.5L", category = FoodCategory.Drink, storageLocation = StorageLocation.Pantry, quantity = 2, cost = 1800, expiryDate = today.plusDays(90)),
    FridgeIngredient(id = 8L, name = "두부", category = FoodCategory.Processed, storageLocation = StorageLocation.Fridge, quantity = 1, cost = 1500, expiryDate = today),
    FridgeIngredient(id = 9L, name = "계란 10구", category = FoodCategory.MeatEggs, storageLocation = StorageLocation.Fridge, quantity = 10, cost = 8900, expiryDate = today.minusDays(1)),
    FridgeIngredient(id = 10L, name = "오징어", category = FoodCategory.Sea, storageLocation = StorageLocation.Freezer, quantity = 2, cost = 8000, expiryDate = today.plusDays(3)),
)
