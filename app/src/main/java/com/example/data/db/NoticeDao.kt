package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Notice
import com.example.data.model.NoticeCategory
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Room Entity [Notice].
 */
@Dao
interface NoticeDao {

    @Query("SELECT * FROM notices ORDER BY isUrgent DESC, id DESC")
    fun getAllNotices(): Flow<List<Notice>>

    @Query("SELECT * FROM notices WHERE id = :id")
    fun getNoticeById(id: Long): Flow<Notice?>

    @Query("SELECT * FROM notices WHERE isUrgent = 1 ORDER BY id DESC")
    fun getUrgentNotices(): Flow<List<Notice>>

    @Query("SELECT * FROM notices WHERE category = :category ORDER BY id DESC")
    fun getNoticesByCategory(category: NoticeCategory): Flow<List<Notice>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotice(notice: Notice): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotices(notices: List<Notice>)

    @Update
    suspend fun updateNotice(notice: Notice)

    @Delete
    suspend fun deleteNotice(notice: Notice)

    @Query("DELETE FROM notices WHERE id = :id")
    suspend fun deleteNoticeById(id: Long)
}
