package com.anddd.nevera.data.api

import com.anddd.nevera.core.common.ApiResponse
import com.anddd.nevera.data.model.DbTestResponse
import retrofit2.http.GET

internal interface DbTestApi {

    @GET("dbtest")
    suspend fun getDbTest(): ApiResponse<DbTestResponse>
}
