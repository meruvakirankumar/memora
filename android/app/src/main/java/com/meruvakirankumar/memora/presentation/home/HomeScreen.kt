package com.meruvakirankumar.memora.presentation.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.LaunchedEffect
import com.meruvakirankumar.memora.domain.model.MemoryStatus
import com.meruvakirankumar.memora.presentation.common.displayLabel
import com.meruvakirankumar.memora.presentation.common.formatDate
import com.meruvakirankumar.memora.presentation.theme.StatusColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddMemory: () -> Unit,
    onOpenMemory: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val query by viewModel.searchQuery.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.messages.collect { message ->
            val result = snackbarHostState.showSnackbar(
                message = message.text,
                actionLabel = message.actionLabel,
            )
            if (result == SnackbarResult.ActionPerformed) message.action?.invoke()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Memora", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddMemory,
                text = { Text("New memory") },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                SearchField(
                    query = query,
                    onQueryChange = viewModel::onQueryChange,
                    onClear = { viewModel.onQueryChange("") },
                )
            }

            when {
                state.isNoSearchResults -> item { NoResultsState(query) }
                state.isEmpty -> item { EmptyState() }
                else -> {
                    item { SummaryRow(state.overdue, state.dueToday, state.upcoming) }

                    if (state.active.isNotEmpty()) {
                        item { SectionHeader("Needs attention") }
                        items(state.active, key = { it.id }) { memory ->
                            ActiveMemoryCard(
                                memory = memory,
                                onOpen = { onOpenMemory(memory.id) },
                                onComplete = { viewModel.complete(memory.id) },
                            )
                        }
                    }

                    if (state.completed.isNotEmpty()) {
                        item { SectionHeader("Completed") }
                        items(state.completed, key = { it.id }) { memory ->
                            CompletedMemoryCard(
                                memory = memory,
                                onOpen = { onOpenMemory(memory.id) },
                                onDelete = { viewModel.delete(memory.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit, onClear: () -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        placeholder = { Text("Search memories") },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(Icons.Filled.Close, contentDescription = "Clear")
                }
            }
        },
    )
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp),
    )
}

@Composable
private fun SummaryRow(overdue: Int, dueToday: Int, upcoming: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        StatCard("Overdue", overdue, StatusColors.overdueContainer(), StatusColors.overdueContent(), Modifier.weight(1f))
        StatCard("Due today", dueToday, StatusColors.dueTodayContainer(), StatusColors.dueTodayContent(), Modifier.weight(1f))
        StatCard("Upcoming", upcoming, StatusColors.upcomingContainer(), StatusColors.upcomingContent(), Modifier.weight(1f))
    }
}

@Composable
private fun StatCard(
    label: String,
    count: Int,
    container: androidx.compose.ui.graphics.Color,
    content: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    Surface(color = container, shape = MaterialTheme.shapes.medium, modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("$count", style = MaterialTheme.typography.headlineMedium, color = content)
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = content,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

@Composable
private fun ActiveMemoryCard(memory: MemoryUi, onOpen: () -> Unit, onComplete: () -> Unit) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    memory.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                StatusPill(memory.status)
            }
            Text(
                "${memory.eventType.displayLabel()} · ${formatDate(memory.eventDate)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FilledTonalButton(onClick = onComplete, modifier = Modifier.fillMaxWidth()) {
                Text("Mark complete")
            }
        }
    }
}

@Composable
private fun CompletedMemoryCard(memory: MemoryUi, onOpen: () -> Unit, onDelete: () -> Unit) {
    OutlinedCard(
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen),
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    memory.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textDecoration = TextDecoration.LineThrough,
                )
                Text(
                    "${memory.eventType.displayLabel()} · ${formatDate(memory.eventDate)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.DeleteOutline,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun StatusPill(status: MemoryStatus) {
    val (container, content) = when (status) {
        MemoryStatus.OVERDUE -> StatusColors.overdueContainer() to StatusColors.overdueContent()
        MemoryStatus.DUE_TODAY -> StatusColors.dueTodayContainer() to StatusColors.dueTodayContent()
        MemoryStatus.COMPLETED -> StatusColors.completedContainer() to StatusColors.completedContent()
        else -> StatusColors.upcomingContainer() to StatusColors.upcomingContent()
    }
    Surface(color = container, shape = CircleShape) {
        Text(
            status.displayLabel(),
            style = MaterialTheme.typography.labelMedium,
            color = content,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun EmptyState() {
    CenteredMessage(
        icon = Icons.Outlined.Inbox,
        title = "Nothing to remember yet",
        body = "Capture a label, bill, or document and Memora will remind you at the right time.",
    )
}

@Composable
private fun NoResultsState(query: String) {
    CenteredMessage(
        icon = Icons.Outlined.SearchOff,
        title = "No matches",
        body = "Nothing matches \"$query\". Try a different search.",
    )
}

@Composable
private fun CenteredMessage(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    body: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 72.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(56.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
        Text(
            body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp, start = 24.dp, end = 24.dp),
        )
    }
}
