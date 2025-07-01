package com.example.vidabnb.data.model.signup
import com.google.gson.annotations.SerializedName

data class SignupResponse(
    @SerializedName("userId") val userId: String,
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("message") val message: String? = null
)
