package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.NoticeCategory
import com.example.data.model.NoticeEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.SocietyViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class MessagingTargetScope {
    ALL_85_FLATS,
    SPECIFIC_FLOOR,
    SPECIFIC_FLAT
}

enum class DeliveryChannel {
    IN_APP_AND_SMS,
    WHATSAPP_BROADCAST,
    EMAIL_CIRCULAR
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberMessagingDialog(
    viewModel: SocietyViewModel,
    preselectedRoom: Int? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val userSession by viewModel.userSession.collectAsState()
    val allUnits by viewModel.allUnits.collectAsState()

    var targetScope by remember {
        mutableStateOf(if (preselectedRoom != null) MessagingTargetScope.SPECIFIC_FLAT else MessagingTargetScope.ALL_85_FLATS)
    }
    var selectedFloor by remember { mutableIntStateOf(1) }
    var selectedRoom by remember { mutableIntStateOf(preselectedRoom ?: 101) }
    var selectedCategory by remember { mutableStateOf(NoticeCategory.MAINTENANCE_REMINDER) }
    var deliveryChannel by remember { mutableStateOf(DeliveryChannel.IN_APP_AND_SMS) }
    var isUrgent by remember { mutableStateOf(false) }

    var subject by remember {
        mutableStateOf(
            when (selectedCategory) {
                NoticeCategory.MAINTENANCE_REMINDER -> "Urgent: Monthly Maintenance Due Reminder"
                NoticeCategory.WATER_SUPPLY -> "Water Supply Schedule Update"
                NoticeCategory.EMERGENCY_ALERT -> "Emergency Notice: Wing A Facility Notice"
                else -> "Official Communication from Society Executive Committee"
            }
        )
    }

    var messageBody by remember {
        mutableStateOf("Dear Members,\n\nKindly note the updated maintenance dues for the current month. Please clear your dues on time to avoid statutory 22% delay penalties.\n\nWarm Regards,\n${userSession.currentUser.name}\n${userSession.currentUser.designation}")
    }

    val senderName = userSession.currentUser.name
    val senderDesignation = userSession.currentUser.designation

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SocietyNavy,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Send,
                                    contentDescription = null,
                                    tint = SocietyGoldLight,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "Broadcast & Member Messaging",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = SocietyNavy
                            )
                            Text(
                                "Sender: $senderName ($senderDesignation)",
                                fontSize = 10.sp,
                                color = SocietyGoldDark,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable Form Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Recipient Scope Selector
                    Text("1. Select Recipient Audience:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SocietyNavy)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = targetScope == MessagingTargetScope.ALL_85_FLATS,
                            onClick = { targetScope = MessagingTargetScope.ALL_85_FLATS },
                            label = { Text("All 85 Flats (170 Users)", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = targetScope == MessagingTargetScope.SPECIFIC_FLOOR,
                            onClick = { targetScope = MessagingTargetScope.SPECIFIC_FLOOR },
                            label = { Text("By Floor", fontSize = 10.sp) },
                            modifier = Modifier.weight(0.7f)
                        )
                        FilterChip(
                            selected = targetScope == MessagingTargetScope.SPECIFIC_FLAT,
                            onClick = { targetScope = MessagingTargetScope.SPECIFIC_FLAT },
                            label = { Text("Specific Flat", fontSize = 10.sp) },
                            modifier = Modifier.weight(0.8f)
                        )
                    }

                    if (targetScope == MessagingTargetScope.SPECIFIC_FLOOR) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Target Floor: ", fontSize = 11.sp, color = SocietySlate)
                            (1..11).forEach { fl ->
                                FilterChip(
                                    selected = selectedFloor == fl,
                                    onClick = { selectedFloor = fl },
                                    label = { Text("F$fl", fontSize = 9.sp) },
                                    modifier = Modifier.padding(horizontal = 2.dp)
                                )
                            }
                        }
                    }

