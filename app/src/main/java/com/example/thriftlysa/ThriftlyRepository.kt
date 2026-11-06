
package com.example.thriftlysa

import android.util.Log

class ThriftlyRepository {

    private val api = ApiClient.api

    // Supabase handles account creation, password hashing,
    // verification and sign-in.
    private val supabaseAuth: SupabaseAuthApi by lazy {
        retrofit2.Retrofit.Builder()
            .baseUrl(BuildConfig.SUPABASE_URL.trimEnd('/') + "/")
            .addConverterFactory(
                retrofit2.converter.gson.GsonConverterFactory.create()
            )
            .build()
            .create(SupabaseAuthApi::class.java)
    }

    private val supabaseKey: String
        get() = BuildConfig.SUPABASE_PUBLISHABLE_KEY

    suspend fun register(
        name: String,
        email: String,
        password: String
    ): RegisterResponse {
        return try {
            Log.d("ThriftlyAuth", "Creating account with Supabase Auth")

            val response = supabaseAuth.signUp(
                supabaseKey,
                SupabaseSignUpRequest(
                    email = email.trim(),
                    password = password,
                    data = mapOf(
                        "full_name" to name.trim(),
                        "name" to name.trim()
                    )
                )
            )

            if (response.isSuccessful) {
                val body = response.body()

                if (body?.access_token != null) {
                    RegisterResponse(
                        true,
                        "Account created successfully. You can now sign in."
                    )
                } else {
                    RegisterResponse(
                        true,
                        "Registration successful. Please check your email to confirm your account, then sign in."
                    )
                }
            } else {
                val details = response.errorBody()?.string().orEmpty()

                Log.w(
                    "ThriftlyAuth",
                    "Supabase sign-up rejected: HTTP ${response.code()} $details"
                )

                RegisterResponse(
                    false,
                    supabaseErrorMessage(details, response.code())
                )
            }
        } catch (error: Exception) {
            Log.e("ThriftlyAuth", "Supabase registration failed", error)

            RegisterResponse(
                false,
                "Could not reach Supabase. Check your internet connection and try again."
            )
        }
    }

    suspend fun login(
        email: String,
        password: String,
        context: android.content.Context
    ): LoginResponse {
        return try {
            Log.d("ThriftlyAuth", "Signing in with Supabase Auth")

            val response = supabaseAuth.signIn(
                supabaseKey,
                SupabaseSignInRequest(
                    email = email.trim(),
                    password = password
                )
            )

            if (response.isSuccessful) {
                val body = response.body()
                val user = body?.user
                val uuid = user?.id ?: body?.id
                val accessToken = body?.access_token

                if (accessToken.isNullOrBlank() || uuid.isNullOrBlank()) {
                    LoginResponse(
                        false,
                        "Supabase returned an incomplete sign-in response. Please try again.",
                        null,
                        null,
                        null
                    )
                } else {
                    val localId = uuid.hashCode() and Int.MAX_VALUE

                    SecurePreferences.get(context).edit()
                        .putString("supabase_user_id", uuid)
                        .putString("supabase_access_token", accessToken)
                        .putString("supabase_refresh_token", body?.refresh_token)
                        .putInt("user_id", localId)
                        .apply()

                    val displayName =
                        user?.user_metadata?.full_name
                            ?: user?.user_metadata?.name
                            ?: email.substringBefore("@")

                    LoginResponse(
                        true,
                        "Login successful.",
                        localId,
                        displayName,
                        user?.email ?: email
                    )
                }
            } else {
                val details = response.errorBody()?.string().orEmpty()

                Log.w(
                    "ThriftlyAuth",
                    "Supabase sign-in rejected: HTTP ${response.code()} $details"
                )

                LoginResponse(
                    false,
                    supabaseErrorMessage(details, response.code()),
                    null,
                    null,
                    null
                )
            }
        } catch (error: Exception) {
            Log.e("ThriftlyAuth", "Supabase login failed", error)

            LoginResponse(
                false,
                "Could not reach Supabase. Check your internet connection and try again.",
                null,
                null,
                null
            )
        }
    }

