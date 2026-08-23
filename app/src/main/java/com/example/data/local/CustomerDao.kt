package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Customer
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers WHERE userId = :userId ORDER BY isFavorite DESC, name ASC")
    fun getCustomersByUser(userId: Long): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    suspend fun getCustomerById(id: Long): Customer?

    @Query("SELECT * FROM customers WHERE userId = :userId AND (name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%') ORDER BY isFavorite DESC, name ASC")
    fun searchCustomers(userId: Long, query: String): Flow<List<Customer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer): Long

    @Update
    suspend fun updateCustomer(customer: Customer)

    @Query("UPDATE customers SET isFavorite = :isFavorite WHERE id = :customerId")
    suspend fun toggleFavorite(customerId: Long, isFavorite: Boolean)

    @Query("UPDATE customers SET totalTransactions = totalTransactions + 1, totalSpend = totalSpend + :amount WHERE id = :customerId")
    suspend fun recordCustomerTransaction(customerId: Long, amount: Long)

    @Delete
    suspend fun deleteCustomer(customer: Customer)

    @Query("SELECT COUNT(*) FROM customers WHERE userId = :userId")
    fun getCustomerCount(userId: Long): Flow<Int>

    @Query("DELETE FROM customers WHERE userId = :userId")
    suspend fun deleteAllByUser(userId: Long)
}
