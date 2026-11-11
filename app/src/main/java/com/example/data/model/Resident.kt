package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity representing a Society Resident stored in the local SQLite database.
 */
@Entity(tableName = "residents")
data class Resident(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val flatNumber: String,
    val wing: String = "Wing A",
    val floor: Int = 1,
    val phone: String,
    val email: String = "",
    val residentType: String = "OWNER", // "OWNER" or "TENANT"
    val numberOfOccupants: Int = 1,
    val emergencyContact: String = "",
    val moveInDate: String = "",
    val isActive: Boolean = true
)

typealias ResidentEntity = Resident
