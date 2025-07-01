package com.example.vidabnb.data.api
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import com.example.vidabnb.data.api.AuthTokenHolder

class AuthInterceptor @Inject constructor(
    private val authTokenHolder: AuthTokenHolder
): Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val token = authTokenHolder.authToken

        val requestBuilder = originalRequest.newBuilder()
        if (!token.isNullOrBlank()) {
            requestBuilder.header("Authorization", "Bearer $token")
        }

        val newRequest = requestBuilder.build()
        return chain.proceed(newRequest)

}
 }