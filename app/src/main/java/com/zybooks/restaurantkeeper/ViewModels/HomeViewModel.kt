package com.zybooks.restaurantkeeper.ViewModels

import android.os.Build
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zybooks.restaurantkeeper.data.AppDatabase
import com.zybooks.restaurantkeeper.data.Converters
import com.zybooks.restaurantkeeper.data.UserEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi

// Sealed class for MediaItem
sealed class MediaItem {
    data class Entry(
        val id: Int,
        val title: String,
        val date: LocalDate,
        val location: String,
        val rating: Int,
        val comments: String,
        val photos: List<String>,
        val coverPhoto: String?) : MediaItem()
    data class Collection(
        val name: String,
        val description: String,
        val entries: List<UserEntry>,
        val createdDate: LocalDate,
        val coverImageUri: String?) : MediaItem()
}


// ViewModel for HomeScreen
class HomeViewModel : ViewModel() {
    var mediaItems = mutableStateListOf<MediaItem>()
        private set

//    init {
//        loadEntries() // load entries when viewmodel is created
//    }

    fun addMediaItem(item: MediaItem) {
        mediaItems.add(item)
    }

    fun loadEntries(db: AppDatabase) {
        viewModelScope.launch(Dispatchers.IO) {
            val databaseEntries = db.userEntryDao().getAllEntries()

            mediaItems.clear() // Clear existing items to avoid duplicates

            databaseEntries.collect { dbEntryList ->
                dbEntryList.forEach { dbEntry ->
                    mediaItems.add(
                        MediaItem.Entry(
                            id = dbEntry.id,
                            title = dbEntry.title,
                            date = dbEntry.date,
                            location = dbEntry.location,
                            rating = dbEntry.rating,
                            comments = dbEntry.comments,
                            photos = dbEntry.photos,
                            coverPhoto = dbEntry.coverPhoto
                        )
                    )
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadCollections(db: AppDatabase){
        viewModelScope.launch(Dispatchers.IO) {
            val databaseCollections = db.collectionDao().getAllCollections()
            val converters = Converters()

            databaseCollections.collect { dbCollectionList ->
                dbCollectionList.forEach { dbCollection ->
                    converters.toUserEntryList(dbCollection.entries.toString())?.let {
                        MediaItem.Collection(
                            name = dbCollection.name,
                            description = dbCollection.description,
                            entries = it,
                            createdDate = dbCollection.createdDate,
                            coverImageUri = dbCollection.coverImageUri,
                        )
                    }?.let {
                        mediaItems.add(
                            it
                        )
                    }
                }
            }
        }
    }

    @OptIn(UnstableApi::class)
    @RequiresApi(Build.VERSION_CODES.O)
    fun getAllEntries(): List<UserEntry>? {
        var convert = Converters()
        val directEntries = mediaItems
            .filterIsInstance<MediaItem.Entry>()

        var entriesList = convert.MediaItemstoUserEntryList(directEntries)
        // Convert to UserEntry

        Log.d("entriesList", "$entriesList")
        return entriesList
    }
}

