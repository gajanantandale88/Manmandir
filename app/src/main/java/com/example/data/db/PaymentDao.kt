package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Payment
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Room Entity [Payment].
 */
@Dao
interface PaymentDao {

    @Query("SELECT * FROM payments ORDER BY id DESC")
    fun getAllPayments(): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE id = :id")
    fun getPaymentById(id: Long): Flow<Payment?>

    @Query("SELECT * FROM payments WHERE residentId = :residentId ORDER BY id DESC")
    fun getPaymentsForResident(residentId: Long): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE flatNumber = :flatNumber ORDER BY id DESC")
    fun getPaymentsForFlat(flatNumber: String): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE paymentMonth = :month ORDER BY id DESC")
    fun getPaymentsByMonth(month: String): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE status = :status ORDER BY id DESC")
    fun getPaymentsByStatus(status: String): Flow<List<Payment>>

    @Query("SELECT SUM(amount) FROM payments WHERE status = 'PAID'")
    fun getTotalCollectedAmount(): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: Payment): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayments(payments: List<Payment>)

    @Update
    suspend fun updatePayment(payment: Payment)

    @Delete
    suspend fun deletePayment(payment: Payment)

    @Query("DELETE FROM payments WHERE id = :id")
    suspend fun deletePaymentById(id: Long)
}
