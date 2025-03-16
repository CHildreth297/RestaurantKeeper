package com.zybooks.restaurantkeeper

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zybooks.restaurantkeeper.data.AppDatabase
import com.zybooks.restaurantkeeper.data.UserEntry
import com.zybooks.restaurantkeeper.data.UserEntryDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate

// Sealed class for MediaItem
sealed class MediaItem {
    data class Entry(
        val id: Int,
        val title: String,
        val date: LocalDate,
        val location: String,
        val rating: Int,
        val comments: String,
        val photos: List<String>) : MediaItem()
    data class Collection(
        val id: Int,
        val name: String,
        val items: List<Entry>) : MediaItem()
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
                            photos = dbEntry.photos
                        )
                    )
                }
            }
        }
    }

    fun loadCollections(db: AppDatabase){

    }
}

