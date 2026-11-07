package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DiaryEntry
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.TealSecondary

@Composable
fun HistoryListScreen(
    entries: List<DiaryEntry>,
    onSelectToEdit: (String) -> Unit,
    onDeleteEntry: (DiaryEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var entryToDelete by remember { mutableStateOf<DiaryEntry?>(null) }

    val filtered = remember(entries, searchQuery) {
        if (searchQuery.isBlank()) entries else {
            val q = searchQuery.trim().lowercase()
            entries.filter {
                it.date.contains(q) ||
                        it.breakfastMeal.lowercase().contains(q) ||
                        it.lunchMeal.lowercase().contains(q) ||
                        it.dinnerMeal.lowercase().contains(q) ||
                        it.breakfast11AmNote.lowercase().contains(q) ||
                        it.lunch5PmNote.lowercase().contains(q)
            }
        }
    }

    if (entryToDelete != null) {
        val entry = entryToDelete!!
        AlertDialog(
            onDismissRequest = { entryToDelete = null },
            title = { Text("Delete Entry for ${entry.date}?") },
            text = { Text("Are you sure you want to delete this diary log? It will also be removed from the cloud database.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteEntry(entry)
                        entryToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { entryToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Search bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 2.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by date, meal, notes (e.g. अंडा, दाल)...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = BluePrimary)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("history_search_input")
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Showing ${filtered.size} of ${entries.size} logs",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (searchQuery.isNotBlank()) "No records match '$searchQuery'" else "No diary logs yet.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filtered, key = { it.id }) { item ->
                    DiaryCardItem(
                        entry = item,
                        onEdit = { onSelectToEdit(item.date) },
                        onDelete = { entryToDelete = item }
                    )
                }
            }
        }
    }
}

@Composable
fun DiaryCardItem(
    entry: DiaryEntry,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Date & Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(BluePrimary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = BluePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = entry.date,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Total Insulin: ${entry.totalInsulinUnits}U",
                            fontSize = 11.sp,
                            color = TealSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Entry", tint = BluePrimary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Entry", tint = Color.Gray)
                    }
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick readings badge row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (entry.fastingSugar.isNotBlank()) {
                    Column {
                        Text("Fasting", fontSize = 10.sp, color = Color.Gray)
                        SugarBadge(entry.fastingSugar)
                    }
                }
                if (entry.lunchPreSugar.isNotBlank()) {
                    Column {
                        Text("Lunch", fontSize = 10.sp, color = Color.Gray)
                        SugarBadge(entry.lunchPreSugar)
                    }
                }
                if (entry.dinnerPreSugar.isNotBlank()) {
                    Column {
                        Text("Dinner", fontSize = 10.sp, color = Color.Gray)
                        SugarBadge(entry.dinnerPreSugar)
                    }
                }
                if (entry.nightInsulinUnits.isNotBlank()) {
                    Column {
                        Text("Night", fontSize = 10.sp, color = Color.Gray)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFEDE7F6))
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${entry.nightInsulinType} ${entry.nightInsulinUnits}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF512DA8)
                            )
                        }
                    }
                }

                if (entry.dinnerMilk.equals("Yes", ignoreCase = true)) {
                    Spacer(modifier = Modifier.weight(1f))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFE8F5E9))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.LocalDrink, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("Milk", fontSize = 10.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Expanded Details Section
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 14.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    Spacer(modifier = Modifier.height(10.dp))

                    DetailItem(
                        label = "Breakfast (नाश्ता)",
                        meal = entry.breakfastMeal,
                        sugar = entry.fastingSugar,
                        postSugar = entry.breakfastPostSugar,
                        insulin = "${entry.breakfastInsulinType} ${entry.breakfastInsulinUnits}",
                        note = entry.breakfast11AmNote
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    DetailItem(
                        label = "Lunch (दोपहर)",
                        meal = entry.lunchMeal,
                        sugar = entry.lunchPreSugar,
                        postSugar = entry.lunchPostSugar,
                        insulin = "${entry.lunchInsulinType} ${entry.lunchInsulinUnits}",
                        note = entry.lunch5PmNote
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    DetailItem(
                        label = "Dinner (रात)",
                        meal = entry.dinnerMeal,
                        sugar = entry.dinnerPreSugar,
                        postSugar = entry.dinnerPostSugar,
                        insulin = "${entry.dinnerInsulinType} ${entry.dinnerInsulinUnits}",
                        note = "Milk: ${entry.dinnerMilk}"
                    )

                    if (entry.nightInsulinUnits.isNotBlank() || entry.generalNotes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Night / 10:00 PM: ${entry.nightInsulinType} ${entry.nightInsulinUnits} • Note: ${entry.generalNotes}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DetailItem(
    label: String,
    meal: String,
    sugar: String,
    postSugar: String,
    insulin: String,
    note: String
) {
    Column {
        Text(text = label, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BluePrimary)
        if (meal.isNotBlank()) {
            Text(text = "Food: $meal", fontSize = 12.sp)
        }
        val extra = listOfNotNull(
            if (sugar.isNotBlank()) "Pre: $sugar" else null,
            if (postSugar.isNotBlank()) "2H Post: $postSugar" else null,
            if (insulin.trim().isNotEmpty() && insulin != "None") "Insulin: $insulin" else null,
            if (note.isNotBlank()) "Note: $note" else null
        ).joinToString(" • ")

        if (extra.isNotBlank()) {
            Text(text = extra, fontSize = 11.sp, color = Color.Gray)
        }
    }
}
