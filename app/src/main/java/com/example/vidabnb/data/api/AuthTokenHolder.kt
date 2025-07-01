package com.example.vidabnb.data.api
import javax.inject.Inject
import javax.inject.Singleton
import com.example.vidabnb.data.model.booking.Booking

@Singleton
class AuthTokenHolder @Inject constructor() {
    var authToken: String? = null
        private set

    var userId: String? = null
        private set

    var username: String? = null // NEW: Stores the username
        private set

    var email: String? = null // NEW: Stores the email
        private set

    // Local wishlist storage for immediate UI updates
    private val _localWishlistIds = mutableSetOf<String>()
    val localWishlistIds: Set<String> get() = _localWishlistIds.toSet()

    // Local bookings storage for immediate UI updates
    private val _localBookings = mutableListOf<Booking>()
    val localBookings: List<Booking> get() = _localBookings.toList()

    fun setAuthData(token: String?, userId: String?,username: String?, email: String?) {
        this.authToken = token
        this.userId = userId
        this.username = username
        this.email = email
    }

    fun clearAuthData() {
        this.authToken = null
        this.userId = null
        this.username = null
        this.email = null
        _localWishlistIds.clear() // Clear wishlist on logout
        _localBookings.clear() // Clear bookings on logout
    }

    fun isAuthenticated(): Boolean {
        return !authToken.isNullOrBlank() && !userId.isNullOrBlank()
    }

    // Wishlist management
    fun addToLocalWishlist(listingId: String) {
        _localWishlistIds.add(listingId)
    }

    fun removeFromLocalWishlist(listingId: String) {
        _localWishlistIds.remove(listingId)
    }

    fun isInLocalWishlist(listingId: String): Boolean {
        return _localWishlistIds.contains(listingId)
    }

    fun setLocalWishlist(listingIds: Set<String>) {
        _localWishlistIds.clear()
        _localWishlistIds.addAll(listingIds)
    }

    // Booking/Trips management
    fun addLocalBooking(booking: Booking) {
        _localBookings.add(booking)
    }

    fun removeLocalBooking(bookingId: String) {
        _localBookings.removeAll { it.bookingId == bookingId }
    }

    fun updateLocalBookingStatus(bookingId: String, newStatus: String) {
        val booking = _localBookings.find { it.bookingId == bookingId }
        booking?.let {
            val updatedBooking = it.copy(status = newStatus)
            val index = _localBookings.indexOf(it)
            _localBookings[index] = updatedBooking
        }
    }

    fun setLocalBookings(bookings: List<Booking>) {
        _localBookings.clear()
        _localBookings.addAll(bookings)
    }
}
