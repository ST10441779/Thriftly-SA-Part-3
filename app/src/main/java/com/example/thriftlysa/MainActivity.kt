
package com.example.thriftlysa

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Build
import android.net.ConnectivityManager
import android.net.Network
import android.util.Log

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.core.content.ContextCompat

import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search

import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

import com.google.firebase.messaging.FirebaseMessaging

import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retrieve the Firebase Cloud Messaging token.
        getFirebaseToken()

        setContent {
            MaterialTheme {
                ThriftlyApp(context = this)
            }
        }
    }
}

// =====================================================
// FIREBASE CLOUD MESSAGING TOKEN
// =====================================================

private fun getFirebaseToken() {

    FirebaseMessaging.getInstance().token
        .addOnCompleteListener { task ->

            if (!task.isSuccessful) {

                Log.e(
                    "ThriftlyFCM",
                    "Could not retrieve Firebase token",
                    task.exception
                )

                return@addOnCompleteListener
            }

            val token = task.result

            Log.d(
                "ThriftlyFCM",
                "Firebase token successfully retrieved"
            )

            // Token is printed for local testing only.
            // Do not share it publicly.
            Log.d(
                "ThriftlyFCM",
                "FCM_TOKEN: $token"
            )
        }
}

// =====================================================
// FIREBASE NOTIFICATION PERMISSION
// =====================================================

@Composable
fun NotificationPermissionRequester() {

    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->

        if (isGranted) {
            Log.d(
                "ThriftlyFCM",
                "Notification permission granted"
            )
        } else {
            Log.d(
                "ThriftlyFCM",
                "Notification permission denied"
            )
        }
    }

    LaunchedEffect(Unit) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            val permission = Manifest.permission.POST_NOTIFICATIONS

            if (
                ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                permissionLauncher.launch(permission)
            }
        }
    }
}

// =====================================================
// MAIN APPLICATION
// =====================================================

@Composable
fun ThriftlyApp(context: Context) {

    val preferences = SecurePreferences.get(context)

    var loggedIn by remember {
        mutableStateOf(
            preferences.getBoolean("logged_in", false)
        )
    }

    if (loggedIn) {

        MainScreen(
            context = context,
            onLogout = {
                preferences.edit().clear().apply()
                loggedIn = false
            }
        )

    } else {

        LoginScreen(
            onLogin = { userId ->

                preferences.edit()
                    .putBoolean("logged_in", true)
                    .putInt("user_id", userId)
                    .apply()

                loggedIn = true
            }
        )
    }
}

// =====================================================
// LOGIN AND REGISTRATION WITH GOOGLE SIGN-IN
// =====================================================

