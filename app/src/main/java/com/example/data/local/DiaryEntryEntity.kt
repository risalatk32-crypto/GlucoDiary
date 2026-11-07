package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.DiaryEntry

@Entity(tableName = "diary_entries")
data class DiaryEntryEntity(
    @PrimaryKey
    val id: String, // "${phoneNumber}_${date}"
    val phoneNumber: String,
    val date: String,
    val timestamp: Long,

    // Breakfast
    val fastingSugar: String,
    val breakfastInsulinType: String,
    val breakfastInsulinUnits: String,
    val breakfastMeal: String,
    val breakfastPostSugar: String,
    val breakfast11AmNote: String,

    // Lunch
    val lunchPreSugar: String,
    val lunchInsulinType: String,
    val lunchInsulinUnits: String,
    val lunchMeal: String,
    val lunchPostSugar: String,
    val lunch5PmNote: String,

    // Dinner
    val dinnerPreSugar: String,
    val dinnerInsulinType: String,
    val dinnerInsulinUnits: String,
    val dinnerMeal: String,
    val dinnerPostSugar: String,
    val dinnerMilk: String,

    // Night / Bedtime
    val nightInsulinType: String,
    val nightInsulinUnits: String,
    val nightInsulinTime: String,
    val nightBedtimeSugar: String,
    val generalNotes: String
) {
    fun toDomain(): DiaryEntry = DiaryEntry(
        id = id,
        phoneNumber = phoneNumber,
        date = date,
        timestamp = timestamp,
        fastingSugar = fastingSugar,
        breakfastInsulinType = breakfastInsulinType,
        breakfastInsulinUnits = breakfastInsulinUnits,
        breakfastMeal = breakfastMeal,
        breakfastPostSugar = breakfastPostSugar,
        breakfast11AmNote = breakfast11AmNote,
        lunchPreSugar = lunchPreSugar,
        lunchInsulinType = lunchInsulinType,
        lunchInsulinUnits = lunchInsulinUnits,
        lunchMeal = lunchMeal,
        lunchPostSugar = lunchPostSugar,
        lunch5PmNote = lunch5PmNote,
        dinnerPreSugar = dinnerPreSugar,
        dinnerInsulinType = dinnerInsulinType,
        dinnerInsulinUnits = dinnerInsulinUnits,
        dinnerMeal = dinnerMeal,
        dinnerPostSugar = dinnerPostSugar,
        dinnerMilk = dinnerMilk,
        nightInsulinType = nightInsulinType,
        nightInsulinUnits = nightInsulinUnits,
        nightInsulinTime = nightInsulinTime,
        nightBedtimeSugar = nightBedtimeSugar,
        generalNotes = generalNotes
    )

    companion object {
        fun fromDomain(entry: DiaryEntry): DiaryEntryEntity = DiaryEntryEntity(
            id = if (entry.id.isNotBlank()) entry.id else "${entry.phoneNumber}_${entry.date}",
            phoneNumber = entry.phoneNumber,
            date = entry.date,
            timestamp = entry.timestamp,
            fastingSugar = entry.fastingSugar,
            breakfastInsulinType = entry.breakfastInsulinType,
            breakfastInsulinUnits = entry.breakfastInsulinUnits,
            breakfastMeal = entry.breakfastMeal,
            breakfastPostSugar = entry.breakfastPostSugar,
            breakfast11AmNote = entry.breakfast11AmNote,
            lunchPreSugar = entry.lunchPreSugar,
            lunchInsulinType = entry.lunchInsulinType,
            lunchInsulinUnits = entry.lunchInsulinUnits,
            lunchMeal = entry.lunchMeal,
            lunchPostSugar = entry.lunchPostSugar,
            lunch5PmNote = entry.lunch5PmNote,
            dinnerPreSugar = entry.dinnerPreSugar,
            dinnerInsulinType = entry.dinnerInsulinType,
            dinnerInsulinUnits = entry.dinnerInsulinUnits,
            dinnerMeal = entry.dinnerMeal,
            dinnerPostSugar = entry.dinnerPostSugar,
            dinnerMilk = entry.dinnerMilk,
            nightInsulinType = entry.nightInsulinType,
            nightInsulinUnits = entry.nightInsulinUnits,
            nightInsulinTime = entry.nightInsulinTime,
            nightBedtimeSugar = entry.nightBedtimeSugar,
            generalNotes = entry.generalNotes
        )
    }
}
