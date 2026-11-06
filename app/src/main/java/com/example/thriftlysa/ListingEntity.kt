package com.example.thriftlysa

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "listings")
data class ListingEntity(
    @PrimaryKey
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