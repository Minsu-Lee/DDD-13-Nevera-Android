package com.anddd.nevera.domain.usecase

import com.anddd.nevera.core.common.ApiResult
import com.anddd.nevera.domain.model.DbTest
import com.anddd.nevera.domain.repository.DbTestRepository
import javax.inject.Inject

class GetDbTestUseCase @Inject constructor(
    private val dbTestRepository: DbTestRepository
) {
    suspend operator fun invoke(): ApiResult<DbTest> = dbTestRepository.getDbTest()
}
