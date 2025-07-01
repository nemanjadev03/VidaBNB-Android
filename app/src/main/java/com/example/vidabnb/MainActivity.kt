package com.example.vidabnb

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.vidabnb.ui.theme.auth.AuthScreen
import com.example.vidabnb.ui.theme.auth.AuthViewModel
import com.example.vidabnb.ui.theme.booking.BookingsScreen
import com.example.vidabnb.ui.theme.details.ListingDetailsScreen
import com.example.vidabnb.ui.theme.home.HomeScreen
import com.example.vidabnb.ui.theme.profile.ProfileScreen
import com.example.vidabnb.ui.theme.common.VidaBNBTheme
import com.example.vidabnb.ui.theme.wishlist.WishlistScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VidaBNBTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    VidBNBApp()
                }
            }
        }
    }
}

@Composable
fun VidBNBApp() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()


    val startDestination = if (isAuthenticated) "home_route" else "auth_route"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("auth_route") {
            AuthScreen(navController = navController)
        }
        composable("home_route") {
            HomeScreen(navController = navController)
        }
        composable("listing_details_route/{listingId}") { backStackEntry ->
            val listingId = backStackEntry.arguments?.getString("listingId")
            if (listingId != null) {
                ListingDetailsScreen(navController = navController, listingId = listingId)
            } else {
                Text("Error: Listing ID not provided", modifier = Modifier.fillMaxSize())
            }
        }
        composable("wishlist_route") {
            WishlistScreen(navController = navController)
        }
        composable("bookings_route") {
            BookingsScreen(navController = navController)
        }
        composable("profile_route") {
            ProfileScreen(navController = navController)
        }
    }
}
