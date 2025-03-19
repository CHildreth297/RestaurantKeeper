package com.zybooks.restaurantkeeper.Screens

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.zybooks.restaurantkeeper.HomeViewModel
import com.zybooks.restaurantkeeper.MediaItem
import com.zybooks.restaurantkeeper.data.AppDatabase

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel = viewModel(),
               navController: NavController,
               db: AppDatabase
) {

    var showDialog by remember { mutableStateOf(false) }

    // Add an initial entry to the list when the screen is first composed
    LaunchedEffect(Unit) {
        viewModel.loadEntries(db) // Reload entries when screen becomes active
        viewModel.loadCollections(db)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "My App", fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { /* TODO: Open navigation drawer */ }) {
                        Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Open user profile */ }) {
                        Icon(imageVector = Icons.Default.Person, contentDescription = "User Profile")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                TextField(
                    value = "",
                    onValueChange = {},
                    placeholder = { Text("Search...") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                shape = CircleShape
            ) {
                Text("+")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            if (viewModel.mediaItems.isEmpty()) {
                Text(
                    text = "You haven't made any entries or collections yet.\nClick the \"+\" to begin!",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2), // Change to Adaptive(150.dp) for dynamic sizing
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(viewModel.mediaItems) { item ->
                        when (item) {
                            is MediaItem.Entry -> {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                        .clickable {
                                            navController.navigate("entry/${item.id}") // Navigate on click
                                        },
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    ) {
                                        Text(
                                            text = item.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                        // Replace with an actual image URL or drawable resource later
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(150.dp)
                                                .background(Color.Gray) // Placeholder background
                                        ) {
                                            val imageUrl = item.photos.firstOrNull() // No need to convert to URI

                                            if (imageUrl != null) {
                                                AsyncImage(
                                                    model = imageUrl,  // Directly pass the string URL
                                                    contentDescription = "Media Image",
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                Text(
                                                    text = "No Image Available",
                                                    color = Color.White,
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier.align(Alignment.Center)
                                                )
                                            }
                                        }

                                    }
                                }
                            }
                            is MediaItem.Collection -> {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                        .clickable { navController.navigate("collection/${item.name}") }, // Navigate to collection screen
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    ) {
                                        Text(
                                            text = item.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )


                                        // 2x2 Grid of Images
                                        Column {
                                            val context = LocalContext.current
                                            val images = item.entries.mapNotNull { it.photos.firstOrNull()?.toUri() }.take(4) // Get first image of each entry
                                            val totalImages = images.size

                                            Log.d("Entries image debug", "Entries: ${item.entries}")
                                            Log.d("ImageDebug", "Images: $images")
                                            Log.d("ImageDebug", "Total Images: $totalImages")

                                            repeat(2) { row ->
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    repeat(2) { col ->
                                                        val index = row * 2 + col
                                                        if (index < totalImages) {
                                                            val imageUri = images[index]
                                                            AsyncImage(
                                                                model = ImageRequest.Builder(context)
                                                                    .data(imageUri)
                                                                    .crossfade(true)
                                                                    .build(),
                                                                contentDescription = "Entry Image",
                                                                modifier = Modifier
                                                                    .weight(1f)
                                                                    .aspectRatio(1f),
                                                                contentScale = ContentScale.Crop
                                                            )
                                                        } else {
                                                            // Show stock image if there aren't enough real images
                                                            Box(
                                                                modifier = Modifier
                                                                    .weight(1f)
                                                                    .aspectRatio(1f)
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
                                                Spacer(modifier = Modifier.height(8.dp)) // Spacing between rows
                                            }
                                        }

                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Create New") },
                text = { Text("Would you like to create a new Entry or Collection?") },
                confirmButton = {
                    Button(
                        onClick = {
                            showDialog = false
                            navController.navigate("entry/new")
                        }
                    ) {
                        Text("Entry")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            showDialog = false
                            navController.navigate("collection/new")
                        }
                    ) {
                        Text("Collection")
                    }
                }
            )

        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    val navController = rememberNavController() // Create a mock NavController
    lateinit var db: AppDatabase // create mock db

    MaterialTheme {
        Surface {
            HomeScreen(navController = navController, db = db) // Pass it to HomeScreen
        }
    }
}

