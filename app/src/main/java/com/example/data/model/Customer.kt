package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val userId: Long = 1L,
    val name: String,
    val phone: String,
    val email: String = "",
    val address: String = "",
    val notes: String = "",
    val isFavorite: Boolean = false,
    val totalTransactions: Long = 0L,
    val totalSpend: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)
