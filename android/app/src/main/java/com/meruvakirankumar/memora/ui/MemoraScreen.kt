package com.meruvakirankumar.memora.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meruvakirankumar.memora.model.EventType
import com.meruvakirankumar.memora.model.MemoryStatus
import com.meruvakirankumar.memora.logic.MemoryStatusResolver

private val Background = Color(0xFFF6F2E8)
private val Hero = Color(0xFF0E2A2B)
private val Accent = Color(0xFFD84F2A)
private val DarkGreen = Color(0xFF17312D)
private val Gold = Color(0xFFF3BE4E)
private val Panel = Color(0xFFFFFDF7)
private val PanelBorder = Color(0xFFE2D8C4)
private val Field = Color(0xFFF8F0DD)
private val Muted = Color(0xFF557068)

@Composable
fun MemoraScreen(
    viewModel: MemoraViewModel,
    onTakePhoto: () -> Unit,
    onSelectImage: () -> Unit,
) {
    val scroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .systemBarsPadding()
            .verticalScroll(scroll)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Hero()

        CapturePanel(viewModel, onTakePhoto, onSelectImage)
        UnderstandPanel(viewModel)
        ConfirmPanel(viewModel)
        RemindPanel(viewModel)
        if (viewModel.completedMemories.isNotEmpty()) {
            CompletedPanel(viewModel)
        }
    }
}

@Composable
private fun Hero() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Hero)
            .padding(24.dp),
    ) {
        Text("MEMORA", color = Gold, fontSize = 13.sp, fontWeight = FontWeight.Black)
        Text(
            "Capture it. Understand it. Act on it.",
            color = Color(0xFFFFF8E8),
            fontSize = 30.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(top = 12.dp),
        )
        Text(
            "Turn real-world labels, receipts, renewals, and dates into confirmed memories with timely reminders.",
            color = Color(0xFFD8E8DE),
            fontSize = 15.sp,
            modifier = Modifier.padding(top = 14.dp),
        )
    }
}

@Composable
private fun Panel(title: String, step: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Panel)
            .border(1.dp, PanelBorder, RoundedCornerShape(18.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(title, color = Color(0xFF16201D), fontSize = 20.sp, fontWeight = FontWeight.Black)
            Text(step, color = Color(0xFF36564F), fontSize = 12.sp, fontWeight = FontWeight.Black)
        }
        content()
    }
}

@Composable
private fun CapturePanel(
    viewModel: MemoraViewModel,
    onTakePhoto: () -> Unit,
    onSelectImage: () -> Unit,
) {
    Panel("Capture", "1/4") {
        val bitmap = viewModel.imageBitmap
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
                    .clip(RoundedCornerShape(14.dp)),
                contentScale = ContentScale.Crop,
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFEAF3EE))
                    .padding(18.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("No image captured", color = Color(0xFF17312D), fontSize = 17.sp, fontWeight = FontWeight.Black)
                Text(
                    "Use the camera or choose a product label, bill, or document.",
                    color = Muted,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PrimaryButton("Take Photo", Modifier.weight(1f), onTakePhoto)
            PrimaryButton("Select Image", Modifier.weight(1f), onSelectImage, background = DarkGreen)
        }
    }
}

@Composable
private fun UnderstandPanel(viewModel: MemoraViewModel) {
    Panel("Understand", "2/4") {
        FieldLabel("Recognized text")
        OutlinedTextField(
            value = viewModel.sourceText,
            onValueChange = viewModel::onSourceTextChanged,
            modifier = Modifier.fillMaxWidth(),
            minLines = 4,
        )
        PrimaryButton("Analyze Memory", Modifier.fillMaxWidth()) { viewModel.analyze() }
    }
}

