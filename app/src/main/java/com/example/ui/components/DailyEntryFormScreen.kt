package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.SectionBreakfastBg
import com.example.ui.theme.SectionBreakfastBorder
import com.example.ui.theme.SectionBreakfastText
import com.example.ui.theme.SectionDinnerBg
import com.example.ui.theme.SectionDinnerBorder
import com.example.ui.theme.SectionDinnerText
import com.example.ui.theme.SectionLunchBg
import com.example.ui.theme.SectionLunchBorder
import com.example.ui.theme.SectionLunchText
import com.example.ui.theme.SectionNightBg
import com.example.ui.theme.SectionNightBorder
import com.example.ui.theme.SectionNightText
import com.example.ui.theme.TealSecondary
import com.example.ui.viewmodel.FormState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val BolusInsulinOptions = listOf("Novorapid", "Regular", "Humalog", "Fiasp", "Apidra", "None")
private val BasalInsulinOptions = listOf("Tresiba", "Levemir", "Lantus", "Toujeo", "None")

@Composable
fun DailyEntryFormScreen(
    formState: FormState,
    onFieldChange: (FormState.() -> FormState) -> Unit,
    onSaveEntry: () -> Unit,
    onSelectDate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showNightTimePicker by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    // Helper to advance or retreat day
    fun shiftDate(days: Int) {
        val sdf = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
        try {
            val parsed = sdf.parse(formState.date) ?: Date()
            val cal = Calendar.getInstance().apply {
                time = parsed
                add(Calendar.DAY_OF_YEAR, days)
            }
            onSelectDate(sdf.format(cal.time))
        } catch (e: Exception) {
            onSelectDate(sdf.format(Date()))
        }
    }

    if (showDatePicker) {
        GlucoDatePickerDialog(
            onDateSelected = { selected ->
                onSelectDate(selected)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

    if (showNightTimePicker) {
        GlucoTimePickerDialog(
            initialTime = formState.nightInsulinTime,
            onTimeSelected = { newTime ->
                onFieldChange { copy(nightInsulinTime = newTime) }
                showNightTimePicker = false
            },
            onDismiss = { showNightTimePicker = false }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Sticky Date Bar with Quick Navigation
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 3.dp,
            shadowElevation = 2.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { shiftDate(-1) }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Day")
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                        .clickable { showDatePicker = true }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("date_picker_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Select Date",
                        tint = BluePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Date (दिनांक)",
                            fontSize = 10.sp,
                            color = BluePrimary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = formState.date.ifBlank { "Select Date" },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = BluePrimary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { shiftDate(1) }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next Day")
                    }
                }
            }
        }

        // Scrollable Form Container
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Save status notification if recently saved
            AnimatedVisibility(visible = formState.saveMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(TealSecondary.copy(alpha = 0.15f))
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudDone, contentDescription = null, tint = TealSecondary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = formState.saveMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = TealSecondary
                        )
                    }
                }
            }

            // ==========================================
            // 1. BREAKFAST / FASTING SECTION (नाश्ता)
            // ==========================================
            MealSectionCard(
                title = "Breakfast / Fasting (नाश्ता)",
                subtitle = "Fasting Sugar • Novorapid • Nashta • 2H Sugar • 11 AM Note",
                icon = Icons.Default.WbSunny,
                headerBg = SectionBreakfastBg,
                headerBorder = SectionBreakfastBorder,
                accentColor = SectionBreakfastText
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = formState.fastingSugar,
                        onValueChange = { onFieldChange { copy(fastingSugar = it) } },
                        label = { Text("Fasting Sugar") },
                        placeholder = { Text("e.g. 142mg") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("fasting_sugar_input")
                    )

                    InsulinUnitStepper(
                        label = "Insulin (इंसुलिन)",
                        currentValue = formState.breakfastInsulinUnits,
                        onValueChange = { onFieldChange { copy(breakfastInsulinUnits = it) } },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    InsulinDropdown(
                        label = "Insulin Type",
                        selectedType = formState.breakfastInsulinType,
                        options = BolusInsulinOptions,
                        onSelect = { onFieldChange { copy(breakfastInsulinType = it) } },
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = formState.breakfastPostSugar,
                        onValueChange = { onFieldChange { copy(breakfastPostSugar = it) } },
                        label = { Text("2H Sugar (Post)") },
                        placeholder = { Text("e.g. 74mg") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("breakfast_2h_sugar_input")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = formState.breakfastMeal,
                    onValueChange = { onFieldChange { copy(breakfastMeal = it) } },
                    label = { Text("Nashta Details (नाश्ता)") },
                    placeholder = { Text("e.g. 3 अंडे 2 रोटी अरहर दाल") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("breakfast_meal_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = formState.breakfast11AmNote,
                    onValueChange = { onFieldChange { copy(breakfast11AmNote = it) } },
                    label = { Text("11 AM Note / Snack (११ बजे का नोट)") },
                    placeholder = { Text("e.g. प्रोटीन पा 2 चम्मच / 1 अंडा / 1 सेब") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("breakfast_11am_note_input")
                )
            }

            // ==========================================
            // 2. LUNCH SECTION (दोपहर का खाना)
            // ==========================================
            MealSectionCard(
                title = "Lunch (दोपहर का खाना)",
                subtitle = "Pre-Lunch Sugar • Novorapid • Lunch Details • 2H Sugar • 5 PM Note",
                icon = Icons.Default.Restaurant,
                headerBg = SectionLunchBg,
                headerBorder = SectionLunchBorder,
                accentColor = SectionLunchText
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = formState.lunchPreSugar,
                        onValueChange = { onFieldChange { copy(lunchPreSugar = it) } },
                        label = { Text("Lunch Sugar (Pre)") },
                        placeholder = { Text("e.g. 208mg") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("lunch_pre_sugar_input")
                    )

                    InsulinUnitStepper(
                        label = "Lunch Insulin",
                        currentValue = formState.lunchInsulinUnits,
                        onValueChange = { onFieldChange { copy(lunchInsulinUnits = it) } },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    InsulinDropdown(
                        label = "Insulin Type",
                        selectedType = formState.lunchInsulinType,
                        options = BolusInsulinOptions,
                        onSelect = { onFieldChange { copy(lunchInsulinType = it) } },
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = formState.lunchPostSugar,
                        onValueChange = { onFieldChange { copy(lunchPostSugar = it) } },
                        label = { Text("2H Sugar (Post)") },
                        placeholder = { Text("e.g. 189mg") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("lunch_2h_sugar_input")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = formState.lunchMeal,
                    onValueChange = { onFieldChange { copy(lunchMeal = it) } },
                    label = { Text("Lunch Food Details (दोपहर का भोजन)") },
                    placeholder = { Text("e.g. सब्जी 3 रोटी / 2 रोटी चिकन मछली") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("lunch_meal_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = formState.lunch5PmNote,
                    onValueChange = { onFieldChange { copy(lunch5PmNote = it) } },
                    label = { Text("5 PM / Note (शाम का नोट)") },
                    placeholder = { Text("e.g. प्रोटीन पा 3 अंडे / 1 सेब 2 केला") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("lunch_5pm_note_input")
                )
            }

            // ==========================================
            // 3. DINNER SECTION (रात का खाना)
            // ==========================================
            MealSectionCard(
                title = "Dinner (रात का खाना)",
                subtitle = "Pre-Dinner Sugar • Novorapid • Dinner Details • 2H Sugar • Milk",
                icon = Icons.Default.LocalDining,
                headerBg = SectionDinnerBg,
                headerBorder = SectionDinnerBorder,
                accentColor = SectionDinnerText
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = formState.dinnerPreSugar,
                        onValueChange = { onFieldChange { copy(dinnerPreSugar = it) } },
                        label = { Text("Dinner Sugar (Pre)") },
                        placeholder = { Text("e.g. 232mg") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dinner_pre_sugar_input")
                    )

                    InsulinUnitStepper(
                        label = "Dinner Insulin",
                        currentValue = formState.dinnerInsulinUnits,
                        onValueChange = { onFieldChange { copy(dinnerInsulinUnits = it) } },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    InsulinDropdown(
                        label = "Insulin Type",
                        selectedType = formState.dinnerInsulinType,
                        options = BolusInsulinOptions,
                        onSelect = { onFieldChange { copy(dinnerInsulinType = it) } },
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = formState.dinnerPostSugar,
                        onValueChange = { onFieldChange { copy(dinnerPostSugar = it) } },
                        label = { Text("2H Sugar (Post)") },
                        placeholder = { Text("e.g. 87mg") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dinner_2h_sugar_input")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = formState.dinnerMeal,
                    onValueChange = { onFieldChange { copy(dinnerMeal = it) } },
                    label = { Text("Dinner Food Details (रात का भोजन)") },
                    placeholder = { Text("e.g. दाल 3 रोटी / बिरयानी 1 रोटी सब्जी") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dinner_meal_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Milk Selection (Yes / No)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Milk (दूध का सेवन)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Did you drink milk with dinner?",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = formState.dinnerMilk.equals("Yes", ignoreCase = true),
                            onClick = { onFieldChange { copy(dinnerMilk = "Yes") } },
                            label = { Text("Yes (हाँ)") },
                            leadingIcon = if (formState.dinnerMilk.equals("Yes", ignoreCase = true)) {
                                { Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null
                        )
                        FilterChip(
                            selected = formState.dinnerMilk.equals("No", ignoreCase = true),
                            onClick = { onFieldChange { copy(dinnerMilk = "No") } },
                            label = { Text("No (नहीं)") }
                        )
                    }
                }
            }

            // ==========================================
            // 4. NIGHT / BEDTIME SECTION (सोते समय)
            // ==========================================
            MealSectionCard(
                title = "Night / Bedtime Section (सोते समय)",
                subtitle = "10:00 PM Night Insulin (Tresiba / Levemir) • Units • Time • Notes",
                icon = Icons.Default.Nightlight,
                headerBg = SectionNightBg,
                headerBorder = SectionNightBorder,
                accentColor = SectionNightText
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    InsulinDropdown(
                        label = "Night Insulin",
                        selectedType = formState.nightInsulinType,
                        options = BasalInsulinOptions,
                        onSelect = { onFieldChange { copy(nightInsulinType = it) } },
                        modifier = Modifier.weight(1f)
                    )

                    InsulinUnitStepper(
                        label = "Night Units (e.g. 14U)",
                        currentValue = formState.nightInsulinUnits,
                        onValueChange = { onFieldChange { copy(nightInsulinUnits = it) } },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Time Selection Field
                    OutlinedTextField(
                        value = formState.nightInsulinTime,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Scheduled Time") },
                        trailingIcon = {
                            IconButton(onClick = { showNightTimePicker = true }) {
                                Icon(Icons.Default.AccessTime, contentDescription = "Pick Time")
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showNightTimePicker = true }
                            .testTag("night_time_picker_field")
                    )

                    OutlinedTextField(
                        value = formState.nightBedtimeSugar,
                        onValueChange = { onFieldChange { copy(nightBedtimeSugar = it) } },
                        label = { Text("Bedtime / 4am Sugar") },
                        placeholder = { Text("e.g. 53mg") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("night_bedtime_sugar_input")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = formState.generalNotes,
                    onValueChange = { onFieldChange { copy(generalNotes = it) } },
                    label = { Text("General Day Notes / Extra (अतिरिक्त टिप्पणी)") },
                    placeholder = { Text("e.g. 4am 53mg, आधा मकई भुट्टा, कमजोरी महसूस हुई") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("general_notes_input")
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Bottom Sticky Save Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 6.dp,
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "GlucoDiary • ${formState.date}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Auto-Synced to Cloud",
                        fontSize = 11.sp,
                        color = TealSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Button(
                    onClick = onSaveEntry,
                    enabled = !formState.isSaving,
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("save_entry_button")
                ) {
                    if (formState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White
                        )
                    } else {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Save Entry (सुरक्षित करें)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun MealSectionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    headerBg: Color,
    headerBorder: Color,
    accentColor: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerBg)
                    .border(
                        width = 1.dp,
                        color = headerBorder,
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = accentColor
                        )
                        Text(
                            text = subtitle,
                            fontSize = 11.sp,
                            color = accentColor.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Body
            Column(modifier = Modifier.padding(14.dp)) {
                content()
            }
        }
    }
}

@Composable
fun InsulinUnitStepper(
    label: String,
    currentValue: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val numeric = currentValue.filter { it.isDigit() }.toIntOrNull() ?: 0

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = currentValue,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = { Text("e.g. 7U") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            trailingIcon = {
                Row(modifier = Modifier.padding(end = 4.dp)) {
                    IconButton(
                        onClick = {
                            val newVal = if (numeric > 0) numeric - 1 else 0
                            onValueChange("${newVal}U")
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                    }
                    IconButton(
                        onClick = {
                            val newVal = numeric + 1
                            onValueChange("${newVal}U")
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
