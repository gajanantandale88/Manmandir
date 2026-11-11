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
import com.example.data.model.ComplaintCategory
import com.example.data.model.ComplaintEntity
import com.example.data.model.ComplaintStatus
import com.example.data.model.UserRole
import com.example.ui.theme.*
import com.example.ui.viewmodel.SocietyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplaintsScreen(
    viewModel: SocietyViewModel,
    modifier: Modifier = Modifier
) {
    val allComplaints by viewModel.allComplaints.collectAsState()
    val role by viewModel.currentRole.collectAsState()
    val residentRoom by viewModel.currentResidentRoom.collectAsState()

    var showSubmitDialog by remember { mutableStateOf(false) }
    var selectedComplaintForUpdate by remember { mutableStateOf<ComplaintEntity?>(null) }

    val displayComplaints = remember(allComplaints, role, residentRoom) {
        val base = if (role == UserRole.RESIDENT) {
            allComplaints.filter { it.roomNumber == residentRoom }
        } else {
            allComplaints
        }
        base.sortedByDescending { it.dateSubmitted }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showSubmitDialog = true },
                containerColor = SocietyNavy,
                contentColor = SocietyGoldLight,
                modifier = Modifier.testTag("submit_complaint_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AddComment, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("New Ticket", fontWeight = FontWeight.Bold)
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
                                text = "Resident Helpdesk & Grievance Desk",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Text(
                                text = if (role == UserRole.RESIDENT) "Service tickets registered for Room $residentRoom" else "Society maintenance tickets & resolution tracking",
                                fontSize = 11.sp,
                                color = SocietyGoldLight
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = SocietyGold
                        ) {
                            Text(
                                text = "${displayComplaints.size} Tickets",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SocietyNavyDark,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // List of complaints
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                if (displayComplaints.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No active service tickets found.", color = SocietySlate, fontSize = 13.sp)
                        }
                    }
                } else {
                    items(displayComplaints, key = { it.id }) { complaint ->
                        ComplaintCardItem(
                            complaint = complaint,
                            isCommittee = role != UserRole.RESIDENT,
                            onUpdate = { selectedComplaintForUpdate = complaint }
                        )
                    }
                }
            }
        }
    }

    if (showSubmitDialog) {
        SubmitComplaintDialog(
            defaultRoom = residentRoom,
            isResident = role == UserRole.RESIDENT,
            onDismiss = { showSubmitDialog = false },
            onSubmit = { newComplaint ->
                viewModel.addComplaint(newComplaint)
                showSubmitDialog = false
            }
        )
    }

    if (selectedComplaintForUpdate != null) {
        UpdateComplaintStatusDialog(
            complaint = selectedComplaintForUpdate!!,
            onDismiss = { selectedComplaintForUpdate = null },
            onSave = { updated ->
                viewModel.updateComplaint(updated)
                selectedComplaintForUpdate = null
            }
        )
    }
}

