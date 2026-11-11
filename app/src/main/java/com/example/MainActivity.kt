package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.SocietyTopHeader
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.localization.societyStrings
import com.example.ui.screens.*
import com.example.ui.theme.*
import android.util.Log
import com.example.ui.viewmodel.ScreenTab
import com.example.ui.viewmodel.SocietyViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val TAG = "MainActivity"
    private var viewModel: SocietyViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            Log.i(TAG, "MainActivity onCreate starting...")
            enableEdgeToEdge()

            // Initialize ViewModel inside protected block
            try {
                val vm: SocietyViewModel by viewModels()
                viewModel = vm
                Log.i(TAG, "SocietyViewModel initialized successfully.")
            } catch (vmError: Throwable) {
                Log.e(TAG, "Error initializing SocietyViewModel: ${vmError.message}", vmError)
            }

            setContent {
                MyApplicationTheme {
                    val currentVm = viewModel
                    if (currentVm != null) {
                        SocietyMainApp(viewModel = currentVm)
                    } else {
                        // Fallback UI in case ViewModel failed to instantiate
                        StartupErrorFallbackScreen(
                            errorMessage = "Unable to initialize Society Database. Please tap retry.",
                            onRetry = {
                                recreate()
                            }
                        )
                    }
                }
            }
            Log.i(TAG, "MainActivity setContent completed successfully.")
        } catch (e: Throwable) {
            Log.e(TAG, "Critical error during MainActivity onCreate: ${e.message}", e)
            try {
                setContent {
                    MyApplicationTheme {
                        StartupErrorFallbackScreen(
                            errorMessage = "App initialization encountered an issue: ${e.localizedMessage ?: e.message}",
                            onRetry = { recreate() }
                        )
                    }
                }
            } catch (fallbackError: Throwable) {
                Log.e(TAG, "Fallback setContent failed: ${fallbackError.message}", fallbackError)
            }
        }
    }
}

