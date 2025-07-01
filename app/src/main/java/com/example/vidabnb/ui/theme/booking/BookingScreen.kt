package com.example.vidabnb.ui.theme.booking

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.vidabnb.data.model.booking.Booking
import com.example.vidabnb.ui.theme.components.VidBnBBottomNavBar
import com.example.vidabnb.util.Resource
import com.example.vidabnb.ui.theme.common.VidaBNBTheme
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingsScreen(
    navController: NavController,
    viewModel: BookingViewModel = hiltViewModel()
) {
    val userBookingsState by viewModel.userBookings.collectAsState()
    val swipeRefreshState =
        rememberSwipeRefreshState(isRefreshing = userBookingsState is Resource.Loading<*>)

    LaunchedEffect(Unit) {
        viewModel.fetchUserBookings()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Trips") },
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
        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = { viewModel.fetchUserBookings() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (userBookingsState) {
                is Resource.Loading<List<Booking>> -> {
                    if (userBookingsState.data == null) { // Only show full screen indicator if no data yet
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                }
                is Resource.Success<List<Booking>> -> {
                    val bookings = userBookingsState.data
                    if (bookings.isNullOrEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("You have no upcoming trips.", style = MaterialTheme.typography.bodyLarge)
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(vertical = 8.dp, horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(bookings) { booking ->
                                BookingCard(
                                    booking = booking,
                                    onCancelClick = { viewModel.cancelBooking(it) },
                                    onRemoveClick = { viewModel.removeBooking(it) }
                                )
                            }
                        }
                    }
                }
                is Resource.Error<List<Booking>> -> {
                    val errorMessage = userBookingsState.message
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error loading trips: $errorMessage", color = MaterialTheme.colorScheme.error)
                    }
                }
                is Resource.Idle<List<Booking>> -> {
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
fun BookingCard(
    booking: Booking,
    onCancelClick: (String) -> Unit = {},
    onRemoveClick: (String) -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            AsyncImage(
                model = booking.listingImageUrl,
                contentDescription = booking.listingTitle,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = booking.listingTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Dates: ${booking.checkInDate} - ${booking.checkOutDate}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Guests: ${booking.numberOfGuests}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Total Price: $${booking.totalPrice}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Status: ${booking.status.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = when (booking.status) {
                            "confirmed" -> MaterialTheme.colorScheme.primary
                            "cancelled" -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.secondary
                        }
                    )

                    if (booking.status == "confirmed") {
                        OutlinedButton(
                            onClick = { onRemoveClick(booking.bookingId) },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BookingsScreenPreview() {
    VidaBNBTheme {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Bookings Screen Preview (shows empty state)", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BookingCardPreview() {
    VidaBNBTheme {
        BookingCard(
            booking = Booking(
                bookingId = "bkg-123",
                listingId = "lst-456",
                listingTitle = "Cozy Mountain Cabin",
                listingImageUrl = "",
                userId = "user1",
                checkInDate = "2025-07-20",
                checkOutDate = "2025-07-25",
                numberOfGuests = 3,
                totalPrice = 750.0,
                status = "confirmed"
            ),
            onCancelClick = {},
            onRemoveClick = {}
        )
    }
}
