
package com.example.thriftlysa

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface SupabaseListingsApi {

    // GET ACTIVE LISTINGS

    @GET("rest/v1/listings")
    suspend fun getListings(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorization: String,
        @Query("select") select: String = "*",
        @Query("status") status: String = "eq.active",
        @Query("order") order: String = "created_at.desc"
    ): Response<List<SupabaseListing>>


    // CREATE LISTING

    @Headers("Content-Type: application/json")
    @POST("rest/v1/listings")
    suspend fun createListing(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorization: String,
        @Header("Prefer") prefer: String = "return=representation",
        @Query("select") select: String = "*",
        @Body request: SupabaseCreateListingRequest
    ): Response<List<SupabaseListing>>


    // ADD FAVOURITE
    // Uses upsert to prevent duplicate favourites.

    @Headers("Content-Type: application/json")
    @POST("rest/v1/favourites")
    suspend fun addFavourite(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorization: String,
        @Header("Prefer") prefer: String =
            "resolution=merge-duplicates,return=minimal",
        @Query("on_conflict") onConflict: String =
            "user_id,listing_id",
        @Body request: SupabaseFavouriteRequest
    ): Response<Unit>


    // REMOVE FAVOURITE

    @DELETE("rest/v1/favourites")
    suspend fun removeFavourite(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorization: String,
        @Query("user_id") userId: String,
        @Query("listing_id") listingId: String
    ): Response<Unit>
}


// FAVOURITE REQUEST MODEL

data class SupabaseFavouriteRequest(
    val user_id: String,
    val listing_id: String
)
