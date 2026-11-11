package com.example.data.repository

import com.example.data.db.MaintenancePaymentDao
import com.example.data.model.MaintenancePayment
import com.example.data.model.MaintenancePaymentStatus
import com.example.data.model.MaintenancePaymentWithResident
import com.example.data.model.ResidentWithMaintenancePayments
import kotlinx.coroutines.flow.Flow

/**
 * Repository abstracting data access for [MaintenancePayment] and its linked [com.example.data.model.Resident] records.
 */
class MaintenancePaymentRepository(private val maintenancePaymentDao: MaintenancePaymentDao) {

    val allMaintenancePayments: Flow<List<MaintenancePayment>> =
        maintenancePaymentDao.getAllMaintenancePayments()

    val allMaintenancePaymentsWithResidents: Flow<List<MaintenancePaymentWithResident>> =
        maintenancePaymentDao.getAllMaintenancePaymentsWithResidents()

    val residentsWithMaintenancePayments: Flow<List<ResidentWithMaintenancePayments>> =
        maintenancePaymentDao.getResidentsWithMaintenancePayments()

    val totalCollected: Flow<Double?> =
        maintenancePaymentDao.getTotalMaintenanceCollected()

    val totalOutstanding: Flow<Double?> =
        maintenancePaymentDao.getTotalMaintenanceOutstanding()

    fun getById(id: Long): Flow<MaintenancePayment?> =
        maintenancePaymentDao.getMaintenancePaymentById(id)

    fun getPaymentsForResident(residentId: Long): Flow<List<MaintenancePayment>> =
        maintenancePaymentDao.getMaintenancePaymentsForResident(residentId)

    fun getPaymentsForUnit(unitNumber: String): Flow<List<MaintenancePayment>> =
        maintenancePaymentDao.getMaintenancePaymentsForUnit(unitNumber)

    fun getPaymentsByStatus(status: MaintenancePaymentStatus): Flow<List<MaintenancePayment>> =
        maintenancePaymentDao.getMaintenancePaymentsByStatus(status)

    fun getPaymentsByMonth(billingMonth: String): Flow<List<MaintenancePayment>> =
        maintenancePaymentDao.getMaintenancePaymentsByMonth(billingMonth)

    fun getResidentWithPaymentsById(residentId: Long): Flow<ResidentWithMaintenancePayments?> =
        maintenancePaymentDao.getResidentWithMaintenancePaymentsById(residentId)

    fun getResidentWithPaymentsByUnit(unitNumber: String): Flow<ResidentWithMaintenancePayments?> =
        maintenancePaymentDao.getResidentWithMaintenancePaymentsByUnit(unitNumber)

    suspend fun insertMaintenancePayment(payment: MaintenancePayment): Long =
        maintenancePaymentDao.insertMaintenancePayment(payment)

    suspend fun insert(payment: MaintenancePayment): Long =
        insertMaintenancePayment(payment)

    suspend fun insertMaintenancePayments(payments: List<MaintenancePayment>): List<Long> =
        maintenancePaymentDao.insertMaintenancePayments(payments)

    suspend fun insertAll(payments: List<MaintenancePayment>): List<Long> =
        insertMaintenancePayments(payments)

    suspend fun updateMaintenancePayment(payment: MaintenancePayment) =
        maintenancePaymentDao.updateMaintenancePayment(payment)

    suspend fun update(payment: MaintenancePayment) =
        updateMaintenancePayment(payment)

    suspend fun deleteMaintenancePayment(payment: MaintenancePayment) =
        maintenancePaymentDao.deleteMaintenancePayment(payment)

    suspend fun delete(payment: MaintenancePayment) =
        deleteMaintenancePayment(payment)

    suspend fun deleteMaintenancePaymentById(id: Long) =
        maintenancePaymentDao.deleteMaintenancePaymentById(id)

    suspend fun deleteById(id: Long) =
        deleteMaintenancePaymentById(id)
}
