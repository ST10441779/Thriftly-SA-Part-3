
package com.example.thriftlysa

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        ListingEntity::class,
        LocalFavouriteEntity::class,
        PendingFavouriteEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class ThriftlyDatabase : RoomDatabase() {

    abstract fun listingDao(): ListingDao

    companion object {

        @Volatile
        private var INSTANCE: ThriftlyDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {

            override fun migrate(database: SupportSQLiteDatabase) {

                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS local_favourites (
                        user_id TEXT NOT NULL,
                        listing_id TEXT NOT NULL,
                        is_favourite INTEGER NOT NULL,
                        PRIMARY KEY(user_id, listing_id)
                    )
                """)

                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS pending_favourite_changes (
                        user_id TEXT NOT NULL,
                        listing_id TEXT NOT NULL,
                        is_favourite INTEGER NOT NULL,
                        updated_at INTEGER NOT NULL,
                        PRIMARY KEY(user_id, listing_id)
                    )
                """)
            }
        }

        fun getDatabase(context: Context): ThriftlyDatabase {

            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ThriftlyDatabase::class.java,
                    "thriftly_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
