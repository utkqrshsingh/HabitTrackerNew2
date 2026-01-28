// data/repository/AuthRepository.kt
package com.mobile.habittrackernew.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

sealed class AuthResult<out T> {
    data class Success<T>(val data: T) : AuthResult<T>()
    data class Error(val message: String) : AuthResult<Nothing>()
    object Loading : AuthResult<Nothing>()
}

@Singleton
class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {
    companion object {
        private const val TAG = "AuthRepository"
    }

    val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    val isLoggedIn: Boolean
        get() = firebaseAuth.currentUser != null

    suspend fun loginWithEmail(email: String, password: String): AuthResult<FirebaseUser> {
        return try {
            Log.d(TAG, "Attempting login with email: $email")
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            result.user?.let {
                Log.d(TAG, "Login successful: ${it.email}")
                AuthResult.Success(it)
            } ?: AuthResult.Error("Login failed: User is null")
        } catch (e: Exception) {
            Log.e(TAG, "Login failed", e)
            AuthResult.Error(getReadableError(e))
        }
    }

    suspend fun signupWithEmail(name: String, email: String, password: String): AuthResult<FirebaseUser> {
        return try {
            Log.d(TAG, "Attempting signup with email: $email")
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()

            result.user?.let { user ->
                // Update display name
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
                user.updateProfile(profileUpdates).await()

                Log.d(TAG, "Signup successful: ${user.email}")
                AuthResult.Success(user)
            } ?: AuthResult.Error("Signup failed: User is null")
        } catch (e: Exception) {
            Log.e(TAG, "Signup failed", e)
            AuthResult.Error(getReadableError(e))
        }
    }

    suspend fun sendPasswordResetEmail(email: String): AuthResult<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Log.d(TAG, "Password reset email sent to: $email")
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send password reset email", e)
            AuthResult.Error(getReadableError(e))
        }
    }

    fun logout() {
        firebaseAuth.signOut()
        Log.d(TAG, "User logged out")
    }

    private fun getReadableError(e: Exception): String {
        val message = e.message ?: return "An unknown error occurred"
        return when {
            message.contains("INVALID_LOGIN_CREDENTIALS") ||
                    message.contains("INVALID_EMAIL") ||
                    message.contains("WRONG_PASSWORD") -> "Invalid email or password"

            message.contains("EMAIL_EXISTS") ||
                    message.contains("email address is already in use") -> "Email already registered. Try logging in."

            message.contains("WEAK_PASSWORD") -> "Password should be at least 6 characters"

            message.contains("USER_NOT_FOUND") -> "No account found with this email"

            message.contains("NETWORK") ||
                    message.contains("network") -> "Network error. Check your connection."

            message.contains("TOO_MANY_ATTEMPTS") ||
                    message.contains("too many attempts") -> "Too many attempts. Try again later."

            message.contains("USER_DISABLED") -> "This account has been disabled"

            else -> message
        }
    }
}