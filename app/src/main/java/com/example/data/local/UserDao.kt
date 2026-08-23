package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getUserById(id: Long): Flow<User?>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserByIdSync(id: Long): User?

    @Query("SELECT * FROM users ORDER BY id ASC")
    fun getAllUsers(): Flow<List<User>>

    @Query("SELECT * FROM users LIMIT 1")
    fun getFirstUser(): Flow<User?>

    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getFirstUserSync(): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Update
    suspend fun updateUser(user: User)

    @Query("UPDATE users SET packageTier = :tier, invoiceLimit = :limit, packageStartDate = :start, packageEndDate = :end WHERE id = :userId")
    suspend fun updateSubscription(userId: Long, tier: String, limit: Int, start: Long, end: Long)

    @Query("UPDATE users SET invoicesThisMonth = invoicesThisMonth + 1 WHERE id = :userId")
    suspend fun incrementInvoiceCount(userId: Long)

    @Query("UPDATE users SET qrisMerchantName = :merchantName, qrisNmid = :nmid WHERE id = :userId")
    suspend fun updateQrisSettings(userId: Long, merchantName: String, nmid: String)

    @Query("UPDATE users SET role = :role WHERE id = :userId")
    suspend fun updateUserRole(userId: Long, role: String)

    @Query("UPDATE users SET lastSyncTime = :syncTime WHERE id = :userId")
    suspend fun updateLastSyncTime(userId: Long, syncTime: Long)

    @Query("UPDATE users SET defaultDueDays = :dueDays, invoicePrefix = :prefix, taxName = :taxName, taxPercent = :taxPercent, taxEnabled = :taxEnabled, invoiceFooterNotes = :footerNotes WHERE id = :userId")
    suspend fun updateInvoiceSettings(userId: Long, dueDays: Int, prefix: String, taxName: String, taxPercent: Int, taxEnabled: Boolean, footerNotes: String)

    @Query("UPDATE users SET website = :website, socialMedia = :socialMedia, signatureName = :sigName, signatureRole = :sigRole WHERE id = :userId")
    suspend fun updateSignatureAndSocial(userId: Long, website: String, socialMedia: String, sigName: String, sigRole: String)

    @Query("UPDATE users SET notificationPreference = :pref, adminWhatsApp = :adminWa WHERE id = :userId")
    suspend fun updateNotificationPreferences(userId: Long, pref: String, adminWa: String)

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUser(userId: Long)
}
