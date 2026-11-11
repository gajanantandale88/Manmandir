package com.example.data.session

import com.example.data.model.UserRole
import com.example.ui.viewmodel.ScreenTab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Granular privileges defined across the Manmandir CHS system.
 */
enum class UserPrivilege {
    // Executive & Financial Privileges
    VIEW_EXECUTIVE_FINANCIALS,
    VIEW_COLLECTION_SUMMARY,
    VIEW_EXPENSE_STATISTICS,
    VIEW_SOCIETY_BANK_BALANCE,

    // Operations & Maintenance Privileges
    COLLECT_MAINTENANCE_PAYMENTS,
    ISSUE_OFFICIAL_RECEIPTS,
    WAIVE_OR_OVERRIDE_PENALTY,
    GENERATE_MONTHLY_DEMANDS,
    RECORD_SOCIETY_EXPENSES,
    EDIT_FINANCIALS_EXPENSES_REVENUE,
    GENERATE_AND_PRINT_FINANCIAL_REPORTS,

    // Records, Documents & Governance Privileges
    MANAGE_SOCIETY_DOCUMENTS,
    REMOVE_SOCIETY_DOCUMENTS,
    MANAGE_INWARD_OUTWARD_CORRESPONDENCE,
    PUBLISH_OFFICIAL_NOTICES,
    MANAGE_AND_RESOLVE_COMPLAINTS,
    MANAGE_MEETINGS_AND_MINUTES,
    ASSIGN_COMMITTEE_TASKS,
    MANAGE_BUILDING_STRUCTURE,
    VIEW_FULL_MEMBER_DIRECTORY,
    EDIT_ALL_MEMBER_NAMES,
    SEND_MESSAGES_TO_MEMBERS,
    VIEW_AUDIT_TRAIL,
    EDIT_SOCIETY_CONFIG_SETTINGS,

    // Resident-Centric Privileges
    VIEW_PERSONAL_MAINTENANCE,
    VIEW_PERSONAL_RECEIPTS,
    VIEW_PERSONAL_DOCUMENTS,
    RAISE_COMPLAINT_TICKET,
    VIEW_PUBLIC_NOTICES,
    VIEW_COMMITTEE_CONTACTS
}

/**
 * Profile of the currently authenticated user in the session.
 */
data class UserProfile(
    val userId: String,
    val name: String,
    val role: UserRole,
    val designation: String,
    val roomNumber: Int? = null,
    val slotNumber: Int? = null, // 1 for Primary Member, 2 for Joint Member / Tenant
    val email: String,
    val phone: String,
    val avatarInitials: String,
    val isOwner: Boolean = true,
    val lastLogin: String = "Today, Active"
)

/**
 * Configuration for dashboard sections visibility based on privileges.
 */
data class DashboardVisibilityConfig(
    val showExecutiveFinancialOverview: Boolean,
    val showCollectionProgressBar: Boolean,
    val showPendingDefaultersList: Boolean,
    val showRecentExpensesSnapshot: Boolean,
    val showBuildingStructureSnapshot: Boolean,
    val showUrgentActionsBar: Boolean,
    val showPersonalResidentCard: Boolean,
    val showPersonalDocumentChecklist: Boolean,
    val showPersonalRecentReceipts: Boolean,
    val showAuditTrailQuickWidget: Boolean,
    val showComplaintsQuickDesk: Boolean,
    val showUpcomingMeetingsQuickList: Boolean,
    val accessibleModules: List<ScreenTab>,
    val restrictedModules: List<ScreenTab>
)

/**
 * Complete active user session state.
 */
data class UserSession(
    val currentUser: UserProfile,
    val privileges: Set<UserPrivilege>,
    val dashboardVisibility: DashboardVisibilityConfig,
    val accessibleTabs: List<ScreenTab>,
    val isAuthenticated: Boolean = true
)

/**
 * User Session Manager
 * Handles authentication roles (Admin, Chairman, Secretary, Treasurer, Committee, Resident),
 * 170 flat users login access (up to 2 per flat for 85 flats), and role-based permissions.
 */
