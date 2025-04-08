package com.example.thephotogalleryapp.model

import android.net.Uri
import java.util.UUID

data class Photo(
    val id: String = UUID.randomUUID().toString(),
    val uri: Uri,
    val name: String,
    val dateAdded: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)