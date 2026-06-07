package com.anddd.nevera.data.repository

import com.anddd.nevera.core.common.NeveraResult
import com.anddd.nevera.domain.model.common.CommonError
import com.anddd.nevera.domain.model.ingredient.FoodCategory
import com.anddd.nevera.domain.model.ingredient.FridgeIngredient
import com.anddd.nevera.domain.model.ingredient.StorageLocation
import com.anddd.nevera.domain.repository.FridgeRepository
import java.time.Instant
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
private val now: Instant = Instant.now()
private fun daysAgo(days: Int): Instant = now.minusSeconds(3600L * 24 * days)

private val mockFridgeIngredients = listOf(
    // Veg
    FridgeIngredient(id = 1L,  name = "제주 햇당근",     category = FoodCategory.Veg,       storageLocation = StorageLocation.Fridge,   quantity = 1,  cost = 6500,  expiryDate = today.plusDays(28),  createdAt = daysAgo(49)),
    FridgeIngredient(id = 2L,  name = "시금치",           category = FoodCategory.Veg,       storageLocation = StorageLocation.Fridge,   quantity = 1,  cost = 2800,  expiryDate = today.plusDays(3),   createdAt = daysAgo(47)),
    FridgeIngredient(id = 3L,  name = "양파",             category = FoodCategory.Veg,       storageLocation = StorageLocation.Pantry,   quantity = 5,  cost = 3000,  expiryDate = today.plusDays(30),  createdAt = daysAgo(45)),
    FridgeIngredient(id = 4L,  name = "마늘",             category = FoodCategory.Veg,       storageLocation = StorageLocation.Pantry,   quantity = 1,  cost = 4500,  expiryDate = today.plusDays(60),  createdAt = daysAgo(43)),
    FridgeIngredient(id = 5L,  name = "브로콜리",         category = FoodCategory.Veg,       storageLocation = StorageLocation.Fridge,   quantity = 1,  cost = 3200,  expiryDate = today.plusDays(5),   createdAt = daysAgo(41)),
    FridgeIngredient(id = 6L,  name = "감자",             category = FoodCategory.Veg,       storageLocation = StorageLocation.Pantry,   quantity = 4,  cost = 5000,  expiryDate = today.plusDays(20),  createdAt = daysAgo(39)),
    FridgeIngredient(id = 7L,  name = "고구마",           category = FoodCategory.Veg,       storageLocation = StorageLocation.Pantry,   quantity = 3,  cost = 4200,  expiryDate = today.plusDays(14),  createdAt = daysAgo(37)),
    // MeatEggs
    FridgeIngredient(id = 8L,  name = "한돈 삼겹살",     category = FoodCategory.MeatEggs,  storageLocation = StorageLocation.Fridge,   quantity = 1,  cost = 24990, expiryDate = today.minusDays(3),  createdAt = daysAgo(35)),
    FridgeIngredient(id = 9L,  name = "닭가슴살",         category = FoodCategory.MeatEggs,  storageLocation = StorageLocation.Fridge,   quantity = 2,  cost = 8900,  expiryDate = today.plusDays(2),   createdAt = daysAgo(33)),
    FridgeIngredient(id = 10L, name = "소고기 불고기용",  category = FoodCategory.MeatEggs,  storageLocation = StorageLocation.Freezer,  quantity = 1,  cost = 18000, expiryDate = today.plusDays(90),  createdAt = daysAgo(31)),
    FridgeIngredient(id = 11L, name = "계란 10구",        category = FoodCategory.MeatEggs,  storageLocation = StorageLocation.Fridge,   quantity = 10, cost = 8900,  expiryDate = today.minusDays(1),  createdAt = daysAgo(29)),
    FridgeIngredient(id = 12L, name = "돼지고기 목살",   category = FoodCategory.MeatEggs,  storageLocation = StorageLocation.Freezer,  quantity = 1,  cost = 12000, expiryDate = today.plusDays(60),  createdAt = daysAgo(27)),
    FridgeIngredient(id = 13L, name = "닭다리",           category = FoodCategory.MeatEggs,  storageLocation = StorageLocation.Freezer,  quantity = 4,  cost = 7500,  expiryDate = today.plusDays(45),  createdAt = daysAgo(25)),
    // Dairy
    FridgeIngredient(id = 14L, name = "서울우유 1L",      category = FoodCategory.Dairy,     storageLocation = StorageLocation.Fridge,   quantity = 2,  cost = 3570,  expiryDate = today.plusDays(1),   createdAt = daysAgo(24)),
    FridgeIngredient(id = 15L, name = "그릭 요거트",      category = FoodCategory.Dairy,     storageLocation = StorageLocation.Fridge,   quantity = 2,  cost = 4200,  expiryDate = today.plusDays(7),   createdAt = daysAgo(22)),
    FridgeIngredient(id = 16L, name = "버터",             category = FoodCategory.Dairy,     storageLocation = StorageLocation.Fridge,   quantity = 1,  cost = 6800,  expiryDate = today.plusDays(120), createdAt = daysAgo(20)),
    FridgeIngredient(id = 17L, name = "슬라이스 치즈",   category = FoodCategory.Dairy,     storageLocation = StorageLocation.Fridge,   quantity = 1,  cost = 5500,  expiryDate = today.plusDays(30),  createdAt = daysAgo(18)),
    FridgeIngredient(id = 18L, name = "생크림",           category = FoodCategory.Dairy,     storageLocation = StorageLocation.Fridge,   quantity = 1,  cost = 3900,  expiryDate = today.plusDays(4),   createdAt = daysAgo(17)),
    // Sea
    FridgeIngredient(id = 19L, name = "새우",             category = FoodCategory.Sea,       storageLocation = StorageLocation.Freezer,  quantity = 3,  cost = 12000, expiryDate = today.plusDays(30),  createdAt = daysAgo(16)),
    FridgeIngredient(id = 20L, name = "오징어",           category = FoodCategory.Sea,       storageLocation = StorageLocation.Freezer,  quantity = 2,  cost = 8000,  expiryDate = today.plusDays(3),   createdAt = daysAgo(15)),
    FridgeIngredient(id = 21L, name = "고등어",           category = FoodCategory.Sea,       storageLocation = StorageLocation.Fridge,   quantity = 1,  cost = 5500,  expiryDate = today.plusDays(2),   createdAt = daysAgo(14)),
    FridgeIngredient(id = 22L, name = "연어",             category = FoodCategory.Sea,       storageLocation = StorageLocation.Fridge,   quantity = 1,  cost = 15000, expiryDate = today.plusDays(1),   createdAt = daysAgo(13)),
    FridgeIngredient(id = 23L, name = "조개",             category = FoodCategory.Sea,       storageLocation = StorageLocation.Freezer,  quantity = 1,  cost = 9000,  expiryDate = today.plusDays(60),  createdAt = daysAgo(12)),
    FridgeIngredient(id = 24L, name = "참치캔",           category = FoodCategory.Sea,       storageLocation = StorageLocation.Pantry,   quantity = 3,  cost = 4500,  expiryDate = today.plusDays(365), createdAt = daysAgo(11)),
    // Fruit
    FridgeIngredient(id = 25L, name = "사과",             category = FoodCategory.Fruit,     storageLocation = StorageLocation.Fridge,   quantity = 4,  cost = 7200,  expiryDate = today.plusDays(14),  createdAt = daysAgo(10)),
    FridgeIngredient(id = 26L, name = "바나나",           category = FoodCategory.Fruit,     storageLocation = StorageLocation.Pantry,   quantity = 5,  cost = 2500,  expiryDate = today.plusDays(4),   createdAt = daysAgo(10)),
    FridgeIngredient(id = 27L, name = "청포도",           category = FoodCategory.Fruit,     storageLocation = StorageLocation.Fridge,   quantity = 1,  cost = 4500,  expiryDate = today.plusDays(14),  createdAt = daysAgo(9)),
    FridgeIngredient(id = 28L, name = "딸기",             category = FoodCategory.Fruit,     storageLocation = StorageLocation.Fridge,   quantity = 1,  cost = 6800,  expiryDate = today.plusDays(3),   createdAt = daysAgo(9)),
    FridgeIngredient(id = 29L, name = "귤",               category = FoodCategory.Fruit,     storageLocation = StorageLocation.Pantry,   quantity = 8,  cost = 5000,  expiryDate = today.plusDays(7),   createdAt = daysAgo(8)),
    FridgeIngredient(id = 30L, name = "수박",             category = FoodCategory.Fruit,     storageLocation = StorageLocation.Fridge,   quantity = 1,  cost = 18000, expiryDate = today.plusDays(5),   createdAt = daysAgo(8)),
    // Sauce
    FridgeIngredient(id = 31L, name = "간장",             category = FoodCategory.Sauce,     storageLocation = StorageLocation.Pantry,   quantity = 1,  cost = 2800,  expiryDate = today.plusDays(180), createdAt = daysAgo(7)),
    FridgeIngredient(id = 32L, name = "된장",             category = FoodCategory.Sauce,     storageLocation = StorageLocation.Pantry,   quantity = 1,  cost = 3500,  expiryDate = today.plusDays(365), createdAt = daysAgo(7)),
    FridgeIngredient(id = 33L, name = "고추장",           category = FoodCategory.Sauce,     storageLocation = StorageLocation.Pantry,   quantity = 1,  cost = 4200,  expiryDate = today.plusDays(365), createdAt = daysAgo(6)),
    FridgeIngredient(id = 34L, name = "참기름",           category = FoodCategory.Sauce,     storageLocation = StorageLocation.Pantry,   quantity = 1,  cost = 7500,  expiryDate = today.plusDays(180), createdAt = daysAgo(6)),
    FridgeIngredient(id = 35L, name = "케첩",             category = FoodCategory.Sauce,     storageLocation = StorageLocation.Fridge,   quantity = 1,  cost = 2900,  expiryDate = today.plusDays(90),  createdAt = daysAgo(5)),
    FridgeIngredient(id = 36L, name = "마요네즈",         category = FoodCategory.Sauce,     storageLocation = StorageLocation.Fridge,   quantity = 1,  cost = 3200,  expiryDate = today.plusDays(60),  createdAt = daysAgo(5)),
    // Drink
    FridgeIngredient(id = 37L, name = "콜라 1.5L",        category = FoodCategory.Drink,     storageLocation = StorageLocation.Pantry,   quantity = 2,  cost = 1800,  expiryDate = today.plusDays(90),  createdAt = daysAgo(4)),
    FridgeIngredient(id = 38L, name = "오렌지주스",       category = FoodCategory.Drink,     storageLocation = StorageLocation.Fridge,   quantity = 1,  cost = 3200,  expiryDate = today.plusDays(7),   createdAt = daysAgo(4)),
    FridgeIngredient(id = 39L, name = "탄산수",           category = FoodCategory.Drink,     storageLocation = StorageLocation.Pantry,   quantity = 3,  cost = 1200,  expiryDate = today.plusDays(180), createdAt = daysAgo(3)),
    FridgeIngredient(id = 40L, name = "맥주",             category = FoodCategory.Drink,     storageLocation = StorageLocation.Fridge,   quantity = 4,  cost = 6000,  expiryDate = today.plusDays(60),  createdAt = daysAgo(3)),
    FridgeIngredient(id = 41L, name = "이온음료",         category = FoodCategory.Drink,     storageLocation = StorageLocation.Pantry,   quantity = 2,  cost = 2400,  expiryDate = today.plusDays(120), createdAt = daysAgo(2)),
    // Processed
    FridgeIngredient(id = 42L, name = "두부",             category = FoodCategory.Processed, storageLocation = StorageLocation.Fridge,   quantity = 1,  cost = 1500,  expiryDate = today,               createdAt = daysAgo(2)),
    FridgeIngredient(id = 43L, name = "라면",             category = FoodCategory.Processed, storageLocation = StorageLocation.Pantry,   quantity = 5,  cost = 4500,  expiryDate = today.plusDays(180), createdAt = daysAgo(2)),
    FridgeIngredient(id = 44L, name = "햄",               category = FoodCategory.Processed, storageLocation = StorageLocation.Fridge,   quantity = 1,  cost = 5800,  expiryDate = today.plusDays(14),  createdAt = daysAgo(1)),
    FridgeIngredient(id = 45L, name = "소시지",           category = FoodCategory.Processed, storageLocation = StorageLocation.Fridge,   quantity = 1,  cost = 4200,  expiryDate = today.plusDays(10),  createdAt = daysAgo(1)),
    FridgeIngredient(id = 46L, name = "어묵",             category = FoodCategory.Processed, storageLocation = StorageLocation.Freezer,  quantity = 2,  cost = 3800,  expiryDate = today.plusDays(30),  createdAt = daysAgo(1)),
    FridgeIngredient(id = 47L, name = "냉동만두",         category = FoodCategory.Processed, storageLocation = StorageLocation.Freezer,  quantity = 1,  cost = 6900,  expiryDate = today.plusDays(60),  createdAt = now.minusSeconds(3600 * 22)),
    FridgeIngredient(id = 48L, name = "김",               category = FoodCategory.Processed, storageLocation = StorageLocation.Pantry,   quantity = 3,  cost = 3500,  expiryDate = today.plusDays(90),  createdAt = now.minusSeconds(3600 * 18)),
    FridgeIngredient(id = 49L, name = "떡볶이 떡",        category = FoodCategory.Processed, storageLocation = StorageLocation.Freezer,  quantity = 1,  cost = 3200,  expiryDate = today.plusDays(30),  createdAt = now.minusSeconds(3600 * 10)),
    FridgeIngredient(id = 50L, name = "냉동 블루베리",   category = FoodCategory.Fruit,     storageLocation = StorageLocation.Freezer,  quantity = 1,  cost = 8500,  expiryDate = today.plusDays(180), createdAt = now),
)
