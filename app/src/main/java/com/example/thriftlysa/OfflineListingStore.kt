package com.example.thriftlysa

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.google.gson.Gson

/**
 * Stores listing submissions locally when the REST API cannot be reached.
 * Pending records are retried when the Home screen next loads online.
 */
class OfflineListingStore(context: Context) :
    SQLiteOpenHelper(context, "thriftly_offline.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE pending_listings (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                payload TEXT NOT NULL,
                created_at INTEGER NOT NULL
            )""".trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // No schema migration is required for version 1.
    }

    fun enqueue(request: CreateListingRequest) {
        val values = ContentValues().apply {
            put("payload", Gson().toJson(request))
            put("created_at", System.currentTimeMillis())
        }
        writableDatabase.insert("pending_listings", null, values)
    }

    fun pending(): List<Pair<Long, CreateListingRequest>> {
        val result = mutableListOf<Pair<Long, CreateListingRequest>>()
        readableDatabase.query(
            "pending_listings", arrayOf("id", "payload"),
            null, null, null, null, "id ASC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getLong(0)
                val request = runCatching {
                    Gson().fromJson(cursor.getString(1), CreateListingRequest::class.java)
                }.getOrNull()
                if (request != null) result.add(id to request)
            }
        }
        return result
    }

    fun remove(id: Long) {
        writableDatabase.delete("pending_listings", "id = ?", arrayOf(id.toString()))
    }

    fun count(): Int {
        readableDatabase.rawQuery("SELECT COUNT(*) FROM pending_listings", null).use {
            return if (it.moveToFirst()) it.getInt(0) else 0
        }
    }
}
