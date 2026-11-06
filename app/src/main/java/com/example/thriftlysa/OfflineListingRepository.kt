
package com.example.thriftlysa

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.Flow

class OfflineListingRepository(context: Context) {

    private val database =
        ThriftlyDatabase.getDatabase(context)

    private val listingDao =
        database.listingDao()

    private val supabaseRepository =
        SupabaseListingRepository(context)

    fun observeCachedListings(): Flow<List<ListingEntity>> {
        return listingDao.observeListings()
    }

    suspend fun getListings(): List<ListingEntity> {

        return try {

            val onlineListings =
                supabaseRepository.getListings()

            val cachedListings = onlineListings.map { listing ->

                ListingEntity(
                    id = listing.id,
                    seller_id = listing.seller_id,
                    title = listing.title,
                    description = listing.description,
                    category = listing.category,
                    item_condition = listing.item_condition,
                    price = listing.price,
                    location = listing.location,
                    image_url = listing.image_url,
                    status = listing.status,
                    created_at = listing.created_at
                )
            }

            listingDao.insertListings(cachedListings)

            cachedListings

        } catch (e: Exception) {

            Log.e(
                "ThriftlyOffline",
                "Could not retrieve online listings",
                e
            )

            listingDao.getAllListings()
        }
    }

    suspend fun saveListing(listing: SupabaseListing) {

        val entity = ListingEntity(
            id = listing.id,
            seller_id = listing.seller_id,
            title = listing.title,
            description = listing.description,
            category = listing.category,
            item_condition = listing.item_condition,
            price = listing.price,
            location = listing.location,
            image_url = listing.image_url,
            status = listing.status,
            created_at = listing.created_at
        )

        listingDao.insertListing(entity)
    }

    // CHECK FAVOURITE STATUS

    suspend fun isFavourite(listingId: String): Boolean {

        val userId =
            supabaseRepository.currentUserId()

        if (userId.isBlank()) return false

        return listingDao.isFavourite(
            userId,
            listingId
        ) ?: false
    }

    // TOGGLE FAVOURITE OFFLINE OR ONLINE

    suspend fun toggleFavourite(
        listingId: String
    ): Boolean {

        val userId =
            supabaseRepository.currentUserId()

        require(userId.isNotBlank()) {
            "Your account ID could not be found. Please log in again."
        }

        val currentlyFavourite =
            isFavourite(listingId)

        val newFavouriteStatus =
            !currentlyFavourite

        listingDao.saveLocalFavourite(
            LocalFavouriteEntity(
                user_id = userId,
                listing_id = listingId,
                is_favourite = newFavouriteStatus
            )
        )

        listingDao.queueFavouriteChange(
            PendingFavouriteEntity(
                user_id = userId,
                listing_id = listingId,
                is_favourite = newFavouriteStatus
            )
        )

        return newFavouriteStatus
    }

    // SYNCHRONISE PENDING CHANGES

    suspend fun syncPendingFavourites(): Int {

        val userId =
            supabaseRepository.currentUserId()

        if (userId.isBlank()) return 0

        val pendingChanges =
            listingDao.getPendingFavouriteChanges(userId)

        var syncedCount = 0

        for (change in pendingChanges) {

            try {

                if (change.is_favourite) {

                    supabaseRepository.addFavourite(
                        change.listing_id
                    )

                } else {

                    supabaseRepository.removeFavourite(
                        change.listing_id
                    )
                }

                listingDao.removePendingFavouriteChange(
                    change.user_id,
                    change.listing_id
                )

                syncedCount++

            } catch (e: Exception) {

                Log.e(
                    "ThriftlySync",
                    "Failed to sync favourite ${change.listing_id}",
                    e
                )
            }
        }

        return syncedCount
    }
}
