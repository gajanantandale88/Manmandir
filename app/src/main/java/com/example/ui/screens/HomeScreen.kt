package com.example.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.data.model.UserRole
import com.example.data.session.UserProfile
import com.example.data.session.UserPrivilege
import com.example.data.session.UserSession
import com.example.data.session.UserSessionManager
import com.example.ui.components.FinancialReportsDialog
import com.example.ui.components.MemberMessagingDialog
import com.example.ui.navigation.DashboardViewType
import com.example.ui.screens.dashboard.AdminDashboardView
import com.example.ui.screens.dashboard.CommitteeDashboardView
import com.example.ui.screens.dashboard.ResidentDashboardView
import com.example.ui.theme.*
import com.example.ui.viewmodel.SocietyViewModel

/**
 * Role-Based Dashboard Router & Navigation Hub
 * Checks the active user's role from UserSessionManager / NavigationState
 * and conditionally renders the appropriate dashboard view (Admin, Committee, or Resident).
 */
@Composable
fun HomeScreen(
    viewModel: SocietyViewModel,
    modifier: Modifier = Modifier
) {
    val userSession by viewModel.userSession.collectAsState()
    val navigationState by viewModel.navigationState.collectAsState()
    val allUnits by viewModel.allUnits.collectAsState()

    var showSessionDialog by remember { mutableStateOf(false) }
    var showFinancialReportsDialog by remember { mutableStateOf(false) }
    var showMessagingDialog by remember { mutableStateOf(false) }

    val activeDashboard = navigationState.activeDashboardView
    val canSendMessage = userSession.privileges.contains(UserPrivilege.SEND_MESSAGES_TO_MEMBERS)
    val canViewReports = userSession.privileges.contains(UserPrivilege.GENERATE_AND_PRINT_FINANCIAL_REPORTS) ||
            userSession.currentUser.role == UserRole.SUPER_ADMIN ||
            userSession.currentUser.role == UserRole.CHAIRMAN ||
            userSession.currentUser.role == UserRole.TREASURER

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SocietyBg)
    ) {
        // Sticky Top User Session Banner with Quick Role Switcher
        ActiveUserSessionHeaderCard(
            session = userSession,
            canSendMessage = canSendMessage,
            canViewReports = canViewReports,
            onManageSession = { showSessionDialog = true },
            onOpenMessaging = { showMessagingDialog = true },
            onOpenReports = { showFinancialReportsDialog = true },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Conditionally Render the Appropriate Role Dashboard View
        Crossfade(
            targetState = activeDashboard,
            animationSpec = tween(300),
            label = "role_dashboard_crossfade",
            modifier = Modifier.weight(1f)
        ) { dashboardType ->
            when (dashboardType) {
                DashboardViewType.ADMIN_DASHBOARD -> {
                    AdminDashboardView(
                        viewModel = viewModel,
                        userSession = userSession
                    )
                }
                DashboardViewType.COMMITTEE_DASHBOARD -> {
                    CommitteeDashboardView(
                        viewModel = viewModel,
                        userSession = userSession
                    )
                }
                DashboardViewType.RESIDENT_DASHBOARD -> {
                    ResidentDashboardView(
                        viewModel = viewModel,
                        userSession = userSession
                    )
                }
            }
        }
    }

    if (showSessionDialog) {
        UserSessionProfileDialog(
            userSession = userSession,
            allUnits = allUnits,
            onDismiss = { showSessionDialog = false },
            onSwitchUser = { profile ->
                viewModel.switchUser(profile)
                showSessionDialog = false
            }
        )
    }

    if (showFinancialReportsDialog) {
        FinancialReportsDialog(
            viewModel = viewModel,
            onDismiss = { showFinancialReportsDialog = false }
        )
    }

    if (showMessagingDialog) {
        MemberMessagingDialog(
            viewModel = viewModel,
            onDismiss = { showMessagingDialog = false }
        )
    }
}