@Composable
fun LoginScreen(onLogin: (Int) -> Unit) {

    val context = LocalContext.current

    val repository = remember {
        ThriftlyRepository()
    }

    val scope = rememberCoroutineScope()

    var registerMode by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Thriftly SA",
            style = MaterialTheme.typography.headlineLarge
        )

        Text("Find it. List it. Reuse it.")

        Spacer(Modifier.height(24.dp))

        if (registerMode) {

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {

                if (!Validation.validEmail(email)) {
                    message = "Please enter a valid email."
                    return@Button
                }

                if (!Validation.validPassword(password)) {
                    message = "Password must contain at least 6 characters."
                    return@Button
                }

                if (registerMode && name.isBlank()) {
                    message = "Please enter your name."
                    return@Button
                }

                scope.launch {

                    loading = true
                    message = ""

                    try {

                        if (registerMode) {

                            val result = repository.register(
                                name,
                                email,
                                password
                            )

                            message = result.message

                            if (result.success) {
                                registerMode = false
                                password = ""
                            }

                        } else {

                            val result = repository.login(
                                email,
                                password,
                                context
                            )

                            message = result.message

                            if (result.success && result.userId != null) {

                                Log.d(
                                    "ThriftlyLogin",
                                    "Login successful"
                                )

                                onLogin(result.userId)
                            }
                        }

                    } catch (e: Exception) {

                        Log.e("ThriftlyLogin", "Login error", e)
                        message = e.message ?: "Something went wrong."

                    } finally {
                        loading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading
        ) {

            Text(
                when {
                    loading -> "Please wait..."
                    registerMode -> "Create Account"
                    else -> "Sign In"
                }
            )
        }

        if (!registerMode) {

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = {

                    val activity = context as? Activity

                    if (activity == null) {
                        message = "Unable to open Google sign-in."
                        return@OutlinedButton
                    }

                    scope.launch {

                        loading = true
                        message = "Opening Google sign-in..."

                        try {

                            val credentialManager =
                                CredentialManager.create(context)

                            val googleOption =
                                GetSignInWithGoogleOption.Builder(
                                    BuildConfig.GOOGLE_WEB_CLIENT_ID
                                ).build()

                            val request =
                                GetCredentialRequest.Builder()
                                    .addCredentialOption(googleOption)
                                    .build()

                            val result =
                                credentialManager.getCredential(
                                    context = activity,
                                    request = request
                                )

                            val credential = result.credential

                            if (
                                credential is CustomCredential &&
                                credential.type ==
                                GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                            ) {

                                val googleCredential =
                                    GoogleIdTokenCredential.createFrom(
                                        credential.data
                                    )

                                val loginResult =
                                    repository.signInWithGoogle(
                                        googleCredential.idToken,
                                        context
                                    )

                                message = loginResult.message

                                if (
                                    loginResult.success &&
                                    loginResult.userId != null
                                ) {
                                    onLogin(loginResult.userId)
                                }

                            } else {

                                message =
                                    "Google did not return a valid sign-in credential."
                            }

                        } catch (e: Exception) {

                            Log.e(
                                "ThriftlyGoogle",
                                "Google sign-in error",
                                e
                            )

                            message = when {
                                e.message?.contains(
                                    "cancel",
                                    ignoreCase = true
                                ) == true -> {
                                    "Google sign-in was cancelled."
                                }

                                else -> {
                                    "Google sign-in failed. Please try again."
                                }
                            }

                        } finally {
                            loading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !loading
            ) {
                Text("Continue with Google")
            }
        }

        Spacer(Modifier.height(8.dp))

        OutlinedButton(
            onClick = {
                registerMode = !registerMode
                message = ""
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading
        ) {

            Text(
                if (registerMode) {
                    "Already have an account?"
                } else {
                    "Create a new account"
                }
            )
        }

        Spacer(Modifier.height(12.dp))

        Text(message)
    }
}

// =====================================================
// MAIN NAVIGATION
// =====================================================

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    context: Context,
    onLogout: () -> Unit
) {

    NotificationPermissionRequester()

    var selectedTab by remember {
        mutableIntStateOf(0)
    }

    Scaffold(

        topBar = {
            TopAppBar(
                title = {
                    Text("Thriftly SA")
                }
            )
        },

        bottomBar = {

            NavigationBar {

                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            Icons.Default.Home,
                            contentDescription = "Home"
                        )
                    },
                    label = { Text("Home") }
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    },
                    label = { Text("Search") }
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Sell"
                        )
                    },
                    label = { Text("Sell") }
                )

                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Profile"
                        )
                    },
                    label = { Text("Profile") }
                )
            }
        }

    ) { padding ->

        when (selectedTab) {

            0 -> HomeScreen(
                Modifier.padding(padding),
                context
            )

            1 -> SearchScreen(
                Modifier.padding(padding),
                context
            )

            2 -> SellScreen(
                Modifier.padding(padding),
                context
            )

            3 -> ProfileScreen(
                Modifier.padding(padding),
                context,
                onLogout
            )
        }
    }
}

// =====================================================
// HOME SCREEN - OFFLINE CACHE AND SYNC
// =====================================================

@Composable
fun HomeScreen(
    modifier: Modifier,
    context: Context
) {

    val repository = remember {
        OfflineListingRepository(context)
    }

    val scope = rememberCoroutineScope()

    var listings by remember {
        mutableStateOf(emptyList<ListingEntity>())
    }

    var message by remember {
        mutableStateOf("Loading listings...")
    }

    var loading by remember {
        mutableStateOf(true)
    }

    var refreshKey by remember {
        mutableIntStateOf(0)
    }

    DisposableEffect(context) {

        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE)
                    as ConnectivityManager

        val networkCallback = object :
            ConnectivityManager.NetworkCallback() {

            override fun onAvailable(network: Network) {

                scope.launch {

                    val synced =
                        repository.syncPendingFavourites()

                    Log.d(
                        "ThriftlySync",
                        "$synced favourite changes synchronised"
                    )
                }
            }
        }

        connectivityManager.registerDefaultNetworkCallback(
            networkCallback
        )

        onDispose {
            connectivityManager.unregisterNetworkCallback(
                networkCallback
            )
        }
    }

    LaunchedEffect(refreshKey) {

        loading = true

        try {

            listings = repository.getListings()

            val synced =
                repository.syncPendingFavourites()

            Log.d(
                "ThriftlySync",
                "Refresh synchronised $synced changes"
            )

            message = if (listings.isEmpty()) {
                "No clothing listings available yet."
            } else {
                "${listings.size} listings available"
            }

        } catch (e: Exception) {

            message = "Unable to load listings."

            Log.e(
                "ThriftlyHome",
                "Home loading error",
                e
            )

        } finally {
            loading = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Welcome to Thriftly SA",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.height(8.dp))

        Text("Discover affordable pre-owned clothing.")

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text(message)

            OutlinedButton(
                onClick = {
                    refreshKey++
                },
                enabled = !loading
            ) {
                Text("Refresh")
            }
        }

        Spacer(Modifier.height(12.dp))

        if (loading) {

            CircularProgressIndicator()

        } else if (listings.isEmpty()) {

            Text(
                "No listings available. Connect to the internet and refresh."
            )

        } else {

            LazyColumn {

                items(
                    items = listings,
                    key = { it.id }
                ) { listing ->

                    OfflineListingCard(
                        listing = listing,
                        context = context
                    )
                }
            }
        }
    }
}

