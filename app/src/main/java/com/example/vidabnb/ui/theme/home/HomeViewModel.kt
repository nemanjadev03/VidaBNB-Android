package com.example.vidabnb.ui.theme.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import com.example.vidabnb.data.model.Listing
import com.example.vidabnb.data.repository.AuthRepository
import com.example.vidabnb.data.repository.ListingRepository
import com.example.vidabnb.data.repository.WishlistRepository
import com.example.vidabnb.util.Resource
import android.util.Log
import com.example.vidabnb.data.api.AuthTokenHolder


@OptIn(FlowPreview::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val listingRepository: ListingRepository,
    private val wishlistRepository: WishlistRepository,
    private val authRepository: AuthRepository,
    private val authTokenHolder: AuthTokenHolder
) : ViewModel() {

    private val _listings = MutableStateFlow<Resource<List<Listing>>>(Resource.Idle())
    val listings: StateFlow<Resource<List<Listing>>> = _listings.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _wishlistedListingIds =
        MutableStateFlow<Set<String>>(authTokenHolder.localWishlistIds)
    val wishlistedListingIds: StateFlow<Set<String>> = _wishlistedListingIds.asStateFlow()

    init {
        combine(_listings, _searchQuery.debounce(300)) { listingsResource, query ->
            when (listingsResource) {
                is Resource.Success -> {
                    val filteredListings = if (query.isBlank()) {
                        listingsResource.data
                    } else {
                        listingsResource.data?.filter {
                            it.title.contains(query, ignoreCase = true) ||
                                    it.location.contains(query, ignoreCase = true) ||
                                    it.description.contains(query, ignoreCase = true)
                        }
                    }
                    Resource.Success(filteredListings ?: emptyList())
                }
                is Resource.Error -> Resource.Error(listingsResource.message ?: "Unknown error")
                is Resource.Loading -> Resource.Loading()
                is Resource.Idle -> Resource.Idle()
            }
        }.onEach {
            _listings.value = it as Resource<List<Listing>>
        }.launchIn(viewModelScope)

        fetchListings()
        // Only fetch wishlist if user is authenticated
        fetchUserWishlistSafely()
    }

    fun fetchListings() {
        listingRepository.getListings().onEach { result ->
            _listings.value = result
        }.launchIn(viewModelScope)
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun clearUserData() {
        _wishlistedListingIds.value = emptySet()
    }

    private fun fetchUserWishlistSafely() {
        if (!authRepository.isAuthenticated()) {
            _wishlistedListingIds.value = emptySet()
            return
        }

        wishlistRepository.getUserWishlist().onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _wishlistedListingIds.value =
                        result.data?.map { it.listingId }?.toSet() ?: emptySet()
                }

                is Resource.Error -> {
                    _wishlistedListingIds.value = emptySet()
                }

                is Resource.Loading -> {
                }

                is Resource.Idle -> {
                    _wishlistedListingIds.value = emptySet()
                }
            }
        }.launchIn(viewModelScope)
    }

    fun toggleWishlist(listingId: String, add: Boolean) {
        Log.d("HomeViewModel", "toggleWishlist called - ListingId: $listingId, Add: $add")
        Log.d("HomeViewModel", "User authenticated: ${authRepository.isAuthenticated()}")
        Log.d("HomeViewModel", "Current userId: ${authRepository.getCurrentUserId()}")

        if (add) {
            Log.d("HomeViewModel", "Adding to wishlist...")
            authTokenHolder.addToLocalWishlist(listingId)
            _wishlistedListingIds.value = authTokenHolder.localWishlistIds

            wishlistRepository.addWishlistItem(listingId).onEach { result ->
                Log.d("HomeViewModel", "Add wishlist result: $result")
                when (result) {
                    is Resource.Success -> {
                        Log.d("HomeViewModel", "Successfully added to server wishlist")
                    }
                    is Resource.Error -> {
                        Log.e("HomeViewModel", "Error adding to wishlist: ${result.message}")
                        authTokenHolder.removeFromLocalWishlist(listingId)
                        _wishlistedListingIds.value = authTokenHolder.localWishlistIds
                    }
                    is Resource.Loading -> {
                        Log.d("HomeViewModel", "Adding to wishlist - loading...")
                    }
                    is Resource.Idle -> {
                        Log.d("HomeViewModel", "Adding to wishlist - idle")
                    }
                }
            }.launchIn(viewModelScope)
        } else {
            Log.d("HomeViewModel", "Removing from wishlist...")
            authTokenHolder.removeFromLocalWishlist(listingId)
            _wishlistedListingIds.value = authTokenHolder.localWishlistIds

            wishlistRepository.removeWishlistItem(listingId).onEach { result ->
                Log.d("HomeViewModel", "Remove wishlist result: $result")
                when (result) {
                    is Resource.Success -> {
                        Log.d("HomeViewModel", "Successfully removed from server wishlist")
                    }
                    is Resource.Error -> {
                        Log.e("HomeViewModel", "Error removing from wishlist: ${result.message}")
                        authTokenHolder.addToLocalWishlist(listingId)
                        _wishlistedListingIds.value = authTokenHolder.localWishlistIds
                    }
                    is Resource.Loading -> {
                        Log.d("HomeViewModel", "Removing from wishlist - loading...")
                    }
                    is Resource.Idle -> {
                        Log.d("HomeViewModel", "Removing from wishlist - idle")
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

    fun refreshWishlist() {
        Log.d("HomeViewModel", "Refreshing wishlist...")
        if (!authRepository.isAuthenticated()) {
            _wishlistedListingIds.value = emptySet()
            return
        }

        wishlistRepository.getUserWishlist().onEach { result ->
            when (result) {
                is Resource.Success -> {
                    val serverWishlistIds = result.data?.map { it.listingId }?.toSet() ?: emptySet()
                    Log.d("HomeViewModel", "Server wishlist has ${serverWishlistIds.size} items")

                    _wishlistedListingIds.value = serverWishlistIds
                }

                is Resource.Error -> {
                    Log.e("HomeViewModel", "Error fetching wishlist: ${result.message}")
                }

                is Resource.Loading -> {
                    Log.d("HomeViewModel", "Loading wishlist...")
                }

                is Resource.Idle -> {
                    Log.d("HomeViewModel", "Wishlist fetch idle")
                }
            }
        }.launchIn(viewModelScope)
    }
}
