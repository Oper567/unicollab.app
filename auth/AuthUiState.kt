package com.unicollabapp.ui.auth

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val confirm: String = "",
    val loading: Boolean = false,
    val error: String? = null
)
