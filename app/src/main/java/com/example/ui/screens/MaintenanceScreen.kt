package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MaintenancePaymentStatus
import com.example.data.model.OccupancyType
import com.example.data.model.PaymentStatus
import com.example.data.model.ResidentWithMaintenancePayments
import com.example.data.model.UnitType
import com.example.data.model.UserRole
import com.example.data.repository.RoomMaintenanceSummary
import com.example.ui.components.PenaltyBreakdownDialog
import com.example.ui.components.ResidentMaintenanceLedgerDialog
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.ScreenTab
import com.example.ui.viewmodel.SocietyViewModel

enum class MaintenanceFilterTab {
    ALL_MEMBERS,
    PENDING_ONLY,
    PENALTY_ACTIVE,
    PAID_ONLY
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceScreen(
    viewModel: SocietyViewModel,
    modifier: Modifier = Modifier
) {
    val roomSummaries by viewModel.roomSummaries.collectAsState()
    val residentsWithPayments by viewModel.residentsWithMaintenancePayments.collectAsState()
    val role by viewModel.currentRole.collectAsState()
    val userSession by viewModel.userSession.collectAsState()
    val selectedSummaryForPenalty by viewModel.selectedRoomForPenaltyView.collectAsState()
    val stats by viewModel.financialStats.collectAsState()

    var filterTab by remember { mutableStateOf(MaintenanceFilterTab.ALL_MEMBERS) }
    var selectedFloorFilter by remember { mutableStateOf<Int?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showGenerateCycleDialog by remember { mutableStateOf(false) }
    var recordPaymentSummary by remember { mutableStateOf<RoomMaintenanceSummary?>(null) }
    var selectedUnitForLedger by remember { mutableStateOf<String?>(null) }

    val residentWithPaymentsByUnit = remember(residentsWithPayments) {
        residentsWithPayments.associateBy { it.resident.flatNumber }
    }

    val canEditFinancials = userSession.privileges.contains(com.example.data.session.UserPrivilege.EDIT_FINANCIALS_EXPENSES_REVENUE) ||
            userSession.currentUser.role == UserRole.SUPER_ADMIN ||
            userSession.currentUser.role == UserRole.CHAIRMAN ||
            userSession.currentUser.role == UserRole.TREASURER

    val residentialSummaries = remember(roomSummaries, filterTab, selectedFloorFilter, searchQuery) {
        roomSummaries.values
            .filter { it.unit?.unitType == UnitType.RESIDENTIAL }
            .filter { summary ->
                when (filterTab) {
                    MaintenanceFilterTab.ALL_MEMBERS -> true
                    MaintenanceFilterTab.PENDING_ONLY -> summary.totalOutstanding > 0
                    MaintenanceFilterTab.PENALTY_ACTIVE -> summary.totalPenalty > 0
                    MaintenanceFilterTab.PAID_ONLY -> summary.totalOutstanding == 0.0
                }
            }
            .filter { summary ->
                selectedFloorFilter == null || (summary.roomNumber / 100) == selectedFloorFilter
            }
            .filter { summary ->
                if (searchQuery.isBlank()) true
                else {
                    val q = searchQuery.trim().lowercase()
                    val roomStr = summary.roomNumber.toString()
                    val owner = summary.unit?.ownerName?.lowercase() ?: ""
                    val tenant = summary.unit?.tenantName?.lowercase() ?: ""
                    roomStr.contains(q) || owner.contains(q) || tenant.contains(q)
                }
            }
            .sortedBy { it.roomNumber }
    }

    Scaffold(
        floatingActionButton = {
            if (canEditFinancials) {
                FloatingActionButton(
                    onClick = { showGenerateCycleDialog = true },
                    containerColor = SocietyNavy,
                    contentColor = SocietyGoldLight,
                    modifier = Modifier.testTag("generate_cycle_fab")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Autorenew, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("New Month Cycle", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(SocietyBg)
        ) {
            // Header stats banner
            Surface(
                color = SocietyNavy,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Monthly Maintenance & Dues",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Owner: ₹500/mo • Tenant: ₹750/mo • 22% Penalty rule",
                                fontSize = 11.sp,
                                color = SocietyGoldLight
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = SocietyGold
                        ) {
                            Text(
                                text = "${residentialSummaries.size} Flats",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SocietyNavyDark,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Metrics Strip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MaintenanceStatPill("Expected", "₹${stats.totalExpectedCurrentMonth.toInt()}", Color.White, modifier = Modifier.weight(1f))
                        MaintenanceStatPill("Collected", "₹${stats.totalCollectedCurrentMonth.toInt()}", SocietyGoldLight, modifier = Modifier.weight(1f))
                        MaintenanceStatPill("Pending", "₹${stats.totalPendingCurrentMonth.toInt()}", Color(0xFFFF8A80), modifier = Modifier.weight(1f))
                        MaintenanceStatPill("Penalties", "₹${stats.totalOverallPenalties.toInt()}", Color(0xFFFFD54F), modifier = Modifier.weight(1f))
                    }
                }
            }

            // Search Bar & Filter Chips
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by room (e.g. 205) or resident name...", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SocietySlate) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                )

                // Category Filter Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = filterTab == MaintenanceFilterTab.ALL_MEMBERS,
                        onClick = { filterTab = MaintenanceFilterTab.ALL_MEMBERS },
                        label = { Text("All (85)", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = filterTab == MaintenanceFilterTab.PENDING_ONLY,
                        onClick = { filterTab = MaintenanceFilterTab.PENDING_ONLY },
                        label = { Text("Pending", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = filterTab == MaintenanceFilterTab.PENALTY_ACTIVE,
                        onClick = { filterTab = MaintenanceFilterTab.PENALTY_ACTIVE },
                        label = { Text("Penalty", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = filterTab == MaintenanceFilterTab.PAID_ONLY,
                        onClick = { filterTab = MaintenanceFilterTab.PAID_ONLY },
                        label = { Text("Paid", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Floor Filters
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedFloorFilter == null,
                            onClick = { selectedFloorFilter = null },
                            label = { Text("All Floors", fontSize = 10.sp) }
                        )
                    }
                    items((1..11).toList()) { floor ->
                        FilterChip(
                            selected = selectedFloorFilter == floor,
                            onClick = {
                                selectedFloorFilter = if (selectedFloorFilter == floor) null else floor
                            },
                            label = { Text("F$floor", fontSize = 10.sp) }
                        )
                    }
                }
            }

            // List of Members
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                if (residentialSummaries.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No residential maintenance records match the selected filter.",
                                color = SocietySlate,
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    items(residentialSummaries, key = { it.roomNumber }) { summary ->
                        val linkedData = residentWithPaymentsByUnit[summary.roomNumber.toString()]
                        MaintenanceDemandCard(
                            summary = summary,
                            linkedResidentPayments = linkedData,
                            isCommittee = role != UserRole.RESIDENT,
                            onRecordPayment = { recordPaymentSummary = summary },
                            onViewUnitLedger = {
                                if (linkedData != null) {
                                    selectedUnitForLedger = summary.roomNumber.toString()
                                }
                            },
                            onViewPenaltyCalculation = {
                                viewModel.setSelectedRoomForPenaltyView(summary)
                            },
                            onSendReminder = {
                                val penaltyNotice = if (summary.totalPenalty > 0) " including ₹${summary.totalPenalty.toInt()} penalty." else "."
                                viewModel.sendReminderAlert(
                                    summary.roomNumber,
                                    "Your maintenance payment of ₹${summary.totalOutstanding.toInt()} for Room ${summary.roomNumber} is pending$penaltyNotice"
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    // Penalty Calculation Breakdown Modal
    if (selectedSummaryForPenalty != null) {
        PenaltyBreakdownDialog(
            summary = selectedSummaryForPenalty!!,
            isCommittee = role != UserRole.RESIDENT,
            onDismiss = { viewModel.setSelectedRoomForPenaltyView(null) },
            onOverridePenalty = { demandId, newPenalty, reason ->
                viewModel.overridePenalty(
                    demandId,
                    selectedSummaryForPenalty!!.roomNumber,
                    "August 2026",
                    newPenalty,
                    reason
                )
            }
        )
    }

    // Payment Recording Dialog
    if (recordPaymentSummary != null) {
        RecordPaymentDialog(
            summary = recordPaymentSummary!!,
            onDismiss = { recordPaymentSummary = null },
            onConfirmPayment = { amount, method, txnRef, penaltyPaid, remarks ->
                viewModel.recordPayment(
                    roomNumber = recordPaymentSummary!!.roomNumber,
                    monthYear = "August 2026",
                    amount = amount,
                    paymentMethod = method,
                    transactionRef = txnRef,
                    penaltyPaid = penaltyPaid,
                    remarks = remarks
                )
                recordPaymentSummary = null
            }
        )
    }

    // Generate New Monthly Cycle Dialog
    if (showGenerateCycleDialog) {
        GenerateCycleDialog(
            onDismiss = { showGenerateCycleDialog = false },
            onGenerate = { monthName, monthIndex, dueDate ->
                viewModel.generateNextMonthDemands(monthName, monthIndex, dueDate)
                showGenerateCycleDialog = false
            }
        )
    }

    // Linked Resident MaintenancePayment Ledger Dialog
    val activeLedgerData = selectedUnitForLedger?.let { residentWithPaymentsByUnit[it] }
    if (activeLedgerData != null) {
        ResidentMaintenanceLedgerDialog(
            residentWithPayments = activeLedgerData,
            canEdit = canEditFinancials,
            onDismiss = { selectedUnitForLedger = null },
            onAddPayment = { newPayment ->
                viewModel.addMaintenancePayment(newPayment)
            },
            onMarkPaid = { pendingPayment ->
                viewModel.updateMaintenancePayment(
                    pendingPayment.copy(
                        status = MaintenancePaymentStatus.PAID,
                        amountPaid = pendingPayment.amount + pendingPayment.penaltyAmount,
                        paymentDate = "02-Nov-2026",
                        paymentMethod = "UPI",
                        transactionReference = "UPI/${pendingPayment.unitNumber}/${(1000..9999).random()}"
                    )
                )
            }
        )
    }
}

@Composable
private fun MaintenanceStatPill(
    label: String,
    value: String,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color.White.copy(alpha = 0.1f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 9.sp, color = Color.White.copy(alpha = 0.7f))
            Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textColor)
        }
    }
}

@Composable
fun MaintenanceDemandCard(
    summary: RoomMaintenanceSummary,
    linkedResidentPayments: ResidentWithMaintenancePayments? = null,
    isCommittee: Boolean,
    onRecordPayment: () -> Unit,
    onViewUnitLedger: () -> Unit = {},
    onViewPenaltyCalculation: () -> Unit,
    onSendReminder: () -> Unit
) {
    val isOwner = summary.unit?.occupancyType == OccupancyType.OWNER
    val residentName = linkedResidentPayments?.resident?.name
        ?: (if (isOwner) summary.unit?.ownerName ?: "Resident" else summary.unit?.tenantName ?: "Tenant")
    val paidHistoryCount = linkedResidentPayments?.maintenancePayments?.count { it.status == MaintenancePaymentStatus.PAID } ?: 0
    val totalHistoryCount = linkedResidentPayments?.maintenancePayments?.size ?: 0

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, if (summary.totalPenalty > 0) SocietyError.copy(alpha = 0.3f) else SocietyCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SocietyNavy
                    ) {
                        Text(
                            text = "${summary.roomNumber}",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = residentName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = SocietyNavyDark
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isOwner) "Owner (₹500/mo)" else "Tenant (₹750/mo)",
                                fontSize = 11.sp,
                                color = SocietySlate
                            )
                            if (linkedResidentPayments != null) {
                                Text(" • ", fontSize = 11.sp, color = SocietySlate)
                                Text(
                                    text = "$paidHistoryCount/$totalHistoryCount Paid",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SocietyNavy
                                )
                            }
                            if (summary.pendingMonthsCount > 0) {
                                Text(" • ", fontSize = 11.sp, color = SocietySlate)
                                Text(
                                    text = "${summary.pendingMonthsCount} Mo Pending",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (summary.pendingMonthsCount >= 2) SocietyError else SocietyWarning
                                )
                            }
                        }
                    }
                }

                StatusBadge(status = summary.status)
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = SocietyCardBorder
            )

            // Dues Breakdown Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Base Maintenance Due", fontSize = 10.sp, color = SocietySlate)
                    Text("₹${summary.totalBasePending.toInt()}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SocietyNavy)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Accrued Penalty (22%)", fontSize = 10.sp, color = SocietySlate)
                    Text(
                        text = if (summary.totalPenalty > 0) "+ ₹${summary.totalPenalty.toInt()}" else "₹0",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (summary.totalPenalty > 0) SocietyError else SocietySuccess
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Total Outstanding", fontSize = 10.sp, color = SocietySlate)
                    Text(
                        text = "₹${summary.totalOutstanding.toInt()}",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = if (summary.totalOutstanding > 0) SocietyError else SocietySuccess
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = onViewUnitLedger,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                    modifier = Modifier
                        .weight(0.9f)
                        .testTag("unit_ledger_btn_${summary.roomNumber}")
                ) {
                    Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ledger", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = onViewPenaltyCalculation,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                    modifier = Modifier.weight(0.95f)
                ) {
                    Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Penalty", fontSize = 11.sp)
                }

                if (summary.totalOutstanding > 0) {
                    if (isCommittee) {
                        OutlinedButton(
                            onClick = onSendReminder,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.weight(0.9f)
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reminder", fontSize = 11.sp)
                        }
                    }

                    Button(
                        onClick = onRecordPayment,
                        colors = ButtonDefaults.buttonColors(containerColor = SocietyNavy),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isCommittee) "Record Pay" else "Pay Dues", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// Record Payment Dialog
@Composable
fun RecordPaymentDialog(
    summary: RoomMaintenanceSummary,
    onDismiss: () -> Unit,
    onConfirmPayment: (amount: Double, method: String, txnRef: String, penaltyPaid: Double, remarks: String) -> Unit
) {
    val totalOutstanding = summary.totalOutstanding
    var amountText by remember { mutableStateOf("${totalOutstanding.toInt()}") }
    var selectedMethod by remember { mutableStateOf("UPI (GooglePay / PhonePe)") }
    var transactionRef by remember { mutableStateOf("UPI/${System.currentTimeMillis() % 1000000}/${summary.roomNumber}") }
    var remarks by remember { mutableStateOf("Maintenance received on time with thanks.") }

    val methods = listOf("UPI (GooglePay / PhonePe)", "Bank NEFT / RTGS", "Cash", "Cheque", "IMPS")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Record Maintenance Payment",
                    fontWeight = FontWeight.Bold,
                    color = SocietyNavy
                )
                Text(
                    text = "Room ${summary.roomNumber} (${summary.unit?.ownerName ?: "Resident"})",
                    fontSize = 12.sp,
                    color = SocietySlate
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(androidx.compose.foundation.rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Outstanding Info Banner
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SocietyNavyDark,
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
                            Text("Total Outstanding Dues", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                            Text("Base: ₹${summary.totalBasePending.toInt()} + Penalty: ₹${summary.totalPenalty.toInt()}", color = SocietyGoldLight, fontSize = 11.sp)
                        }
                        Text("₹${summary.totalOutstanding.toInt()}", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Payment Amount (₹)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Payment Method:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = SocietyNavy)

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    methods.forEach { method ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (selectedMethod == method) SocietyGoldContainer else Color.Transparent)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            RadioButton(
                                selected = selectedMethod == method,
                                onClick = { selectedMethod = method }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(method, fontSize = 12.sp, color = SocietyNavyDark)
                        }
                    }
                }

                OutlinedTextField(
                    value = transactionRef,
                    onValueChange = { transactionRef = it },
                    label = { Text("Transaction Ref / Cheque No. / Receipt ID") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("Remarks (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: summary.totalOutstanding
                    val penaltyPart = if (summary.totalPenalty > 0) summary.totalPenalty else 0.0
                    onConfirmPayment(amt, selectedMethod, transactionRef, penaltyPart, remarks)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SocietyNavy)
            ) {
                Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Generate Digital Receipt")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Generate New Monthly Demand Cycle Dialog
@Composable
fun GenerateCycleDialog(
    onDismiss: () -> Unit,
    onGenerate: (monthName: String, monthIndex: Int, dueDate: String) -> Unit
) {
    var monthName by remember { mutableStateOf("September 2026") }
    var monthIndexText by remember { mutableStateOf("202609") }
    var dueDate by remember { mutableStateOf("10-Sep-2026") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Generate Monthly Maintenance Demand", fontWeight = FontWeight.Bold, color = SocietyNavy)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "This will automatically generate maintenance demand for all 85 residential units in Wing A (₹500 for owners, ₹750 for tenants).",
                    fontSize = 12.sp,
                    color = SocietySlate
                )

                OutlinedTextField(
                    value = monthName,
                    onValueChange = { monthName = it },
                    label = { Text("Month Name") },
                    placeholder = { Text("e.g. September 2026") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Payment Due Date") },
                    placeholder = { Text("e.g. 10-Sep-2026") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val idx = monthIndexText.toIntOrNull() ?: 202609
                    onGenerate(monthName, idx, dueDate)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SocietyNavy)
            ) {
                Text("Generate Demands (85 Units)")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
