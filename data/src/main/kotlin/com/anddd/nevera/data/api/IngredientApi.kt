package com.anddd.nevera.data.api

import com.anddd.nevera.core.network.model.ApiResponse
import com.anddd.nevera.data.model.ingredient.DisposedIngredientResponse
import com.anddd.nevera.data.model.ingredient.IngredientResponse
import com.anddd.nevera.data.model.ingredient.RegisterIngredientRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

internal interface IngredientApi {

    @POST("api/v1/inventory")
    suspend fun registerIngredients(
        @Body items: List<RegisterIngredientRequest>,
    ): ApiResponse<Unit>

    @GET("api/v1/savings/consumed")
    suspend fun getRescuedIngredients(
        @Query("offset") offset: Int,
        @Query("limit") limit: Int,
    ): ApiResponse<List<IngredientResponse>>

    @GET("api/v1/savings/wasted")
    suspend fun getDisposedIngredients(
        @Query("offset") offset: Int,
        @Query("limit") limit: Int,
    ): ApiResponse<List<DisposedIngredientResponse>>
}
