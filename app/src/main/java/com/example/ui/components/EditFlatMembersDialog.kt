package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.OccupancyType
import com.example.data.model.UnitEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.SocietyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditFlatMembersDialog(
    viewModel: SocietyViewModel,
    unit: UnitEntity,
    onDismiss: () -> Unit
) {
    val userSession by viewModel.userSession.collectAsState()

    var occupancyType by remember { mutableStateOf(unit.occupancyType) }
    var monthlyMaintenance by remember { mutableStateOf(unit.monthlyMaintenance.toString()) }

    // User 1 (Primary Member / Owner or Primary Tenant)
    var user1Name by remember { mutableStateOf(unit.getEffectiveUser1Name()) }
    var user1Phone by remember { mutableStateOf(unit.getEffectiveUser1Phone()) }
    var user1Email by remember { mutableStateOf(unit.getEffectiveUser1Email()) }
    var user1Designation by remember {
        mutableStateOf(if (unit.user1Designation.isNotBlank()) unit.user1Designation else if (occupancyType == OccupancyType.TENANT) "Primary Tenant" else "Primary Owner")
    }

    // User 2 (Joint Member / Co-Owner / Co-Tenant)
    var user2Name by remember { mutableStateOf(unit.getEffectiveUser2Name()) }
    var user2Phone by remember { mutableStateOf(unit.getEffectiveUser2Phone()) }
    var user2Email by remember { mutableStateOf(unit.getEffectiveUser2Email()) }
    var user2Designation by remember {
        mutableStateOf(if (unit.user2Designation.isNotBlank()) unit.user2Designation else if (occupancyType == OccupancyType.TENANT) "Flat Owner (Landlord)" else "Co-Owner / Joint Member")
    }

    // Other flat details
    var numberOfOccupants by remember { mutableStateOf(unit.numberOfOccupants.toString()) }
    var emergencyContact by remember { mutableStateOf(unit.emergencyContact) }
    var submittedDocs by remember { mutableStateOf(unit.submittedDocs) }
    var pendingDocs by remember { mutableStateOf(unit.pendingDocs) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SocietyNavy,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${unit.roomNumber}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "Edit Flat Members & Name Change",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = SocietyNavy
                            )
                            Text(
                                "Floor ${unit.floor} • Wing A • Up to 2 Users per Flat",
                                fontSize = 10.sp,
                                color = SocietySlate
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Authorized role badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = SocietyNavyDark,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = SocietyGoldLight, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Full Authorization Mode: ${userSession.currentUser.name} (${userSession.currentUser.designation})",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Scrollable Edit Fields
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 1. Occupancy & Monthly Maintenance
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, SocietyCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("1. Flat Tenancy & Maintenance Configuration", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = SocietyNavy)

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = occupancyType == OccupancyType.OWNER,
                                    onClick = {
                                        occupancyType = OccupancyType.OWNER
                                        monthlyMaintenance = "500.0"
                                        user1Designation = "Primary Owner"
                                        user2Designation = "Co-Owner / Joint Member"
                                    },
                                    label = { Text("Owner Occupied (₹500/mo)", fontSize = 10.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = occupancyType == OccupancyType.TENANT,
                                    onClick = {
                                        occupancyType = OccupancyType.TENANT
                                        monthlyMaintenance = "750.0"
                                        user1Designation = "Primary Tenant"
                                        user2Designation = "Flat Owner (Landlord)"
                                    },
                                    label = { Text("Tenanted (₹750/mo)", fontSize = 10.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            OutlinedTextField(
                                value = monthlyMaintenance,
                                onValueChange = { monthlyMaintenance = it },
                                label = { Text("Monthly Maintenance Rate (₹)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // 2. USER SLOT 1 (Primary User)
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, SocietyCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("USER 1 (Primary Member)", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = SocietyNavy)
                                Surface(shape = RoundedCornerShape(4.dp), color = SocietyNavy.copy(alpha = 0.1f)) {
                                    Text("Slot 1 of 2", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SocietyNavy, modifier = Modifier.padding(4.dp))
                                }
                            }

                            OutlinedTextField(
                                value = user1Name,
                                onValueChange = { user1Name = it },
                                label = { Text("Member 1 Full Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = user1Phone,
                                    onValueChange = { user1Phone = it },
                                    label = { Text("Mobile Number") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = user1Designation,
                                    onValueChange = { user1Designation = it },
                                    label = { Text("Role / Relation") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            OutlinedTextField(
                                value = user1Email,
                                onValueChange = { user1Email = it },
                                label = { Text("Email Address") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // 3. USER SLOT 2 (Co-Owner / Joint Member / Tenant)
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, SocietyCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("USER 2 (Co-Owner / Joint Member)", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = SocietyNavy)
                                Surface(shape = RoundedCornerShape(4.dp), color = SocietyTeal.copy(alpha = 0.15f)) {
                                    Text("Slot 2 of 2", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SocietyTeal, modifier = Modifier.padding(4.dp))
                                }
                            }

                            OutlinedTextField(
                                value = user2Name,
                                onValueChange = { user2Name = it },
                                label = { Text("Member 2 Full Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = user2Phone,
                                    onValueChange = { user2Phone = it },
                                    label = { Text("Mobile Number") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = user2Designation,
                                    onValueChange = { user2Designation = it },
                                    label = { Text("Role / Relation") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            OutlinedTextField(
                                value = user2Email,
                                onValueChange = { user2Email = it },
                                label = { Text("Email Address") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // 4. Documents & Records
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, SocietyCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("4. Verification Records & Contacts", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = SocietyNavy)

                            OutlinedTextField(
                                value = emergencyContact,
                                onValueChange = { emergencyContact = it },
                                label = { Text("Emergency Contact Number") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = submittedDocs,
                                onValueChange = { submittedDocs = it },
                                label = { Text("Verified Submitted Documents") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = pendingDocs,
                                onValueChange = { pendingDocs = it },
                                label = { Text("Pending Documents / NOCs") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Save Changes Button
                Button(
                    onClick = {
                        val maintVal = monthlyMaintenance.toDoubleOrNull() ?: (if (occupancyType == OccupancyType.TENANT) 750.0 else 500.0)
                        val occupantsCount = numberOfOccupants.toIntOrNull() ?: unit.numberOfOccupants

                        // Build updated unit entity
                        val updatedUnit = unit.copy(
                            occupancyType = occupancyType,
                            monthlyMaintenance = maintVal,
                            numberOfOccupants = occupantsCount,
                            emergencyContact = emergencyContact,
                            submittedDocs = submittedDocs,
                            pendingDocs = pendingDocs,
                            // Primary Owner / Member representation
                            ownerName = if (occupancyType == OccupancyType.OWNER) user1Name else (if (user2Designation.contains("Owner", ignoreCase = true)) user2Name else unit.ownerName),
                            ownerPhone = if (occupancyType == OccupancyType.OWNER) user1Phone else user2Phone,
                            ownerEmail = if (occupancyType == OccupancyType.OWNER) user1Email else user2Email,
                            // Tenant representation
                            tenantName = if (occupancyType == OccupancyType.TENANT) user1Name else "",
                            tenantPhone = if (occupancyType == OccupancyType.TENANT) user1Phone else "",
                            tenantEmail = if (occupancyType == OccupancyType.TENANT) user1Email else "",
                            // Slot 1
                            user1Name = user1Name,
                            user1Phone = user1Phone,
                            user1Email = user1Email,
                            user1Designation = user1Designation,
                            // Slot 2
                            user2Name = user2Name,
                            user2Phone = user2Phone,
                            user2Email = user2Email,
                            user2Designation = user2Designation
                        )

                        viewModel.updateUnitProfile(updatedUnit)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SocietyNavy),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_flat_members_button")
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp), tint = SocietyGoldLight)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Changes for Flat ${unit.roomNumber}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
