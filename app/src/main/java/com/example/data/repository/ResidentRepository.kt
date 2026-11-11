package com.example.data.repository

import com.example.data.db.ResidentDao
import com.example.data.model.Resident
import kotlinx.coroutines.flow.Flow

/**
 * Repository providing access to the Room [Resident] database table.
 */
class ResidentRepository(private val residentDao: ResidentDao) {

    val allResidents: Flow<List<Resident>> = residentDao.getAllResidents()
    val residentsCount: Flow<Int> = residentDao.getResidentsCount()

    fun getResidentById(id: Long): Flow<Resident?> = residentDao.getResidentById(id)

    suspend fun getResidentByIdSync(id: Long): Resident? = residentDao.getResidentByIdSync(id)

    fun getResidentsByFlat(flatNumber: String): Flow<List<Resident>> = residentDao.getResidentsByFlat(flatNumber)

    suspend fun getResidentByFlatSync(flatNumber: String): Resident? = residentDao.getResidentByFlatSync(flatNumber)

    fun getResidentsByType(type: String): Flow<List<Resident>> = residentDao.getResidentsByType(type)

    fun searchResidents(query: String): Flow<List<Resident>> = residentDao.searchResidents(query)

    suspend fun insertResident(resident: Resident): Long = residentDao.insertResident(resident)
    suspend fun insert(resident: Resident): Long = insertResident(resident)

    suspend fun insertResidents(residents: List<Resident>): List<Long> = residentDao.insertResidents(residents)
    suspend fun insertAll(residents: List<Resident>): List<Long> = insertResidents(residents)

    suspend fun updateResident(resident: Resident) = residentDao.updateResident(resident)
    suspend fun update(resident: Resident) = updateResident(resident)

    suspend fun deleteResident(resident: Resident) = residentDao.deleteResident(resident)
    suspend fun delete(resident: Resident) = deleteResident(resident)

    suspend fun deleteResidentById(id: Long) = residentDao.deleteResidentById(id)
    suspend fun deleteById(id: Long) = deleteResidentById(id)
}
