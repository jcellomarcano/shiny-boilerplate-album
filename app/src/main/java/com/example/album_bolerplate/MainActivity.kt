package com.example.album_bolerplate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.album_bolerplate.presentation.ui.screens.AlbumListScreen
import com.example.album_bolerplate.presentation.ui.screens.ItemDetailScreen
import com.example.album_bolerplate.presentation.ui.screens.ItemListScreen
import com.example.album_bolerplate.presentation.ui.theme.AlbumbolerplateTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AlbumbolerplateTheme {
                AppNavigation()
            }
        }
    }
}

object AppDestinations {
    const val ALBUM_LIST_ROUTE = "albums"
    const val ITEM_LIST_ROUTE = "items" // Base route
    const val ITEM_DETAIL_ROUTE = "itemDetail" // Base route
    const val ALBUM_ID_ARG = "albumId"
    const val ITEM_ID_ARG = "itemId"

    val itemListRouteWithArgs = "$ITEM_LIST_ROUTE/{$ALBUM_ID_ARG}"
    val itemDetailRouteWithArgs = "$ITEM_DETAIL_ROUTE/{$ITEM_ID_ARG}"
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = AppDestinations.ALBUM_LIST_ROUTE
    ) {
        // Screen 1: Album List
        composable(route = AppDestinations.ALBUM_LIST_ROUTE) {
            AlbumListScreen(
                onNavigateToItems = { albumId ->
                    navController.navigate("${AppDestinations.ITEM_LIST_ROUTE}/$albumId")
                }
            )
        }

        // Screen 2: Item List (per Album)
        composable(
            route = AppDestinations.itemListRouteWithArgs,
            arguments = listOf(navArgument(AppDestinations.ALBUM_ID_ARG) { type = NavType.IntType })
        ) { backStackEntry ->
            ItemListScreen(
                onNavigateToDetail = { itemId ->
                    navController.navigate("${AppDestinations.ITEM_DETAIL_ROUTE}/$itemId")
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Screen 3: Item Detail
        composable(
            route = AppDestinations.itemDetailRouteWithArgs,
            arguments = listOf(navArgument(AppDestinations.ITEM_ID_ARG) { type = NavType.IntType })
        ) { backStackEntry ->
            ItemDetailScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}