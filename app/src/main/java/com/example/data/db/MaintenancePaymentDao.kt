package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.MaintenancePayment
import com.example.data.model.MaintenancePaymentStatus
import com.example.data.model.MaintenancePaymentWithResident
import com.example.data.model.ResidentWithMaintenancePayments
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Room Entity [MaintenancePayment] and its relational queries with [com.example.data.model.Resident].
 */
@Dao
interface MaintenancePaymentDao {

    @Query("SELECT * FROM maintenance_payments ORDER BY id DESC")
    fun getAllMaintenancePayments(): Flow<List<MaintenancePayment>>

    @Transaction
    @Query("SELECT * FROM maintenance_payments ORDER BY id DESC")
    fun getAllMaintenancePaymentsWithResidents(): Flow<List<MaintenancePaymentWithResident>>

    @Query("SELECT * FROM maintenance_payments WHERE id = :id")
    fun getMaintenancePaymentById(id: Long): Flow<MaintenancePayment?>

    @Query("SELECT * FROM maintenance_payments WHERE residentId = :residentId ORDER BY id DESC")
    fun getMaintenancePaymentsForResident(residentId: Long): Flow<List<MaintenancePayment>>

    @Query("SELECT * FROM maintenance_payments WHERE unitNumber = :unitNumber ORDER BY id DESC")
    fun getMaintenancePaymentsForUnit(unitNumber: String): Flow<List<MaintenancePayment>>

    @Query("SELECT * FROM maintenance_payments WHERE unitNumber = :unitNumber ORDER BY id DESC")
    suspend fun getMaintenancePaymentsForUnitSync(unitNumber: String): List<MaintenancePayment>

    @Query("SELECT * FROM maintenance_payments WHERE status = :status ORDER BY unitNumber ASC")
    fun getMaintenancePaymentsByStatus(status: MaintenancePaymentStatus): Flow<List<MaintenancePayment>>

    @Query("SELECT * FROM maintenance_payments WHERE billingMonth = :billingMonth ORDER BY unitNumber ASC")
    fun getMaintenancePaymentsByMonth(billingMonth: String): Flow<List<MaintenancePayment>>

    @Transaction
    @Query("SELECT * FROM residents ORDER BY flatNumber ASC")
    fun getResidentsWithMaintenancePayments(): Flow<List<ResidentWithMaintenancePayments>>

    @Transaction
    @Query("SELECT * FROM residents WHERE id = :residentId")
    fun getResidentWithMaintenancePaymentsById(residentId: Long): Flow<ResidentWithMaintenancePayments?>

    @Transaction
    @Query("SELECT * FROM residents WHERE flatNumber = :unitNumber LIMIT 1")
    fun getResidentWithMaintenancePaymentsByUnit(unitNumber: String): Flow<ResidentWithMaintenancePayments?>

    @Query("SELECT SUM(amountPaid) FROM maintenance_payments WHERE status = 'PAID' OR status = 'PARTIAL'")
    fun getTotalMaintenanceCollected(): Flow<Double?>

    @Query("SELECT SUM(amount + penaltyAmount - amountPaid) FROM maintenance_payments WHERE status = 'PENDING' OR status = 'OVERDUE' OR status = 'PARTIAL'")
    fun getTotalMaintenanceOutstanding(): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaintenancePayment(payment: MaintenancePayment): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaintenancePayments(payments: List<MaintenancePayment>): List<Long>

    @Update
    suspend fun updateMaintenancePayment(payment: MaintenancePayment)

    @Delete
    suspend fun deleteMaintenancePayment(payment: MaintenancePayment)

    @Query("DELETE FROM maintenance_payments WHERE id = :id")
    suspend fun deleteMaintenancePaymentById(id: Long)
}
