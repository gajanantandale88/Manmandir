package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Resident
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Room Entity [Resident].
 */
@Dao
interface ResidentDao {

    @Query("SELECT * FROM residents ORDER BY flatNumber ASC")
    fun getAllResidents(): Flow<List<Resident>>

    @Query("SELECT * FROM residents WHERE id = :id")
    fun getResidentById(id: Long): Flow<Resident?>

    @Query("SELECT * FROM residents WHERE id = :id")
    suspend fun getResidentByIdSync(id: Long): Resident?

    @Query("SELECT * FROM residents WHERE flatNumber = :flatNumber")
    fun getResidentsByFlat(flatNumber: String): Flow<List<Resident>>

    @Query("SELECT * FROM residents WHERE flatNumber = :flatNumber LIMIT 1")
    suspend fun getResidentByFlatSync(flatNumber: String): Resident?

    @Query("SELECT * FROM residents ORDER BY flatNumber ASC")
    suspend fun getAllResidentsSync(): List<Resident>

    @Query("SELECT * FROM residents WHERE residentType = :type ORDER BY flatNumber ASC")
    fun getResidentsByType(type: String): Flow<List<Resident>>

    @Query("SELECT * FROM residents WHERE name LIKE '%' || :query || '%' OR flatNumber LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%'")
    fun searchResidents(query: String): Flow<List<Resident>>

    @Query("SELECT COUNT(*) FROM residents")
    fun getResidentsCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResident(resident: Resident): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResidents(residents: List<Resident>): List<Long>

    @Update
    suspend fun updateResident(resident: Resident)

    @Delete
    suspend fun deleteResident(resident: Resident)

    @Query("DELETE FROM residents WHERE id = :id")
    suspend fun deleteResidentById(id: Long)
}
