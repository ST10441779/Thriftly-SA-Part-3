
package com.example.thriftlysa

import androidx.room.Entity

@Entity(
    tableName = "local_favourites",
    primaryKeys = ["user_id", "listing_id"]
)
data class LocalFavouriteEntity(
    val user_id: String,
    val listing_id: String,
    val is_favourite: Boolean
)
