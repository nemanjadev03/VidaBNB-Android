package com.example.vidabnb.ui.theme.wishlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vidabnb.data.api.AuthTokenHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import com.example.vidabnb.data.model.Wishlist
import com.example.vidabnb.data.repository.AuthRepository
import com.example.vidabnb.data.repository.ListingRepository
import com.example.vidabnb.data.repository.WishlistRepository
import com.example.vidabnb.util.Resource


@HiltViewModel
class WishlistViewModel @Inject constructor(
    private val wishlistRepository: WishlistRepository,
    private val authRepository: AuthRepository,
    private val listingRepository: ListingRepository,
    private val authTokenHolder: AuthTokenHolder // Add shared state access
) : ViewModel() {

    private val _userWishlist = MutableStateFlow<Resource<List<Wishlist>>>(Resource.Idle())
    val userWishlist: StateFlow<Resource<List<Wishlist>>> = _userWishlist.asStateFlow()

    init {
        fetchUserWishlist()
    }

    fun fetchUserWishlist() {
        val userId = authRepository.getCurrentUserId()
        if (userId == null) {
            _userWishlist.value = Resource.Success(emptyList())
            return
        }


        createWishlistFromLocalData()
    }

    private fun createWishlistFromLocalData() {
        val localWishlistIds = authTokenHolder.localWishlistIds
        android.util.Log.d(
            "WishlistViewModel",
            "Creating wishlist from ${localWishlistIds.size} local IDs"
        )

        if (localWishlistIds.isEmpty()) {
            _userWishlist.value = Resource.Success(emptyList())
            return
        }


        _userWishlist.value = Resource.Loading()

        listingRepository.getListings().onEach { listingsResult ->
            when (listingsResult) {
                is Resource.Success -> {
                    val allListings = listingsResult.data ?: emptyList()


                    val wishlistItems = localWishlistIds.mapNotNull { listingId ->
                        val listing = allListings.find { it.id == listingId }
                        listing?.let {
                            Wishlist(
                                wishlistItemId = "w_$listingId",
                                listingId = listingId,
                                listingTitle = it.title,
                                listingImageUrl = it.imageUrl,
                                location = it.location,
                                pricePerNight = it.pricePerNight
                            )
                        }
                    }

                    android.util.Log.d(
                        "WishlistViewModel",
                        "Created ${wishlistItems.size} wishlist items from server data"
                    )
                    _userWishlist.value = Resource.Success(wishlistItems)
                }

                is Resource.Error -> {
                    android.util.Log.e(
                        "WishlistViewModel",
                        "Error fetching listings: ${listingsResult.message}"
                    )

                    _userWishlist.value = Resource.Success(emptyList())
                }

                is Resource.Loading -> {

                }

                is Resource.Idle -> {

                }
            }
        }.launchIn(viewModelScope)
    }

    fun removeWishlistItem(listingId: String) {

        authTokenHolder.removeFromLocalWishlist(listingId)


        createWishlistFromLocalData()

        wishlistRepository.removeWishlistItem(listingId).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    android.util.Log.d("WishlistViewModel", "Successfully removed from server")
                }

                is Resource.Error -> {
                    android.util.Log.e(
                        "WishlistViewModel",
                        "Error removing from server: ${result.message}"
                    )
                    // Revert if server removal fails
                    authTokenHolder.addToLocalWishlist(listingId)
                    createWishlistFromLocalData()
                }

                else -> {
                }
            }
        }.launchIn(viewModelScope)
    }
}