    // Google Sign-In through Supabase Auth.
    // The Google ID token is exchanged for a Supabase session.
    suspend fun signInWithGoogle(
        idToken: String,
        context: android.content.Context
    ): LoginResponse {
        return try {
            Log.d(
                "ThriftlyAuth",
                "Signing in with Google through Supabase"
            )

            val response = supabaseAuth.signInWithGoogle(
                supabaseKey,
                SupabaseGoogleIdTokenRequest(
                    id_token = idToken
                )
            )

            if (response.isSuccessful) {
                val body = response.body()
                val user = body?.user

                val uuid = user?.id ?: body?.id
                val accessToken = body?.access_token

                if (accessToken.isNullOrBlank() || uuid.isNullOrBlank()) {
                    LoginResponse(
                        false,
                        "Supabase returned an incomplete Google sign-in response.",
                        null,
                        null,
                        null
                    )
                } else {
                    val localId = uuid.hashCode() and Int.MAX_VALUE

                    SecurePreferences.get(context).edit()
                        .putString("supabase_user_id", uuid)
                        .putString("supabase_access_token", accessToken)
                        .putString("supabase_refresh_token", body?.refresh_token)
                        .putInt("user_id", localId)
                        .apply()

                    val displayName =
                        user?.user_metadata?.full_name
                            ?: user?.user_metadata?.name
                            ?: user?.email?.substringBefore("@")
                            ?: "Thriftly User"

                    LoginResponse(
                        true,
                        "Google login successful.",
                        localId,
                        displayName,
                        user?.email ?: body?.email
                    )
                }
            } else {
                val details = response.errorBody()?.string().orEmpty()

                Log.w(
                    "ThriftlyAuth",
                    "Google sign-in rejected: HTTP ${response.code()} $details"
                )

                LoginResponse(
                    false,
                    supabaseErrorMessage(details, response.code()),
                    null,
                    null,
                    null
                )
            }
        } catch (error: Exception) {
            Log.e("ThriftlyAuth", "Google sign-in failed", error)

            LoginResponse(
                false,
                "Google sign-in failed. Check your internet connection and try again.",
                null,
                null,
                null
            )
        }
    }

    private fun supabaseErrorMessage(
        details: String,
        status: Int
    ): String {
        val lower = details.lowercase()

        return when {
            "invalid login credentials" in lower ->
                "Incorrect email or password. Please try again."

            "email not confirmed" in lower ->
                "Please confirm your email address before signing in."

            "user already registered" in lower ->
                "An account with this email already exists. Please sign in instead."

            "password should be" in lower ->
                "Your password does not meet the security requirements."

            "email rate limit" in lower ->
                "Too many attempts. Please wait a few minutes and try again."

            else ->
                "Supabase authentication error (HTTP $status). Check your Supabase Auth settings."
        }
    }

    suspend fun getListings(): ListingsResponse {
        return try {
            Log.d("ThriftlyAPI", "GET listings")

            val response = api.getListings()

            if (response.isSuccessful) {
                response.body()
                    ?: ListingsResponse(
                        false,
                        "Empty server response.",
                        emptyList()
                    )
            } else {
                ListingsResponse(
                    false,
                    "Server error.",
                    emptyList()
                )
            }
        } catch (e: Exception) {
            Log.e("ThriftlyAPI", "Listings error", e)

            ListingsResponse(
                false,
                "Could not load listings.",
                emptyList()
            )
        }
    }

    suspend fun createListing(
        request: CreateListingRequest,
        context: android.content.Context
    ): CreateListingResponse {
        return try {
            Log.d("ThriftlyAPI", "POST create listing")

            val response = api.createListing(request)

            if (response.isSuccessful) {
                response.body()
                    ?: CreateListingResponse(
                        false,
                        "Empty server response.",
                        null
                    )
            } else {
                CreateListingResponse(
                    false,
                    "Server error.",
                    null
                )
            }
        } catch (e: Exception) {
            Log.w(
                "ThriftlyAPI",
                "API unavailable; saving listing for later synchronisation",
                e
            )

            OfflineListingStore(context).use { store ->
                store.enqueue(request)
            }

            CreateListingResponse(
                true,
                "No connection. Your listing was saved on this device and will sync when you are online.",
                null
            )
        }
    }

    suspend fun syncPendingListings(
        context: android.content.Context
    ): Int {
        val store = OfflineListingStore(context)
        var synced = 0

        try {
            store.pending().forEach { (id, request) ->
                try {
                    val response = api.createListing(request)

                    if (
                        response.isSuccessful &&
                        response.body()?.success == true
                    ) {
                        store.remove(id)
                        synced++
                    }
                } catch (error: Exception) {
                    Log.w(
                        "ThriftlyAPI",
                        "Pending listing sync paused",
                        error
                    )
                    return synced
                }
            }
        } finally {
            store.close()
        }

        Log.i(
            "ThriftlyAPI",
            "Synchronised $synced offline listing(s)"
        )

        return synced
    }

    suspend fun toggleFavourite(
        userId: Int,
        listingId: Int
    ): FavouriteResponse {
        return try {
            Log.d("ThriftlyAPI", "Toggling favourite")

            val response = api.toggleFavourite(
                FavouriteRequest(
                    userId,
                    listingId
                )
            )

            if (response.isSuccessful) {
                response.body()
                    ?: FavouriteResponse(
                        false,
                        false,
                        "Empty server response."
                    )
            } else {
                FavouriteResponse(
                    false,
                    false,
                    "Server error."
                )
            }
        } catch (e: Exception) {
            Log.e("ThriftlyAPI", "Favourite error", e)

            FavouriteResponse(
                false,
                false,
                "Could not update favourite."
            )
        }
    }
}
