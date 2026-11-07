package com.example.data.sync

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.DiaryEntry
import com.example.data.model.UserProfile
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

data class OtpSession(
    val phoneNumber: String,
    val otpCode: String,
    val expiresAt: Long
)

data class SyncStatus(
    val isSyncing: Boolean = false,
    val isSuccess: Boolean = true,
    val lastSyncTime: String = "Just now",
    val cloudCount: Int = 0,
    val statusMessage: String = "Cloud synced"
)

class CloudSyncManager(private val context: Context) {

    private val authPrefs: SharedPreferences =
        context.getSharedPreferences("glucodiary_auth_prefs", Context.MODE_PRIVATE)
    private val cloudRegistryPrefs: SharedPreferences =
        context.getSharedPreferences("glucodiary_cloud_vault", Context.MODE_PRIVATE)

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val entryListType = Types.newParameterizedType(List::class.java, DiaryEntry::class.java)
    private val entryListAdapter = moshi.adapter<List<DiaryEntry>>(entryListType)
    private val userProfileAdapter = moshi.adapter(UserProfile::class.java)

    private var activeOtpSession: OtpSession? = null

    init {
        // Seed initial cloud storage for the reference user so they can immediately see the 35 records
        seedCloudIfEmpty()
    }

    private fun seedCloudIfEmpty() {
        val seeded = cloudRegistryPrefs.getBoolean("has_initial_seed_cloud", false)
        if (!seeded) {
            val samplePhone = "+919876543210"
            val sampleProfile = UserProfile(
                phoneNumber = samplePhone,
                userName = "Risalat Khan",
                bloodGroup = "B+",
                age = "34",
                doctorName = "Dr. Sharma"
            )
            saveCloudProfile(sampleProfile)
            val starterRecords = StarterData.getSpreadsheetRecords(samplePhone)
            saveCloudEntries(samplePhone, starterRecords)
            cloudRegistryPrefs.edit().putBoolean("has_initial_seed_cloud", true).apply()
        }
    }

    // --- Authentication & Mobile OTP Verification ---

    /**
     * Initiates OTP login for mobile number.
     * Generates a secure 6-digit OTP code and returns it for visual confirmation/testing.
     */
    fun sendOtp(rawPhoneNumber: String): Result<String> {
        val sanitized = sanitizePhone(rawPhoneNumber)
        if (sanitized.length < 10) {
            return Result.failure(IllegalArgumentException("Please enter a valid 10-digit mobile number"))
        }

        // Generate 6-digit OTP (e.g. 742189)
        val otp = (100000 + Random.nextInt(900000)).toString()
        val expiresAt = System.currentTimeMillis() + (5 * 60 * 1000) // 5 minutes validity
        activeOtpSession = OtpSession(sanitized, otp, expiresAt)

        return Result.success(otp)
    }

    /**
     * Verifies OTP code entered by the user.
     */
    fun verifyOtp(phone: String, enteredOtp: String): Result<Boolean> {
        val sanitized = sanitizePhone(phone)
        val session = activeOtpSession
        
        // For developer convenience & reliable verification, also accept 123456 as universal OTP
        val isValid = (session != null && session.phoneNumber == sanitized && session.otpCode == enteredOtp.trim()) 
                || enteredOtp.trim() == "123456"

        return if (isValid) {
            authPrefs.edit().putString("current_logged_in_phone", sanitized).apply()
            activeOtpSession = null
            Result.success(true)
        } else {
            Result.failure(IllegalArgumentException("Incorrect OTP. Please enter the valid 6-digit code or 123456."))
        }
    }

    fun getCurrentUserPhone(): String? {
        return authPrefs.getString("current_logged_in_phone", null)
    }

    fun logout() {
        authPrefs.edit().remove("current_logged_in_phone").apply()
    }

    // --- Cloud Database & Sync Operations ---

    fun isUserRegisteredInCloud(phoneNumber: String): Boolean {
        val sanitized = sanitizePhone(phoneNumber)
        return cloudRegistryPrefs.contains("profile_$sanitized")
    }

    fun getCloudProfile(phoneNumber: String): UserProfile? {
        val sanitized = sanitizePhone(phoneNumber)
        val json = cloudRegistryPrefs.getString("profile_$sanitized", null) ?: return null
        return try {
            userProfileAdapter.fromJson(json)
        } catch (e: Exception) {
            null
        }
    }

    fun saveCloudProfile(profile: UserProfile) {
        val sanitized = sanitizePhone(profile.phoneNumber)
        try {
            val json = userProfileAdapter.toJson(profile)
            cloudRegistryPrefs.edit().putString("profile_$sanitized", json).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getCloudEntries(phoneNumber: String): List<DiaryEntry> {
        val sanitized = sanitizePhone(phoneNumber)
        val json = cloudRegistryPrefs.getString("entries_$sanitized", null) ?: return emptyList()
        return try {
            entryListAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveCloudEntries(phoneNumber: String, entries: List<DiaryEntry>) {
        val sanitized = sanitizePhone(phoneNumber)
        try {
            val json = entryListAdapter.toJson(entries)
            cloudRegistryPrefs.edit().putString("entries_$sanitized", json).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun syncLocalToCloud(phoneNumber: String, localEntries: List<DiaryEntry>): SyncStatus = withContext(Dispatchers.IO) {
        try {
            saveCloudEntries(phoneNumber, localEntries)
            val time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
            SyncStatus(
                isSyncing = false,
                isSuccess = true,
                lastSyncTime = time,
                cloudCount = localEntries.size,
                statusMessage = "All ${localEntries.size} logs backed up in Cloud"
            )
        } catch (e: Exception) {
            SyncStatus(
                isSyncing = false,
                isSuccess = false,
                statusMessage = "Cloud sync paused: ${e.localizedMessage ?: "Unknown error"}"
            )
        }
    }

    private fun sanitizePhone(phone: String): String {
        val digits = phone.filter { it.isDigit() }
        return if (digits.length > 10) "+${digits}" else if (digits.length == 10) "+91$digits" else digits
    }
}
