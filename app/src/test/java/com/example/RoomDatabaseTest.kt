package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDatabase
import com.example.data.db.MaintenancePaymentDao
import com.example.data.db.NoticeDao
import com.example.data.db.PaymentDao
import com.example.data.db.ResidentDao
import com.example.data.model.MaintenancePayment
import com.example.data.model.MaintenancePaymentStatus
import com.example.data.model.Notice
import com.example.data.model.NoticeCategory
import com.example.data.model.Payment
import com.example.data.model.Resident
import com.example.data.repository.MaintenancePaymentRepository
import com.example.data.repository.NoticeRepository
import com.example.data.repository.PaymentRepository
import com.example.data.repository.ResidentRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RoomDatabaseTest {

    private lateinit var database: AppDatabase
    private lateinit var residentDao: ResidentDao
    private lateinit var maintenancePaymentDao: MaintenancePaymentDao
    private lateinit var paymentDao: PaymentDao
    private lateinit var noticeDao: NoticeDao

    private lateinit var residentRepository: ResidentRepository
    private lateinit var maintenancePaymentRepository: MaintenancePaymentRepository
    private lateinit var paymentRepository: PaymentRepository
    private lateinit var noticeRepository: NoticeRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        residentDao = database.residentDao()
        maintenancePaymentDao = database.maintenancePaymentDao()
        paymentDao = database.paymentDao()
        noticeDao = database.noticeDao()

        residentRepository = ResidentRepository(residentDao)
        maintenancePaymentRepository = MaintenancePaymentRepository(maintenancePaymentDao)
        paymentRepository = PaymentRepository(paymentDao)
        noticeRepository = NoticeRepository(noticeDao)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testResidentEntityCRUD() = runBlocking {
        val resident = Resident(
            name = "Gajanan S.",
            flatNumber = "402",
            wing = "Wing A",
            floor = 4,
            phone = "+91 98200 11223",
            email = "gajanan@example.com",
            residentType = "OWNER",
            numberOfOccupants = 3,
            emergencyContact = "+91 98200 99999",
            moveInDate = "01-Jan-2025",
            isActive = true
        )

        // Insert
        val id = residentRepository.insert(resident)
        assertTrue(id > 0)

        // Query
        val allResidents = residentRepository.allResidents.first()
        assertEquals(1, allResidents.size)
        assertEquals("Gajanan S.", allResidents[0].name)
        assertEquals("402", allResidents[0].flatNumber)

        // Search
        val searched = residentRepository.searchResidents("Gajanan").first()
        assertEquals(1, searched.size)

        // Update
        val updatedResident = allResidents[0].copy(phone = "+91 98200 99887")
        residentRepository.update(updatedResident)
        val afterUpdate = residentRepository.getResidentById(id).first()
        assertNotNull(afterUpdate)
        assertEquals("+91 98200 99887", afterUpdate?.phone)

        // Delete
        residentRepository.delete(updatedResident)
        val afterDelete = residentRepository.allResidents.first()
        assertEquals(0, afterDelete.size)
    }

    @Test
    fun testPaymentEntityCRUD() = runBlocking {
        val payment1 = Payment(
            flatNumber = "402",
            residentName = "Gajanan S.",
            amount = 500.0,
            paymentDate = "26-Sep-2026",
            paymentMonth = "August 2026",
            paymentMode = "UPI",
            status = "PAID",
            transactionRef = "UPI/202608/402",
            receiptNumber = "MM/26-27/501",
            remarks = "Maintenance August"
        )
        val payment2 = Payment(
            flatNumber = "402",
            residentName = "Gajanan S.",
            amount = 750.0,
            paymentDate = "26-Sep-2026",
            paymentMonth = "September 2026",
            paymentMode = "Bank Transfer",
            status = "PAID",
            transactionRef = "NEFT/SBIN/402",
            receiptNumber = "MM/26-27/502",
            remarks = "Maintenance September"
        )

        val id1 = paymentRepository.insert(payment1)
        val id2 = paymentRepository.insert(payment2)
        assertTrue(id1 > 0)
        assertTrue(id2 > 0)

        val allPayments = paymentRepository.allPayments.first()
        assertEquals(2, allPayments.size)

        val totalCollected = paymentRepository.totalCollectedAmount.first()
        assertEquals(1250.0, totalCollected ?: 0.0, 0.01)

        val forFlat = paymentRepository.getPaymentsForFlat("402").first()
        assertEquals(2, forFlat.size)

        // Delete one
        paymentRepository.deleteById(id1)
        val remaining = paymentRepository.allPayments.first()
        assertEquals(1, remaining.size)
        assertEquals(id2, remaining[0].id)
    }

    @Test
    fun testNoticeEntityCRUD() = runBlocking {
        val notice1 = Notice(
            title = "Annual Fire Safety Drill",
            category = NoticeCategory.EMERGENCY_ALERT,
            content = "Fire drill on Sunday 10 AM",
            date = "26-Sep-2026",
            isUrgent = true,
            issuedBy = "Secretary"
        )
        val notice2 = Notice(
            title = "Water Tank Cleaning",
            category = NoticeCategory.WATER_SUPPLY,
            content = "Cleaning scheduled for Monday",
            date = "26-Sep-2026",
            isUrgent = false,
            issuedBy = "Chairman"
        )

        val id1 = noticeRepository.insert(notice1)
        val id2 = noticeRepository.insert(notice2)
        assertTrue(id1 > 0)
        assertTrue(id2 > 0)

        val allNotices = noticeRepository.allNotices.first()
        assertEquals(2, allNotices.size)

        val urgentNotices = noticeRepository.urgentNotices.first()
        assertEquals(1, urgentNotices.size)
        assertEquals("Annual Fire Safety Drill", urgentNotices[0].title)

        // Delete
        noticeRepository.deleteById(id2)
        val remaining = noticeRepository.allNotices.first()
        assertEquals(1, remaining.size)
        assertEquals(id1, remaining[0].id)
    }

    @Test
    fun testMaintenancePaymentLinkedToResident() = runBlocking {
        // 1. Insert parent Resident for Unit 504
        val residentId = residentRepository.insert(
            Resident(
                name = "Priya Deshmukh",
                flatNumber = "504",
                wing = "Wing A",
                floor = 5,
                phone = "+91 98201 55443",
                residentType = "TENANT",
                numberOfOccupants = 2,
                moveInDate = "01-Mar-2026"
            )
        )
        assertTrue(residentId > 0)

        // 2. Insert PAID and PENDING MaintenancePayment records linked via residentId
        val paidPaymentId = maintenancePaymentRepository.insert(
            MaintenancePayment(
                residentId = residentId,
                unitNumber = "504",
                billingMonth = "October 2026",
                amount = 750.0,
                penaltyAmount = 0.0,
                amountPaid = 750.0,
                dueDate = "10-Oct-2026",
                paymentDate = "06-Oct-2026",
                status = MaintenancePaymentStatus.PAID,
                paymentMethod = "UPI",
                transactionReference = "UPI/202610/504"
            )
        )
        val pendingPaymentId = maintenancePaymentRepository.insert(
            MaintenancePayment(
                residentId = residentId,
                unitNumber = "504",
                billingMonth = "November 2026",
                amount = 750.0,
                penaltyAmount = 0.0,
                amountPaid = 0.0,
                dueDate = "10-Nov-2026",
                paymentDate = null,
                status = MaintenancePaymentStatus.PENDING
            )
        )
        assertTrue(paidPaymentId > 0)
        assertTrue(pendingPaymentId > 0)

        // 3. Verify @Relation ResidentWithMaintenancePayments query
        val residentWithPayments = maintenancePaymentRepository
            .getResidentWithPaymentsById(residentId)
            .first()
        assertNotNull(residentWithPayments)
        assertEquals("Priya Deshmukh", residentWithPayments?.resident?.name)
        assertEquals(2, residentWithPayments?.maintenancePayments?.size)

        // 4. Verify @Relation MaintenancePaymentWithResident join query
        val joinedPayments = maintenancePaymentRepository.allMaintenancePaymentsWithResidents.first()
        assertEquals(2, joinedPayments.size)
        assertEquals("504", joinedPayments[0].resident.flatNumber)

        // 5. Verify status update from PENDING to PAID with paymentDate
        val pendingEntry = maintenancePaymentRepository.getById(pendingPaymentId).first()
        assertNotNull(pendingEntry)
        assertNull(pendingEntry?.paymentDate)

        maintenancePaymentRepository.update(
            pendingEntry!!.copy(
                status = MaintenancePaymentStatus.PAID,
                amountPaid = 750.0,
                paymentDate = "02-Nov-2026",
                paymentMethod = "Bank Transfer"
            )
        )
        val totalCollected = maintenancePaymentRepository.totalCollected.first()
        assertEquals(1500.0, totalCollected ?: 0.0, 0.01)

        // 6. Verify ForeignKey CASCADE delete when parent Resident is deleted
        residentRepository.deleteById(residentId)
        val remainingMaintenancePayments = maintenancePaymentRepository.allMaintenancePayments.first()
        assertEquals(0, remainingMaintenancePayments.size)
    }
}
