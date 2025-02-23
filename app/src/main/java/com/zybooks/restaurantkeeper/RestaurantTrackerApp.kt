package com.zybooks.restaurantkeeper

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun RestaurantTrackerApp() {
    // TODO: add routing
}


@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EntryScreen(
    onSave: (EntryData) -> Unit = {},
) {
    var title by remember { mutableStateOf("")}
    var location by remember { mutableStateOf("")}
    var date by remember { mutableStateOf(LocalDate.now())}
    var rating by remember { mutableIntStateOf(0) }
    var comments by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    // date formatter
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMMM d, yyyy") }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDatePicker = false
                }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(
                state = rememberDatePickerState(
                    initialSelectedDateMillis = date
                        .toEpochDay() * 24 * 60 * 60 * 1000
                ),
                showModeToggle = false,
                modifier = Modifier.padding(16.dp)
            )
        }

        Column(
            // modifier to control composable's size, behavior, and appearance
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Title
            TextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("Enter title") },
                textStyle = TextStyle(
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold
                ),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.background,
                    focusedContainerColor = MaterialTheme.colorScheme.background,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.background,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary
                ),
                singleLine = true,
                maxLines = 1,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                ),
                modifier = Modifier
                    .fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Location
            // TODO: modify location to use photo metadata
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Date
            TextField(
                value = date.format(dateFormatter),
                onValueChange = { },
                label = { Text("Date") },
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        showDatePicker = true
                    }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Rating
            Text("Rating")
            // TODO: how to create stars as buttons
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

            // Photos
            OutlinedButton(
                onClick = { /* TODO: implement */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                // TODO: Implement
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save entry
            Button(
                onClick = {
                    onSave(
                        EntryData(
                            title = title,
                            location = location,
                            date = date,
                            rating = rating,
                            comments = comments
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
        }
    }
}

// TODO: implement function for keyboard popup
//fun addEntryInput(onEnterEntry: (String) -> Unit){
//    var keyboardController = LocalSoftwareKeyboardController.current
//}

fun EntryList(
    EntryList: List<EntryData>
) {
    // TODO: implement onDelete, onShare, etc.
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


@RequiresApi(Build.VERSION_CODES.O)
@Preview(
    showBackground = true,
    widthDp = 360,
    heightDp = 800,
    name = "EntryScreenPreview"
)

@Composable
fun EntryScreenPreview() {
    MaterialTheme{
        Surface {
            EntryScreen()
        }
    }
}

// Entry page includes the following: Title, Location, Date, Rating, Comments, Photos
data class EntryData(
    val title: String,
    val location: String,
    val date: LocalDate,
    val rating: Int,
    val comments: String,
    val photos: List<String> = emptyList()
)

