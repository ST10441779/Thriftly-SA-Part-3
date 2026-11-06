
package com.example.thriftlysa

data class SupabaseListing(
    val id: String,
    val seller_id: String,
    val title: String,
    val description: String,
    val category: String,
    val item_condition: String,
    val price: Double,
    val location: String,
    val image_url: String? = null,
    val status: String = "active",
    val created_at: String? = null
)

data class SupabaseCreateListingRequest(
    val seller_id: String,
    val title: String,
    val description: String,
    val category: String,
    val item_condition: String,
    val price: Double,
    val location: String,
    val image_url: String? = null
)
