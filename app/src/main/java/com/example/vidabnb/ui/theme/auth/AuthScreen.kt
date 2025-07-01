package com.example.vidabnb.ui.theme.auth

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.vidabnb.ui.theme.common.VidaBNBTheme
import com.example.vidabnb.ui.theme.auth.FormValidationState
import com.example.vidabnb.ui.theme.auth.ValidationError
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.vidabnb.util.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    // State variables for form inputs
    var isLoginMode by remember { mutableStateOf(true) }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Observing states from ViewModel
    val loginState by authViewModel.loginState.collectAsState()
    val signupState by authViewModel.signupState.collectAsState()
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
    val loginValidation by authViewModel.loginValidation.collectAsState()
    val signupValidation by authViewModel.signupValidation.collectAsState()

    val context = LocalContext.current

    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) {
            navController.navigate("home_route") {
                popUpTo("auth_route") { inclusive = true } // Clear back stack
            }
        }
    }

    LaunchedEffect(loginState) {
        when (loginState) {
            is Resource.Success -> {
                Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show()
            }
            is Resource.Error -> {
                Toast.makeText(context, (loginState as Resource.Error).message ?: "Login failed", Toast.LENGTH_LONG).show()
            }
            is Resource.Loading -> {  }
            is Resource.Idle -> {
            }
        }
    }

    LaunchedEffect(signupState) {
        when (signupState) {
            is Resource.Success -> {
                Toast.makeText(
                    context,
                    "Registration successful! Please log in.",
                    Toast.LENGTH_SHORT
                ).show()
                isLoginMode = true
                username = ""
                email = ""
                password = ""
            }
            is Resource.Error -> {
                Toast.makeText(context, (signupState as Resource.Error).message ?: "Registration failed", Toast.LENGTH_LONG).show()
            }
            is Resource.Loading -> {  }
            is Resource.Idle -> {
            }
        }
    }

    AuthScreenContent(
        isLoginMode = isLoginMode,
        username = username,
        email = email,
        password = password,
        onUsernameChange = {
            username = it
            if (!isLoginMode) authViewModel.clearSignupValidation()
        },
        onEmailChange = {
            email = it
            if (isLoginMode) authViewModel.clearLoginValidation() else authViewModel.clearSignupValidation()
        },
        onPasswordChange = {
            password = it
            if (isLoginMode) authViewModel.clearLoginValidation() else authViewModel.clearSignupValidation()
        },
        onLoginClick = { authViewModel.login(email, password) },
        onSignupClick = { authViewModel.signup(username, email, password) },
        onToggleMode = {
            isLoginMode = !isLoginMode
            authViewModel.clearLoginValidation()
            authViewModel.clearSignupValidation()
        },
        isLoading = loginState is Resource.Loading || signupState is Resource.Loading,
        loginValidation = loginValidation,
        signupValidation = signupValidation
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreenContent(
    isLoginMode: Boolean,
    username: String,
    email: String,
    password: String,
    onUsernameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onSignupClick: () -> Unit,
    onToggleMode: () -> Unit,
    isLoading: Boolean,
    loginValidation: FormValidationState,
    signupValidation: FormValidationState
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isLoginMode) "Login" else "Sign Up") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isLoginMode) "Welcome Back!" else "Join VidBnB!",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            if (!isLoginMode) {
                OutlinedTextField(
                    value = username,
                    onValueChange = onUsernameChange,
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    isError = signupValidation.errors.any { it.field == "username" },
                    supportingText = {
                        signupValidation.errors.find { it.field == "username" }?.let {
                            Text(it.message, color = MaterialTheme.colorScheme.error)
                        }
                    }
                )
                Spacer(Modifier.height(16.dp))
            }

            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                isError = if (isLoginMode)
                    loginValidation.errors.any { it.field == "email" }
                else
                    signupValidation.errors.any { it.field == "email" },
                supportingText = {
                    if (isLoginMode) {
                        loginValidation.errors.find { it.field == "email" }?.let {
                            Text(it.message, color = MaterialTheme.colorScheme.error)
                        }
                    } else {
                        signupValidation.errors.find { it.field == "email" }?.let {
                            Text(it.message, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                isError = if (isLoginMode)
                    loginValidation.errors.any { it.field == "password" }
                else
                    signupValidation.errors.any { it.field == "password" },
                supportingText = {
                    if (isLoginMode) {
                        loginValidation.errors.find { it.field == "password" }?.let {
                            Text(it.message, color = MaterialTheme.colorScheme.error)
                        }
                    } else {
                        signupValidation.errors.find { it.field == "password" }?.let {
                            Text(it.message, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
            Spacer(Modifier.height(24.dp))

            Button(
                onClick = if (isLoginMode) onLoginClick else onSignupClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLoading,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Text(if (isLoginMode) "Login" else "Sign Up", style = MaterialTheme.typography.titleMedium)
                }
            }

            val errorMessage =
                if (isLoginMode) loginValidation.errors.find { it.field == "general" }?.message else signupValidation.errors.find { it.field == "general" }?.message
            if (!errorMessage.isNullOrBlank()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            TextButton(onClick = onToggleMode) {
                Text(
                    text = if (isLoginMode) "Don't have an account? Sign Up" else "Already have an account? Login",
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AuthScreenPreview() {
    VidaBNBTheme {
        AuthScreenContent(
            isLoginMode = true,
            username = "",
            email = "test@example.com",
            password = "password",
            onUsernameChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onLoginClick = {},
            onSignupClick = {},
            onToggleMode = {},
            isLoading = false,
            loginValidation = FormValidationState(isValid = true, errors = emptyList()),
            signupValidation = FormValidationState(isValid = true, errors = emptyList())
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AuthScreenSignupModePreview() {
    VidaBNBTheme {
        AuthScreenContent(
            isLoginMode = false,
            username = "NewUser",
            email = "new@example.com",
            password = "securepassword",
            onUsernameChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onLoginClick = {},
            onSignupClick = {},
            onToggleMode = {},
            isLoading = false,
            loginValidation = FormValidationState(isValid = true, errors = emptyList()),
            signupValidation = FormValidationState(isValid = true, errors = emptyList())
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AuthScreenLoadingPreview() {
    VidaBNBTheme {
        AuthScreenContent(
            isLoginMode = true,
            username = "",
            email = "loading@example.com",
            password = "password",
            onUsernameChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onLoginClick = {},
            onSignupClick = {},
            onToggleMode = {},
            isLoading = true,
            loginValidation = FormValidationState(isValid = true, errors = emptyList()),
            signupValidation = FormValidationState(isValid = true, errors = emptyList())
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AuthScreenErrorPreview() {
    VidaBNBTheme {
        AuthScreenContent(
            isLoginMode = true,
            username = "",
            email = "error@example.com",
            password = "password",
            onUsernameChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onLoginClick = {},
            onSignupClick = {},
            onToggleMode = {},
            isLoading = false,
            loginValidation = FormValidationState(
                isValid = false,
                errors = listOf(
                    ValidationError(
                        field = "email",
                        message = "Please enter a valid email address"
                    )
                )
            ),
            signupValidation = FormValidationState(isValid = true, errors = emptyList())
        )
    }
}
