package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OccupancyType
import com.example.data.model.Resident
import com.example.data.model.UnitEntity
import com.example.data.model.UnitType
import com.example.data.model.UserRole
import com.example.data.session.UserPrivilege
import com.example.ui.components.AddResidentDialog
import com.example.ui.components.EditFlatMembersDialog
import com.example.ui.components.MemberMessagingDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.SocietyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectoryScreen(
    viewModel: SocietyViewModel,
    modifier: Modifier = Modifier
) {
    val allUnits by viewModel.allUnits.collectAsState()
    val allResidents by viewModel.allResidents.collectAsState()
    val userSession by viewModel.userSession.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFloorFilter by remember { mutableStateOf<Int?>(null) }
    var filterType by remember { mutableStateOf<OccupancyType?>(null) }

    var editingUnit by remember { mutableStateOf<UnitEntity?>(null) }
    var messagingUnitRoom by remember { mutableStateOf<Int?>(null) }
    var showAddResidentDialog by remember { mutableStateOf(false) }

    val canEditMembers = userSession.privileges.contains(UserPrivilege.EDIT_ALL_MEMBER_NAMES) ||
            userSession.currentUser.role == UserRole.SUPER_ADMIN ||
            userSession.currentUser.role == UserRole.CHAIRMAN

    val canSendMessage = userSession.privileges.contains(UserPrivilege.SEND_MESSAGES_TO_MEMBERS)

    val filteredMembers = remember(allUnits, searchQuery, selectedFloorFilter, filterType) {
        allUnits.filter { it.unitType == UnitType.RESIDENTIAL }
            .filter { selectedFloorFilter == null || it.floor == selectedFloorFilter }
            .filter { filterType == null || it.occupancyType == filterType }
            .filter { unit ->
                if (searchQuery.isBlank()) true
                else {
                    val q = searchQuery.trim().lowercase()
                    unit.roomNumber.toString().contains(q) ||
                            unit.getEffectiveUser1Name().lowercase().contains(q) ||
                            unit.getEffectiveUser2Name().lowercase().contains(q) ||
                            unit.getEffectiveUser1Phone().contains(q) ||
                            unit.getEffectiveUser2Phone().contains(q) ||
                            unit.ownerName.lowercase().contains(q) ||
                            unit.tenantName.lowercase().contains(q)
                }
            }.sortedBy { it.roomNumber }
    }

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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Society Member Directory",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Text(
                            text = "85 Flats • 170 Registered Users (Up to 2 per Flat)",
                            fontSize = 11.sp,
                            color = SocietyGoldLight
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = SocietyGold
                        ) {
                            Text(
                                text = "Room DB: ${allResidents.size} Residents",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SocietyNavyDark,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Button(
                            onClick = { showAddResidentDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SocietyGold,
                                contentColor = SocietyNavyDark
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(28.dp)
                                .testTag("add_resident_header_btn")
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ Add Resident", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (canEditMembers) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.White.copy(alpha = 0.15f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = SocietyGoldLight, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Admin / Chairman Authorization: Name changes & 2 users per flat edit enabled",
                                fontSize = 10.sp,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Search & Filter Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by Flat #, Member 1, Member 2, Phone...", fontSize = 12.sp) },
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = filterType == null,
                    onClick = { filterType = null },
                    label = { Text("All 85 Flats", fontSize = 11.sp) },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = filterType == OccupancyType.OWNER,
                    onClick = { filterType = OccupancyType.OWNER },
                    label = { Text("Owners Only", fontSize = 11.sp) },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = filterType == OccupancyType.TENANT,
                    onClick = { filterType = OccupancyType.TENANT },
                    label = { Text("Tenants Only", fontSize = 11.sp) },
                    modifier = Modifier.weight(1f)
                )
            }

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
                        onClick = { selectedFloorFilter = if (selectedFloorFilter == floor) null else floor },
                        label = { Text("F$floor", fontSize = 10.sp) }
                    )
                }
            }
        }

        // Member Cards List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(filteredMembers, key = { it.roomNumber }) { unit ->
                EnhancedMemberDirectoryCard(
                    unit = unit,
                    canEditMembers = canEditMembers,
                    canSendMessage = canSendMessage,
                    onEdit = { editingUnit = unit },
                    onSendMessage = { messagingUnitRoom = unit.roomNumber },
                    onContact = { channel, phone ->
                        viewModel.showSnackbar("Initiating $channel connection with $phone (Flat ${unit.roomNumber})...")
                    }
                )
            }
        }
    }

    editingUnit?.let { unit ->
        EditFlatMembersDialog(
            viewModel = viewModel,
            unit = unit,
            onDismiss = { editingUnit = null }
        )
    }

    messagingUnitRoom?.let { room ->
        MemberMessagingDialog(
            viewModel = viewModel,
            preselectedRoom = room,
            onDismiss = { messagingUnitRoom = null }
        )
    }

    if (showAddResidentDialog) {
        AddResidentDialog(
            onDismiss = { showAddResidentDialog = false },
            onSave = { newResident ->
                viewModel.addResident(newResident)
                showAddResidentDialog = false
            }
        )
    }
}

