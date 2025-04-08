package com.example.thephotogalleryapp.ui

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.thephotogalleryapp.PhotoGalleryApplication
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PhotoGridScreen(
    navigateToPhotoView: (Int) -> Unit,
    requestCameraPermission: () -> Unit // <- bạn truyền từ trên xuống
) {
    val context = LocalContext.current
    val application = context.applicationContext as PhotoGalleryApplication
    val repository = application.repository
    val viewModel: PhotoViewModel = viewModel(factory = PhotoViewModelFactory(repository))

    val photos by viewModel.photos.collectAsState()
    var expanded by remember { mutableStateOf(false) }
    var currentPhotoURI by remember { mutableStateOf<Uri?>(null) }

    // Launcher chọn ảnh từ thư viện
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val photoName = "Gallery_$timeStamp"
            viewModel.addPhotoFromUri(it, photoName)
        }
    }

    // Launcher chụp ảnh
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        val capturedUri = currentPhotoURI
        if (success && capturedUri != null) {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val photoName = "Camera_$timeStamp"
            viewModel.addPhotoFromUri(capturedUri, photoName)
        } else {
            Toast.makeText(context, "Không thể chụp ảnh", Toast.LENGTH_SHORT).show()
        }
    }

    // Hàm tạo file ảnh và gọi cameraLauncher
    fun launchCamera() {
        try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val imageFileName = "JPEG_${timeStamp}_"
            val storageDir = context.getExternalFilesDir(null)
            val photoFile = File.createTempFile(imageFileName, ".jpg", storageDir)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
            currentPhotoURI = uri
            cameraLauncher.launch(uri)
        } catch (e: Exception) {
            Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Giao tiếp với bên ngoài nếu cần cấp quyền
    LaunchedEffect(Unit) {
        PhotoGridStateHolder.launchCamera = { launchCamera() }
    }

    Scaffold(
        floatingActionButton = {
            Box {
                FloatingActionButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Photo")
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Chọn từ thư viện") },
                        onClick = {
                            expanded = false
                            galleryLauncher.launch("image/*")
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Chụp ảnh") },
                        onClick = {
                            expanded = false
                            requestCameraPermission() // <- gọi hàm bên ngoài
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Cài đặt") },
                        onClick = {
                            expanded = false
                            Toast.makeText(context, "Đi tới Cài đặt", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 128.dp),
            contentPadding = paddingValues,
            modifier = Modifier.fillMaxSize()
        ) {
            items(photos.size) { index ->
                val photo = photos[index]
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .aspectRatio(1f)
                ) {
                    PhotoGridItem(
                        photo = photo,
                        onClick = { navigateToPhotoView(index) },
                        onLongClick = {
                            Toast.makeText(context, "Long press: ${photo.name}", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }

    // Expose launchCamera ra ngoài nếu bạn muốn
    LaunchedEffect(Unit) {
        PhotoGridStateHolder.launchCamera = { launchCamera() }
    }
}

// Singleton hoặc state holder (nếu bạn muốn gọi launchCamera từ nơi cấp quyền)
object PhotoGridStateHolder {
    var launchCamera: (() -> Unit)? = null
}
