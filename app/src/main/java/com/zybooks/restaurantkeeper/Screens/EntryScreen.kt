package com.zybooks.restaurantkeeper.Screens

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.android.gms.maps.model.LatLng
import com.zybooks.restaurantkeeper.EntryViewModel
import com.zybooks.restaurantkeeper.HomeViewModel
import com.zybooks.restaurantkeeper.data.AppDatabase
import com.zybooks.restaurantkeeper.ui.theme.Purple40
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import coil.compose.rememberImagePainter

@RequiresApi(Build.VERSION_CODES.O)
@Preview(
    showBackground = true,
    widthDp = 360,
    heightDp = 800,
    name = "EntryScreenPreview"
)

@Composable
fun EntryScreenPreview() {
    val navController = rememberNavController()
    val context = LocalContext.current
    MaterialTheme {
        Surface {
            EntryScreen(
                db = AppDatabase.getDatabase(LocalContext.current),
                entryId = 0,
                onBack = {},  // Provide an empty lambda for back action
                navController = navController,
                context = context
            )
        }
    }
}


@SuppressLint("StateFlowValueCalledInComposition")
@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EntryScreen(
    db: AppDatabase,
    onBack: () -> Unit,
    entryId: Int,
    entryViewModel: EntryViewModel = viewModel(),
    homeViewModel: HomeViewModel = viewModel(),
    navController: NavController,
    context: Context
) {
    Log.d("EntryIdEntryScreen", "EntryId: $entryId")
    val entryState by entryViewModel.entryState.collectAsState()
    val highestEntryId by entryViewModel.nextEntryId.collectAsState()

    var title by remember { mutableStateOf("") }
    var location by remember ({ mutableStateOf<LatLng?>(null) })
    var location_expand by remember ({ mutableStateOf(false) })
    var UserAddress by remember ({ mutableStateOf("") })
    var ShowManualLocationDialog by remember ({ mutableStateOf(false) })
    var DeniedPermissionDialog by remember ({ mutableStateOf(false) })
    var ShowMap by remember({ mutableStateOf(false) })
    var date by remember { mutableStateOf(LocalDate.now()) }
    var rating by remember { mutableIntStateOf(0) }
    var comments by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var photoUris = remember { mutableStateListOf<Uri>() }


// Load existing entry data if we're editing (entryId > 0)
    LaunchedEffect(entryId) {
        if (entryId > 0) {
            entryViewModel.loadEntry(entryId, db = db)
        }
        else {
            entryViewModel.getHighestId(db = db)
            Log.d("HighestId", "Getting highest id: $highestEntryId")
        }
    }

    LaunchedEffect (entryViewModel.currentLocation) {
        location = entryViewModel.currentLocation
    }

    LaunchedEffect(entryState) {
        entryState?.let { entry ->
            title = entry.title
            UserAddress = entry.location
            date = entry.date
            rating = entry.rating
            comments = entry.comments
//            photoUris = entry.photos.map { it.toUri() }
            photoUris.clear()

//            android.util.Log.d("TAG", "1 print entry as string: ${collection.entries.joinToString(", ")} | List size: ${collection.entries.size}")

            // Something wrong here?
            for (photo in entry.photos) {
//                android.util.Log.d("loop print entry as string",entry)
//                converters.toUserEntry(entry)?.let { entries.add(it) }
                photoUris.add(photo.toUri())
            }
        }
        Log.d("LoadTitle", "${entryState?.title}")
    }


    // Photo picker launcher to select multiple images
    val photoPickLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris: List<Uri> ->
        Log.d("photoPickLauncher", "Selected URIs: $uris")
        // Clear existing photos first
        photoUris.clear()

        // Use a for loop to add the URIs one by one
        for (uri in uris) {
            photoUris.add(uri)
        }

    }

    // date formatter
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMMM d, yyyy") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Entry Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->

        if (showDatePicker) {
            // Initialize with the current date, properly accounting for timezone
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = date
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
            )

            Dialog(
                onDismissRequest = { showDatePicker = false }
            ) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp

                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // calendar
                        DatePicker(
                            state = datePickerState,
                            showModeToggle = false
                        )

                        // separate the calendar from buttons
                        Spacer(modifier = Modifier.height(16.dp))

                        // button row with ample space
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            horizontalArrangement = Arrangement.End
                        ) { // cancel button
                            Button(
                                onClick = { showDatePicker = false },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    contentColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.padding(end = 6.dp)
                            ) {
                                Text(
                                    "Cancel",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // ok button
                            Button(
                                onClick = {
                                    datePickerState.selectedDateMillis?.let { millis ->
                                        date = Instant.ofEpochMilli(millis)
                                            .atZone(ZoneId.systemDefault())
                                            .toLocalDate()
                                        // time returned UTC time, TODO: would want to have another solution besides a hardcoded add day
                                        date = date.plusDays(1)
                                    }
                                    showDatePicker = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Purple40),
                                modifier = Modifier.padding(2.dp)
                            ) {
                                Text(
                                    "OK",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        Column(
            // modifier to control composable's size, behavior, and appearance
            modifier = Modifier
                .fillMaxSize()
//                .padding(16.dp)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Title
            TextField(
                value = title,
                onValueChange = { title = it },
                placeholder = {
                    Text(
                        "Enter title",
                        fontSize = 42.sp,
                        color = Color.Gray.copy(alpha = 0.5f)
                    )
                },
                textStyle = TextStyle(fontSize = 42.sp, fontWeight = FontWeight.Bold),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.background,
                    focusedContainerColor = MaterialTheme.colorScheme.background,
                ),
                singleLine = true,
                maxLines = 1,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Location
            // get location permissions
            if (ShowMap) {

                // check to see if permission is already granted
                val hasLocationPermission = ContextCompat.checkSelfPermission(
                    context,
                    ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED


                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { isGranted -> entryViewModel.hasPermission = isGranted }


                LaunchedEffect(key1 = true) {
                    entryViewModel.hasPermission = hasLocationPermission
                    if (!entryViewModel.hasPermission) {
                        Log.d("PermissionDebug", "launching permission request")
                        permissionLauncher.launch(ACCESS_FINE_LOCATION)
                    }
                }

                if (entryViewModel.hasPermission) {
                    entryViewModel.createClient(context)
                    entryViewModel.acquireLocation()


                    if (location != null) {
                        entryViewModel.getAddressFromLocation(
                            context,
                            location!!.latitude,
                            location!!.longitude
                        )
                        // If using a state variable to store the location
                        UserAddress = entryViewModel.addressText.collectAsState().value

                        // Display the address
                        Text("Your location: $UserAddress")
                    }
                } else {
                    DeniedPermissionDialog = true
                }

            }
            // shows alert dialog if user denies permission to location services
            if (DeniedPermissionDialog) {
                Dialog(onDismissRequest = { DeniedPermissionDialog = false })
                {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        tonalElevation = 6.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Location Access Required",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "To get your current location automatically, please enable location permissions")
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                TextButton(onClick = { DeniedPermissionDialog = false }) {
                                    Text("Cancel")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                TextButton(onClick = {
                                    // This won't grant permissions directly - it should open settings
                                    val intent =
                                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                            data =
                                                Uri.fromParts("package", context.packageName, null)
                                        }
                                    context.startActivity(intent)
                                    DeniedPermissionDialog = false
                                }) {
                                    Text("Allow")
                                }
                            }
                        }
                    }
                }
            }
            // shows dialog for manual entry for location
            if (ShowManualLocationDialog) {
                Dialog(onDismissRequest = { ShowManualLocationDialog = false }) {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 6.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Title
                            Text(
                                text = "Restaurant Name",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            TextField(
                                value = UserAddress,
                                onValueChange = { UserAddress = it },
                                placeholder = { Text("Enter name of the restaurant") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = {
                                        ShowManualLocationDialog = false
                                    }) {
                                    Text("Save")
                                }
                                TextButton(onClick = { ShowManualLocationDialog = false }) {
                                    Text("Cancel")
                                }
                            }
                        }

                    }
                }
            }


            // location
            // TODO (FUTURE) : modify location to use photo metadata
            ExposedDropdownMenuBox(
                expanded = location_expand,
                onExpandedChange = { location_expand = it }
            ) {
                OutlinedTextField(
                    value = UserAddress,
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text("Location", fontSize = 14.sp, color = Color.Gray) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = location_expand)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = location_expand,
                    onDismissRequest = { location_expand = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Enter manually") },
                        onClick = {
                            location_expand = false
                            ShowManualLocationDialog = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Get current location") },
                        onClick = {
                            location_expand = false
                            ShowMap = true
                        }
                    )
                }
            }


            Spacer(modifier = Modifier.height(16.dp))

            // Date
            TextField(
                value = date.format(dateFormatter),
                onValueChange = { },
                label = { Text("Date") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                interactionSource = remember { MutableInteractionSource() }
                    .also { interactionSource -> LaunchedEffect(interactionSource) {
                        interactionSource.interactions.collect {
                            if (it is PressInteraction.Release) {
                                showDatePicker = true
                            }
                        }

                    }
                    }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Rating
            Text(
                "Rating",
                modifier = Modifier.padding(8.dp))
            StarRating(rating = rating, onRatingChanged = { rating = it })

            Spacer(modifier = Modifier.height(16.dp))

            // Comments
            OutlinedTextField(
                value = comments,
                onValueChange = { comments = it },
                label = { Text("Comments") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 10
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Photo Picker (Multiple Photos)
            OutlinedButton(
                onClick = {
                    photoPickLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Select Photos")
            }

            Log.d("len of photosUris","${photoUris.size}")

            // Image Previews
            if (photoUris.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(photoUris) { uri ->
                        Log.d("photo uri", uri.toString())
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(uri) // Provide the URI to load the image
                                .crossfade(true) // Optional: enables crossfade transition
                                .build(),
                            contentDescription = "Selected Photo",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save entry
            Button(
                onClick = {

                    entryViewModel.saveEntry(
                        id = if (entryId != -1) entryId else highestEntryId,
                        title = title,
                        location = UserAddress,
                        date = date,
                        rating = rating,
                        comments = comments,
                        photos = photoUris.map { it.toString() },
                        onSaveComplete = {
                            // UI notification
                            Toast.makeText(context, "Entry Saved!",  Toast.LENGTH_SHORT).show()
                            homeViewModel.loadEntries(db = db)

                            navController.navigate("home")},
                        db = db
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
        }

    }
}

@Composable
fun StarRating(rating: Int, onRatingChanged: (Int) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.Start, // alignment on left
        modifier = Modifier.fillMaxWidth()
    ) {
        for (i in 1..5) { // Five-star rating system
            IconButton(onClick = { onRatingChanged(i) }) {
                Icon(
                    imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = "Rating $i",
                    tint = if (i <= rating) Color(0xFFFFD700) else Color.Gray, // Gold for selected, gray for unselected
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}
