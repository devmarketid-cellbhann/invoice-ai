package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.Invoice
import com.example.data.model.InvoiceDetail
import com.example.data.model.InvoiceWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDao {
    @Transaction
    @Query("SELECT * FROM invoices WHERE userId = :userId ORDER BY invoiceDate DESC")
    fun getAllInvoicesWithDetails(userId: Long): Flow<List<InvoiceWithDetails>>

    @Transaction
    @Query("SELECT * FROM invoices WHERE userId = :userId AND isArchived = :isArchived ORDER BY invoiceDate DESC")
    fun getInvoicesByArchiveStatus(userId: Long, isArchived: Boolean): Flow<List<InvoiceWithDetails>>

    @Transaction
    @Query("SELECT * FROM invoices WHERE userId = :userId AND customerId = :customerId ORDER BY invoiceDate DESC")
    fun getInvoicesByCustomer(userId: Long, customerId: Long): Flow<List<InvoiceWithDetails>>

    @Transaction
    @Query("SELECT * FROM invoices WHERE invoiceNumber = :invoiceNumber LIMIT 1")
    suspend fun getInvoiceWithDetailsByNumber(invoiceNumber: String): InvoiceWithDetails?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: Invoice)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoiceDetails(details: List<InvoiceDetail>)

    @Update
    suspend fun updateInvoice(invoice: Invoice)

    @Query("UPDATE invoices SET status = 'Sudah Dibayar', paidDate = :paidDate WHERE invoiceNumber = :invoiceNumber")
    suspend fun markInvoicePaid(invoiceNumber: String, paidDate: Long = System.currentTimeMillis())

    @Query("UPDATE invoices SET status = 'Terlambat' WHERE dueDate < :currentTimestamp AND status = 'Belum Dibayar'")
    suspend fun updateOverdueStatus(currentTimestamp: Long = System.currentTimeMillis())

    @Query("UPDATE invoices SET isArchived = :isArchived WHERE invoiceNumber = :invoiceNumber")
    suspend fun setArchived(invoiceNumber: String, isArchived: Boolean)

    @Delete
    suspend fun deleteInvoice(invoice: Invoice)

    @Query("DELETE FROM invoice_details WHERE invoiceNumber = :invoiceNumber")
    suspend fun deleteInvoiceDetailsByNumber(invoiceNumber: String)

    @Query("SELECT COUNT(*) FROM invoices WHERE userId = :userId AND invoiceDate >= :startOfMonth AND invoiceDate <= :endOfMonth")
    suspend fun countInvoicesThisMonth(userId: Long, startOfMonth: Long, endOfMonth: Long): Int

    @Query("SELECT COUNT(*) FROM invoices WHERE invoiceNumber LIKE :prefix || '%'")
    suspend fun countInvoicesForPrefix(prefix: String): Int

    @Query("SELECT * FROM invoice_details")
    fun getAllInvoiceDetails(): Flow<List<InvoiceDetail>>

    @Query("DELETE FROM invoices WHERE userId = :userId")
    suspend fun deleteAllInvoicesByUser(userId: Long)

    @Query("DELETE FROM invoice_details WHERE invoiceNumber IN (SELECT invoiceNumber FROM invoices WHERE userId = :userId)")
    suspend fun deleteAllInvoiceDetailsByUser(userId: Long)
}
