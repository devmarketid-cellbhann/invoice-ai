package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val userId: Long = 1L,
    val name: String,
    val price: Long,
    val unit: String = "Pcs", // Pcs, Lot, Kg, Jam, Hari, Paket, Box
    val category: String = "Umum",
    val stock: Int = 100,
    val notes: String = "",
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
