package com.example.vidabnb.data.model

data class ApiResponse<T>(
    val data: T?,
    val message: String?,
    val success: Boolean
)
