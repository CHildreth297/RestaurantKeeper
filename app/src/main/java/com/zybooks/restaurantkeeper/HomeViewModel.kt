package com.zybooks.restaurantkeeper

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

// Sealed class for MediaItem
sealed class MediaItem {
    data class Entry(val id: Int, val title: String) : MediaItem()
    data class Collection(val id: Int, val name: String, val items: List<Entry>) : MediaItem()
}

// ViewModel for HomeScreen
class HomeViewModel : ViewModel() {
    var mediaItems = mutableStateListOf<MediaItem>()
        private set

    fun addMediaItem(item: MediaItem) {
        mediaItems.add(item)
    }
}
