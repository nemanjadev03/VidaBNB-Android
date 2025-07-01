package com.example.vidabnb.data.repository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import com.example.vidabnb.data.api.VidaBNBApiService
import com.example.vidabnb.data.model.booking.BookingResponse
import com.example.vidabnb.data.model.booking.Booking
import com.example.vidabnb.data.model.booking.BookingRequest
import com.example.vidabnb.util.Resource

class BookingRepository @Inject constructor(
    private val apiService: VidaBNBApiService
) {
    fun createBooking(request: BookingRequest): Flow<Resource<BookingResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.createBooking(request)
            if (response.isSuccessful) {
                val bookingResponse = response.body()
                if (bookingResponse != null) {
                    emit(Resource.Success(bookingResponse))
                } else {
                    emit(Resource.Error("Booking created but no response body received."))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                emit(Resource.Error(errorBody ?: "Booking failed with code: ${response.code()}"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected HTTP error occurred during booking"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection for booking."))
        } catch (e: Exception) {
            emit(Resource.Error("An unknown error occurred during booking: ${e.localizedMessage}"))
        }
    }

    fun getUserBookings(userId: String): Flow<Resource<List<Booking>>> = flow {
        emit(Resource.Loading())
        try {
            val bookings = apiService.getUserBookings(userId)
            emit(Resource.Success(bookings))
        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected HTTP error occurred while fetching bookings"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection for bookings."))
        } catch (e: Exception) {
            emit(Resource.Error("An unknown error occurred while fetching bookings: ${e.localizedMessage}"))
        }
    }

    fun cancelBooking(bookingId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.cancelBooking(bookingId)
            if (response.isSuccessful) {
                emit(Resource.Success(Unit))
            } else {
                val errorBody = response.errorBody()?.string()
                emit(Resource.Error(errorBody ?: "Failed to cancel booking: ${response.code()}"))
            }
        } catch (e: HttpException) {
            emit(
                Resource.Error(
                    e.localizedMessage
                        ?: "An unexpected HTTP error occurred while cancelling booking"
                )
            )
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
        } catch (e: Exception) {
            emit(Resource.Error("An unknown error occurred while cancelling booking: ${e.localizedMessage}"))
        }
    }
}
