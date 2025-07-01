package com.example.vidabnb.data.api
import com.example.vidabnb.data.model.Listing
import com.example.vidabnb.data.model.login.LoginRequest
import com.example.vidabnb.data.model.login.LoginResponse
import retrofit2.http.GET
import retrofit2.http.DELETE
import retrofit2.http.PUT
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Body
import retrofit2.Response
import com.example.vidabnb.data.model.booking.BookingRequest
import com.example.vidabnb.data.model.booking.Booking
import com.example.vidabnb.data.model.booking.BookingResponse
import com.example.vidabnb.data.model.Wishlist
import com.example.vidabnb.data.model.signup.SignupRequest
import com.example.vidabnb.data.model.signup.SignupResponse
interface VidaBNBApiService {
    @GET("listings")
    suspend fun getListings(): List<Listing>

    @GET("listings/{id}")
    suspend fun getListingDetails(@Path("id") id: String): Listing

    @POST("auth/login")
    suspend fun loginUser(@Body request: LoginRequest): Response<LoginResponse>

    @POST("bookings")
    suspend fun createBooking(@Body request: BookingRequest): Response<BookingResponse>

    @GET("users/{userId}/bookings")
    suspend fun getUserBookings(@Path("userId") userId: String): List<Booking>

    @DELETE("bookings/{bookingId}")
    suspend fun cancelBooking(@Path("bookingId") bookingId: String): Response<Unit>

    @PUT("bookings/{bookingId}/status")
    suspend fun updateBookingStatus(@Path("bookingId") bookingId: String, @Body statusUpdate: Map<String, String>): Response<BookingResponse>

    @POST("auth/signup")
    suspend fun signupUser(@Body request: SignupRequest): Response<SignupResponse>

    @POST("users/{userId}/wishlists/{listingId}")
    suspend fun addWishlistItem(
        @Path("userId") userId: String,
        @Path("listingId") listingId: String
    ): Response<Wishlist>

    @DELETE("users/{userId}/wishlists/{listingId}")
    suspend fun removeWishlistItem(
        @Path("userId") userId: String,
        @Path("listingId") listingId: String
    ): Response<Unit>

    @GET("users/{userId}/wishlists")
    suspend fun getUserWishlist(@Path("userId") userId: String): List<Wishlist>
}
