package com.mobile.habittrackernew.ui.screens.auth

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.habittrackernew.data.preferences.PreferencesManager
import com.mobile.habittrackernew.services.GoogleAuthHelper
import com.mobile.habittrackernew.services.GoogleSignInResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
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
    val googleSignInIntent: Intent? = null
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
    private val preferencesManager: PreferencesManager,
    private val googleAuthHelper: GoogleAuthHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _loginForm = MutableStateFlow(LoginFormState())
    val loginForm: StateFlow<LoginFormState> = _loginForm.asStateFlow()

    private val _signupForm = MutableStateFlow(SignupFormState())
    val signupForm: StateFlow<SignupFormState> = _signupForm.asStateFlow()

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            val isLoggedIn = preferencesManager.isLoggedIn.first()
            val isFirstLaunch = preferencesManager.isFirstLaunch.first()
            _uiState.update {
                it.copy(
                    isLoggedIn = isLoggedIn,
                    isFirstLaunch = isFirstLaunch,
                    isLoading = false
                )
            }
        }
    }

    // Login Form Updates
    fun updateLoginEmail(email: String) {
        _loginForm.update { it.copy(email = email, emailError = null) }
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

    // Signup Form Updates
    fun updateSignupName(name: String) {
        _signupForm.update { it.copy(name = name, nameError = null) }
    }

    fun updateSignupEmail(email: String) {
        _signupForm.update { it.copy(email = email, emailError = null) }
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

    // Validation
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

        if (!form.acceptedTerms) {
            isValid = false
        }

        return isValid
    }

    // Auth Actions
    fun login() {
        if (!validateLoginForm()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            delay(1500)

            val form = _loginForm.value

            preferencesManager.setLoggedIn(
                isLoggedIn = true,
                userId = "user_${System.currentTimeMillis()}",
                userName = form.email.substringBefore("@"),
                userEmail = form.email
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
    }

    fun signup() {
        if (!validateSignupForm()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            delay(1500)

            val form = _signupForm.value

            preferencesManager.setLoggedIn(
                isLoggedIn = true,
                userId = "user_${System.currentTimeMillis()}",
                userName = form.name,
                userEmail = form.email
            )

            preferencesManager.setFirstLaunch(false)

            _uiState.update {
                it.copy(
                    isLoading = false,
                    signupSuccess = true,
                    isLoggedIn = true
                )
            }
        }
    }

    // Google Sign-In
    fun getGoogleSignInIntent(): Intent {
        return googleAuthHelper.getSignInIntent()
    }

    fun handleGoogleSignInResult(data: Intent?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = googleAuthHelper.handleSignInResult(data)) {
                is GoogleSignInResult.Success -> {
                    val user = result.user
                    preferencesManager.setLoggedIn(
                        isLoggedIn = true,
                        userId = user.id,
                        userName = user.name,
                        userEmail = user.email,
                        userPhoto = user.photoUrl ?: ""
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
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }

                GoogleSignInResult.Cancelled -> {
                    _uiState.update {
                        it.copy(isLoading = false)
                    }
                }
            }
        }
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
}