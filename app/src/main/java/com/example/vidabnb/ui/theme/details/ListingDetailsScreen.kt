package com.example.vidabnb.ui.theme.details
import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Remove
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.vidabnb.ui.theme.common.VidaBNBTheme
import com.example.vidabnb.util.Resource
import com.example.vidabnb.data.model.booking.BookingResponse
import com.example.vidabnb.data.model.Listing
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingDetailsScreen(
    navController: NavController,
    listingId: String,
    viewModel: ListingDetailsViewModel = hiltViewModel()
) {
    val listingDetailsState by viewModel.listingDetails.collectAsState()
    val bookingResultState by viewModel.bookingResult.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(listingId) {
        viewModel.fetchListingDetails(listingId)
    }
    LaunchedEffect(bookingResultState) {
        when (bookingResultState) {
            is Resource.Success -> {
                Toast.makeText(context, "Booking successful!", Toast.LENGTH_LONG).show()
                viewModel.resetBookingResult()
            }
            is Resource.Error -> {
                Toast.makeText(context, (bookingResultState as Resource.Error).message ?: "Booking failed", Toast.LENGTH_LONG).show()
                viewModel.resetBookingResult()
            }
            is Resource.Loading -> {  }
            is Resource.Idle -> {
            }
        }
    }

    ListingDetailsScreenContent(
        listingDetailsState = listingDetailsState,
        bookingResultState = bookingResultState,
        onBackClick = { navController.popBackStack() },
        onBookNowClick = { checkIn, checkOut, guests, price ->
            viewModel.bookListing(checkIn, checkOut, guests, price)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingDetailsScreenContent(
    listingDetailsState: Resource<Listing>,
    bookingResultState: Resource<BookingResponse>,
    onBackClick: () -> Unit,
    onBookNowClick: (checkInDate: String, checkOutDate: String, numberOfGuests: Int, totalPrice: Double) -> Unit
) {
    val context = LocalContext.current

    var checkInDate by remember { mutableStateOf("") }
    var checkOutDate by remember { mutableStateOf("") }
    var numberOfGuests by remember { mutableStateOf(1) }
    val listingPricePerNight = (listingDetailsState as? Resource.Success)?.data?.pricePerNight ?: 0.0

    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Calculate total price
    val totalPrice by remember(checkInDate, checkOutDate, listingPricePerNight) {
        val price = if (checkInDate.isNotBlank() && checkOutDate.isNotBlank() && listingPricePerNight > 0) {
            try {
                val startDate = dateFormat.parse(checkInDate)
                val endDate = dateFormat.parse(checkOutDate)
                if (startDate != null && endDate != null && endDate.after(startDate)) {
                    val diff = endDate.time - startDate.time
                    val days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)
                    listingPricePerNight * days
                } else 0.0
            } catch (e: Exception) {
                0.0
            }
        } else 0.0
        mutableStateOf(price)
    }

    // Date picker dialogs
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val checkInDatePickerDialog = remember {
        DatePickerDialog(
            context,
            { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDayOfMonth: Int ->
                val selectedDate = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDayOfMonth)
                }
                checkInDate = dateFormat.format(selectedDate.time)
                // If check-out is before check-in, reset check-out
                if (checkOutDate.isNotBlank()) {
                    val currentCheckOut = dateFormat.parse(checkOutDate)
                    if (currentCheckOut != null && currentCheckOut.before(selectedDate.time)) {
                        checkOutDate = ""
                    }
                }
            }, year, month, day
        ).apply {
            datePicker.minDate = calendar.timeInMillis // Cannot select past dates
        }
    }

    val checkOutDatePickerDialog = remember {
        DatePickerDialog(
            context,
            { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDayOfMonth: Int ->
                val selectedDate = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDayOfMonth)
                }
                checkOutDate = dateFormat.format(selectedDate.time)
            }, year, month, day
        ).apply {
            // Min date for checkout should be after check-in
            if (checkInDate.isNotBlank()) {
                try {
                    val startDate = dateFormat.parse(checkInDate)
                    if (startDate != null) {
                        datePicker.minDate = startDate.time + TimeUnit.DAYS.toMillis(1) // At least 1 day after check-in
                    }
                } catch (e: Exception) {  }
            } else {
                datePicker.minDate = calendar.timeInMillis
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Listing Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        when (listingDetailsState) {
            is Resource.Loading -> {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is Resource.Success -> {
                val listing = listingDetailsState.data
                if (listing == null) {
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues), contentAlignment = Alignment.Center) {
                        Text("Listing not found.", style = MaterialTheme.typography.bodyLarge)
                    }
                }
                else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(listing.imageUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = listing.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(250.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                        }
                        item {
                            Text(
                                text = listing.title,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        item {
                            Text(
                                text = listing.location,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        item {
                            Text(
                                text = "$${listing.pricePerNight} per night",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        item {
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                        }
                        item {
                            Text(
                                text = "Description:",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = listing.description,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        item {
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                        }
                        item {
                            Text(
                                text = "Details:",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Beds: ${listing.beds}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Max Guests: ${listing.guests}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Hosted by: ${listing.hostName}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        item {
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                        }
                        item {
                            Text(
                                text = "Select Dates and Guests:",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))

                            // Check-in Date Picker
                            OutlinedTextField(
                                value = checkInDate,
                                onValueChange = { /* Read-only */ },
                                label = { Text("Check-in Date") },
                                readOnly = true,
                                trailingIcon = {
                                    IconButton(onClick = { checkInDatePickerDialog.show() }) {
                                        Icon(Icons.Default.DateRange, contentDescription = "Select Check-in Date")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )
                            Spacer(Modifier.height(16.dp))

                            // Check-out Date Picker
                            OutlinedTextField(
                                value = checkOutDate,
                                onValueChange = { /* Read-only */ },
                                label = { Text("Check-out Date") },
                                readOnly = true,
                                trailingIcon = {
                                    IconButton(onClick = { checkOutDatePickerDialog.show() }) {
                                        Icon(Icons.Default.DateRange, contentDescription = "Select Check-out Date")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )
                            Spacer(Modifier.height(16.dp))

                            // Number of Guests Selector
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Number of Guests:", style = MaterialTheme.typography.bodyLarge)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { if (numberOfGuests > 1) numberOfGuests-- },
                                        enabled = numberOfGuests > 1
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = "Decrease guests")
                                    }
                                    Text(
                                        text = "$numberOfGuests",
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )
                                    IconButton(
                                        onClick = { if (numberOfGuests < listing.guests) numberOfGuests++ },
                                        enabled = numberOfGuests < listing.guests // Max guests from listing
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Increase guests")
                                    }
                                }
                            }
                            Spacer(Modifier.height(16.dp))

                            // Total Price Display
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Total Price:",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "$${String.format("%.2f", totalPrice)}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        item {
                            // Booking Section
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    if (checkInDate.isBlank() || checkOutDate.isBlank()) {
                                        Toast.makeText(context, "Please select check-in and check-out dates.", Toast.LENGTH_SHORT).show()
                                    } else if (totalPrice <= 0) {
                                        Toast.makeText(context, "Please select valid dates for booking.", Toast.LENGTH_SHORT).show()
                                    } else {
                                        onBookNowClick(checkInDate, checkOutDate, numberOfGuests, totalPrice)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = bookingResultState !is Resource.Loading, // Disable button while booking
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                if (bookingResultState is Resource.Loading) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                                } else {
                                    Text("Book Now", style = MaterialTheme.typography.titleMedium)
                                }
                            }
                            // Show booking result feedback
                            if (bookingResultState is Resource.Error) {
                                Text(
                                    text = (bookingResultState as Resource.Error).message ?: "Booking error",
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
            is Resource.Error -> {
                val errorMessage = listingDetailsState.message
                Box(modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues), contentAlignment = Alignment.Center) {
                    Text("Error loading listing details: $errorMessage", color = MaterialTheme.colorScheme.error)
                }
            }
            is Resource.Idle -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Initial state", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun ListingDetailsScreenPreview() {
    VidaBNBTheme {
        ListingDetailsScreenContent(
            listingDetailsState = Resource.Success(
                Listing(
                    id = "preview_listing_id",
                    title = "Stunning Beachfront Villa with Pool",
                    description = "Experience luxury in this spacious villa directly on the beach. Perfect for families or large groups, featuring a private pool, breathtaking ocean views, and direct beach access. Fully equipped kitchen, multiple bedrooms, and outdoor dining area.",
                    imageUrl = "",
                    pricePerNight = 350.0,
                    location = "Santorini, Greece",
                    beds = 4,
                    guests = 8,
                    hostName = "Jane Smith"
                )
            ),
            bookingResultState = Resource.Loading(),
            onBackClick = {},
            onBookNowClick = { _, _, _, _ -> }
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun ListingDetailsScreenLoadingPreview() {
    VidaBNBTheme {
        ListingDetailsScreenContent(
            listingDetailsState = Resource.Loading(),
            bookingResultState = Resource.Loading(),
            onBackClick = {},
            onBookNowClick = { _, _, _, _ -> }
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun ListingDetailsScreenErrorPreview() {
    VidaBNBTheme {
        ListingDetailsScreenContent(
            listingDetailsState = Resource.Error("Failed to load listing. Please check your connection."),
            bookingResultState = Resource.Loading(),
            onBackClick = {},
            onBookNowClick = { _, _, _, _ -> }
        )
    }
}
