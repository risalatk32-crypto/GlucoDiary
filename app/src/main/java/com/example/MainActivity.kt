package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AnalyticsScreen
import com.example.ui.components.DailyEntryFormScreen
import com.example.ui.components.HistoryListScreen
import com.example.ui.components.LoginScreen
import com.example.ui.components.ProfileScreen
import com.example.ui.components.SpreadsheetTableView
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.GlucoDiaryTheme
import com.example.ui.theme.TealSecondary
import com.example.ui.viewmodel.AuthUiState
import com.example.ui.viewmodel.DiaryViewModel

enum class MainTab(val title: String, val hindiTitle: String, val icon: ImageVector) {
    ENTRY("Daily Entry", "दैनिक डायरी", Icons.Default.EditNote),
    TABLE("Spreadsheet", "तालिका", Icons.Default.TableChart),
    HISTORY("History", "इतिहास", Icons.Default.History),
    TRENDS("Analytics", "आंकड़े", Icons.Default.Assessment),
    PROFILE("Profile", "प्रोफाइल", Icons.Default.Person)
}

class MainActivity : ComponentActivity() {

    private val viewModel: DiaryViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            GlucoDiaryTheme {
                val context = LocalContext.current
                val authUiState by viewModel.authUiState.collectAsStateWithLifecycle()
                val formState by viewModel.formState.collectAsStateWithLifecycle()
                val entries by viewModel.allEntries.collectAsStateWithLifecycle()
                val profile by viewModel.userProfile.collectAsStateWithLifecycle()
                val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
                val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

                var currentTab by remember { mutableStateOf(MainTab.ENTRY) }
                val snackbarHostState = remember { SnackbarHostState() }

                // Display toast or snackbar messages
                LaunchedEffect(toastMessage) {
                    toastMessage?.let { msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        viewModel.clearToast()
                    }
                }

                // Handle back button on sub-tabs
                if (currentTab != MainTab.ENTRY) {
                    BackHandler {
                        currentTab = MainTab.ENTRY
                    }
                }

                if (authUiState !is AuthUiState.LoggedIn) {
                    // Mobile OTP Authentication Screen
                    LoginScreen(
                        authUiState = authUiState,
                        onSendOtp = { phone -> viewModel.sendOtp(phone) },
                        onVerifyOtp = { phone, otp -> viewModel.verifyOtp(phone, otp) }
                    )
                } else {
                    // Main App Interface
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = {
                            TopAppBar(
                                title = {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "GlucoDiary",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp,
                                                color = Color.White
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            // Cloud sync chip
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(Color.White.copy(alpha = 0.2f))
                                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.CloudDone,
                                                        contentDescription = "Cloud Status",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "Cloud Synced",
                                                        color = Color.White,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = "दैनिक ब्लड शुगर एवं आहार डायरी",
                                            fontSize = 11.sp,
                                            color = Color.White.copy(alpha = 0.85f)
                                        )
                                    }
                                },
                                actions = {
                                    IconButton(
                                        onClick = { viewModel.refreshSyncStatus() },
                                        modifier = Modifier.testTag("top_sync_button")
                                    ) {
                                        Icon(
                                            Icons.Default.Sync,
                                            contentDescription = "Sync with Cloud",
                                            tint = Color.White
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = BluePrimary,
                                    titleContentColor = Color.White
                                )
                            )
                        },
                        bottomBar = {
                            NavigationBar(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .windowInsetsPadding(WindowInsets.navigationBars),
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = 8.dp
                            ) {
                                MainTab.entries.forEach { tab ->
                                    val isSelected = currentTab == tab
                                    NavigationBarItem(
                                        selected = isSelected,
                                        onClick = { currentTab = tab },
                                        icon = {
                                            Icon(
                                                imageVector = tab.icon,
                                                contentDescription = tab.title
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = tab.title,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = BluePrimary,
                                            selectedTextColor = BluePrimary,
                                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                        ),
                                        modifier = Modifier.testTag("nav_item_${tab.name.lowercase()}")
                                    )
                                }
                            }
                        },
                        snackbarHost = { SnackbarHost(snackbarHostState) }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (currentTab) {
                                MainTab.ENTRY -> {
                                    DailyEntryFormScreen(
                                        formState = formState,
                                        onFieldChange = { updater -> viewModel.updateFormField(updater) },
                                        onSaveEntry = { viewModel.saveCurrentEntry() },
                                        onSelectDate = { dateStr -> viewModel.selectDate(dateStr) }
                                    )
                                }
                                MainTab.TABLE -> {
                                    SpreadsheetTableView(
                                        entries = entries,
                                        onSelectRowToEdit = { date ->
                                            viewModel.selectDate(date)
                                            currentTab = MainTab.ENTRY
                                        },
                                        onImportStarter = { viewModel.importStarterRecords() }
                                    )
                                }
                                MainTab.HISTORY -> {
                                    HistoryListScreen(
                                        entries = entries,
                                        onSelectToEdit = { date ->
                                            viewModel.selectDate(date)
                                            currentTab = MainTab.ENTRY
                                        },
                                        onDeleteEntry = { entry -> viewModel.deleteEntry(entry) }
                                    )
                                }
                                MainTab.TRENDS -> {
                                    AnalyticsScreen(
                                        entries = entries,
                                        userProfile = profile
                                    )
                                }
                                MainTab.PROFILE -> {
                                    ProfileScreen(
                                        userProfile = profile,
                                        syncStatus = syncStatus,
                                        onSaveProfile = { name, bg, age, doc ->
                                            viewModel.updateProfile(name, bg, age, doc)
                                        },
                                        onSyncNow = { viewModel.refreshSyncStatus() },
                                        onImportStarter = { viewModel.importStarterRecords() },
                                        onLogout = { viewModel.logout() }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