// =====================================================
// ORIGINAL SUPABASE LISTING CARD
// =====================================================

@Composable
fun SupabaseListingCard(
    listing: SupabaseListing,
    context: Context
) {

    val repository = remember {
        SupabaseListingRepository(context)
    }

    val scope = rememberCoroutineScope()

    var message by remember {
        mutableStateOf("")
    }

    var saving by remember {
        mutableStateOf(false)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = listing.title,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "R%.2f".format(listing.price),
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(Modifier.height(4.dp))

            Text("Category: ${listing.category}")
            Text("Condition: ${listing.item_condition}")
            Text("Location: ${listing.location}")

            if (listing.description.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(listing.description)
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {

                    scope.launch {

                        saving = true
                        message = ""

                        try {

                            repository.addFavourite(listing.id)
                            message = "Added to favourites!"

                        } catch (e: Exception) {

                            message = e.message
                                ?: "Could not save favourite."

                        } finally {
                            saving = false
                        }
                    }
                },
                enabled = !saving
            ) {

                Icon(
                    Icons.Default.Favorite,
                    contentDescription = "Favourite"
                )

                Text("Add to Favourites")
            }

            if (message.isNotBlank()) {

                Spacer(Modifier.height(4.dp))

                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// =====================================================
// OFFLINE LISTING CARD - LOCAL FAVOURITES
// =====================================================

@Composable
fun OfflineListingCard(
    listing: ListingEntity,
    context: Context
) {

    val repository = remember {
        OfflineListingRepository(context)
    }

    val scope = rememberCoroutineScope()

    var message by remember {
        mutableStateOf("")
    }

    var isFavourite by remember {
        mutableStateOf(false)
    }

    var saving by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(listing.id) {
        isFavourite = repository.isFavourite(listing.id)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = listing.title,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "R%.2f".format(listing.price),
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(Modifier.height(4.dp))

            Text("Category: ${listing.category}")
            Text("Condition: ${listing.item_condition}")
            Text("Location: ${listing.location}")

            if (listing.description.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(listing.description)
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {

                    scope.launch {

                        saving = true
                        message = ""

                        try {

                            isFavourite =
                                repository.toggleFavourite(listing.id)

                            message = if (isFavourite) {
                                "Saved to favourites on this device."
                            } else {
                                "Removed from favourites on this device."
                            }

                        } catch (e: Exception) {

                            message = e.message
                                ?: "Could not update favourite."

                        } finally {
                            saving = false
                        }
                    }
                },
                enabled = !saving
            ) {

                Icon(
                    Icons.Default.Favorite,
                    contentDescription = "Favourite"
                )

                Text(
                    when {
                        saving -> "Saving..."
                        isFavourite -> "Remove Favourite"
                        else -> "Add Favourite"
                    }
                )
            }

            if (message.isNotBlank()) {

                Spacer(Modifier.height(4.dp))

                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// =====================================================
// SEARCH SCREEN - ROOM OFFLINE CACHING
// =====================================================

@Composable
fun SearchScreen(
    modifier: Modifier,
    context: Context
) {

    val repository = remember {
        OfflineListingRepository(context)
    }

    var listings by remember {
        mutableStateOf(emptyList<ListingEntity>())
    }

    var searchText by remember {
        mutableStateOf("")
    }

    var category by remember {
        mutableStateOf("All")
    }

    var message by remember {
        mutableStateOf("Loading products...")
    }

    var loading by remember {
        mutableStateOf(true)
    }

    LaunchedEffect(Unit) {

        try {

            listings = repository.getListings()

            message = "${listings.size} products loaded."

        } catch (e: Exception) {

            message = "Unable to load products."

        } finally {
            loading = false
        }
    }

    val filteredListings = listings.filter { listing ->

        val matchesSearch =
            listing.title.contains(searchText, ignoreCase = true) ||
                    listing.description.contains(searchText, ignoreCase = true) ||
                    listing.location.contains(searchText, ignoreCase = true)

        val matchesCategory =
            category == "All" ||
                    listing.category.equals(category, ignoreCase = true)

        matchesSearch && matchesCategory
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Search & Filter",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            label = { Text("Search products") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))

        Text("Filter by category")

        Spacer(Modifier.height(6.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            listOf(
                "All",
                "Clothing",
                "Shoes",
                "Accessories"
            ).forEach { selectedCategory ->

                OutlinedButton(
                    onClick = {
                        category = selectedCategory
                    }
                ) {
                    Text(selectedCategory)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Text(message)

        Spacer(Modifier.height(8.dp))

        if (loading) {

            CircularProgressIndicator()

        } else if (filteredListings.isEmpty()) {

            Text("No matching products. Try another search.")

        } else {

            LazyColumn {

                items(
                    items = filteredListings,
                    key = { it.id }
                ) { listing ->

                    OfflineListingCard(
                        listing = listing,
                        context = context
                    )
                }
            }
        }
    }
}

// =====================================================
// SELL SCREEN - SUPABASE PUBLISHING
// =====================================================

@Composable
fun SellScreen(
    modifier: Modifier,
    context: Context
) {

    val repository = remember {
        SupabaseListingRepository(context)
    }

    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Clothing") }
    var condition by remember { mutableStateOf("Good") }
    var price by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    var message by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var success by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        Text(
            text = "Sell Your Clothing",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.height(8.dp))

        Text("List your pre-owned clothing on Thriftly SA.")

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Clothing title") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !loading
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            enabled = !loading
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = category,
            onValueChange = { category = it },
            label = { Text("Category") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = condition,
            onValueChange = { condition = it },
            label = { Text("Condition") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = price,
            onValueChange = { price = it },
            label = { Text("Price (R)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !loading
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Your location") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !loading
        )

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {

                if (loading) return@Button

                val priceValue = price.trim().toDoubleOrNull()

                if (
                    title.isBlank() ||
                    description.isBlank() ||
                    category.isBlank() ||
                    condition.isBlank() ||
                    location.isBlank() ||
                    priceValue == null ||
                    priceValue <= 0
                ) {

                    success = false
                    message = "Please complete all fields with a valid price."
                    return@Button
                }

                scope.launch {

                    loading = true
                    message = "Connecting to Thriftly SA..."
                    success = false

                    try {

                        Log.d(
                            "ThriftlySell",
                            "Starting listing publication"
                        )

                        val newListing = repository.createListing(
                            title = title.trim(),
                            description = description.trim(),
                            category = category.trim(),
                            itemCondition = condition.trim(),
                            price = priceValue,
                            location = location.trim()
                        )

                        Log.d(
                            "ThriftlySell",
                            "Listing published: ${newListing.id}"
                        )

                        success = true
                        message = "Success! ${newListing.title} has been published."

                        title = ""
                        description = ""
                        category = "Clothing"
                        condition = "Good"
                        price = ""
                        location = ""

                    } catch (e: Exception) {

                        Log.e(
                            "ThriftlySell",
                            "Listing publication failed",
                            e
                        )

                        success = false
                        message = e.message
                            ?: "Unable to publish listing. Please try again."

                    } finally {
                        loading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading
        ) {

            if (loading) {

                CircularProgressIndicator(
                    modifier = Modifier.height(20.dp),
                    strokeWidth = 2.dp
                )

            } else {

                Text("Publish Listing")
            }
        }

        Spacer(Modifier.height(12.dp))

        if (message.isNotBlank()) {

            Text(
                text = message,
                color = if (success) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                },
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

// =====================================================
// PROFILE AND SETTINGS
// =====================================================

@Composable
fun ProfileScreen(
    modifier: Modifier,
    context: Context,
    onLogout: () -> Unit
) {

    val preferences = SecurePreferences.get(context)

    var language by remember {
        mutableStateOf(
            preferences.getString("language", "English") ?: "English"
        )
    }

    var notifications by remember {
        mutableStateOf(
            preferences.getBoolean("notifications", true)
        )
    }

    var darkMode by remember {
        mutableStateOf(
            preferences.getBoolean("dark_mode", false)
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            "Profile & Settings",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.height(20.dp))

        Text("Language")

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            listOf(
                "English",
                "isiZulu",
                "isiXhosa"
            ).forEach { selectedLanguage ->

                OutlinedButton(
                    onClick = {

                        language = selectedLanguage

                        preferences.edit()
                            .putString("language", selectedLanguage)
                            .apply()
                    }
                ) {
                    Text(selectedLanguage)
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text("Notifications")

            Switch(
                checked = notifications,
                onCheckedChange = {

                    notifications = it

                    preferences.edit()
                        .putBoolean("notifications", it)
                        .apply()
                }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text("Dark Mode Preference")

            Switch(
                checked = darkMode,
                onCheckedChange = {

                    darkMode = it

                    preferences.edit()
                        .putBoolean("dark_mode", it)
                        .apply()
                }
            )
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sign Out")
        }
    }
}
