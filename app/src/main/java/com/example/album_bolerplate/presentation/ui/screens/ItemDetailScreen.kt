package com.example.album_bolerplate.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.album_bolerplate.R
import com.example.album_bolerplate.domain.models.Item
import com.example.album_bolerplate.presentation.viewmodels.ItemDetailViewModel
import com.example.album_bolerplate.presentation.ui.common.FullScreenLoading
import com.example.album_bolerplate.presentation.ui.theme.AlbumbolerplateTheme
import com.example.album_bolerplate.presentation.uistates.ItemDetailUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    viewModel: ItemDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // Display title dynamically based on state
                    val titleText = when (val state = uiState) {
                        is ItemDetailUiState.Success -> state.item.title
                        else -> "Item Detail"
                    }
                    Text(titleText)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is ItemDetailUiState.Loading -> FullScreenLoading()
                is ItemDetailUiState.Success -> ItemDetailContent(item = state.item)
                is ItemDetailUiState.NotFound -> Text("Item not found.")
                is ItemDetailUiState.Error -> Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun ItemDetailContent(item: Item) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(item.url) // Use the full URL here
                .crossfade(true)
                .build(),
            placeholder = painterResource(R.drawable.ic_placeholder),
            error = painterResource(R.drawable.ic_error),
            contentDescription = item.title,
            contentScale = ContentScale.Fit, // Fit the image nicely
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // Image takes most space
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = item.title,
            style = MaterialTheme.typography.headlineSmall
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ItemDetailContentPreview() {
    AlbumbolerplateTheme {
        ItemDetailContent(
            item = Item(
                albumId = 1,
                id = 1,
                title = "A Very Detailed Item Title Example",
                url = "https://placehold.co/600x600", // Placeholder URL for preview
                thumbnailUrl = ""
            )
        )
    }
}