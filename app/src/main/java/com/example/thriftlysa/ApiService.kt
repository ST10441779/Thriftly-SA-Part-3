package com.example.thriftlysa

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

import retrofit2.http.Headers
import retrofit2.http.Header

interface ApiService {

    @POST("register.php")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<RegisterResponse>

    @POST("login.php")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @GET("listings.php")
    suspend fun getListings():
            Response<ListingsResponse>

    @POST("create_listing.php")
    suspend fun createListing(
        @Body request: CreateListingRequest
    ): Response<CreateListingResponse>

    @POST("favourite.php")
    suspend fun toggleFavourite(
        @Body request: FavouriteRequest
    ): Response<FavouriteResponse>
}

/** Supabase's hosted authentication endpoints. */
interface SupabaseAuthApi {
    @retrofit2.http.Headers("Content-Type: application/json")
    @POST("auth/v1/signup")
    suspend fun signUp(
        @retrofit2.http.Header("apikey") publishableKey: String,
        @Body request: SupabaseSignUpRequest
    ): Response<SupabaseAuthResponse>

    @retrofit2.http.Headers("Content-Type: application/json")
    @POST("auth/v1/token?grant_type=password")
    suspend fun signIn(
        @retrofit2.http.Header("apikey") publishableKey: String,
        @Body request: SupabaseSignInRequest
    ): Response<SupabaseAuthResponse>

    @Headers("Content-Type: application/json")
    @POST("auth/v1/token?grant_type=id_token")
    suspend fun signInWithGoogle(
        @Header("apikey") publishableKey: String,
        @Body request: SupabaseGoogleIdTokenRequest
    ): Response<SupabaseAuthResponse>
}
