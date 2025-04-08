package com.example.thephotogalleryapp.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.thephotogalleryapp.PhotoGalleryApplication
import android.Manifest
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
@Composable
fun PhotoGalleryApp() {
    val context = LocalContext.current
    val application = context.applicationContext as PhotoGalleryApplication
    val repository = application.repository

    val viewModel: PhotoViewModel = viewModel(
        factory = PhotoViewModelFactory(repository)
    )

    val navController = rememberNavController()

    LaunchedEffect(Unit) {
        viewModel.loadSamplePhotos()
    }

    // Tạo launcher xin quyền CAMERA
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(context, "Đã được cấp quyền camera", Toast.LENGTH_SHORT).show()
            // TODO: Gọi mở camera tại đây hoặc truyền trigger xuống
        } else {
            Toast.makeText(context, "Từ chối quyền camera", Toast.LENGTH_SHORT).show()
        }
    }

    NavHost(navController = navController, startDestination = "grid") {
        composable("grid") {
            // Truyền launcher xuống screen nếu cần
            PhotoGridScreen(
                navigateToPhotoView = { index ->
                    navController.navigate("photoView/$index")
                },
                requestCameraPermission = {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            )
        }

        composable("photoView/{index}") { backStackEntry ->
            val index = backStackEntry.arguments?.getString("index")?.toIntOrNull() ?: 0

            PhotoViewScreen(
                photoIndex = index,
                navigateBack = { navController.popBackStack() },
                repository = repository
            )
        }
    }
}

