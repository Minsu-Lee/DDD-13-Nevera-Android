package com.anddd.nevera.data.datasource

import com.anddd.nevera.core.common.ApiResponse
import com.anddd.nevera.data.api.DbTestApi
import com.anddd.nevera.data.model.DbTestResponse
import javax.inject.Inject

internal class RemoteDbTestDataSourceImpl @Inject constructor(
    private val dbTestApi: DbTestApi
) : DbTestDataSource {

    override suspend fun getDbTest(): ApiResponse<DbTestResponse> = dbTestApi.getDbTest()
}
