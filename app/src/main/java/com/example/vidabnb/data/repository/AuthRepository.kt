package com.example.vidabnb.data.repository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import com.example.vidabnb.data.api.VidaBNBApiService
import com.example.vidabnb.data.api.AuthTokenHolder
import com.example.vidabnb.data.model.login.LoginRequest
import com.example.vidabnb.data.model.login.LoginResponse
import com.example.vidabnb.util.Resource
import com.example.vidabnb.data.model.signup.SignupRequest
import com.example.vidabnb.data.model.signup.SignupResponse

class AuthRepository (
    private val apiService: VidaBNBApiService,
    private val authTokenHolder: AuthTokenHolder
){
    fun login(request: LoginRequest): Flow<Resource<LoginResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.loginUser(request)
            if (response.isSuccessful) {
                val loginResponse = response.body()
                if (loginResponse != null) {
                    // --- CRITICAL FIX: Store full user data in AuthTokenHolder ---
                    authTokenHolder.setAuthData(
                        loginResponse.token,
                        loginResponse.userId,
                        loginResponse.username, // Pass username
                        loginResponse.email     // Pass email
                    )
                    emit(Resource.Success(loginResponse))
                } else {
                    emit(Resource.Error("Login successful but no response body received."))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                emit(Resource.Error(errorBody ?: "Login failed with code: ${response.code()}"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected HTTP error occurred during login"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection for login."))
        } catch (e: Exception) {
            emit(Resource.Error("An unknown error occurred during login: ${e.localizedMessage}"))
        }
    }

    fun signup(request: SignupRequest): Flow<Resource<SignupResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.signupUser(request)
            if (response.isSuccessful) {
                val signupResponse = response.body()
                if (signupResponse != null) {
                    emit(Resource.Success(signupResponse))
                } else {
                    emit(Resource.Error("Registration successful but no response body received."))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                when (response.code()) {
                    400 -> emit(Resource.Error("User with this email already exists"))
                    422 -> emit(Resource.Error("Invalid user data provided"))
                    else -> emit(
                        Resource.Error(
                            errorBody ?: "Registration failed with code: ${response.code()}"
                        )
                    )
                }
            }
        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected HTTP error occurred during registration"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection for registration."))
        } catch (e: Exception) {
            emit(Resource.Error("An unknown error occurred during registration: ${e.localizedMessage}"))
        }
    }

    fun logout() {
        authTokenHolder.clearAuthData()
    }

    fun isAuthenticated(): Boolean {
        return authTokenHolder.isAuthenticated()
    }

    fun getCurrentUserId(): String? {
        return authTokenHolder.userId
    }
    fun getCurrentUsername(): String? {
        return authTokenHolder.username
    }


    fun getCurrentEmail(): String? {
        return authTokenHolder.email
    }
}
