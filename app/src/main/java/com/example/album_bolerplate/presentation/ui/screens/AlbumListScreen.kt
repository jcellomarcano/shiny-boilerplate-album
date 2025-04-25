package com.example.album_bolerplate.presentation.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.material3.Divider
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.album_bolerplate.domain.models.Album
import com.example.album_bolerplate.presentation.viewmodels.AlbumListViewModel
import com.example.album_bolerplate.presentation.ui.common.PaginatedLazyVerticalGrid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumListScreen(
    viewModel: AlbumListViewModel = hiltViewModel(),
    onNavigateToItems: (albumId: Int) -> Unit // Navigation callback
) {
    val albums: LazyPagingItems<Album> = viewModel.albumPagingDataFlow.collectAsLazyPagingItems()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val refreshError by viewModel.refreshError.collectAsState()
    val context = LocalContext.current

    // Effect to check if initial load resulted in empty data and trigger refresh
    LaunchedEffect(albums.loadState) {
        val refreshState = albums.loadState.refresh
        if (refreshState is LoadState.NotLoading && albums.itemCount == 0) {
            Log.d("AlbumListScreen", "Initial load complete, but no items found. Triggering refresh.")
            viewModel.refreshData()
        }
    }

    // Effect to show refresh errors in a Toast (or Snackbar)
    LaunchedEffect(refreshError) {
        refreshError?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            viewModel.clearRefreshError() // Clear error after showing
        }
    }

    LaunchedEffect(albums.loadState) {
        Log.d("AlbumListScreen", "Paging LoadState: ${albums.loadState}")
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Albums") }) }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {

            // *** DEBUGGING: Display LoadState and Item Count (Optional) ***
            Text(
                text = "Item Count: ${albums.itemCount}",
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Text(
                text = "Refresh State: ${albums.loadState.refresh}",
                modifier = Modifier.padding(horizontal = 8.dp),
                color = if (albums.loadState.refresh is LoadState.Error) Color.Red else Color.Gray
            )
            Text(
                text = "Append State: ${albums.loadState.append}",
                modifier = Modifier.padding(horizontal = 8.dp),
                color = if (albums.loadState.append is LoadState.Error) Color.Red else Color.Gray
            )
            Text( // Show manual refresh state
                text = "Manual Refreshing: $isRefreshing",
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color
            )
            PaginatedLazyVerticalGrid(
                pagingItems = albums,
                itemKey = { album -> album.albumId },
                gridCells = GridCells.Fixed(2),
                itemContent = { album ->
                    album?.let {
                        AlbumGridItem(album = it, onAlbumClick = onNavigateToItems)
                    }
                }
            )
        }
    }
}
