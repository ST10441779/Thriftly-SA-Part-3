
package com.example.thriftlysa

import android.content.Context
import android.util.Log

class SupabaseListingRepository(
    private val context: Context
) {

    private val api = SupabaseDataClient.api

    private val preferences =
        SecurePreferences.get(context)

    private val apiKey =
        BuildConfig.SUPABASE_PUBLISHABLE_KEY

    private fun getAccessToken(): String {
        return preferences.getString(
            "supabase_access_token",
            ""
        ) ?: ""
    }

    private fun getUserId(): String {
        return preferences.getString(
            "supabase_user_id",
            ""
        ) ?: ""
    }

    // GET THE CURRENT SUPABASE USER UUID

    fun currentUserId(): String {
        return getUserId()
    }

    private fun authorization(): String {

        val token = getAccessToken()

        require(token.isNotBlank()) {
            "Please log in before using listings."
        }

        return "Bearer $token"
    }

    // GET ACTIVE LISTINGS

    suspend fun getListings(): List<SupabaseListing> {

        val response = api.getListings(
            apiKey = apiKey,
            authorization = authorization()
        )

        if (!response.isSuccessful) {

            val errorMessage =
                response.errorBody()?.string()
                    ?: "No error details returned"

            Log.e(
                "ThriftlySupabase",
                "Get listings failed. HTTP ${response.code()}: $errorMessage"
            )

            throw Exception(
                "Could not load listings. HTTP ${response.code()}: $errorMessage"
            )
        }

        val listings =
            response.body() ?: emptyList()

        Log.d(
            "ThriftlySupabase",
            "Successfully received ${listings.size} listings"
        )

        return listings
    }

    // CREATE A NEW CLOTHING LISTING

    suspend fun createListing(
        title: String,
        description: String,
        category: String,
        itemCondition: String,
        price: Double,
        location: String,
        imageUrl: String? = null
    ): SupabaseListing {

        val userId = getUserId()

        require(userId.isNotBlank()) {
            "Your account ID could not be found. Please log in again."
        }

        val request = SupabaseCreateListingRequest(
            seller_id = userId,
            title = title,
            description = description,
            category = category,
            item_condition = itemCondition,
            price = price,
            location = location,
            image_url = imageUrl
        )

        val response = api.createListing(
            apiKey = apiKey,
            authorization = authorization(),
            request = request
        )

        if (!response.isSuccessful) {

            val errorMessage =
                response.errorBody()?.string()
                    ?: "No error details returned"

            Log.e(
                "ThriftlySupabase",
                "Create listing failed. HTTP ${response.code()}: $errorMessage"
            )

            throw Exception(
                "Could not create listing. HTTP ${response.code()}: $errorMessage"
            )
        }

        return response.body()?.firstOrNull()
            ?: throw Exception(
                "The listing was created but no listing data was returned."
            )
    }

    // ADD FAVOURITE

    suspend fun addFavourite(listingId: String) {

        val userId = getUserId()

        require(userId.isNotBlank()) {
            "Your account ID could not be found. Please log in again."
        }

        val response = api.addFavourite(
            apiKey = apiKey,
            authorization = authorization(),
            request = SupabaseFavouriteRequest(
                user_id = userId,
                listing_id = listingId
            )
        )

        if (!response.isSuccessful) {

            val errorMessage =
                response.errorBody()?.string()
                    ?: "No error details returned"

            Log.e(
                "ThriftlySupabase",
                "Add favourite failed. HTTP ${response.code()}: $errorMessage"
            )

            throw Exception(
                "Could not add favourite. HTTP ${response.code()}: $errorMessage"
            )
        }
    }

    // REMOVE FAVOURITE

    suspend fun removeFavourite(listingId: String) {

        val userId = getUserId()

        require(userId.isNotBlank()) {
            "Your account ID could not be found. Please log in again."
        }

        val response = api.removeFavourite(
            apiKey = apiKey,
            authorization = authorization(),
            userId = "eq.$userId",
            listingId = "eq.$listingId"
        )

        if (!response.isSuccessful) {

            val errorMessage =
                response.errorBody()?.string()
                    ?: "No error details returned"

            Log.e(
                "ThriftlySupabase",
                "Remove favourite failed. HTTP ${response.code()}: $errorMessage"
            )

            throw Exception(
                "Could not remove favourite. HTTP ${response.code()}: $errorMessage"
            )
        }
    }
}
