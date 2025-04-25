package com.example.album_bolerplate.presentation.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.album_bolerplate.R
import com.example.album_bolerplate.presentation.ui.theme.AppShapes
import com.example.album_bolerplate.domain.models.Album
import com.example.album_bolerplate.presentation.ui.theme.AlbumbolerplateTheme


@Composable
fun AlbumGridItem(
    album: Album,
    onAlbumClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable { onAlbumClick(album.albumId) },
        shape = AppShapes.medium
    ) {
        Column {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(album.albumCoverUrl)
                    .crossfade(true)
                    .build(),
                placeholder = painterResource(R.drawable.ic_placeholder),
                error = painterResource(R.drawable.ic_error),
                contentDescription = album.name ?: "Album cover",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Image takes most space
            )
            Text(
                text = album.name ?: "Album ${album.albumId}",
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AlbumGridItemPreview() {
    AlbumbolerplateTheme {
        AlbumGridItem(
            album = Album(albumId = 1, name = "Album Title Preview", albumCoverUrl = ""),
            onAlbumClick = {}
        )
    }
}