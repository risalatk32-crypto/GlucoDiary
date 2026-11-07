package com.example.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DiaryEntry
import com.example.data.model.UserProfile
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.SugarElevated
import com.example.ui.theme.SugarHigh
import com.example.ui.theme.SugarLow
import com.example.ui.theme.SugarNormal
import com.example.ui.theme.TealSecondary

@Composable
fun AnalyticsScreen(
    entries: List<DiaryEntry>,
    userProfile: UserProfile,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Calculations
    val fastingReadings = remember(entries) {
        entries.mapNotNull { it.getNumericSugar(it.fastingSugar) }
    }
    val avgFasting = remember(fastingReadings) {
        if (fastingReadings.isNotEmpty()) fastingReadings.average().toInt() else 0
    }

    val allSugarReadings = remember(entries) {
        entries.flatMap { entry ->
            listOfNotNull(
                entry.getNumericSugar(entry.fastingSugar),
                entry.getNumericSugar(entry.breakfastPostSugar),
                entry.getNumericSugar(entry.lunchPreSugar),
                entry.getNumericSugar(entry.lunchPostSugar),
                entry.getNumericSugar(entry.dinnerPreSugar),
                entry.getNumericSugar(entry.dinnerPostSugar),
                entry.getNumericSugar(entry.nightBedtimeSugar)
            )
        }
    }

    val overallAvgSugar = remember(allSugarReadings) {
        if (allSugarReadings.isNotEmpty()) allSugarReadings.average().toInt() else 0
    }

    val totalReadingsCount = allSugarReadings.size

    val inNormalCount = allSugarReadings.count { it in 70..140 }
    val inElevatedCount = allSugarReadings.count { it in 141..199 }
    val inHighCount = allSugarReadings.count { it >= 200 }
    val inLowCount = allSugarReadings.count { it < 70 }

    val normalPct = if (totalReadingsCount > 0) inNormalCount.toFloat() / totalReadingsCount else 0f
    val elevatedPct = if (totalReadingsCount > 0) inElevatedCount.toFloat() / totalReadingsCount else 0f
    val highPct = if (totalReadingsCount > 0) inHighCount.toFloat() / totalReadingsCount else 0f

    val avgDailyInsulin = remember(entries) {
        val daysWithInsulin = entries.map { it.totalInsulinUnits }.filter { it > 0 }
        if (daysWithInsulin.isNotEmpty()) daysWithInsulin.average() else 0.0
    }

    fun shareDoctorReport() {
        val sb = StringBuilder()
        sb.appendLine("📋 GlucoDiary • Medical Blood Sugar & Diet Report")
        sb.appendLine("Name: ${userProfile.userName.ifBlank { "Patient" }} | Age: ${userProfile.age} | Blood Group: ${userProfile.bloodGroup}")
        sb.appendLine("Mobile: ${userProfile.phoneNumber}")
        sb.appendLine("Total Days Logged: ${entries.size}")
        sb.appendLine("-------------------------------------------")
        sb.appendLine("Average Fasting Blood Sugar: $avgFasting mg/dL")
        sb.appendLine("Overall Average Sugar: $overallAvgSugar mg/dL")
        sb.appendLine("Average Daily Insulin: ${String.format("%.1f", avgDailyInsulin)} Units")
        sb.appendLine("Range Distribution:")
        sb.appendLine("• In Target (70-140 mg/dL): ${(normalPct * 100).toInt()}% ($inNormalCount readings)")
        sb.appendLine("• Elevated (141-199 mg/dL): ${(elevatedPct * 100).toInt()}% ($inElevatedCount readings)")
        sb.appendLine("• High (>= 200 mg/dL): ${(highPct * 100).toInt()}% ($inHighCount readings)")
        sb.appendLine("-------------------------------------------")
        sb.appendLine("Recent 5 Days Summary:")
        entries.take(5).forEach { entry ->
            sb.appendLine("${entry.date}: Fasting=${entry.fastingSugar}, Novorapid=${entry.breakfastInsulinUnits}, Lunch=${entry.lunchPreSugar}/${entry.lunchMeal}, Dinner=${entry.dinnerPreSugar}/${entry.dinnerMeal}, Tresiba=${entry.nightInsulinUnits}")
        }
        sb.appendLine("Generated from GlucoDiary (दैनिक ब्लड शुगर एवं आहार डायरी)")

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "GlucoDiary Health Report - ${userProfile.userName}")
            putExtra(Intent.EXTRA_TEXT, sb.toString())
        }
        context.startActivity(Intent.createChooser(intent, "Share Doctor Report"))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quick Stats Metric Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Avg Fasting",
                value = if (avgFasting > 0) "$avgFasting mg" else "--",
                subtitle = "Goal: 70-130 mg/dL",
                color = BluePrimary,
                modifier = Modifier.weight(1f)
            )

            StatCard(
                title = "Overall Avg",
                value = if (overallAvgSugar > 0) "$overallAvgSugar mg" else "--",
                subtitle = "$totalReadingsCount total tests",
                color = TealSecondary,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Daily Insulin",
                value = if (avgDailyInsulin > 0) "${String.format("%.1f", avgDailyInsulin)}U" else "--",
                subtitle = "Basal + Bolus total",
                color = Color(0xFF6A1B9A),
                modifier = Modifier.weight(1f)
            )

            StatCard(
                title = "Days Tracked",
                value = "${entries.size}",
                subtitle = "Synchronized to Cloud",
                color = Color(0xFF00796B),
                modifier = Modifier.weight(1f)
            )
        }

        // Glycemic Distribution Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Speed, contentDescription = null, tint = BluePrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Blood Sugar Distribution",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Progress Bars
                DistributionBar(
                    label = "Normal / Target (70 - 140 mg/dL)",
                    percentage = normalPct,
                    count = inNormalCount,
                    color = SugarNormal
                )

                Spacer(modifier = Modifier.height(12.dp))

                DistributionBar(
                    label = "Borderline / Elevated (141 - 199 mg/dL)",
                    percentage = elevatedPct,
                    count = inElevatedCount,
                    color = SugarElevated
                )

                Spacer(modifier = Modifier.height(12.dp))

                DistributionBar(
                    label = "High Sugar (>= 200 mg/dL)",
                    percentage = highPct,
                    count = inHighCount,
                    color = SugarHigh
                )
            }
        }

        // Share Report Button
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.HealthAndSafety, contentDescription = null, tint = BluePrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Doctor Consultation Report",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = BluePrimary
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Generate a formatted summary of your blood sugar readings, insulin doses, and meal history to share directly with your diabetologist or doctor.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = { shareDoctorReport() },
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share Medical Report with Doctor", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = subtitle, fontSize = 10.sp, color = Color.Gray)
        }
    }
}

@Composable
fun DistributionBar(
    label: String,
    percentage: Float,
    count: Int,
    color: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Text(
                text = "${(percentage * 100).toInt()}% ($count)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { percentage },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.15f)
        )
    }
}
