package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UnitType
import com.example.data.model.UserRole
import com.example.data.session.UserPrivilege
import com.example.ui.components.FinancialReportsDialog
import com.example.ui.components.MetricStatCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.FinancialStats
import com.example.ui.viewmodel.ScreenTab
import com.example.ui.viewmodel.SocietyViewModel

@Composable
fun FinancialDashboardScreen(
    viewModel: SocietyViewModel,
    modifier: Modifier = Modifier
) {
    val stats by viewModel.financialStats.collectAsState()
    val expenses by viewModel.allExpenses.collectAsState()
    val roomSummaries by viewModel.roomSummaries.collectAsState()
    val userSession by viewModel.userSession.collectAsState()

    var showFinancialReportsDialog by remember { mutableStateOf(false) }

    val canEditFinancials = userSession.privileges.contains(UserPrivilege.EDIT_FINANCIALS_EXPENSES_REVENUE) ||
            userSession.currentUser.role == UserRole.SUPER_ADMIN ||
            userSession.currentUser.role == UserRole.CHAIRMAN ||
            userSession.currentUser.role == UserRole.TREASURER

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SocietyBg),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Hero Account Balance Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SocietyNavy),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SOCIETY FINANCIAL BALANCE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SocietyGoldLight
                        )
                        if (userSession.currentUser.role == UserRole.TREASURER) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SocietyGold
                            ) {
                                Text(
                                    "Treasurer Desk",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SocietyNavyDark,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "₹${stats.closingBalance.toInt()}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "Net Available Liquid Society Funds (SBI A/c ••8912)",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.75f)
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 14.dp),
                        color = Color.White.copy(alpha = 0.15f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Opening Balance", fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
                            Text("₹${stats.openingBalance.toInt()}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                        }
                        Column {
                            Text("Total Expenses", fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
                            Text("₹${stats.totalSocietyExpenses.toInt()}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFFF8A80))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Overall Arrears", fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
                            Text("₹${stats.totalOverallOutstanding.toInt()}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SocietyGoldLight)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action buttons: Download & Print PDF Statements
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showFinancialReportsDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SocietyGold,
                                contentColor = SocietyNavyDark
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("download_statements_pdf_button")
                        ) {
                            Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("PDF Reports & Print", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        if (canEditFinancials) {
                            OutlinedButton(
                                onClick = { viewModel.setTab(ScreenTab.EXPENSES) },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.AddCard, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Manage Expenses", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Monthly Performance Block
        item {
            Text(
                text = "${stats.currentMonth} Collection Performance",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = SocietyNavy
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricStatCard(
                    title = "Target Demanded",
                    value = "₹${stats.totalExpectedCurrentMonth.toInt()}",
                    subtitle = "85 Residential Units",
                    icon = Icons.Default.TrendingUp,
                    iconColor = SocietyNavy,
                    modifier = Modifier.weight(1f)
                )
                MetricStatCard(
                    title = "Total Collected",
                    value = "₹${stats.totalCollectedCurrentMonth.toInt()}",
                    subtitle = "${stats.paidMembersCount} Paid / 85",
                    icon = Icons.Default.CheckCircle,
                    iconColor = SocietySuccess,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricStatCard(
                    title = "Pending Dues",
                    value = "₹${stats.totalPendingCurrentMonth.toInt()}",
                    subtitle = "${stats.pendingMembersCount} Members",
                    icon = Icons.Default.PendingActions,
                    iconColor = SocietyError,
                    modifier = Modifier.weight(1f)
                )
                MetricStatCard(
                    title = "Accrued Penalties",
                    value = "₹${stats.totalOverallPenalties.toInt()}",
                    subtitle = "22% / month rule",
                    icon = Icons.Default.Percent,
                    iconColor = SocietyWarning,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Collection Progress & Rate
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
                        Text("Recovery Efficiency", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SocietyNavy)
                        Text("${stats.collectionRatePercent.toInt()}%", fontWeight = FontWeight.Black, fontSize = 16.sp, color = SocietyNavy)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val progress = (stats.collectionRatePercent / 100.0).toFloat().coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        color = SocietyNavy,
                        trackColor = SocietySurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${stats.paidMembersCount} members have cleared August dues. ${stats.pendingMembersCount} accounts pending reminder follow-ups.",
                        fontSize = 11.sp,
                        color = SocietySlate
                    )
                }
            }
        }

        // Floor-wise Collection Rank
        item {
            Text(
                text = "Floor-Wise Collection Ranking (Floors 1 to 11)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = SocietyNavy
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, SocietyCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    (1..11).forEach { floor ->
                        val floorSummaries = roomSummaries.values.filter {
                            it.roomNumber / 100 == floor && it.unit?.unitType == UnitType.RESIDENTIAL
                        }
                        val totalInFloor = floorSummaries.size
                        val paidInFloor = floorSummaries.count { it.totalOutstanding == 0.0 }
                        val pendingAmt = floorSummaries.sumOf { it.totalOutstanding }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = SocietyNavy
                                ) {
                                    Text(
                                        "F$floor",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "$paidInFloor of $totalInFloor Paid",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SocietyNavyDark
                                )
                            }

                            Text(
                                text = if (pendingAmt > 0) "Pending: ₹${pendingAmt.toInt()}" else "100% Cleared ✓",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (pendingAmt > 0) SocietyError else SocietySuccess
                            )
                        }
                        if (floor < 11) {
                            HorizontalDivider(color = SocietyCardBorder.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }
    }

    if (showFinancialReportsDialog) {
        FinancialReportsDialog(
            viewModel = viewModel,
            onDismiss = { showFinancialReportsDialog = false }
        )
    }
}
