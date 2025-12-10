# VidaBNB 🏡  
A modern Airbnb-style booking application built in Kotlin using Jetpack Compose and Clean Architecture.  
The app provides a smooth, intuitive user experience for browsing rentals, managing bookings, handling user authentication, and maintaining personalized wishlists — all powered by a mock backend (Mockoon).

---

## Features

### Authentication  
- Email & password login  
- Account creation  
- Persistent user session  

### Booking System  
- Browse available properties  
- View detailed property information  
- Make and manage bookings  

### Wishlist  
- Save favorite properties  
- Quickly access items marked for future interest  

### Profile  
- View and edit user details  
- Manage saved listings and reservation history  

---

## Architecture  
The project follows Clean Architecture, structured into layered modules:

/api → Networking layer (API calls, DTOs)
/di → Dependency injection setup
/model → Domain models, use cases
/ui → Jetpack Compose screens, navigation
/util → Helpers, extensions, utilities

## 🛠️ Tech Stack

- Kotlin
- Jetpack Compose
- Clean Architecture
- Mockoon (mock backend API)
- Coroutines + Flow
- Dependency Injection (Hilt)
- Retrofit / OkHttp
- Jetpack Navigation

---

## Installation & Setup

1. Clone the repository  
   ```bash
   git clone https://github.com/your-account/VidaBNB-Android.git
