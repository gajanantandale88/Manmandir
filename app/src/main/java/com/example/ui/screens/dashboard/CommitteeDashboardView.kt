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
import com.example.ui.theme.*
import com.example.ui.viewmodel.ScreenTab
import com.example.ui.viewmodel.SocietyViewModel

@Composable
fun CommitteeDashboardView(
    viewModel: SocietyViewModel,
    userSession: UserSession,
    modifier: Modifier = Modifier
) {
    val stats by viewModel.financialStats.collectAsState()
    val allUnits by viewModel.allUnits.collectAsState()
    val roomSummaries by viewModel.roomSummaries.collectAsState()
    val notices by viewModel.allNotices.collectAsState()
    val complaints by viewModel.allComplaints.collectAsState()
    val meetings by viewModel.allMeetings.collectAsState()
    val expenses by viewModel.allExpenses.collectAsState()

    val pendingSummaries = roomSummaries.values.filter { it.status != PaymentStatus.CURRENT_PAID }
        .sortedByDescending { it.totalOutstanding }

    val openComplaints = complaints.filter { it.status != ComplaintStatus.RESOLVED && it.status != ComplaintStatus.CLOSED }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SocietyBg),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Committee Operations Hero
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SocietyNavy),
                elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("committee_operations_hero_card")
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
                                    .background(SocietyGold),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.SupervisorAccount,
                                    contentDescription = null,
                                    tint = SocietyNavyDark,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "MANAGING COMMITTEE DESK",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = SocietyGoldLight
                                )
                                Text(
                                    text = "Operations & Society Services",
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
                                text = stats.currentMonth,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Month Collections", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                            Text("₹${stats.totalCollectedCurrentMonth.toInt()}", fontSize = 22.sp, fontWeight = FontWeight.Black, color = SocietyGoldLight)
                            Text("${stats.paidMembersCount} Units Collected", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Pending Collection", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                            Text("₹${stats.totalPendingCurrentMonth.toInt()}", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color(0xFFFF8A80))
                            Text("${stats.pendingMembersCount} Units Pending", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

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

                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Recovery: ${stats.collectionRatePercent.toInt()}%", fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
                        Text("Target: ₹${stats.totalExpectedCurrentMonth.toInt()}", fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.setTab(ScreenTab.MAINTENANCE) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SocietyGold,
                                contentColor = SocietyNavyDark
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CurrencyRupee, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Record Payment", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { viewModel.setTab(ScreenTab.NOTICES) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Post Notice", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 2. Operational Highlights (Complaints + Meetings)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Complaints Card
                Card(
                    onClick = { viewModel.setTab(ScreenTab.COMPLAINTS) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, SocietyCardBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(SocietyOrange.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.SupportAgent, contentDescription = null, tint = SocietyOrange, modifier = Modifier.size(18.dp))
                            }
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = SocietyOrange
                            ) {
                                Text(
                                    text = "${openComplaints.size} Active",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Resident Tickets", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SocietyNavyDark)
                        Text("Manage service requests", fontSize = 10.sp, color = SocietySlate)
                    }
                }

                // Meetings & Agenda Card
                Card(
                    onClick = { viewModel.setTab(ScreenTab.MEETINGS_TASKS) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, SocietyCardBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(SocietyNavy.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.EventNote, contentDescription = null, tint = SocietyNavy, modifier = Modifier.size(18.dp))
                            }
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = SocietyNavy
                            ) {
                                Text(
                                    text = "Monthly MC",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Meetings & Tasks", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SocietyNavyDark)
                        Text("Minutes & resolutions", fontSize = 10.sp, color = SocietySlate)
                    }
                }
            }
        }

        // 3. Society Management Modules
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Committee Modules",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = SocietyNavy
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = SocietyGold.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "Committee Member Access",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SocietyNavyDark,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        item {
            ManagementModulesGrid(
                accessibleTabs = userSession.dashboardVisibility.accessibleModules,
                restrictedTabs = userSession.dashboardVisibility.restrictedModules,
                onNavigate = { tab -> viewModel.setTab(tab) }
            )
        }

        // 4. Pending Maintenance Collection Action Desk
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
                                Icon(Icons.Default.HourglassBottom, contentDescription = null, tint = SocietyGoldDark, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Pending Collections Queue", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SocietyNavy)
                            }
                            TextButton(onClick = { viewModel.setTab(ScreenTab.MAINTENANCE) }) {
                                Text("Collection Desk (${pendingSummaries.size})", fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

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
                                            Text("${item.pendingMonthsCount} mo. due • ₹${(item.unit?.monthlyMaintenance ?: 500.0).toInt()}/mo", fontSize = 10.sp, color = SocietySlate)
                                        }
                                    }
                                    Button(
                                        onClick = {
                                            viewModel.setSelectedUnit(item.unit)
                                            viewModel.setTab(ScreenTab.MAINTENANCE)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SocietyGold, contentColor = SocietyNavyDark),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("Collect ₹${item.totalOutstanding.toInt()}", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. Recent Expenses / Vouchers Snapshot
        if (expenses.isNotEmpty()) {
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
                                Icon(Icons.Default.Receipt, contentDescription = null, tint = SocietyNavy, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Society Expenses & Bills", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SocietyNavy)
                            }
                            TextButton(onClick = { viewModel.setTab(ScreenTab.EXPENSES) }) {
                                Text("All Vouchers", fontSize = 12.sp)
                            }
                        }

                        expenses.take(2).forEach { exp ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SocietySurfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(exp.category.name.replace("_", " "), fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = SocietyNavyDark)
                                        Text(exp.description, fontSize = 10.sp, color = SocietySlate)
                                    }
                                    Text("₹${exp.amount.toInt()}", fontWeight = FontWeight.Black, fontSize = 12.sp, color = SocietyError)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