@Composable
fun StartupErrorFallbackScreen(
    errorMessage: String,
    onRetry: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = SocietyBg
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, SocietyCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = SocietyError,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "Manmandir CHS",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = SocietyNavy
                    )
                    Text(
                        text = errorMessage,
                        fontSize = 13.sp,
                        color = SocietySlate,
                        lineHeight = 18.sp
                    )
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(containerColor = SocietyNavy),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Retry Loading", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocietyMainApp(viewModel: SocietyViewModel) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val currentTab by viewModel.currentTab.collectAsState()
    val currentRole by viewModel.currentRole.collectAsState()
    val residentRoom by viewModel.currentResidentRoom.collectAsState()
    val userSession by viewModel.userSession.collectAsState()
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val snackbarMsg by viewModel.snackbarMessage.collectAsState()

    var showGlobalSearchDialog by remember { mutableStateOf(false) }

    LaunchedEffect(snackbarMsg) {
        snackbarMsg?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    // Back handler closes drawer if open, or goes back to Dashboard if in sub-tab
    BackHandler(enabled = drawerState.isOpen || currentTab != ScreenTab.DASHBOARD) {
        if (drawerState.isOpen) {
            coroutineScope.launch { drawerState.close() }
        } else {
            viewModel.setTab(ScreenTab.DASHBOARD)
        }
    }

    CompositionLocalProvider(LocalAppLanguage provides currentLanguage) {
        val strings = societyStrings

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    drawerContainerColor = Color.White,
                    drawerContentColor = SocietyNavyDark,
                    modifier = Modifier.width(300.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Drawer Header
                        Column(modifier = Modifier.padding(vertical = 12.dp)) {
                            Text(
                                text = strings.appName,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.5.sp
                                ),
                                color = SocietyNavy
                            )
                            Text(
                                text = "${strings.appSubtitle} (${strings.proposedBadge})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SocietyNavyLight
                            )
                            Text(
                                text = "Wing A • 11 Floors • 85 Residential Units",
                                fontSize = 10.sp,
                                color = SocietySlate
                            )
                        }

                        // Language Selection Card inside Drawer
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SocietySurfaceVariant,
                            border = BorderStroke(1.dp, SocietyCardBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .testTag("drawer_language_selector")
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Language,
                                        contentDescription = null,
                                        tint = SocietyNavy,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = strings.selectLanguageTitle,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SocietyNavyDark
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    listOf(
                                        Triple(AppLanguage.ENGLISH, "English", "EN"),
                                        Triple(AppLanguage.MARATHI, "मराठी", "MR"),
                                        Triple(AppLanguage.HINDI, "हिंदी", "HI")
                                    ).forEach { (lang, label, code) ->
                                        val isSelected = currentLanguage == lang
                                        Surface(
                                            onClick = { viewModel.setLanguage(lang) },
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isSelected) SocietyGoldContainer else Color.White,
                                            border = BorderStroke(
                                                1.dp,
                                                if (isSelected) SocietyGoldDark else SocietyCardBorder
                                            ),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier.padding(vertical = 6.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = label,
                                                    fontSize = 11.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSelected) SocietyNavyDark else SocietySlate
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        HorizontalDivider(
                            color = SocietyCardBorder,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        // Navigation Items
                        val navItems = listOf(
                            Triple(ScreenTab.DASHBOARD, strings.tabHome, Icons.Default.Dashboard),
                            Triple(ScreenTab.BUILDING_STRUCTURE, strings.tabBuilding, Icons.Default.Apartment),
                            Triple(ScreenTab.MAINTENANCE, strings.tabMaintenance, Icons.Default.CurrencyRupee),
                            Triple(ScreenTab.RECEIPTS, strings.tabReceipts, Icons.Default.ReceiptLong),
                            Triple(ScreenTab.FINANCIALS, strings.tabFinancials, Icons.Default.Analytics),
                            Triple(ScreenTab.DOCUMENTS, strings.tabDocuments, Icons.Default.Folder),
                            Triple(ScreenTab.CORRESPONDENCE, strings.tabCorrespondence, Icons.Default.ForwardToInbox),
                            Triple(ScreenTab.EXPENSES, strings.tabExpenses, Icons.Default.Receipt),
                            Triple(ScreenTab.NOTICES, strings.tabNotices, Icons.Default.Announcement),
                            Triple(ScreenTab.COMPLAINTS, strings.tabComplaints, Icons.Default.SupportAgent),
                            Triple(ScreenTab.MEETINGS_TASKS, strings.tabMeetings, Icons.Default.EventNote),
                            Triple(ScreenTab.DIRECTORY, strings.tabDirectory, Icons.Default.Contacts),
                            Triple(ScreenTab.AUDIT_SETTINGS, strings.tabSettings, Icons.Default.Settings)
                        )

                        navItems.forEach { (tab, label, icon) ->
                            val isSelected = currentTab == tab
                            val isAccessible = userSession.accessibleTabs.contains(tab)
                            NavigationDrawerItem(
                                label = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            label,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 13.sp,
                                            color = if (isAccessible) {
                                                if (isSelected) SocietyNavyDark else SocietyNavy
                                            } else {
                                                SocietySlate.copy(alpha = 0.5f)
                                            }
                                        )
                                        if (!isAccessible) {
                                            Icon(
                                                Icons.Default.Lock,
                                                contentDescription = "Restricted",
                                                tint = SocietySlate.copy(alpha = 0.45f),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                },
                                icon = {
                                    Icon(
                                        icon,
                                        contentDescription = label,
                                        tint = if (isSelected) SocietyNavyDark else if (isAccessible) SocietySlate else SocietySlate.copy(alpha = 0.4f)
                                    )
                                },
                                selected = isSelected,
                                onClick = {
                                    viewModel.setTab(tab)
                                    coroutineScope.launch { drawerState.close() }
                                },
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = SocietyGoldContainer,
                                    selectedTextColor = SocietyNavyDark,
                                    unselectedTextColor = SocietyNavy,
                                    selectedIconColor = SocietyNavyDark,
                                    unselectedIconColor = SocietySlate
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "${strings.appName} CHS v1.0.0 (Wing A)",
                            fontSize = 9.sp,
                            color = SocietySlate
                        )
                    }
                }
            }
        ) {
            Scaffold(
                topBar = {
                    SocietyTopHeader(
                        currentRole = currentRole,
                        currentResidentRoom = residentRoom,
                        currentLanguage = currentLanguage,
                        onRoleChange = { newRole -> viewModel.setRole(newRole) },
                        onResidentRoomChange = { newRoom -> viewModel.setResidentRoom(newRoom) },
                        onLanguageChange = { newLang -> viewModel.setLanguage(newLang) },
                        onSearchClick = { showGlobalSearchDialog = true },
                        onMenuClick = {
                            coroutineScope.launch { drawerState.open() }
                        }
                    )
                },
                bottomBar = {
                    NavigationBar(
                        containerColor = Color.White,
                        contentColor = SocietyNavyDark,
                        tonalElevation = 6.dp
                    ) {
                        val bottomItems = listOf(
                            Triple(ScreenTab.DASHBOARD, strings.tabHome, Icons.Default.Home),
                            Triple(ScreenTab.BUILDING_STRUCTURE, strings.tabBuilding, Icons.Default.Apartment),
                            Triple(ScreenTab.MAINTENANCE, strings.tabMaintenance, Icons.Default.CurrencyRupee),
                            Triple(ScreenTab.RECEIPTS, strings.tabReceipts, Icons.Default.ReceiptLong)
                        )

                        bottomItems.forEach { (tab, label, icon) ->
                            val isSelected = currentTab == tab
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { viewModel.setTab(tab) },
                                icon = {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = label,
                                        tint = if (isSelected) SocietyNavy else SocietySlate
                                    )
                                },
                                label = {
                                    Text(
                                        label,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) SocietyNavy else SocietySlate
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = SocietyGoldContainer,
                                    selectedIconColor = SocietyNavy,
                                    unselectedIconColor = SocietySlate,
                                    selectedTextColor = SocietyNavy,
                                    unselectedTextColor = SocietySlate
                                )
                            )
                        }
                    }
                },
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) { innerPadding ->
                Crossfade(
                    targetState = currentTab,
                    label = "screen_crossfade",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) { tab ->
                    when (tab) {
                        ScreenTab.DASHBOARD -> HomeScreen(viewModel = viewModel)
                        ScreenTab.BUILDING_STRUCTURE -> BuildingStructureScreen(viewModel = viewModel)
                        ScreenTab.MAINTENANCE -> MaintenanceScreen(viewModel = viewModel)
                        ScreenTab.RECEIPTS -> ReceiptsScreen(viewModel = viewModel)
                        ScreenTab.FINANCIALS -> FinancialDashboardScreen(viewModel = viewModel)
                        ScreenTab.DOCUMENTS -> DocumentsScreen(viewModel = viewModel)
                        ScreenTab.CORRESPONDENCE -> CorrespondenceScreen(viewModel = viewModel)
                        ScreenTab.EXPENSES -> ExpensesScreen(viewModel = viewModel)
                        ScreenTab.NOTICES -> NoticesScreen(viewModel = viewModel)
                        ScreenTab.COMPLAINTS -> ComplaintsScreen(viewModel = viewModel)
                        ScreenTab.MEETINGS_TASKS -> MeetingsTasksScreen(viewModel = viewModel)
                        ScreenTab.DIRECTORY -> DirectoryScreen(viewModel = viewModel)
                        ScreenTab.AUDIT_SETTINGS -> AuditLogSettingsScreen(viewModel = viewModel)
                    }
                }
            }
        }

        if (showGlobalSearchDialog) {
            GlobalSearchDialog(
                viewModel = viewModel,
                onDismiss = { showGlobalSearchDialog = false },
                onNavigateToTab = { tab ->
                    viewModel.setTab(tab)
                    showGlobalSearchDialog = false
                }
            )
        }
    }
}

