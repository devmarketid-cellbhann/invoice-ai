package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class AppNotification(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val userId: Long = 1L,
    val title: String,
    val message: String,
    val type: String, // "PAYMENT_APPROVED", "PAYMENT_REJECTED", "PENDING_VERIFICATION", "PACKAGE_EXPIRING", "INVOICE_OVERDUE", "ADMIN_MESSAGE", "INVOICE_CREATED"
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val actionData: String = ""
)
