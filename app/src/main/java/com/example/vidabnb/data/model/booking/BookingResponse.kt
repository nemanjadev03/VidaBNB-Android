package com.example.vidabnb.data.model.booking
import com.google.gson.annotations.SerializedName

data class BookingResponse(
    @SerializedName("bookingId") val bookingId: String,
    @SerializedName("listingId") val listingId: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("checkInDate") val checkInDate: String,
    @SerializedName("checkOutDate") val checkOutDate: String,
    @SerializedName("numberOfGuests") val numberOfGuests: Int,
    @SerializedName("totalPrice") val totalPrice: Double,
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String? = null
)
