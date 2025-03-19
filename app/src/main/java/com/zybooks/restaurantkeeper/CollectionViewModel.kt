package com.zybooks.restaurantkeeper

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zybooks.restaurantkeeper.MediaItem
import com.zybooks.restaurantkeeper.data.AppDatabase
import com.zybooks.restaurantkeeper.data.UserCollection
import com.zybooks.restaurantkeeper.data.UserEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import com.zybooks.restaurantkeeper.data.UserCollectionDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CollectionViewModel : ViewModel() {
    var collectionName = mutableStateOf("")
        private set

    var description = mutableStateOf("")
        private set

    val entries = mutableStateListOf<MediaItem.Entry>()

    private val _collectionState = MutableStateFlow<UserCollection?>(null)
    val collectionState: StateFlow<UserCollection?> = _collectionState

    private val _nextCollectionId = MutableStateFlow(1)
    val nextCollectionId: StateFlow<Int> = _nextCollectionId.asStateFlow()

    var collectionPhotos = mutableStateListOf<List<String>>()

    fun saveCollection(
                  name: String,
                  description: String,
                  entries: List<String>,
                  createdDate: LocalDate,
                  coverImageUri: String,
                  onSaveComplete: () -> Unit,
                  db: AppDatabase
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            // create UserCollection for database

            val collection = UserCollection(
                name = name,
                description = description,
                entries = entries,
                createdDate = createdDate,
                coverImageUri = coverImageUri
            )
            db.collectionDao().upsertCollection(collection)
            Log.d("CollectionViewModel", "Collection upserted")

            // Immediately verify it's really there
            val verifyCollection = db.collectionDao().getCollectionById(name)
            Log.d("CollectionViewModelVerify", "Verification query returned: ${verifyCollection != null}")
            if (verifyCollection != null) {
                Log.d("CollectionViewModelVerify", "Verified entry details: ${verifyCollection.name}")
            }

            // switch to main thread to update UI
            withContext(Dispatchers.Main){
                onSaveComplete()
            }

        }

    }

    fun loadCollection(name: String, db: AppDatabase) {
        viewModelScope.launch(Dispatchers.IO) {
            val collection = db.collectionDao().getCollectionById(name)
            Log.d("loading collection", "$collection")
            // Switch to Main thread
            withContext(Dispatchers.Main) {
                _collectionState.value = collection
            }
        }
    }

}