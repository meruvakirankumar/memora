package com.meruvakirankumar.memora.presentation.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.meruvakirankumar.memora.domain.model.EventType
import com.meruvakirankumar.memora.domain.model.MemoryStatus
import com.meruvakirankumar.memora.presentation.common.displayLabel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MemoryDetailScreen(
    onBack: () -> Unit,
    viewModel: MemoryDetailViewModel = hiltViewModel(),
) {
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.missing) {
        if (viewModel.missing) onBack()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Memory") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            viewModel.status?.let { StatusPill(it) }

            SectionLabel("Title")
            OutlinedTextField(
                value = viewModel.title,
                onValueChange = viewModel::onTitleChange,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            SectionLabel("Event type")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                EventType.entries.forEach { type ->
                    FilterChip(
                        selected = viewModel.eventType == type,
                        onClick = { viewModel.onEventTypeChange(type) },
                        label = { Text(type.displayLabel()) },
                    )
                }
            }

            SectionLabel("Action date")
            OutlinedTextField(
                value = viewModel.dateText,
                onValueChange = viewModel::onDateChange,
                singleLine = true,
                placeholder = { Text("YYYY-MM-DD") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = viewModel.error != null,
                supportingText = viewModel.error?.let { { Text(it) } },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Filled.CalendarMonth, contentDescription = "Pick date")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            if (showDatePicker) {
                val pickerState = rememberDatePickerState(
                    initialSelectedDateMillis = isoToUtcMillis(viewModel.dateText),
                )
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            pickerState.selectedDateMillis?.let { millis ->
                                viewModel.onPickDate(
                                    Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate(),
                                )
                            }
                            showDatePicker = false
                        }) { Text("OK") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                    },
                ) {
                    DatePicker(state = pickerState)
                }
            }

            SectionLabel("Remind me before")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                (1..10).forEach { days ->
                    FilterChip(
                        selected = viewModel.reminderLeadDays == days,
                        onClick = { viewModel.onReminderLeadDaysChange(days) },
                        label = { Text(if (days == 1) "1 day" else "$days days") },
                    )
                }
            }

            Button(
                onClick = { viewModel.save(onBack) },
                enabled = !viewModel.saving,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
            ) {
                Text(if (viewModel.saving) "Saving…" else "Save changes")
            }

            if (viewModel.isCompleted) {
                FilledTonalButton(onClick = { viewModel.reopen(onBack) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Reopen")
                }
            } else {
                FilledTonalButton(onClick = { viewModel.complete(onBack) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Mark complete")
                }
            }

            TextButton(onClick = { viewModel.delete(onBack) }, modifier = Modifier.fillMaxWidth()) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun StatusPill(status: MemoryStatus) {
    Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = CircleShape) {
        Text(
            status.displayLabel(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

private fun isoToUtcMillis(iso: String): Long? = runCatching {
    LocalDate.parse(iso).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
}.getOrNull()
