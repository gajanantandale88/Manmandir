package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import com.example.data.model.CommitteeTaskEntity
import com.example.data.model.MeetingEntity
import com.example.data.model.MeetingType
import com.example.data.model.TaskStatus
import com.example.data.model.UserRole
import com.example.ui.theme.*
import com.example.ui.viewmodel.SocietyViewModel

enum class MeetingsTasksTab {
    MEETINGS,
    TASKS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingsTasksScreen(
    viewModel: SocietyViewModel,
    modifier: Modifier = Modifier
) {
    val allMeetings by viewModel.allMeetings.collectAsState()
    val allTasks by viewModel.allTasks.collectAsState()
    val role by viewModel.currentRole.collectAsState()

    var activeTab by remember { mutableStateOf(MeetingsTasksTab.MEETINGS) }
    var showAddMeetingDialog by remember { mutableStateOf(false) }
    var showAddTaskDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            if (role != UserRole.RESIDENT) {
                FloatingActionButton(
                    onClick = {
                        if (activeTab == MeetingsTasksTab.MEETINGS) showAddMeetingDialog = true
                        else showAddTaskDialog = true
                    },
                    containerColor = SocietyNavy,
                    contentColor = SocietyGoldLight,
                    modifier = Modifier.testTag("add_meeting_task_fab")
                ) {
                    Icon(
                        if (activeTab == MeetingsTasksTab.MEETINGS) Icons.Default.Event else Icons.Default.AddTask,
                        contentDescription = "Add"
                    )
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
                    Text(
                        text = "Meetings & Committee Action Items",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                    Text(
                        text = "General Body Meetings, MC Resolutions & Operations Checklist",
                        fontSize = 11.sp,
                        color = SocietyGoldLight
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = activeTab == MeetingsTasksTab.MEETINGS,
                            onClick = { activeTab = MeetingsTasksTab.MEETINGS },
                            label = { Text("Meetings (${allMeetings.size})", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SocietyGold,
                                selectedLabelColor = SocietyNavyDark,
                                containerColor = Color.White.copy(alpha = 0.15f),
                                labelColor = Color.White
                            )
                        )
                        FilterChip(
                            selected = activeTab == MeetingsTasksTab.TASKS,
                            onClick = { activeTab = MeetingsTasksTab.TASKS },
                            label = { Text("Committee Tasks (${allTasks.size})", fontSize = 11.sp) },
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

            if (activeTab == MeetingsTasksTab.MEETINGS) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(allMeetings, key = { it.id }) { meeting ->
                        MeetingCardItem(meeting = meeting)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(allTasks, key = { it.id }) { task ->
                        TaskCardItem(
                            task = task,
                            isCommittee = role != UserRole.RESIDENT,
                            onToggleStatus = {
                                val nextStatus = when (task.status) {
                                    TaskStatus.PENDING -> TaskStatus.IN_PROGRESS
                                    TaskStatus.IN_PROGRESS -> TaskStatus.COMPLETED
                                    TaskStatus.COMPLETED -> TaskStatus.PENDING
                                }
                                viewModel.updateTask(task.copy(status = nextStatus))
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddMeetingDialog) {
        AddMeetingDialog(
            onDismiss = { showAddMeetingDialog = false },
            onAdd = { newMeeting ->
                viewModel.addMeeting(newMeeting)
                showAddMeetingDialog = false
            }
        )
    }

    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onAdd = { newTask ->
                viewModel.addTask(newTask)
                showAddTaskDialog = false
            }
        )
    }
}

@Composable
fun MeetingCardItem(meeting: MeetingEntity) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, SocietyCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = SocietyNavy
                ) {
                    Text(
                        meeting.type.name.replace("_", " "),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Text("${meeting.date} • ${meeting.time}", fontSize = 11.sp, color = SocietySlate)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("Venue: ${meeting.venue}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SocietyGold)

            Spacer(modifier = Modifier.height(6.dp))

            Text("Agenda / Topics:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SocietySlate)
            Text(meeting.agenda, fontSize = 12.sp, color = SocietyNavy, lineHeight = 16.sp)

            if (meeting.decisions.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = SocietySurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Decisions & Resolutions: ${meeting.decisions}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SocietyNavy,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TaskCardItem(
    task: CommitteeTaskEntity,
    isCommittee: Boolean,
    onToggleStatus: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, SocietyCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onToggleStatus,
                    enabled = isCommittee
                ) {
                    Icon(
                        imageVector = when (task.status) {
                            TaskStatus.COMPLETED -> Icons.Default.CheckCircle
                            TaskStatus.IN_PROGRESS -> Icons.Default.HourglassTop
                            TaskStatus.PENDING -> Icons.Default.RadioButtonUnchecked
                        },
                        contentDescription = null,
                        tint = when (task.status) {
                            TaskStatus.COMPLETED -> SocietySuccess
                            TaskStatus.IN_PROGRESS -> SocietyGold
                            TaskStatus.PENDING -> SocietySlate
                        }
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Column {
                    Text(task.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SocietyNavyDark)
                    Text(task.description, fontSize = 11.sp, color = SocietySlate)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Assigned: ${task.assignedTo} • Due: ${task.dueDate}", fontSize = 10.sp, color = SocietyNavy)
                }
            }
        }
    }
}

@Composable
fun AddMeetingDialog(
    onDismiss: () -> Unit,
    onAdd: (MeetingEntity) -> Unit
) {
    var meetingType by remember { mutableStateOf(MeetingType.COMMITTEE_MEETING) }
    var date by remember { mutableStateOf("28-Aug-2026") }
    var time by remember { mutableStateOf("08:00 PM") }
    var venue by remember { mutableStateOf("Welfare Centre Hall (Ground Floor)") }
    var agenda by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Schedule Society Meeting", fontWeight = FontWeight.Bold, color = SocietyNavy) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Meeting Type:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = SocietyNavy)
                MeetingType.values().forEach { type ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { meetingType = type }
                            .padding(vertical = 2.dp)
                    ) {
                        RadioButton(selected = meetingType == type, onClick = { meetingType = type })
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(type.name.replace("_", " "), fontSize = 11.sp)
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Date") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = time,
                        onValueChange = { time = it },
                        label = { Text("Time") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = venue,
                    onValueChange = { venue = it },
                    label = { Text("Venue") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = agenda,
                    onValueChange = { agenda = it },
                    label = { Text("Agenda Topics") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val meeting = MeetingEntity(
                        type = meetingType,
                        date = date,
                        time = time,
                        venue = venue,
                        agenda = agenda.ifBlank { "Society general discussions and monthly operations review" },
                        decisions = "Pending meeting completion",
                        minutes = "",
                        attendanceSummary = "All committee members and Wing A residents invited",
                        signedMinutesDoc = null
                    )
                    onAdd(meeting)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SocietyNavy)
            ) {
                Text("Schedule Meeting")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onAdd: (CommitteeTaskEntity) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var assignedTo by remember { mutableStateOf("Rajesh Sharma (Secretary)") }
    var dueDate by remember { mutableStateOf("31-Aug-2026") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Assign Committee Action Item", fontWeight = FontWeight.Bold, color = SocietyNavy) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") },
                    placeholder = { Text("e.g. CCTV Camera AMC Renewal") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Task Details") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = assignedTo,
                    onValueChange = { assignedTo = it },
                    label = { Text("Assign To Member") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Due Target Date") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val task = CommitteeTaskEntity(
                        title = title.ifBlank { "Committee Task" },
                        description = description,
                        assignedTo = assignedTo,
                        dueDate = dueDate,
                        status = TaskStatus.PENDING,
                        contractorInfo = "",
                        attachments = ""
                    )
                    onAdd(task)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SocietyNavy)
            ) {
                Text("Create Task")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
