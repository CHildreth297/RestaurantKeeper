package com.zybooks.restaurantkeeper.data

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class Converters {
    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? {
        return value?.format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? {
        return if (value == null) null else LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE)
    }

    @TypeConverter
    fun fromString(value: String): List<String> {
        return value.split(",").filter { it.isNotEmpty() }
    }

    @TypeConverter
    fun toString(list: List<String>): String {
        return list.joinToString(",")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun fromUserEntryList(value: List<UserEntry>?): String? {
        if (value == null) return null
        val jsonArray = JSONArray()
        for (entry in value) {
            val jsonObject = JSONObject()
            jsonObject.put("id", entry.id)
            jsonObject.put("title", entry.title)
            jsonObject.put("location", entry.location)
            jsonObject.put("date", fromLocalDate(entry.date))
            jsonObject.put("rating", entry.rating)
            jsonObject.put("comments", entry.comments)

            // Convert photos list to JSON array
            val photosArray = JSONArray()
            for (photo in entry.photos) {
                photosArray.put(photo)
            }
            jsonObject.put("photos", photosArray)

            jsonArray.put(jsonObject)
        }
        return jsonArray.toString()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverter
    fun toUserEntryList(value: String?): List<UserEntry>? {
        if (value == null) return null

        val jsonArray = JSONArray(value)
        val entries = mutableListOf<UserEntry>()

        // Check if the array is empty
        if (jsonArray.length() == 0) {
            return emptyList()
        }

        // Check if the first item is an array or object
        for (i in 0 until jsonArray.length()) {
            try {
                val jsonObject = jsonArray.getJSONObject(i)

                // Now safely parse the object
                val photosArray = jsonObject.optJSONArray("photos") ?: JSONArray()
                val photos = mutableListOf<String>()
                for (j in 0 until photosArray.length()) {
                    photos.add(photosArray.getString(j))
                }

                val entry = UserEntry(
                    id = jsonObject.optInt("id", 0),
                    title = jsonObject.optString("title", ""),
                    location = jsonObject.optString("location", ""),
                    date = try {
                        LocalDate.parse(jsonObject.optString("date"))
                    } catch (e: Exception) {
                        LocalDate.now()
                    },
                    rating = jsonObject.optInt("rating", 0),
                    comments = jsonObject.optString("comments", ""),
                    photos = photos
                )
                entries.add(entry)
            } catch (e: Exception) {
                // Skip invalid entries
                continue
            }
        }

        return entries
    }
}

@Database(
    entities = [
        UserEntry::class,
        UserCollection::class,
        //EntryCollectionCrossRef::class
    ],
    version = 1,  // Keep as 1 if this is a fresh install
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userEntryDao(): UserEntryDao
    abstract fun collectionDao(): UserCollectionDao

    companion object {
        // Use constants for database configuration
        private const val DATABASE_NAME = "RestaurantTrackerApp_database"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}