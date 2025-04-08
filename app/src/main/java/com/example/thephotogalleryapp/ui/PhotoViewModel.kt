package com.example.thephotogalleryapp.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thephotogalleryapp.data.PhotoRepository
import com.example.thephotogalleryapp.model.Photo
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PhotoViewModel(private val repository: PhotoRepository) : ViewModel() {

    val photos: StateFlow<List<Photo>> = repository.allPhotos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentPhotoIndex = MutableStateFlow(0)
    val currentPhotoIndex: StateFlow<Int> = _currentPhotoIndex.asStateFlow()

    val currentPhoto: StateFlow<Photo?> = combine(photos, currentPhotoIndex) { photos, index ->
        photos.getOrNull(index)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun openPhoto(index: Int) {
        viewModelScope.launch {
            // Đợi cho tới khi danh sách ảnh không rỗng
            val photos = photos.first { it.isNotEmpty() }

            // Kiểm tra index hợp lệ và log để debug
            println("Mở ảnh: Index yêu cầu = $index, tổng số ảnh = ${photos.size}")


            if (index in 0 until photos.size) {
                _currentPhotoIndex.value = index
                println("Đã thiết lập index = $_currentPhotoIndex.value")
            } else {
                _currentPhotoIndex.value = 0
                println("Index không hợp lệ, thiết lập về 0")
            }
        }
    }

    fun previousPhoto() {
        if (photos.value.isNotEmpty()) {
            _currentPhotoIndex.update { index ->
                if (index > 0) index - 1 else index
            }
        }
    }

    fun nextPhoto() {
        if (photos.value.isNotEmpty()) {
            _currentPhotoIndex.update { index ->
                if (index < photos.value.lastIndex) index + 1 else index
            }
        }
    }

    fun toggleFavorite(photoId: String) {
        val target = photos.value.find { it.id == photoId } ?: return
        viewModelScope.launch {
            repository.toggleFavorite(photoId, !target.isFavorite)
        }
    }

    fun deletePhoto(photoId: String) {
        viewModelScope.launch {
            repository.deletePhoto(photoId)
            // Nếu xóa ảnh hiện tại thì cần cập nhật lại index hợp lệ
            val index = photos.value.indexOfFirst { it.id == photoId }
            if (index != -1 && _currentPhotoIndex.value >= photos.value.size) {
                _currentPhotoIndex.value = (photos.value.size - 1).coerceAtLeast(0)
            }
        }
    }

    fun addPhotoFromUri(uri: Uri, name: String) {
        val newPhoto = Photo(uri = uri, name = name)
        viewModelScope.launch {
            repository.addPhoto(newPhoto)
        }
    }

    fun loadSamplePhotos() {
        viewModelScope.launch {
            val samplePhotos = listOf(
                Photo("1", Uri.parse("https://picsum.photos/id/1/500/500"), "Photo 1"),
                Photo("2", Uri.parse("https://picsum.photos/id/2/500/500"), "Photo 2"),
                Photo("3", Uri.parse("https://picsum.photos/id/3/500/500"), "Photo 3")
            )
            samplePhotos.forEach { repository.addPhoto(it) }
        }
    }
}
