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
import com.example.data.model.OccupancyType
import com.example.data.model.PaymentStatus
import com.example.data.model.UnitEntity
import com.example.data.model.UnitType
import com.example.data.model.UserRole
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.SocietyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuildingStructureScreen(
    viewModel: SocietyViewModel,
    modifier: Modifier = Modifier
) {
    val allUnits by viewModel.allUnits.collectAsState()
    val roomSummaries by viewModel.roomSummaries.collectAsState()
    val role by viewModel.currentRole.collectAsState()
    val selectedFloor by viewModel.floorFilter.collectAsState()
    val selectedUnit by viewModel.selectedUnit.collectAsState()

    var activeFloorTab by remember { mutableIntStateOf(selectedFloor ?: 1) }
    var showGroundFloorFacilities by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SocietyBg)
    ) {
        // Top Banner with Building Stats
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
                            text = "Wing A Building Structure",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Text(
                            text = "88 Physical Units • 85 Residential Members",
                            fontSize = 11.sp,
                            color = SocietyGoldLight
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "11 Floors + Ground",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Floor Selector Pills
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = showGroundFloorFacilities,
                            onClick = {
                                showGroundFloorFacilities = true
                            },
                            label = { Text("Ground Floor", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SocietyGold,
                                selectedLabelColor = SocietyNavyDark,
                                containerColor = Color.White.copy(alpha = 0.15f),
                                labelColor = Color.White
                            )
                        )
                    }
                    items((1..11).toList()) { floor ->
                        val isSelected = !showGroundFloorFacilities && activeFloorTab == floor
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                showGroundFloorFacilities = false
                                activeFloorTab = floor
                                viewModel.setFloorFilter(floor)
                            },
                            label = { Text("Floor $floor", fontSize = 11.sp) },
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
        }

        // Main Content Area
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (showGroundFloorFacilities) {
                item {
                    Text(
                        text = "Ground Floor Common Society Facilities",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = SocietyNavy
                    )
                }

                item {
                    GroundFacilityCard(
                        name = "Society Office (Ground Floor Annexe)",
                        category = "Administrative & Resident Support",
                        details = "Open Mon-Sat: 09:00 AM - 01:00 PM & 06:00 PM - 09:00 PM\nStaff: Society Manager, Accountant & Maintenance Clerk\nContact: +91 22 2430 1104",
                        icon = Icons.Default.Apartment,
                        color = SocietyNavy
                    )
                }

                item {
                    GroundFacilityCard(
                        name = "Balwadi (Pre-Primary Nursery Facility)",
                        category = "Community Education & Child Care",
                        details = "Dedicated 450 sq.ft. child activity hall with safety mats and educational toys for society children.",
                        icon = Icons.Default.ChildCare,
                        color = Color(0xFF1E88E5)
                    )
                }

                item {
                    GroundFacilityCard(
                        name = "Welfare Centre & Community Hall",
                        category = "Meetings, Cultural Events & Recreations",
                        details = "Multi-purpose 1200 sq.ft hall equipped with projector, sound system, indoor games (table tennis, chess) and meeting seats for AGM.",
                        icon = Icons.Default.Groups,
                        color = Color(0xFF43A047)
                    )
                }
            } else {
                // Floor Rooms Grid (8 units per floor)
                val floorUnits = allUnits.filter { it.floor == activeFloorTab }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Floor $activeFloorTab Units (${floorUnits.size} Units)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = SocietyNavy
                        )

                        // Special notes for Floor 1 and Floor 8
                        when (activeFloorTab) {
                            1 -> {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFE0E7FF)
                                ) {
                                    Text(
                                        "Flat 104 = Society Office",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SocietyNavy,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            8 -> {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFF3E8FF)
                                ) {
                                    Text(
                                        "803 & 804 = Refuge Areas",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF6B21A8),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Grid of 8 Units
                items(floorUnits.chunked(2)) { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        pair.forEach { unit ->
                            val summary = roomSummaries[unit.roomNumber]
                            FloorRoomCard(
                                unit = unit,
                                summary = summary,
                                onClick = { viewModel.setSelectedUnit(unit) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (pair.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }

    // Unit Details Bottom Sheet / Dialog
    if (selectedUnit != null) {
        val unit = selectedUnit!!
        val summary = roomSummaries[unit.roomNumber]

        UnitDetailsDialog(
            unit = unit,
            summary = summary,
            isCommittee = role != UserRole.RESIDENT,
            onDismiss = { viewModel.setSelectedUnit(null) },
            onEditProfile = { showEditProfileDialog = true },
            onViewPenaltyBreakdown = {
                viewModel.setSelectedRoomForPenaltyView(summary)
            },
            onSendReminder = {
                viewModel.sendReminderAlert(
                    unit.roomNumber,
                    "Friendly reminder: Maintenance for Room ${unit.roomNumber} is due/pending."
                )
            }
        )
    }

    if (showEditProfileDialog && selectedUnit != null) {
        com.example.ui.components.EditFlatMembersDialog(
            viewModel = viewModel,
            unit = selectedUnit!!,
            onDismiss = { showEditProfileDialog = false }
        )
    }
}

@Composable
fun FloorRoomCard(
    unit: UnitEntity,
    summary: com.example.data.repository.RoomMaintenanceSummary?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isOffice = unit.unitType == UnitType.OFFICE
    val isRefuge = unit.unitType == UnitType.REFUGE_AREA
    val isTenant = unit.occupancyType == OccupancyType.TENANT

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isOffice -> Color(0xFFEEF2FF)
                isRefuge -> Color(0xFFFAF5FF)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(
            1.dp,
            when {
                isOffice -> Color(0xFFC7D2FE)
                isRefuge -> Color(0xFFE9D5FF)
                else -> SocietyCardBorder
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .height(135.dp)
            .testTag("room_card_${unit.roomNumber}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${unit.roomNumber}",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = when {
                        isOffice -> SocietyNavy
                        isRefuge -> Color(0xFF6B21A8)
                        else -> SocietyNavyDark
                    }
                )

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when {
                        isOffice -> Color(0xFF4F46E5)
                        isRefuge -> Color(0xFF9333EA)
                        isTenant -> SocietyTeal
                        else -> SocietyNavy
                    }
                ) {
                    Text(
                        text = when {
                            isOffice -> "OFFICE"
                            isRefuge -> "REFUGE"
                            isTenant -> "TENANT"
                            else -> "OWNER"
                        },
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Column {
                val residentName = when {
                    isOffice -> "Manmandir Office"
                    isRefuge -> "Safety Refuge Area"
                    isTenant -> unit.tenantName
                    else -> unit.ownerName
                }
                Text(
                    text = residentName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = SocietyNavy,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!isOffice && !isRefuge) {
                    Text(
                        text = "Rate: ₹${unit.monthlyMaintenance.toInt()}/mo",
                        fontSize = 10.sp,
                        color = SocietySlate
                    )
                }
            }

            if (!isOffice && !isRefuge && summary != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusBadge(status = summary.status)
                    if (summary.totalOutstanding > 0) {
                        Text(
                            text = "₹${summary.totalOutstanding.toInt()}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = SocietyError
                        )
                    }
                }
            } else if (isOffice || isRefuge) {
                Text(
                    text = if (isOffice) "Non-Residential Facility" else "Fire Escape & Safety Area",
                    fontSize = 9.sp,
                    color = SocietySlate
                )
            }
        }
    }
}

@Composable
fun GroundFacilityCard(
    name: String,
    category: String,
    details: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, SocietyCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = SocietyNavyDark
                )
                Text(
                    text = category,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SocietyGold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = details,
                    fontSize = 11.sp,
                    color = SocietySlate,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun UnitDetailsDialog(
    unit: UnitEntity,
    summary: com.example.data.repository.RoomMaintenanceSummary?,
    isCommittee: Boolean,
    onDismiss: () -> Unit,
    onEditProfile: () -> Unit,
    onViewPenaltyBreakdown: () -> Unit,
    onSendReminder: () -> Unit
) {
    val isOffice = unit.unitType == UnitType.OFFICE
    val isRefuge = unit.unitType == UnitType.REFUGE_AREA
    val isTenant = unit.occupancyType == OccupancyType.TENANT

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
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
                            text = "Room ${unit.roomNumber}",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Floor ${unit.floor}",
                        fontSize = 12.sp,
                        color = SocietySlate
                    )
                }

                if (isCommittee && !isOffice && !isRefuge) {
                    IconButton(onClick = onEditProfile) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = SocietyNavy)
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(androidx.compose.foundation.rememberScrollState())
            ) {
                if (isOffice || isRefuge) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isOffice) Color(0xFFEEF2FF) else Color(0xFFFAF5FF),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (isOffice) "Official Society Office" else "Designated Refuge Area",
                                fontWeight = FontWeight.Bold,
                                color = if (isOffice) SocietyNavy else Color(0xFF6B21A8)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isOffice)
                                    "This unit is allocated exclusively for the society administrative office, committee meetings, and society record keeping. Not subject to residential maintenance charges."
                                else
                                    "This floor area is designated as a statutory fire refuge platform under CFO and BMC building regulations. Must remain unobstructed at all times.",
                                fontSize = 11.sp,
                                color = SocietySlate
                            )
                        }
                    }
                } else {
                    // Resident Info Card
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SocietySurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            InfoRow("Owner Name", unit.ownerName)
                            InfoRow("Owner Contact", unit.ownerPhone)
                            InfoRow("Owner Email", unit.ownerEmail)

                            if (isTenant) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                                InfoRow("Tenant Name", unit.tenantName, isHighlight = true)
                                InfoRow("Tenant Contact", unit.tenantPhone)
                                InfoRow("Tenant Email", unit.tenantEmail)
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                            InfoRow("Occupants Count", "${unit.numberOfOccupants} Persons")
                            InfoRow("Occupancy Since", unit.occupancyDate)
                            InfoRow("Emergency Contact", unit.emergencyContact)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Documents Status
                    Text("Documents Status:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SocietyNavy)
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, SocietyCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SocietySuccess, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Submitted: ${unit.submittedDocs.ifBlank { "None" }}", fontSize = 11.sp, color = SocietyNavyDark)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (unit.pendingDocs.isBlank() || unit.pendingDocs == "None") Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (unit.pendingDocs.isBlank() || unit.pendingDocs == "None") SocietySuccess else SocietyError,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pending: ${unit.pendingDocs.ifBlank { "None" }}", fontSize = 11.sp, color = if (unit.pendingDocs.isBlank() || unit.pendingDocs == "None") SocietySuccess else SocietyError)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Maintenance Financial Status
                    if (summary != null) {
                        Text("Maintenance Account:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SocietyNavy)
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SocietyNavyDark,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Monthly Rate:", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                                    Text("₹${unit.monthlyMaintenance.toInt()}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Total Outstanding:", color = SocietyGoldLight, fontSize = 11.sp)
                                    Text("₹${summary.totalOutstanding.toInt()}", color = SocietyGoldLight, fontWeight = FontWeight.Black, fontSize = 14.sp)
                                }

                                if (summary.totalPenalty > 0) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    TextButton(
                                        onClick = onViewPenaltyBreakdown,
                                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFFF8A80)),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("View Penalty Calculation (₹${summary.totalPenalty.toInt()}) →", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (isCommittee && !isOffice && !isRefuge && summary != null && summary.totalOutstanding > 0) {
                Button(
                    onClick = onSendReminder,
                    colors = ButtonDefaults.buttonColors(containerColor = SocietyNavy)
                ) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Send Reminder")
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        },
        dismissButton = {
            if (isCommittee && !isOffice && !isRefuge && summary != null && summary.totalOutstanding > 0) {
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        }
    )
}

@Composable
private fun InfoRow(label: String, value: String, isHighlight: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 11.sp, color = SocietySlate)
        Text(
            text = value.ifBlank { "N/A" },
            fontSize = 11.sp,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.SemiBold,
            color = if (isHighlight) SocietyTeal else SocietyNavy
        )
    }
}