class UserSessionManager {

    companion object {
        // Pre-configured executive management profiles
        val ADMIN_USER = UserProfile(
            userId = "admin_001",
            name = "Rajesh Sharma",
            role = UserRole.SUPER_ADMIN,
            designation = "Super Admin (Full Authorization)",
            roomNumber = 101,
            email = "admin@manmandir-chs.org",
            phone = "+91 98201 11223",
            avatarInitials = "SA",
            isOwner = true
        )

        val CHAIRMAN_USER = UserProfile(
            userId = "chair_001",
            name = "Suresh Kadam",
            role = UserRole.CHAIRMAN,
            designation = "Society Chairman (Full Authorization)",
            roomNumber = 201,
            email = "chairman@manmandir-chs.org",
            phone = "+91 98201 55667",
            avatarInitials = "SK",
            isOwner = true
        )

        val SECRETARY_USER = UserProfile(
            userId = "sec_001",
            name = "Ramesh Verma",
            role = UserRole.SECRETARY,
            designation = "Hon. Secretary (Records & Docs Removal)",
            roomNumber = 301,
            email = "secretary@manmandir-chs.org",
            phone = "+91 98202 33445",
            avatarInitials = "RV",
            isOwner = true
        )

        val TREASURER_USER = UserProfile(
            userId = "treas_001",
            name = "Sunita Patil",
            role = UserRole.TREASURER,
            designation = "Treasurer (Finances & Reports PDF)",
            roomNumber = 401,
            email = "treasurer@manmandir-chs.org",
            phone = "+91 98203 77889",
            avatarInitials = "SP",
            isOwner = true
        )

        val COMMITTEE_MEMBER_USER = UserProfile(
            userId = "comm_001",
            name = "Sneha Kulkarni",
            role = UserRole.COMMITTEE_MEMBER,
            designation = "Committee Member (Operations)",
            roomNumber = 501,
            email = "sneha.kulkarni@manmandir-chs.org",
            phone = "+91 98204 88990",
            avatarInitials = "SK",
            isOwner = true
        )

        val EXECUTIVE_PROFILES = listOf(
            ADMIN_USER,
            CHAIRMAN_USER,
            SECRETARY_USER,
            TREASURER_USER,
            COMMITTEE_MEMBER_USER
        )

        // Generate the 170 Flat User Profiles (85 flats × 2 users per flat)
        fun generateAll170FlatUsers(): List<UserProfile> {
            val list = mutableListOf<UserProfile>()
            val floors = 1..11
            for (floor in floors) {
                val maxUnits = if (floor == 11) 5 else 8
                for (unitIdx in 1..maxUnits) {
                    val room = floor * 100 + unitIdx
                    // User 1 (Primary Member / Owner or Primary Tenant)
                    val isTenant = (unitIdx == 2 || unitIdx == 6)
                    val user1Name = if (isTenant) "Tenant Resident $room" else "Flat Owner $room"
                    val user2Name = if (isTenant) "Flat Owner $room (Landlord)" else "Joint Member / Spouse $room"
                    
                    list.add(
                        UserProfile(
                            userId = "flat_${room}_user1",
                            name = user1Name,
                            role = UserRole.RESIDENT,
                            designation = if (isTenant) "Flat $room - Primary Tenant (User 1)" else "Flat $room - Primary Owner (User 1)",
                            roomNumber = room,
                            slotNumber = 1,
                            email = "user1.$room@manmandir-chs.org",
                            phone = "+91 98201 ${1000 + room}",
                            avatarInitials = "${room % 100}A",
                            isOwner = !isTenant
                        )
                    )

                    // User 2 (Joint Member / Co-Owner / Co-Tenant)
                    list.add(
                        UserProfile(
                            userId = "flat_${room}_user2",
                            name = user2Name,
                            role = UserRole.RESIDENT,
                            designation = if (isTenant) "Flat $room - Landlord / Owner (User 2)" else "Flat $room - Joint Member / Co-Owner (User 2)",
                            roomNumber = room,
                            slotNumber = 2,
                            email = "user2.$room@manmandir-chs.org",
                            phone = "+91 98202 ${2000 + room}",
                            avatarInitials = "${room % 100}B",
                            isOwner = true
                        )
                    )
                }
            }
            return list
        }

        val ALL_170_FLAT_USERS by lazy { generateAll170FlatUsers() }

        val PRESET_PROFILES = listOf(
            ADMIN_USER,
            CHAIRMAN_USER,
            SECRETARY_USER,
            TREASURER_USER,
            COMMITTEE_MEMBER_USER,
            ALL_170_FLAT_USERS.first(), // Flat 101 User 1
            ALL_170_FLAT_USERS[1]       // Flat 101 User 2
        )
    }

