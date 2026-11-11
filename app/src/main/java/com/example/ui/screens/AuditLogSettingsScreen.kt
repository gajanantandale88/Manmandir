package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AuditLogEntity
import com.example.data.model.MaintenancePaymentStatus
import com.example.data.model.MaintenancePaymentWithResident
import com.example.data.model.Notice
import com.example.data.model.Payment
import com.example.data.model.Resident
import com.example.data.model.SocietySettingsEntity
import com.example.data.model.UserRole
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.localization.societyStrings
import com.example.ui.theme.*
import com.example.ui.viewmodel.SocietyViewModel

enum class AuditSettingsTab {
    SOCIETY_SETTINGS,
    AUDIT_LOGS,
    ROOM_DATABASE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditLogSettingsScreen(
    viewModel: SocietyViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    val allLogs by viewModel.allAuditLogs.collectAsState()
    val allResidents by viewModel.allResidents.collectAsState()
    val allMaintenancePaymentsWithResidents by viewModel.allMaintenancePaymentsWithResidents.collectAsState()
    val allPayments by viewModel.allPayments.collectAsState()
    val allNotices by viewModel.allNotices.collectAsState()
    val totalCollectedPayments by viewModel.totalCollectedPayments.collectAsState()
    val role by viewModel.currentRole.collectAsState()
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val s = societyStrings

    var activeTab by remember { mutableStateOf(AuditSettingsTab.SOCIETY_SETTINGS) }

    var ownerRate by remember(settings) { mutableStateOf("${settings.ownerMaintenanceRate.toInt()}") }
    var tenantRate by remember(settings) { mutableStateOf("${settings.tenantMaintenanceRate.toInt()}") }
    var penaltyRate by remember(settings) { mutableStateOf("${settings.penaltyRateMonthlyPercent.toInt()}") }
    var gracePeriod by remember(settings) { mutableStateOf("${settings.gracePeriodMonths}") }
    var openingBalance by remember(settings) { mutableStateOf("${settings.openingBalance.toInt()}") }
    var secretary by remember(settings) { mutableStateOf(settings.secretaryName) }
    var treasurer by remember(settings) { mutableStateOf(settings.treasurerName) }
    var chairman by remember(settings) { mutableStateOf(settings.chairmanName) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SocietyBg)
    ) {
        // Header
        Surface(
            color = SocietyNavy,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "${s.tabSettings} & Audit Trail",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
                Text(
                    text = "Configurable Rates, Grace Rules & Immutable Activity Logs",
                    fontSize = 11.sp,
                    color = SocietyGoldLight
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = activeTab == AuditSettingsTab.SOCIETY_SETTINGS,
                        onClick = { activeTab = AuditSettingsTab.SOCIETY_SETTINGS },
                        label = { Text(s.tabSettings, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SocietyGold,
                            selectedLabelColor = SocietyNavyDark,
                            containerColor = Color.White.copy(alpha = 0.15f),
                            labelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = activeTab == AuditSettingsTab.AUDIT_LOGS,
                        onClick = { activeTab = AuditSettingsTab.AUDIT_LOGS },
                        label = { Text("Audit Trail (${allLogs.size})", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SocietyGold,
                            selectedLabelColor = SocietyNavyDark,
                            containerColor = Color.White.copy(alpha = 0.15f),
                            labelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = activeTab == AuditSettingsTab.ROOM_DATABASE,
                        onClick = { activeTab = AuditSettingsTab.ROOM_DATABASE },
                        label = {
                            Text(
                                "Room DB (${allResidents.size + allMaintenancePaymentsWithResidents.size + allPayments.size + allNotices.size})",
                                fontSize = 11.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SocietyGold,
                            selectedLabelColor = SocietyNavyDark,
                            containerColor = Color.White.copy(alpha = 0.15f),
                            labelColor = Color.White
                        )
                    )
                }
            }
        }

        when (activeTab) {
            AuditSettingsTab.SOCIETY_SETTINGS -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 30.dp)
            ) {
                // 1. Language & Localization Preference Card
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, SocietyGoldLight),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("language_settings_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(SocietyGoldContainer, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Language,
                                        contentDescription = null,
                                        tint = SocietyGoldDark,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = s.selectLanguageTitle,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = SocietyNavy
                                    )
                                    Text(
                                        text = "${s.languageLabel}: ${currentLanguage.nativeName} (${currentLanguage.displayName})",
                                        fontSize = 11.sp,
                                        color = SocietySlate
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    Triple(AppLanguage.ENGLISH, "English", "Default (EN)"),
                                    Triple(AppLanguage.MARATHI, "मराठी", "मराठी (MR)"),
                                    Triple(AppLanguage.HINDI, "हिंदी", "हिंदी (HI)")
                                ).forEach { (lang, name, sub) ->
                                    val isSelected = currentLanguage == lang
                                    Surface(
                                        onClick = { viewModel.setLanguage(lang) },
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) SocietyGoldContainer else SocietyBg,
                                        border = BorderStroke(
                                            1.5.dp,
                                            if (isSelected) SocietyGoldDark else SocietyCardBorder
                                        ),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = name,
                                                    fontSize = 13.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSelected) SocietyNavyDark else SocietyNavy
                                                )
                                                if (isSelected) {
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Icon(
                                                        Icons.Default.CheckCircle,
                                                        contentDescription = null,
                                                        tint = SocietyGoldDark,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = sub,
                                                fontSize = 9.sp,
                                                color = if (isSelected) SocietyNavyLight else SocietySlate
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, SocietyCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                "Maintenance & Penalty Computation Parameters",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = SocietyNavy
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = ownerRate,
                                    onValueChange = { ownerRate = it },
                                    label = { Text("Owner Rate (₹/mo)") },
                                    singleLine = true,
                                    enabled = role != UserRole.RESIDENT,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = tenantRate,
                                    onValueChange = { tenantRate = it },
                                    label = { Text("Tenant Rate (₹/mo)") },
                                    singleLine = true,
                                    enabled = role != UserRole.RESIDENT,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = penaltyRate,
                                    onValueChange = { penaltyRate = it },
                                    label = { Text("Penalty (% / month)") },
                                    singleLine = true,
                                    enabled = role != UserRole.RESIDENT,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = gracePeriod,
                                    onValueChange = { gracePeriod = it },
                                    label = { Text("Grace Period (Months)") },
                                    singleLine = true,
                                    enabled = role != UserRole.RESIDENT,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            OutlinedTextField(
                                value = openingBalance,
                                onValueChange = { openingBalance = it },
                                label = { Text("Opening Society Bank Balance (₹)") },
                                singleLine = true,
                                enabled = role != UserRole.RESIDENT,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, SocietyCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                "Managing Committee Authorised Signatories",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = SocietyNavy
                            )

                            OutlinedTextField(
                                value = secretary,
                                onValueChange = { secretary = it },
                                label = { Text("Hon. Secretary") },
                                singleLine = true,
                                enabled = role != UserRole.RESIDENT,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = treasurer,
                                onValueChange = { treasurer = it },
                                label = { Text("Hon. Treasurer") },
                                singleLine = true,
                                enabled = role != UserRole.RESIDENT,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = chairman,
                                onValueChange = { chairman = it },
                                label = { Text("Hon. Chairman") },
                                singleLine = true,
                                enabled = role != UserRole.RESIDENT,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                if (role != UserRole.RESIDENT) {
                    item {
                        Button(
                            onClick = {
                                val newSettings = settings.copy(
                                    ownerMaintenanceRate = ownerRate.toDoubleOrNull() ?: settings.ownerMaintenanceRate,
                                    tenantMaintenanceRate = tenantRate.toDoubleOrNull() ?: settings.tenantMaintenanceRate,
                                    penaltyRateMonthlyPercent = penaltyRate.toDoubleOrNull() ?: settings.penaltyRateMonthlyPercent,
                                    gracePeriodMonths = gracePeriod.toIntOrNull() ?: settings.gracePeriodMonths,
                                    openingBalance = openingBalance.toDoubleOrNull() ?: settings.openingBalance,
                                    secretaryName = secretary,
                                    treasurerName = treasurer,
                                    chairmanName = chairman
                                )
                                viewModel.updateSettings(newSettings)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SocietyNavy),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("save_society_settings_button")
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save Society Governance Settings", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            }
            AuditSettingsTab.AUDIT_LOGS -> {
                // Audit Log View
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 30.dp)
                ) {
                    if (allLogs.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No audit log entries recorded yet.", color = SocietySlate, fontSize = 13.sp)
                            }
                        }
                    } else {
                        items(allLogs, key = { it.id }) { log ->
                            AuditLogCardItem(log = log)
                        }
                    }
                }
            }
            AuditSettingsTab.ROOM_DATABASE -> {
                RoomDatabaseInspectView(
                    allResidents = allResidents,
                    allMaintenancePaymentsWithResidents = allMaintenancePaymentsWithResidents,
                    allPayments = allPayments,
                    allNotices = allNotices,
                    totalCollected = totalCollectedPayments
                )
            }
        }
    }
}

@Composable
fun RoomDatabaseInspectView(
    allResidents: List<Resident>,
    allMaintenancePaymentsWithResidents: List<MaintenancePaymentWithResident>,
    allPayments: List<Payment>,
    allNotices: List<Notice>,
    totalCollected: Double
) {
    var selectedEntityTab by remember { mutableStateOf("MAINTENANCE_PAYMENTS") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 30.dp)
    ) {
        // Room Database Architecture Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, SocietyGold),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SocietyNavy,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Storage, contentDescription = null, tint = SocietyGold, modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Room Database (SQLite v3)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SocietyNavy)
                                Text("manmandir_society_db • ForeignKey Linked", fontSize = 11.sp, color = SocietySlate)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFFDCFCE7)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(shape = androidx.compose.foundation.shape.CircleShape, color = Color(0xFF16A34A), modifier = Modifier.size(6.dp)) {}
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Local SQLite Synced", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "Local persistent Room database tracking MaintenancePayment dates, amounts, and statuses per unit, linked via ForeignKey (residentId) to Resident entities.",
                        fontSize = 11.sp,
                        color = SocietySlate
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    // 4 Metric Pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SocietySurfaceVariant,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Resident", fontSize = 9.sp, color = SocietySlate, fontWeight = FontWeight.SemiBold)
                                Text("${allResidents.size}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SocietyNavy)
                                Text("residents", fontSize = 8.sp, color = SocietySlate)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SocietySurfaceVariant,
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Maint.Pay", fontSize = 9.sp, color = SocietySlate, fontWeight = FontWeight.SemiBold)
                                Text("${allMaintenancePaymentsWithResidents.size}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SocietyNavy)
                                Text("FK -> residentId", fontSize = 8.sp, color = SocietySlate)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SocietySurfaceVariant,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Receipts", fontSize = 9.sp, color = SocietySlate, fontWeight = FontWeight.SemiBold)
                                Text("${allPayments.size}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SocietyNavy)
                                Text("payments", fontSize = 8.sp, color = SocietySlate)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SocietySurfaceVariant,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Notice", fontSize = 9.sp, color = SocietySlate, fontWeight = FontWeight.SemiBold)
                                Text("${allNotices.size}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SocietyNavy)
                                Text("notices", fontSize = 8.sp, color = SocietySlate)
                            }
                        }
                    }
                }
            }
        }

        // Sub Navigation Chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedEntityTab == "MAINTENANCE_PAYMENTS",
                    onClick = { selectedEntityTab = "MAINTENANCE_PAYMENTS" },
                    label = { Text("Maint.Pay (${allMaintenancePaymentsWithResidents.size})", fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SocietyNavy,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.weight(1.2f)
                )
                FilterChip(
                    selected = selectedEntityTab == "RESIDENTS",
                    onClick = { selectedEntityTab = "RESIDENTS" },
                    label = { Text("Residents (${allResidents.size})", fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SocietyNavy,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = selectedEntityTab == "PAYMENTS",
                    onClick = { selectedEntityTab = "PAYMENTS" },
                    label = { Text("Receipts (${allPayments.size})", fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SocietyNavy,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = selectedEntityTab == "NOTICES",
                    onClick = { selectedEntityTab = "NOTICES" },
                    label = { Text("Notices (${allNotices.size})", fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SocietyNavy,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.weight(0.9f)
                )
            }
        }

        // Entity Items List
        when (selectedEntityTab) {
            "MAINTENANCE_PAYMENTS" -> {
                items(allMaintenancePaymentsWithResidents, key = { it.maintenancePayment.id }) { joined ->
                    val mp = joined.maintenancePayment
                    val res = joined.resident
                    val statusColor = when (mp.status) {
                        MaintenancePaymentStatus.PAID -> SocietySuccess
                        MaintenancePaymentStatus.OVERDUE -> SocietyError
                        MaintenancePaymentStatus.PENDING -> SocietyWarning
                        else -> SocietySlate
                    }
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, SocietyCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(shape = RoundedCornerShape(4.dp), color = SocietyNavy) {
                                        Text(
                                            text = "Unit ${mp.unitNumber}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = mp.billingMonth,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SocietyNavy
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${res.name} (FK Resident #${mp.residentId})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SocietyNavyDark
                                )
                                Text(
                                    text = if (mp.paymentDate != null) "Paid: ${mp.paymentDate}" else "Due: ${mp.dueDate}",
                                    fontSize = 10.sp,
                                    color = SocietySlate
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "₹${(mp.amount + mp.penaltyAmount).toInt()}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor
                                )
                                Text(
                                    text = mp.status.name,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor
                                )
                            }
                        }
                    }
                }
            }
            "RESIDENTS" -> {
                items(allResidents, key = { it.id }) { resident ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, SocietyCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = SocietyNavy
                                    ) {
                                        Text("Flat ${resident.flatNumber}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (resident.residentType == "OWNER") SocietyGold else SocietySurfaceVariant
                                    ) {
                                        Text(resident.residentType, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SocietyNavyDark, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(resident.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SocietyNavyDark)
                                Text("${resident.phone} • Floor ${resident.floor}", fontSize = 11.sp, color = SocietySlate)
                            }

                            Text("Room ID #${resident.id}", fontSize = 10.sp, color = SocietySlate)
                        }
                    }
                }
            }
            "PAYMENTS" -> {
                items(allPayments, key = { it.id }) { payment ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, SocietyCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = SocietyNavy
                                    ) {
                                        Text("Flat ${payment.flatNumber}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(payment.paymentMonth, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = SocietyNavy)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(payment.residentName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SocietyNavyDark)
                                Text("${payment.paymentMode} • Ref: ${payment.transactionRef}", fontSize = 10.sp, color = SocietySlate)
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("₹${payment.amount.toInt()}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SocietySuccess)
                                Text(payment.paymentDate, fontSize = 10.sp, color = SocietySlate)
                            }
                        }
                    }
                }
            }
            "NOTICES" -> {
                items(allNotices, key = { it.id }) { notice ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, SocietyCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (notice.isUrgent) {
                                    Surface(shape = RoundedCornerShape(4.dp), color = SocietyError) {
                                        Text("URGENT", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                    }
                                } else {
                                    Surface(shape = RoundedCornerShape(4.dp), color = SocietyNavy) {
                                        Text(notice.category.name, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                    }
                                }
                                Text(notice.date, fontSize = 10.sp, color = SocietySlate)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(notice.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SocietyNavyDark)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(notice.content, fontSize = 11.sp, color = SocietySlate, maxLines = 2)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AuditLogCardItem(log: AuditLogEntity) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, SocietyCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = SocietyNavy
                ) {
                    Text(
                        log.action,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Text(log.formattedDateTime, fontSize = 10.sp, color = SocietySlate)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(log.details, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SocietyNavyDark)

            val prev = log.previousValue
            val next = log.newValue
            if (!prev.isNullOrBlank() || !next.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = SocietySurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "From: ${prev ?: "N/A"} → To: ${next ?: "N/A"}",
                        fontSize = 10.sp,
                        color = SocietyNavy,
                        modifier = Modifier.padding(6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text("Performed by: ${log.userName}", fontSize = 10.sp, color = SocietySlate)
        }
    }
}
