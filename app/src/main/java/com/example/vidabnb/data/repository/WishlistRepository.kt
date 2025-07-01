package com.example.vidabnb.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import com.example.vidabnb.data.api.VidaBNBApiService
import com.example.vidabnb.data.api.AuthTokenHolder
import com.example.vidabnb.util.Resource
import com.example.vidabnb.data.model.Wishlist
import android.util.Log

class WishlistRepository @Inject constructor(
    private val apiService: VidaBNBApiService,
    private val authTokenHolder: AuthTokenHolder
) {

    fun addWishlistItem(listingId: String): Flow<Resource<Wishlist>> = flow {
        emit(Resource.Loading())
        val userId = authTokenHolder.userId
        Log.d("WishlistRepository", "Adding to wishlist - UserId: $userId, ListingId: $listingId")

        if (userId == null) {
            Log.e("WishlistRepository", "User not logged in")
            emit(Resource.Error("User not logged in. Cannot add to wishlist."))
            return@flow
        }

        try {
            Log.d("WishlistRepository", "Calling API: POST /users/$userId/wishlists/$listingId")
            val response = apiService.addWishlistItem(userId, listingId)
            Log.d("WishlistRepository", "API Response code: ${response.code()}")

            if (response.isSuccessful) {
                val wishlistItem = response.body()
                if (wishlistItem != null) {
                    Log.d(
                        "WishlistRepository",
                        "Successfully added to wishlist: ${wishlistItem.listingTitle}"
                    )
                    emit(Resource.Success(wishlistItem))
                } else {
                    Log.e("WishlistRepository", "Response body is null")
                    emit(Resource.Error("Successfully added to wishlist, but no item data received."))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("WishlistRepository", "API Error: ${response.code()} - $errorBody")
                emit(Resource.Error(errorBody ?: "Failed to add to wishlist: ${response.code()}"))
            }
        } catch (e: HttpException) {
            Log.e("WishlistRepository", "HTTP Exception: ${e.message}")
            emit(Resource.Error("HTTP Error: ${e.code()} - ${e.message}"))
        } catch (e: IOException) {
            Log.e("WishlistRepository", "IO Exception: ${e.message}")
            if (e.message?.contains("malformed JSON") == true) {
                emit(Resource.Error("Server returned HTML instead of JSON. Check your Mockoon route: POST /users/:userId/wishlists/:listingId"))
            } else {
                emit(Resource.Error("Couldn't reach server. Check your internet connection."))
            }
        } catch (e: Exception) {
            Log.e("WishlistRepository", "Unknown Exception: ${e.message}")
            emit(Resource.Error("An unknown error occurred: ${e.localizedMessage}"))
        }
    }

    fun removeWishlistItem(listingId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        val userId = authTokenHolder.userId
        Log.d(
            "WishlistRepository",
            "Removing from wishlist - UserId: $userId, ListingId: $listingId"
        )

        if (userId == null) {
            Log.e("WishlistRepository", "User not logged in")
            emit(Resource.Error("User not logged in. Cannot remove from wishlist."))
            return@flow
        }

        try {
            Log.d("WishlistRepository", "Calling API: DELETE /users/$userId/wishlists/$listingId")
            val response = apiService.removeWishlistItem(userId, listingId)
            Log.d("WishlistRepository", "API Response code: ${response.code()}")

            if (response.isSuccessful) {
                Log.d("WishlistRepository", "Successfully removed from wishlist")
                emit(Resource.Success(Unit))
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("WishlistRepository", "API Error: ${response.code()} - $errorBody")
                emit(Resource.Error(errorBody ?: "Failed to remove from wishlist: ${response.code()}"))
            }
        } catch (e: HttpException) {
            Log.e("WishlistRepository", "HTTP Exception: ${e.message}")
            emit(Resource.Error("HTTP Error: ${e.code()} - ${e.message}"))
        } catch (e: IOException) {
            Log.e("WishlistRepository", "IO Exception: ${e.message}")
            if (e.message?.contains("malformed JSON") == true) {
                emit(Resource.Error("Server returned HTML instead of JSON. Check your Mockoon route: DELETE /users/:userId/wishlists/:listingId"))
            } else {
                emit(Resource.Error("Couldn't reach server. Check your internet connection."))
            }
        } catch (e: Exception) {
            Log.e("WishlistRepository", "Unknown Exception: ${e.message}")
            emit(Resource.Error("An unknown error occurred: ${e.localizedMessage}"))
        }
    }


    fun getUserWishlist(): Flow<Resource<List<Wishlist>>> = flow {
        emit(Resource.Loading())
        val userId = authTokenHolder.userId
        Log.d("WishlistRepository", "Fetching user wishlist - UserId: $userId")

        if (userId == null) {
            Log.e("WishlistRepository", "User not logged in")
            emit(Resource.Error("User not logged in. Cannot fetch wishlist."))
            return@flow
        }

        try {
            Log.d("WishlistRepository", "Calling API: GET /users/$userId/wishlists")
            val wishlist = apiService.getUserWishlist(userId)
            Log.d("WishlistRepository", "Successfully fetched wishlist with ${wishlist.size} items")
            emit(Resource.Success(wishlist))
        } catch (e: HttpException) {
            Log.e("WishlistRepository", "HTTP Exception: ${e.message}")
            emit(Resource.Error("HTTP Error: ${e.code()} - ${e.message}"))
        } catch (e: IOException) {
            Log.e("WishlistRepository", "IO Exception: ${e.message}")
            if (e.message?.contains("malformed JSON") == true) {
                emit(Resource.Error("Server returned HTML instead of JSON. Check your Mockoon route: GET /users/:userId/wishlists"))
            } else {
                emit(Resource.Error("Couldn't reach server. Check your internet connection."))
            }
        } catch (e: Exception) {
            Log.e("WishlistRepository", "Unknown Exception: ${e.message}")
            emit(Resource.Error("An unknown error occurred: ${e.localizedMessage}"))
        }
    }
}
