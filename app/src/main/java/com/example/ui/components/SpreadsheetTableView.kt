package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DiaryEntry
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.SectionBreakfastBg
import com.example.ui.theme.SectionBreakfastText
import com.example.ui.theme.SectionDinnerBg
import com.example.ui.theme.SectionDinnerText
import com.example.ui.theme.SectionLunchBg
import com.example.ui.theme.SectionLunchText
import com.example.ui.theme.TealSecondary

// Column widths for smooth scrolling
private val colDateWidth = 90.dp
private val colSugarWidth = 75.dp
private val colInsulinWidth = 85.dp
private val colMealWidth = 140.dp
private val colNoteWidth = 130.dp
private val colMilkWidth = 65.dp

@Composable
fun SpreadsheetTableView(
    entries: List<DiaryEntry>,
    onSelectRowToEdit: (String) -> Unit,
    onImportStarter: () -> Unit,
    modifier: Modifier = Modifier
) {
    val horizontalScrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Info & Quick Actions Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 2.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.TableChart,
                    contentDescription = null,
                    tint = BluePrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "दैनिक ब्लड शुगर एवं आहार डायरी",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = BluePrimary
                    )
                    Text(
                        text = "${entries.size} Records Logged • Tap any row to edit",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (entries.isEmpty()) {
                    Button(
                        onClick = onImportStarter,
                        colors = ButtonDefaults.buttonColors(containerColor = TealSecondary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                        Text("Import Sheet", fontSize = 11.sp)
                    }
                }
            }
        }

        if (entries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No records yet in this account",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "You can add entries via the 'Daily Entry' tab or load your authentic spreadsheet records.",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onImportStarter,
                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                        ) {
                            Text("Load My 35 Spreadsheet Records")
                        }
                    }
                }
            }
            return
        }

        // Two-way scrollable spreadsheet container
        Box(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(horizontalScrollState)
                .testTag("spreadsheet_scroll_container")
        ) {
            Column {
                // Tier 1 Section Headers (Matching Excel Sheet Colors)
                Row(
                    modifier = Modifier.height(34.dp)
                ) {
                    HeaderCell(
                        text = "Date (दिनांक)",
                        width = colDateWidth,
                        bg = Color(0xFFECEFF1),
                        textColor = Color.DarkGray
                    )

                    // Breakfast Header Group (Light Blue)
                    HeaderCell(
                        text = "Breakfast / Fasting (नाश्ता)",
                        width = colSugarWidth + colInsulinWidth + colMealWidth + colSugarWidth + colNoteWidth,
                        bg = SectionBreakfastBg,
                        textColor = SectionBreakfastText
                    )

                    // Lunch Header Group (Light Lavender)
                    HeaderCell(
                        text = "Lunch (दोपहर का खाना)",
                        width = colSugarWidth + colInsulinWidth + colMealWidth + colSugarWidth + colNoteWidth,
                        bg = SectionLunchBg,
                        textColor = SectionLunchText
                    )

                    // Dinner Header Group (Light Pink)
                    HeaderCell(
                        text = "Dinner (रात का खाना)",
                        width = colSugarWidth + colInsulinWidth + colMealWidth + colSugarWidth + colInsulinWidth + colMilkWidth,
                        bg = SectionDinnerBg,
                        textColor = SectionDinnerText
                    )
                }

                // Tier 2 Column Names
                Row(
                    modifier = Modifier.height(38.dp)
                ) {
                    SubHeaderCell("Date", colDateWidth, Color(0xFFCFD8DC))

                    // Breakfast subheaders
                    SubHeaderCell("Sugar", colSugarWidth, SectionBreakfastBg)
                    SubHeaderCell("novorapid", colInsulinWidth, SectionBreakfastBg, isHighlight = true)
                    SubHeaderCell("Nashta", colMealWidth, SectionBreakfastBg)
                    SubHeaderCell("2H Sugar", colSugarWidth, SectionBreakfastBg)
                    SubHeaderCell("11 Am", colNoteWidth, SectionBreakfastBg)

                    // Lunch subheaders
                    SubHeaderCell("Sugar", colSugarWidth, SectionLunchBg)
                    SubHeaderCell("novorapid", colInsulinWidth, SectionLunchBg, isHighlight = true)
                    SubHeaderCell("Lunch", colMealWidth, SectionLunchBg)
                    SubHeaderCell("2H sugar", colSugarWidth, SectionLunchBg)
                    SubHeaderCell("5pm / Note", colNoteWidth, SectionLunchBg)

                    // Dinner subheaders
                    SubHeaderCell("Sugar", colSugarWidth, SectionDinnerBg)
                    SubHeaderCell("novorapid", colInsulinWidth, SectionDinnerBg, isHighlight = true)
                    SubHeaderCell("Dinner", colMealWidth, SectionDinnerBg)
                    SubHeaderCell("2H sugar", colSugarWidth, SectionDinnerBg)
                    SubHeaderCell("tresiba", colInsulinWidth, SectionDinnerBg, isHighlight = true)
                    SubHeaderCell("milk", colMilkWidth, SectionDinnerBg)
                }

                // Table Rows
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    itemsIndexed(entries, key = { _, item -> item.id }) { index, entry ->
                        val rowBg = if (index % 2 == 0) Color.White else Color(0xFFF9FAFB)
                        SpreadsheetRow(
                            entry = entry,
                            rowBg = rowBg,
                            onRowClick = { onSelectRowToEdit(entry.date) }
                        )
                        HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 0.8.dp)
                    }
                }
            }
        }
    }
}

