package com.meruvakirankumar.memora.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.meruvakirankumar.memora.presentation.common.badgeColor
import com.meruvakirankumar.memora.presentation.common.displayLabel
import com.meruvakirankumar.memora.presentation.common.formatDate

private val Cream = Color(0xFFFFFDF7)
private val Muted = Color(0xFF557068)
private val TileBg = Color(0xFFEAF3EE)
private val Ink = Color(0xFF15201D)
private val CardBg = Color(0xFFF8F0DD)
private val SubInk = Color(0xFF40504A)
private val DoneBg = Color(0xFFEEF4EA)
private val DoneInk = Color(0xFF3C4A44)
private val Danger = Color(0xFFB23D28)

@Composable
fun HomeScreen(
    onAddMemory: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddMemory,
                containerColor = MaterialTheme.colorScheme.primary,
            ) {
                Text("New Memory", color = Cream, fontWeight = FontWeight.Black)
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(vertical = 20.dp),
        ) {
            item { Header() }

            if (state.isEmpty) {
                item { EmptyState() }
            } else {
                item { SummaryRow(state.overdue, state.dueToday, state.upcoming) }
                items(state.active, key = { it.id }) { memory ->
                    ActiveMemoryCard(memory) { viewModel.complete(memory.id) }
                }
                if (state.completed.isNotEmpty()) {
                    item {
                        Text(
                            "Completed",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                    items(state.completed, key = { it.id }) { memory ->
                        CompletedMemoryCard(memory) { viewModel.delete(memory.id) }
                    }
                }
            }
        }
    }
}

@Composable
private fun Header() {
    Column {
        Text("Memora", fontSize = 34.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
        Text(
            "Remember what matters. Act when it's time.",
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.secondary,
        )
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(24.dp),
    ) {
        Text("No memories yet", fontSize = 18.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.secondary)
        Text(
            "Add your first memory and Memora will remind you when it's time to act.",
            fontSize = 14.sp,
            color = Muted,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Composable
private fun SummaryRow(overdue: Int, dueToday: Int, upcoming: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        SummaryTile("Overdue", overdue, Modifier.weight(1f))
        SummaryTile("Due Today", dueToday, Modifier.weight(1f))
        SummaryTile("Upcoming", upcoming, Modifier.weight(1f))
    }
}

@Composable
private fun SummaryTile(label: String, count: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(TileBg)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("$count", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Ink)
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Muted)
    }
}

@Composable
private fun ActiveMemoryCard(memory: MemoryUi, onComplete: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardBg)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(memory.title, fontSize = 17.sp, fontWeight = FontWeight.Black, color = Ink, modifier = Modifier.weight(1f))
            StatusBadge(memory)
        }
        Text(
            "${memory.eventType.displayLabel()} on ${formatDate(memory.eventDate)}",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = SubInk,
        )
        ExtendedFloatingActionButton(
            onClick = onComplete,
            containerColor = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Mark Complete", color = Cream, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun CompletedMemoryCard(memory: MemoryUi, onDelete: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DoneBg)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            memory.title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            color = DoneInk,
            textDecoration = TextDecoration.LineThrough,
        )
        Text(
            "${memory.eventType.displayLabel()} on ${formatDate(memory.eventDate)}",
            fontSize = 14.sp,
            color = SubInk,
        )
        Text(
            "Delete",
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            color = Danger,
            modifier = Modifier.clickable { onDelete() },
        )
    }
}

@Composable
private fun StatusBadge(memory: MemoryUi) {
    Text(
        memory.status.displayLabel(),
        fontSize = 11.sp,
        fontWeight = FontWeight.Black,
        color = Ink,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(memory.status.badgeColor())
            .padding(horizontal = 10.dp, vertical = 6.dp),
    )
}
