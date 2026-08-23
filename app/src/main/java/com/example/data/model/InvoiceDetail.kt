package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "invoice_details")
data class InvoiceDetail(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val invoiceNumber: String,
    val productId: Long? = null,
    val productName: String,
    val unitPrice: Long,
    val quantity: Int,
    val unit: String = "Pcs",
    val lineSubtotal: Long
)
