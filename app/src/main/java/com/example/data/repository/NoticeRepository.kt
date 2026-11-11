package com.example.data.repository

import com.example.data.db.NoticeDao
import com.example.data.model.Notice
import com.example.data.model.NoticeCategory
import kotlinx.coroutines.flow.Flow

/**
 * Repository providing access to the Room [Notice] database table.
 */
class NoticeRepository(private val noticeDao: NoticeDao) {

    val allNotices: Flow<List<Notice>> = noticeDao.getAllNotices()
    val urgentNotices: Flow<List<Notice>> = noticeDao.getUrgentNotices()

    fun getNoticeById(id: Long): Flow<Notice?> = noticeDao.getNoticeById(id)

    fun getNoticesByCategory(category: NoticeCategory): Flow<List<Notice>> = noticeDao.getNoticesByCategory(category)

    suspend fun insertNotice(notice: Notice): Long = noticeDao.insertNotice(notice)
    suspend fun insert(notice: Notice): Long = insertNotice(notice)

    suspend fun insertNotices(notices: List<Notice>) = noticeDao.insertNotices(notices)
    suspend fun insertAll(notices: List<Notice>) = insertNotices(notices)

    suspend fun updateNotice(notice: Notice) = noticeDao.updateNotice(notice)
    suspend fun update(notice: Notice) = updateNotice(notice)

    suspend fun deleteNotice(notice: Notice) = noticeDao.deleteNotice(notice)
    suspend fun delete(notice: Notice) = deleteNotice(notice)

    suspend fun deleteNoticeById(id: Long) = noticeDao.deleteNoticeById(id)
    suspend fun deleteById(id: Long) = deleteNoticeById(id)
}
