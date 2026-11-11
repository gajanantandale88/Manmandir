package com.example.ui.screens.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ComplaintStatus
import com.example.data.model.PaymentStatus
import com.example.data.session.UserSession
import com.example.ui.components.BuildingStructureOverviewCard
import com.example.ui.components.ManagementModulesGrid
import com.example.ui.localization.societyStrings
import com.example.ui.theme.*
import com.example.ui.viewmodel.FinancialStats
import com.example.ui.viewmodel.ScreenTab
import com.example.ui.viewmodel.SocietyViewModel

@Composable
fun AdminDashboardView(
    viewModel: SocietyViewModel,
    userSession: UserSession,
    modifier: Modifier = Modifier
) {
    val stats by viewModel.financialStats.collectAsState()
    val allUnits by viewModel.allUnits.collectAsState()
    val roomSummaries by viewModel.roomSummaries.collectAsState()
    val notices by viewModel.allNotices.collectAsState()
    val complaints by viewModel.allComplaints.collectAsState()
    val allAuditLogs by viewModel.allAuditLogs.collectAsState()
    val allReceipts by viewModel.allReceipts.collectAsState()
    val s = societyStrings

    val pendingSummaries = roomSummaries.values.filter { it.status != PaymentStatus.CURRENT_PAID }
        .sortedByDescending { it.totalOutstanding }

    val openComplaints = complaints.filter { it.status == ComplaintStatus.SUBMITTED || it.status == ComplaintStatus.IN_PROGRESS || it.status == ComplaintStatus.ASSIGNED }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SocietyBg),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Super Admin Executive Hero Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SocietyNavyDark),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_executive_hero_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF7B1FA2)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "${s.roleSuperAdmin.uppercase()} ${s.overviewTitle.uppercase()}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = SocietyGoldLight
                                )
                                Text(
                                    text = "${s.appName} CHS",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Wing A (85 Units)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Master Financial Metrics Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Treasury Bank Balance", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                            Text("₹18,45,200", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.White)
                            Text("SBI A/c ••8912 (Active)", fontSize = 10.sp, color = SocietyGoldLight)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(s.collectedThisMonth, fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                            Text("₹${stats.totalCollectedCurrentMonth.toInt()}", fontSize = 20.sp, fontWeight = FontWeight.Black, color = SocietyGold)
                            Text("${stats.paidMembersCount} of 85 units paid", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Progress Bar
                    val progress = (stats.collectionRatePercent / 100.0).toFloat().coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = SocietyGold,
                        trackColor = Color.White.copy(alpha = 0.2f)
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${s.collectionRate}: ${stats.collectionRatePercent.toInt()}%", fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
                        Text("Pending Dues: ₹${stats.totalPendingCurrentMonth.toInt()}", fontSize = 10.sp, color = Color(0xFFFF8A80), fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.setTab(ScreenTab.FINANCIALS) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SocietyGold,
                                contentColor = SocietyNavyDark
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(s.tabFinancials, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { viewModel.setTab(ScreenTab.AUDIT_SETTINGS) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(s.tabSettings, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 2. High Priority Operational Quick Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Pending Arrears Card
                Card(
                    onClick = { viewModel.setTab(ScreenTab.MAINTENANCE) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, SocietyCardBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SocietyError.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.HourglassBottom, contentDescription = null, tint = SocietyError, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("${pendingSummaries.size} Defaulters", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SocietyNavyDark)
                            Text("₹${stats.totalPendingCurrentMonth.toInt()} pending", fontSize = 10.sp, color = SocietySlate)
                        }
                    }
                }

                // Open Complaints Card
                Card(
                    onClick = { viewModel.setTab(ScreenTab.COMPLAINTS) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, SocietyCardBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SocietyOrange.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.SupportAgent, contentDescription = null, tint = SocietyOrange, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("${openComplaints.size} Complaints", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SocietyNavyDark)
                            Text("Needs Attention", fontSize = 10.sp, color = SocietySlate)
                        }
                    }
                }
            }
        }

        // 3. Society Modules Grid (Full Access for Admin)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Executive Management Modules",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = SocietyNavy
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF7B1FA2).copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "Full Super Admin Access (13/13)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7B1FA2),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        item {
            ManagementModulesGrid(
                accessibleTabs = userSession.dashboardVisibility.accessibleModules,
                restrictedTabs = emptyList(),
                onNavigate = { tab -> viewModel.setTab(tab) }
            )
        }

        // 4. Defaulters Action Desk
        if (pendingSummaries.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, SocietyCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.WarningAmber, contentDescription = null, tint = SocietyError, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Pending Defaulters Snapshot", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SocietyNavy)
                            }
                            TextButton(onClick = { viewModel.setTab(ScreenTab.MAINTENANCE) }) {
                                Text("View All (${pendingSummaries.size})", fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        pendingSummaries.take(3).forEach { item ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = SocietySurfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = SocietyNavyDark
                                        ) {
                                            Text("Flat ${item.roomNumber}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(item.unit?.ownerName ?: "Member", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = SocietyNavyDark)
                                            Text("${item.pendingMonthsCount} months unpaid • Penalty ₹${item.totalPenalty.toInt()}", fontSize = 10.sp, color = SocietyError)
                                        }
                                    }
                                    Text("₹${item.totalOutstanding.toInt()}", fontWeight = FontWeight.Black, fontSize = 13.sp, color = SocietyError)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. System Governance & Audit Trail
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, SocietyCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF7B1FA2), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("System Audit Trail & Integrity", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SocietyNavy)
                        }
                        TextButton(onClick = { viewModel.setTab(ScreenTab.AUDIT_SETTINGS) }) {
                            Text("Full Logs (${allAuditLogs.size})", fontSize = 12.sp)
                        }
                    }
                    Text(
                        text = "Real-time ledger and modification tracking active across all 85 units.",
                        fontSize = 11.sp,
                        color = SocietySlate
                    )
                    if (allAuditLogs.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        allAuditLogs.take(2).forEach { log ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SocietySurfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.History, contentDescription = null, tint = SocietySlate, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(log.action, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = SocietyNavyDark)
                                        Text("By ${log.userName} • ${log.formattedDateTime}", fontSize = 9.sp, color = SocietySlate)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 6. Building Structure Snapshot
        item {
            BuildingStructureOverviewCard(
                onExploreClick = { viewModel.setTab(ScreenTab.BUILDING_STRUCTURE) }
            )
        }
    }
}
