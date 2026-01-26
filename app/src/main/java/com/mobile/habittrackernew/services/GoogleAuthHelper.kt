package com.mobile.habittrackernew.services

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

data class GoogleUser(
    val id: String,
    val name: String,
    val email: String,
    val photoUrl: String?
)

sealed class GoogleSignInResult {
    data class Success(val user: GoogleUser) : GoogleSignInResult()
    data class Error(val message: String) : GoogleSignInResult()
    object Cancelled : GoogleSignInResult()
}

@Singleton
class GoogleAuthHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var googleSignInClient: GoogleSignInClient

    init {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .requestId()
            .build()

        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }

    fun getSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }

    fun handleSignInResult(task: Task<GoogleSignInAccount>): GoogleSignInResult {
        return try {
            val account = task.getResult(ApiException::class.java)
            if (account != null) {
                GoogleSignInResult.Success(
                    GoogleUser(
                        id = account.id ?: "",
                        name = account.displayName ?: "User",
                        email = account.email ?: "",
                        photoUrl = account.photoUrl?.toString()
                    )
                )
            } else {
                GoogleSignInResult.Error("Sign-in failed: Account is null")
            }
        } catch (e: ApiException) {
            Log.e("GoogleAuth", "Sign-in failed with code: ${e.statusCode}", e)
            when (e.statusCode) {
                12501 -> GoogleSignInResult.Cancelled
                12502 -> GoogleSignInResult.Error("Sign-in cancelled")
                7 -> GoogleSignInResult.Error("Network error. Please check your connection.")
                else -> GoogleSignInResult.Error("Sign-in failed: ${e.message}")
            }
        }
    }

    fun handleSignInResult(data: Intent?): GoogleSignInResult {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        return handleSignInResult(task)
    }

    fun getCurrentUser(): GoogleUser? {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        return account?.let {
            GoogleUser(
                id = it.id ?: "",
                name = it.displayName ?: "User",
                email = it.email ?: "",
                photoUrl = it.photoUrl?.toString()
            )
        }
    }

    fun signOut(onComplete: () -> Unit) {
        googleSignInClient.signOut().addOnCompleteListener {
            onComplete()
        }
    }

    fun revokeAccess(onComplete: () -> Unit) {
        googleSignInClient.revokeAccess().addOnCompleteListener {
            onComplete()
        }
    }
}