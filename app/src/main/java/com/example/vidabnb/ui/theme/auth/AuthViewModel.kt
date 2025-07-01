package com.example.vidabnb.ui.theme.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import com.example.vidabnb.data.model.login.LoginRequest
import com.example.vidabnb.data.model.login.LoginResponse
import com.example.vidabnb.data.model.signup.SignupRequest
import com.example.vidabnb.data.model.signup.SignupResponse
import com.example.vidabnb.data.repository.AuthRepository
import com.example.vidabnb.util.Resource

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<Resource<LoginResponse>>(Resource.Idle())
    val loginState: StateFlow<Resource<LoginResponse>> = _loginState.asStateFlow()

    private val _signupState = MutableStateFlow<Resource<SignupResponse>>(Resource.Idle())
    val signupState: StateFlow<Resource<SignupResponse>> = _signupState.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(authRepository.isAuthenticated())
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _loginValidation = MutableStateFlow(FormValidationState())
    val loginValidation: StateFlow<FormValidationState> = _loginValidation.asStateFlow()

    private val _signupValidation = MutableStateFlow(FormValidationState())
    val signupValidation: StateFlow<FormValidationState> = _signupValidation.asStateFlow()

    private val _currentUsername = MutableStateFlow<String?>(authRepository.getCurrentUsername())
    val currentUsername: StateFlow<String?> = _currentUsername.asStateFlow()

    private val _currentUserEmail = MutableStateFlow<String?>(authRepository.getCurrentEmail())
    val currentUserEmail: StateFlow<String?> = _currentUserEmail.asStateFlow()

    private val _currentUserId = MutableStateFlow<String?>(authRepository.getCurrentUserId())
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    init {
        _isAuthenticated.onEach {
            _currentUsername.value = authRepository.getCurrentUsername()
            _currentUserEmail.value = authRepository.getCurrentEmail()
            _currentUserId.value = authRepository.getCurrentUserId()
        }.launchIn(viewModelScope)
    }
    fun login(email: String, password: String) {
        val validation = validateLoginForm(email, password)
        _loginValidation.value = validation

        if (!validation.isValid) {
            return
        }

        _loginState.value = Resource.Loading()
        authRepository.login(LoginRequest(email, password)).onEach { result ->
            _loginState.value = result
            if (result is Resource.Success) {
                _isAuthenticated.value = authRepository.isAuthenticated()
            }
        }.launchIn(viewModelScope)
    }

    fun signup(username: String, email: String, password: String) {
        val validation = validateSignupForm(username, email, password)
        _signupValidation.value = validation

        if (!validation.isValid) {
            return
        }

        _signupState.value = Resource.Loading()
        authRepository.signup(SignupRequest(username, email, password)).onEach { result ->
            _signupState.value = result
             if (result is Resource.Success) {
                 login(email, password)
             }
        }.launchIn(viewModelScope)
    }

    fun logout() {
        authRepository.logout()
        _isAuthenticated.value = authRepository.isAuthenticated()
        _loginState.value = Resource.Idle()
        _signupState.value = Resource.Idle()
        _loginValidation.value = FormValidationState()
        _signupValidation.value = FormValidationState()
    }

    fun getCurrentUserId(): String? {
        return authRepository.getCurrentUserId()
    }

    fun clearLoginValidation() {
        _loginValidation.value = FormValidationState()
    }

    fun clearSignupValidation() {
        _signupValidation.value = FormValidationState()
    }

    private fun validateLoginForm(email: String, password: String): FormValidationState {
        val errors = mutableListOf<ValidationError>()

        if (email.isBlank()) {
            errors.add(ValidationError("email", "Email is required"))
        } else if (!isValidEmail(email)) {
            errors.add(ValidationError("email", "Please enter a valid email address"))
        }

        if (password.isBlank()) {
            errors.add(ValidationError("password", "Password is required"))
        } else if (password.length < 6) {
            errors.add(ValidationError("password", "Password must be at least 6 characters"))
        }

        return FormValidationState(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }

    private fun validateSignupForm(
        username: String,
        email: String,
        password: String
    ): FormValidationState {
        val errors = mutableListOf<ValidationError>()

        if (username.isBlank()) {
            errors.add(ValidationError("username", "Username is required"))
        } else if (username.length < 3) {
            errors.add(ValidationError("username", "Username must be at least 3 characters"))
        } else if (username.length > 20) {
            errors.add(ValidationError("username", "Username must be less than 20 characters"))
        } else if (!isValidUsername(username)) {
            errors.add(
                ValidationError(
                    "username",
                    "Username can only contain letters, numbers, and underscores"
                )
            )
        }

        if (email.isBlank()) {
            errors.add(ValidationError("email", "Email is required"))
        } else if (!isValidEmail(email)) {
            errors.add(ValidationError("email", "Please enter a valid email address"))
        }

        if (password.isBlank()) {
            errors.add(ValidationError("password", "Password is required"))
        } else if (password.length < 8) {
            errors.add(ValidationError("password", "Password must be at least 8 characters"))
        } else if (!hasValidPasswordComplexity(password)) {
            errors.add(
                ValidationError(
                    "password",
                    "Password must contain at least one letter and one number"
                )
            )
        }

        return FormValidationState(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidUsername(username: String): Boolean {
        return username.matches(Regex("^[a-zA-Z0-9_]+$"))
    }

    private fun hasValidPasswordComplexity(password: String): Boolean {
        val hasLetter = password.any { it.isLetter() }
        val hasNumber = password.any { it.isDigit() }
        return hasLetter && hasNumber
    }

    fun getFieldError(field: String, isSignup: Boolean = false): String? {
        val validation = if (isSignup) _signupValidation.value else _loginValidation.value
        return validation.errors.find { it.field == field }?.message
    }
}
