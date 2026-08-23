package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activity_logs")
data class ActivityLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val userId: Long = 1L,
    val userEmail: String = "",
    val userName: String = "",
    val action: String, // "LOGIN", "CREATE_INVOICE", "DUPLICATE_INVOICE", "SUBMIT_PAYMENT", "APPROVE_PAYMENT", "UPDATE_SETTINGS", "EXPORT_DATA", etc.
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)
