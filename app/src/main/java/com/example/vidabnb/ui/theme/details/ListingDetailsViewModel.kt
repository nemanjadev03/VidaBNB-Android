package com.example.vidabnb.ui.theme.details
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import com.example.vidabnb.data.model.booking.BookingRequest
import com.example.vidabnb.data.model.booking.BookingResponse
import com.example.vidabnb.data.model.Listing
import com.example.vidabnb.data.repository.ListingRepository
import com.example.vidabnb.data.repository.AuthRepository
import com.example.vidabnb.data.repository.BookingRepository
import com.example.vidabnb.data.api.AuthTokenHolder
import com.example.vidabnb.util.Resource

@HiltViewModel
class ListingDetailsViewModel @Inject constructor(
    private val listingRepository: ListingRepository,
    private val bookingRepository: BookingRepository,
    private val authRepository: AuthRepository,
    private val authTokenHolder: AuthTokenHolder
) : ViewModel() {

    private val _listingDetails = MutableStateFlow<Resource<Listing>>(Resource.Idle())
    val listingDetails: StateFlow<Resource<Listing>> = _listingDetails.asStateFlow()

    private val _bookingResult = MutableStateFlow<Resource<BookingResponse>>(Resource.Idle())
    val bookingResult: StateFlow<Resource<BookingResponse>> = _bookingResult.asStateFlow()


    fun fetchListingDetails(id: String) {
        listingRepository.getListingDetails(id).onEach { result ->
            _listingDetails.value = result
        }.launchIn(viewModelScope)
    }

    fun bookListing(
        checkInDate: String,
        checkOutDate: String,
        numberOfGuests: Int,
        totalPrice: Double
    ) {
        val currentListing = (listingDetails.value as? Resource.Success)?.data
        val userId = authRepository.getCurrentUserId()

        if (currentListing == null) {
            _bookingResult.value = Resource.Error("Listing not available for booking.")
            return
        }
        if (userId == null) {
            _bookingResult.value = Resource.Error("User not logged in. Please log in to book.")
            return
        }

        val bookingRequest = BookingRequest(
            listingId = currentListing.id,
            userId = userId,
            checkInDate = checkInDate,
            checkOutDate = checkOutDate,
            numberOfGuests = numberOfGuests,
            totalPrice = totalPrice
        )

        _bookingResult.value = Resource.Loading()
        bookingRepository.createBooking(bookingRequest).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    android.util.Log.d("ListingDetailsViewModel", "Booking successful")

                    addToLocalBookings(
                        listingId = currentListing.id,
                        listingTitle = currentListing.title,
                        listingImageUrl = currentListing.imageUrl,
                        checkInDate = checkInDate,
                        checkOutDate = checkOutDate,
                        numberOfGuests = numberOfGuests,
                        totalPrice = totalPrice
                    )

                    _bookingResult.value = result
                }

                is Resource.Error -> {
                    android.util.Log.e(
                        "ListingDetailsViewModel",
                        "Booking failed: ${result.message}"
                    )
                    _bookingResult.value = result
                }

                is Resource.Loading -> {
                    _bookingResult.value = result
                }

                is Resource.Idle -> {
                    _bookingResult.value = result
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun addToLocalBookings(
        listingId: String,
        listingTitle: String,
        listingImageUrl: String,
        checkInDate: String,
        checkOutDate: String,
        numberOfGuests: Int,
        totalPrice: Double
    ) {
        val booking = com.example.vidabnb.data.model.booking.Booking(
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
        android.util.Log.d("ListingDetailsViewModel", "Added booking to local state: $listingTitle")
    }

    fun resetBookingResult() {
        _bookingResult.value = Resource.Idle()
    }
}
