package com.example.vidabnb.data.model

data class Listing(
    val id: String,
    val title: String,
    val description: String,
    val pricePerNight: Double,
    val imageUrl: String,
    val location: String,
    val beds: Int,
    val guests: Int,
    val hostName: String,
)
