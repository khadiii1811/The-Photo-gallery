package com.example.thephotogalleryapp.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.thephotogalleryapp.data.PhotoRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.*

@Composable
fun PhotoViewScreen(
    photoIndex: Int,
    navigateBack: () -> Unit,
    repository: PhotoRepository
) {
    val factory = remember { PhotoViewModelFactory(repository) }
    val viewModel: PhotoViewModel = viewModel(factory = factory)

    val currentPhoto by viewModel.currentPhoto.collectAsState()
    val context = LocalContext.current

    // Zoom and pan state
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // Drag state
    val dragScope = rememberCoroutineScope()
    var dragOffset by remember { mutableStateOf(0f) }
    val dragState = rememberDraggableState { delta -> dragOffset += delta }

    // Transformable (zoom & pan) state
    val transformableState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 3f)

        val maxX = (scale - 1) * 500
        val maxY = (scale - 1) * 500

        offset = Offset(
            x = (offset.x + offsetChange.x).coerceIn(-maxX / 2, maxX / 2),
            y = (offset.y + offsetChange.y).coerceIn(-maxY / 2, maxY / 2)
        )
    }

    LaunchedEffect(photoIndex) {
        viewModel.openPhoto(photoIndex)
    }
    BackHandler { navigateBack() }

    // UI chính
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .draggable(
                state = dragState,
                orientation = Orientation.Horizontal,
                onDragStarted = {
                    if (scale > 1f) {
                        scale = 1f
                        offset = Offset.Zero
                    }
                },
                onDragStopped = { velocity ->
                    dragScope.launch {
                        when {
                            dragOffset < -100 && velocity < 0 -> viewModel.nextPhoto()
                            dragOffset > 100 && velocity > 0 -> viewModel.previousPhoto()
                        }
                        dragOffset = 0f
                    }
                }
            )
    ) {
        currentPhoto?.let { photo ->
            AsyncImage(
                model = photo.uri,
                contentDescription = photo.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .transformable(transformableState)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offset.x
                        translationY = offset.y
                    }
            )

            // Toolbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = {
                    viewModel.previousPhoto()
                    scale = 1f
                    offset = Offset.Zero
                }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Previous",
                        tint = Color.White
                    )
                }

                IconButton(onClick = {
                    viewModel.toggleFavorite(photo.id)
                    Toast.makeText(context, "Toggled favorite", Toast.LENGTH_SHORT).show()
                }) {
                    Icon(
                        imageVector = if (photo.isFavorite)
                            Icons.Default.Favorite
                        else
                            Icons.Default.FavoriteBorder,
                        contentDescription = "Toggle Favorite",
                        tint = if (photo.isFavorite) Color.Red else Color.White
                    )
                }

                IconButton(onClick = {
                    viewModel.nextPhoto()
                    scale = 1f
                    offset = Offset.Zero
                }) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Next",
                        tint = Color.White
                    )
                }
            }
        } ?: Text(
            text = "No photo selected",
            color = Color.White,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

