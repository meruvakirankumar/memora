package com.meruvakirankumar.memora.presentation.add

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.meruvakirankumar.memora.domain.model.DateResolution
import com.meruvakirankumar.memora.domain.model.EventType
import com.meruvakirankumar.memora.presentation.common.displayLabel
import com.meruvakirankumar.memora.presentation.common.formatDate

private val Cream = Color(0xFFFFFDF7)
private val DarkGreen = Color(0xFF17312D)
private val ChipBg = Color(0xFFECE3D0)
private val ChipInk = Color(0xFF40504A)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddMemoryScreen(
    onSaved: () -> Unit,
    onBack: () -> Unit,
    viewModel: AddMemoryViewModel = hiltViewModel(),
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("Confirm Memory", fontSize = 26.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.secondary)
        Text(
            "This is what Memora will remember. Edit anything before confirming.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.secondary,
        )

        val context = LocalContext.current
        var thumbnail by remember { mutableStateOf<ImageBitmap?>(null) }
        LaunchedEffect(viewModel.imageUri) {
            thumbnail = viewModel.imageUri?.let { uri ->
                runCatching {
                    context.contentResolver.openInputStream(android.net.Uri.parse(uri)).use { input ->
                        android.graphics.BitmapFactory.decodeStream(input)?.asImageBitmap()
                    }
                }.getOrNull()
            }
        }
        thumbnail?.let { bitmap ->
            Image(
                bitmap = bitmap,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
                    .clip(RoundedCornerShape(16.dp)),
            )
        }

        if (viewModel.extracting) {
            Text("Reading image...", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
        }
        viewModel.extractionNote?.let { note ->
            Text(
                note,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFEAF3EE))
                    .padding(12.dp),
            )
        }

        FieldLabel("Title")
        OutlinedTextField(
            value = viewModel.title,
            onValueChange = viewModel::onTitleChange,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        FieldLabel("Event")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            EventType.entries.forEach { type ->
                val selected = viewModel.eventType == type
                Text(
                    text = type.displayLabel(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = if (selected) Cream else ChipInk,
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (selected) DarkGreen else ChipBg)
                        .clickable { viewModel.onEventTypeChange(type) }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }
        }

        FieldLabel("Action date (YYYY-MM-DD)")
        OutlinedTextField(
            value = viewModel.dateText,
            onValueChange = viewModel::onDateChange,
            singleLine = true,
            placeholder = { Text("2027-08-31") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = viewModel.error != null,
            modifier = Modifier.fillMaxWidth(),
        )

        viewModel.error?.let { message ->
            Text(message, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
        }

        if (viewModel.dateResolution == DateResolution.PICK_ORDER ||
            viewModel.dateResolution == DateResolution.PICK_DATE
        ) {
            FieldLabel(
                if (viewModel.dateResolution == DateResolution.PICK_ORDER) {
                    "Which date is correct?"
                } else {
                    "Choose the correct date"
                },
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                viewModel.dateOptions.forEach { option ->
                    Text(
                        text = formatDate(option),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = ChipInk,
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(ChipBg)
                            .clickable { viewModel.onPickDate(option) }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                    )
                }
            }
        }

        FieldLabel("Remind me this many days before")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            (1..10).forEach { days ->
                val selected = viewModel.reminderLeadDays == days
                Text(
                    text = days.toString(),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = if (selected) Cream else ChipInk,
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (selected) DarkGreen else ChipBg)
                        .clickable { viewModel.onReminderLeadDaysChange(days) }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                )
            }
        }

        Button(
            onClick = { viewModel.save(onSaved) },
            enabled = !viewModel.saving,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (viewModel.saving) "Saving..." else "Confirm Memory", color = Cream, fontWeight = FontWeight.Black)
        }

        TextButton(onClick = { viewModel.discard(onBack) }, modifier = Modifier.fillMaxWidth()) {
            Text("Cancel", color = MaterialTheme.colorScheme.secondary)
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(text, fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color(0xFF4F5C57))
}
