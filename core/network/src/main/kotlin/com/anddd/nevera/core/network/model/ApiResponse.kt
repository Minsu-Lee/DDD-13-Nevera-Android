package com.anddd.nevera.core.network.model

data class ApiResponse<T>(
    val result: T?,
    val error: ApiError?
)

data class ApiError(
    val code: Int?,
    val message: String?
)