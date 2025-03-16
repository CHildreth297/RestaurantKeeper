package com.zybooks.restaurantkeeper

import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.zybooks.restaurantkeeper.data.AppDatabase
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CollectionScreen(
    onBack: () -> Unit,
    navController: NavController,
    collectionId: Int,
    collectionViewModel: CollectionViewModel = viewModel(),
    entryViewModel: EntryViewModel = viewModel(),
    homeViewModel: HomeViewModel = viewModel(),
    db: AppDatabase,
    context: Context
) {
    val isNewCollection = collectionId == -1 // Check if creating new

    LaunchedEffect(collectionId) {
        if (!isNewCollection && collectionViewModel.entries.isEmpty()) {
                    entryViewModel.saveEntry(
                        id = 1,
                        title = "test",
                        location = "",
                        date = LocalDate.now(),
                        rating = 0,
                        comments = "",
                        photos = emptyList(),
                        onSaveComplete = {
                            // UI notification
                            Toast.makeText(context, "Entry Saved!",  Toast.LENGTH_SHORT).show()
                            homeViewModel.loadEntries(db = db)

                            navController.navigate("home")},
                        db = db
                    )
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
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            // Collection Name Field
            OutlinedTextField(
                value = collectionViewModel.collectionName.value,
                onValueChange = { collectionViewModel.collectionName.value = it },
                label = { Text("Collection Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Collection Description Field
            OutlinedTextField(
                value = collectionViewModel.description.value,
                onValueChange = { collectionViewModel.description.value = it },
                label = { Text("Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Display Entries in a LazyVerticalGrid (if there are any entries)
            if (collectionViewModel.entries.isNotEmpty()) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(collectionViewModel.entries) { entry ->
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

            Spacer(modifier = Modifier.height(16.dp))

            // Save Button
            Button(
                onClick = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true } // Clears back stack
                    }
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Save")
            }
        }
    }
}