@Composable
fun EnhancedMemberDirectoryCard(
    unit: UnitEntity,
    canEditMembers: Boolean,
    canSendMessage: Boolean,
    onEdit: () -> Unit,
    onSendMessage: () -> Unit,
    onContact: (channel: String, phone: String) -> Unit
) {
    val isOwner = unit.occupancyType == OccupancyType.OWNER
    val user1Name = unit.getEffectiveUser1Name()
    val user1Phone = unit.getEffectiveUser1Phone()
    val user1Email = unit.getEffectiveUser1Email()
    val user1Designation = if (unit.user1Designation.isNotBlank()) unit.user1Designation else if (isOwner) "Primary Owner" else "Primary Tenant"

    val user2Name = unit.getEffectiveUser2Name()
    val user2Phone = unit.getEffectiveUser2Phone()
    val user2Email = unit.getEffectiveUser2Email()
    val user2Designation = if (unit.user2Designation.isNotBlank()) unit.user2Designation else if (isOwner) "Co-Owner / Joint Member" else "Flat Owner (Landlord)"

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, SocietyCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header with Flat number, floor, occupancy, and Edit button
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
                            text = "Flat ${unit.roomNumber}",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Floor ${unit.floor} • Wing A",
                        fontSize = 11.sp,
                        color = SocietySlate
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isOwner) SocietyNavyLight.copy(alpha = 0.12f) else SocietyTeal.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = if (isOwner) "OWNER" else "TENANT",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isOwner) SocietyNavy else SocietyTeal,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (canEditMembers) {
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Members", tint = SocietyNavy, modifier = Modifier.size(16.dp))
                        }
                    }

                    if (canSendMessage) {
                        IconButton(
                            onClick = onSendMessage,
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Message Flat", tint = SocietyGoldDark, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // USER 1 (Primary User)
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = SocietySurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = RoundedCornerShape(4.dp), color = SocietyNavy) {
                                Text("USER 1", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(user1Name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SocietyNavyDark)
                        }
                        Text(user1Designation, fontSize = 9.sp, color = SocietySlate, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(user1Phone, fontSize = 10.sp, color = SocietyNavy, fontWeight = FontWeight.SemiBold)
                            Text(user1Email, fontSize = 9.sp, color = SocietySlate)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            IconButton(onClick = { onContact("Phone Call", user1Phone) }, modifier = Modifier.size(26.dp)) {
                                Icon(Icons.Default.Phone, contentDescription = null, tint = SocietyNavy, modifier = Modifier.size(14.dp))
                            }
                            IconButton(onClick = { onContact("WhatsApp", user1Phone) }, modifier = Modifier.size(26.dp)) {
                                Icon(Icons.Default.Chat, contentDescription = null, tint = SocietySuccess, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // USER 2 (Co-Owner / Joint Member)
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = SocietyTeal.copy(alpha = 0.08f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = RoundedCornerShape(4.dp), color = SocietyTeal) {
                                Text("USER 2", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(user2Name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SocietyNavyDark)
                        }
                        Text(user2Designation, fontSize = 9.sp, color = SocietySlate, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(user2Phone, fontSize = 10.sp, color = SocietyNavy, fontWeight = FontWeight.SemiBold)
                            Text(user2Email, fontSize = 9.sp, color = SocietySlate)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            IconButton(onClick = { onContact("Phone Call", user2Phone) }, modifier = Modifier.size(26.dp)) {
                                Icon(Icons.Default.Phone, contentDescription = null, tint = SocietyNavy, modifier = Modifier.size(14.dp))
                            }
                            IconButton(onClick = { onContact("WhatsApp", user2Phone) }, modifier = Modifier.size(26.dp)) {
                                Icon(Icons.Default.Chat, contentDescription = null, tint = SocietySuccess, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
