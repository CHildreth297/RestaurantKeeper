package com.zybooks.restaurantkeeper

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.zybooks.restaurantkeeper.data.AppDatabase
import com.zybooks.restaurantkeeper.data.UserEntry
import com.zybooks.restaurantkeeper.data.UserEntryDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.Locale
import kotlin.coroutines.resume


class EntryViewModel : ViewModel(){
    // location
    var hasPermission by mutableStateOf(false)
    var currentLocation: LatLng? by mutableStateOf(null)
        private set
    var locationClient: FusedLocationProviderClient? = null
        private set

    private val _addressText = MutableStateFlow<String>("Fetching location...")
    val addressText: StateFlow<String> = _addressText

    private val _entryState = MutableStateFlow<UserEntry?>(null)
    val entryState: StateFlow<UserEntry?> = _entryState

    fun requestPermission(
        context: Context,
        permissionLauncher: ManagedActivityResultLauncher<String, Boolean>
    ){
        if (ActivityCompat.checkSelfPermission(
            context, ACCESS_FINE_LOCATION) != PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                context, ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED
            ){
            permissionLauncher.launch(ACCESS_FINE_LOCATION)
        }
        else {
            hasPermission = true
        }
    }

    fun createClient(context: Context){
        if(locationClient == null)
            locationClient = LocationServices.getFusedLocationProviderClient(context)
    }

    @SuppressLint("MissingPermission")
    fun acquireLocation() {
        viewModelScope.launch(Dispatchers.IO) {
            locationClient?.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                CancellationTokenSource().token)?.addOnSuccessListener {
                    location -> if (location != null) {
                        location.let {
                            currentLocation = LatLng(it.latitude, it.longitude)
                        }
                    }
                }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getAddressFromLocation(context: Context, latitude: Double, longitude: Double) {
        _addressText.value = "Fetching address..."
        Log.d("Lat: ", "$latitude")
        Log.d("Long: ", "$longitude")

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())

                // Handle this based on API level
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    // We can't use the callback directly in a coroutine, so use suspendCancellableCoroutine
                    val address = suspendCancellableCoroutine<String> { continuation ->
                        geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                            if (addresses.isNotEmpty()) {
                                continuation.resume(formatAddress(addresses[0]))
                            } else {
                                continuation.resume("No address found")
                            }
                        }
                    }
                    _addressText.value = address

                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    val address = if (addresses != null && addresses.isNotEmpty()) {
                        formatAddress(addresses[0])
                    } else {
                        "No address found"
                    }
                    _addressText.value = address
                }
            } catch (e: Exception) {
                _addressText.value = "Error: ${e.message}"
            }
        }
    }

    // Helper function to format the address
    private fun formatAddress(address: Address): String {
        val sb = StringBuilder()

        // Add the most important parts of the address
        if (address.thoroughfare != null) {
            sb.append(address.thoroughfare) // Street name
        }

        if (address.locality != null) {
            if (sb.isNotEmpty()) sb.append(", ")
            sb.append(address.locality) // City
        }

        if (address.adminArea != null) {
            if (sb.isNotEmpty()) sb.append(", ")
            sb.append(address.adminArea) // State
        }

        // If we couldn't get a meaningful address, fall back to coordinates
        if (sb.isEmpty()) {
            sb.append("${address.latitude}, ${address.longitude}")
        }

        return sb.toString()
    }

    fun saveEntry(id: Int,
                  title: String,
                  location: String,
                  date: LocalDate,
                  rating: Int,
                  comments: String,
                  photos: List<String>,
                  onSaveComplete: () -> Unit,
                  db: AppDatabase) {
        viewModelScope.launch(Dispatchers.IO) {
            // create UserEntry for database

            val entry = UserEntry(
                id = id,
                title = title,
                location = location,
                date = date,
                rating = rating,
                comments = comments,
                photos = photos
            )

            db.userEntryDao().UpsertEntry(entry)

            // switch to main thread to update UI
            withContext(Dispatchers.Main){
                onSaveComplete()
            }

        }

    }

    fun loadEntry(id: Int, db: AppDatabase) {
        viewModelScope.launch(Dispatchers.IO) {
            val entry = db.userEntryDao().getEntryById(id)

            // Switch to Main thread
            withContext(Dispatchers.Main) {
                _entryState.value = entry
            }
        }
    }

}