    private val _session = MutableStateFlow(createSessionForUser(ADMIN_USER))
    val session: StateFlow<UserSession> = _session.asStateFlow()

    val currentUser: StateFlow<UserProfile>
        get() = MutableStateFlow(_session.value.currentUser).asStateFlow()

    val currentRole: StateFlow<UserRole>
        get() = MutableStateFlow(_session.value.currentUser.role).asStateFlow()

    /**
     * Switch logged-in user profile to one of the preset accounts or any of the 170 flat users.
     */
    fun switchUser(user: UserProfile) {
        _session.value = createSessionForUser(user)
    }

    /**
     * Switch role directly, mapping to an appropriate default profile.
     */
    fun switchRole(role: UserRole, roomNumber: Int? = null, slotNumber: Int = 1) {
        val targetProfile = when (role) {
            UserRole.SUPER_ADMIN -> ADMIN_USER
            UserRole.CHAIRMAN -> CHAIRMAN_USER
            UserRole.SECRETARY -> SECRETARY_USER
            UserRole.TREASURER -> TREASURER_USER
            UserRole.COMMITTEE_MEMBER -> COMMITTEE_MEMBER_USER
            UserRole.RESIDENT -> {
                val room = roomNumber ?: _session.value.currentUser.roomNumber ?: 101
                val slot = slotNumber
                val existing = ALL_170_FLAT_USERS.find { it.roomNumber == room && it.slotNumber == slot }
                existing ?: UserProfile(
                    userId = "flat_${room}_user$slot",
                    name = "Resident Flat $room (User $slot)",
                    role = UserRole.RESIDENT,
                    designation = "Flat $room - Member Slot $slot",
                    roomNumber = room,
                    slotNumber = slot,
                    email = "user$slot.$room@manmandir-chs.org",
                    phone = "+91 98200 00$room",
                    avatarInitials = "R$room",
                    isOwner = slot == 1
                )
            }
        }
        _session.value = createSessionForUser(targetProfile)
    }

    /**
     * Switch resident room number when in Resident mode.
     */
    fun switchResidentRoom(
        roomNumber: Int,
        isOwner: Boolean = true,
        residentName: String? = null,
        slotNumber: Int = 1
    ) {
        val current = _session.value.currentUser
        val updatedProfile = current.copy(
            userId = "flat_${roomNumber}_user$slotNumber",
            name = residentName ?: (if (isOwner) "Owner (Flat $roomNumber, Slot $slotNumber)" else "Tenant (Flat $roomNumber, Slot $slotNumber)"),
            role = UserRole.RESIDENT,
            designation = if (isOwner) "Flat $roomNumber Owner (User $slotNumber)" else "Flat $roomNumber Tenant (User $slotNumber)",
            roomNumber = roomNumber,
            slotNumber = slotNumber,
            email = "user$slotNumber.$roomNumber@manmandir-chs.org",
            avatarInitials = "R$roomNumber",
            isOwner = isOwner
        )
        _session.value = createSessionForUser(updatedProfile)
    }

    /**
     * Check if active session holds the specified privilege.
     */
    fun hasPrivilege(privilege: UserPrivilege): Boolean {
        return _session.value.privileges.contains(privilege)
    }

    /**
     * Check if active session can access a given navigation screen tab.
     */
    fun canAccessTab(tab: ScreenTab): Boolean {
        return _session.value.accessibleTabs.contains(tab)
    }

