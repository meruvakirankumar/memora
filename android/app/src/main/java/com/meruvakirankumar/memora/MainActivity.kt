package com.meruvakirankumar.memora

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import com.meruvakirankumar.memora.notifications.NotificationChannels
import com.meruvakirankumar.memora.ui.MemoraScreen
import com.meruvakirankumar.memora.ui.MemoraViewModel

class MainActivity : ComponentActivity() {

    private var pendingCameraLaunch = false

    private val takePhoto = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmap?.let { viewModelRef?.onImageSelected(it.asImageBitmap()) }
    }

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri ?: return@registerForActivityResult
        val bitmap = decodeBitmap(uri)
        bitmap?.let { viewModelRef?.onImageSelected(it.asImageBitmap()) }
    }

    private val requestCamera = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted && pendingCameraLaunch) {
            pendingCameraLaunch = false
            takePhoto.launch(null)
        }
    }

    private val requestNotifications =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op */ }

    private var viewModelRef: MemoraViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        NotificationChannels.ensureCreated(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            val vm: MemoraViewModel = viewModel()
            viewModelRef = vm
            MemoraScreen(
                viewModel = vm,
                onTakePhoto = ::launchCamera,
                onSelectImage = { pickImage.launch("image/*") },
            )
        }
    }

    private fun launchCamera() {
        pendingCameraLaunch = true
        requestCamera.launch(Manifest.permission.CAMERA)
    }

    private fun decodeBitmap(uri: android.net.Uri): Bitmap? =
        contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
}
