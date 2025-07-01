package com.example.vidabnb.data.model
import com.google.gson.annotations.SerializedName

data class Wishlist(
    @SerializedName("wishlistItemId") val wishlistItemId: String,
    @SerializedName("listingId") val listingId: String,
    @SerializedName("listingTitle") val listingTitle: String,
    @SerializedName("listingImageUrl") val listingImageUrl: String,
    @SerializedName("location") val location: String,
    @SerializedName("pricePerNight") val pricePerNight: Double
)
