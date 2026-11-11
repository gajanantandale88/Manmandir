package com.example.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

/**
 * Status of a monthly maintenance payment for a residential unit.
 */
enum class MaintenancePaymentStatus {
    PAID,
    PENDING,
    OVERDUE,
    PARTIAL,
    WAIVED
}

/**
 * Room Entity tracking payment dates, amounts, and statuses for each residential unit,
 * linked directly to the existing [Resident] entity via foreign key [residentId].
 */
@Entity(
    tableName = "maintenance_payments",
    foreignKeys = [
        ForeignKey(
            entity = Resident::class,
            parentColumns = ["id"],
            childColumns = ["residentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["residentId"]),
        Index(value = ["unitNumber"])
    ]
)
data class MaintenancePayment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val residentId: Long,
    val unitNumber: String, // e.g., "101", "402"
    val billingMonth: String, // e.g., "August 2026"
    val amount: Double, // Base maintenance amount (e.g., ₹500 or ₹750)
    val penaltyAmount: Double = 0.0,
    val amountPaid: Double = 0.0,
    val dueDate: String, // e.g., "10-Aug-2026"
    val paymentDate: String? = null, // Null when PENDING or OVERDUE; populated when PAID/PARTIAL
    val status: MaintenancePaymentStatus = MaintenancePaymentStatus.PENDING,
    val paymentMethod: String = "", // "UPI", "Bank Transfer", "Cash", "Cheque"
    val transactionReference: String = "",
    val receiptNumber: String = "",
    val remarks: String = ""
)

typealias MaintenancePaymentEntity = MaintenancePayment

/**
 * Relational POJO representing a [Resident] alongside all of their linked [MaintenancePayment] records.
 */
data class ResidentWithMaintenancePayments(
    @Embedded val resident: Resident,
    @Relation(
        parentColumn = "id",
        entityColumn = "residentId"
    )
    val maintenancePayments: List<MaintenancePayment>
)

/**
 * Relational POJO representing a [MaintenancePayment] joined with its parent [Resident].
 */
data class MaintenancePaymentWithResident(
    @Embedded val maintenancePayment: MaintenancePayment,
    @Relation(
        parentColumn = "residentId",
        entityColumn = "id"
    )
    val resident: Resident
)
