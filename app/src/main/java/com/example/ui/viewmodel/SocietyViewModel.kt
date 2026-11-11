package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.AuditLogEntity
import com.example.data.model.CommitteeTaskEntity
import com.example.data.model.ComplaintEntity
import com.example.data.model.CorrespondenceEntity
import com.example.data.model.ExpenseEntity
import com.example.data.model.MaintenanceDemandEntity
import com.example.data.model.MaintenancePayment
import com.example.data.model.MaintenancePaymentStatus
import com.example.data.model.MaintenancePaymentWithResident
import com.example.data.model.MeetingEntity
import com.example.data.model.Notice
import com.example.data.model.NoticeEntity
import com.example.data.model.OccupancyType
import com.example.data.model.Payment
import com.example.data.model.PaymentEntity
import com.example.data.model.PaymentStatus
import com.example.data.model.ReceiptEntity
import com.example.data.model.Resident
import com.example.data.model.ResidentEntity
import com.example.data.model.ResidentWithMaintenancePayments
import com.example.data.model.SocietyDocumentEntity
import com.example.data.model.SocietySettingsEntity
import com.example.data.model.TaskStatus
import com.example.data.model.UnitEntity
import com.example.data.model.UnitType
import com.example.data.model.UserRole
import com.example.data.repository.MonthPenaltyDetail
import com.example.data.repository.RoomMaintenanceSummary
import com.example.data.repository.SocietyRepository
import com.example.data.session.DashboardVisibilityConfig
import com.example.data.session.UserProfile
import com.example.data.session.UserPrivilege
import com.example.data.session.UserSession
import com.example.data.session.UserSessionManager
import com.example.ui.navigation.AppNavigationManager
import com.example.ui.navigation.DashboardViewType
import com.example.ui.navigation.NavigationState
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.SocietyStrings
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ScreenTab {
    DASHBOARD,
    BUILDING_STRUCTURE,
    MAINTENANCE,
    RECEIPTS,
    FINANCIALS,
    DOCUMENTS,
    CORRESPONDENCE,
    EXPENSES,
    NOTICES,
    COMPLAINTS,
    MEETINGS_TASKS,
    DIRECTORY,
    AUDIT_SETTINGS
}

data class FinancialStats(
    val currentMonth: String = "August 2026",
    val totalExpectedCurrentMonth: Double = 0.0,
    val totalCollectedCurrentMonth: Double = 0.0,
    val totalPendingCurrentMonth: Double = 0.0,
    val totalPenaltyCurrentMonth: Double = 0.0,
    val paidMembersCount: Int = 0,
    val pendingMembersCount: Int = 0,
    val totalOverallOutstanding: Double = 0.0,
    val totalOverallPenalties: Double = 0.0,
    val totalSocietyExpenses: Double = 0.0,
    val openingBalance: Double = 485600.0,
    val closingBalance: Double = 485600.0,
    val collectionRatePercent: Double = 0.0
)

class SocietyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SocietyRepository

    init {
        val db = AppDatabase.getDatabase(application, viewModelScope)
        repository = SocietyRepository(
            dao = db.societyDao(),
            residentDao = db.residentDao(),
            maintenancePaymentDao = db.maintenancePaymentDao(),
            paymentDao = db.paymentDao(),
            noticeDao = db.noticeDao()
        )
    }

    // User Session & Authentication Management
    val sessionManager = UserSessionManager()
    val userSession: StateFlow<UserSession> = sessionManager.session
    val currentUser: StateFlow<UserProfile> = sessionManager.session.map { it.currentUser }
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserSessionManager.ADMIN_USER)

    // Central Navigation State Manager
    val navigationManager = AppNavigationManager(sessionManager)
    val navigationState: StateFlow<NavigationState> = navigationManager.navigationState

    // Language & Localization Preference
    private val prefs = application.getSharedPreferences("manmandir_prefs", Context.MODE_PRIVATE)
    private val _currentLanguage = MutableStateFlow(
        AppLanguage.fromCode(prefs.getString("selected_app_language", AppLanguage.ENGLISH.code))
    )
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    fun setLanguage(language: AppLanguage) {
        _currentLanguage.value = language
        prefs.edit().putString("selected_app_language", language.code).apply()
        val strings = SocietyStrings.get(language)
        showSnackbar("${strings.languageLabel}: ${language.nativeName} (${language.displayName})")
    }

    private val _currentRole = MutableStateFlow(UserRole.SUPER_ADMIN)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    private val _currentResidentRoom = MutableStateFlow(101)
    val currentResidentRoom: StateFlow<Int> = _currentResidentRoom.asStateFlow()

    private val _currentTab = MutableStateFlow(ScreenTab.DASHBOARD)
    val currentTab: StateFlow<ScreenTab> = _currentTab.asStateFlow()

    private val _floorFilter = MutableStateFlow<Int?>(null)
    val floorFilter: StateFlow<Int?> = _floorFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedUnit = MutableStateFlow<UnitEntity?>(null)
    val selectedUnit: StateFlow<UnitEntity?> = _selectedUnit.asStateFlow()

    private val _selectedReceipt = MutableStateFlow<ReceiptEntity?>(null)
    val selectedReceipt: StateFlow<ReceiptEntity?> = _selectedReceipt.asStateFlow()

    private val _selectedRoomForPenaltyView = MutableStateFlow<RoomMaintenanceSummary?>(null)
    val selectedRoomForPenaltyView: StateFlow<RoomMaintenanceSummary?> = _selectedRoomForPenaltyView.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    // Room Database Streams
    val allResidents: StateFlow<List<Resident>> = repository.allResidents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMaintenancePayments: StateFlow<List<MaintenancePayment>> = repository.allMaintenancePayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMaintenancePaymentsWithResidents: StateFlow<List<MaintenancePaymentWithResident>> =
        repository.allMaintenancePaymentsWithResidents
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val residentsWithMaintenancePayments: StateFlow<List<ResidentWithMaintenancePayments>> =
        repository.residentsWithMaintenancePayments
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPayments: StateFlow<List<Payment>> = repository.allPayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalCollectedPayments: StateFlow<Double> = repository.totalCollectedPayments
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val allUnits: StateFlow<List<UnitEntity>> = repository.allUnits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDemands: StateFlow<List<MaintenanceDemandEntity>> = repository.allDemands
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allReceipts: StateFlow<List<ReceiptEntity>> = repository.allReceipts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDocuments: StateFlow<List<SocietyDocumentEntity>> = repository.allDocuments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val residentDocuments: StateFlow<List<SocietyDocumentEntity>> = repository.residentDocuments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCorrespondence: StateFlow<List<CorrespondenceEntity>> = repository.allCorrespondence
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allExpenses: StateFlow<List<ExpenseEntity>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotices: StateFlow<List<NoticeEntity>> = repository.allNotices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTasks: StateFlow<List<CommitteeTaskEntity>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMeetings: StateFlow<List<MeetingEntity>> = repository.allMeetings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allComplaints: StateFlow<List<ComplaintEntity>> = repository.allComplaints
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAuditLogs: StateFlow<List<AuditLogEntity>> = repository.allAuditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settings: StateFlow<SocietySettingsEntity> = repository.settings
        .combine(_currentRole) { set, _ -> set ?: SocietySettingsEntity() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SocietySettingsEntity())

    // Computed Room Maintenance Summaries (Map of roomNumber -> RoomMaintenanceSummary)
    val roomSummaries: StateFlow<Map<Int, RoomMaintenanceSummary>> = combine(
        allUnits,
        allDemands,
        settings
    ) { units, demands, curSettings ->
        units.associate { unit ->
            unit.roomNumber to repository.calculateRoomSummary(unit, demands, curSettings, currentMonthIndex = 202608)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Financial Dashboard calculation
    val financialStats: StateFlow<FinancialStats> = combine(
        roomSummaries,
        allExpenses,
        settings
    ) { summaries, expenses, curSettings ->
        val residentialSummaries = summaries.values.filter { it.unit?.unitType == UnitType.RESIDENTIAL }
        val currentMonthDemands = residentialSummaries.mapNotNull { it.currentMonthDemand }

        val totalExpected = currentMonthDemands.sumOf { it.baseAmount }
        val totalCollected = currentMonthDemands.filter { it.isPaid }.sumOf { it.paidAmount }
        val totalPending = totalExpected - totalCollected

        val paidCount = currentMonthDemands.count { it.isPaid }
        val pendingCount = currentMonthDemands.count { !it.isPaid }

        val totalOverallOutstanding = residentialSummaries.sumOf { it.totalBasePending }
        val totalOverallPenalties = residentialSummaries.sumOf { it.totalPenalty }
        val totalExpensesAmount = expenses.sumOf { it.amount }

        val opening = curSettings.openingBalance
        val allTimeCollections = residentialSummaries.flatMap { it.demands }.filter { it.isPaid }.sumOf { it.paidAmount }
        val closing = opening + allTimeCollections - totalExpensesAmount

        val collectionRate = if (totalExpected > 0) (totalCollected / totalExpected) * 100.0 else 0.0

        FinancialStats(
            currentMonth = "August 2026",
            totalExpectedCurrentMonth = totalExpected,
            totalCollectedCurrentMonth = totalCollected,
            totalPendingCurrentMonth = totalPending,
            totalPenaltyCurrentMonth = totalOverallPenalties,
            paidMembersCount = paidCount,
            pendingMembersCount = pendingCount,
            totalOverallOutstanding = totalOverallOutstanding + totalOverallPenalties,
            totalOverallPenalties = totalOverallPenalties,
            totalSocietyExpenses = totalExpensesAmount,
            openingBalance = opening,
            closingBalance = closing,
            collectionRatePercent = collectionRate
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FinancialStats())

    // State Mutators & Session Authentication
    fun setRole(role: UserRole) {
        _currentRole.value = role
        sessionManager.switchRole(role, _currentResidentRoom.value)
        navigationManager.onUserSessionChanged(sessionManager.session.value)
        val user = sessionManager.session.value.currentUser
        _currentTab.value = navigationManager.navigationState.value.currentTab
        showSnackbar("Active Role: ${user.designation} (${user.name})")
    }

    fun switchUser(user: UserProfile) {
        sessionManager.switchUser(user)
        _currentRole.value = user.role
        user.roomNumber?.let { _currentResidentRoom.value = it }
        navigationManager.onUserSessionChanged(sessionManager.session.value)
        _currentTab.value = navigationManager.navigationState.value.currentTab
        showSnackbar("Logged in as ${user.name} • ${user.designation}")
    }

    fun setResidentRoom(roomNumber: Int) {
        _currentResidentRoom.value = roomNumber
        val unit = allUnits.value.find { it.roomNumber == roomNumber }
        val isOwner = unit?.occupancyType != OccupancyType.TENANT
        val name = if (isOwner) unit?.ownerName else unit?.tenantName
        sessionManager.switchResidentRoom(roomNumber, isOwner, name)
        _currentRole.value = UserRole.RESIDENT
        navigationManager.onUserSessionChanged(sessionManager.session.value)
        _currentTab.value = navigationManager.navigationState.value.currentTab
        showSnackbar("Switched to Flat $roomNumber (${name ?: "Resident"})")
    }

    fun setTab(tab: ScreenTab) {
        if (navigationManager.navigateTo(tab)) {
            _currentTab.value = tab
        } else {
            navigationManager.navigationState.value.accessDeniedMessage?.let {
                showSnackbar("🔒 $it")
            } ?: showSnackbar("🔒 Access Restricted: ${tab.name.replace("_", " ")} requires Committee/Admin privileges")
        }
    }

    fun navigateBack(): Boolean {
        val handled = navigationManager.navigateBack()
        if (handled) {
            _currentTab.value = navigationManager.navigationState.value.currentTab
        }
        return handled
    }

    fun hasPrivilege(privilege: UserPrivilege): Boolean {
        return sessionManager.hasPrivilege(privilege)
    }

    fun canAccessTab(tab: ScreenTab): Boolean {
        return sessionManager.canAccessTab(tab)
    }

    fun setFloorFilter(floor: Int?) {
        _floorFilter.value = floor
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedUnit(unit: UnitEntity?) {
        _selectedUnit.value = unit
    }

    fun setSelectedReceipt(receipt: ReceiptEntity?) {
        _selectedReceipt.value = receipt
    }

    fun setSelectedRoomForPenaltyView(summary: RoomMaintenanceSummary?) {
        _selectedRoomForPenaltyView.value = summary
    }

    fun showSnackbar(message: String) {
        _snackbarMessage.value = message
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun getActiveUserName(): String {
        val user = sessionManager.session.value.currentUser
        return "${user.name} (${user.designation})"
    }

    // Actions
    fun recordPayment(
        roomNumber: Int,
        monthYear: String,
        amount: Double,
        paymentMethod: String,
        transactionRef: String,
        penaltyPaid: Double = 0.0,
        remarks: String = ""
    ) {
        viewModelScope.launch {
            val user = getActiveUserName()
            val receiptId = repository.recordPayment(
                roomNumber = roomNumber,
                monthYear = monthYear,
                amountPaid = amount,
                paymentMethod = paymentMethod,
                transactionRef = transactionRef,
                authorizedMember = "Mrs. Sunita Patil (Treasurer)",
                recordedBy = user,
                penaltyPaid = penaltyPaid,
                remarks = remarks
            )
            showSnackbar("Payment of ₹$amount recorded for Room $roomNumber! Digital Receipt generated.")
        }
    }

    fun overridePenalty(
        demandId: Long,
        roomNumber: Int,
        monthYear: String,
        newPenalty: Double,
        reason: String
    ) {
        viewModelScope.launch {
            val user = getActiveUserName()
            repository.overridePenalty(demandId, roomNumber, monthYear, newPenalty, reason, user)
            showSnackbar("Penalty for Room $roomNumber adjusted to ₹$newPenalty")
        }
    }

    fun updateSettings(newSettings: SocietySettingsEntity) {
        viewModelScope.launch {
            val user = getActiveUserName()
            repository.updateSocietySettings(newSettings, user)
            showSnackbar("Society Settings updated successfully!")
        }
    }

    fun updateUnitProfile(unit: UnitEntity) {
        viewModelScope.launch {
            val user = getActiveUserName()
            repository.updateUnitProfile(unit, user)
            showSnackbar("Details for Room ${unit.roomNumber} updated!")
        }
    }

    fun generateNextMonthDemands(monthName: String, monthIndex: Int, dueDate: String) {
        viewModelScope.launch {
            val user = getActiveUserName()
            repository.generateMonthlyDemand(monthName, monthIndex, dueDate, user)
            showSnackbar("Generated $monthName maintenance demands for all 85 residential units!")
        }
    }

    fun addDocument(doc: SocietyDocumentEntity) {
        viewModelScope.launch {
            val user = getActiveUserName()
            repository.addDocument(doc, user)
            showSnackbar("Document '${doc.title}' uploaded successfully!")
        }
    }

    fun deleteDocument(id: Long, title: String) {
        viewModelScope.launch {
            val user = getActiveUserName()
            repository.deleteDocument(id, title, user)
            showSnackbar("Document deleted.")
        }
    }

    fun addExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            val user = getActiveUserName()
            repository.addExpense(expense, user)
            showSnackbar("Expense of ₹${expense.amount} recorded!")
        }
    }

    fun addCorrespondence(item: CorrespondenceEntity) {
        viewModelScope.launch {
            val user = getActiveUserName()
            repository.addCorrespondence(item, user)
            showSnackbar("Correspondence '${item.subject}' recorded!")
        }
    }

    fun updateCorrespondence(item: CorrespondenceEntity) {
        viewModelScope.launch {
            val user = getActiveUserName()
            repository.updateCorrespondence(item, user)
            showSnackbar("Follow-up updated for '${item.subject}'")
        }
    }

    fun addNotice(notice: NoticeEntity) {
        viewModelScope.launch {
            val user = getActiveUserName()
            repository.addNotice(notice, user)
            showSnackbar("Notice '${notice.title}' published!")
        }
    }

    fun addTask(task: CommitteeTaskEntity) {
        viewModelScope.launch {
            val user = getActiveUserName()
            repository.addTask(task, user)
            showSnackbar("Task '${task.title}' assigned to ${task.assignedTo}")
        }
    }

    fun updateTask(task: CommitteeTaskEntity) {
        viewModelScope.launch {
            val user = getActiveUserName()
            repository.updateTask(task, user)
            showSnackbar("Task status updated to ${task.status.name}")
        }
    }

    fun addMeeting(meeting: MeetingEntity) {
        viewModelScope.launch {
            val user = getActiveUserName()
            repository.addMeeting(meeting, user)
            showSnackbar("Meeting on ${meeting.date} scheduled!")
        }
    }

    fun addComplaint(complaint: ComplaintEntity) {
        viewModelScope.launch {
            val user = getActiveUserName()
            repository.addComplaint(complaint, user)
            showSnackbar("Complaint registered! Ticket: ${complaint.ticketNumber}")
        }
    }

    fun updateComplaint(complaint: ComplaintEntity) {
        viewModelScope.launch {
            val user = getActiveUserName()
            repository.updateComplaint(complaint, user)
            showSnackbar("Ticket #${complaint.ticketNumber} updated to ${complaint.status.name}")
        }
    }

    fun sendReminderAlert(roomNumber: Int, message: String, channel: String = "In-App & SMS") {
        viewModelScope.launch {
            showSnackbar("🔔 Sent $channel reminder to Room $roomNumber: \"$message\"")
        }
    }

    // Room Database CRUD for Resident Entity
    fun addResident(resident: Resident) {
        viewModelScope.launch {
            try {
                val user = getActiveUserName()
                repository.insertResident(resident, user)
                showSnackbar("Resident ${resident.name} saved to local Room database")
            } catch (e: Exception) {
                showSnackbar("Error saving resident: ${e.message}")
            }
        }
    }

    fun updateResident(resident: Resident) {
        viewModelScope.launch {
            try {
                val user = getActiveUserName()
                repository.updateResident(resident, user)
                showSnackbar("Resident ${resident.name} updated in local Room database")
            } catch (e: Exception) {
                showSnackbar("Error updating resident: ${e.message}")
            }
        }
    }

    fun deleteResident(resident: Resident) {
        viewModelScope.launch {
            try {
                val user = getActiveUserName()
                repository.deleteResident(resident, user)
                showSnackbar("Resident ${resident.name} removed from local Room database")
            } catch (e: Exception) {
                showSnackbar("Error deleting resident: ${e.message}")
            }
        }
    }

    // Room Database CRUD for Payment Entity
    fun recordPayment(payment: Payment) {
        viewModelScope.launch {
            try {
                val user = getActiveUserName()
                repository.recordPayment(payment, user)
                showSnackbar("Payment of ₹${payment.amount} recorded in local Room database")
            } catch (e: Exception) {
                showSnackbar("Error recording payment: ${e.message}")
            }
        }
    }

    fun updatePayment(payment: Payment) {
        viewModelScope.launch {
            try {
                val user = getActiveUserName()
                repository.updatePayment(payment, user)
                showSnackbar("Payment record updated in local Room database")
            } catch (e: Exception) {
                showSnackbar("Error updating payment: ${e.message}")
            }
        }
    }

    fun deletePayment(payment: Payment) {
        viewModelScope.launch {
            try {
                val user = getActiveUserName()
                repository.deletePayment(payment, user)
                showSnackbar("Payment record deleted from local Room database")
            } catch (e: Exception) {
                showSnackbar("Error deleting payment: ${e.message}")
            }
        }
    }

    // Room Database CRUD for MaintenancePayment Entity (linked to Resident)
    fun addMaintenancePayment(payment: MaintenancePayment) {
        viewModelScope.launch {
            try {
                val user = getActiveUserName()
                repository.insertMaintenancePayment(payment, user)
                showSnackbar("MaintenancePayment for Unit ${payment.unitNumber} linked to Resident & saved")
            } catch (e: Exception) {
                showSnackbar("Error saving MaintenancePayment: ${e.message}")
            }
        }
    }

    fun updateMaintenancePayment(payment: MaintenancePayment) {
        viewModelScope.launch {
            try {
                val user = getActiveUserName()
                repository.updateMaintenancePayment(payment, user)
                showSnackbar("MaintenancePayment for Unit ${payment.unitNumber} updated to ${payment.status.name}")
            } catch (e: Exception) {
                showSnackbar("Error updating MaintenancePayment: ${e.message}")
            }
        }
    }

    fun deleteMaintenancePayment(payment: MaintenancePayment) {
        viewModelScope.launch {
            try {
                val user = getActiveUserName()
                repository.deleteMaintenancePayment(payment, user)
                showSnackbar("MaintenancePayment record deleted")
            } catch (e: Exception) {
                showSnackbar("Error deleting MaintenancePayment: ${e.message}")
            }
        }
    }
}
