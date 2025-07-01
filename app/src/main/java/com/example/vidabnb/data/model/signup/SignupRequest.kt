package com.example.vidabnb.data.model.signup
import com.google.gson.annotations.SerializedName

data class SignupRequest(
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)
