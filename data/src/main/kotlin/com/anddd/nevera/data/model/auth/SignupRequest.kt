package com.anddd.nevera.data.model.auth

internal data class SignupRequest(
    val email: String,
    val password: String,
    val passwordCheck: String,
    val name: String,
    val passwordMatch: Boolean
)