@Composable
private fun ConfirmPanel(viewModel: MemoraViewModel) {
    val candidate = viewModel.candidate
    Panel("Confirm", "3/4") {
        FieldLabel("Title")
        OutlinedTextField(
            value = candidate.title,
            onValueChange = viewModel::updateTitle,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        FieldLabel("Event")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            EventType.entries.forEach { type ->
                val selected = candidate.eventType == type
                Text(
                    text = type.label(),
                    color = if (selected) Color(0xFFFFFDF7) else Color(0xFF40504A),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (selected) DarkGreen else Color(0xFFECE3D0))
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                        .clickableNoRipple { viewModel.updateEventType(type) },
                )
            }
        }

        FieldLabel("Action date (YYYY-MM-DD)")
        OutlinedTextField(
            value = candidate.dateIso,
            onValueChange = viewModel::updateDate,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = viewModel.dateError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
        if (viewModel.dateError) {
            Text("Use YYYY-MM-DD so Memora can remind you on time.", color = Accent, fontSize = 12.sp)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFEAF3EE))
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("Confidence: ${candidate.confidence.label()}", color = Color(0xFF17312D), fontSize = 13.sp, fontWeight = FontWeight.Black)
            Text("Status: ${viewModel.candidateStatus().label()}", color = Color(0xFF17312D), fontSize = 13.sp, fontWeight = FontWeight.Black)
        }

        PrimaryButton("Confirm Memory", Modifier.fillMaxWidth()) { viewModel.confirm() }
    }
}

@Composable
private fun RemindPanel(viewModel: MemoraViewModel) {
    Panel("Remind & Act", "4/4") {
        val active = viewModel.activeMemories
        if (active.isEmpty()) {
            Text("Confirmed memories will appear here when they need attention.", color = Muted, fontSize = 15.sp)
        } else {
            val (overdue, dueToday, upcoming) = viewModel.summary()
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                SummaryTile("Overdue", overdue, Modifier.weight(1f))
                SummaryTile("Due Today", dueToday, Modifier.weight(1f))
                SummaryTile("Upcoming", upcoming, Modifier.weight(1f))
            }
            active.forEach { memory ->
                val status = MemoryStatusResolver.statusFor(memory)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Field)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(memory.title, color = Color(0xFF15201D), fontSize = 17.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                        StatusBadge(status)
                    }
                    Text(
                        "${memory.eventType.label()} on ${MemoryStatusResolver.formatDate(memory.dateIso)}",
                        color = Color(0xFF40504A),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    PrimaryButton("Mark Complete", background = DarkGreen) { viewModel.complete(memory.id) }
                }
            }
        }
    }
}

@Composable
private fun CompletedPanel(viewModel: MemoraViewModel) {
    Panel("Completed", viewModel.completedMemories.size.toString()) {
        viewModel.completedMemories.forEach { memory ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFEEF4EA))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    memory.title,
                    color = Color(0xFF3C4A44),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    textDecoration = TextDecoration.LineThrough,
                )
                Text(
                    "${memory.eventType.label()} on ${MemoryStatusResolver.formatDate(memory.dateIso)}",
                    color = Color(0xFF40504A),
                    fontSize = 14.sp,
                )
                Text(
                    "Delete",
                    color = Color(0xFFB23D28),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.clickableNoRipple { viewModel.delete(memory.id) },
                )
            }
        }
    }
}

@Composable
private fun SummaryTile(label: String, count: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFEAF3EE))
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(count.toString(), color = Color(0xFF15201D), fontSize = 24.sp, fontWeight = FontWeight.Black)
        Text(label, color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun StatusBadge(status: MemoryStatus) {
    val color = when (status) {
        MemoryStatus.UPCOMING -> Color(0xFFD9E9F5)
        MemoryStatus.DUE_TODAY -> Gold
        MemoryStatus.OVERDUE -> Color(0xFFF2B7A5)
        MemoryStatus.COMPLETED -> Color(0xFFCDE5C8)
    }
    Text(
        status.label(),
        color = Color(0xFF15201D),
        fontSize = 11.sp,
        fontWeight = FontWeight.Black,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color)
            .padding(horizontal = 10.dp, vertical = 6.dp),
    )
}

@Composable
private fun FieldLabel(text: String) {
    Text(text, color = Color(0xFF4F5C57), fontSize = 12.sp, fontWeight = FontWeight.Black)
}

@Composable
private fun PrimaryButton(
    text: String,
    modifier: Modifier = Modifier,
    background: Color = Accent,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = background),
    ) {
        Text(text, color = Color(0xFFFFFDF7), fontSize = 14.sp, fontWeight = FontWeight.Black)
    }
}

private fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier =
    this.clickable(onClick = onClick)
