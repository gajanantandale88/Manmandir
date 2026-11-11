package com.example.data.repository

import com.example.data.db.PaymentDao
import com.example.data.model.Payment
import kotlinx.coroutines.flow.Flow

/**
 * Repository providing access to the Room [Payment] database table.
 */
class PaymentRepository(private val paymentDao: PaymentDao) {

    val allPayments: Flow<List<Payment>> = paymentDao.getAllPayments()
    val totalCollectedAmount: Flow<Double?> = paymentDao.getTotalCollectedAmount()

    fun getPaymentById(id: Long): Flow<Payment?> = paymentDao.getPaymentById(id)

    fun getPaymentsForResident(residentId: Long): Flow<List<Payment>> = paymentDao.getPaymentsForResident(residentId)

    fun getPaymentsForFlat(flatNumber: String): Flow<List<Payment>> = paymentDao.getPaymentsForFlat(flatNumber)

    fun getPaymentsByMonth(month: String): Flow<List<Payment>> = paymentDao.getPaymentsByMonth(month)

    fun getPaymentsByStatus(status: String): Flow<List<Payment>> = paymentDao.getPaymentsByStatus(status)

    suspend fun insertPayment(payment: Payment): Long = paymentDao.insertPayment(payment)
    suspend fun insert(payment: Payment): Long = insertPayment(payment)

    suspend fun insertPayments(payments: List<Payment>) = paymentDao.insertPayments(payments)
    suspend fun insertAll(payments: List<Payment>) = insertPayments(payments)

    suspend fun updatePayment(payment: Payment) = paymentDao.updatePayment(payment)
    suspend fun update(payment: Payment) = updatePayment(payment)

    suspend fun deletePayment(payment: Payment) = paymentDao.deletePayment(payment)
    suspend fun delete(payment: Payment) = deletePayment(payment)

    suspend fun deletePaymentById(id: Long) = paymentDao.deletePaymentById(id)
    suspend fun deleteById(id: Long) = deletePaymentById(id)
}
