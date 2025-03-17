package com.zybooks.restaurantkeeper.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.time.LocalDate

// make database for collections
@Entity (tableName = "user_collections")
data class UserCollection(
    @PrimaryKey val name: String,
    val description: String,
    @TypeConverters(Converters::class)
    val entries: List<String>,
    @TypeConverters(Converters::class)
    val createdDate: LocalDate,
    val coverImageUri: String? = null
)

//@Entity(
//    tableName = "entry_collection_cross_ref",
//    primaryKeys = ["entryId", "collectionId"],
//    foreignKeys = [
//        ForeignKey(
//            entity = UserEntry::class,
//            parentColumns = ["id"],
//            childColumns = ["entryId"],
//            onDelete = ForeignKey.CASCADE
//        ),
//        ForeignKey(
//            entity = UserCollection::class,
//            parentColumns = ["id"],
//            childColumns = ["collectionId"],
//            onDelete = ForeignKey.CASCADE
//        )
//    ],
//    indices = [
//        Index("entryId"),
//        Index("collectionId")
//    ]
//)
//data class EntryCollectionCrossRef(
//    val entryId: Int,
//    val collectionId: Int
//)