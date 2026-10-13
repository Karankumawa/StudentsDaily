package com.example.studentsdaily.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AssignmentLate
import androidx.compose.material.icons.rounded.Class
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.studentsdaily.data.model.TimetableItem
import com.example.studentsdaily.data.model.TimetableType
import com.example.studentsdaily.ui.theme.StudentsDailyTheme
import com.example.studentsdaily.ui.viewmodel.TimetableViewModel

val DAYS_OF_WEEK = listOf("All", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableScreen(
    modifier: Modifier = Modifier,
    viewModel: TimetableViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Class Schedule & Deadlines",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Day-of-week active filter bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DAYS_OF_WEEK.forEach { day ->
                    val isSelected = uiState.selectedDay == day
                    val count = if (day == "All") uiState.timetableItems.size
                    else uiState.timetableItems.count { it.dayOfWeek.equals(day, ignoreCase = true) }

                    val shortDay = when (day) {
                        "Monday" -> "Mon"
                        "Tuesday" -> "Tue"
                        "Wednesday" -> "Wed"
                        "Thursday" -> "Thu"
                        "Friday" -> "Fri"
                        "Saturday" -> "Sat"
                        "Sunday" -> "Sun"
                        else -> "All"
                    }

                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setSelectedDay(day) },
                        label = {
                            Text(
                                text = "$shortDay ($count)",
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (uiState.filteredItems.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (uiState.selectedDay == "All") "No schedule items added yet. Tap + to add one!"
                            else "No classes or deadlines scheduled for ${uiState.selectedDay}. Enjoy your day!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.filteredItems, key = { it.id }) { item ->
                        TimetableItemCard(
                            item = item,
                            onDelete = { viewModel.deleteTimetableItem(item.id) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddBottomSheet = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            Icon(imageVector = Icons.Rounded.Add, contentDescription = "Add Schedule Item")
        }
    }

    if (showAddBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddBottomSheet = false },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            AddTimetableItemBottomSheetContent(
                initialDay = if (uiState.selectedDay != "All") uiState.selectedDay else "Monday",
                onDismiss = { showAddBottomSheet = false },
                onConfirm = { subject, instructor, room, day, startTime, endTime, type ->
                    viewModel.addTimetableItem(subject, instructor, room, day, startTime, endTime, type)
                    showAddBottomSheet = false
                }
            )
        }
    }
}

@Composable
fun TimetableItemCard(
    item: TimetableItem,
    onDelete: () -> Unit
) {
    // Session Type Color Bar (Lecture = Blue, Lab = Teal, Exam = Red, Deadline = Orange)
    val (typeColor, typeIcon) = when (item.type) {
        TimetableType.LECTURE -> Color(0xFF1E88E5) to Icons.Rounded.Class
        TimetableType.LAB -> Color(0xFF00897B) to Icons.Rounded.Code
        TimetableType.EXAM -> Color(0xFFE53935) to Icons.Rounded.Event
        TimetableType.DEADLINE -> Color(0xFFFB8C00) to Icons.Rounded.AssignmentLate
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Vertical Session-Type Colored Indicator Bar
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(64.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(typeColor)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Left Time Indicator Column
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(68.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = item.startTime,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "to",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Text(
                            text = item.endTime,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Main Details
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = item.subject,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = typeColor.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = typeIcon,
                                contentDescription = null,
                                tint = typeColor,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = item.type.displayName,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = typeColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Room Location Badge with Map Pin Icon & Instructor Details
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (item.room.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.LocationOn,
                                    contentDescription = "Room Location Pin",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = item.room,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (item.instructor.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Person,
                                contentDescription = "Instructor",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = item.instructor,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = "Delete Item",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTimetableItemBottomSheetContent(
    initialDay: String,
    onDismiss: () -> Unit,
    onConfirm: (subject: String, instructor: String, room: String, day: String, startTime: String, endTime: String, type: TimetableType) -> Unit
) {
    var subject by remember { mutableStateOf("") }
    var instructor by remember { mutableStateOf("") }
    var room by remember { mutableStateOf("") }
    var dayOfWeek by remember { mutableStateOf(if (initialDay != "All") initialDay else "Monday") }
    var startTime by remember { mutableStateOf("09:00") }
    var endTime by remember { mutableStateOf("10:30") }
    var selectedType by remember { mutableStateOf(TimetableType.LECTURE) }

    var expandedDayMenu by remember { mutableStateOf(false) }
    var expandedTypeMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .padding(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Add Schedule Item",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        OutlinedTextField(
            value = subject,
            onValueChange = { subject = it },
            label = { Text("Subject / Course Title") },
            leadingIcon = { Icon(imageVector = Icons.Rounded.Class, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = instructor,
            onValueChange = { instructor = it },
            label = { Text("Instructor / Professor") },
            leadingIcon = { Icon(imageVector = Icons.Rounded.Person, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = room,
            onValueChange = { room = it },
            label = { Text("Room / Location (e.g. Hall 301)") },
            leadingIcon = { Icon(imageVector = Icons.Rounded.LocationOn, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        ExposedDropdownMenuBox(
            expanded = expandedDayMenu,
            onExpandedChange = { expandedDayMenu = !expandedDayMenu }
        ) {
            OutlinedTextField(
                value = dayOfWeek,
                onValueChange = {},
                readOnly = true,
                label = { Text("Day of Week") },
                leadingIcon = { Icon(imageVector = Icons.Rounded.Event, contentDescription = null) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDayMenu) },
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expandedDayMenu,
                onDismissRequest = { expandedDayMenu = false }
            ) {
                DAYS_OF_WEEK.filter { it != "All" }.forEach { day ->
                    DropdownMenuItem(
                        text = { Text(day) },
                        onClick = {
                            dayOfWeek = day
                            expandedDayMenu = false
                        }
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = startTime,
                onValueChange = { startTime = it },
                label = { Text("Start Time") },
                leadingIcon = { Icon(imageVector = Icons.Rounded.Schedule, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = endTime,
                onValueChange = { endTime = it },
                label = { Text("End Time") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }

        ExposedDropdownMenuBox(
            expanded = expandedTypeMenu,
            onExpandedChange = { expandedTypeMenu = !expandedTypeMenu }
        ) {
            OutlinedTextField(
                value = selectedType.displayName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Event Type") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTypeMenu) },
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expandedTypeMenu,
                onDismissRequest = { expandedTypeMenu = false }
            ) {
                TimetableType.entries.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type.displayName) },
                        onClick = {
                            selectedType = type
                            expandedTypeMenu = false
                        }
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                onClick = {
                    if (subject.isNotBlank()) {
                        onConfirm(subject, instructor, room, dayOfWeek, startTime, endTime, selectedType)
                    }
                }
            ) {
                Text(
                    text = "Save Schedule",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TimetableScreenPreview() {
    StudentsDailyTheme {
        TimetableScreen()
    }
}
