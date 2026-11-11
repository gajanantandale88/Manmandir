package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AuditLogEntity
import com.example.data.model.CommitteeTaskEntity
import com.example.data.model.ComplaintCategory
import com.example.data.model.ComplaintEntity
import com.example.data.model.ComplaintStatus
import com.example.data.model.CorrespondenceEntity
import com.example.data.model.CorrespondenceType
import com.example.data.model.DocumentCategory
import com.example.data.model.ExpenseCategory
import com.example.data.model.ExpenseEntity
import com.example.data.model.FollowUpStatus
import com.example.data.model.MaintenanceDemandEntity
import com.example.data.model.MaintenancePayment
import com.example.data.model.MaintenancePaymentStatus
import com.example.data.model.MeetingEntity
import com.example.data.model.MeetingType
import com.example.data.model.Notice
import com.example.data.model.NoticeCategory
import com.example.data.model.NoticeEntity
import com.example.data.model.OccupancyType
import com.example.data.model.Payment
import com.example.data.model.PaymentEntity
import com.example.data.model.ReceiptEntity
import com.example.data.model.Resident
import com.example.data.model.ResidentEntity
import com.example.data.model.SocietyDocumentEntity
import com.example.data.model.SocietySettingsEntity
import com.example.data.model.TaskStatus
import com.example.data.model.UnitEntity
import com.example.data.model.UnitType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Resident::class,
        MaintenancePayment::class,
        Payment::class,
        Notice::class,
        UnitEntity::class,
        MaintenanceDemandEntity::class,
        ReceiptEntity::class,
        SocietyDocumentEntity::class,
        CorrespondenceEntity::class,
        ExpenseEntity::class,
        CommitteeTaskEntity::class,
        MeetingEntity::class,
        ComplaintEntity::class,
        AuditLogEntity::class,
        SocietySettingsEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun residentDao(): ResidentDao
    abstract fun maintenancePaymentDao(): MaintenancePaymentDao
    abstract fun paymentDao(): PaymentDao
    abstract fun noticeDao(): NoticeDao
    abstract fun societyDao(): SocietyDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "manmandir_society_db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(private val scope: CoroutineScope) : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialDatabase(database)
                    }
                }
            }
        }

        suspend fun populateInitialDatabase(database: AppDatabase) {
            val dao = database.societyDao()
            val residentDao = database.residentDao()
            val maintenancePaymentDao = database.maintenancePaymentDao()
            val paymentDao = database.paymentDao()
            val noticeDao = database.noticeDao()
            // 1. Seed Society Settings
            val defaultSettings = SocietySettingsEntity(
                id = 1,
                societyName = "Manmandir Cooperative Housing Society (Proposed)",
                wingName = "Wing A (11 Floors)",
                ownerMaintenanceRate = 500.0,
                tenantMaintenanceRate = 750.0,
                gracePeriodMonths = 2,
                penaltyRateMonthlyPercent = 22.0,
                dueDayOfMonth = 10,
                openingBalance = 485600.0,
                emergencyContact = "+91 98200 45678",
                secretaryName = "Mr. Rajesh Sharma",
                chairmanName = "Mr. Suresh Kadam",
                treasurerName = "Mrs. Sunita Patil"
            )
            dao.insertSettings(defaultSettings)

            // 2. Generate 88 Units across 11 floors (8 units per floor)
            val unitsList = mutableListOf<UnitEntity>()
            val sampleOwnerNames = listOf(
                "Rajesh Sharma", "Suresh Kadam", "Sunita Patil", "Amit Verma", "Vijay Deshmukh",
                "Deepak Mehta", "Kiran Joshi", "Sanjay Rao", "Anil Kulkarni", "Prashant Shinde",
                "Mahesh Gaikwad", "Manoj Tiwari", "Girish Hegde", "Prakash Shetty", "Ramesh Sawant",
                "Nitin More", "Sachin Kale", "Vikas Jadhav", "Ashok Bhave", "Dinesh Naik",
                "Rohit Pawar", "Hemant Rane", "Sunil Gokhale", "Kishore Chitre", "Vinay Patwardhan",
                "Gajanan Mane", "Arun Chitale", "Sameer Bhat", "Ravindra Date", "Bhalchandra Joshi",
                "Shrikant Karkhanis", "Ajay Godbole", "Chetan Pendse", "Jayant Kelkar", "Abhay Soman",
                "Pankaj Apte", "Shailesh Bapat", "Vidyadhar Sathe", "Milind Oka", "Mukund Ranade",
                "Anand Sahasrabuddhe", "Umesh Phadke", "Sarang Mahajan", "Tushar Agashe", "Narendra Gogte",
                "Gautam Kunte", "Harish Natu", "Satyajit Gadgil", "Chandrashekhar Limaye", "Sudhir Datar",
                "Pradeep Karve", "Bhushan Deodhar", "Avinash Sohoni", "Kedar Barve", "Siddharth Damle",
                "Makarand Paranjape", "Yogesh Vaze", "Sujit Kirtane", "Amol Dixit", "Swapnil Modak",
                "Devendra Tilak", "Vikram Chiplunkar", "Mandhar Marathe", "Omkar Athavale", "Nikhil Abhyankar",
                "Kaustubh Ghaisas", "Pushkar Velankar", "Chaitanya Ranade", "Tanmay Kane", "Nachiket Lele",
                "Pranav Kelkar", "Atharva Joshi", "Chinmay Gore", "Rohan Phatak", "Varun Sahani"
            )

            val sampleTenantNames = listOf(
                "Rahul Khanna", "Pooja Singhania", "Vikram Malhotra", "Sneha Roy",
                "Arjun Kapoor", "Meera Sen", "Aditya Nair", "Priya Menon",
                "Karan Bajaj", "Divya Pillai", "Rishabh Sethi", "Ananya Ghosh",
                "Siddharth Bhatt", "Neha Nambiar", "Varun Chawla", "Isha Mukherjee",
                "Gaurav Saxena", "Tanvi Joshi", "Naveen Reddy", "Shweta Iyengar"
            )

            var ownerIdx = 0
            var tenantIdx = 0

            for (floor in 1..11) {
                for (roomIndex in 1..8) {
                    val roomNumber = floor * 100 + roomIndex

                    when {
                        roomNumber == 104 -> {
                            // Society Office on Floor 1
                            unitsList.add(
                                UnitEntity(
                                    roomNumber = 104,
                                    floor = 1,
                                    unitType = UnitType.OFFICE,
                                    occupancyType = OccupancyType.NONE,
                                    ownerName = "Manmandir CHS Office",
                                    ownerPhone = "+91 22 2430 1104",
                                    ownerEmail = "office@manmandir-chs.org",
                                    monthlyMaintenance = 0.0,
                                    numberOfOccupants = 0,
                                    submittedDocs = "Society Registration, Office Allotment Resolution",
                                    pendingDocs = "None",
                                    isOccupied = true
                                )
                            )
                        }
                        roomNumber == 803 -> {
                            // Refuge Area 1 on Floor 8
                            unitsList.add(
                                UnitEntity(
                                    roomNumber = 803,
                                    floor = 8,
                                    unitType = UnitType.REFUGE_AREA,
                                    occupancyType = OccupancyType.NONE,
                                    ownerName = "Designated Refuge Area #1",
                                    ownerPhone = "N/A",
                                    ownerEmail = "refuge@manmandir-chs.org",
                                    monthlyMaintenance = 0.0,
                                    numberOfOccupants = 0,
                                    submittedDocs = "CFO Fire NOC, BMC Approved Floor Plan",
                                    pendingDocs = "None",
                                    isOccupied = false
                                )
                            )
                        }
                        roomNumber == 804 -> {
                            // Refuge Area 2 on Floor 8
                            unitsList.add(
                                UnitEntity(
                                    roomNumber = 804,
                                    floor = 8,
                                    unitType = UnitType.REFUGE_AREA,
                                    occupancyType = OccupancyType.NONE,
                                    ownerName = "Designated Refuge Area #2",
                                    ownerPhone = "N/A",
                                    ownerEmail = "refuge@manmandir-chs.org",
                                    monthlyMaintenance = 0.0,
                                    numberOfOccupants = 0,
                                    submittedDocs = "CFO Fire NOC, BMC Approved Floor Plan",
                                    pendingDocs = "None",
                                    isOccupied = false
                                )
                            )
                        }
                        else -> {
                            // Residential Unit (Total 85 units)
                            val isTenant = (roomIndex == 2 || roomIndex == 6) && (tenantIdx < sampleTenantNames.size)
                            val ownerName = sampleOwnerNames[ownerIdx % sampleOwnerNames.size]
                            ownerIdx++

                            if (isTenant) {
                                val tenantName = sampleTenantNames[tenantIdx % sampleTenantNames.size]
                                tenantIdx++
                                unitsList.add(
                                    UnitEntity(
                                        roomNumber = roomNumber,
                                        floor = floor,
                                        unitType = UnitType.RESIDENTIAL,
                                        occupancyType = OccupancyType.TENANT,
                                        ownerName = ownerName,
                                        ownerPhone = "+91 9820${(1000 + roomNumber)}",
                                        ownerEmail = "owner.${roomNumber}@manmandir-chs.org",
                                        tenantName = tenantName,
                                        tenantPhone = "+91 9870${(2000 + roomNumber)}",
                                        tenantEmail = "${tenantName.lowercase().replace(" ", ".")}@gmail.com",
                                        alternateContact = "+91 22 2844 ${1000 + roomNumber}",
                                        numberOfOccupants = 3,
                                        occupancyDate = "01-Jan-2025",
                                        emergencyContact = "+91 98200 99999",
                                        submittedDocs = "Registered Rent Agreement, Police Verification",
                                        pendingDocs = if (roomIndex == 6) "Tenant ID Proof Renewal" else "None",
                                        monthlyMaintenance = 750.0,
                                        isOccupied = true,
                                        user1Name = tenantName,
                                        user1Phone = "+91 9870${(2000 + roomNumber)}",
                                        user1Email = "${tenantName.lowercase().replace(" ", ".")}@gmail.com",
                                        user1Designation = "Primary Tenant",
                                        user2Name = ownerName,
                                        user2Phone = "+91 9820${(1000 + roomNumber)}",
                                        user2Email = "owner.${roomNumber}@manmandir-chs.org",
                                        user2Designation = "Flat Owner (Landlord)"
                                    )
                                )
                            } else {
                                val coOwnerName = when (roomIndex % 3) {
                                    0 -> "${ownerName.split(" ").firstOrNull() ?: ""} (Spouse)"
                                    1 -> "${ownerName.split(" ").firstOrNull() ?: ""} (Joint Owner)"
                                    else -> "Co-Owner / Nominee"
                                }
                                unitsList.add(
                                    UnitEntity(
                                        roomNumber = roomNumber,
                                        floor = floor,
                                        unitType = UnitType.RESIDENTIAL,
                                        occupancyType = OccupancyType.OWNER,
                                        ownerName = ownerName,
                                        ownerPhone = "+91 9820${(1000 + roomNumber)}",
                                        ownerEmail = "${ownerName.lowercase().replace(" ", ".")}@gmail.com",
                                        tenantName = "",
                                        tenantPhone = "",
                                        tenantEmail = "",
                                        alternateContact = "+91 22 2844 ${1000 + roomNumber}",
                                        numberOfOccupants = (2..5).random(),
                                        occupancyDate = "15-Aug-2024",
                                        emergencyContact = "+91 98201 11222",
                                        submittedDocs = "Index II, Allotment Letter, Share Certificate Application, Aadhaar/PAN",
                                        pendingDocs = if (roomNumber in listOf(305, 507, 702)) "PAN Card Copy" else "None",
                                        monthlyMaintenance = 500.0,
                                        isOccupied = true,
                                        user1Name = ownerName,
                                        user1Phone = "+91 9820${(1000 + roomNumber)}",
                                        user1Email = "${ownerName.lowercase().replace(" ", ".")}@gmail.com",
                                        user1Designation = "Primary Member (Owner)",
                                        user2Name = coOwnerName,
                                        user2Phone = "+91 9821${(3000 + roomNumber)}",
                                        user2Email = "joint.${roomNumber}@manmandir-chs.org",
                                        user2Designation = "Co-Owner / Joint Member"
                                    )
                                )
                            }
                        }
                    }
                }
            }
            dao.insertUnits(unitsList)

            // 3. Generate Maintenance Demands for June, July, August 2026
            val months = listOf(
                Triple("June 2026", 202606, "10-Jun-2026"),
                Triple("July 2026", 202607, "10-Jul-2026"),
                Triple("August 2026", 202608, "10-Aug-2026")
            )

            val demandsList = mutableListOf<MaintenanceDemandEntity>()
            val receiptsList = mutableListOf<ReceiptEntity>()
            var receiptCounter = 101

            for (unit in unitsList.filter { it.unitType == UnitType.RESIDENTIAL }) {
                val base = if (unit.occupancyType == OccupancyType.TENANT) 750.0 else 500.0

                for ((mName, mIndex, mDue) in months) {
                    val isPaid: Boolean
                    val paidDate: String?
                    val payMethod: String?
                    val txnRef: String?
                    val receiptNum: String?

                    // Simulate realistic status based on room number pattern
                    when {
                        unit.roomNumber in listOf(101, 102, 103, 105, 201, 202, 301, 401, 501, 601, 701, 801, 901, 1001, 1101, 205) -> {
                            // Prompt payer (all months paid)
                            isPaid = true
                            paidDate = "05-$mName"
                            payMethod = "UPI (GooglePay)"
                            txnRef = "UPI/2026${mIndex}/${unit.roomNumber}"
                            receiptNum = "MM/26-27/${receiptCounter++}"
                        }
                        unit.roomNumber in listOf(106, 206, 306, 406, 506) -> {
                            // Paid June and July, August Pending
                            if (mIndex < 202608) {
                                isPaid = true
                                paidDate = "08-$mName"
                                payMethod = "Bank NEFT"
                                txnRef = "NEFT/SBIN/887${mIndex}"
                                receiptNum = "MM/26-27/${receiptCounter++}"
                            } else {
                                isPaid = false
                                paidDate = null
                                payMethod = null
                                txnRef = null
                                receiptNum = null
                            }
                        }
                        unit.roomNumber in listOf(707, 807, 907) -> {
                            // Paid June, July & August Pending (2 months pending)
                            if (mIndex == 202606) {
                                isPaid = true
                                paidDate = "02-$mName"
                                payMethod = "Cheque #44891"
                                txnRef = "CHQ/HDFC/44891"
                                receiptNum = "MM/26-27/${receiptCounter++}"
                            } else {
                                isPaid = false
                                paidDate = null
                                payMethod = null
                                txnRef = null
                                receiptNum = null
                            }
                        }
                        unit.roomNumber in listOf(1008, 1108) -> {
                            // Unpaid for all 3 months -> Overdue with 22% penalty on expired grace period
                            isPaid = false
                            paidDate = null
                            payMethod = null
                            txnRef = null
                            receiptNum = null
                        }
                        else -> {
                            // Generally up-to-date or current month pending
                            if (mIndex < 202608) {
                                isPaid = true
                                paidDate = "06-$mName"
                                payMethod = "UPI"
                                txnRef = "UPI/AXIS/${unit.roomNumber}"
                                receiptNum = "MM/26-27/${receiptCounter++}"
                            } else {
                                isPaid = (unit.roomNumber % 2 == 0)
                                paidDate = if (isPaid) "07-Aug-2026" else null
                                payMethod = if (isPaid) "Cash" else null
                                txnRef = if (isPaid) "CSH-REC-${unit.roomNumber}" else null
                                receiptNum = if (isPaid) "MM/26-27/${receiptCounter++}" else null
                            }
                        }
                    }

                    demandsList.add(
                        MaintenanceDemandEntity(
                            roomNumber = unit.roomNumber,
                            monthYear = mName,
                            monthIndex = mIndex,
                            baseAmount = base,
                            paidAmount = if (isPaid) base else 0.0,
                            dueDate = mDue,
                            isPaid = isPaid,
                            paidDate = paidDate,
                            paymentMethod = payMethod,
                            transactionRef = txnRef,
                            receiptNumber = receiptNum
                        )
                    )

                    if (isPaid && receiptNum != null && paidDate != null && payMethod != null && txnRef != null) {
                        receiptsList.add(
                            ReceiptEntity(
                                receiptNumber = receiptNum,
                                date = paidDate,
                                roomNumber = unit.roomNumber,
                                residentName = if (unit.occupancyType == OccupancyType.TENANT) unit.tenantName else unit.ownerName,
                                occupancyType = unit.occupancyType,
                                maintenanceMonth = mName,
                                maintenanceAmount = base,
                                previousOutstanding = 0.0,
                                penaltyAmount = 0.0,
                                totalAmountReceived = base,
                                paymentMethod = payMethod,
                                transactionRef = txnRef,
                                remainingBalance = 0.0,
                                authorizedMember = "Mrs. Sunita Patil (Treasurer)",
                                remarks = "Maintenance received on time with thanks."
                            )
                        )
                    }
                }
            }
            dao.insertDemands(demandsList)
            dao.insertReceipts(receiptsList)

            // Seed Room Resident entities
            val residentsList = mutableListOf<Resident>()
            for (unit in unitsList.filter { it.unitType == UnitType.RESIDENTIAL }) {
                val isTenant = unit.occupancyType == OccupancyType.TENANT
                residentsList.add(
                    Resident(
                        name = if (isTenant) unit.tenantName else unit.ownerName,
                        flatNumber = unit.roomNumber.toString(),
                        wing = "Wing A",
                        floor = unit.floor,
                        phone = if (isTenant) unit.tenantPhone else unit.ownerPhone,
                        email = if (isTenant) unit.tenantEmail else unit.ownerEmail,
                        residentType = if (isTenant) "TENANT" else "OWNER",
                        numberOfOccupants = unit.numberOfOccupants,
                        emergencyContact = unit.emergencyContact,
                        moveInDate = unit.occupancyDate.ifBlank { "01-Jan-2025" },
                        isActive = true
                    )
                )
            }
            residentDao.insertResidents(residentsList)

            // Retrieve inserted Residents with auto-generated IDs to link ForeignKey on MaintenancePayment
            val insertedResidentsByUnit = residentDao.getAllResidentsSync().associateBy { it.flatNumber }

            // Seed Room MaintenancePayment entities linked to Resident records
            val maintenancePaymentsList = mutableListOf<MaintenancePayment>()
            for (demand in demandsList) {
                val unitStr = demand.roomNumber.toString()
                val linkedResident = insertedResidentsByUnit[unitStr] ?: continue
                val status = when {
                    demand.isPaid -> MaintenancePaymentStatus.PAID
                    demand.monthIndex < 202608 -> MaintenancePaymentStatus.OVERDUE
                    else -> MaintenancePaymentStatus.PENDING
                }
                val penalty = if (!demand.isPaid && demand.monthIndex <= 202606) {
                    val overdueMonths = 202608 - demand.monthIndex - 1
                    if (overdueMonths > 0) kotlin.math.round(demand.baseAmount * 0.22 * overdueMonths) else 0.0
                } else {
                    0.0
                }
                maintenancePaymentsList.add(
                    MaintenancePayment(
                        residentId = linkedResident.id,
                        unitNumber = unitStr,
                        billingMonth = demand.monthYear,
                        amount = demand.baseAmount,
                        penaltyAmount = penalty,
                        amountPaid = if (demand.isPaid) demand.paidAmount else 0.0,
                        dueDate = demand.dueDate,
                        paymentDate = demand.paidDate,
                        status = status,
                        paymentMethod = demand.paymentMethod ?: "",
                        transactionReference = demand.transactionRef ?: "",
                        receiptNumber = demand.receiptNumber ?: "",
                        remarks = if (demand.isPaid) "Maintenance received on time" else "Monthly maintenance demand"
                    )
                )
            }
            maintenancePaymentDao.insertMaintenancePayments(maintenancePaymentsList)

            // Seed Room Payment entities
            val paymentsList = mutableListOf<Payment>()
            for (receipt in receiptsList) {
                val linkedResidentId = insertedResidentsByUnit[receipt.roomNumber.toString()]?.id ?: 0L
                paymentsList.add(
                    Payment(
                        residentId = linkedResidentId,
                        flatNumber = receipt.roomNumber.toString(),
                        residentName = receipt.residentName,
                        amount = receipt.totalAmountReceived,
                        paymentDate = receipt.date,
                        paymentMonth = receipt.maintenanceMonth,
                        paymentMode = receipt.paymentMethod,
                        status = "PAID",
                        transactionRef = receipt.transactionRef,
                        receiptNumber = receipt.receiptNumber,
                        remarks = receipt.remarks
                    )
                )
            }
            paymentDao.insertPayments(paymentsList)

            // 4. Seed Central Society Documents
            val societyDocs = listOf(
                SocietyDocumentEntity(
                    title = "Society Registration Proposal & Byelaws Copy",
                    category = DocumentCategory.SOCIETY_REGISTRATION,
                    fileRef = "DOC-REG-BYELAWS-2026.pdf",
                    dateUploaded = "10-Jan-2026",
                    isResidentAccessible = true,
                    uploadedBy = "Secretary (Rajesh Sharma)",
                    description = "Official copy of Society registration proposal submitted to the Sub-Registrar of Cooperative Societies.",
                    fileSize = "4.8 MB"
                ),
                SocietyDocumentEntity(
                    title = "BMC Occupation Certificate (OC) & Sanctioned Plans",
                    category = DocumentCategory.BMC_CORRESPONDENCE,
                    fileRef = "BMC-OC-SANCTION-WING-A.pdf",
                    dateUploaded = "15-Feb-2026",
                    isResidentAccessible = true,
                    uploadedBy = "Chairman (Suresh Kadam)",
                    description = "Municipal Corporation of Greater Mumbai approved building plans, OC and water sanction layout.",
                    fileSize = "8.2 MB"
                ),
                SocietyDocumentEntity(
                    title = "CFO Fire Safety NOC & Refuge Area Compliance",
                    category = DocumentCategory.GOVERNMENT_LETTERS,
                    fileRef = "CFO-FIRE-NOC-WING-A.pdf",
                    dateUploaded = "20-Mar-2026",
                    isResidentAccessible = true,
                    uploadedBy = "Secretary (Rajesh Sharma)",
                    description = "Fire Brigade NOC compliance certificate verifying refuge floors 803 and 804 with fire escape apparatus.",
                    fileSize = "2.1 MB"
                ),
                SocietyDocumentEntity(
                    title = "MMRDA Infrastructure & Access Road Sanction",
                    category = DocumentCategory.MMRDA_CORRESPONDENCE,
                    fileRef = "MMRDA-ACCESS-ROAD-2026.pdf",
                    dateUploaded = "05-Apr-2026",
                    isResidentAccessible = true,
                    uploadedBy = "Managing Committee",
                    description = "MMRDA communication regarding society peripheral service road and drainage connectivity.",
                    fileSize = "1.5 MB"
                ),
                SocietyDocumentEntity(
                    title = "Annual General Body Meeting (AGM) #01 Minutes",
                    category = DocumentCategory.MEETING_MINUTES,
                    fileRef = "AGM-01-SIGNED-MINUTES.pdf",
                    dateUploaded = "12-May-2026",
                    isResidentAccessible = true,
                    uploadedBy = "Secretary (Rajesh Sharma)",
                    description = "Signed minutes of the 1st General Body Meeting covering maintenance structure ₹500/₹750 approval.",
                    fileSize = "3.2 MB"
                ),
                SocietyDocumentEntity(
                    title = "Otis Lift Comprehensive Annual Maintenance Contract (AMC)",
                    category = DocumentCategory.AGREEMENTS,
                    fileRef = "OTIS-LIFT-AMC-2026-27.pdf",
                    dateUploaded = "01-Jun-2026",
                    isResidentAccessible = true,
                    uploadedBy = "Treasurer (Sunita Patil)",
                    description = "Signed 2-year maintenance contract with Otis Elevator Company India Ltd for Wing A high-speed lifts.",
                    fileSize = "2.9 MB"
                ),
                SocietyDocumentEntity(
                    title = "Local Police Station Security Guard Verification NOC",
                    category = DocumentCategory.POLICE_CORRESPONDENCE,
                    fileRef = "POLICE-VERIF-SECURITY-STAFF.pdf",
                    dateUploaded = "18-Jun-2026",
                    isResidentAccessible = false,
                    uploadedBy = "Managing Committee",
                    description = "Verified character certificates and police clearance for all security and housekeeping personnel.",
                    fileSize = "1.8 MB"
                ),
                SocietyDocumentEntity(
                    title = "Water Tank Cleaning & Microbiological Test Report",
                    category = DocumentCategory.BILLS_AND_QUOTATIONS,
                    fileRef = "WATER-TANK-CLEAN-REPORT-JULY.pdf",
                    dateUploaded = "20-Jul-2026",
                    isResidentAccessible = true,
                    uploadedBy = "Office Admin",
                    description = "Underground and overhead tank desilting, UV disinfection certification and lab potability certificate.",
                    fileSize = "1.1 MB"
                )
            )
            dao.insertDocuments(societyDocs)

            // 5. Seed Correspondence
            val correspondences = listOf(
                CorrespondenceEntity(
                    type = CorrespondenceType.INCOMING,
                    date = "12-Aug-2026",
                    party = "BMC G/North Ward Water Dept",
                    subject = "Sanction of Additional 100mm Water Line Connection",
                    referenceNumber = "BMC/GN/WAT/2026/9024",
                    description = "Formal reply from Executive Engineer sanctioning the enhanced municipal water quota for Wing A.",
                    status = FollowUpStatus.IN_PROGRESS,
                    followUpDate = "28-Aug-2026",
                    responsibleMember = "Mr. Suresh Kadam (Chairman)",
                    attachedDocument = "BMC-WATER-SANCTION-LETTER.pdf"
                ),
                CorrespondenceEntity(
                    type = CorrespondenceType.OUTGOING,
                    date = "05-Aug-2026",
                    party = "Adani Electricity / MSEDCL",
                    subject = "Application for Common Area Tariff Rectification & Solar Net-Metering",
                    referenceNumber = "MM/OUT/2026/044",
                    description = "Formal application requesting residential tariff classification for society common pumping and lift load.",
                    status = FollowUpStatus.PENDING,
                    followUpDate = "25-Aug-2026",
                    responsibleMember = "Mrs. Sunita Patil (Treasurer)",
                    attachedDocument = "MSEDCL-APPLICATION-COPY.pdf"
                ),
                CorrespondenceEntity(
                    type = CorrespondenceType.INCOMING,
                    date = "22-Jul-2026",
                    party = "Local Police Station (Sr. Inspector)",
                    subject = "Mandate for 100% Tenant Verification Forms & Index II",
                    referenceNumber = "POL/LPS/VERIF/2026/112",
                    description = "Advisory from Police Department to ensure all tenants submit verification copies within 15 days.",
                    status = FollowUpStatus.RESOLVED,
                    followUpDate = "10-Aug-2026",
                    responsibleMember = "Mr. Rajesh Sharma (Secretary)",
                    attachedDocument = "POLICE-MANDATE-NOTICE.pdf"
                ),
                CorrespondenceEntity(
                    type = CorrespondenceType.OUTGOING,
                    date = "18-Jun-2026",
                    party = "Otis Elevator Co. Ltd",
                    subject = "Notice regarding sensor alignment on Passenger Lift #1",
                    referenceNumber = "MM/OUT/2026/031",
                    description = "Service call escalation regarding intermittent door sensor obstruction on 8th floor.",
                    status = FollowUpStatus.CLOSED,
                    followUpDate = "25-Jun-2026",
                    responsibleMember = "Office Admin",
                    attachedDocument = "LIFT-SERVICE-CALL-REPORT.pdf"
                )
            )
            dao.insertCorrespondenceList(correspondences)

            // 6. Seed Society Expenses
            val expenses = listOf(
                ExpenseEntity(
                    date = "15-Aug-2026",
                    category = ExpenseCategory.SECURITY,
                    description = "Monthly Security Agency Charges (4 Guards 24x7 rotation)",
                    vendorName = "Rakshak Security & Facility Services",
                    amount = 48000.0,
                    paymentMethod = "Bank NEFT",
                    billNumber = "RSFS/AUG/2026-92",
                    committeeApproval = "Approved in MC Meeting #12",
                    receiptDoc = "REC-SECURITY-AUG.pdf",
                    remarks = "Payment cleared after verifying biometric attendance."
                ),
                ExpenseEntity(
                    date = "10-Aug-2026",
                    category = ExpenseCategory.ELECTRICITY,
                    description = "Common Meter Electricity Bill (Lifts, Pumps, Corridor Lighting)",
                    vendorName = "MSEDCL / Adani Power",
                    amount = 32450.0,
                    paymentMethod = "Online Billdesk",
                    billNumber = "MSEB-CA-9082341",
                    committeeApproval = "Regular Operating Expense",
                    receiptDoc = "MSEB-AUG-RECEIPT.pdf",
                    remarks = "Paid before due date to avail prompt payment discount."
                ),
                ExpenseEntity(
                    date = "05-Aug-2026",
                    category = ExpenseCategory.LIFT,
                    description = "Quarterly Comprehensive Lift AMC & Preventive Lubrication",
                    vendorName = "Otis Elevator Company India Ltd",
                    amount = 18500.0,
                    paymentMethod = "Cheque #90214",
                    billNumber = "OTIS-INV-88741",
                    committeeApproval = "Approved under AMC Contract",
                    receiptDoc = "OTIS-Q2-INVOICE.pdf",
                    remarks = "Both Passenger and Service lifts inspected and certified."
                ),
                ExpenseEntity(
                    date = "01-Aug-2026",
                    category = ExpenseCategory.CLEANING,
                    description = "Housekeeping Staff Salary & Common Area Sanitization Materials",
                    vendorName = "CleanCity Facility Solutions",
                    amount = 22000.0,
                    paymentMethod = "Bank Transfer",
                    billNumber = "CCFS/2026/08",
                    committeeApproval = "Regular Operating Expense",
                    receiptDoc = "CLEANING-STAFF-VOUCHER.pdf",
                    remarks = "Covers daily floor mopping, refuge area sanitization, and garbage disposal."
                ),
                ExpenseEntity(
                    date = "22-Jul-2026",
                    category = ExpenseCategory.WATER,
                    description = "Emergency Water Tankers during municipal pipeline maintenance (6 tankers)",
                    vendorName = "Om Sai Water Suppliers",
                    amount = 12000.0,
                    paymentMethod = "UPI",
                    billNumber = "OSWS-TNK-441",
                    committeeApproval = "Emergency Approved by Chairman",
                    receiptDoc = "WATER-TANKER-CHALLAN.pdf",
                    remarks = "Procured during BMC 24-hour supply shutdown."
                )
            )
            dao.insertExpenses(expenses)

            // 7. Seed Notices
            val notices = listOf(
                NoticeEntity(
                    title = "Notice: Monthly Maintenance Dues for August 2026",
                    category = NoticeCategory.MAINTENANCE_REMINDER,
                    content = "Dear Members and Residents, Monthly maintenance for August 2026 (₹500 for Owners / ₹750 for Tenants) is due by 10th August. As per society rules, a 2-month grace period applies before the 22% monthly penalty is levied. Please pay via UPI, Bank Transfer or at the Society Office.",
                    date = "01-Aug-2026",
                    isUrgent = true,
                    issuedBy = "Managing Committee"
                ),
                NoticeEntity(
                    title = "Overhead & Underground Water Tank Cleaning Schedule",
                    category = NoticeCategory.WATER_SUPPLY,
                    content = "Water tanks across Wing A will undergo mechanized UV desilting and chemical sanitization on Sunday, 24th August from 10:00 AM to 4:00 PM. Water supply will remain suspended during this window. Residents are requested to store adequate water.",
                    date = "15-Aug-2026",
                    isUrgent = false,
                    issuedBy = "Hon. Secretary (Rajesh Sharma)"
                ),
                NoticeEntity(
                    title = "Mandatory Police Verification Submission for all Tenants",
                    category = NoticeCategory.GOVERNMENT_NOTICE,
                    content = "All flat owners with rented units are strictly instructed to submit copies of registered tenancy agreements and local police verification acknowledgment slips at the Society Office (Flat 104) before 31st August.",
                    date = "10-Aug-2026",
                    isUrgent = true,
                    issuedBy = "Managing Committee"
                ),
                NoticeEntity(
                    title = "Independence Day & Society Cultural Gathering",
                    category = NoticeCategory.SOCIETY_EVENT,
                    content = "Flag hoisting ceremony was celebrated at the ground floor Welfare Centre followed by society tea and cultural performances by children.",
                    date = "15-Aug-2026",
                    isUrgent = false,
                    issuedBy = "Cultural Sub-Committee"
                )
            )
            dao.insertNotices(notices)
            noticeDao.insertNotices(notices)

            // 8. Seed Committee Tasks
            val tasks = listOf(
                CommitteeTaskEntity(
                    title = "Rooftop Solar Grid Net-Metering Installation",
                    description = "Coordinate with MSEDCL accredited vendor for 15 kWp solar panel installation to offset common lift and lighting power bills.",
                    assignedTo = "Mr. Suresh Kadam (Chairman)",
                    dueDate = "15-Sep-2026",
                    status = TaskStatus.IN_PROGRESS,
                    contractorInfo = "SunSpark Solar Energy Ltd (Mr. Nitin Joshi - 98201 55667)",
                    attachments = "Solar-Feasibility-Report.pdf"
                ),
                CommitteeTaskEntity(
                    title = "CCTV Surveillance Expansion in Refuge Floors & Staircases",
                    description = "Install 8 high-definition night vision IP cameras on 8th Floor Refuge area and emergency staircases.",
                    assignedTo = "Mr. Rajesh Sharma (Secretary)",
                    dueDate = "30-Aug-2026",
                    status = TaskStatus.IN_PROGRESS,
                    contractorInfo = "SecureVision Tech Solutions (Mr. Arvind Patel)",
                    attachments = "CCTV-Quotation-WingA.pdf"
                ),
                CommitteeTaskEntity(
                    title = "Annual Fire Extinguisher Refilling & Hose Reel Drill",
                    description = "Complete ABC dry powder cartridge refilling on all 11 floors and conduct resident fire drill.",
                    assignedTo = "Mrs. Sunita Patil (Treasurer)",
                    dueDate = "10-Sep-2026",
                    status = TaskStatus.PENDING,
                    contractorInfo = "FireGuard Systems Mumbai",
                    attachments = "Fire-Safety-Checklist.pdf"
                )
            )
            dao.insertTasks(tasks)

            // 9. Seed Meetings
            val meetings = listOf(
                MeetingEntity(
                    type = MeetingType.GENERAL_BODY_MEETING,
                    date = "10-May-2026",
                    time = "10:30 AM",
                    venue = "Society Welfare Centre (Ground Floor)",
                    agenda = "1. Confirmation of provisional committee\n2. Formal approval of monthly maintenance ₹500 (Owner) / ₹750 (Tenant)\n3. 22% penalty rule on dues beyond 2-month grace\n4. Lift AMC contract finalization",
                    minutes = "All 85 residential members attended or sent proxies. Resolution #1 passed unanimously approving maintenance charges.",
                    decisions = "1. Maintenance ₹500/₹750 approved.\n2. Penalty 22% per month after 2 months grace approved.\n3. Otis AMC ratified.",
                    attendanceSummary = "74 Members Present in person, 8 Proxies, Quorum established.",
                    signedMinutesDoc = "AGM-01-SIGNED.pdf"
                ),
                MeetingEntity(
                    type = MeetingType.COMMITTEE_MEETING,
                    date = "02-Aug-2026",
                    time = "07:30 PM",
                    venue = "Society Office (Flat 104)",
                    agenda = "1. Review of July 2026 maintenance collection\n2. Status of pending tenant documents\n3. Water tank cleaning quote selection\n4. Independence Day celebration budget",
                    minutes = "Committee reviewed pending accounts. Instructed treasurer to issue reminder notices for unpaid members.",
                    decisions = "Sanctioned ₹12,000 for tank cleaning. Reminders dispatched to overdue flats.",
                    attendanceSummary = "All 7 Committee Members present.",
                    signedMinutesDoc = "MC-MEET-12-MINUTES.pdf"
                )
            )
            dao.insertMeetings(meetings)

            // 10. Seed Complaints
            val complaints = listOf(
                ComplaintEntity(
                    ticketNumber = "TKT-1081",
                    roomNumber = 702,
                    residentName = "Vijay Deshmukh",
                    category = ComplaintCategory.WATER,
                    description = "Low water pressure in kitchen inlet pipe during morning supply hours (06:00 AM - 08:00 AM).",
                    status = ComplaintStatus.IN_PROGRESS,
                    dateSubmitted = "18-Aug-2026",
                    assignedTo = "Society Plumber (Sanjay)",
                    resolutionRemarks = "Plumber inspected air-lock in 7th floor manifold. Pressure booster valve adjustment underway."
                ),
                ComplaintEntity(
                    ticketNumber = "TKT-1082",
                    roomNumber = 405,
                    residentName = "Amit Verma",
                    category = ComplaintCategory.LIFT,
                    description = "Slight squeaking noise heard when Passenger Lift #1 ascends past 4th floor.",
                    status = ComplaintStatus.RESOLVED,
                    dateSubmitted = "12-Aug-2026",
                    assignedTo = "Otis Service Engineer (Rahul)",
                    resolutionRemarks = "Guide rail guide shoe lubricated and counterweight rollers calibrated. Issue resolved."
                ),
                ComplaintEntity(
                    ticketNumber = "TKT-1083",
                    roomNumber = 205,
                    residentName = "Rohit Pawar",
                    category = ComplaintCategory.PARKING,
                    description = "Visitor scooter frequently parked blocking entrance of stilt parking bay #09.",
                    status = ComplaintStatus.CLOSED,
                    dateSubmitted = "08-Aug-2026",
                    assignedTo = "Security Supervisor (Day Shift)",
                    resolutionRemarks = "Security instructed to enforce strict visitor parking in designated open slots only."
                )
            )
            dao.insertComplaints(complaints)

            // 11. Seed Audit Logs
            val auditLogs = listOf(
                AuditLogEntity(
                    formattedDateTime = "10-May-2026 12:45 PM",
                    userName = "Super Admin (System)",
                    action = "INITIAL_SYSTEM_SETUP",
                    details = "Initialized Manmandir Cooperative Housing Society (Proposed) Wing A with 88 total units (85 residential, 1 office #104, 2 refuge #803 & #804) and Ground Floor facilities.",
                    previousValue = "None",
                    newValue = "88 Units Initialized"
                ),
                AuditLogEntity(
                    formattedDateTime = "10-May-2026 01:15 PM",
                    userName = "Hon. Secretary (Rajesh Sharma)",
                    action = "MAINTENANCE_RATES_CONFIGURED",
                    details = "Configured Owner rate ₹500, Tenant rate ₹750, 2-Month Grace Period, 22% Monthly Penalty Rate as approved in General Body Meeting.",
                    previousValue = "₹0.00",
                    newValue = "Owner: ₹500, Tenant: ₹750, Penalty: 22%/mo"
                ),
                AuditLogEntity(
                    formattedDateTime = "01-Aug-2026 09:00 AM",
                    userName = "Automated Monthly Engine",
                    action = "MONTHLY_DEMAND_GENERATED",
                    details = "Generated August 2026 maintenance demand for 85 residential units.",
                    previousValue = "July 2026 Cycle",
                    newValue = "August 2026 Active Cycle"
                )
            )
            for (log in auditLogs) {
                dao.insertAuditLog(log)
            }
        }
    }
}
