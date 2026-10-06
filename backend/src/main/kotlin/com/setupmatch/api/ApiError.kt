package com.setupmatch.api

data class ApiError(
    val message: String,
    val field_errors: Map<String, String> = emptyMap(),
)
