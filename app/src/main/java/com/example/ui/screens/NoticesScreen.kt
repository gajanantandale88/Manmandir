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
import com.example.data.model.NoticeCategory
import com.example.data.model.NoticeEntity
import com.example.data.model.UserRole
import com.example.ui.theme.*
import com.example.ui.viewmodel.SocietyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoticesScreen(
    viewModel: SocietyViewModel,
    modifier: Modifier = Modifier
) {
    val allNotices by viewModel.allNotices.collectAsState()
    val role by viewModel.currentRole.collectAsState()

    var selectedCat by remember { mutableStateOf<NoticeCategory?>(null) }
    var showPublishDialog by remember { mutableStateOf(false) }

    val filteredNotices = remember(allNotices, selectedCat) {
        allNotices.filter { selectedCat == null || it.category == selectedCat }
            .sortedByDescending { it.isUrgent }
    }

    Scaffold(
        floatingActionButton = {
            if (role != UserRole.RESIDENT) {
                FloatingActionButton(
                    onClick = { showPublishDialog = true },
                    containerColor = SocietyNavy,
                    contentColor = SocietyGoldLight,
                    modifier = Modifier.testTag("publish_notice_fab")
                ) {
                    Icon(Icons.Default.Campaign, contentDescription = "Publish Notice")
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
                                text = "Society Digital Notice Board",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Official announcements, maintenance schedules & emergency alerts",
                                fontSize = 11.sp,
                                color = SocietyGoldLight
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = SocietyGold
                        ) {
                            Text(
                                text = "${allNotices.size} Notices",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SocietyNavyDark,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = selectedCat == null,
                                onClick = { selectedCat = null },
                                label = { Text("All Categories", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SocietyGold,
                                    selectedLabelColor = SocietyNavyDark,
                                    containerColor = Color.White.copy(alpha = 0.15f),
                                    labelColor = Color.White
                                )
                            )
                        }
                        items(NoticeCategory.values()) { cat ->
                            FilterChip(
                                selected = selectedCat == cat,
                                onClick = { selectedCat = if (selectedCat == cat) null else cat },
                                label = { Text(cat.name.replace("_", " "), fontSize = 11.sp) },
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

            // Notices list
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredNotices, key = { it.id }) { notice ->
                    NoticeCardItem(notice = notice)
                }
            }
        }
    }

    if (showPublishDialog) {
        PublishNoticeDialog(
            onDismiss = { showPublishDialog = false },
            onPublish = { newNotice ->
                viewModel.addNotice(newNotice)
                showPublishDialog = false
            }
        )
    }
}

@Composable
fun NoticeCardItem(notice: NoticeEntity) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notice.isUrgent) SocietyErrorContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            if (notice.isUrgent) SocietyError.copy(alpha = 0.4f) else SocietyCardBorder
        ),
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
                    if (notice.isUrgent) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = SocietyError
                        ) {
                            Text(
                                "URGENT",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 9.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = SocietyGoldContainer
                    ) {
                        Text(
                            text = notice.category.name.replace("_", " "),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8D6E00),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(notice.date, fontSize = 11.sp, color = SocietySlate)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = notice.title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = SocietyNavyDark
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = notice.content,
                fontSize = 12.sp,
                color = SocietyNavy,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Published by: ${notice.issuedBy}",
                    fontSize = 10.sp,
                    color = SocietySlate
                )
            }
        }
    }
}

@Composable
fun PublishNoticeDialog(
    onDismiss: () -> Unit,
    onPublish: (NoticeEntity) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(NoticeCategory.MAINTENANCE_REMINDER) }
    var isUrgent by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Publish Society Notice", fontWeight = FontWeight.Bold, color = SocietyNavy) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Notice Title") },
                    placeholder = { Text("e.g. Schedule of Overhead Water Tank Cleaning") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Notice Body Content") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Category:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = SocietyNavy)

                NoticeCategory.values().take(5).forEach { cat ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedCategory = cat }
                            .padding(vertical = 2.dp)
                    ) {
                        RadioButton(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(cat.name.replace("_", " "), fontSize = 11.sp)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isUrgent, onCheckedChange = { isUrgent = it })
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Mark as High-Priority / Urgent Alert", fontSize = 12.sp, color = SocietyError, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val notice = NoticeEntity(
                        title = title.ifBlank { "Society Notice" },
                        content = content,
                        date = "22-Aug-2026",
                        category = selectedCategory,
                        isUrgent = isUrgent,
                        issuedBy = "Managing Committee (Rajesh Sharma)",
                        acknowledgedByUsers = ""
                    )
                    onPublish(notice)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SocietyNavy)
            ) {
                Text("Publish to All Members")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
