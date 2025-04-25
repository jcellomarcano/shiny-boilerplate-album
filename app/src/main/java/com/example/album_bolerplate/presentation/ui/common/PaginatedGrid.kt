package com.example.album_bolerplate.presentation.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey

@Composable
fun <T : Any> PaginatedLazyVerticalGrid(
    modifier: Modifier = Modifier,
    pagingItems: LazyPagingItems<T>,
    itemKey: ((item: T) -> Any)? = null,
    gridCells: GridCells = GridCells.Adaptive(minSize = 128.dp),
    contentPadding: PaddingValues = PaddingValues(8.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(8.dp),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(8.dp),
    itemContent: @Composable LazyGridItemScope.(item: T?) -> Unit
) {
    LazyVerticalGrid(
        columns = gridCells,
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = verticalArrangement,
        horizontalArrangement = horizontalArrangement
    ) {
        items(
            count = pagingItems.itemCount,
            key = pagingItems.itemKey(itemKey)
        ) { index ->
            val item = pagingItems[index]
            itemContent(item)
        }

        // Handle Loading states
        pagingItems.loadState.apply {
            when {
                refresh is LoadState.Error -> {
                    val e = refresh as LoadState.Error
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        ErrorMessageItem(
                            message = e.error.localizedMessage ?: "Error loading initial data",
                            onRetry = { pagingItems.retry() }
                        )
                    }
                }
                append is LoadState.Error -> {
                    val e = append as LoadState.Error
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        ErrorMessageItem(
                            message = e.error.localizedMessage ?: "Error loading more data",
                            onRetry = { pagingItems.retry() }
                        )
                    }
                }
                refresh is LoadState.Loading -> {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        FullScreenLoading()
                    }
                }
                append is LoadState.Loading -> {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        LoadingItem()
                    }
                }
            }
        }
    }
}
