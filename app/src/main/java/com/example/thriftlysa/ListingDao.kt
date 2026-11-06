
package com.example.thriftlysa

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ListingDao {

    // CLOTHING LISTINGS

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListings(
        listings: List<ListingEntity>
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListing(
        listing: ListingEntity
    )

    @Query("SELECT * FROM listings ORDER BY created_at DESC")
    fun observeListings(): Flow<List<ListingEntity>>

    @Query("SELECT * FROM listings ORDER BY created_at DESC")
    suspend fun getAllListings(): List<ListingEntity>

    @Query("DELETE FROM listings")
    suspend fun deleteAllListings()


    // LOCAL FAVOURITES

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveLocalFavourite(
        favourite: LocalFavouriteEntity
    )

    @Query("""
        SELECT is_favourite
        FROM local_favourites
        WHERE user_id = :userId
        AND listing_id = :listingId
        LIMIT 1
    """)
    suspend fun isFavourite(
        userId: String,
        listingId: String
    ): Boolean?

    @Query("""
        SELECT * FROM local_favourites
        WHERE user_id = :userId
        AND is_favourite = 1
    """)
    fun observeLocalFavourites(
        userId: String
    ): Flow<List<LocalFavouriteEntity>>


    // PENDING SYNCHRONISATION

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun queueFavouriteChange(
        change: PendingFavouriteEntity
    )

    @Query("""
        SELECT * FROM pending_favourite_changes
        WHERE user_id = :userId
        ORDER BY updated_at ASC
    """)
    suspend fun getPendingFavouriteChanges(
        userId: String
    ): List<PendingFavouriteEntity>

    @Query("""
        DELETE FROM pending_favourite_changes
        WHERE user_id = :userId
        AND listing_id = :listingId
    """)
    suspend fun removePendingFavouriteChange(
        userId: String,
        listingId: String
    ): Int
}