@Composable
fun ComplaintCardItem(
    complaint: ComplaintEntity,
    isCommittee: Boolean,
    onUpdate: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, SocietyCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
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
                        shape = RoundedCornerShape(6.dp),
                        color = SocietyNavy
                    ) {
                        Text(
                            text = complaint.ticketNumber,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Room ${complaint.roomNumber} (${complaint.residentName})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = SocietyNavyDark
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = when (complaint.status) {
                        ComplaintStatus.RESOLVED, ComplaintStatus.CLOSED -> SocietySuccessContainer
                        ComplaintStatus.IN_PROGRESS, ComplaintStatus.ASSIGNED -> SocietyGoldContainer
                        ComplaintStatus.SUBMITTED -> SocietyErrorContainer
                    }
                ) {
                    Text(
                        text = complaint.status.name.replace("_", " "),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (complaint.status) {
                            ComplaintStatus.RESOLVED, ComplaintStatus.CLOSED -> SocietySuccess
                            ComplaintStatus.IN_PROGRESS, ComplaintStatus.ASSIGNED -> Color(0xFF8D6E00)
                            ComplaintStatus.SUBMITTED -> SocietyError
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = SocietySurfaceVariant
                ) {
                    Text(
                        complaint.category.name.replace("_", " "),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SocietySlate,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Submitted: ${complaint.dateSubmitted}", fontSize = 11.sp, color = SocietySlate)
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(complaint.description, fontSize = 12.sp, color = SocietyNavy)

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = SocietySurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("Assigned To: ${complaint.assignedTo.ifBlank { "Unassigned" }}", fontSize = 10.sp, color = SocietyNavy)
                    if (complaint.resolutionRemarks.isNotBlank()) {
                        Text("Resolution: ${complaint.resolutionRemarks}", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = SocietySuccess)
                    }
                }
            }

            if (isCommittee) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onUpdate,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Update Status / Assign Staff", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun SubmitComplaintDialog(
    defaultRoom: Int,
    isResident: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (ComplaintEntity) -> Unit
) {
    var roomNumberText by remember { mutableStateOf("$defaultRoom") }
    var residentName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(ComplaintCategory.PLUMBING) }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Register Service Ticket", fontWeight = FontWeight.Bold, color = SocietyNavy) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = roomNumberText,
                    onValueChange = { roomNumberText = it },
                    label = { Text("Flat / Room Number") },
                    singleLine = true,
                    enabled = !isResident,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = residentName,
                    onValueChange = { residentName = it },
                    label = { Text("Your Name / Contact Person") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Issue Category:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = SocietyNavy)
                ComplaintCategory.values().take(6).forEach { cat ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { category = cat }
                            .padding(vertical = 2.dp)
                    ) {
                        RadioButton(selected = category == cat, onClick = { category = cat })
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(cat.name.replace("_", " "), fontSize = 11.sp)
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Detailed Description of Issue") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val room = roomNumberText.toIntOrNull() ?: defaultRoom
                    val complaint = ComplaintEntity(
                        ticketNumber = "TKT-${(1000..9999).random()}",
                        roomNumber = room,
                        residentName = residentName.ifBlank { "Resident Room $room" },
                        category = category,
                        description = description.ifBlank { "Maintenance request" },
                        status = ComplaintStatus.SUBMITTED,
                        dateSubmitted = "22-Aug-2026",
                        assignedTo = "Society Maintenance Staff",
                        resolutionRemarks = ""
                    )
                    onSubmit(complaint)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SocietyNavy)
            ) {
                Text("Submit Ticket")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun UpdateComplaintStatusDialog(
    complaint: ComplaintEntity,
    onDismiss: () -> Unit,
    onSave: (ComplaintEntity) -> Unit
) {
    var status by remember { mutableStateOf(complaint.status) }
    var assignedTo by remember { mutableStateOf(complaint.assignedTo) }
    var resolutionRemarks by remember { mutableStateOf(complaint.resolutionRemarks) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Ticket #${complaint.ticketNumber}", fontWeight = FontWeight.Bold, color = SocietyNavy) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Select Status:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = SocietyNavy)
                ComplaintStatus.values().forEach { st ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { status = st }
                            .padding(vertical = 2.dp)
                    ) {
                        RadioButton(selected = status == st, onClick = { status = st })
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(st.name.replace("_", " "), fontSize = 11.sp)
                    }
                }

                OutlinedTextField(
                    value = assignedTo,
                    onValueChange = { assignedTo = it },
                    label = { Text("Assigned Contractor / Staff") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = resolutionRemarks,
                    onValueChange = { resolutionRemarks = it },
                    label = { Text("Resolution Remarks") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = complaint.copy(
                        status = status,
                        assignedTo = assignedTo,
                        resolutionRemarks = resolutionRemarks
                    )
                    onSave(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SocietyNavy)
            ) {
                Text("Save Updates")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
