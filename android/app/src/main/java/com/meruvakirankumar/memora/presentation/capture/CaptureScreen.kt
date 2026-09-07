package com.meruvakirankumar.memora.presentation.capture

import android.Manifest
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

private val Cream = Color(0xFFFFFDF7)
private val DarkGreen = Color(0xFF17312D)
private val EmptyBg = Color(0xFFEAF3EE)
private val Muted = Color(0xFF557068)

@Composable
fun CaptureScreen(
    onContinue: (imageUri: String) -> Unit,
    onManualEntry: () -> Unit,
    onBack: () -> Unit,
    viewModel: CaptureViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    var preview by remember { mutableStateOf<ImageBitmap?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        viewModel.onCameraResult(success)
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            viewModel.prepareCameraTarget { uri -> cameraLauncher.launch(Uri.parse(uri)) }
        }
    }
    val pickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        viewModel.onExternalPicked(uri?.toString())
    }

    val capturedUri = viewModel.capturedUri
    LaunchedEffect(capturedUri) {
        preview = capturedUri?.let { decodeBitmap(context, it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("Capture", fontSize = 30.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
        Text(
            "Take a photo or choose an image of the label, bill, or document you want to remember.",
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.secondary,
        )

        if (preview != null) {
            Image(
                bitmap = preview!!,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
                    .clip(RoundedCornerShape(16.dp)),
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(EmptyBg)
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    if (viewModel.busy) "Loading image..." else "No image captured",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = DarkGreen,
                    textAlign = TextAlign.Center,
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            CaptureButton("Take Photo", Modifier.weight(1f), MaterialTheme.colorScheme.primary) {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
            CaptureButton("Choose Image", Modifier.weight(1f), DarkGreen) {
                pickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        }

        Button(
            onClick = { capturedUri?.let(onContinue) },
            enabled = capturedUri != null,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Continue", color = Cream, fontWeight = FontWeight.Black)
        }

        TextButton(onClick = onManualEntry, modifier = Modifier.fillMaxWidth()) {
            Text("Enter manually instead", color = MaterialTheme.colorScheme.secondary)
        }
        TextButton(
            onClick = {
                viewModel.discard()
                onBack()
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Cancel", color = Muted)
        }
    }
}

@Composable
private fun CaptureButton(text: String, modifier: Modifier, background: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = background),
        modifier = modifier,
    ) {
        Text(text, color = Cream, fontWeight = FontWeight.Black)
    }
}

private fun decodeBitmap(context: android.content.Context, uri: String): ImageBitmap? =
    runCatching {
        context.contentResolver.openInputStream(Uri.parse(uri)).use { input ->
            BitmapFactory.decodeStream(input)?.asImageBitmap()
        }
    }.getOrNull()
