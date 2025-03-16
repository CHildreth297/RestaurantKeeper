package com.zybooks.restaurantkeeper.data

import android.util.Log
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow


@Dao
interface UserEntryDao {
    @Upsert
    fun UpsertEntry(entry: UserEntry) {
        Log.d("DATABASE_DEBUG", "Inserted entry")}

    @Delete
    fun deleteEntry(entry: UserEntry)

    // Flow for observing database changes
    @Query("SELECT * FROM user_entries")
    fun getAllEntries(): Flow<List<UserEntry>>

    // Single item query - make it suspend and match the id type
    @Query("SELECT * FROM user_entries WHERE id = :entryId")
    fun getEntryById(entryId: Int): UserEntry?

    // List query - make it suspend or return Flow
    @Query("SELECT * FROM user_entries WHERE rating >= :minRating")
    fun getEntriesByMinRating(minRating: Int): List<UserEntry>
}