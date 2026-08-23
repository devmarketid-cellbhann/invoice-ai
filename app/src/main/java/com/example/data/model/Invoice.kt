package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "invoices")
data class Invoice(
    @PrimaryKey
    val invoiceNumber: String, // Format: INV-YYYYMMDD-XXXX or Custom Prefix
    val userId: Long = 1L,
    val customerId: Long,
    val customerName: String,
    val customerPhone: String = "",
    val customerAddress: String = "",
    val invoiceDate: Long = System.currentTimeMillis(),
    val dueDate: Long = System.currentTimeMillis() + (7L * 24 * 60 * 60 * 1000),
    val status: String = "Belum Dibayar", // "Belum Dibayar", "Sudah Dibayar", "Terlambat"
    val notes: String = "",
    val subtotal: Long = 0L,
    val taxPercent: Int = 0, // 0 - 25%
    val taxAmount: Long = 0L,
    val grandTotal: Long = 0L,
    val paidDate: Long? = null,
    val isArchived: Boolean = false,
    val pdfOrientation: String = "PORTRAIT", // "PORTRAIT", "LANDSCAPE"
    val createdAt: Long = System.currentTimeMillis()
)
