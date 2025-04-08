// Path: app/kotlin+java/com.example.thephotogalleryapp/MainActivity.kt

package com.example.thephotogalleryapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.thephotogalleryapp.data.PhotoRepository
import com.example.thephotogalleryapp.ui.PhotoGalleryApp
import com.example.thephotogalleryapp.ui.PhotoViewModel
import com.example.thephotogalleryapp.ui.PhotoViewModelFactory
import com.example.thephotogalleryapp.ui.theme.ThePhotoGalleryAppTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Lấy repository từ Application class
        val repository: PhotoRepository = (application as PhotoGalleryApplication).repository

        setContent {
            ThePhotoGalleryAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Truyền factory để khởi tạo ViewModel
                    val viewModel: PhotoViewModel = viewModel(
                        factory = PhotoViewModelFactory(repository)
                    )
                    PhotoGalleryApp()
                }
            }
        }
    }
}