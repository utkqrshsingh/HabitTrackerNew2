package com.mobile.habittrackernew.data.models

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

sealed class AuthState {
    object Loading : AuthState()
    object Unauthenticated : AuthState()
    data class Authenticated(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}