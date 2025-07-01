package com.example.vidabnb.ui.theme.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import com.example.vidabnb.data.model.booking.Booking
import com.example.vidabnb.data.repository.AuthRepository
import com.example.vidabnb.data.repository.BookingRepository
import com.example.vidabnb.util.Resource
import com.example.vidabnb.data.api.AuthTokenHolder
import android.util.Log

@HiltViewModel
class BookingViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val authRepository: AuthRepository,
    private val authTokenHolder: AuthTokenHolder
) : ViewModel() {

    private val _userBookings = MutableStateFlow<Resource<List<Booking>>>(Resource.Idle())
    val userBookings: StateFlow<Resource<List<Booking>>> = _userBookings.asStateFlow()

    init {
        fetchUserBookings()
    }

    fun fetchUserBookings() {
        val userId = authRepository.getCurrentUserId()
        if (userId == null) {
            _userBookings.value = Resource.Success(emptyList())
            return
        }


        createBookingsFromLocalData()

        _userBookings.value = Resource.Loading()
        bookingRepository.getUserBookings(userId).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    val serverBookings = result.data ?: emptyList()
                    Log.d("BookingViewModel", "Server returned ${serverBookings.size} bookings")

                    authTokenHolder.setLocalBookings(serverBookings)
                    createBookingsFromLocalData()
                }

                is Resource.Error -> {
                    Log.e("BookingViewModel", "Error fetching bookings: ${result.message}")
                    createBookingsFromLocalData()
                }

                is Resource.Loading -> {
                }

                is Resource.Idle -> {
                    createBookingsFromLocalData()
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun createBookingsFromLocalData() {
        val localBookings = authTokenHolder.localBookings
        Log.d("BookingViewModel", "Using ${localBookings.size} local bookings")
        _userBookings.value = Resource.Success(localBookings)
    }

    fun addLocalBooking(
        listingId: String,
        listingTitle: String,
        listingImageUrl: String,
        checkInDate: String,
        checkOutDate: String,
        numberOfGuests: Int,
        totalPrice: Double
    ) {
        val booking = Booking(
            bookingId = "local_${System.currentTimeMillis()}",
            listingId = listingId,
            listingTitle = listingTitle,
            listingImageUrl = listingImageUrl,
            userId = authRepository.getCurrentUserId() ?: "",
            checkInDate = checkInDate,
            checkOutDate = checkOutDate,
            numberOfGuests = numberOfGuests,
            totalPrice = totalPrice,
            status = "confirmed"
        )

        authTokenHolder.addLocalBooking(booking)
        createBookingsFromLocalData()
        Log.d("BookingViewModel", "Added local booking: ${booking.listingTitle}")
    }

    fun cancelBooking(bookingId: String) {
        Log.d("BookingViewModel", "Cancelling booking: $bookingId")

        authTokenHolder.updateLocalBookingStatus(bookingId, "cancelled")
        createBookingsFromLocalData()

        bookingRepository.cancelBooking(bookingId).onEach { result ->
            when (result) {
                is Resource.Success<Unit> -> {
                    Log.d("BookingViewModel", "Successfully cancelled booking on server")
                }

                is Resource.Error<Unit> -> {
                    Log.e("BookingViewModel", "Error cancelling booking: ${result.message}")
                    authTokenHolder.updateLocalBookingStatus(bookingId, "confirmed")
                    createBookingsFromLocalData()
                }

                is Resource.Loading<Unit> -> {
                }

                is Resource.Idle<Unit> -> {
                }
            }
        }.launchIn(viewModelScope)
    }

    fun removeBooking(bookingId: String) {
        Log.d("BookingViewModel", "Removing booking: $bookingId")

        authTokenHolder.removeLocalBooking(bookingId)
        createBookingsFromLocalData()

        bookingRepository.cancelBooking(bookingId).onEach { result ->
            when (result) {
                is Resource.Success<Unit> -> {
                    Log.d("BookingViewModel", "Successfully removed booking from server")
                }

                is Resource.Error<Unit> -> {
                    Log.e("BookingViewModel", "Error removing booking: ${result.message}")
                }

                is Resource.Loading<Unit> -> {
                }

                is Resource.Idle<Unit> -> {
                }
            }
        }.launchIn(viewModelScope)
    }
}
