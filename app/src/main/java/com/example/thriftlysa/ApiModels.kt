package com.example.thriftlysa

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class CreateListingRequest(
    val sellerId: Int,
    val title: String,
    val description: String,
    val category: String,
    val condition: String,
    val price: Double,
    val location: String
)

data class FavouriteRequest(
    val userId: Int,
    val listingId: Int
)

data class RegisterResponse(
    val success: Boolean,
    val message: String
)

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val userId: Int?,
    val name: String?,
    val email: String?
)

data class Listing(
    val id: Int,
    val seller_id: Int,
    val title: String,
    val description: String,
    val category: String,
    val item_condition: String,
    val price: Double,
    val location: String,
    val created_at: String?,
    val seller_name: String?
)

data class ListingsResponse(
    val success: Boolean,
    val message: String?,
    val listings: List<Listing>?
)

data class CreateListingResponse(
    val success: Boolean,
    val message: String,
    val listingId: Int?
)

data class FavouriteResponse(
    val success: Boolean,
    val favourite: Boolean,
    val message: String
)

// Supabase Auth REST payloads. Passwords are sent over HTTPS directly to Supabase Auth;
// the app never stores the raw password.
data class SupabaseSignUpRequest(
    val email: String,
    val password: String,
    val data: Map<String, String>
)

data class SupabaseSignInRequest(
    val email: String,
    val password: String
)

data class SupabaseGoogleIdTokenRequest(
    val provider: String = "google",
    val id_token: String
)

data class SupabaseAuthResponse(
    val id: String? = null,
    val email: String? = null,
    val user_metadata: SupabaseUserMetadata? = null,
    val user: SupabaseAuthUser? = null,
    val access_token: String? = null,
    val refresh_token: String? = null,
    val token_type: String? = null,
    val msg: String? = null,
    val message: String? = null
)

data class SupabaseAuthUser(
    val id: String? = null,
    val email: String? = null,
    val user_metadata: SupabaseUserMetadata? = null
)

data class SupabaseUserMetadata(
    val full_name: String? = null,
    val name: String? = null
)
