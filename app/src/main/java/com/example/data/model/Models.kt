package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole {
    SUPER_ADMIN,
    CHAIRMAN,
    SECRETARY,
    TREASURER,
    COMMITTEE_MEMBER,
    RESIDENT
}

enum class UnitType {
    RESIDENTIAL,
    OFFICE,
    REFUGE_AREA
}

enum class OccupancyType {
    OWNER,
    TENANT,
    NONE
}

enum class PaymentStatus {
    CURRENT_PAID,
    CURRENT_DUE,
    ONE_MONTH_PENDING,
    TWO_MONTHS_PENDING,
    PENALTY_APPLICABLE
}

enum class DocumentCategory {
    SOCIETY_REGISTRATION,
    GOVERNMENT_LETTERS,
    BMC_CORRESPONDENCE,
    MMRDA_CORRESPONDENCE,
    POLICE_CORRESPONDENCE,
    MEETING_MINUTES,
    NOTICES,
    RESOLUTIONS,
    MAINTENANCE_RECORDS,
    BILLS_AND_QUOTATIONS,
    AGREEMENTS,
    IMPORTANT_SOCIETY_DOCS
}

enum class CorrespondenceType {
    INCOMING,
    OUTGOING
}

enum class FollowUpStatus {
    PENDING,
    IN_PROGRESS,
    REPLY_RECEIVED,
    RESOLVED,
    CLOSED
}

enum class ExpenseCategory {
    ELECTRICITY,
    WATER,
    LIFT,
    CLEANING,
    SECURITY,
    REPAIRS,
    PLUMBING,
    ELECTRICAL,
    PEST_CONTROL,
    GOVERNMENT_FEES,
    STATIONERY,
    CULTURAL_ACTIVITIES,
    EMERGENCY,
    OTHER
}

enum class NoticeCategory {
    MAINTENANCE_REMINDER,
    WATER_SUPPLY,
    LIFT_MAINTENANCE,
    ELECTRICITY,
    MEETING_NOTICE,
    EMERGENCY_ALERT,
    GOVERNMENT_NOTICE,
    SOCIETY_EVENT,
    REPAIR_UPDATE
}

enum class MeetingType {
    GENERAL_BODY_MEETING,
    COMMITTEE_MEETING,
    EMERGENCY_MEETING
}

enum class ComplaintCategory {
    WATER,
    LIFT,
    ELECTRICITY,
    CLEANING,
    SECURITY,
    PLUMBING,
    LEAKAGE,
    PARKING,
    MAINTENANCE,
    OTHER
}

enum class ComplaintStatus {
    SUBMITTED,
    ASSIGNED,
    IN_PROGRESS,
    RESOLVED,
    CLOSED
}

enum class TaskStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED
}

@Entity(tableName = "units")
data class UnitEntity(
    @PrimaryKey val roomNumber: Int,
    val floor: Int,
    val unitType: UnitType,
    val occupancyType: OccupancyType,
    val ownerName: String,
    val ownerPhone: String,
    val ownerEmail: String,
    val tenantName: String = "",
    val tenantPhone: String = "",
    val tenantEmail: String = "",
    val alternateContact: String = "",
    val numberOfOccupants: Int = 0,
    val occupancyDate: String = "",
    val emergencyContact: String = "",
    val submittedDocs: String = "",
    val pendingDocs: String = "",
    val monthlyMaintenance: Double = 500.0,
    val isOccupied: Boolean = true,
    // Slot 1 (Primary Flat User)
    val user1Name: String = "",
    val user1Phone: String = "",
    val user1Email: String = "",
    val user1Designation: String = "Primary Member",
    // Slot 2 (Co-Owner / Joint Member / Tenant)
    val user2Name: String = "",
    val user2Phone: String = "",
    val user2Email: String = "",
    val user2Designation: String = "Joint Member / Co-Owner"
) {
    fun getEffectiveUser1Name(): String = if (user1Name.isNotBlank()) user1Name else (if (occupancyType == OccupancyType.TENANT && tenantName.isNotBlank()) tenantName else ownerName)
    fun getEffectiveUser1Phone(): String = if (user1Phone.isNotBlank()) user1Phone else (if (occupancyType == OccupancyType.TENANT && tenantPhone.isNotBlank()) tenantPhone else ownerPhone)
    fun getEffectiveUser1Email(): String = if (user1Email.isNotBlank()) user1Email else (if (occupancyType == OccupancyType.TENANT && tenantEmail.isNotBlank()) tenantEmail else ownerEmail)

    fun getEffectiveUser2Name(): String = if (user2Name.isNotBlank()) user2Name else (if (occupancyType == OccupancyType.TENANT) ownerName else "Joint Member / Nominee")
    fun getEffectiveUser2Phone(): String = if (user2Phone.isNotBlank()) user2Phone else alternateContact
    fun getEffectiveUser2Email(): String = if (user2Email.isNotBlank()) user2Email else "user2.$roomNumber@manmandir.org"
}

