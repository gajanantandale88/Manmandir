package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity representing a Payment record stored in the local SQLite database.
 */
@Entity(tableName = "payments")
data class Payment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val residentId: Long = 0,
    val flatNumber: String,
    val residentName: String,
    val amount: Double,
    val paymentDate: String,
    val paymentMonth: String = "August 2026",
    val paymentMode: String = "UPI", // "UPI", "Cash", "Cheque", "Bank Transfer"
    val status: String = "PAID", // "PAID", "PENDING", "FAILED"
    val transactionRef: String = "",
    val receiptNumber: String = "",
    val remarks: String = ""
)

typealias PaymentEntity = Payment
