package com.example.data.repository

import com.example.data.db.MaintenancePaymentDao
import com.example.data.db.NoticeDao
import com.example.data.db.PaymentDao
import com.example.data.db.ResidentDao
import com.example.data.db.SocietyDao
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
import com.example.data.model.UnitEntity
import com.example.data.model.UnitType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class MonthPenaltyDetail(
    val monthYear: String,
    val monthIndex: Int,
    val maintenanceAmount: Double,
    val paidAmount: Double,
    val balance: Double,
    val monthsOverdue: Int,
    val isBeyondGrace: Boolean,
    val penaltyAmount: Double,
    val totalAmount: Double
)

data class RoomMaintenanceSummary(
    val roomNumber: Int,
    val unit: UnitEntity?,
    val demands: List<MaintenanceDemandEntity>,
    val currentMonthDemand: MaintenanceDemandEntity?,
    val totalBasePending: Double,
    val totalPenalty: Double,
    val totalOutstanding: Double,
    val status: PaymentStatus,
    val pendingMonthsCount: Int,
    val penaltyBreakdown: List<MonthPenaltyDetail>
)

class SocietyRepository(
    private val dao: SocietyDao,
    private val residentDao: ResidentDao? = null,
    private val maintenancePaymentDao: MaintenancePaymentDao? = null,
    private val paymentDao: PaymentDao? = null,
    private val noticeDao: NoticeDao? = null
) {

    val residentRepository: ResidentRepository? = residentDao?.let { ResidentRepository(it) }
    val maintenancePaymentRepository: MaintenancePaymentRepository? =
        maintenancePaymentDao?.let { MaintenancePaymentRepository(it) }
    val paymentRepository: PaymentRepository? = paymentDao?.let { PaymentRepository(it) }
    val noticeRepository: NoticeRepository? = noticeDao?.let { NoticeRepository(it) }

    val allResidents: Flow<List<Resident>> = residentDao?.getAllResidents() ?: flowOf(emptyList())
    val allMaintenancePayments: Flow<List<MaintenancePayment>> =
        maintenancePaymentDao?.getAllMaintenancePayments() ?: flowOf(emptyList())
    val allMaintenancePaymentsWithResidents: Flow<List<MaintenancePaymentWithResident>> =
        maintenancePaymentDao?.getAllMaintenancePaymentsWithResidents() ?: flowOf(emptyList())
    val residentsWithMaintenancePayments: Flow<List<ResidentWithMaintenancePayments>> =
        maintenancePaymentDao?.getResidentsWithMaintenancePayments() ?: flowOf(emptyList())
    val allPayments: Flow<List<Payment>> = paymentDao?.getAllPayments() ?: flowOf(emptyList())
    val totalCollectedPayments: Flow<Double?> = paymentDao?.getTotalCollectedAmount() ?: flowOf(0.0)

    val allUnits: Flow<List<UnitEntity>> = dao.getAllUnits()
    val allDemands: Flow<List<MaintenanceDemandEntity>> = dao.getAllDemands()
    val allReceipts: Flow<List<ReceiptEntity>> = dao.getAllReceipts()
    val allDocuments: Flow<List<SocietyDocumentEntity>> = dao.getAllDocuments()
    val residentDocuments: Flow<List<SocietyDocumentEntity>> = dao.getResidentAccessibleDocuments()
    val allCorrespondence: Flow<List<CorrespondenceEntity>> = dao.getAllCorrespondence()
    val allExpenses: Flow<List<ExpenseEntity>> = dao.getAllExpenses()
    val allNotices: Flow<List<NoticeEntity>> = dao.getAllNotices()
    val allTasks: Flow<List<CommitteeTaskEntity>> = dao.getAllTasks()
    val allMeetings: Flow<List<MeetingEntity>> = dao.getAllMeetings()
    val allComplaints: Flow<List<ComplaintEntity>> = dao.getAllComplaints()
    val allAuditLogs: Flow<List<AuditLogEntity>> = dao.getAllAuditLogs()
    val settings: Flow<SocietySettingsEntity?> = dao.getSettings()

    fun getDemandsForRoom(roomNumber: Int): Flow<List<MaintenanceDemandEntity>> = dao.getDemandsForRoom(roomNumber)
    fun getReceiptsForRoom(roomNumber: Int): Flow<List<ReceiptEntity>> = dao.getReceiptsForRoom(roomNumber)
    fun getComplaintsForRoom(roomNumber: Int): Flow<List<ComplaintEntity>> = dao.getComplaintsForRoom(roomNumber)

    private fun getCurrentDateTimeFormatted(): String {
        val sdf = SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun getCurrentDateFormatted(): String {
        val sdf = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
        return sdf.format(Date())
    }

    // Calculate room maintenance summary with complete itemized penalty breakdown
    fun calculateRoomSummary(
        unit: UnitEntity?,
        demands: List<MaintenanceDemandEntity>,
        settings: SocietySettingsEntity,
        currentMonthIndex: Int = 202608
    ): RoomMaintenanceSummary {
        if (unit == null || unit.unitType != UnitType.RESIDENTIAL) {
            return RoomMaintenanceSummary(
                roomNumber = unit?.roomNumber ?: 0,
                unit = unit,
                demands = emptyList(),
                currentMonthDemand = null,
                totalBasePending = 0.0,
                totalPenalty = 0.0,
                totalOutstanding = 0.0,
                status = PaymentStatus.CURRENT_PAID,
                pendingMonthsCount = 0,
                penaltyBreakdown = emptyList()
            )
        }

        val sortedDemands = demands.filter { it.roomNumber == unit.roomNumber }.sortedBy { it.monthIndex }
        val currentDemand = sortedDemands.find { it.monthIndex == currentMonthIndex }

        val penaltyList = mutableListOf<MonthPenaltyDetail>()
        var totalBasePending = 0.0
        var totalPenalty = 0.0
        var unpaidMonthsCount = 0

        for (demand in sortedDemands) {
            val balance = (demand.baseAmount - demand.paidAmount).coerceAtLeast(0.0)
            if (balance > 0.0) {
                unpaidMonthsCount++
                totalBasePending += balance

                // Calculate month difference (e.g. 202608 - 202606 = 2 months)
                val demandYear = demand.monthIndex / 100
                val demandMonth = demand.monthIndex % 100
                val curYear = currentMonthIndex / 100
                val curMonth = currentMonthIndex % 100
                val monthsDiff = (curYear - demandYear) * 12 + (curMonth - demandMonth)

                val isBeyondGrace = monthsDiff > settings.gracePeriodMonths
                val penaltyRate = settings.penaltyRateMonthlyPercent / 100.0

                // If penalty was manually overridden by committee, use that
                val penaltyForThisMonth = if (demand.penaltyOverride != null) {
                    demand.penaltyOverride
                } else if (isBeyondGrace) {
                    val penaltyMonths = monthsDiff - settings.gracePeriodMonths
                    balance * penaltyRate * penaltyMonths.coerceAtLeast(1)
                } else {
                    0.0
                }

                totalPenalty += penaltyForThisMonth

                penaltyList.add(
                    MonthPenaltyDetail(
                        monthYear = demand.monthYear,
                        monthIndex = demand.monthIndex,
                        maintenanceAmount = demand.baseAmount,
                        paidAmount = demand.paidAmount,
                        balance = balance,
                        monthsOverdue = monthsDiff,
                        isBeyondGrace = isBeyondGrace,
                        penaltyAmount = penaltyForThisMonth,
                        totalAmount = balance + penaltyForThisMonth
                    )
                )
            }
        }

        val status = when {
            unpaidMonthsCount == 0 -> PaymentStatus.CURRENT_PAID
            unpaidMonthsCount == 1 && (currentDemand?.isPaid == false) -> PaymentStatus.CURRENT_DUE
            unpaidMonthsCount == 1 -> PaymentStatus.ONE_MONTH_PENDING
            unpaidMonthsCount == 2 -> PaymentStatus.TWO_MONTHS_PENDING
            else -> PaymentStatus.PENALTY_APPLICABLE
        }

        return RoomMaintenanceSummary(
            roomNumber = unit.roomNumber,
            unit = unit,
            demands = sortedDemands,
            currentMonthDemand = currentDemand,
            totalBasePending = totalBasePending,
            totalPenalty = totalPenalty,
            totalOutstanding = totalBasePending + totalPenalty,
            status = status,
            pendingMonthsCount = unpaidMonthsCount,
            penaltyBreakdown = penaltyList
        )
    }

    // Record a payment and generate a digital receipt
    suspend fun recordPayment(
        roomNumber: Int,
        monthYear: String,
        amountPaid: Double,
        paymentMethod: String,
        transactionRef: String,
        authorizedMember: String,
        recordedBy: String,
        penaltyPaid: Double = 0.0,
        remarks: String = ""
    ): Long {
        val dateStr = getCurrentDateFormatted()
        val randomReceiptSuffix = (1000..9999).random()
        val receiptNumber = "MM/26-27/$randomReceiptSuffix"

        val unit = dao.getUnitByNumber(roomNumber)
        val residentName = if (unit?.occupancyType == OccupancyType.TENANT) unit.tenantName else unit?.ownerName ?: "Resident $roomNumber"
        val occupancy = unit?.occupancyType ?: OccupancyType.OWNER

        // Update the demand
        val demands = dao.getDemandsForRoomSync(roomNumber)
        val targetDemand = demands.find { it.monthYear == monthYear }
        if (targetDemand != null) {
            val updated = targetDemand.copy(
                isPaid = true,
                paidAmount = targetDemand.baseAmount,
                paidDate = dateStr,
                paymentMethod = paymentMethod,
                transactionRef = transactionRef,
                receiptNumber = receiptNumber
            )
            dao.updateDemand(updated)
        }

        // Remaining balance calculation
        val remainingDemands = demands.filter { it.monthYear != monthYear && !it.isPaid }
        val remainingBalance = remainingDemands.sumOf { it.baseAmount }

        val receipt = ReceiptEntity(
            receiptNumber = receiptNumber,
            date = dateStr,
            roomNumber = roomNumber,
            residentName = residentName,
            occupancyType = occupancy,
            maintenanceMonth = monthYear,
            maintenanceAmount = amountPaid - penaltyPaid,
            previousOutstanding = 0.0,
            penaltyAmount = penaltyPaid,
            totalAmountReceived = amountPaid,
            paymentMethod = paymentMethod,
            transactionRef = transactionRef,
            remainingBalance = remainingBalance,
            authorizedMember = authorizedMember,
            remarks = remarks.ifBlank { "Payment recorded successfully." }
        )
        val receiptId = dao.insertReceipt(receipt)

        // Sync linked MaintenancePayment entity for this unit & resident
        val unitStr = roomNumber.toString()
        val linkedResident = residentDao?.getResidentByFlatSync(unitStr)
        if (linkedResident != null && maintenancePaymentDao != null) {
            val unitPayments = maintenancePaymentDao.getMaintenancePaymentsForUnitSync(unitStr)
            val existingPayment = unitPayments.find { it.billingMonth == monthYear }
            if (existingPayment != null) {
                maintenancePaymentDao.updateMaintenancePayment(
                    existingPayment.copy(
                        amountPaid = amountPaid,
                        penaltyAmount = penaltyPaid,
                        paymentDate = dateStr,
                        status = MaintenancePaymentStatus.PAID,
                        paymentMethod = paymentMethod,
                        transactionReference = transactionRef,
                        receiptNumber = receiptNumber,
                        remarks = remarks.ifBlank { "Payment recorded successfully." }
                    )
                )
            } else {
                maintenancePaymentDao.insertMaintenancePayment(
                    MaintenancePayment(
                        residentId = linkedResident.id,
                        unitNumber = unitStr,
                        billingMonth = monthYear,
                        amount = (amountPaid - penaltyPaid).coerceAtLeast(0.0),
                        penaltyAmount = penaltyPaid,
                        amountPaid = amountPaid,
                        dueDate = dateStr,
                        paymentDate = dateStr,
                        status = MaintenancePaymentStatus.PAID,
                        paymentMethod = paymentMethod,
                        transactionReference = transactionRef,
                        receiptNumber = receiptNumber,
                        remarks = remarks.ifBlank { "Payment recorded successfully." }
                    )
                )
            }
        }

        // Audit Log
        dao.insertAuditLog(
            AuditLogEntity(
                formattedDateTime = getCurrentDateTimeFormatted(),
                userName = recordedBy,
                action = "PAYMENT_RECORDED",
                details = "Recorded payment of ₹$amountPaid for Room $roomNumber ($monthYear) via $paymentMethod. Receipt: $receiptNumber",
                previousValue = "Pending",
                newValue = "Paid (₹$amountPaid)"
            )
        )

        return receiptId
    }

    // Override penalty with audit logging
    suspend fun overridePenalty(
        demandId: Long,
        roomNumber: Int,
        monthYear: String,
        newPenaltyAmount: Double,
        reason: String,
        authorizedBy: String
    ) {
        val demand = dao.getDemandsForRoomSync(roomNumber).find { it.id == demandId } ?: return
        val updated = demand.copy(
            penaltyOverride = newPenaltyAmount,
            overrideReason = reason
        )
        dao.updateDemand(updated)

        dao.insertAuditLog(
            AuditLogEntity(
                formattedDateTime = getCurrentDateTimeFormatted(),
                userName = authorizedBy,
                action = "PENALTY_OVERRIDE",
                details = "Adjusted penalty for Room $roomNumber ($monthYear) to ₹$newPenaltyAmount. Reason: $reason",
                previousValue = "Auto-calculated",
                newValue = "₹$newPenaltyAmount"
            )
        )
    }

    // Update society settings
    suspend fun updateSocietySettings(
        newSettings: SocietySettingsEntity,
        authorizedBy: String
    ) {
        val oldSettings = dao.getSettingsSync()
        dao.updateSettings(newSettings)

        dao.insertAuditLog(
            AuditLogEntity(
                formattedDateTime = getCurrentDateTimeFormatted(),
                userName = authorizedBy,
                action = "SETTINGS_UPDATED",
                details = "Updated Society Rates: Owner ₹${newSettings.ownerMaintenanceRate}, Tenant ₹${newSettings.tenantMaintenanceRate}, Grace Period ${newSettings.gracePeriodMonths} months, Penalty ${newSettings.penaltyRateMonthlyPercent}%/mo",
                previousValue = "Owner ₹${oldSettings?.ownerMaintenanceRate}, Tenant ₹${oldSettings?.tenantMaintenanceRate}",
                newValue = "Owner ₹${newSettings.ownerMaintenanceRate}, Tenant ₹${newSettings.tenantMaintenanceRate}"
            )
        )
    }

    // Generate monthly maintenance demands for all active 85 residential units
    suspend fun generateMonthlyDemand(
        monthName: String,
        monthIndex: Int,
        dueDate: String,
        authorizedBy: String
    ) {
        val settings = dao.getSettingsSync() ?: SocietySettingsEntity()
        val allUnitsList = mutableListOf<UnitEntity>()
        dao.getAllUnits().collect { list ->
            allUnitsList.clear()
            allUnitsList.addAll(list)
            return@collect
        }

        // We can query units directly or filter
        val residential = allUnitsList.filter { it.unitType == UnitType.RESIDENTIAL }
        val demands = residential.map { unit ->
            val rate = if (unit.occupancyType == OccupancyType.TENANT) settings.tenantMaintenanceRate else settings.ownerMaintenanceRate
            MaintenanceDemandEntity(
                roomNumber = unit.roomNumber,
                monthYear = monthName,
                monthIndex = monthIndex,
                baseAmount = rate,
                paidAmount = 0.0,
                dueDate = dueDate,
                isPaid = false
            )
        }
        dao.insertDemands(demands)

        // Also generate linked MaintenancePayment records for each unit's Resident
        if (residentDao != null && maintenancePaymentDao != null) {
            val residentsByFlat = residentDao.getAllResidentsSync().associateBy { it.flatNumber }
            val newMaintenancePayments = demands.mapNotNull { demand ->
                val resident = residentsByFlat[demand.roomNumber.toString()] ?: return@mapNotNull null
                MaintenancePayment(
                    residentId = resident.id,
                    unitNumber = demand.roomNumber.toString(),
                    billingMonth = monthName,
                    amount = demand.baseAmount,
                    penaltyAmount = 0.0,
                    amountPaid = 0.0,
                    dueDate = dueDate,
                    paymentDate = null,
                    status = MaintenancePaymentStatus.PENDING,
                    remarks = "Generated monthly cycle demand"
                )
            }
            maintenancePaymentDao.insertMaintenancePayments(newMaintenancePayments)
        }

        dao.insertAuditLog(
            AuditLogEntity(
                formattedDateTime = getCurrentDateTimeFormatted(),
                userName = authorizedBy,
                action = "MONTHLY_DEMAND_GENERATED",
                details = "Generated $monthName maintenance demand for ${demands.size} residential units. Total demand value: ₹${demands.sumOf { it.baseAmount }}",
                previousValue = "Previous Cycle",
                newValue = "$monthName Cycle Active"
            )
        )
    }

    // Update Unit profile
    suspend fun updateUnitProfile(unit: UnitEntity, authorizedBy: String) {
        dao.updateUnit(unit)
        dao.insertAuditLog(
            AuditLogEntity(
                formattedDateTime = getCurrentDateTimeFormatted(),
                userName = authorizedBy,
                action = "UNIT_PROFILE_UPDATED",
                details = "Updated information for Room ${unit.roomNumber} (${unit.occupancyType.name}). Documents submitted: ${unit.submittedDocs}",
                previousValue = "Previous profile",
                newValue = "Updated profile"
            )
        )
    }

    // Documents
    suspend fun addDocument(doc: SocietyDocumentEntity, user: String) {
        dao.insertDocument(doc)
        dao.insertAuditLog(
            AuditLogEntity(
                formattedDateTime = getCurrentDateTimeFormatted(),
                userName = user,
                action = "DOCUMENT_UPLOADED",
                details = "Uploaded document '${doc.title}' in category ${doc.category.name}. Resident Accessible: ${doc.isResidentAccessible}",
                previousValue = "None",
                newValue = doc.title
            )
        )
    }

    suspend fun deleteDocument(id: Long, title: String, user: String) {
        dao.deleteDocument(id)
        dao.insertAuditLog(
            AuditLogEntity(
                formattedDateTime = getCurrentDateTimeFormatted(),
                userName = user,
                action = "DOCUMENT_DELETED",
                details = "Deleted document '$title' (ID: $id)",
                previousValue = title,
                newValue = "Deleted"
            )
        )
    }

    // Expenses
    suspend fun addExpense(expense: ExpenseEntity, user: String) {
        dao.insertExpense(expense)
        dao.insertAuditLog(
            AuditLogEntity(
                formattedDateTime = getCurrentDateTimeFormatted(),
                userName = user,
                action = "EXPENSE_RECORDED",
                details = "Recorded society expense of ₹${expense.amount} for ${expense.description} (Category: ${expense.category.name}, Vendor: ${expense.vendorName})",
                previousValue = "None",
                newValue = "₹${expense.amount}"
            )
        )
    }

    // Correspondence
    suspend fun addCorrespondence(item: CorrespondenceEntity, user: String) {
        dao.insertCorrespondence(item)
        dao.insertAuditLog(
            AuditLogEntity(
                formattedDateTime = getCurrentDateTimeFormatted(),
                userName = user,
                action = "CORRESPONDENCE_RECORDED",
                details = "Added ${item.type.name} correspondence with ${item.party} regarding '${item.subject}' (Ref: ${item.referenceNumber})",
                previousValue = "None",
                newValue = item.subject
            )
        )
    }

    suspend fun updateCorrespondence(item: CorrespondenceEntity, user: String) {
        dao.updateCorrespondence(item)
        dao.insertAuditLog(
            AuditLogEntity(
                formattedDateTime = getCurrentDateTimeFormatted(),
                userName = user,
                action = "CORRESPONDENCE_UPDATED",
                details = "Updated follow-up status to ${item.status.name} for '${item.subject}' (Responsible: ${item.responsibleMember})",
                previousValue = "Prior status",
                newValue = item.status.name
            )
        )
    }

    // Notices
    suspend fun addNotice(notice: NoticeEntity, user: String) {
        dao.insertNotice(notice)
        noticeDao?.insertNotice(notice)
        dao.insertAuditLog(
            AuditLogEntity(
                formattedDateTime = getCurrentDateTimeFormatted(),
                userName = user,
                action = "NOTICE_PUBLISHED",
                details = "Published notice '${notice.title}' (Category: ${notice.category.name}, Urgent: ${notice.isUrgent})",
                previousValue = "None",
                newValue = notice.title
            )
        )
    }

    // Room Entity Operations: Resident
    suspend fun insertResident(resident: Resident, user: String): Long {
        val id = residentDao?.insertResident(resident) ?: -1L
        dao.insertAuditLog(
            AuditLogEntity(
                formattedDateTime = getCurrentDateTimeFormatted(),
                userName = user,
                action = "RESIDENT_ADDED_ROOM_DB",
                details = "Added resident ${resident.name} for Flat ${resident.flatNumber} in local Room database",
                previousValue = "None",
                newValue = "${resident.name} (Flat ${resident.flatNumber})"
            )
        )
        return id
    }

    suspend fun updateResident(resident: Resident, user: String) {
        residentDao?.updateResident(resident)
        dao.insertAuditLog(
            AuditLogEntity(
                formattedDateTime = getCurrentDateTimeFormatted(),
                userName = user,
                action = "RESIDENT_UPDATED_ROOM_DB",
                details = "Updated resident record for ${resident.name} (Flat ${resident.flatNumber}) in local Room database",
                previousValue = "Previous record",
                newValue = "${resident.name} (${resident.phone})"
            )
        )
    }

    suspend fun deleteResident(resident: Resident, user: String) {
        residentDao?.deleteResident(resident)
        dao.insertAuditLog(
            AuditLogEntity(
                formattedDateTime = getCurrentDateTimeFormatted(),
                userName = user,
                action = "RESIDENT_DELETED_ROOM_DB",
                details = "Deleted resident ${resident.name} (Flat ${resident.flatNumber}) from local Room database",
                previousValue = resident.name,
                newValue = "Deleted"
            )
        )
    }

    // Room Entity Operations: Payment
    suspend fun recordPayment(payment: Payment, user: String): Long {
        val id = paymentDao?.insertPayment(payment) ?: -1L
        dao.insertAuditLog(
            AuditLogEntity(
                formattedDateTime = getCurrentDateTimeFormatted(),
                userName = user,
                action = "PAYMENT_RECORDED_ROOM_DB",
                details = "Recorded payment ₹${payment.amount} for Flat ${payment.flatNumber} (${payment.paymentMonth}) via ${payment.paymentMode} in local Room database",
                previousValue = "None",
                newValue = "₹${payment.amount} (${payment.paymentMode})"
            )
        )
        return id
    }

    suspend fun updatePayment(payment: Payment, user: String) {
        paymentDao?.updatePayment(payment)
        dao.insertAuditLog(
            AuditLogEntity(
                formattedDateTime = getCurrentDateTimeFormatted(),
                userName = user,
                action = "PAYMENT_UPDATED_ROOM_DB",
                details = "Updated payment status to ${payment.status} for Flat ${payment.flatNumber} in local Room database",
                previousValue = "Previous state",
                newValue = "${payment.status} (₹${payment.amount})"
            )
        )
    }

    suspend fun deletePayment(payment: Payment, user: String) {
        paymentDao?.deletePayment(payment)
        dao.insertAuditLog(
            AuditLogEntity(
                formattedDateTime = getCurrentDateTimeFormatted(),
                userName = user,
                action = "PAYMENT_DELETED_ROOM_DB",
                details = "Deleted payment record for Flat ${payment.flatNumber} (₹${payment.amount}) from local Room database",
                previousValue = "₹${payment.amount}",
                newValue = "Deleted"
            )
        )
    }

    // Room Entity Operations: MaintenancePayment (linked to Resident)
    suspend fun insertMaintenancePayment(payment: MaintenancePayment, user: String): Long {
        val resolvedResidentId = if (payment.residentId > 0L) {
            payment.residentId
        } else {
            residentDao?.getResidentByFlatSync(payment.unitNumber)?.id
                ?: throw IllegalArgumentException("No Resident found for Unit ${payment.unitNumber} to link MaintenancePayment")
        }
        val toInsert = payment.copy(residentId = resolvedResidentId)
        val id = maintenancePaymentDao?.insertMaintenancePayment(toInsert) ?: -1L
        dao.insertAuditLog(
            AuditLogEntity(
                formattedDateTime = getCurrentDateTimeFormatted(),
                userName = user,
                action = "MAINTENANCE_PAYMENT_LINKED_ROOM_DB",
                details = "Tracked MaintenancePayment (${toInsert.status.name}) of ₹${toInsert.amount} for Unit ${toInsert.unitNumber} linked to Resident #$resolvedResidentId",
                previousValue = "None",
                newValue = "${toInsert.status.name} (₹${toInsert.amount})"
            )
        )
        return id
    }

    suspend fun updateMaintenancePayment(payment: MaintenancePayment, user: String) {
        maintenancePaymentDao?.updateMaintenancePayment(payment)
        dao.insertAuditLog(
            AuditLogEntity(
                formattedDateTime = getCurrentDateTimeFormatted(),
                userName = user,
                action = "MAINTENANCE_PAYMENT_UPDATED_ROOM_DB",
                details = "Updated MaintenancePayment #${payment.id} for Unit ${payment.unitNumber} to status ${payment.status.name}",
                previousValue = "Previous status",
                newValue = "${payment.status.name} (${payment.paymentDate ?: "Unpaid"})"
            )
        )
    }

    suspend fun deleteMaintenancePayment(payment: MaintenancePayment, user: String) {
        maintenancePaymentDao?.deleteMaintenancePayment(payment)
        dao.insertAuditLog(
            AuditLogEntity(
                formattedDateTime = getCurrentDateTimeFormatted(),
                userName = user,
                action = "MAINTENANCE_PAYMENT_DELETED_ROOM_DB",
                details = "Removed MaintenancePayment #${payment.id} for Unit ${payment.unitNumber}",
                previousValue = "₹${payment.amount}",
                newValue = "Deleted"
            )
        )
    }

    // Tasks
    suspend fun addTask(task: CommitteeTaskEntity, user: String) {
        dao.insertTask(task)
        dao.insertAuditLog(
            AuditLogEntity(
                formattedDateTime = getCurrentDateTimeFormatted(),
                userName = user,
                action = "COMMITTEE_TASK_CREATED",
                details = "Created task '${task.title}' assigned to ${task.assignedTo} (Due: ${task.dueDate})",
                previousValue = "None",
                newValue = task.title
            )
        )
    }

    suspend fun updateTask(task: CommitteeTaskEntity, user: String) {
        dao.updateTask(task)
        dao.insertAuditLog(
            AuditLogEntity(
                formattedDateTime = getCurrentDateTimeFormatted(),
                userName = user,
                action = "COMMITTEE_TASK_UPDATED",
                details = "Updated task '${task.title}' status to ${task.status.name}",
                previousValue = "Prior state",
                newValue = task.status.name
            )
        )
    }

    // Meetings
    suspend fun addMeeting(meeting: MeetingEntity, user: String) {
        dao.insertMeeting(meeting)
        dao.insertAuditLog(
            AuditLogEntity(
                formattedDateTime = getCurrentDateTimeFormatted(),
                userName = user,
                action = "MEETING_RECORDED",
                details = "Scheduled/Recorded ${meeting.type.name} on ${meeting.date} at ${meeting.venue}",
                previousValue = "None",
                newValue = meeting.date
            )
        )
    }

    // Complaints
    suspend fun addComplaint(complaint: ComplaintEntity, user: String) {
        dao.insertComplaint(complaint)
        dao.insertAuditLog(
            AuditLogEntity(
                formattedDateTime = getCurrentDateTimeFormatted(),
                userName = user,
                action = "COMPLAINT_SUBMITTED",
                details = "Complaint submitted #${complaint.ticketNumber} for Room ${complaint.roomNumber} (${complaint.category.name})",
                previousValue = "None",
                newValue = complaint.ticketNumber
            )
        )
    }

    suspend fun updateComplaint(complaint: ComplaintEntity, user: String) {
        dao.updateComplaint(complaint)
        dao.insertAuditLog(
            AuditLogEntity(
                formattedDateTime = getCurrentDateTimeFormatted(),
                userName = user,
                action = "COMPLAINT_STATUS_UPDATED",
                details = "Updated ticket #${complaint.ticketNumber} to status ${complaint.status.name}",
                previousValue = "Prior status",
                newValue = complaint.status.name
            )
        )
    }
}
