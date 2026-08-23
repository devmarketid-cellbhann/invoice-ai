package com.example.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class InvoiceWithDetails(
    @Embedded val invoice: Invoice,
    @Relation(
        parentColumn = "invoiceNumber",
        entityColumn = "invoiceNumber"
    )
    val details: List<InvoiceDetail>
)
