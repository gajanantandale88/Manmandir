package com.example.ui.screens.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.model.OccupancyType
import com.example.data.model.PaymentStatus
import com.example.data.session.UserSession
import com.example.ui.components.BuildingStructureOverviewCard
import com.example.ui.components.ManagementModulesGrid
import com.example.ui.components.ResidentDocumentStatusCard
import com.example.ui.localization.societyStrings
import com.example.ui.theme.*
import com.example.ui.viewmodel.ScreenTab
import com.example.ui.viewmodel.SocietyViewModel

@Composable
fun ResidentDashboardView(
    viewModel: SocietyViewModel,
    userSession: UserSession,
    modifier: Modifier = Modifier
) {
    val residentRoom by viewModel.currentResidentRoom.collectAsState()
    val allUnits by viewModel.allUnits.collectAsState()
    val roomSummaries by viewModel.roomSummaries.collectAsState()
    val notices by viewModel.allNotices.collectAsState()
    val complaints by viewModel.allComplaints.collectAsState()
    val allReceipts by viewModel.allReceipts.collectAsState()

    val activeRoom = userSession.currentUser.roomNumber ?: residentRoom
    val residentUnit = allUnits.find { it.roomNumber == activeRoom }
    val residentSummary = roomSummaries[activeRoom]

    val myReceipts = allReceipts.filter { it.roomNumber == activeRoom }
    val myComplaints = complaints.filter { it.roomNumber == activeRoom }
    val s = societyStrings

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SocietyBg),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Resident Personal Flat Maintenance Hero Card
        item {
            ResidentFlatHeroCard(
                unit = residentUnit,
                summary = residentSummary,
                activeRoom = activeRoom,
                onPayClick = {
                    viewModel.setSelectedUnit(residentUnit)
                    viewModel.setTab(ScreenTab.MAINTENANCE)
                },
                onViewPenaltyClick = {
                    viewModel.setSelectedRoomForPenaltyView(residentSummary)
                },
                onViewReceiptsClick = { viewModel.setTab(ScreenTab.RECEIPTS) }
            )
        }

        // 2. Urgent Notices Broadcast
        val urgentNotice = notices.find { it.isUrgent }
        if (urgentNotice != null) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SocietyErrorContainer),
                    border = BorderStroke(1.dp, SocietyError.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SocietyError.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Campaign,
                                contentDescription = null,
                                tint = SocietyError,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "URGENT ANNOUNCEMENT • ${urgentNotice.date}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = SocietyError
                            )
                            Text(
                                text = urgentNotice.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = SocietyNavyDark
                            )
                            Text(
                                text = urgentNotice.content,
                                fontSize = 11.sp,
                                color = SocietySlate,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(onClick = { viewModel.setTab(ScreenTab.NOTICES) }) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "View", tint = SocietyError)
                        }
                    }
                }
            }
        }

        // 3. Quick Actions Row (Raise Complaint & View Directory)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
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
                            Icon(Icons.Default.AddComment, contentDescription = null, tint = SocietyOrange, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Raise Ticket", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SocietyNavyDark)
                            Text("${myComplaints.size} Active", fontSize = 10.sp, color = SocietySlate)
                        }
                    }
                }

                Card(
                    onClick = { viewModel.setTab(ScreenTab.DIRECTORY) },
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
                                .background(SocietyNavy.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Contacts, contentDescription = null, tint = SocietyNavy, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Committee Help", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SocietyNavyDark)
                            Text("Secretary & Reps", fontSize = 10.sp, color = SocietySlate)
                        }
                    }
                }
            }
        }

        // 4. Resident Accessible Modules Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Member Portal Modules",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = SocietyNavy
                )
                Text(
                    text = "Resident View",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SocietySlate
                )
            }
        }

        item {
            ManagementModulesGrid(
                accessibleTabs = userSession.dashboardVisibility.accessibleModules,
                restrictedTabs = userSession.dashboardVisibility.restrictedModules,
                onNavigate = { tab -> viewModel.setTab(tab) }
            )
        }

        // 5. Recent Digital Receipts for this Flat
        if (myReceipts.isNotEmpty()) {
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
                                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = SocietyNavy, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("My Digital Receipts", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SocietyNavy)
                            }
                            TextButton(onClick = { viewModel.setTab(ScreenTab.RECEIPTS) }) {
                                Text("View All (${myReceipts.size})", fontSize = 12.sp)
                            }
                        }

                        val latest = myReceipts.first()
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SocietySuccessContainer,
                            border = BorderStroke(1.dp, SocietySuccess.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Receipt #${latest.receiptNumber}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SocietyNavyDark)
                                    Text("₹${latest.totalAmountReceived.toInt()} paid on ${latest.date}", fontSize = 11.sp, color = SocietySlate)
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = SocietySuccess
                                ) {
                                    Text("VERIFIED", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.White, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // 6. Resident Statutory Document Compliance Status
        item {
            ResidentDocumentStatusCard(
                unit = residentUnit,
                onViewDocs = { viewModel.setTab(ScreenTab.DOCUMENTS) }
            )
        }
    }
}

@Composable
fun ResidentFlatHeroCard(
    unit: com.example.data.model.UnitEntity?,
    summary: com.example.data.repository.RoomMaintenanceSummary?,
    activeRoom: Int,
    onPayClick: () -> Unit,
    onViewPenaltyClick: () -> Unit,
    onViewReceiptsClick: () -> Unit
) {
    val isOwner = unit?.occupancyType != OccupancyType.TENANT
    val residentName = if (isOwner) unit?.ownerName ?: "Member" else unit?.tenantName ?: "Resident"
    val monthlyRate = unit?.monthlyMaintenance ?: (if (isOwner) 500.0 else 750.0)
    val status = summary?.status ?: PaymentStatus.CURRENT_PAID
    val totalDue = summary?.totalOutstanding ?: 0.0
    val penalty = summary?.totalPenalty ?: 0.0
    val s = societyStrings

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SocietyNavy),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("resident_flat_hero_card")
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
                Column {
                    Text(
                        text = s.roleResident.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = SocietyGoldLight
                    )
                    Text(
                        text = residentName,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = if (isOwner) "Registered Flat Owner" else "Tenant (₹750/mo rate)",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SocietyGold
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = s.flatPrefix.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = SocietyNavyDark
                        )
                        Text(
                            text = "$activeRoom",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = SocietyNavyDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dues Breakdown
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color.White.copy(alpha = 0.08f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(s.tabMaintenance, fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
                        if (totalDue == 0.0) {
                            Text(s.statusPaid, fontSize = 18.sp, fontWeight = FontWeight.Black, color = SocietySuccess)
                            Text("All dues cleared up to current month", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                        } else {
                            Text("₹${totalDue.toInt()} Due", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFFFF8A80))
                            if (penalty > 0) {
                                Text("Includes ₹${penalty.toInt()} late penalty (21% p.a.)", fontSize = 10.sp, color = Color(0xFFFF8A80))
                            } else {
                                Text("Monthly rate: ₹${monthlyRate.toInt()}/mo", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                            }
                        }
                    }

                    if (totalDue > 0.0) {
                        Button(
                            onClick = onPayClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SocietyGold,
                                contentColor = SocietyNavyDark
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(s.payNowButton, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    } else {
                        OutlinedButton(
                            onClick = onViewReceiptsClick,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(s.tabReceipts, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
