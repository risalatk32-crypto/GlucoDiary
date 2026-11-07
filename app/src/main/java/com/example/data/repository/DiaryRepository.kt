package com.example.data.repository

import com.example.data.local.DiaryDao
import com.example.data.local.DiaryEntryEntity
import com.example.data.model.DiaryEntry
import com.example.data.model.UserProfile
import com.example.data.sync.CloudSyncManager
import com.example.data.sync.StarterData
import com.example.data.sync.SyncStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class DiaryRepository(
    private val diaryDao: DiaryDao,
    private val cloudSyncManager: CloudSyncManager
) {

    fun getEntriesFlow(phoneNumber: String): Flow<List<DiaryEntry>> {
        return diaryDao.getEntriesForPhoneFlow(phoneNumber).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun getEntryForDate(phoneNumber: String, date: String): DiaryEntry? = withContext(Dispatchers.IO) {
        diaryDao.getEntryForDate(phoneNumber, date)?.toDomain()
    }

    suspend fun saveEntry(entry: DiaryEntry) = withContext(Dispatchers.IO) {
        val entity = DiaryEntryEntity.fromDomain(entry)
        diaryDao.insertOrUpdate(entity)
        // Automatically sync to cloud registry
        val allLocal = diaryDao.getEntriesForPhone(entry.phoneNumber).map { it.toDomain() }
        cloudSyncManager.saveCloudEntries(entry.phoneNumber, allLocal)
    }

    suspend fun deleteEntry(id: String, phoneNumber: String) = withContext(Dispatchers.IO) {
        diaryDao.deleteEntryById(id)
        val allLocal = diaryDao.getEntriesForPhone(phoneNumber).map { it.toDomain() }
        cloudSyncManager.saveCloudEntries(phoneNumber, allLocal)
    }

    suspend fun restoreOrInitializeUser(phoneNumber: String): Pair<UserProfile, Int> = withContext(Dispatchers.IO) {
        // Check if user has cloud history
        val cloudEntries = cloudSyncManager.getCloudEntries(phoneNumber)
        val profile = cloudSyncManager.getCloudProfile(phoneNumber) ?: UserProfile(
            phoneNumber = phoneNumber,
            userName = "User",
            bloodGroup = "B+",
            age = "30"
        )

        if (cloudEntries.isNotEmpty()) {
            val entities = cloudEntries.map { DiaryEntryEntity.fromDomain(it) }
            diaryDao.insertAll(entities)
        } else {
            // New user, check if local has anything, else keep empty
            val local = diaryDao.getEntriesForPhone(phoneNumber)
            if (local.isEmpty()) {
                // Fresh empty account as required by prompt!
            }
        }

        Pair(profile, cloudEntries.size)
    }

    suspend fun importStarterSpreadsheetData(phoneNumber: String): Int = withContext(Dispatchers.IO) {
        val records = StarterData.getSpreadsheetRecords(phoneNumber)
        val entities = records.map { DiaryEntryEntity.fromDomain(it) }
        diaryDao.insertAll(entities)
        cloudSyncManager.saveCloudEntries(phoneNumber, records)
        records.size
    }

    suspend fun getUserProfile(phoneNumber: String): UserProfile = withContext(Dispatchers.IO) {
        cloudSyncManager.getCloudProfile(phoneNumber) ?: UserProfile(
            phoneNumber = phoneNumber,
            userName = "",
            bloodGroup = "B+",
            age = ""
        )
    }

    suspend fun saveUserProfile(profile: UserProfile) = withContext(Dispatchers.IO) {
        cloudSyncManager.saveCloudProfile(profile)
    }

    suspend fun syncWithCloud(phoneNumber: String): SyncStatus = withContext(Dispatchers.IO) {
        val localEntities = diaryDao.getEntriesForPhone(phoneNumber).map { it.toDomain() }
        val cloudEntries = cloudSyncManager.getCloudEntries(phoneNumber)

        // Merge logic: combine entries by date, prioritizing latest
        val mergedMap = linkedMapOf<String, DiaryEntry>()
        cloudEntries.forEach { mergedMap[it.date] = it }
        localEntities.forEach { mergedMap[it.date] = it }

        val mergedList = mergedMap.values.toList()
        diaryDao.insertAll(mergedList.map { DiaryEntryEntity.fromDomain(it) })
        cloudSyncManager.syncLocalToCloud(phoneNumber, mergedList)
    }
}
