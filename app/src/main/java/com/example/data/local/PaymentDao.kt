package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Payment
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments WHERE userId = :userId ORDER BY paymentDate DESC")
    fun getPaymentsByUser(userId: Long): Flow<List<Payment>>

    @Query("SELECT * FROM payments ORDER BY paymentDate DESC")
    fun getAllPayments(): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE id = :id LIMIT 1")
    suspend fun getPaymentById(id: Long): Payment?

    @Query("SELECT * FROM payments WHERE userId = :userId AND paymentStatus = 'Menunggu Verifikasi' LIMIT 1")
    suspend fun getPendingPaymentByUser(userId: Long): Payment?

    @Query("SELECT * FROM payments WHERE proofHash = :hash AND proofHash != '' LIMIT 1")
    suspend fun getPaymentByProofHash(hash: String): Payment?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: Payment): Long

    @Update
    suspend fun updatePayment(payment: Payment)

    @Query("UPDATE payments SET paymentStatus = :status, verifiedAt = :verifiedAt, verifiedBy = :verifiedBy, rejectionReason = :reason, statusHistory = :statusHistory WHERE id = :paymentId")
    suspend fun updatePaymentStatus(paymentId: Long, status: String, verifiedAt: Long?, verifiedBy: String, reason: String, statusHistory: String)

    @Query("DELETE FROM payments WHERE userId = :userId")
    suspend fun deleteAllByUser(userId: Long)
}
