package com.anddd.nevera.data.datasource

import com.anddd.nevera.core.network.model.ApiResponse
import com.anddd.nevera.data.model.fridge.FridgeIngredientResponse
import com.anddd.nevera.data.model.fridge.FridgeIngredientsResponse
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import javax.inject.Inject

internal class FakeFridgeRemoteDataSource @Inject constructor() : FridgeRemoteDataSource {

    override suspend fun getFridgeIngredients(
        storageLocation: String?,
        category: String?,
        sortType: String,
        page: Int,
        size: Int,
    ): ApiResponse<FridgeIngredientsResponse> {
        val filtered = mockIngredients
            .filter { storageLocation == null || it.location == storageLocation }
            .filter { category == null || it.category == category }
        return ApiResponse(
            result = FridgeIngredientsResponse(
                content = filtered,
                last = true,
                number = 0,
            ),
            error = null,
        )
    }

    override suspend fun getFridgeIngredientById(id: Long): ApiResponse<FridgeIngredientResponse> {
        val ingredient = mockIngredients.first { it.id == id }
        return ApiResponse(result = ingredient, error = null)
    }

    private companion object {
        private val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

        private fun expiryDateOf(daysFromNow: Long): String {
            val date = LocalDate.now().plusDays(daysFromNow)
            return OffsetDateTime.of(date.atStartOfDay(), ZoneOffset.ofHours(9)).format(formatter)
        }

        private fun createdAt(): String =
            OffsetDateTime.of(LocalDate.now().minusDays(7).atStartOfDay(), ZoneOffset.ofHours(9))
                .format(formatter)

        private fun mockItem(
            id: Long,
            name: String,
            category: String,
            location: String,
            quantity: Int,
            cost: Int,
            daysUntilExpiry: Long,
        ) = FridgeIngredientResponse(
            id = id,
            name = name,
            category = category,
            location = location,
            quantity = quantity,
            expirationDate = expiryDateOf(daysUntilExpiry),
            dDay = daysUntilExpiry.toInt(),
            cost = cost,
            createdAt = createdAt(),
        )

        val mockIngredients = listOf(
            mockItem(1L, "제주 햇당근", "VEGETABLE", "FRIDGE", quantity = 2, cost = 6500, daysUntilExpiry = 28),
            mockItem(2L, "한돈 삼겹살", "MEATEGGS", "FRIDGE", quantity = 1, cost = 24990, daysUntilExpiry = -3),
            mockItem(3L, "서울우유 1L", "DAIRY", "FRIDGE", quantity = 1, cost = 3570, daysUntilExpiry = 5),
            mockItem(4L, "동물복지 유정란", "MEATEGGS", "FRIDGE", quantity = 10, cost = 8900, daysUntilExpiry = 4),
            mockItem(5L, "새우", "SEA", "FREEZER", quantity = 3, cost = 12000, daysUntilExpiry = 0),
            mockItem(6L, "청포도", "FRUIT", "FRIDGE", quantity = 1, cost = 4500, daysUntilExpiry = 14),
            mockItem(7L, "간장", "SAUCE", "PANTRY", quantity = 1, cost = 2800, daysUntilExpiry = 180),
            mockItem(8L, "콜라 1.5L", "DRINK", "PANTRY", quantity = 2, cost = 1800, daysUntilExpiry = 90),
            mockItem(9L, "두부", "PROCESSED", "FRIDGE", quantity = 1, cost = 1500, daysUntilExpiry = 1),
        )
    }
}