    /**
     * Returns the list of all executive profiles.
     */
    fun getExecutiveProfiles(): List<UserProfile> = EXECUTIVE_PROFILES

    /**
     * Returns all 170 registered flat user profiles.
     */
    fun getAll170FlatUsers(): List<UserProfile> = ALL_170_FLAT_USERS

    /**
     * Helper to verify if user can send messages (Admin, Chairman, Secretary, Treasurer).
     */
    fun canSendMessage(): Boolean {
        return hasPrivilege(UserPrivilege.SEND_MESSAGES_TO_MEMBERS)
    }

    /**
     * Helper to verify if user has full authorization & edit option for member names & 2 users per flat (Admin, Chairman).
     */
    fun canEditMemberNames(): Boolean {
        return hasPrivilege(UserPrivilege.EDIT_ALL_MEMBER_NAMES)
    }

    /**
     * Helper to verify if user can access & remove documents (Secretary, Admin, Chairman).
     */
    fun canRemoveDocuments(): Boolean {
        return hasPrivilege(UserPrivilege.REMOVE_SOCIETY_DOCUMENTS)
    }

    /**
     * Helper to verify if user can edit maintenance, expenses, revenue and print/download reports (Treasurer, Admin, Chairman).
     */
    fun canEditFinancialsAndDownloadReports(): Boolean {
        return hasPrivilege(UserPrivilege.EDIT_FINANCIALS_EXPENSES_REVENUE) ||
               hasPrivilege(UserPrivilege.GENERATE_AND_PRINT_FINANCIAL_REPORTS)
    }

    /**
     * Logs out the session by resetting to Flat 101 User 1.
     */
    fun logout() {
        switchRole(UserRole.RESIDENT, 101, 1)
    }

    // Helper logic to compute privileges, accessible tabs, and dashboard visibility
    private fun createSessionForUser(user: UserProfile): UserSession {
        val privileges = computePrivileges(user.role)
        val accessibleTabs = computeAccessibleTabs(user.role)
        val dashboardVisibility = computeDashboardVisibility(user.role, accessibleTabs)

        return UserSession(
            currentUser = user,
            privileges = privileges,
            dashboardVisibility = dashboardVisibility,
            accessibleTabs = accessibleTabs,
            isAuthenticated = true
        )
    }

