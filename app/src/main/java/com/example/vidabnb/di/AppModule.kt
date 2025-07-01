package com.example.vidabnb.di
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton
import com.example.vidabnb.data.api.VidaBNBApiService
import com.example.vidabnb.data.api.AuthInterceptor
import com.example.vidabnb.data.api.AuthTokenHolder
import com.example.vidabnb.data.repository.AuthRepository
import com.example.vidabnb.data.repository.BookingRepository
import com.example.vidabnb.data.repository.ListingRepository
import com.example.vidabnb.data.repository.WishlistRepository

@Module
@InstallIn(SingletonComponent::class)
object AppModule {


    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        val gson = com.google.gson.GsonBuilder()
            .setLenient() // Allow malformed JSON
            .create()

        return Retrofit.Builder()
            // For Android Emulator: use 10.0.2.2 (maps to host localhost)
            // For Physical Device: use your computer's IP address (e.g., "http://192.168.1.100:3000/")
            // Make sure Mockoon is running on 0.0.0.0:3000 not just 127.0.0.1:3000
            .baseUrl("http://10.0.2.2:3000/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideVidBnBApiService(retrofit: Retrofit): VidaBNBApiService {
        return retrofit.create(VidaBNBApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        apiService: VidaBNBApiService,
        authTokenHolder: AuthTokenHolder
    ): AuthRepository {
        return AuthRepository(apiService, authTokenHolder)
    }

    @Provides
    @Singleton
    fun provideListingRepository(apiService: VidaBNBApiService): ListingRepository {
        return ListingRepository(apiService)
    }

    @Provides
    @Singleton
    fun provideBookingRepository(apiService: VidaBNBApiService): BookingRepository {
        return BookingRepository(apiService)
    }

    @Provides
    @Singleton
    fun provideWishlistRepository(
        apiService: VidaBNBApiService,
        authTokenHolder: AuthTokenHolder
    ): WishlistRepository {
        return WishlistRepository(apiService, authTokenHolder)
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(authTokenHolder: AuthTokenHolder): AuthInterceptor {
        return AuthInterceptor(authTokenHolder)
    }

    @Provides
    @Singleton
    fun provideAuthTokenHolder(): AuthTokenHolder {
        return AuthTokenHolder()
    }
}
