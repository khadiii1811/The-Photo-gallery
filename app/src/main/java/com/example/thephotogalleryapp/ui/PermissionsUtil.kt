package com.example.thephotogalleryapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestPermissions(
    permissions: Array<String>,
    onPermissionsGranted: @Composable () -> Unit
) {
    val permissionState = rememberMultiplePermissionsState(permissions.toList())

    when {
        permissionState.allPermissionsGranted -> {
            onPermissionsGranted()
        }
        else -> {
            LaunchedEffect(Unit) {
                permissionState.launchMultiplePermissionRequest()
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Permissions required")
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { permissionState.launchMultiplePermissionRequest() }
                ) {
                    Text("Request Permissions")
                }
            }
        }
    }
}