package com.example.vidabnb.ui.theme.wishlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.vidabnb.data.model.Wishlist
import com.example.vidabnb.ui.theme.components.VidBnBBottomNavBar
import com.example.vidabnb.util.Resource
import com.example.vidabnb.ui.theme.common.VidaBNBTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistScreen(
    navController: NavController,
    viewModel: WishlistViewModel = hiltViewModel()
) {
    val userWishlistState by viewModel.userWishlist.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchUserWishlist()
    }

    LaunchedEffect(Unit) {
        viewModel.fetchUserWishlist()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Wishlists") },
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
            when (userWishlistState) {
                is Resource.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is Resource.Success -> {
                    val wishlistItems = (userWishlistState as Resource.Success).data
                    if (wishlistItems.isNullOrEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Your wishlist is empty.", style = MaterialTheme.typography.bodyLarge)
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(vertical = 8.dp, horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(wishlistItems) { item ->
                                WishlistItemCard(
                                    wishlistItem = item,
                                    onRemoveClick = { listingId ->
                                        viewModel.removeWishlistItem(listingId)
                                    },
                                    onItemClick = { listingId ->
                                        navController.navigate("listing_details_route/$listingId")
                                    }
                                )
                            }
                        }
                    }
                }
                is Resource.Error -> {
                    val errorMessage = (userWishlistState as Resource.Error).message
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error loading wishlist: $errorMessage", color = MaterialTheme.colorScheme.error)
                    }
                }
                is Resource.Idle -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistItemCard(wishlistItem: Wishlist, onRemoveClick: (String) -> Unit, onItemClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick(wishlistItem.listingId) }, // Make the whole card clickable
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = wishlistItem.listingImageUrl,
                contentDescription = wishlistItem.listingTitle,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = wishlistItem.listingTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = wishlistItem.location,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$${wishlistItem.pricePerNight} per night",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            IconButton(onClick = { onRemoveClick(wishlistItem.listingId) }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove from Wishlist",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WishlistScreenPreview() {
    VidaBNBTheme {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Wishlist Screen Preview (shows empty state)", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WishlistItemCardPreview() {
    VidaBNBTheme {
        WishlistItemCard(
            wishlistItem = Wishlist(
                wishlistItemId = "wli-1",
                listingId = "lst-101",
                listingTitle = "Sunny Beach Villa",
                listingImageUrl = "",
                location = "Dubrovnik, Croatia",
                pricePerNight = 300.0
            ),
            onRemoveClick = {},
            onItemClick = {}
        )
    }
}
