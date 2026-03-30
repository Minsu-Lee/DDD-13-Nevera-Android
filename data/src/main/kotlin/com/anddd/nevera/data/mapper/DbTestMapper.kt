package com.anddd.nevera.data.mapper

import com.anddd.nevera.data.model.DbTestResponse
import com.anddd.nevera.domain.model.DbTest

internal fun DbTestResponse.toDomain(): DbTest = DbTest(
    id = id,
    name = name
)
