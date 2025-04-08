package com.example.thephotogalleryapp.data

import android.net.Uri
import com.example.thephotogalleryapp.model.Photo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PhotoRepository(private val photoDao: PhotoDao) {
    val allPhotos: Flow<List<Photo>> = photoDao.getAllPhotos().map { entities ->
        entities.map { entity ->
            Photo(
                id = entity.id,
                uri = Uri.parse(entity.uri),
                name = entity.name,
                dateAdded = entity.dateAdded,
                isFavorite = entity.isFavorite
            )
        }
    }

    suspend fun addPhoto(photo: Photo) {
        photoDao.insertPhoto(
            PhotoEntity(
                id = photo.id,
                uri = photo.uri.toString(),
                name = photo.name,
                dateAdded = photo.dateAdded,
                isFavorite = photo.isFavorite
            )
        )
    }

    suspend fun toggleFavorite(id: String, isFavorite: Boolean) {
        photoDao.updateFavorite(id, isFavorite)
    }

    suspend fun deletePhoto(id: String) {
        photoDao.deletePhoto(id)
    }
}