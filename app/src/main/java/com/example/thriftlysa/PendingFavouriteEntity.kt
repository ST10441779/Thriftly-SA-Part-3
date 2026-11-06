
package com.example.thriftlysa

import androidx.room.Entity

@Entity(
    tableName = "pending_favourite_changes",
    primaryKeys = ["user_id", "listing_id"]
)
data class PendingFavouriteEntity(
    val user_id: String,
    val listing_id: String,
    val is_favourite: Boolean,
    val updated_at: Long = System.currentTimeMillis()
)
