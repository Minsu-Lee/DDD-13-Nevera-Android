package com.anddd.nevera.data.datasource

import com.anddd.nevera.core.common.ApiResponse
import com.anddd.nevera.data.model.DbTestResponse
import kotlinx.coroutines.delay
import javax.inject.Inject

internal class FakeDbTestDataSourceImpl @Inject constructor() : DbTestDataSource {

    override suspend fun getDbTest(): ApiResponse<DbTestResponse> {
        delay(300)
        return ApiResponse(result = DbTestResponse(id = 1, name = "fake_name"), error = null)
    }
}
