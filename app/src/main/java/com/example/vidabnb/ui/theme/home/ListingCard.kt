package com.example.vidabnb.ui.theme.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import com.example.vidabnb.data.model.Listing
import com.example.vidabnb.ui.theme.common.VidaBNBTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingCard(
    listing: Listing,
    onListingClick: (String) -> Unit,
    onToggleWishlist: (String, Boolean) -> Unit,
    isWishlisted: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .clickable { onListingClick(listing.id) },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            ) {
                val imageUrl = remember { listing.imageUrl }
                AsyncImage(
                    model = imageUrl,
                    contentDescription = listing.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                IconButton(
                    onClick = { onToggleWishlist(listing.id, !isWishlisted) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = if (isWishlisted) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = if (isWishlisted) "Remove from Wishlist" else "Add to Wishlist",
                        tint = if (isWishlisted) Color.Red else Color.White
                    )
                }
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = listing.title,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = listing.location,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "$${listing.pricePerNight} per night",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ListingCardPreview() {
    VidaBNBTheme {
        ListingCard(
            listing = Listing(
                id = "1",
                title = "Cozy Apartment in NYC",
                description = "A very cozy apartment.",
                pricePerNight = 120.0,
                imageUrl = "https://picsum.photos/id/237/600/400",
                location = "New York, USA",
                beds = 3,
                guests = 5,
                hostName = "Nemanja"
            ),
            onListingClick = {},
            onToggleWishlist = {_,_->},
            isWishlisted = false
        )
    }
}