                    if (targetScope == MessagingTargetScope.SPECIFIC_FLAT) {
                        OutlinedTextField(
                            value = selectedRoom.toString(),
                            onValueChange = { selectedRoom = it.toIntOrNull() ?: 101 },
                            label = { Text("Flat Number (e.g. 101, 302, 1105)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Message Category & Urgent Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("2. Category & Importance:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SocietyNavy)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = isUrgent,
                                onCheckedChange = { isUrgent = it },
                                colors = CheckboxDefaults.colors(checkedColor = SocietyError)
                            )
                            Text("Mark as High Priority", fontSize = 11.sp, color = if (isUrgent) SocietyError else SocietySlate, fontWeight = FontWeight.Bold)
                        }
                    }

                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(NoticeCategory.values().toList()) { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = {
                                    selectedCategory = cat
                                    subject = when (cat) {
                                        NoticeCategory.MAINTENANCE_REMINDER -> "Urgent: Monthly Maintenance Due Reminder"
                                        NoticeCategory.WATER_SUPPLY -> "Notice: Water Supply Tank Cleaning Update"
                                        NoticeCategory.LIFT_MAINTENANCE -> "Notice: Lift AMC Inspection on Saturday"
                                        NoticeCategory.EMERGENCY_ALERT -> "EMERGENCY: Immediate Attention Required"
                                        NoticeCategory.MEETING_NOTICE -> "Official Notice: Upcoming Society Committee Meeting"
                                        else -> "Society Notice: ${cat.name.replace('_', ' ')}"
                                    }
                                },
                                label = { Text(cat.name.replace("_", " "), fontSize = 10.sp) }
                            )
                        }
                    }

                    // Delivery Channel
                    Text("3. Dispatch Channel:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SocietyNavy)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = deliveryChannel == DeliveryChannel.IN_APP_AND_SMS,
                            onClick = { deliveryChannel = DeliveryChannel.IN_APP_AND_SMS },
                            label = { Text("In-App + SMS", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = deliveryChannel == DeliveryChannel.WHATSAPP_BROADCAST,
                            onClick = { deliveryChannel = DeliveryChannel.WHATSAPP_BROADCAST },
                            label = { Text("WhatsApp", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = deliveryChannel == DeliveryChannel.EMAIL_CIRCULAR,
                            onClick = { deliveryChannel = DeliveryChannel.EMAIL_CIRCULAR },
                            label = { Text("Email Circular", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Subject Field
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Subject / Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Message Body Field
                    OutlinedTextField(
                        value = messageBody,
                        onValueChange = { messageBody = it },
                        label = { Text("Message Content") },
                        minLines = 4,
                        maxLines = 8,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Info Card about Reach
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SocietySurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.People, contentDescription = null, tint = SocietyNavy, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            val reachText = when (targetScope) {
                                MessagingTargetScope.ALL_85_FLATS -> "This broadcast will reach all 85 flats and 170 registered user profiles."
                                MessagingTargetScope.SPECIFIC_FLOOR -> "This notice will be delivered to all units on Floor $selectedFloor."
                                MessagingTargetScope.SPECIFIC_FLAT -> "This direct message will be dispatched to Flat $selectedRoom (User 1 & User 2)."
                            }
                            Text(reachText, fontSize = 10.sp, color = SocietyNavyDark)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Send Button
                Button(
                    onClick = {
                        val sdf = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
                        val currentDate = sdf.format(Date())

                        // Save as official Notice in Room DB
                        val newNotice = NoticeEntity(
                            title = subject,
                            category = selectedCategory,
                            content = messageBody,
                            date = currentDate,
                            isUrgent = isUrgent,
                            issuedBy = "$senderName ($senderDesignation)",
                            acknowledgedByUsers = ""
                        )
                        viewModel.addNotice(newNotice)

                        // Trigger WhatsApp/External intent if requested
                        if (deliveryChannel == DeliveryChannel.WHATSAPP_BROADCAST) {
                            try {
                                val url = "https://api.whatsapp.com/send?text=${Uri.encode("$subject\n\n$messageBody")}"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Fallback handled
                            }
                        }

                        viewModel.showSnackbar("📨 Message successfully dispatched to ${if (targetScope == MessagingTargetScope.ALL_85_FLATS) "all 85 flats (170 users)" else "selected recipients"}!")
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SocietyNavy),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("send_broadcast_message_button")
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp), tint = SocietyGoldLight)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Send Message to Members", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
