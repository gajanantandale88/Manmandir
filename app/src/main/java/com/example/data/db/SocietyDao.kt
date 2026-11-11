package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AuditLogEntity
import com.example.data.model.CommitteeTaskEntity
import com.example.data.model.ComplaintEntity
import com.example.data.model.CorrespondenceEntity
import com.example.data.model.CorrespondenceType
import com.example.data.model.ExpenseEntity
import com.example.data.model.MaintenanceDemandEntity
import com.example.data.model.MeetingEntity
import com.example.data.model.NoticeEntity
import com.example.data.model.ReceiptEntity
import com.example.data.model.SocietyDocumentEntity
import com.example.data.model.SocietySettingsEntity
import com.example.data.model.UnitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SocietyDao {

    // Units
    @Query("SELECT * FROM units ORDER BY roomNumber ASC")
    fun getAllUnits(): Flow<List<UnitEntity>>

    @Query("SELECT * FROM units WHERE roomNumber = :roomNumber")
    suspend fun getUnitByNumber(roomNumber: Int): UnitEntity?

    @Query("SELECT * FROM units WHERE unitType = 'RESIDENTIAL' ORDER BY roomNumber ASC")
    fun getResidentialUnits(): Flow<List<UnitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnits(units: List<UnitEntity>)

    @Update
    suspend fun updateUnit(unit: UnitEntity)

    // Maintenance Demands
    @Query("SELECT * FROM maintenance_demands ORDER BY monthIndex DESC, roomNumber ASC")
    fun getAllDemands(): Flow<List<MaintenanceDemandEntity>>

    @Query("SELECT * FROM maintenance_demands WHERE roomNumber = :roomNumber ORDER BY monthIndex DESC")
    fun getDemandsForRoom(roomNumber: Int): Flow<List<MaintenanceDemandEntity>>

    @Query("SELECT * FROM maintenance_demands WHERE roomNumber = :roomNumber ORDER BY monthIndex DESC")
    suspend fun getDemandsForRoomSync(roomNumber: Int): List<MaintenanceDemandEntity>

    @Query("SELECT * FROM maintenance_demands WHERE monthIndex = :monthIndex ORDER BY roomNumber ASC")
    fun getDemandsForMonth(monthIndex: Int): Flow<List<MaintenanceDemandEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDemands(demands: List<MaintenanceDemandEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDemand(demand: MaintenanceDemandEntity): Long

    @Update
    suspend fun updateDemand(demand: MaintenanceDemandEntity)

    // Receipts
    @Query("SELECT * FROM receipts ORDER BY id DESC")
    fun getAllReceipts(): Flow<List<ReceiptEntity>>

    @Query("SELECT * FROM receipts WHERE roomNumber = :roomNumber ORDER BY id DESC")
    fun getReceiptsForRoom(roomNumber: Int): Flow<List<ReceiptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceipt(receipt: ReceiptEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceipts(receipts: List<ReceiptEntity>)

    // Documents
    @Query("SELECT * FROM society_documents ORDER BY id DESC")
    fun getAllDocuments(): Flow<List<SocietyDocumentEntity>>

    @Query("SELECT * FROM society_documents WHERE isResidentAccessible = 1 ORDER BY id DESC")
    fun getResidentAccessibleDocuments(): Flow<List<SocietyDocumentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(doc: SocietyDocumentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocuments(docs: List<SocietyDocumentEntity>)

    @Query("DELETE FROM society_documents WHERE id = :id")
    suspend fun deleteDocument(id: Long)

    // Correspondence
    @Query("SELECT * FROM correspondence ORDER BY id DESC")
    fun getAllCorrespondence(): Flow<List<CorrespondenceEntity>>

    @Query("SELECT * FROM correspondence WHERE type = :type ORDER BY id DESC")
    fun getCorrespondenceByType(type: CorrespondenceType): Flow<List<CorrespondenceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCorrespondence(item: CorrespondenceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCorrespondenceList(list: List<CorrespondenceEntity>)

    @Update
    suspend fun updateCorrespondence(item: CorrespondenceEntity)

    // Expenses
    @Query("SELECT * FROM expenses ORDER BY id DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenses(expenses: List<ExpenseEntity>)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpense(id: Long)

    // Notices
    @Query("SELECT * FROM notices ORDER BY isUrgent DESC, id DESC")
    fun getAllNotices(): Flow<List<NoticeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotice(notice: NoticeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotices(notices: List<NoticeEntity>)

    @Update
    suspend fun updateNotice(notice: NoticeEntity)

    @Query("DELETE FROM notices WHERE id = :id")
    suspend fun deleteNotice(id: Long)

    // Committee Tasks
    @Query("SELECT * FROM committee_tasks ORDER BY id DESC")
    fun getAllTasks(): Flow<List<CommitteeTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: CommitteeTaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<CommitteeTaskEntity>)

    @Update
    suspend fun updateTask(task: CommitteeTaskEntity)

    @Query("DELETE FROM committee_tasks WHERE id = :id")
    suspend fun deleteTask(id: Long)

    // Meetings
    @Query("SELECT * FROM meetings ORDER BY id DESC")
    fun getAllMeetings(): Flow<List<MeetingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeeting(meeting: MeetingEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeetings(meetings: List<MeetingEntity>)

    @Update
    suspend fun updateMeeting(meeting: MeetingEntity)

    // Complaints
    @Query("SELECT * FROM complaints ORDER BY id DESC")
    fun getAllComplaints(): Flow<List<ComplaintEntity>>

    @Query("SELECT * FROM complaints WHERE roomNumber = :roomNumber ORDER BY id DESC")
    fun getComplaintsForRoom(roomNumber: Int): Flow<List<ComplaintEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComplaint(complaint: ComplaintEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComplaints(complaints: List<ComplaintEntity>)

    @Update
    suspend fun updateComplaint(complaint: ComplaintEntity)

    // Audit Logs
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllAuditLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity): Long

    // Society Settings
    @Query("SELECT * FROM society_settings WHERE id = 1")
    fun getSettings(): Flow<SocietySettingsEntity?>

    @Query("SELECT * FROM society_settings WHERE id = 1")
    suspend fun getSettingsSync(): SocietySettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: SocietySettingsEntity)

    @Update
    suspend fun updateSettings(settings: SocietySettingsEntity)
}