    private fun computePrivileges(role: UserRole): Set<UserPrivilege> {
        return when (role) {
            // Super Admin & Chairman have FULL authorization & edit access
            UserRole.SUPER_ADMIN,
            UserRole.CHAIRMAN -> UserPrivilege.values().toSet()

            // Secretary has full member directory access, view/upload and REMOVE documents, and can send messages
            UserRole.SECRETARY -> setOf(
                UserPrivilege.VIEW_EXECUTIVE_FINANCIALS,
                UserPrivilege.VIEW_COLLECTION_SUMMARY,
                UserPrivilege.VIEW_EXPENSE_STATISTICS,
                UserPrivilege.VIEW_SOCIETY_BANK_BALANCE,
                UserPrivilege.MANAGE_SOCIETY_DOCUMENTS,
                UserPrivilege.REMOVE_SOCIETY_DOCUMENTS,
                UserPrivilege.MANAGE_INWARD_OUTWARD_CORRESPONDENCE,
                UserPrivilege.PUBLISH_OFFICIAL_NOTICES,
                UserPrivilege.SEND_MESSAGES_TO_MEMBERS,
                UserPrivilege.MANAGE_AND_RESOLVE_COMPLAINTS,
                UserPrivilege.MANAGE_MEETINGS_AND_MINUTES,
                UserPrivilege.ASSIGN_COMMITTEE_TASKS,
                UserPrivilege.MANAGE_BUILDING_STRUCTURE,
                UserPrivilege.VIEW_FULL_MEMBER_DIRECTORY,
                UserPrivilege.VIEW_AUDIT_TRAIL,
                UserPrivilege.VIEW_PERSONAL_MAINTENANCE,
                UserPrivilege.VIEW_PERSONAL_RECEIPTS,
                UserPrivilege.VIEW_PERSONAL_DOCUMENTS,
                UserPrivilege.RAISE_COMPLAINT_TICKET,
                UserPrivilege.VIEW_PUBLIC_NOTICES,
                UserPrivilege.VIEW_COMMITTEE_CONTACTS
            )

            // Treasurer has edit access to maintenance, expenses, revenue, can print and download PDF reports, and send messages
            UserRole.TREASURER -> setOf(
                UserPrivilege.VIEW_EXECUTIVE_FINANCIALS,
                UserPrivilege.VIEW_COLLECTION_SUMMARY,
                UserPrivilege.VIEW_EXPENSE_STATISTICS,
                UserPrivilege.VIEW_SOCIETY_BANK_BALANCE,
                UserPrivilege.COLLECT_MAINTENANCE_PAYMENTS,
                UserPrivilege.ISSUE_OFFICIAL_RECEIPTS,
                UserPrivilege.WAIVE_OR_OVERRIDE_PENALTY,
                UserPrivilege.GENERATE_MONTHLY_DEMANDS,
                UserPrivilege.RECORD_SOCIETY_EXPENSES,
                UserPrivilege.EDIT_FINANCIALS_EXPENSES_REVENUE,
                UserPrivilege.GENERATE_AND_PRINT_FINANCIAL_REPORTS,
                UserPrivilege.SEND_MESSAGES_TO_MEMBERS,
                UserPrivilege.MANAGE_SOCIETY_DOCUMENTS,
                UserPrivilege.VIEW_FULL_MEMBER_DIRECTORY,
                UserPrivilege.VIEW_AUDIT_TRAIL,
                UserPrivilege.VIEW_PERSONAL_MAINTENANCE,
                UserPrivilege.VIEW_PERSONAL_RECEIPTS,
                UserPrivilege.VIEW_PERSONAL_DOCUMENTS,
                UserPrivilege.RAISE_COMPLAINT_TICKET,
                UserPrivilege.VIEW_PUBLIC_NOTICES,
                UserPrivilege.VIEW_COMMITTEE_CONTACTS
            )

            // General Committee Member
            UserRole.COMMITTEE_MEMBER -> setOf(
                UserPrivilege.VIEW_EXECUTIVE_FINANCIALS,
                UserPrivilege.VIEW_COLLECTION_SUMMARY,
                UserPrivilege.VIEW_EXPENSE_STATISTICS,
                UserPrivilege.VIEW_SOCIETY_BANK_BALANCE,
                UserPrivilege.MANAGE_SOCIETY_DOCUMENTS,
                UserPrivilege.MANAGE_INWARD_OUTWARD_CORRESPONDENCE,
                UserPrivilege.PUBLISH_OFFICIAL_NOTICES,
                UserPrivilege.MANAGE_AND_RESOLVE_COMPLAINTS,
                UserPrivilege.MANAGE_MEETINGS_AND_MINUTES,
                UserPrivilege.ASSIGN_COMMITTEE_TASKS,
                UserPrivilege.VIEW_FULL_MEMBER_DIRECTORY,
                UserPrivilege.VIEW_AUDIT_TRAIL,
                UserPrivilege.VIEW_PERSONAL_MAINTENANCE,
                UserPrivilege.VIEW_PERSONAL_RECEIPTS,
                UserPrivilege.VIEW_PERSONAL_DOCUMENTS,
                UserPrivilege.RAISE_COMPLAINT_TICKET,
                UserPrivilege.VIEW_PUBLIC_NOTICES,
                UserPrivilege.VIEW_COMMITTEE_CONTACTS
            )

            // Standard Resident (170 flat users)
            UserRole.RESIDENT -> setOf(
                UserPrivilege.VIEW_PERSONAL_MAINTENANCE,
                UserPrivilege.VIEW_PERSONAL_RECEIPTS,
                UserPrivilege.VIEW_PERSONAL_DOCUMENTS,
                UserPrivilege.RAISE_COMPLAINT_TICKET,
                UserPrivilege.VIEW_PUBLIC_NOTICES,
                UserPrivilege.VIEW_COMMITTEE_CONTACTS
            )
        }
    }

