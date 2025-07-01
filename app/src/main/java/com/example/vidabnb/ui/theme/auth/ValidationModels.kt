package com.example.vidabnb.ui.theme.auth

data class ValidationError(
    val field: String,
    val message: String
)

data class FormValidationState(
    val isValid: Boolean = true,
    val errors: List<ValidationError> = emptyList()
)