@Entity(tableName = "maintenance_demands")
data class MaintenanceDemandEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val roomNumber: Int,
    val monthYear: String, // e.g. "August 2026"
    val monthIndex: Int, // e.g. 202608 for ordering and month calculations
    val baseAmount: Double, // ₹500 for owner, ₹750 for tenant
    val paidAmount: Double = 0.0,
    val dueDate: String,
    val isPaid: Boolean = false,
    val paidDate: String? = null,
    val paymentMethod: String? = null, // Cash, UPI, Bank Transfer, Cheque
    val transactionRef: String? = null,
    val receiptNumber: String? = null,
    val penaltyOverride: Double? = null,
    val overrideReason: String? = null
)

@Entity(tableName = "receipts")
data class ReceiptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val receiptNumber: String,
    val date: String,
    val roomNumber: Int,
    val residentName: String,
    val occupancyType: OccupancyType,
    val maintenanceMonth: String,
    val maintenanceAmount: Double,
    val previousOutstanding: Double,
    val penaltyAmount: Double,
    val totalAmountReceived: Double,
    val paymentMethod: String,
    val transactionRef: String,
    val remainingBalance: Double,
    val authorizedMember: String,
    val remarks: String = ""
)

@Entity(tableName = "society_documents")
data class SocietyDocumentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: DocumentCategory,
    val fileRef: String,
    val dateUploaded: String,
    val isResidentAccessible: Boolean = true,
    val uploadedBy: String,
    val description: String = "",
    val fileSize: String = "1.2 MB"
)

@Entity(tableName = "correspondence")
data class CorrespondenceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: CorrespondenceType,
    val date: String,
    val party: String, // Sender or Recipient
    val subject: String,
    val referenceNumber: String,
    val description: String,
    val status: FollowUpStatus,
    val followUpDate: String? = null,
    val responsibleMember: String,
    val attachedDocument: String? = null
)

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val category: ExpenseCategory,
    val description: String,
    val vendorName: String,
    val amount: Double,
    val paymentMethod: String,
    val billNumber: String,
    val committeeApproval: String,
    val receiptDoc: String? = null,
    val remarks: String = ""
)

// NoticeEntity is defined as Room Entity Notice in Notice.kt with typealias NoticeEntity = Notice

@Entity(tableName = "committee_tasks")
data class CommitteeTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val assignedTo: String,
    val dueDate: String,
    val status: TaskStatus,
    val contractorInfo: String = "",
    val attachments: String = ""
)

@Entity(tableName = "meetings")
data class MeetingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: MeetingType,
    val date: String,
    val time: String,
    val venue: String,
    val agenda: String,
    val minutes: String = "",
    val decisions: String = "",
    val attendanceSummary: String = "",
    val signedMinutesDoc: String? = null
)

@Entity(tableName = "complaints")
data class ComplaintEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ticketNumber: String,
    val roomNumber: Int,
    val residentName: String,
    val category: ComplaintCategory,
    val description: String,
    val status: ComplaintStatus,
    val dateSubmitted: String,
    val assignedTo: String = "Society Office",
    val resolutionRemarks: String = ""
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val formattedDateTime: String,
    val userName: String,
    val action: String,
    val details: String,
    val previousValue: String? = null,
    val newValue: String? = null
)

@Entity(tableName = "society_settings")
data class SocietySettingsEntity(
    @PrimaryKey val id: Int = 1,
    val societyName: String = "Manmandir Cooperative Housing Society (Proposed)",
    val wingName: String = "Wing A",
    val ownerMaintenanceRate: Double = 500.0,
    val tenantMaintenanceRate: Double = 750.0,
    val gracePeriodMonths: Int = 2,
    val penaltyRateMonthlyPercent: Double = 22.0,
    val dueDayOfMonth: Int = 10,
    val openingBalance: Double = 425000.0,
    val emergencyContact: String = "+91 98200 12345",
    val secretaryName: String = "Mr. Rajesh Sharma (Secretary)",
    val chairmanName: String = "Mr. Suresh Kadam (Chairman)",
    val treasurerName: String = "Mrs. Sunita Patil (Treasurer)"
)
