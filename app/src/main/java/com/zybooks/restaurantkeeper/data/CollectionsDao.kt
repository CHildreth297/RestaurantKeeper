package com.zybooks.restaurantkeeper.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserCollectionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertCollection(collection: UserCollection)

    @Delete
    fun deleteCollection(collection: UserCollection)

    // Query operations
    @Query("SELECT * FROM user_collections ORDER BY name ASC")
    fun getAllCollections(): Flow<List<UserCollection>>

    @Query("SELECT * FROM user_collections WHERE name = :collectionName")
    suspend fun getCollectionById(collectionName: String): UserCollection?

}