@Composable
fun SpreadsheetRow(
    entry: DiaryEntry,
    rowBg: Color,
    onRowClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .background(rowBg)
            .clickable { onRowClick() }
            .height(44.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Date (Sticky highlight style)
        DataCell(
            text = entry.date,
            width = colDateWidth,
            isBold = true,
            bg = Color(0xFFECEFF1).copy(alpha = 0.7f),
            textColor = Color(0xFF263238)
        )

        // Breakfast
        SugarDataCell(entry.fastingSugar, colSugarWidth)
        InsulinDataCell(entry.breakfastInsulinUnits, colInsulinWidth, SectionBreakfastText)
        DataCell(entry.breakfastMeal, colMealWidth)
        SugarDataCell(entry.breakfastPostSugar, colSugarWidth)
        DataCell(entry.breakfast11AmNote, colNoteWidth)

        // Lunch
        SugarDataCell(entry.lunchPreSugar, colSugarWidth)
        InsulinDataCell(entry.lunchInsulinUnits, colInsulinWidth, SectionLunchText)
        DataCell(entry.lunchMeal, colMealWidth)
        SugarDataCell(entry.lunchPostSugar, colSugarWidth)
        DataCell(entry.lunch5PmNote, colNoteWidth)

        // Dinner
        SugarDataCell(entry.dinnerPreSugar, colSugarWidth)
        InsulinDataCell(entry.dinnerInsulinUnits, colInsulinWidth, SectionDinnerText)
        DataCell(entry.dinnerMeal, colMealWidth)
        SugarDataCell(entry.dinnerPostSugar, colSugarWidth)
        InsulinDataCell(entry.nightInsulinUnits, colInsulinWidth, TealSecondary)
        MilkDataCell(entry.dinnerMilk, colMilkWidth)
    }
}

@Composable
fun HeaderCell(text: String, width: androidx.compose.ui.unit.Dp, bg: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .width(width)
            .height(34.dp)
            .background(bg)
            .border(0.5.dp, Color(0xFFB0BEC5)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SubHeaderCell(
    text: String,
    width: androidx.compose.ui.unit.Dp,
    bg: Color,
    isHighlight: Boolean = false
) {
    Box(
        modifier = Modifier
            .width(width)
            .height(38.dp)
            .background(bg)
            .border(0.5.dp, Color(0xFFCFD8DC))
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Medium,
            fontSize = 11.sp,
            color = if (isHighlight) Color(0xFFC2185B) else Color(0xFF37474F),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun DataCell(
    text: String,
    width: androidx.compose.ui.unit.Dp,
    isBold: Boolean = false,
    bg: Color = Color.Transparent,
    textColor: Color = Color(0xFF212121)
) {
    Box(
        modifier = Modifier
            .width(width)
            .height(44.dp)
            .background(bg)
            .border(0.5.dp, Color(0xFFE0E0E0))
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = textColor,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun SugarDataCell(sugar: String, width: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .width(width)
            .height(44.dp)
            .border(0.5.dp, Color(0xFFE0E0E0))
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        if (sugar.isNotBlank()) {
            val num = sugar.filter { it.isDigit() }.toIntOrNull()
            val color = when {
                num != null && num < 70 -> Color(0xFF6A1B9A)
                num != null && num <= 140 -> Color(0xFF2E7D32)
                num != null && num <= 199 -> Color(0xFFE65100)
                num != null -> Color(0xFFC62828)
                else -> Color.DarkGray
            }
            Text(
                text = sugar,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun InsulinDataCell(units: String, width: androidx.compose.ui.unit.Dp, accentColor: Color) {
    Box(
        modifier = Modifier
            .width(width)
            .height(44.dp)
            .border(0.5.dp, Color(0xFFE0E0E0))
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        if (units.isNotBlank()) {
            Text(
                text = units,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor
            )
        }
    }
}

@Composable
fun MilkDataCell(milk: String, width: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .width(width)
            .height(44.dp)
            .border(0.5.dp, Color(0xFFE0E0E0)),
        contentAlignment = Alignment.Center
    ) {
        val isYes = milk.equals("Yes", ignoreCase = true)
        Text(
            text = if (milk.isBlank()) "" else milk,
            fontSize = 11.sp,
            fontWeight = if (isYes) FontWeight.Bold else FontWeight.Normal,
            color = if (isYes) Color(0xFF1B5E20) else Color.Gray
        )
    }
}
