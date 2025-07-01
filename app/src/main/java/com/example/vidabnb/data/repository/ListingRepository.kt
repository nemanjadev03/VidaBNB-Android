package com.example.vidabnb.data.repository

import com.example.vidabnb.data.api.VidaBNBApiService
import com.example.vidabnb.data.model.Listing
import com.example.vidabnb.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class ListingRepository @Inject constructor(private val apiService: VidaBNBApiService){
    fun getListings(): Flow<Resource<List<Listing>>> = flow {
        emit(Resource.Loading())
        try {
            val listings = apiService.getListings()
            emit(Resource.Success(listings))
        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected HTTP error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
        } catch (e: Exception) {
            emit(Resource.Error("An unknown error occurred: ${e.localizedMessage}"))
        }
    }

    fun getListingDetails(id: String): Flow<Resource<Listing>> = flow {
        emit(Resource.Loading())
        try {
            val listing = apiService.getListingDetails(id)
            emit(Resource.Success(listing))
        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected HTTP error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
        } catch (e: Exception) {
            emit(Resource.Error("An unknown error occurred: ${e.localizedMessage}"))
        }
    }
}