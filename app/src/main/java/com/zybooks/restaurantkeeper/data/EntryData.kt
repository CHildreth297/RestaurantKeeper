package com.zybooks.restaurantkeeper.data


import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.time.LocalDate



// make database for entries
@Entity(tableName = "user_entries")
data class UserEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val location: String,
    @TypeConverters(Converters::class)
    val date: LocalDate,
    val rating: Int,
    val comments: String,
    @TypeConverters(Converters::class)
    val photos: List<String>
)


