package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.DiaryEntry
import com.example.data.model.UserProfile
import com.example.data.repository.DiaryRepository
import com.example.data.sync.CloudSyncManager
import com.example.data.sync.SyncStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

sealed interface AuthUiState {
    data object LoggedOut : AuthUiState
    data class OtpSent(val phoneNumber: String, val simulatedOtp: String, val message: String) : AuthUiState
    data class LoggedIn(val profile: UserProfile, val isNewAccount: Boolean, val restoredCount: Int) : AuthUiState
}

data class FormState(
    val date: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val fastingSugar: String = "",
    val breakfastInsulinType: String = "Novorapid",
    val breakfastInsulinUnits: String = "7U",
    val breakfastMeal: String = "",
    val breakfastPostSugar: String = "",
    val breakfast11AmNote: String = "",
    val lunchPreSugar: String = "",
    val lunchInsulinType: String = "Novorapid",
    val lunchInsulinUnits: String = "8U",
    val lunchMeal: String = "",
    val lunchPostSugar: String = "",
    val lunch5PmNote: String = "",
    val dinnerPreSugar: String = "",
    val dinnerInsulinType: String = "Novorapid",
    val dinnerInsulinUnits: String = "8U",
    val dinnerMeal: String = "",
    val dinnerPostSugar: String = "",
    val dinnerMilk: String = "Yes",
    val nightInsulinType: String = "Tresiba",
    val nightInsulinUnits: String = "14U",
    val nightInsulinTime: String = "10:00 PM",
    val nightBedtimeSugar: String = "",
    val generalNotes: String = "",
    val isSaving: Boolean = false,
    val saveMessage: String? = null
)

class DiaryViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val cloudSyncManager = CloudSyncManager(application)
    private val repository = DiaryRepository(db.diaryDao(), cloudSyncManager)

    private val _authUiState = MutableStateFlow<AuthUiState>(AuthUiState.LoggedOut)
    val authUiState: StateFlow<AuthUiState> = _authUiState.asStateFlow()

    private val _currentPhoneNumber = MutableStateFlow<String?>(null)
    val currentPhoneNumber: StateFlow<String?> = _currentPhoneNumber.asStateFlow()

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _formState = MutableStateFlow(FormState(date = getCurrentDateFormatted()))
    val formState: StateFlow<FormState> = _formState.asStateFlow()

    private val _syncStatus = MutableStateFlow(SyncStatus())
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // All diary entries for current user in realtime Flow
    val allEntries: StateFlow<List<DiaryEntry>> = _currentPhoneNumber.flatMapLatest { phone ->
        if (phone != null) {
            repository.getEntriesFlow(phone)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        checkExistingLogin()
    }

    private fun checkExistingLogin() {
        val existingPhone = cloudSyncManager.getCurrentUserPhone()
        if (!existingPhone.isNullOrBlank()) {
            _currentPhoneNumber.value = existingPhone
            viewModelScope.launch {
                val (profile, count) = repository.restoreOrInitializeUser(existingPhone)
                _userProfile.value = profile
                _authUiState.value = AuthUiState.LoggedIn(
                    profile = profile,
                    isNewAccount = false,
                    restoredCount = count
                )
                loadEntryForDate(_formState.value.date)
                refreshSyncStatus()
            }
        } else {
            _authUiState.value = AuthUiState.LoggedOut
        }
    }

    // --- Authentication Actions ---

    fun sendOtp(phoneNumber: String) {
        val cleanPhone = phoneNumber.trim()
        val result = cloudSyncManager.sendOtp(cleanPhone)
        result.onSuccess { otp ->
            _authUiState.value = AuthUiState.OtpSent(
                phoneNumber = cleanPhone,
                simulatedOtp = otp,
                message = "OTP verification code sent! (Use $otp or 123456)"
            )
            _toastMessage.value = "OTP Code: $otp"
        }.onFailure { ex ->
            _toastMessage.value = ex.message ?: "Failed to send OTP"
        }
    }

    fun verifyOtp(phone: String, enteredOtp: String) {
        val result = cloudSyncManager.verifyOtp(phone, enteredOtp)
        result.onSuccess {
            val cleanPhone = if (phone.filter { it.isDigit() }.length == 10) "+91${phone.filter { it.isDigit() }}" else phone
            _currentPhoneNumber.value = cleanPhone

            viewModelScope.launch {
                val isRegistered = cloudSyncManager.isUserRegisteredInCloud(cleanPhone)
                val (profile, restoredCount) = repository.restoreOrInitializeUser(cleanPhone)
                
                _userProfile.value = profile
                _authUiState.value = AuthUiState.LoggedIn(
                    profile = profile,
                    isNewAccount = !isRegistered && restoredCount == 0,
                    restoredCount = restoredCount
                )
                loadEntryForDate(_formState.value.date)
                refreshSyncStatus()
                _toastMessage.value = if (restoredCount > 0) {
                    "Welcome back! Restored $restoredCount diary logs from Cloud."
                } else {
                    "Mobile verified! Welcome to GlucoDiary."
                }
            }
        }.onFailure { ex ->
            _toastMessage.value = ex.message ?: "Invalid OTP"
        }
    }

    fun logout() {
        cloudSyncManager.logout()
        _currentPhoneNumber.value = null
        _authUiState.value = AuthUiState.LoggedOut
        _toastMessage.value = "Logged out successfully"
    }

    // --- Form & Entry Management ---

    fun selectDate(dateStr: String) {
        _formState.value = _formState.value.copy(date = dateStr)
        loadEntryForDate(dateStr)
    }

    fun loadEntryForDate(dateStr: String) {
        val phone = _currentPhoneNumber.value ?: return
        viewModelScope.launch {
            val existing = repository.getEntryForDate(phone, dateStr)
            if (existing != null) {
                _formState.value = FormState(
                    date = existing.date,
                    timestamp = existing.timestamp,
                    fastingSugar = existing.fastingSugar,
                    breakfastInsulinType = existing.breakfastInsulinType,
                    breakfastInsulinUnits = existing.breakfastInsulinUnits,
                    breakfastMeal = existing.breakfastMeal,
                    breakfastPostSugar = existing.breakfastPostSugar,
                    breakfast11AmNote = existing.breakfast11AmNote,
                    lunchPreSugar = existing.lunchPreSugar,
                    lunchInsulinType = existing.lunchInsulinType,
                    lunchInsulinUnits = existing.lunchInsulinUnits,
                    lunchMeal = existing.lunchMeal,
                    lunchPostSugar = existing.lunchPostSugar,
                    lunch5PmNote = existing.lunch5PmNote,
                    dinnerPreSugar = existing.dinnerPreSugar,
                    dinnerInsulinType = existing.dinnerInsulinType,
                    dinnerInsulinUnits = existing.dinnerInsulinUnits,
                    dinnerMeal = existing.dinnerMeal,
                    dinnerPostSugar = existing.dinnerPostSugar,
                    dinnerMilk = existing.dinnerMilk,
                    nightInsulinType = existing.nightInsulinType,
                    nightInsulinUnits = existing.nightInsulinUnits,
                    nightInsulinTime = existing.nightInsulinTime,
                    nightBedtimeSugar = existing.nightBedtimeSugar,
                    generalNotes = existing.generalNotes
                )
            } else {
                // Clear fields for new entry on this date while preserving date
                _formState.value = FormState(
                    date = dateStr,
                    timestamp = System.currentTimeMillis()
                )
            }
        }
    }

    fun updateFormField(updater: FormState.() -> FormState) {
        _formState.value = _formState.value.updater()
    }

    fun saveCurrentEntry() {
        val phone = _currentPhoneNumber.value ?: return
        val current = _formState.value
        if (current.date.isBlank()) {
            _toastMessage.value = "Please select a date"
            return
        }

        viewModelScope.launch {
            _formState.value = current.copy(isSaving = true)
            val entry = DiaryEntry(
                id = "${phone}_${current.date}",
                phoneNumber = phone,
                date = current.date,
                timestamp = current.timestamp,
                fastingSugar = current.fastingSugar.trim(),
                breakfastInsulinType = current.breakfastInsulinType.trim(),
                breakfastInsulinUnits = current.breakfastInsulinUnits.trim(),
                breakfastMeal = current.breakfastMeal.trim(),
                breakfastPostSugar = current.breakfastPostSugar.trim(),
                breakfast11AmNote = current.breakfast11AmNote.trim(),
                lunchPreSugar = current.lunchPreSugar.trim(),
                lunchInsulinType = current.lunchInsulinType.trim(),
                lunchInsulinUnits = current.lunchInsulinUnits.trim(),
                lunchMeal = current.lunchMeal.trim(),
                lunchPostSugar = current.lunchPostSugar.trim(),
                lunch5PmNote = current.lunch5PmNote.trim(),
                dinnerPreSugar = current.dinnerPreSugar.trim(),
                dinnerInsulinType = current.dinnerInsulinType.trim(),
                dinnerInsulinUnits = current.dinnerInsulinUnits.trim(),
                dinnerMeal = current.dinnerMeal.trim(),
                dinnerPostSugar = current.dinnerPostSugar.trim(),
                dinnerMilk = current.dinnerMilk.trim(),
                nightInsulinType = current.nightInsulinType.trim(),
                nightInsulinUnits = current.nightInsulinUnits.trim(),
                nightInsulinTime = current.nightInsulinTime.trim(),
                nightBedtimeSugar = current.nightBedtimeSugar.trim(),
                generalNotes = current.generalNotes.trim()
            )

            repository.saveEntry(entry)
            refreshSyncStatus()
            _formState.value = current.copy(
                isSaving = false,
                saveMessage = "Saved & Synced (${current.date})"
            )
            _toastMessage.value = "Entry for ${current.date} saved & synced to cloud"
        }
    }

    fun deleteEntry(entry: DiaryEntry) {
        val phone = _currentPhoneNumber.value ?: return
        viewModelScope.launch {
            repository.deleteEntry(entry.id, phone)
            refreshSyncStatus()
            _toastMessage.value = "Deleted entry for ${entry.date}"
            if (_formState.value.date == entry.date) {
                loadEntryForDate(entry.date)
            }
        }
    }

    fun importStarterRecords() {
        val phone = _currentPhoneNumber.value ?: return
        viewModelScope.launch {
            val count = repository.importStarterSpreadsheetData(phone)
            refreshSyncStatus()
            loadEntryForDate(_formState.value.date)
            _toastMessage.value = "Imported $count spreadsheet records successfully!"
        }
    }

    fun refreshSyncStatus() {
        val phone = _currentPhoneNumber.value ?: return
        viewModelScope.launch {
            val status = repository.syncWithCloud(phone)
            _syncStatus.value = status
        }
    }

    fun updateProfile(userName: String, bloodGroup: String, age: String, doctorName: String) {
        val phone = _currentPhoneNumber.value ?: return
        val updated = _userProfile.value.copy(
            phoneNumber = phone,
            userName = userName.trim(),
            bloodGroup = bloodGroup.trim(),
            age = age.trim(),
            doctorName = doctorName.trim()
        )
        viewModelScope.launch {
            repository.saveUserProfile(updated)
            _userProfile.value = updated
            _toastMessage.value = "Profile updated & synced to Cloud"
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    companion object {
        fun getCurrentDateFormatted(): String {
            val sdf = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
            return sdf.format(Date())
        }
    }
}
