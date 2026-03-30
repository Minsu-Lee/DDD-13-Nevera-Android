package com.anddd.nevera.data.datasource

import com.anddd.nevera.core.common.ApiResponse
import com.anddd.nevera.data.model.DbTestResponse

internal interface DbTestDataSource {
    suspend fun getDbTest(): ApiResponse<DbTestResponse>
}