    private fun computeAccessibleTabs(role: UserRole): List<ScreenTab> {
        return when (role) {
            UserRole.SUPER_ADMIN,
            UserRole.CHAIRMAN,
            UserRole.SECRETARY,
            UserRole.TREASURER,
            UserRole.COMMITTEE_MEMBER -> listOf(
                ScreenTab.DASHBOARD,
                ScreenTab.BUILDING_STRUCTURE,
                ScreenTab.MAINTENANCE,
                ScreenTab.RECEIPTS,
                ScreenTab.FINANCIALS,
                ScreenTab.DOCUMENTS,
                ScreenTab.CORRESPONDENCE,
                ScreenTab.EXPENSES,
                ScreenTab.NOTICES,
                ScreenTab.COMPLAINTS,
                ScreenTab.MEETINGS_TASKS,
                ScreenTab.DIRECTORY,
                ScreenTab.AUDIT_SETTINGS
            )

            UserRole.RESIDENT -> listOf(
                ScreenTab.DASHBOARD,
                ScreenTab.BUILDING_STRUCTURE,
                ScreenTab.MAINTENANCE,
                ScreenTab.RECEIPTS,
                ScreenTab.DOCUMENTS,
                ScreenTab.NOTICES,
                ScreenTab.COMPLAINTS,
                ScreenTab.DIRECTORY
            )
        }
    }

    private fun computeDashboardVisibility(
        role: UserRole,
        accessibleTabs: List<ScreenTab>
    ): DashboardVisibilityConfig {
        val allTabs = ScreenTab.values().toList()
        val restrictedTabs = allTabs.filterNot { accessibleTabs.contains(it) }

        return when (role) {
            UserRole.SUPER_ADMIN,
            UserRole.CHAIRMAN -> DashboardVisibilityConfig(
                showExecutiveFinancialOverview = true,
                showCollectionProgressBar = true,
                showPendingDefaultersList = true,
                showRecentExpensesSnapshot = true,
                showBuildingStructureSnapshot = true,
                showUrgentActionsBar = true,
                showPersonalResidentCard = false,
                showPersonalDocumentChecklist = false,
                showPersonalRecentReceipts = false,
                showAuditTrailQuickWidget = true,
                showComplaintsQuickDesk = true,
                showUpcomingMeetingsQuickList = true,
                accessibleModules = accessibleTabs,
                restrictedModules = emptyList()
            )

            UserRole.SECRETARY,
            UserRole.TREASURER,
            UserRole.COMMITTEE_MEMBER -> DashboardVisibilityConfig(
                showExecutiveFinancialOverview = true,
                showCollectionProgressBar = true,
                showPendingDefaultersList = true,
                showRecentExpensesSnapshot = true,
                showBuildingStructureSnapshot = true,
                showUrgentActionsBar = true,
                showPersonalResidentCard = false,
                showPersonalDocumentChecklist = false,
                showPersonalRecentReceipts = false,
                showAuditTrailQuickWidget = true,
                showComplaintsQuickDesk = true,
                showUpcomingMeetingsQuickList = true,
                accessibleModules = accessibleTabs,
                restrictedModules = emptyList()
            )

            UserRole.RESIDENT -> DashboardVisibilityConfig(
                showExecutiveFinancialOverview = false,
                showCollectionProgressBar = false,
                showPendingDefaultersList = false,
                showRecentExpensesSnapshot = false,
                showBuildingStructureSnapshot = true,
                showUrgentActionsBar = false,
                showPersonalResidentCard = true,
                showPersonalDocumentChecklist = true,
                showPersonalRecentReceipts = true,
                showAuditTrailQuickWidget = false,
                showComplaintsQuickDesk = true,
                showUpcomingMeetingsQuickList = false,
                accessibleModules = accessibleTabs,
                restrictedModules = restrictedTabs
            )
        }
    }
}
