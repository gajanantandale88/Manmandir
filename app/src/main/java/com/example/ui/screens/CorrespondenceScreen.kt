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
import com.example.data.model.CorrespondenceEntity
import com.example.data.model.CorrespondenceType
import com.example.data.model.FollowUpStatus
import com.example.data.model.UserRole
import com.example.ui.theme.*
import com.example.ui.viewmodel.SocietyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CorrespondenceScreen(
    viewModel: SocietyViewModel,
    modifier: Modifier = Modifier
) {
    val allCorr by viewModel.allCorrespondence.collectAsState()
    val role by viewModel.currentRole.collectAsState()

    var activeTypeFilter by remember { mutableStateOf<CorrespondenceType?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredList = remember(allCorr, activeTypeFilter) {
        allCorr.filter { activeTypeFilter == null || it.type == activeTypeFilter }
            .sortedByDescending { it.date }
    }

    Scaffold(
        floatingActionButton = {
            if (role != UserRole.RESIDENT) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = SocietyNavy,
                    contentColor = SocietyGoldLight,
                    modifier = Modifier.testTag("add_correspondence_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Correspondence")
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
            // Top Header
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
                                text = "Society Inward / Outward Records",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Text(
                                text = "BMC, Electricity, Police, Lift & Infrastructure Letters",
                                fontSize = 11.sp,
                                color = SocietyGoldLight
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = SocietyGold
                        ) {
                            Text(
                                text = "${allCorr.size} Records",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SocietyNavyDark,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = activeTypeFilter == null,
                            onClick = { activeTypeFilter = null },
                            label = { Text("All Correspondence", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SocietyGold,
                                selectedLabelColor = SocietyNavyDark,
                                containerColor = Color.White.copy(alpha = 0.15f),
                                labelColor = Color.White
                            )
                        )
                        FilterChip(
                            selected = activeTypeFilter == CorrespondenceType.INCOMING,
                            onClick = { activeTypeFilter = CorrespondenceType.INCOMING },
                            label = { Text("Inward Letters", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SocietyGold,
                                selectedLabelColor = SocietyNavyDark,
                                containerColor = Color.White.copy(alpha = 0.15f),
                                labelColor = Color.White
                            )
                        )
                        FilterChip(
                            selected = activeTypeFilter == CorrespondenceType.OUTGOING,
                            onClick = { activeTypeFilter = CorrespondenceType.OUTGOING },
                            label = { Text("Outward Letters", fontSize = 11.sp) },
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

            // List of Letters
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                if (filteredList.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No correspondence entries found.", color = SocietySlate, fontSize = 13.sp)
                        }
                    }
                } else {
                    items(filteredList, key = { it.id }) { item ->
                        CorrespondenceCard(
                            item = item,
                            isCommittee = role != UserRole.RESIDENT,
                            onStatusUpdate = { newStatus ->
                                viewModel.updateCorrespondence(item.copy(status = newStatus))
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddCorrespondenceDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { newCorr ->
                viewModel.addCorrespondence(newCorr)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun CorrespondenceCard(
    item: CorrespondenceEntity,
    isCommittee: Boolean,
    onStatusUpdate: (FollowUpStatus) -> Unit
) {
    val isIncoming = item.type == CorrespondenceType.INCOMING

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
                        color = if (isIncoming) Color(0xFFE0F2FE) else Color(0xFFFEF3C7)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isIncoming) Icons.Default.CallReceived else Icons.Default.CallMade,
                                contentDescription = null,
                                tint = if (isIncoming) Color(0xFF0369A1) else Color(0xFFB45309),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isIncoming) "INWARD" else "OUTWARD",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isIncoming) Color(0xFF0369A1) else Color(0xFFB45309)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    Text(item.date, fontSize = 11.sp, color = SocietySlate)
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = when (item.status) {
                        FollowUpStatus.REPLY_RECEIVED, FollowUpStatus.RESOLVED, FollowUpStatus.CLOSED -> SocietySuccessContainer
                        FollowUpStatus.IN_PROGRESS -> SocietyGoldContainer
                        FollowUpStatus.PENDING -> SocietyErrorContainer
                    }
                ) {
                    Text(
                        text = item.status.name.replace("_", " "),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (item.status) {
                            FollowUpStatus.REPLY_RECEIVED, FollowUpStatus.RESOLVED, FollowUpStatus.CLOSED -> SocietySuccess
                            FollowUpStatus.IN_PROGRESS -> Color(0xFF8D6E00)
                            FollowUpStatus.PENDING -> SocietyError
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.subject,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = SocietyNavyDark
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = item.description,
                fontSize = 11.sp,
                color = SocietySlate,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = SocietySurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Party: ${item.party}", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = SocietyNavy)
                    Text("Ref: ${item.referenceNumber}", fontSize = 10.sp, color = SocietySlate)
                }
            }

            if (!item.followUpDate.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Alarm, contentDescription = null, tint = SocietyWarning, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Follow-up by: ${item.followUpDate}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SocietyWarning)
                    }
                    Text("Assigned: ${item.responsibleMember}", fontSize = 10.sp, color = SocietySlate)
                }
            }

            if (isCommittee && item.status == FollowUpStatus.PENDING) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { onStatusUpdate(FollowUpStatus.IN_PROGRESS) },
                    colors = ButtonDefaults.buttonColors(containerColor = SocietyNavy),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Mark In-Progress / Assigned")
                }
            }
        }
    }
}

@Composable
fun AddCorrespondenceDialog(
    onDismiss: () -> Unit,
    onAdd: (CorrespondenceEntity) -> Unit
) {
    var type by remember { mutableStateOf(CorrespondenceType.OUTGOING) }
    var party by remember { mutableStateOf("") }
    var referenceNumber by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var responsibleMember by remember { mutableStateOf("Rajesh Sharma (Secretary)") }
    var followUpDate by remember { mutableStateOf("15-Sep-2026") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Official Correspondence", fontWeight = FontWeight.Bold, color = SocietyNavy) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = type == CorrespondenceType.INCOMING,
                        onClick = { type = CorrespondenceType.INCOMING },
                        label = { Text("Inward Letter") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = type == CorrespondenceType.OUTGOING,
                        onClick = { type = CorrespondenceType.OUTGOING },
                        label = { Text("Outward Letter") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = party,
                    onValueChange = { party = it },
                    label = { Text("Department / Party (e.g. BMC Ward H-West, Otis)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = referenceNumber,
                    onValueChange = { referenceNumber = it },
                    label = { Text("Letter / Ref Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Details / Decisions") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = responsibleMember,
                    onValueChange = { responsibleMember = it },
                    label = { Text("Responsible Committee Member") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = followUpDate,
                    onValueChange = { followUpDate = it },
                    label = { Text("Follow-up Target Date") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val entry = CorrespondenceEntity(
                        type = type,
                        date = "22-Aug-2026",
                        party = party.ifBlank { "Authority / Vendor" },
                        referenceNumber = referenceNumber.ifBlank { "MAN/2026/${System.currentTimeMillis() % 1000}" },
                        subject = subject.ifBlank { "Official Society Matter" },
                        description = description,
                        responsibleMember = responsibleMember,
                        followUpDate = followUpDate,
                        status = FollowUpStatus.PENDING,
                        attachedDocument = "docs/corr_${System.currentTimeMillis()}.pdf"
                    )
                    onAdd(entry)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SocietyNavy)
            ) {
                Text("Save Entry")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
