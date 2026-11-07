package com.example.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Domain model representing a single day's blood sugar, meals, and insulin log.
 * Matching the exact structure of "दैनिक ब्लड शुगर एवं आहार डायरी".
 */
data class DiaryEntry(
    val id: String = "",
    val phoneNumber: String = "",
    val date: String = "", // e.g., "07/08/26" or "07/08/2026"
    val timestamp: Long = System.currentTimeMillis(),

    // --- Breakfast / Fasting (नाश्ता) ---
    val fastingSugar: String = "", // e.g., "142mg" or "142"
    val breakfastInsulinType: String = "Novorapid",
    val breakfastInsulinUnits: String = "", // e.g., "7U"
    val breakfastMeal: String = "", // e.g., "3 अंडे 2 रोटी अरहर दाल"
    val breakfastPostSugar: String = "", // e.g., "74mg"
    val breakfast11AmNote: String = "", // e.g., "प्रोटीन पा 2 चम्मच"

    // --- Lunch (दोपहर का खाना) ---
    val lunchPreSugar: String = "", // e.g., "208mg"
    val lunchInsulinType: String = "Novorapid",
    val lunchInsulinUnits: String = "", // e.g., "8U"
    val lunchMeal: String = "", // e.g., "सब्जी 3 रोटी"
    val lunchPostSugar: String = "", // e.g., "189mg"
    val lunch5PmNote: String = "", // e.g., "प्रोटीन पा 3 अंडे"

    // --- Dinner (रात का खाना) ---
    val dinnerPreSugar: String = "", // e.g., "232mg"
    val dinnerInsulinType: String = "Novorapid",
    val dinnerInsulinUnits: String = "", // e.g., "8U"
    val dinnerMeal: String = "", // e.g., "सब्जी 3 रोटी"
    val dinnerPostSugar: String = "", // e.g., "87mg"
    val dinnerMilk: String = "Yes", // "Yes" or "No"

    // --- Night / Bedtime (सोते समय) ---
    val nightInsulinType: String = "Tresiba", // e.g. Tresiba, Levemir, Lantus
    val nightInsulinUnits: String = "", // e.g., "14U"
    val nightInsulinTime: String = "10:00 PM", // Dedicated time field
    val nightBedtimeSugar: String = "", // e.g. 4am or bedtime sugar
    val generalNotes: String = "" // Additional remarks
) {
    // Helper to safely extract integer sugar value for graphs and status badges
    fun getNumericSugar(raw: String): Int? {
        val digitsOnly = raw.filter { it.isDigit() }
        return digitsOnly.toIntOrNull()
    }

    // Helper to safely extract insulin unit number
    fun getNumericInsulinUnits(raw: String): Double {
        val sanitized = raw.replace("U", "", ignoreCase = true).trim()
        return sanitized.toDoubleOrNull() ?: 0.0
    }

    // Daily total insulin
    val totalInsulinUnits: Double
        get() = getNumericInsulinUnits(breakfastInsulinUnits) +
                getNumericInsulinUnits(lunchInsulinUnits) +
                getNumericInsulinUnits(dinnerInsulinUnits) +
                getNumericInsulinUnits(nightInsulinUnits)

    companion object {
        fun todayDateString(): String {
            val sdf = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
            return sdf.format(Date())
        }
    }
}
