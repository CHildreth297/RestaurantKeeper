package com.zybooks.restaurantkeeper.data

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.zybooks.restaurantkeeper.MediaItem
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
        Log.d("Converters", "fromString - Input value: $value")

        // Check if the input contains "UserEntry"
        val result: List<String> = if (value.contains("UserEntry")) {
            Log.d("Converters", "Entered UserEntry handling block.")

            // Handle serialized UserEntry list
            value.split("),")
                .map { it.trim() + ")" }  // Keep the closing parenthesis for UserEntry
                .filter { it.isNotEmpty() }
        } else {
            Log.d("Converters", "Entered URI handling block.")

            // Otherwise, treat it as a list of URIs
            value.split(",")
                .map {
                    val trimmedUri = it.trim()

                    // Remove the last character (closing parenthesis) if it exists
                    val finalUri = if (trimmedUri.isNotEmpty() && trimmedUri.last() == ')') {
                        trimmedUri.dropLast(1)
                    } else {
                        trimmedUri
                    }

                    finalUri
                }
                .filter { it.isNotEmpty() }
        }

        Log.d("Converters", "fromString - Output list: $result")
        return result
    }



    @TypeConverter
    fun toString(list: List<String>): String {
        Log.d("Converters", "toString - Input list: $list")
        val result = list.joinToString(",")
        Log.d("Converters", "toString - Output value: $result")
        return result
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
        if (value.isNullOrBlank()) return null

        Log.d("input for toUserEntryList", value)

        // Remove surrounding brackets [ ]
        val trimmedValue = value.removePrefix("[").removeSuffix("]")

        // Split entries while handling multiple entries
        val entryRegex = Regex("""UserEntry\((.*?)\)""")
        val matches = entryRegex.findAll(trimmedValue)

        val entries = mutableListOf<UserEntry>()

        for (match in matches) {
            val entryString = match.groupValues[1] // Extract inside UserEntry(...)

            // Extract fields manually
            val id = Regex("""id=(\d+)""").find(entryString)?.groupValues?.get(1)?.toInt() ?: 0
            val title = Regex("""title=([^,]+)""").find(entryString)?.groupValues?.get(1)?.trim() ?: ""
            val location = Regex("""location=([^,]*)""").find(entryString)?.groupValues?.get(1)?.trim() ?: ""
            val dateStr = Regex("""date=([0-9-]+)""").find(entryString)?.groupValues?.get(1) ?: LocalDate.now().toString()
            val rating = Regex("""rating=(\d+)""").find(entryString)?.groupValues?.get(1)?.toInt() ?: 0
            val comments = Regex("""comments=([^,]*)""").find(entryString)?.groupValues?.get(1)?.trim() ?: ""

            // Extract photos array - make sure to account for coverPhoto field following it
            val photosMatch = Regex("""photos=\[(.*?)\]""").find(entryString)
            val photos = photosMatch?.groupValues?.get(1)?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()

            // Extract coverPhoto (likely the last field)
            val coverPhoto = Regex("""coverPhoto=([^)]*)""").find(entryString)?.groupValues?.get(1)?.trim() ?: ""

            // Construct UserEntry object
            val entry = UserEntry(
                id = id,
                title = title,
                location = location,
                date = try { LocalDate.parse(dateStr) } catch (e: Exception) { LocalDate.now() },
                rating = rating,
                comments = comments,
                photos = photos,
                coverPhoto = coverPhoto
            )
            entries.add(entry)
        }

        return entries
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverter
    fun toUserEntry(value: String?): UserEntry? {
        if (value.isNullOrEmpty()) return null

        return try {
            // Extract fields using regex - updated to handle coverPhoto
            val regex = """UserEntry\(id=(\d+), title=([^,]*), location=([^,]*), date=([\d-]+), rating=(\d+), comments=([^,]*), photos=\[(.*?)], coverPhoto=([^)]*)""".toRegex()
            val matchResult = regex.find(value)

            if (matchResult != null) {
                val (id, title, location, date, rating, comments, photos, coverPhoto) = matchResult.destructured

                // Convert the extracted values to appropriate types
                UserEntry(
                    id = id.toInt(),
                    title = title.trim(),
                    location = location.trim(),
                    date = LocalDate.parse(date),
                    rating = rating.toInt(),
                    comments = comments.trim(),
                    photos = if (photos.isNotEmpty()) photos.split(", ").map { it.trim() } else emptyList(),
                    coverPhoto = coverPhoto.trim()
                )
            } else {
                null // Return null if parsing fails
            }
        } catch (e: Exception) {
            Log.e("toUserEntry", "Failed to parse entry: $value", e)
            null
        }
    }


    @TypeConverter
    fun MediaItemstoUserEntryList(value: List<MediaItem.Entry>?): List<UserEntry>? {
        if (value == null) return null

        val userEntries = mutableListOf<UserEntry>()

        for (entry in value) {
            val userEntry = entry.coverPhoto?.let {
                UserEntry(
                    id = entry.id,
                    title = entry.title,
                    location = entry.location,
                    date = entry.date,
                    rating = entry.rating,
                    comments = entry.comments,
                    photos = entry.photos,
                    coverPhoto = it
                )
            }
            if (userEntry != null) {
                userEntries.add(userEntry)
            }
        }

        return userEntries
    }

}


@Database(
    entities = [
        UserEntry::class,
        UserCollection::class,
        //EntryCollectionCrossRef::class
    ],
    version = 2,  // Keep as 1 if this is a fresh install
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userEntryDao(): UserEntryDao
    abstract fun collectionDao(): UserCollectionDao


    companion object {
        // Use constants for database configuration
        private const val DATABASE_NAME = "RestaurantTrackerApp_database"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add the coverPhoto column to your table
                database.execSQL("ALTER TABLE user_entries ADD COLUMN coverPhoto TEXT NOT NULL DEFAULT ''")
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}