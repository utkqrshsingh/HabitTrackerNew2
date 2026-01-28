// ui/screens/auth/AuthViewModel.kt
package com.mobile.habittrackernew.ui.screens.auth

import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.mobile.habittrackernew.data.preferences.PreferencesManager
import com.mobile.habittrackernew.data.repository.AuthRepository
import com.mobile.habittrackernew.data.repository.AuthResult
import com.mobile.habittrackernew.services.GoogleAuthHelper
import com.mobile.habittrackernew.services.GoogleSignInResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val isFirstLaunch: Boolean = true,
    val error: String? = null,
    val loginSuccess: Boolean = false,
    val signupSuccess: Boolean = false,
    val passwordResetSent: Boolean = false
)

data class LoginFormState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val rememberMe: Boolean = false,
    val isPasswordVisible: Boolean = false
)

data class SignupFormState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val isPasswordVisible: Boolean = false,
    val acceptedTerms: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val preferencesManager: PreferencesManager,
    private val googleAuthHelper: GoogleAuthHelper
) : ViewModel() {

    companion object {
        private const val TAG = "AuthViewModel"
    }

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _loginForm = MutableStateFlow(LoginFormState())
    val loginForm: StateFlow<LoginFormState> = _loginForm.asStateFlow()

    private val _signupForm = MutableStateFlow(SignupFormState())
    val signupForm: StateFlow<SignupFormState> = _signupForm.asStateFlow()

    init {
        Log.d(TAG, "AuthViewModel initialized")
        checkAuthState()
        loadSavedEmail()  // NEW: Load saved email if Remember Me was checked
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            try {
                val isFirstLaunch = preferencesManager.isFirstLaunch.first()
                val isLoggedIn = authRepository.isLoggedIn

                Log.d(TAG, "Auth state: isLoggedIn=$isLoggedIn, isFirstLaunch=$isFirstLaunch")

                _uiState.update {
                    it.copy(
                        isLoggedIn = isLoggedIn,
                        isFirstLaunch = isFirstLaunch,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking auth state", e)
            }
        }
    }

    // NEW: Load saved email from preferences
    private fun loadSavedEmail() {
        viewModelScope.launch {
            try {
                val rememberMe = preferencesManager.rememberMe.first()
                val savedEmail = preferencesManager.savedEmail.first()

                Log.d(TAG, "Remember Me: $rememberMe, Saved Email: $savedEmail")

                if (rememberMe && savedEmail.isNotBlank()) {
                    _loginForm.update {
                        it.copy(
                            email = savedEmail,
                            rememberMe = true
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading saved email", e)
            }
        }
    }

    // ==================== Form Updates ====================
    fun updateLoginEmail(email: String) {
        _loginForm.update { it.copy(email = email.trim(), emailError = null) }
    }

    fun updateLoginPassword(password: String) {
        _loginForm.update { it.copy(password = password, passwordError = null) }
    }

    fun toggleLoginPasswordVisibility() {
        _loginForm.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun toggleRememberMe() {
        _loginForm.update { it.copy(rememberMe = !it.rememberMe) }
    }

    fun updateSignupName(name: String) {
        _signupForm.update { it.copy(name = name, nameError = null) }
    }

    fun updateSignupEmail(email: String) {
        _signupForm.update { it.copy(email = email.trim(), emailError = null) }
    }

    fun updateSignupPassword(password: String) {
        _signupForm.update { it.copy(password = password, passwordError = null) }
    }

    fun updateSignupConfirmPassword(password: String) {
        _signupForm.update { it.copy(confirmPassword = password, confirmPasswordError = null) }
    }

    fun toggleSignupPasswordVisibility() {
        _signupForm.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun toggleAcceptedTerms() {
        _signupForm.update { it.copy(acceptedTerms = !it.acceptedTerms) }
    }

    // ==================== Validation ====================
    private fun validateLoginForm(): Boolean {
        val form = _loginForm.value
        var isValid = true

        if (form.email.isBlank()) {
            _loginForm.update { it.copy(emailError = "Email is required") }
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(form.email).matches()) {
            _loginForm.update { it.copy(emailError = "Invalid email format") }
            isValid = false
        }

        if (form.password.isBlank()) {
            _loginForm.update { it.copy(passwordError = "Password is required") }
            isValid = false
        } else if (form.password.length < 6) {
            _loginForm.update { it.copy(passwordError = "Password must be at least 6 characters") }
            isValid = false
        }

        return isValid
    }

    private fun validateSignupForm(): Boolean {
        val form = _signupForm.value
        var isValid = true

        if (form.name.isBlank()) {
            _signupForm.update { it.copy(nameError = "Name is required") }
            isValid = false
        }

        if (form.email.isBlank()) {
            _signupForm.update { it.copy(emailError = "Email is required") }
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(form.email).matches()) {
            _signupForm.update { it.copy(emailError = "Invalid email format") }
            isValid = false
        }

        if (form.password.isBlank()) {
            _signupForm.update { it.copy(passwordError = "Password is required") }
            isValid = false
        } else if (form.password.length < 6) {
            _signupForm.update { it.copy(passwordError = "Password must be at least 6 characters") }
            isValid = false
        }

        if (form.confirmPassword != form.password) {
            _signupForm.update { it.copy(confirmPasswordError = "Passwords don't match") }
            isValid = false
        }

        return isValid
    }

    // ==================== LOGIN ====================
    fun login() {
        Log.d(TAG, ">>> LOGIN BUTTON CLICKED <<<")

        if (!validateLoginForm()) {
            Log.d(TAG, "Login validation failed")
            return
        }

        val form = _loginForm.value

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = authRepository.loginWithEmail(form.email, form.password)) {
                is AuthResult.Success -> {
                    Log.d(TAG, "Login SUCCESS: ${result.data.email}")

                    // NEW: Save Remember Me preference
                    preferencesManager.setRememberMe(form.rememberMe, form.email)

                    saveUserToPreferences(result.data)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loginSuccess = true,
                            isLoggedIn = true
                        )
                    }
                }

                is AuthResult.Error -> {
                    Log.e(TAG, "Login ERROR: ${result.message}")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }

                AuthResult.Loading -> {}
            }
        }
    }

    // ==================== SIGNUP ====================
    fun signup() {
        Log.d(TAG, ">>> SIGNUP BUTTON CLICKED <<<")

        if (!validateSignupForm()) {
            Log.d(TAG, "Signup validation failed")
            return
        }

        if (!_signupForm.value.acceptedTerms) {
            _uiState.update { it.copy(error = "Please accept the terms and conditions") }
            return
        }

        val form = _signupForm.value

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = authRepository.signupWithEmail(form.name, form.email, form.password)) {
                is AuthResult.Success -> {
                    Log.d(TAG, "Signup SUCCESS: ${result.data.email}")
                    saveUserToPreferences(result.data)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            signupSuccess = true,
                            isLoggedIn = true
                        )
                    }
                }

                is AuthResult.Error -> {
                    Log.e(TAG, "Signup ERROR: ${result.message}")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }

                AuthResult.Loading -> {}
            }
        }
    }

    // ==================== GOOGLE SIGN-IN ====================
    fun getGoogleSignInIntent(): Intent {
        Log.d(TAG, "Getting Google Sign-In intent")
        return googleAuthHelper.getSignInIntent()
    }

    fun handleGoogleSignInResult(data: Intent?) {
        Log.d(TAG, "Handling Google Sign-In result")

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = googleAuthHelper.handleSignInResult(data)) {
                is GoogleSignInResult.Success -> {
                    Log.d(TAG, "Google Sign-In SUCCESS: ${result.user.email}")

                    preferencesManager.setLoggedIn(
                        isLoggedIn = true,
                        userId = result.user.id,
                        userName = result.user.name,
                        userEmail = result.user.email,
                        userPhoto = result.user.photoUrl ?: ""
                    )
                    preferencesManager.setFirstLaunch(false)

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loginSuccess = true,
                            isLoggedIn = true
                        )
                    }
                }

                is GoogleSignInResult.Error -> {
                    Log.e(TAG, "Google Sign-In ERROR: ${result.message}")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }

                GoogleSignInResult.Cancelled -> {
                    Log.d(TAG, "Google Sign-In CANCELLED")
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    // ==================== PASSWORD RESET ====================
    fun sendPasswordResetEmail() {
        val email = _loginForm.value.email
        if (email.isBlank()) {
            _uiState.update { it.copy(error = "Please enter your email first") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = authRepository.sendPasswordResetEmail(email)) {
                is AuthResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            passwordResetSent = true
                        )
                    }
                }

                is AuthResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }

                AuthResult.Loading -> {}
            }
        }
    }

    // ==================== HELPERS ====================
    private suspend fun saveUserToPreferences(user: FirebaseUser) {
        preferencesManager.setLoggedIn(
            isLoggedIn = true,
            userId = user.uid,
            userName = user.displayName ?: "User",
            userEmail = user.email ?: "",
            userPhoto = user.photoUrl?.toString() ?: ""
        )
        preferencesManager.setFirstLaunch(false)
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun resetLoginSuccess() {
        _uiState.update { it.copy(loginSuccess = false) }
    }

    fun resetSignupSuccess() {
        _uiState.update { it.copy(signupSuccess = false) }
    }

    fun resetPasswordResetSent() {
        _uiState.update { it.copy(passwordResetSent = false) }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            googleAuthHelper.signOut()
            preferencesManager.logout()  // This now preserves Remember Me settings
            _uiState.update { AuthUiState() }

            // Reload saved email if Remember Me was enabled
            loadSavedEmail()
        }
    }
}