@Composable
fun GlobalSearchDialog(
    viewModel: SocietyViewModel,
    onDismiss: () -> Unit,
    onNavigateToTab: (ScreenTab) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val allUnits by viewModel.allUnits.collectAsState()
    val allDocs by viewModel.allDocuments.collectAsState()
    val allNotices by viewModel.allNotices.collectAsState()

    val matchedUnits = remember(query, allUnits) {
        if (query.isBlank()) emptyList()
        else {
            val q = query.trim().lowercase()
            allUnits.filter {
                it.roomNumber.toString().contains(q) ||
                        it.ownerName.lowercase().contains(q) ||
                        it.tenantName.lowercase().contains(q)
            }.take(5)
        }
    }

    val matchedDocs = remember(query, allDocs) {
        if (query.isBlank()) emptyList()
        else {
            val q = query.trim().lowercase()
            allDocs.filter {
                it.title.lowercase().contains(q) || it.fileRef.lowercase().contains(q)
            }.take(3)
        }
    }

    val matchedNotices = remember(query, allNotices) {
        if (query.isBlank()) emptyList()
        else {
            val q = query.trim().lowercase()
            allNotices.filter {
                it.title.lowercase().contains(q) || it.content.lowercase().contains(q)
            }.take(3)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Global Society Search", fontWeight = FontWeight.Bold, color = SocietyNavy)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search flats, residents, docs, notices...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (query.isNotBlank()) {
                    if (matchedUnits.isNotEmpty()) {
                        Text("Flats & Residents:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SocietyNavy)
                        matchedUnits.forEach { unit ->
                            Surface(
                                onClick = {
                                    viewModel.setSelectedUnit(unit)
                                    onNavigateToTab(ScreenTab.BUILDING_STRUCTURE)
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Room ${unit.roomNumber} (${unit.ownerName})", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                    Text("Floor ${unit.floor}", fontSize = 11.sp, color = SocietySlate)
                                }
                            }
                        }
                    }

                    if (matchedDocs.isNotEmpty()) {
                        Text("Documents:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SocietyNavy)
                        matchedDocs.forEach { doc ->
                            Surface(
                                onClick = { onNavigateToTab(ScreenTab.DOCUMENTS) },
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(doc.title, fontSize = 12.sp, modifier = Modifier.padding(10.dp))
                            }
                        }
                    }

                    if (matchedNotices.isNotEmpty()) {
                        Text("Notices:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SocietyNavy)
                        matchedNotices.forEach { notice ->
                            Surface(
                                onClick = { onNavigateToTab(ScreenTab.NOTICES) },
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(notice.title, fontSize = 12.sp, modifier = Modifier.padding(10.dp))
                            }
                        }
                    }

                    if (matchedUnits.isEmpty() && matchedDocs.isEmpty() && matchedNotices.isEmpty()) {
                        Text("No matching records found for '$query'.", fontSize = 12.sp, color = SocietySlate)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
