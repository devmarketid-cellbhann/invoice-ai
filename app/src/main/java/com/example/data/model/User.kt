package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val fullName: String,
    val businessName: String,
    val email: String,
    val whatsapp: String,
    val address: String = "",
    val website: String = "",
    val socialMedia: String = "",
    val logoUri: String? = null,
    val password: String,
    val role: String = "Owner", // "Owner", "Kasir", "Admin"
    val packageTier: String = "Gratis", // "Gratis", "Pro", "Bisnis"
    val packageStartDate: Long = System.currentTimeMillis(),
    val packageEndDate: Long = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000),
    val invoicesThisMonth: Int = 0,
    val invoiceLimit: Int = 5, // 5 for Gratis, -1 for unlimited
    val qrisMerchantName: String = "INVOICEAI NUSANTARA PUSAT",
    val qrisNmid: String = "ID1029384756019",
    val cloudSyncEnabled: Boolean = true,
    val lastSyncTime: Long = System.currentTimeMillis(),
    // Invoice settings
    val defaultDueDays: Int = 7, // 7, 14, 30, etc.
    val invoicePrefix: String = "INV-",
    val taxName: String = "PPN",
    val taxPercent: Int = 11,
    val taxEnabled: Boolean = false,
    val invoiceFooterNotes: String = "Pembayaran via Transfer Bank. Harap sertakan No. Invoice pada berita transfer.",
    val signatureName: String = "Pemilik Usaha",
    val signatureRole: String = "Owner / Pimpinan",
    val signatureDate: Long = System.currentTimeMillis(),
    val notificationPreference: String = "ALL", // "ALL", "IN_APP_ONLY", "OFF"
    val adminWhatsApp: String = "081234567890",
    val darkMode: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
