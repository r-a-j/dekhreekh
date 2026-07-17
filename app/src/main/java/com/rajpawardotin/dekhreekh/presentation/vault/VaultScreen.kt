package com.rajpawardotin.dekhreekh.presentation.vault

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rajpawardotin.dekhreekh.domain.models.WorkoutSession
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultScreen(
    uiState: VaultState,
    sortOrder: SortOrder,
    activeTagFilter: Set<String>,
    hideLowActivity: Boolean,
    tagUsageCounts: Map<String, Int>,
    onBackClick: () -> Unit,
    onSessionClick: (String) -> Unit = {},
    onImportClick: () -> Unit = {},
    onExportClick: (String, String?) -> Unit = { _, _ -> },
    onSortChange: (SortOrder) -> Unit = {},
    onTagToggle: (String) -> Unit = {},
    onClearTagFilter: () -> Unit = {},
    onHideLowActivityToggle: (Boolean) -> Unit = {},
    onRenameSession: (String, String, List<String>) -> Unit = { _, _, _ -> },
    onDeleteSession: (String) -> Unit = {},
    onRenameTagGlobally: (String, String) -> Unit = { _, _ -> },
    onDeleteTagGlobally: (String) -> Unit = {}
) {
    val darkBg = Color(0xFF0D0D14)
    val glassBg = Color(0xEA12121E)
    val voltGreen = Color(0xFFD4FF00)
    val dimText = Color(0xFF6B6B80)

    // Action bottom sheet state
    var sheetSession by remember { mutableStateOf<WorkoutSession?>(null) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showTagEditor by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showTagManager by remember { mutableStateOf(false) }
    var tagToRename by remember { mutableStateOf<String?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Derive allTags from state
    val allTags = (uiState as? VaultState.HistoryLoaded)?.allTags ?: emptySet()

    Scaffold(
        containerColor = darkBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Vault",
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // Sort button
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.Default.Sort, contentDescription = "Sort", tint = voltGreen)
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false },
                            containerColor = Color(0xFF1A1A2E)
                        ) {
                            SortOrder.entries.forEach { order ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            order.label,
                                            color = if (sortOrder == order) voltGreen else Color.White,
                                            fontWeight = if (sortOrder == order) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = { onSortChange(order); showSortMenu = false },
                                    leadingIcon = {
                                        if (sortOrder == order) Icon(Icons.Default.Check, null, tint = voltGreen)
                                    }
                                )
                            }
                        }
                    }
                    // Low-activity filter toggle
                    IconButton(onClick = { onHideLowActivityToggle(!hideLowActivity) }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Hide low activity",
                            tint = if (hideLowActivity) voltGreen else dimText
                        )
                    }
                    // Manage tags button
                    IconButton(onClick = { showTagManager = true }) {
                        Icon(Icons.Default.Label, contentDescription = "Manage Tags", tint = Color.White)
                    }
                    // Import button
                    IconButton(
                        onClick = onImportClick,
                        modifier = Modifier.testTag("ImportButton")
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = "Import GPX", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = darkBg,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(darkBg)
        ) {

            // ── Tag filter chip row ──────────────────────────────────────────
            if (allTags.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (activeTagFilter.isNotEmpty()) {
                        item {
                            FilterChip(
                                selected = false,
                                onClick = onClearTagFilter,
                                label = { Text("Clear", fontSize = 11.sp) },
                                leadingIcon = { Icon(Icons.Default.Close, null, modifier = Modifier.size(14.dp)) },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = Color(0x33FF4C4C),
                                    labelColor = Color(0xFFFF6B6B)
                                )
                            )
                        }
                    }
                    items(allTags.toList().sorted()) { tag ->
                        val active = tag in activeTagFilter
                        FilterChip(
                            selected = active,
                            onClick = { onTagToggle(tag) },
                            label = { Text("#$tag", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0x33D4FF00),
                                selectedLabelColor = voltGreen,
                                containerColor = Color(0x1AFFFFFF),
                                labelColor = dimText
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = active,
                                selectedBorderColor = voltGreen,
                                borderColor = Color(0x26FFFFFF)
                            )
                        )
                    }
                }
            }

            // ── Session list ─────────────────────────────────────────────────
            when (uiState) {
                VaultState.Empty -> EmptyVaultContent(voltGreen = voltGreen, dimText = dimText)
                is VaultState.HistoryLoaded -> {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.testTag("VaultSessionList")
                    ) {
                        items(uiState.items, key = { item ->
                            when (item) {
                                is VaultListItem.Header -> "header_${item.dateLabel}"
                                is VaultListItem.SessionItem -> item.session.id
                            }
                        }) { item ->
                            when (item) {
                                is VaultListItem.Header -> DateHeaderRow(item.dateLabel, dimText)
                                is VaultListItem.SessionItem -> SessionCard(
                                    session = item.session,
                                    voltGreen = voltGreen,
                                    glassBg = glassBg,
                                    dimText = dimText,
                                    onClick = { onSessionClick(item.session.id) },
                                    onLongPress = { sheetSession = item.session }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // ── Session Action Bottom Sheet ──────────────────────────────────────────
    sheetSession?.let { session ->
        ModalBottomSheet(
            onDismissRequest = { sheetSession = null },
            sheetState = sheetState,
            containerColor = Color(0xFF13131F),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .width(40.dp)
                        .height(4.dp)
                        .background(Color(0x33FFFFFF), RoundedCornerShape(2.dp))
                )
            }
        ) {
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                Text(
                    text = session.name ?: "Session",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = Color.White
                )
                Text(
                    text = formatTimestamp(session.startTime),
                    fontSize = 12.sp,
                    color = Color(0xFF6B6B80),
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                // Tag chips display
                if (session.tags.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        items(session.tags) { tag ->
                            Box(
                                modifier = Modifier
                                    .background(Color(0x1AD4FF00), RoundedCornerShape(20.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("#$tag", color = voltGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Divider(color = Color(0x1AFFFFFF), modifier = Modifier.padding(bottom = 8.dp))

                SheetAction(Icons.Default.DriveFileRenameOutline, "Rename", Color.White) {
                    showRenameDialog = true
                }
                SheetAction(Icons.Default.Tag, "Edit Tags", Color(0xFFB06CFF)) {
                    showTagEditor = true
                }
                SheetAction(Icons.Default.FileUpload, "Export GPX", voltGreen) {
                    onExportClick(session.id, session.name)
                    sheetSession = null
                }
                SheetAction(Icons.Default.DeleteOutline, "Delete", Color(0xFFFF4C4C)) {
                    onDeleteSession(session.id)
                    sheetSession = null
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // ── Rename Dialog ────────────────────────────────────────────────────────
    if (showRenameDialog) {
        val session = sheetSession ?: return
        var nameInput by remember { mutableStateOf(session.name ?: "") }
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            containerColor = Color(0xFF13131F),
            title = { Text("Rename Session", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Session Name") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = voltGreen,
                        focusedLabelColor = voltGreen,
                        cursorColor = voltGreen,
                        unfocusedBorderColor = Color(0x33FFFFFF),
                        unfocusedLabelColor = Color(0xFF6B6B80),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("RenameTextField")
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onRenameSession(session.id, nameInput, session.tags)
                    showRenameDialog = false
                    sheetSession = null
                }) {
                    Text("Save", color = voltGreen, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel", color = Color(0xFF6B6B80))
                }
            }
        )
    }

    // ── Tag Editor Dialog ────────────────────────────────────────────────────
    if (showTagEditor) {
        val session = sheetSession ?: return
        var tagInput by remember { mutableStateOf("") }
        val currentTags = remember { mutableStateListOf(*session.tags.toTypedArray()) }

        AlertDialog(
            onDismissRequest = { showTagEditor = false },
            containerColor = Color(0xFF13131F),
            title = { Text("Edit Tags", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    // Current tags
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        currentTags.forEach { tag ->
                            InputChip(
                                selected = false,
                                onClick = { currentTags.remove(tag) },
                                label = { Text("#$tag", fontSize = 11.sp) },
                                trailingIcon = { Icon(Icons.Default.Close, null, modifier = Modifier.size(14.dp)) },
                                colors = InputChipDefaults.inputChipColors(
                                    containerColor = Color(0x26D4FF00),
                                    labelColor = voltGreen
                                )
                            )
                        }
                    }
                    // Add new tag
                    OutlinedTextField(
                        value = tagInput,
                        onValueChange = { tagInput = it.replace(",", "").replace(" ", "").lowercase() },
                        label = { Text("Add tag (press Enter)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            val trimmed = tagInput.trim().lowercase()
                            if (trimmed.isNotEmpty() && !currentTags.contains(trimmed)) {
                                currentTags.add(trimmed)
                            }
                            tagInput = ""
                        }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = voltGreen,
                            focusedLabelColor = voltGreen,
                            cursorColor = voltGreen,
                            unfocusedBorderColor = Color(0x33FFFFFF),
                            unfocusedLabelColor = Color(0xFF6B6B80),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("TagInputField")
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val trimmed = tagInput.trim().lowercase()
                    if (trimmed.isNotEmpty() && !currentTags.contains(trimmed)) {
                        currentTags.add(trimmed)
                    }
                    onRenameSession(session.id, session.name ?: "", currentTags.toList())
                    showTagEditor = false
                    sheetSession = null
                }) {
                    Text("Save", color = voltGreen, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTagEditor = false }) {
                    Text("Cancel", color = Color(0xFF6B6B80))
                }
            }
        )
    }

    // ── Global Tag Manager Dialog ────────────────────────────────────────────
    if (showTagManager) {
        AlertDialog(
            onDismissRequest = { showTagManager = false },
            containerColor = Color(0xFF13131F),
            title = {
                Text(
                    "Manage Global Tags",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                if (tagUsageCounts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No active tags found", color = dimText, fontSize = 14.sp)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                    ) {
                        items(tagUsageCounts.toList().sortedByDescending { it.second }) { (tag, count) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0x0DFFFFFF), RoundedCornerShape(8.dp))
                                    .border(BorderStroke(1.dp, Color(0x1AFFFFFF)), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val tagColor = getTagColor(tag)
                                Text(
                                    text = "#$tag",
                                    color = tagColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "$count ${if (count == 1) "run" else "runs"}",
                                    color = dimText,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                                IconButton(
                                    onClick = { tagToRename = tag },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Rename Tag",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onDeleteTagGlobally(tag) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Tag",
                                        tint = Color(0xFFFF4C4C),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTagManager = false }) {
                    Text("Close", color = voltGreen, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // ── Global Tag Rename Dialog ─────────────────────────────────────────────
    tagToRename?.let { oldTag ->
        var newTagName by remember { mutableStateOf(oldTag) }
        AlertDialog(
            onDismissRequest = { tagToRename = null },
            containerColor = Color(0xFF13131F),
            title = { Text("Rename Tag Globally", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newTagName,
                    onValueChange = { newTagName = it.replace(",", "").replace(" ", "").lowercase() },
                    label = { Text("New Tag Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = voltGreen,
                        focusedLabelColor = voltGreen,
                        cursorColor = voltGreen,
                        unfocusedBorderColor = Color(0x33FFFFFF),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        unfocusedLabelColor = Color(0xFF6B6B80)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val trimmed = newTagName.trim().lowercase()
                    if (trimmed.isNotEmpty() && trimmed != oldTag) {
                        onRenameTagGlobally(oldTag, trimmed)
                    }
                    tagToRename = null
                }) {
                    Text("Rename", color = voltGreen, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { tagToRename = null }) {
                    Text("Cancel", color = Color(0xFF6B6B80))
                }
            }
        )
    }
}

// ── Sub-composables ───────────────────────────────────────────────────────────

@Composable
private fun DateHeaderRow(label: String, dimText: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label.uppercase(),
            color = dimText,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.5.sp
        )
        Spacer(modifier = Modifier.width(8.dp))
        HorizontalDivider(color = Color(0x1AFFFFFF), modifier = Modifier.weight(1f))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SessionCard(
    session: WorkoutSession,
    voltGreen: Color,
    glassBg: Color,
    dimText: Color,
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {
    val distKm = String.format("%.2f", session.totalDistanceMeters / 1000f)
    val durationMin = session.totalDurationSeconds / 60
    val durationStr = "${durationMin / 60}h ${durationMin % 60}m".let {
        if (durationMin < 60) "${durationMin}m" else it
    }
    val timeStr = Instant.ofEpochMilli(session.startTime)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("hh:mm a"))

    Card(
        colors = CardDefaults.cardColors(containerColor = glassBg),
        border = BorderStroke(
            1.dp,
            if (session.isLowActivity) Color(0x33FF9800) else Color(0x1AFFFFFF)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("SessionCard_${session.id}")
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongPress
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = session.name ?: session.activityType,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        if (session.isLowActivity) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .background(Color(0x33FF9800), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "⚠ <5m",
                                    color = Color(0xFFFF9800),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp,
                                    modifier = Modifier.testTag("LowActivityBadge_${session.id}")
                                )
                            }
                        }
                    }
                    Text(text = timeStr, color = dimText, fontSize = 11.sp)
                }
                Text(
                    text = "$distKm km",
                    color = voltGreen,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )
            }

            if (session.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    items(session.tags) { tag ->
                        val tagColor = getTagColor(tag)
                        Text(
                            text = "#$tag",
                            color = tagColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .background(tagColor.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                MiniStat(label = "DURATION", value = durationStr, color = dimText)
                if (session.averagePace > 0) {
                    val paceMin = session.averagePace / 60
                    val paceSec = session.averagePace % 60
                    MiniStat(label = "PACE", value = "$paceMin:${paceSec.toString().padStart(2, '0')}/km", color = dimText)
                }
            }
        }
    }
}

@Composable
private fun MiniStat(label: String, value: String, color: Color) {
    Column {
        Text(text = label, color = color, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
        Text(text = value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SheetAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = label, color = tint, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun EmptyVaultContent(voltGreen: Color, dimText: Color) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Inbox,
                contentDescription = null,
                tint = dimText,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("No sessions yet", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Start a run or import a GPX file", color = dimText, fontSize = 13.sp)
        }
    }
}

private fun formatTimestamp(epochMs: Long): String {
    return Instant.ofEpochMilli(epochMs)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("EEE, MMM dd yyyy • hh:mm a"))
}

private fun getTagColor(tag: String): Color {
    val hash = tag.hashCode()
    val colors = listOf(
        Color(0xFFB06CFF), // Purple
        Color(0xFF00E5FF), // Cyan
        Color(0xFFFF4081), // Pink
        Color(0xFFFF9100), // Orange
        Color(0xFF00E676), // Green
        Color(0xFF29B6F6), // Blue
        Color(0xFFE040FB), // Magenta
        Color(0xFFFF8A80)  // Coral
    )
    val index = java.lang.Math.abs(hash) % colors.size
    return colors[index]
}