@Composable
fun ActiveUserSessionHeaderCard(
    session: UserSession,
    canSendMessage: Boolean,
    canViewReports: Boolean,
    onManageSession: () -> Unit,
    onOpenMessaging: () -> Unit,
    onOpenReports: () -> Unit,
    modifier: Modifier = Modifier
) {
    val user = session.currentUser
    val roleColor = when (user.role) {
        UserRole.SUPER_ADMIN -> Color(0xFF7B1FA2)
        UserRole.CHAIRMAN -> Color(0xFFC2410C)
        UserRole.SECRETARY -> SocietyNavy
        UserRole.TREASURER -> SocietyGold
        UserRole.COMMITTEE_MEMBER -> Color(0xFF0284C7)
        UserRole.RESIDENT -> SocietyTeal
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, SocietyCardBorder),
        shadowElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(roleColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.avatarInitials,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            color = if (user.role == UserRole.TREASURER) SocietyNavyDark else Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = user.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = SocietyNavyDark
                            )
                            user.roomNumber?.let { room ->
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = SocietyNavy.copy(alpha = 0.08f)
                                ) {
                                    Text(
                                        text = "Flat $room${if (user.slotNumber != null) " (User ${user.slotNumber})" else ""}",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SocietyNavy,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = user.designation,
                            fontSize = 11.sp,
                            color = SocietySlate,
                            maxLines = 1
                        )
                    }
                }

                Button(
                    onClick = onManageSession,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = roleColor.copy(alpha = 0.15f),
                        contentColor = if (user.role == UserRole.TREASURER) SocietyNavyDark else roleColor
                    ),
                    shape = RoundedCornerShape(18.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                    modifier = Modifier.testTag("manage_session_button")
                ) {
                    Icon(
                        imageVector = when (user.role) {
                            UserRole.SUPER_ADMIN -> Icons.Default.AdminPanelSettings
                            UserRole.CHAIRMAN -> Icons.Default.VerifiedUser
                            UserRole.SECRETARY -> Icons.Default.FolderShared
                            UserRole.TREASURER -> Icons.Default.AccountBalance
                            UserRole.COMMITTEE_MEMBER -> Icons.Default.SupervisorAccount
                            UserRole.RESIDENT -> Icons.Default.Person
                        },
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = when (user.role) {
                            UserRole.SUPER_ADMIN -> "Admin"
                            UserRole.CHAIRMAN -> "Chairman"
                            UserRole.SECRETARY -> "Secretary"
                            UserRole.TREASURER -> "Treasurer"
                            UserRole.COMMITTEE_MEMBER -> "Committee"
                            UserRole.RESIDENT -> "Resident"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Quick Role Action Bar (Messages & Financial Reports)
            if (canSendMessage || canViewReports) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = SocietyCardBorder.copy(alpha = 0.6f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (canSendMessage) {
                        Surface(
                            onClick = onOpenMessaging,
                            shape = RoundedCornerShape(8.dp),
                            color = SocietyNavy.copy(alpha = 0.08f),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("header_send_message_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, tint = SocietyNavy, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Send Message to Members", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SocietyNavy)
                            }
                        }
                    }

                    if (canViewReports) {
                        Surface(
                            onClick = onOpenReports,
                            shape = RoundedCornerShape(8.dp),
                            color = SocietyGold.copy(alpha = 0.15f),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("header_financial_reports_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.Assessment, contentDescription = null, tint = SocietyNavyDark, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Income & Expense PDF", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SocietyNavyDark)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserSessionProfileDialog(
    userSession: UserSession,
    allUnits: List<com.example.data.model.UnitEntity>,
    onDismiss: () -> Unit,
    onSwitchUser: (UserProfile) -> Unit
) {
    val currentUser = userSession.currentUser
    val executiveProfiles = UserSessionManager.EXECUTIVE_PROFILES
    val all170FlatUsers = UserSessionManager.ALL_170_FLAT_USERS

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Executive Roles, 1: 170 Flat Users, 2: Active Privileges
    var flatSearchQuery by remember { mutableStateOf("") }
    var selectedFloorFilter by remember { mutableStateOf<Int?>(null) }

    val filteredFlatUsers = remember(all170FlatUsers, flatSearchQuery, selectedFloorFilter, allUnits) {
        all170FlatUsers.filter { user ->
            val room = user.roomNumber ?: 101
            val floor = room / 100
            val matchesFloor = selectedFloorFilter == null || floor == selectedFloorFilter
            val matchesQuery = if (flatSearchQuery.isBlank()) true else {
                val q = flatSearchQuery.trim().lowercase()
                room.toString().contains(q) || user.name.lowercase().contains(q) || user.phone.contains(q)
            }
            matchesFloor && matchesQuery
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Shield,
                    contentDescription = null,
                    tint = SocietyNavy,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "User Accounts & Role Access",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = SocietyNavy
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Active User Summary Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SocietyNavyDark,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    when (currentUser.role) {
                                        UserRole.SUPER_ADMIN -> Color(0xFF7B1FA2)
                                        UserRole.CHAIRMAN -> Color(0xFFC2410C)
                                        UserRole.SECRETARY -> SocietyNavyLight
                                        UserRole.TREASURER -> SocietyGold
                                        UserRole.COMMITTEE_MEMBER -> Color(0xFF0284C7)
                                        UserRole.RESIDENT -> SocietyTeal
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentUser.avatarInitials,
                                fontWeight = FontWeight.Bold,
                                color = if (currentUser.role == UserRole.TREASURER) SocietyNavyDark else Color.White,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(currentUser.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                            Text(currentUser.designation, color = SocietyGoldLight, fontSize = 10.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 3 Navigation Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = SocietyNavy
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Executives (5)", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("170 Flat Users", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Privileges (${userSession.privileges.size})", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                when (selectedTab) {
                    0 -> {
                        // Executive Committee Accounts
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 280.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(executiveProfiles) { profile ->
                                val isCurrent = profile.userId == currentUser.userId
                                Surface(
                                    onClick = { onSwitchUser(profile) },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isCurrent) SocietyGold.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                                    border = if (isCurrent) BorderStroke(1.5.dp, SocietyGold) else null,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        when (profile.role) {
                                                            UserRole.SUPER_ADMIN -> Color(0xFF7B1FA2)
                                                            UserRole.CHAIRMAN -> Color(0xFFC2410C)
                                                            UserRole.SECRETARY -> SocietyNavy
                                                            UserRole.TREASURER -> SocietyGold
                                                            UserRole.COMMITTEE_MEMBER -> Color(0xFF0284C7)
                                                            UserRole.RESIDENT -> SocietyTeal
                                                        }
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    profile.avatarInitials,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (profile.role == UserRole.TREASURER) SocietyNavyDark else Color.White
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(profile.name, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = SocietyNavyDark)
                                                Text(profile.designation, fontSize = 9.sp, color = SocietySlate)
                                            }
                                        }

                                        if (isCurrent) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = SocietyNavyDark
                                            ) {
                                                Text("LOGGED IN", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        // 170 Flat Users (85 flats × 2 users per flat)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 280.dp)
                        ) {
                            OutlinedTextField(
                                value = flatSearchQuery,
                                onValueChange = { flatSearchQuery = it },
                                placeholder = { Text("Search by Flat # or Name...", fontSize = 10.sp) },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SocietySlate, modifier = Modifier.size(16.dp)) },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                item {
                                    FilterChip(
                                        selected = selectedFloorFilter == null,
                                        onClick = { selectedFloorFilter = null },
                                        label = { Text("All", fontSize = 9.sp) }
                                    )
                                }
                                items((1..11).toList()) { fl ->
                                    FilterChip(
                                        selected = selectedFloorFilter == fl,
                                        onClick = { selectedFloorFilter = if (selectedFloorFilter == fl) null else fl },
                                        label = { Text("F$fl", fontSize = 9.sp) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text("Showing ${filteredFlatUsers.size} of 170 registered user logins:", fontSize = 9.sp, color = SocietySlate, fontWeight = FontWeight.SemiBold)

                            LazyColumn(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(filteredFlatUsers, key = { it.userId }) { user ->
                                    val isCurrent = user.userId == currentUser.userId
                                    Surface(
                                        onClick = { onSwitchUser(user) },
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isCurrent) SocietyGold.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                                        border = if (isCurrent) BorderStroke(1.5.dp, SocietyGold) else null,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = if (user.slotNumber == 1) SocietyNavy else SocietyTeal
                                                ) {
                                                    Text(
                                                        text = "F${user.roomNumber} • U${user.slotNumber}",
                                                        color = Color.White,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Column {
                                                    Text(user.name, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SocietyNavyDark)
                                                    Text(user.designation, fontSize = 8.sp, color = SocietySlate)
                                                }
                                            }

                                            if (isCurrent) {
                                                Surface(shape = RoundedCornerShape(4.dp), color = SocietyNavyDark) {
                                                    Text("ACTIVE", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        // Granted Privileges
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 280.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(userSession.privileges.toList()) { priv ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = SocietySuccess,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = priv.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                                        fontSize = 11.sp,
                                        color = SocietyNavyDark
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = SocietyNavy)
            ) {
                Text("Done")
            }
        }
    )
}
