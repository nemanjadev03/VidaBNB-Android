package com.example.vidabnb.ui.theme.home

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.vidabnb.ui.theme.components.VidBnBBottomNavBar
import com.example.vidabnb.util.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val listingsState by viewModel.listings.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val wishlistedListingIds by viewModel.wishlistedListingIds.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.refreshWishlist()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("VidBnB") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            VidBnBBottomNavBar(navController = navController)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                label = { Text("Search listings...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                singleLine = true
            )

            when (listingsState) {
                is Resource.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is Resource.Success -> {
                    val listings = (listingsState as Resource.Success).data
                    if (listings.isNullOrEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No listings found matching your search.", style = MaterialTheme.typography.bodyLarge)
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(bottom = 16.dp) // Add padding for bottom bar
                        ) {
                            items(listings) { listing ->
                                val isWishlisted = wishlistedListingIds.contains(listing.id)
                                ListingCard(
                                    listing = listing,
                                    onListingClick = { id ->
                                        navController.navigate("listing_details_route/$id")
                                        Toast.makeText(context, "Clicked on listing: $id", Toast.LENGTH_SHORT).show()
                                    },
                                    onToggleWishlist = { id, add ->
                                        viewModel.toggleWishlist(id, add)
                                    },
                                    isWishlisted = isWishlisted
                                )
                            }
                        }
                    }
                }
                is Resource.Error -> {
                    val errorMessage = (listingsState as Resource.Error).message
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error loading listings: $errorMessage", color = MaterialTheme.colorScheme.error)
                    }
                }
                is Resource.Idle -> {
                    // Initial state, show loading indicator
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}