@Composable
fun EditUnitProfileDialog(
    unit: UnitEntity,
    onDismiss: () -> Unit,
    onSave: (UnitEntity) -> Unit
) {
    var ownerName by remember { mutableStateOf(unit.ownerName) }
    var ownerPhone by remember { mutableStateOf(unit.ownerPhone) }
    var ownerEmail by remember { mutableStateOf(unit.ownerEmail) }
    var tenantName by remember { mutableStateOf(unit.tenantName) }
    var tenantPhone by remember { mutableStateOf(unit.tenantPhone) }
    var tenantEmail by remember { mutableStateOf(unit.tenantEmail) }
    var occupants by remember { mutableStateOf("${unit.numberOfOccupants}") }
    var emergencyContact by remember { mutableStateOf(unit.emergencyContact) }
    var submittedDocs by remember { mutableStateOf(unit.submittedDocs) }
    var pendingDocs by remember { mutableStateOf(unit.pendingDocs) }
    var occupancyType by remember { mutableStateOf(unit.occupancyType) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Update Room ${unit.roomNumber} Details", fontWeight = FontWeight.Bold, color = SocietyNavy)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(androidx.compose.foundation.rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Occupancy Type Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = occupancyType == OccupancyType.OWNER,
                        onClick = { occupancyType = OccupancyType.OWNER },
                        label = { Text("Owner Flat") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = occupancyType == OccupancyType.TENANT,
                        onClick = { occupancyType = OccupancyType.TENANT },
                        label = { Text("Tenant Flat") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = ownerName,
                    onValueChange = { ownerName = it },
                    label = { Text("Owner Full Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = ownerPhone,
                    onValueChange = { ownerPhone = it },
                    label = { Text("Owner Mobile Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = ownerEmail,
                    onValueChange = { ownerEmail = it },
                    label = { Text("Owner Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (occupancyType == OccupancyType.TENANT) {
                    OutlinedTextField(
                        value = tenantName,
                        onValueChange = { tenantName = it },
                        label = { Text("Tenant Full Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = tenantPhone,
                        onValueChange = { tenantPhone = it },
                        label = { Text("Tenant Mobile Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = tenantEmail,
                        onValueChange = { tenantEmail = it },
                        label = { Text("Tenant Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = occupants,
                    onValueChange = { occupants = it },
                    label = { Text("Number of Occupants") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = emergencyContact,
                    onValueChange = { emergencyContact = it },
                    label = { Text("Emergency Contact") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = submittedDocs,
                    onValueChange = { submittedDocs = it },
                    label = { Text("Submitted Documents") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = pendingDocs,
                    onValueChange = { pendingDocs = it },
                    label = { Text("Pending Documents") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = unit.copy(
                        occupancyType = occupancyType,
                        ownerName = ownerName,
                        ownerPhone = ownerPhone,
                        ownerEmail = ownerEmail,
                        tenantName = tenantName,
                        tenantPhone = tenantPhone,
                        tenantEmail = tenantEmail,
                        numberOfOccupants = occupants.toIntOrNull() ?: unit.numberOfOccupants,
                        emergencyContact = emergencyContact,
                        submittedDocs = submittedDocs,
                        pendingDocs = pendingDocs,
                        monthlyMaintenance = if (occupancyType == OccupancyType.TENANT) 750.0 else 500.0
                    )
                    onSave(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SocietyNavy)
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
