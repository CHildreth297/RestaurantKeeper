package com.zybooks.restaurantkeeper

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.zybooks.restaurantkeeper.Screens.CollectionScreen
import com.zybooks.restaurantkeeper.Screens.EntryScreen
import com.zybooks.restaurantkeeper.Screens.HomeScreen
import com.zybooks.restaurantkeeper.data.AppDatabase

@androidx.annotation.OptIn(UnstableApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RestaurantTrackerApp(db: AppDatabase) {
    val navController = rememberNavController()
    val context = LocalContext.current

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(navController = navController, db = db)
        }
        composable("entry/{entryId}") { backStackEntry ->
            val entryId = backStackEntry.arguments?.getString("entryId")?.toIntOrNull()
            Log.d("EntryId", "EntryId: $entryId")
            EntryScreen(db,
                entryId = if (entryId != null) entryId else -1,
                onBack = { navController.popBackStack() },
                navController = navController,
                context = context)

        }
        composable("collection/{collectionName}") { backStackEntry ->
            val collectionNameArg = backStackEntry.arguments?.getString("collectionName")
            val collectionName = collectionNameArg.toString() // empty string for new entry

            CollectionScreen(
                collectionName = collectionName,
                onBack = { navController.popBackStack() },
                navController = navController,
                db = db,
                context = context
            )
        }
    }
}