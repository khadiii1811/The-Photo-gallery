package com.example.thephotogalleryapp

import android.app.Application
import com.example.thephotogalleryapp.data.PhotoDatabase
import com.example.thephotogalleryapp.data.PhotoRepository
class PhotoGalleryApplication : Application() {
    val database by lazy { PhotoDatabase.getDatabase(this) }
    val repository by lazy { PhotoRepository(database.photoDao()) }
}