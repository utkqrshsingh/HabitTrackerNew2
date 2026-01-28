// services/GoogleAuthHelper.kt
package com.mobile.habittrackernew.services

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.mobile.habittrackernew.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
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
    @ApplicationContext private val context: Context,
    private val firebaseAuth: FirebaseAuth
) {
    companion object {
        private const val TAG = "GoogleAuthHelper"
    }

    private val googleSignInClient: GoogleSignInClient

    init {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .requestProfile()
            .build()

        googleSignInClient = GoogleSignIn.getClient(context, gso)
        Log.d(TAG, "GoogleAuthHelper initialized with Firebase")
    }

    fun getSignInIntent(): Intent {
        Log.d(TAG, "Getting sign-in intent")
        googleSignInClient.signOut() // Always show account picker
        return googleSignInClient.signInIntent
    }

    suspend fun handleSignInResult(data: Intent?): GoogleSignInResult {
        Log.d(TAG, "Handling sign-in result")

        if (data == null) {
            Log.e(TAG, "Sign-in data is null")
            return GoogleSignInResult.Error("Sign-in failed: No data received")
        }

        return try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)

            if (account != null && account.idToken != null) {
                Log.d(TAG, "Google account obtained: ${account.email}")
                // Authenticate with Firebase
                firebaseAuthWithGoogle(account)
            } else {
                Log.e(TAG, "Account or idToken is null")
                GoogleSignInResult.Error("Sign-in failed: Could not get account info")
            }
        } catch (e: ApiException) {
            Log.e(TAG, "Google sign-in failed with code: ${e.statusCode}", e)
            when (e.statusCode) {
                12501 -> GoogleSignInResult.Cancelled
                12502 -> GoogleSignInResult.Error("Sign-in was cancelled")
                7 -> GoogleSignInResult.Error("Network error. Check your connection.")
                10 -> GoogleSignInResult.Error("Configuration error. Check SHA-1 in Firebase.")
                12500 -> GoogleSignInResult.Error("Sign-in failed. Please try again.")
                else -> GoogleSignInResult.Error("Sign-in failed (code: ${e.statusCode})")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during Google sign-in", e)
            GoogleSignInResult.Error("Sign-in failed: ${e.message}")
        }
    }

    private suspend fun firebaseAuthWithGoogle(account: GoogleSignInAccount): GoogleSignInResult {
        return try {
            Log.d(TAG, "Authenticating with Firebase...")
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()

            authResult.user?.let { firebaseUser ->
                Log.d(TAG, "Firebase auth successful: ${firebaseUser.email}")
                GoogleSignInResult.Success(
                    GoogleUser(
                        id = firebaseUser.uid,
                        name = firebaseUser.displayName ?: "User",
                        email = firebaseUser.email ?: "",
                        photoUrl = firebaseUser.photoUrl?.toString()
                    )
                )
            } ?: GoogleSignInResult.Error("Firebase auth failed: User is null")
        } catch (e: Exception) {
            Log.e(TAG, "Firebase auth failed", e)
            GoogleSignInResult.Error("Authentication failed: ${e.message}")
        }
    }

    fun getCurrentUser(): GoogleUser? {
        return firebaseAuth.currentUser?.let { user ->
            GoogleUser(
                id = user.uid,
                name = user.displayName ?: "User",
                email = user.email ?: "",
                photoUrl = user.photoUrl?.toString()
            )
        }
    }

    fun isSignedIn(): Boolean = firebaseAuth.currentUser != null

    fun signOut(onComplete: () -> Unit = {}) {
        firebaseAuth.signOut()
        googleSignInClient.signOut().addOnCompleteListener {
            Log.d(TAG, "Signed out from Firebase and Google")
            onComplete()
        }
    }

    fun revokeAccess(onComplete: () -> Unit = {}) {
        firebaseAuth.signOut()
        googleSignInClient.revokeAccess().addOnCompleteListener {
            Log.d(TAG, "Access revoked")
            onComplete()
        }
    }
}