package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryDao {

    @Query("SELECT * FROM diary_entries WHERE phoneNumber = :phoneNumber ORDER BY timestamp DESC, date DESC")
    fun getEntriesForPhoneFlow(phoneNumber: String): Flow<List<DiaryEntryEntity>>

    @Query("SELECT * FROM diary_entries WHERE phoneNumber = :phoneNumber ORDER BY timestamp DESC, date DESC")
    suspend fun getEntriesForPhone(phoneNumber: String): List<DiaryEntryEntity>

    @Query("SELECT * FROM diary_entries WHERE phoneNumber = :phoneNumber AND date = :date LIMIT 1")
    suspend fun getEntryForDate(phoneNumber: String, date: String): DiaryEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entry: DiaryEntryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<DiaryEntryEntity>)

    @Query("DELETE FROM diary_entries WHERE id = :id")
    suspend fun deleteEntryById(id: String)

    @Query("DELETE FROM diary_entries WHERE phoneNumber = :phoneNumber")
    suspend fun clearAllForPhone(phoneNumber: String)
}
