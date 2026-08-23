package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE userId = :userId ORDER BY isFavorite DESC, name ASC")
    fun getProductsByUser(userId: Long): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: Long): Product?

    @Query("SELECT * FROM products WHERE userId = :userId AND (name LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%') ORDER BY isFavorite DESC, name ASC")
    fun searchProducts(userId: Long, query: String): Flow<List<Product>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    @Update
    suspend fun updateProduct(product: Product)

    @Query("UPDATE products SET isFavorite = :isFavorite WHERE id = :productId")
    suspend fun toggleFavorite(productId: Long, isFavorite: Boolean)

    @Query("UPDATE products SET stock = stock - :qty WHERE id = :productId AND stock >= :qty")
    suspend fun deductStock(productId: Long, qty: Int): Int

    @Query("SELECT * FROM products WHERE userId = :userId AND stock <= 5 ORDER BY stock ASC")
    fun getLowStockProducts(userId: Long): Flow<List<Product>>

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("DELETE FROM products WHERE userId = :userId")
    suspend fun deleteAllByUser(userId: Long)
}
