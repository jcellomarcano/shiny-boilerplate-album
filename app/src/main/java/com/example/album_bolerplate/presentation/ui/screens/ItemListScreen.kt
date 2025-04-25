package com.example.album_bolerplate.presentation.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.album_bolerplate.domain.models.Item
import com.example.album_bolerplate.presentation.viewmodels.ItemListViewModel
import com.example.album_bolerplate.presentation.ui.common.PaginatedLazyVerticalGrid


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemListScreen(
    viewModel: ItemListViewModel = hiltViewModel(),
    onNavigateToDetail: (itemId: Int) -> Unit,
    onNavigateBack: () -> Unit
) {
    val items: LazyPagingItems<Item> = viewModel.itemPagingDataFlow.collectAsLazyPagingItems()
    val albumId by viewModel.albumId.collectAsState() // Get albumId for title

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = albumId?.let { "Album $it Items" } ?: "Items") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        PaginatedLazyVerticalGrid(
            modifier = Modifier.padding(paddingValues),
            pagingItems = items,
            itemKey = { item -> item.id },
            gridCells = GridCells.Fixed(2),
            itemContent = { item ->
                item?.let {
                    ItemGridItem(item = it, onItemClick = onNavigateToDetail)
                }
            }
        )
    }
}