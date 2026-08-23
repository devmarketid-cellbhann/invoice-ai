package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payments")
data class Payment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val userId: Long = 1L,
    val userEmail: String = "",
    val userName: String = "",
    val businessName: String = "",
    val planPurchased: String, // "Pro" atau "Bisnis"
    val paymentDate: Long = System.currentTimeMillis(),
    val amountPaid: Long, // Rp 29.000 / Rp 79.000
    val paymentStatus: String = "Menunggu Verifikasi", // "Menunggu Verifikasi", "Disetujui", "Ditolak", "Kadaluwarsa"
    val paymentMethod: String = "QRIS", // "QRIS", "Transfer Bank"
    val senderName: String = "",
    val senderBank: String = "BCA", // BCA, Mandiri, BRI, BNI, GoPay, OVO, Dana, ShopeePay
    val referenceCode: String = "", // INVQ-XXXX-XXXX
    val proofNote: String = "",
    val proofImageUri: String? = null,
    val proofHash: String = "", // Hash or string check to prevent duplicate uploads
    val mismatchAmountWarning: Boolean = false, // Flagged if amount does not equal standard price
    val rejectionReason: String = "",
    val statusHistory: String = "", // Audit trail of changes
    val verifiedAt: Long? = null,
    val verifiedBy: String = ""
)
