package com.anddd.nevera.data.api

import com.anddd.nevera.core.network.model.ApiResponse
import com.anddd.nevera.data.model.wish.CreateWishRequest
import com.anddd.nevera.data.model.wish.WishResponse
import retrofit2.http.Body
import retrofit2.http.POST

internal interface WishApi {

    @POST("api/v1/savings/wish")
    suspend fun createWish(@Body request: CreateWishRequest): ApiResponse<WishResponse>
}
