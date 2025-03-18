package com.zybooks.restaurantkeeper.Screens

import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.zybooks.restaurantkeeper.CollectionViewModel
import com.zybooks.restaurantkeeper.HomeViewModel
import com.zybooks.restaurantkeeper.MediaItem
import com.zybooks.restaurantkeeper.data.AppDatabase
import com.zybooks.restaurantkeeper.data.Converters
import com.zybooks.restaurantkeeper.data.UserEntry
import java.time.LocalDate
import com.google.gson.Gson

fun updateEntriesWithAllEntries(entries: MutableList<UserEntry>, allEntries: List<UserEntry>) {
    for (i in entries.indices) {
        val entry = entries[i]
        // Find the corresponding entry in allEntries by matching ID
        val matchingEntry = allEntries.find { it.id == entry.id }

        // If a matching entry exists and the title is different, create a new UserEntry with updated title
        matchingEntry?.let {
            if (entry.title != it.title) {
                // Create a new entry with updated title
                entries[i] = entry.copy(title = it.title)
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CollectionScreen(
    onBack: () -> Unit,
    navController: NavController,
    collectionName: String,
    collectionViewModel: CollectionViewModel = viewModel(),
    homeViewModel: HomeViewModel = viewModel(),
    db: AppDatabase,
    context: Context
) {
    val gson = Gson()
    val isNewCollection = collectionName == "" // Check if creating new
    val collectionState by collectionViewModel.collectionState.collectAsState()

    // Call loadEntries inside a LaunchedEffect
    LaunchedEffect(key1 = collectionName) {
        // Ensure this only runs once when the collection screen is shown
        homeViewModel.loadEntries(db)  // Load entries when this screen is opened
    }

    // Now you can safely access allEntries once loadEntries has been called
    val allEntries: List<UserEntry>? = homeViewModel.getAllEntries()

    Log.d("HomeViewModel", "All entries: $allEntries")

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val entries = remember { mutableStateListOf<UserEntry>() }
    var createdDate by remember { mutableStateOf(LocalDate.now()) } // Using current date for creation
    var coverImageUri by remember { mutableStateOf("") } // Default empty string for cover image URI
    var showEntrySelection by remember { mutableStateOf(false) } // Dialog visibility state
    val selectedEntries = remember { mutableStateListOf<UserEntry>() }

    val converters = Converters()

    LaunchedEffect(collectionName) {
        Log.d("enter collectionName LE", collectionName)
        if (collectionName != "") {
            Log.d("2 enter collectionName LE", collectionName)
            collectionViewModel.loadCollection(collectionName, db = db)
        }
    }

    LaunchedEffect(collectionState) {
        collectionState?.let { collection ->
            name = collection.name
            description = collection.description

            // Update the entries list contents instead of reassigning
            entries.clear()

            Log.d("TAG", "1 print entry as string: ${collection.entries.joinToString(", ")} | List size: ${collection.entries.size}")

            // Something wrong here?
            for (entry in collection.entries) {
                Log.d("loop print entry as string",entry)
                converters.toUserEntry(entry)?.let { entries.add(it) }
            }

            Log.d("in for loop", "entries: $entries")

            if (allEntries != null) {
                updateEntriesWithAllEntries(entries, allEntries)
            }

            createdDate = collection.createdDate
            coverImageUri = collection.coverImageUri.toString()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (isNewCollection) "New Collection" else "Edit Collection") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showEntrySelection = true }) {
                Text("+")
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                // Collection Name Field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Collection Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Collection Description Field
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    maxLines = 5
                )

                Spacer(modifier = Modifier.height(24.dp))

                Log.d("CollectionScreen", "Entries: $entries")

                // **Fix: Wrap list inside a Box with weight(1f) instead of making Column scrollable**
                Box(
                    modifier = Modifier
                        .weight(1f) // This ensures the grid takes up available space properly
                ) {
                    if (entries.isNotEmpty()) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(entries) { entry ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                        .clickable { navController.navigate("entry/${entry.id}") },
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    ) {
                                        Text(
                                            text = entry.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )

                                        // Placeholder for Entry Image
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(100.dp)
                                                .background(Color.Gray),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "Stock Image",
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Save Button (always stays visible)
                Button(
                    onClick = {
                        Log.d("CollectionScreen", "Entries before saving: $entries")
                        val jsonEntries = gson.toJson(entries) // Convert entries list to a valid JSON string
                        collectionViewModel.saveCollection(
                            name = name,
                            description = description,
                            entries = entries.map { it.toString() },
                            createdDate = createdDate,
                            coverImageUri = coverImageUri,
                            onSaveComplete = {
                                Log.d("CollectionScreen", "the json toString: ${entries.map { it.toString() }} | List size: ${entries.size}")
                                Toast.makeText(context, "Collection Saved!", Toast.LENGTH_SHORT).show()
                                homeViewModel.loadCollections(db = db)
                                navController.navigate("home")
                            },
                            db = db
                        )
                    },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Save")
                }
            }
        }



        // Add the entry selection dialog here
        if (showEntrySelection) {
            AlertDialog(
                onDismissRequest = { showEntrySelection = false },
                title = { Text("Select Entries to Add") },
                text = {


                    LazyColumn {
                        itemsIndexed(allEntries ?: emptyList()) { index, entry ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Checkbox for selecting the entry
                                Checkbox(
                                    checked = selectedEntries.contains(entry),
                                    onCheckedChange = { isChecked ->
                                        if (isChecked) {
                                            if (entry != null) {
                                                selectedEntries.add(entry)
                                            }
                                        } else {
                                            selectedEntries.remove(entry)
                                        }
                                    }
                                )
                                if (entry != null) {
                                    Text(text = entry.title)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showEntrySelection = false
                            Log.d("Selected entries total", "${selectedEntries.size}")
                            // Convert selectedEntries to UserEntry and update entries list
                            entries.clear()

                            for (entry in selectedEntries) {
                                    entries.add(entry)
                                Log.d("selected entry", "selected entry: ${entry.title}")
                            }
//                            selectedEntries.forEach { mediaItem ->
//                                converters.toUserEntry(mediaItem.toString())?.let { userEntry ->
//                                    Log.d("selected entry", "selected entry: ${mediaItem.title}")
//                                    entries.add(userEntry)
//                                }
//                            }

                            // Log the updated entries
                            // Log.d("CollectionScreen", "Updated Entries: $entries")
                        }
                    ) {
                        Text("Done")
                    }
                }

            )
        }
    }
}
