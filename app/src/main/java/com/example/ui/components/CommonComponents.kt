package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.OccupancyType
import com.example.data.model.PaymentStatus
import com.example.data.model.ReceiptEntity
import com.example.data.model.UnitEntity
import com.example.data.model.UnitType
import com.example.data.model.UserRole
import com.example.data.repository.RoomMaintenanceSummary
import com.example.ui.theme.*
import com.example.ui.viewmodel.ScreenTab
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.localization.societyStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocietyTopHeader(
    currentRole: UserRole,
    currentResidentRoom: Int,
    currentLanguage: AppLanguage = LocalAppLanguage.current,
    onRoleChange: (UserRole) -> Unit,
    onResidentRoomChange: (Int) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit = {},
    onSearchClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    val s = societyStrings
    var showRoleMenu by remember { mutableStateOf(false) }
    var showLanguageMenu by remember { mutableStateOf(false) }
    var showRoomSelectorDialog by remember { mutableStateOf(false) }

    Surface(
        color = Color.White,
        tonalElevation = 3.dp,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onMenuClick,
                        modifier = Modifier.testTag("menu_drawer_button")
                    ) {
                        Icon(
                            Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = SocietyNavy
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = s.appName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.2.sp
                                ),
                                color = SocietyNavy
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = SocietyGoldContainer,
                                border = BorderStroke(1.dp, SocietyGoldLight)
                            ) {
                                Text(
                                    text = s.proposedBadge,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SocietyGoldDark,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = s.societyTagline,
                            style = MaterialTheme.typography.labelSmall,
                            color = SocietySlate,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Language Switcher Dropdown Chip
                    Box(modifier = Modifier.padding(end = 4.dp)) {
                        Surface(
                            onClick = { showLanguageMenu = true },
                            shape = RoundedCornerShape(18.dp),
                            color = SocietySurfaceVariant,
                            border = BorderStroke(1.dp, SocietyCardBorder),
                            modifier = Modifier.testTag("language_switcher_chip")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Language,
                                    contentDescription = s.languageLabel,
                                    tint = SocietyNavy,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = when (currentLanguage) {
                                        AppLanguage.ENGLISH -> "EN"
                                        AppLanguage.MARATHI -> "मराठी"
                                        AppLanguage.HINDI -> "हिंदी"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SocietyNavyDark
                                )
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = SocietyNavy,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showLanguageMenu,
                            onDismissRequest = { showLanguageMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("🇬🇧  English")
                                        if (currentLanguage == AppLanguage.ENGLISH) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Icon(Icons.Default.Check, contentDescription = null, tint = SocietyNavy, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                },
                                onClick = {
                                    onLanguageChange(AppLanguage.ENGLISH)
                                    showLanguageMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("🇮🇳  मराठी (Marathi)", fontWeight = if (currentLanguage == AppLanguage.MARATHI) FontWeight.Bold else FontWeight.Normal)
                                        if (currentLanguage == AppLanguage.MARATHI) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Icon(Icons.Default.Check, contentDescription = null, tint = SocietyNavy, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                },
                                onClick = {
                                    onLanguageChange(AppLanguage.MARATHI)
                                    showLanguageMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("🇮🇳  हिंदी (Hindi)", fontWeight = if (currentLanguage == AppLanguage.HINDI) FontWeight.Bold else FontWeight.Normal)
                                        if (currentLanguage == AppLanguage.HINDI) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Icon(Icons.Default.Check, contentDescription = null, tint = SocietyNavy, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                },
                                onClick = {
                                    onLanguageChange(AppLanguage.HINDI)
                                    showLanguageMenu = false
                                }
                            )
                        }
                    }

                    IconButton(
                        onClick = onSearchClick,
                        modifier = Modifier.testTag("global_search_button")
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = s.searchPlaceholder,
                            tint = SocietyNavy
                        )
                    }

                    // Role Switcher Chip
                    Box {
                        Surface(
                            onClick = { showRoleMenu = true },
                            shape = RoundedCornerShape(20.dp),
                            color = when (currentRole) {
                                UserRole.SUPER_ADMIN -> Color(0xFF7B1FA2)
                                UserRole.CHAIRMAN -> Color(0xFFC2185B)
                                UserRole.SECRETARY -> Color(0xFF1976D2)
                                UserRole.TREASURER -> Color(0xFF00796B)
                                UserRole.COMMITTEE_MEMBER -> SocietyGold
                                UserRole.RESIDENT -> SocietyTeal
                            },
                            modifier = Modifier.testTag("role_switcher_chip")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = when (currentRole) {
                                        UserRole.SUPER_ADMIN -> Icons.Default.AdminPanelSettings
                                        UserRole.CHAIRMAN -> Icons.Default.Stars
                                        UserRole.SECRETARY -> Icons.Default.Assignment
                                        UserRole.TREASURER -> Icons.Default.AccountBalance
                                        UserRole.COMMITTEE_MEMBER -> Icons.Default.SupervisorAccount
                                        UserRole.RESIDENT -> Icons.Default.Home
                                    },
                                    contentDescription = null,
                                    tint = if (currentRole == UserRole.COMMITTEE_MEMBER) SocietyNavyDark else Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = when (currentRole) {
                                        UserRole.SUPER_ADMIN -> s.roleSuperAdmin
                                        UserRole.CHAIRMAN -> s.roleChairman
                                        UserRole.SECRETARY -> s.roleSecretary
                                        UserRole.TREASURER -> s.roleTreasurer
                                        UserRole.COMMITTEE_MEMBER -> s.roleCommittee
                                        UserRole.RESIDENT -> "${s.flatPrefix} $currentResidentRoom"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (currentRole == UserRole.COMMITTEE_MEMBER) SocietyNavyDark else Color.White
                                )
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = if (currentRole == UserRole.COMMITTEE_MEMBER) SocietyNavyDark else Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showRoleMenu,
                            onDismissRequest = { showRoleMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("${s.roleSuperAdmin} (Full Access)") },
                                leadingIcon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null) },
                                onClick = {
                                    onRoleChange(UserRole.SUPER_ADMIN)
                                    showRoleMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(s.roleCommittee) },
                                leadingIcon = { Icon(Icons.Default.SupervisorAccount, contentDescription = null) },
                                onClick = {
                                    onRoleChange(UserRole.COMMITTEE_MEMBER)
                                    showRoleMenu = false
                                }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("${s.roleResident} (${s.roleSwitchPrompt}...)") },
                                leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                                onClick = {
                                    onRoleChange(UserRole.RESIDENT)
                                    showRoleMenu = false
                                    showRoomSelectorDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showRoomSelectorDialog) {
        RoomSelectorDialog(
            currentRoom = currentResidentRoom,
            onDismiss = { showRoomSelectorDialog = false },
            onSelectRoom = { room ->
                onResidentRoomChange(room)
                showRoomSelectorDialog = false
            }
        )
    }
}

@Composable
fun RoomSelectorDialog(
    currentRoom: Int,
    onDismiss: () -> Unit,
    onSelectRoom: (Int) -> Unit
) {
    var selectedFloor by remember { mutableIntStateOf(currentRoom / 100) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Select Resident Room",
                fontWeight = FontWeight.Bold,
                color = SocietyNavy
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Switch active resident view to simulate tenant/owner access for any flat in Wing A:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text("Select Floor (1 to 11):", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    (1..6).forEach { floor ->
                        FilterChip(
                            selected = selectedFloor == floor,
                            onClick = { selectedFloor = floor },
                            label = { Text("F$floor", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    (7..11).forEach { floor ->
                        FilterChip(
                            selected = selectedFloor == floor,
                            onClick = { selectedFloor = floor },
                            label = { Text("F$floor", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Select Unit on Floor $selectedFloor:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))

                val roomsOnFloor = (1..8).map { selectedFloor * 100 + it }
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    roomsOnFloor.chunked(4).forEach { rowRooms ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            rowRooms.forEach { room ->
                                val isOffice = room == 104
                                val isRefuge = room == 803 || room == 804
                                val isSelected = currentRoom == room

                                Surface(
                                    onClick = {
                                        if (!isOffice && !isRefuge) {
                                            onSelectRoom(room)
                                        }
                                    },
                                    enabled = !isOffice && !isRefuge,
                                    shape = RoundedCornerShape(8.dp),
                                    color = when {
                                        isSelected -> SocietyNavy
                                        isOffice -> Color(0xFFE0E7FF)
                                        isRefuge -> Color(0xFFF3E8FF)
                                        else -> SocietySurfaceVariant
                                    },
                                    border = if (isSelected) null else BorderStroke(1.dp, SocietyCardBorder),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "$room",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (isSelected) Color.White else if (isOffice || isRefuge) Color.Gray else SocietyNavy
                                        )
                                        Text(
                                            text = when {
                                                isOffice -> "Office"
                                                isRefuge -> "Refuge"
                                                room % 4 == 2 || room % 8 == 6 -> "Tenant"
                                                else -> "Owner"
                                            },
                                            fontSize = 9.sp,
                                            color = if (isSelected) SocietyGoldLight else if (isOffice || isRefuge) Color.Gray else SocietySlate
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun StatusBadge(
    status: PaymentStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label, icon) = when (status) {
        PaymentStatus.CURRENT_PAID -> Quadruple(SocietySuccessContainer, SocietySuccess, "Paid ✓", Icons.Default.CheckCircle)
        PaymentStatus.CURRENT_DUE -> Quadruple(SocietyGoldContainer, Color(0xFF8D6E00), "Due", Icons.Default.Schedule)
        PaymentStatus.ONE_MONTH_PENDING -> Quadruple(SocietyWarningContainer, SocietyWarning, "1 Mo Pending", Icons.Default.Warning)
        PaymentStatus.TWO_MONTHS_PENDING -> Quadruple(Color(0xFFFFE0B2), Color(0xFFE65100), "2 Mo Pending", Icons.Default.ReportProblem)
        PaymentStatus.PENALTY_APPLICABLE -> Quadruple(SocietyErrorContainer, SocietyError, "Penalty Active ⚠️", Icons.Default.Error)
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun MetricStatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, SocietyCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// Penalty Breakdown Modal
@Composable
fun PenaltyBreakdownDialog(
    summary: RoomMaintenanceSummary,
    onDismiss: () -> Unit,
    onOverridePenalty: ((demandId: Long, newPenalty: Double, reason: String) -> Unit)? = null,
    isCommittee: Boolean = false
) {
    var showOverrideInput by remember { mutableStateOf(false) }
    var overrideAmountText by remember { mutableStateOf("") }
    var overrideReasonText by remember { mutableStateOf("") }
    var targetDemandId by remember { mutableLongStateOf(0L) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Penalty Calculation",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = SocietyNavy
                        )
                        Text(
                            text = "Room ${summary.roomNumber} (${if (summary.unit?.occupancyType == OccupancyType.TENANT) "Tenant" else "Owner"})",
                            style = MaterialTheme.typography.bodySmall,
                            color = SocietySlate
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Society Rule Box
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SocietyGoldContainer,
                    border = BorderStroke(1.dp, SocietyGold.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFF8D6E00),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Society Penalty Rule:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFF8D6E00)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• 22% per month penalty is calculated automatically on outstanding maintenance dues after the 2-month grace period has elapsed.\n• Grace period: 2 Months from original demand date.",
                            fontSize = 11.sp,
                            color = SocietyNavyDark,
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Itemized Month Breakdown:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = SocietyNavy
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (summary.penaltyBreakdown.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SocietySuccessContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "✓ All maintenance dues are cleared. No pending balance or penalties applicable.",
                            fontSize = 12.sp,
                            color = SocietySuccess,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                } else {
                    // Table Header
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = SocietyNavyLight,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Month", modifier = Modifier.weight(1.3f), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("Maint.", modifier = Modifier.weight(0.9f), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                            Text("Paid", modifier = Modifier.weight(0.8f), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                            Text("Bal.", modifier = Modifier.weight(0.9f), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                            Text("Penalty", modifier = Modifier.weight(1.0f), color = SocietyGoldLight, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                            Text("Total", modifier = Modifier.weight(1.1f), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    summary.penaltyBreakdown.forEach { item ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (item.penaltyAmount > 0) SocietyErrorContainer.copy(alpha = 0.5f) else SocietySurfaceVariant,
                            border = BorderStroke(0.5.dp, SocietyCardBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1.3f)) {
                                    Text(item.monthYear, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SocietyNavy)
                                    Text(
                                        text = if (item.isBeyondGrace) ">2 Mo Grace" else "In Grace",
                                        fontSize = 9.sp,
                                        color = if (item.isBeyondGrace) SocietyError else SocietySlate
                                    )
                                }
                                Text("₹${item.maintenanceAmount.toInt()}", modifier = Modifier.weight(0.9f), fontSize = 11.sp, textAlign = TextAlign.End)
                                Text("₹${item.paidAmount.toInt()}", modifier = Modifier.weight(0.8f), fontSize = 11.sp, color = SocietySuccess, textAlign = TextAlign.End)
                                Text("₹${item.balance.toInt()}", modifier = Modifier.weight(0.9f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.End)
                                Text(
                                    text = if (item.penaltyAmount > 0) "+₹${item.penaltyAmount.toInt()}" else "₹0",
                                    modifier = Modifier.weight(1.0f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (item.penaltyAmount > 0) SocietyError else SocietySlate,
                                    textAlign = TextAlign.End
                                )
                                Text("₹${item.totalAmount.toInt()}", modifier = Modifier.weight(1.1f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SocietyNavy, textAlign = TextAlign.End)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Summary Totals Card
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SocietyNavyDark,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Unpaid Base Maintenance:", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                            Text("₹${summary.totalBasePending.toInt()}", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Accrued Penalty (22%/mo):", color = SocietyGoldLight, fontSize = 12.sp)
                            Text("+ ₹${summary.totalPenalty.toInt()}", color = SocietyGoldLight, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        HorizontalDivider(
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total Amount Payable:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("₹${summary.totalOutstanding.toInt()}", color = SocietyGoldLight, fontWeight = FontWeight.Black, fontSize = 18.sp)
                        }
                    }
                }

                if (isCommittee && onOverridePenalty != null && summary.totalPenalty > 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = {
                            val firstPenaltyDemand = summary.demands.find { it.isPaid.not() }
                            targetDemandId = firstPenaltyDemand?.id ?: 0L
                            showOverrideInput = !showOverrideInput
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Authorised Committee Penalty Override")
                    }

                    AnimatedVisibility(visible = showOverrideInput) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            OutlinedTextField(
                                value = overrideAmountText,
                                onValueChange = { overrideAmountText = it },
                                label = { Text("New Penalty Amount (₹)") },
                                placeholder = { Text("e.g. 0.0") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = overrideReasonText,
                                onValueChange = { overrideReasonText = it },
                                label = { Text("Reason for Adjustment (Audit Log Mandate)") },
                                placeholder = { Text("e.g. Medical emergency concession approved in MC Meet #13") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    val newAmt = overrideAmountText.toDoubleOrNull() ?: 0.0
                                    val reason = overrideReasonText.ifBlank { "Committee Discretionary Adjustment" }
                                    onOverridePenalty(targetDemandId, newAmt, reason)
                                    showOverrideInput = false
                                    onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SocietyNavy),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Confirm Adjustment & Record in Audit Log")
                            }
                        }
                    }
                }
            }
        }
    }
}

// Digital Maintenance Receipt Card & Modal
@Composable
fun DigitalReceiptDialog(
    receipt: ReceiptEntity,
    onDismiss: () -> Unit,
    onShare: () -> Unit = {},
    onDownload: () -> Unit = {}
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Receipt Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = SocietySuccessContainer
                    ) {
                        Text(
                            text = "OFFICIAL DIGITAL RECEIPT",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SocietySuccess,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Society Seal & Heading
                Text(
                    text = "MANMANDIR COOPERATIVE HOUSING SOCIETY",
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    color = SocietyNavy,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "(PROPOSED)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = SocietyGold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Wing A, Society Office Flat 104",
                    fontSize = 10.sp,
                    color = SocietySlate,
                    textAlign = TextAlign.Center
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = SocietyCardBorder
                )

                // Receipt Meta Box
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SocietySurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Receipt No.", fontSize = 10.sp, color = SocietySlate)
                                Text(receipt.receiptNumber, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SocietyNavy)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Date", fontSize = 10.sp, color = SocietySlate)
                                Text(receipt.date, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SocietyNavy)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Flat / Unit", fontSize = 10.sp, color = SocietySlate)
                                Text("Room ${receipt.roomNumber}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SocietyNavy)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Resident Status", fontSize = 10.sp, color = SocietySlate)
                                Text(receipt.occupancyType.name, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = SocietyNavy)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Column {
                            Text("Received With Thanks From:", fontSize = 10.sp, color = SocietySlate)
                            Text(receipt.residentName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SocietyNavy)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Breakdown Table
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, SocietyCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Payment Particulars (${receipt.maintenanceMonth}):",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = SocietyNavy
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        ReceiptLineItem("Monthly Maintenance Amount", "₹${receipt.maintenanceAmount.toInt()}")
                        if (receipt.previousOutstanding > 0) {
                            ReceiptLineItem("Previous Outstanding Arrears", "₹${receipt.previousOutstanding.toInt()}")
                        }
                        if (receipt.penaltyAmount > 0) {
                            ReceiptLineItem("Applicable Penalty (22%/mo)", "₹${receipt.penaltyAmount.toInt()}", isError = true)
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                        ReceiptLineItem("Total Amount Received", "₹${receipt.totalAmountReceived.toInt()}", isBold = true, isHighlight = true)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Payment Mode & Balance
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SocietySurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Payment Mode:", fontSize = 11.sp, color = SocietySlate)
                            Text(receipt.paymentMethod, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = SocietyNavy)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Transaction Ref / ID:", fontSize = 11.sp, color = SocietySlate)
                            Text(receipt.transactionRef, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SocietyNavy)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Balance Dues Post-Payment:", fontSize = 11.sp, color = SocietySlate)
                            Text("₹${receipt.remainingBalance.toInt()}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (receipt.remainingBalance > 0) SocietyError else SocietySuccess)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Authorized Signatory
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text("Computer-generated digital receipt", fontSize = 9.sp, color = Color.Gray)
                        Text("Valid without physical signature", fontSize = 9.sp, color = Color.Gray)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Issued by:", fontSize = 9.sp, color = SocietySlate)
                        Text(receipt.authorizedMember, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = SocietyNavy)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onShare,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share")
                    }
                    Button(
                        onClick = onDownload,
                        colors = ButtonDefaults.buttonColors(containerColor = SocietyNavy),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Download")
                    }
                }
            }
        }
    }
}

@Composable
private fun ReceiptLineItem(
    label: String,
    value: String,
    isBold: Boolean = false,
    isHighlight: Boolean = false,
    isError: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = if (isHighlight) SocietyNavy else SocietySlate
        )
        Text(
            text = value,
            fontSize = if (isHighlight) 14.sp else 12.sp,
            fontWeight = if (isBold || isHighlight) FontWeight.Bold else FontWeight.SemiBold,
            color = when {
                isError -> SocietyError
                isHighlight -> SocietySuccess
                else -> SocietyNavy
            }
        )
    }
}

@Composable
fun ManagementModulesGrid(
    accessibleTabs: List<ScreenTab>,
    restrictedTabs: List<ScreenTab>,
    onNavigate: (ScreenTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val allModules = listOf(
        Triple("Maintenance", Icons.Default.CurrencyRupee, ScreenTab.MAINTENANCE),
        Triple("Building / Units", Icons.Default.Apartment, ScreenTab.BUILDING_STRUCTURE),
        Triple("Digital Receipts", Icons.Default.ReceiptLong, ScreenTab.RECEIPTS),
        Triple("Documents", Icons.Default.Folder, ScreenTab.DOCUMENTS),
        Triple("Correspondence", Icons.Default.ForwardToInbox, ScreenTab.CORRESPONDENCE),
        Triple("Expenses & Bills", Icons.Default.Receipt, ScreenTab.EXPENSES),
        Triple("Notice Board", Icons.Default.Announcement, ScreenTab.NOTICES),
        Triple("Complaints Desk", Icons.Default.SupportAgent, ScreenTab.COMPLAINTS),
        Triple("Meetings & Tasks", Icons.Default.EventNote, ScreenTab.MEETINGS_TASKS),
        Triple("Directory", Icons.Default.Contacts, ScreenTab.DIRECTORY),
        Triple("Financial Summary", Icons.Default.Analytics, ScreenTab.FINANCIALS),
        Triple("Audit & Settings", Icons.Default.Settings, ScreenTab.AUDIT_SETTINGS)
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        allModules.chunked(3).forEach { rowModules ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowModules.forEach { (title, icon, tab) ->
                    val isAccessible = accessibleTabs.contains(tab)
                    Card(
                        onClick = { onNavigate(tab) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isAccessible) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        border = BorderStroke(1.dp, if (isAccessible) SocietyCardBorder else SocietyCardBorder.copy(alpha = 0.4f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (isAccessible) 1.dp else 0.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(95.dp)
                            .testTag("module_${tab.name.lowercase()}")
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isAccessible) SocietyNavyLight.copy(alpha = 0.1f)
                                            else Color.Gray.copy(alpha = 0.15f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = title,
                                        tint = if (isAccessible) SocietyNavy else Color.Gray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = title,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isAccessible) SocietyNavyDark else SocietySlate.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            if (!isAccessible) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color.Black.copy(alpha = 0.6f),
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(6.dp)
                                        .size(18.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Lock,
                                        contentDescription = "Restricted",
                                        tint = Color.White,
                                        modifier = Modifier.padding(3.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BuildingStructureOverviewCard(
    onExploreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, SocietyCardBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Apartment, contentDescription = null, tint = SocietyNavy, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Building Structure & Unit Matrix", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SocietyNavy)
                }
                TextButton(onClick = onExploreClick) {
                    Text("Floor Map", fontSize = 12.sp)
                }
            }

            Text(
                text = "Wing A: 11 Floors, 85 Total Residential Flats, 1 Society Office (104), 2 Refuge Areas (803, 804).",
                fontSize = 11.sp,
                color = SocietySlate
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SocietySurfaceVariant,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Floors", fontSize = 10.sp, color = SocietySlate)
                        Text("11", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SocietyNavyDark)
                    }
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SocietySurfaceVariant,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Flats", fontSize = 10.sp, color = SocietySlate)
                        Text("85 Units", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SocietyNavyDark)
                    }
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SocietySurfaceVariant,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Office", fontSize = 10.sp, color = SocietySlate)
                        Text("Flat 104", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SocietyGold)
                    }
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SocietySurfaceVariant,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Refuge", fontSize = 10.sp, color = SocietySlate)
                        Text("803/804", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7B1FA2))
                    }
                }
            }
        }
    }
}

@Composable
fun ResidentDocumentStatusCard(
    unit: UnitEntity?,
    onViewDocs: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, SocietyCardBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = SocietySuccess, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Statutory Compliance & Legal Records", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SocietyNavy)
                }
                TextButton(onClick = onViewDocs) {
                    Text("View Vault", fontSize = 12.sp)
                }
            }

            Text(
                text = "Statutory documents registered for Flat ${unit?.roomNumber ?: 205} under Maharashtra Co-operative Societies Act:",
                fontSize = 11.sp,
                color = SocietySlate
            )

            Spacer(modifier = Modifier.height(8.dp))

            val complianceItems = listOf(
                "Share Certificate" to true,
                "Registered Sale Deed / Agreement" to true,
                "Index II & Property Registration" to true,
                "Nomination Form Submitted (Appendix 14)" to (unit?.occupancyType != OccupancyType.TENANT)
            )

            complianceItems.forEach { (item, isDone) ->
                Row(
                    modifier = Modifier.padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (isDone) Icons.Default.CheckCircle else Icons.Default.HourglassEmpty,
                        contentDescription = null,
                        tint = if (isDone) SocietySuccess else SocietyWarning,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item,
                        fontSize = 11.sp,
                        color = if (isDone) SocietyNavyDark else SocietySlate,
                        fontWeight = if (isDone) FontWeight.Medium else FontWeight.Normal
                    )
                }
            }
        }
    }
}
