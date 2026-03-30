package com.anddd.nevera.domain.repository

import com.anddd.nevera.core.common.ApiResult
import com.anddd.nevera.domain.model.DbTest

interface DbTestRepository {
    suspend fun getDbTest(): ApiResult<DbTest>
}
