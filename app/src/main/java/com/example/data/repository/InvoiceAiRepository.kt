package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.model.ActivityLog
import com.example.data.model.AppNotification
import com.example.data.model.Customer
import com.example.data.model.Invoice
import com.example.data.model.InvoiceDetail
import com.example.data.model.InvoiceWithDetails
import com.example.data.model.Payment
import com.example.data.model.Product
import com.example.data.model.User
import com.example.util.Formatters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class InvoiceAiRepository(private val db: AppDatabase) {

    private val userDao = db.userDao()
    private val customerDao = db.customerDao()
    private val productDao = db.productDao()
    private val invoiceDao = db.invoiceDao()
    private val paymentDao = db.paymentDao()
    private val notificationDao = db.notificationDao()
    private val activityLogDao = db.activityLogDao()

    val currentUser: Flow<User?> = userDao.getFirstUser()

    fun getCustomers(userId: Long): Flow<List<Customer>> = customerDao.getCustomersByUser(userId)
    fun searchCustomers(userId: Long, query: String): Flow<List<Customer>> = customerDao.searchCustomers(userId, query)

    fun getProducts(userId: Long): Flow<List<Product>> = productDao.getProductsByUser(userId)
    fun searchProducts(userId: Long, query: String): Flow<List<Product>> = productDao.searchProducts(userId, query)
    fun getLowStockProducts(userId: Long): Flow<List<Product>> = productDao.getLowStockProducts(userId)

    fun getAllInvoices(userId: Long): Flow<List<InvoiceWithDetails>> = invoiceDao.getAllInvoicesWithDetails(userId)
    fun getInvoicesByArchiveStatus(userId: Long, isArchived: Boolean): Flow<List<InvoiceWithDetails>> = 
        invoiceDao.getInvoicesByArchiveStatus(userId, isArchived)
    fun getInvoicesByCustomer(userId: Long, customerId: Long): Flow<List<InvoiceWithDetails>> = 
        invoiceDao.getInvoicesByCustomer(userId, customerId)

    fun getPayments(userId: Long): Flow<List<Payment>> = paymentDao.getPaymentsByUser(userId)
    fun getAllPaymentsForAdmin(): Flow<List<Payment>> = paymentDao.getAllPayments()
    fun getAllUsers(): Flow<List<User>> = userDao.getAllUsers()

    // Notification flows
    fun getNotifications(userId: Long): Flow<List<AppNotification>> = notificationDao.getNotificationsByUser(userId)
    fun getUnreadNotificationCount(userId: Long): Flow<Int> = notificationDao.getUnreadCount(userId)

    // Activity log flows
    fun getActivityLogs(userId: Long): Flow<List<ActivityLog>> = activityLogDao.getLogsByUser(userId)
    fun getAllActivityLogsForAdmin(): Flow<List<ActivityLog>> = activityLogDao.getAllLogsForAdmin()

    suspend fun checkAndSeedInitialData() = withContext(Dispatchers.IO) {
        val existingUser = userDao.getFirstUserSync()
        if (existingUser == null) {
            val defaultUser = User(
                id = 1L,
                fullName = "Budi Santoso",
                businessName = "PT Kreasi Solusi Digital",
                email = "budi@kreasidigital.id",
                whatsapp = "081234567890",
                address = "Jl. Sudirman No. 45, Jakarta Selatan",
                website = "www.kreasidigital.id",
                socialMedia = "@kreasidigital.id",
                password = "password123",
                role = "Owner",
                packageTier = "Gratis",
                invoicesThisMonth = 3,
                invoiceLimit = 5,
                qrisMerchantName = "INVOICEAI NUSANTARA PUSAT",
                qrisNmid = "ID1029384756019",
                cloudSyncEnabled = true,
                defaultDueDays = 7,
                invoicePrefix = "INV-",
                taxName = "PPN",
                taxPercent = 11,
                taxEnabled = true,
                signatureName = "Budi Santoso, S.Kom",
                signatureRole = "Direktur Utama"
            )
            userDao.insertUser(defaultUser)

            val adminUser = User(
                id = 2L,
                fullName = "Administrator (Subhan)",
                businessName = "InvoiceAI DevMarket Pusat",
                email = "devmarketid@gmail.com",
                whatsapp = "081234567890",
                address = "Gedung Cyber 2 Lt. 15, Kuningan, Jakarta",
                website = "www.devmarket.id",
                password = "Subhan1211",
                role = "Admin",
                packageTier = "Bisnis",
                invoicesThisMonth = 0,
                invoiceLimit = -1,
                qrisMerchantName = "INVOICEAI NUSANTARA PUSAT",
                qrisNmid = "ID1029384756019"
            )
            val kasirUser = User(
                id = 3L,
                fullName = "Siti Kasir",
                businessName = "PT Kreasi Solusi Digital",
                email = "kasir@kreasidigital.id",
                whatsapp = "081299887766",
                address = "Cabang Senopati, Jakarta Selatan",
                password = "kasir123password",
                role = "Kasir",
                packageTier = "Gratis",
                invoicesThisMonth = 1,
                invoiceLimit = 5
            )
            userDao.insertUser(adminUser)
            userDao.insertUser(kasirUser)

            // Seed Customers
            val c1 = Customer(
                id = 1L,
                userId = 1L,
                name = "CV Maju Jaya Abadi",
                phone = "081987654321",
                email = "info@majujaya.co.id",
                address = "Kawasan Industri Pulo Gadung, Jakarta Timur",
                notes = "Klien prioritas maintenance IT bulanan",
                isFavorite = true,
                totalTransactions = 2,
                totalSpend = 6500000L
            )
            val c2 = Customer(
                id = 2L,
                userId = 1L,
                name = "Toko Berkah Mandiri",
                phone = "085612345678",
                email = "berkahmandiri@gmail.com",
                address = "Ruko Grand Galaxy City Blok RGA No. 12, Bekasi",
                notes = "Usaha retail fashion & perlengkapan",
                isFavorite = false,
                totalTransactions = 1,
                totalSpend = 2500000L
            )
            val c3 = Customer(
                id = 3L,
                userId = 1L,
                name = "PT Nusantara Logistik",
                phone = "081398761234",
                email = "finance@nusantaralogistik.com",
                address = "Jl. Tanjung Priok Raya No. 88, Jakarta Utara",
                notes = "Proyek integrasi sistem tracking pengiriman",
                isFavorite = true,
                totalTransactions = 1,
                totalSpend = 8000000L
            )
            customerDao.insertCustomer(c1)
            customerDao.insertCustomer(c2)
            customerDao.insertCustomer(c3)

            // Seed Products
            val p1 = Product(
                id = 1L,
                userId = 1L,
                name = "Jasa Pembuatan Website Company Profile",
                price = 3500000L,
                unit = "Paket",
                category = "Jasa Digital",
                stock = 99,
                isFavorite = true,
                notes = "Desain responsif, CMS, domain .id & hosting 1 tahun"
            )
            val p2 = Product(
                id = 2L,
                userId = 1L,
                name = "Paket Maintenance & Backup Server",
                price = 1500000L,
                unit = "Bulan",
                category = "Jasa IT",
                stock = 99,
                isFavorite = true,
                notes = "Monitoring 24/7, security patch, & backup cloud mingguan"
            )
            val p3 = Product(
                id = 3L,
                userId = 1L,
                name = "Desain UI/UX Mobile App",
                price = 2500000L,
                unit = "Paket",
                category = "Desain",
                stock = 99,
                notes = "Figma prototyping, high fidelity, 10 screen utama"
            )
            val p4 = Product(
                id = 4L,
                userId = 1L,
                name = "Hardware POS Barcode Scanner",
                price = 750000L,
                unit = "Pcs",
                category = "Perangkat Keras",
                stock = 3,
                notes = "Scanner 2D QR Code wireless Bluetooth"
            )
            productDao.insertProduct(p1)
            productDao.insertProduct(p2)
            productDao.insertProduct(p3)
            productDao.insertProduct(p4)

            // Seed Sample Invoices
            val todayPrefix = Formatters.getTodayDatePrefix()
            val inv1Number = "INV-$todayPrefix-0001"
            val inv2Number = "INV-$todayPrefix-0002"
            val inv3Number = "INV-$todayPrefix-0003"

            val inv1 = Invoice(
                invoiceNumber = inv1Number,
                userId = 1L,
                customerId = 1L,
                customerName = "CV Maju Jaya Abadi",
                customerPhone = "081987654321",
                customerAddress = "Kawasan Industri Pulo Gadung, Jakarta Timur",
                invoiceDate = System.currentTimeMillis() - (10L * 24 * 60 * 60 * 1000),
                dueDate = System.currentTimeMillis() - (3L * 24 * 60 * 60 * 1000),
                status = "Sudah Dibayar",
                notes = "Pembayaran via Transfer BCA. Terima kasih!",
                subtotal = 5000000L,
                taxPercent = 11,
                taxAmount = 550000L,
                grandTotal = 5550000L,
                paidDate = System.currentTimeMillis() - (4L * 24 * 60 * 60 * 1000)
            )
            val inv1Details = listOf(
                InvoiceDetail(
                    invoiceNumber = inv1Number,
                    productId = 1L,
                    productName = "Jasa Pembuatan Website Company Profile",
                    unitPrice = 3500000L,
                    quantity = 1,
                    unit = "Paket",
                    lineSubtotal = 3500000L
                ),
                InvoiceDetail(
                    invoiceNumber = inv1Number,
                    productId = 2L,
                    productName = "Paket Maintenance & Backup Server",
                    unitPrice = 1500000L,
                    quantity = 1,
                    unit = "Bulan",
                    lineSubtotal = 1500000L
                )
            )

            val inv2 = Invoice(
                invoiceNumber = inv2Number,
                userId = 1L,
                customerId = 2L,
                customerName = "Toko Berkah Mandiri",
                customerPhone = "085612345678",
                customerAddress = "Ruko Grand Galaxy City Blok RGA No. 12, Bekasi",
                invoiceDate = System.currentTimeMillis() - (5L * 24 * 60 * 60 * 1000),
                dueDate = System.currentTimeMillis() + (2L * 24 * 60 * 60 * 1000),
                status = "Belum Dibayar",
                notes = "Jatuh tempo 7 hari sejak tanggal terbit faktur.",
                subtotal = 2500000L,
                taxPercent = 0,
                taxAmount = 0L,
                grandTotal = 2500000L
            )
            val inv2Details = listOf(
                InvoiceDetail(
                    invoiceNumber = inv2Number,
                    productId = 3L,
                    productName = "Desain UI/UX Mobile App",
                    unitPrice = 2500000L,
                    quantity = 1,
                    unit = "Paket",
                    lineSubtotal = 2500000L
                )
            )

            val inv3 = Invoice(
                invoiceNumber = inv3Number,
                userId = 1L,
                customerId = 3L,
                customerName = "PT Nusantara Logistik",
                customerPhone = "081398761234",
                customerAddress = "Jl. Tanjung Priok Raya No. 88, Jakarta Utara",
                invoiceDate = System.currentTimeMillis() - (15L * 24 * 60 * 60 * 1000),
                dueDate = System.currentTimeMillis() - (2L * 24 * 60 * 60 * 1000),
                status = "Terlambat",
                notes = "Mohon segera melakukan transfer pelunasan.",
                subtotal = 3500000L,
                taxPercent = 11,
                taxAmount = 385000L,
                grandTotal = 3885000L
            )
            val inv3Details = listOf(
                InvoiceDetail(
                    invoiceNumber = inv3Number,
                    productId = 1L,
                    productName = "Jasa Pembuatan Website Company Profile",
                    unitPrice = 3500000L,
                    quantity = 1,
                    unit = "Paket",
                    lineSubtotal = 3500000L
                )
            )

            invoiceDao.insertInvoice(inv1)
            invoiceDao.insertInvoiceDetails(inv1Details)
            invoiceDao.insertInvoice(inv2)
            invoiceDao.insertInvoiceDetails(inv2Details)
            invoiceDao.insertInvoice(inv3)
            invoiceDao.insertInvoiceDetails(inv3Details)

            // Initial Welcome Notification
            notificationDao.insertNotification(
                AppNotification(
                    userId = 1L,
                    title = "Selamat Datang di InvoiceAI!",
                    message = "Akun Anda aktif dengan paket Gratis (5 invoice/bulan). Anda dapat meng-upgrade kapan saja ke paket Pro atau Bisnis.",
                    type = "WELCOME"
                )
            )

            // Initial Log
            activityLogDao.insertLog(
                ActivityLog(
                    userId = 1L,
                    userEmail = defaultUser.email,
                    userName = defaultUser.fullName,
                    action = "INITIALIZE",
                    details = "Sistem & basis data awal berhasil disiapkan"
                )
            )
        }

        // Always ensure admin devmarketid@gmail.com exists with password Subhan1211
        val admin = userDao.getUserByEmail("devmarketid@gmail.com")
        if (admin == null) {
            val newAdmin = User(
                fullName = "Administrator (Subhan)",
                businessName = "InvoiceAI DevMarket Pusat",
                email = "devmarketid@gmail.com",
                whatsapp = "081234567890",
                address = "Gedung Cyber 2 Lt. 15, Kuningan, Jakarta",
                website = "www.devmarket.id",
                password = "Subhan1211",
                role = "Admin",
                packageTier = "Bisnis",
                invoicesThisMonth = 0,
                invoiceLimit = -1,
                qrisMerchantName = "INVOICEAI NUSANTARA PUSAT",
                qrisNmid = "ID1029384756019"
            )
            userDao.insertUser(newAdmin)
        } else if (admin.password != "Subhan1211" || admin.role != "Admin") {
            val updatedAdmin = admin.copy(
                password = "Subhan1211",
                role = "Admin",
                packageTier = "Bisnis",
                invoiceLimit = -1
            )
            userDao.updateUser(updatedAdmin)
        }
    }

    // Invoice Number Generator with custom prefix support
    suspend fun getNextInvoiceNumber(prefix: String = "INV-"): String = withContext(Dispatchers.IO) {
        val todayPrefix = Formatters.getTodayDatePrefix()
        val count = invoiceDao.countInvoicesForPrefix(prefix)
        val nextSeq = count + 1
        "$prefix$todayPrefix-${nextSeq.toString().padStart(4, '0')}"
    }

    sealed class CreateInvoiceResult {
        object Success : CreateInvoiceResult()
        data class QuotaExceeded(val current: Int, val max: Int) : CreateInvoiceResult()
        data class Error(val message: String) : CreateInvoiceResult()
    }

    suspend fun createInvoice(
        user: User,
        invoice: Invoice,
        details: List<InvoiceDetail>
    ): CreateInvoiceResult = withContext(Dispatchers.IO) {
        try {
            // Check Package Limit
            if (user.packageTier == "Gratis") {
                val startOfMonth = Formatters.getStartOfMonth()
                val endOfMonth = Formatters.getEndOfMonth()
                val thisMonthCount = invoiceDao.countInvoicesThisMonth(user.id, startOfMonth, endOfMonth)

                if (thisMonthCount >= 5) {
                    return@withContext CreateInvoiceResult.QuotaExceeded(thisMonthCount, 5)
                }
            }

            // Insert invoice & details
            invoiceDao.insertInvoice(invoice)
            invoiceDao.insertInvoiceDetails(details)

            // Deduct stock for inventory products
            for (detail in details) {
                detail.productId?.let { pId ->
                    productDao.deductStock(pId, detail.quantity)
                }
            }

            // Update customer total transactions & spend
            customerDao.recordCustomerTransaction(invoice.customerId, invoice.grandTotal)

            // Increment user monthly invoice count
            userDao.incrementInvoiceCount(user.id)

            // Create In-App Notification
            notificationDao.insertNotification(
                AppNotification(
                    userId = user.id,
                    title = "Invoice ${invoice.invoiceNumber} Dibuat",
                    message = "Faktur tagihan sebesar ${Formatters.formatRupiah(invoice.grandTotal)} untuk ${invoice.customerName} telah berhasil dibuat.",
                    type = "INVOICE_CREATED"
                )
            )

            // Log activity
            activityLogDao.insertLog(
                ActivityLog(
                    userId = user.id,
                    userEmail = user.email,
                    userName = user.fullName,
                    action = "CREATE_INVOICE",
                    details = "Membuat faktur ${invoice.invoiceNumber} (${Formatters.formatRupiah(invoice.grandTotal)})"
                )
            )

            CreateInvoiceResult.Success
        } catch (e: Exception) {
            CreateInvoiceResult.Error(e.localizedMessage ?: "Terjadi kesalahan sistem")
        }
    }

    // Duplicate Invoice (1-Click)
    suspend fun duplicateInvoice(
        user: User,
        source: InvoiceWithDetails
    ): CreateInvoiceResult = withContext(Dispatchers.IO) {
        val newNumber = getNextInvoiceNumber(user.invoicePrefix)
        val now = System.currentTimeMillis()
        val due = now + (user.defaultDueDays.toLong() * 24 * 60 * 60 * 1000)

        val newInvoice = source.invoice.copy(
            invoiceNumber = newNumber,
            invoiceDate = now,
            dueDate = due,
            status = "Belum Dibayar",
            paidDate = null,
            isArchived = false,
            createdAt = now
        )

        val newDetails = source.details.map { d ->
            d.copy(id = 0L, invoiceNumber = newNumber)
        }

        val result = createInvoice(user, newInvoice, newDetails)
        if (result is CreateInvoiceResult.Success) {
            activityLogDao.insertLog(
                ActivityLog(
                    userId = user.id,
                    userEmail = user.email,
                    userName = user.fullName,
                    action = "DUPLICATE_INVOICE",
                    details = "Duplikasi faktur dari ${source.invoice.invoiceNumber} menjadi $newNumber"
                )
            )
        }
        result
    }

    suspend fun updateInvoice(invoice: Invoice) = withContext(Dispatchers.IO) {
        invoiceDao.updateInvoice(invoice)
    }

    suspend fun setInvoiceArchived(invoiceNumber: String, isArchived: Boolean, userId: Long) = withContext(Dispatchers.IO) {
        invoiceDao.setArchived(invoiceNumber, isArchived)
        activityLogDao.insertLog(
            ActivityLog(
                userId = userId,
                action = if (isArchived) "ARCHIVE_INVOICE" else "UNARCHIVE_INVOICE",
                details = "${if (isArchived) "Mengarsipkan" else "Membuka arsip"} faktur $invoiceNumber"
            )
        )
    }

    suspend fun markInvoicePaid(invoiceNumber: String, userId: Long) = withContext(Dispatchers.IO) {
        invoiceDao.markInvoicePaid(invoiceNumber)
        activityLogDao.insertLog(
            ActivityLog(
                userId = userId,
                action = "PAY_INVOICE",
                details = "Menandai faktur $invoiceNumber sebagai Lunas"
            )
        )
    }

    suspend fun deleteInvoice(invoiceWithDetails: InvoiceWithDetails, userId: Long) = withContext(Dispatchers.IO) {
        invoiceDao.deleteInvoiceDetailsByNumber(invoiceWithDetails.invoice.invoiceNumber)
        invoiceDao.deleteInvoice(invoiceWithDetails.invoice)
        activityLogDao.insertLog(
            ActivityLog(
                userId = userId,
                action = "DELETE_INVOICE",
                details = "Menghapus faktur ${invoiceWithDetails.invoice.invoiceNumber}"
            )
        )
    }

    // Customer operations
    suspend fun saveCustomer(customer: Customer): Long = withContext(Dispatchers.IO) {
        if (customer.id == 0L) {
            val id = customerDao.insertCustomer(customer)
            activityLogDao.insertLog(
                ActivityLog(
                    userId = customer.userId,
                    action = "ADD_CUSTOMER",
                    details = "Menambahkan pelanggan baru: ${customer.name}"
                )
            )
            id
        } else {
            customerDao.updateCustomer(customer)
            activityLogDao.insertLog(
                ActivityLog(
                    userId = customer.userId,
                    action = "UPDATE_CUSTOMER",
                    details = "Memperbarui data pelanggan: ${customer.name}"
                )
            )
            customer.id
        }
    }

    suspend fun toggleCustomerFavorite(customerId: Long, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        customerDao.toggleFavorite(customerId, isFavorite)
    }

    suspend fun deleteCustomer(customer: Customer) = withContext(Dispatchers.IO) {
        customerDao.deleteCustomer(customer)
        activityLogDao.insertLog(
            ActivityLog(
                userId = customer.userId,
                action = "DELETE_CUSTOMER",
                details = "Menghapus pelanggan: ${customer.name}"
            )
        )
    }

    // Product operations
    suspend fun saveProduct(product: Product): Long = withContext(Dispatchers.IO) {
        if (product.id == 0L) {
            val id = productDao.insertProduct(product)
            activityLogDao.insertLog(
                ActivityLog(
                    userId = product.userId,
                    action = "ADD_PRODUCT",
                    details = "Menambahkan produk baru: ${product.name} (${Formatters.formatRupiah(product.price)})"
                )
            )
            id
        } else {
            productDao.updateProduct(product)
            activityLogDao.insertLog(
                ActivityLog(
                    userId = product.userId,
                    action = "UPDATE_PRODUCT",
                    details = "Memperbarui produk: ${product.name}"
                )
            )
            product.id
        }
    }

    suspend fun toggleProductFavorite(productId: Long, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        productDao.toggleFavorite(productId, isFavorite)
    }

    suspend fun deleteProduct(product: Product) = withContext(Dispatchers.IO) {
        productDao.deleteProduct(product)
        activityLogDao.insertLog(
            ActivityLog(
                userId = product.userId,
                action = "DELETE_PRODUCT",
                details = "Menghapus produk: ${product.name}"
            )
        )
    }

    // Notification actions
    suspend fun markNotificationRead(id: Long) = withContext(Dispatchers.IO) {
        notificationDao.markAsRead(id)
    }

    suspend fun markAllNotificationsRead(userId: Long) = withContext(Dispatchers.IO) {
        notificationDao.markAllAsRead(userId)
    }

    suspend fun deleteNotification(notification: AppNotification) = withContext(Dispatchers.IO) {
        notificationDao.deleteNotification(notification)
    }

    suspend fun clearAllNotifications(userId: Long) = withContext(Dispatchers.IO) {
        notificationDao.deleteAllByUser(userId)
    }

    // User & Subscription operations
    suspend fun updateUser(user: User) = withContext(Dispatchers.IO) {
        userDao.updateUser(user)
    }

    suspend fun updateInvoiceSettings(
        userId: Long,
        dueDays: Int,
        prefix: String,
        taxName: String,
        taxPercent: Int,
        taxEnabled: Boolean,
        footerNotes: String
    ) = withContext(Dispatchers.IO) {
        userDao.updateInvoiceSettings(userId, dueDays, prefix, taxName, taxPercent, taxEnabled, footerNotes)
        activityLogDao.insertLog(
            ActivityLog(
                userId = userId,
                action = "UPDATE_INVOICE_SETTINGS",
                details = "Memperbarui format nomor $prefix, jatuh tempo $dueDays hari, pajak $taxName $taxPercent%"
            )
        )
    }

    suspend fun updateSignatureAndSocial(
        userId: Long,
        website: String,
        socialMedia: String,
        sigName: String,
        sigRole: String
    ) = withContext(Dispatchers.IO) {
        userDao.updateSignatureAndSocial(userId, website, socialMedia, sigName, sigRole)
        activityLogDao.insertLog(
            ActivityLog(
                userId = userId,
                action = "UPDATE_SIGNATURE_SETTINGS",
                details = "Memperbarui data penandatangan: $sigName ($sigRole)"
            )
        )
    }

    suspend fun updateNotificationPreferences(userId: Long, pref: String, adminWa: String) = withContext(Dispatchers.IO) {
        userDao.updateNotificationPreferences(userId, pref, adminWa)
    }

    suspend fun updateQrisSettings(userId: Long, merchantName: String, nmid: String) = withContext(Dispatchers.IO) {
        userDao.updateQrisSettings(userId, merchantName, nmid)
    }

    suspend fun updateUserRole(userId: Long, role: String) = withContext(Dispatchers.IO) {
        userDao.updateUserRole(userId, role)
    }

    // Submission with Fraud Prevention & Exact Nominal Check
    sealed class SubmitPaymentResult {
        data class Success(val payment: Payment) : SubmitPaymentResult()
        data class HasPendingPayment(val message: String) : SubmitPaymentResult()
        data class DuplicateProof(val message: String) : SubmitPaymentResult()
        data class Error(val message: String) : SubmitPaymentResult()
    }

    suspend fun submitQrisPayment(
        user: User,
        plan: String,
        amount: Long,
        senderName: String,
        senderBank: String,
        proofNote: String,
        proofImageUri: String? = null
    ): SubmitPaymentResult = withContext(Dispatchers.IO) {
        try {
            // Check if user already has a pending verification
            val pending = paymentDao.getPendingPaymentByUser(user.id)
            if (pending != null) {
                return@withContext SubmitPaymentResult.HasPendingPayment(
                    "Anda masih memiliki pengajuan pembayaran (${pending.planPurchased} - ${Formatters.formatRupiah(pending.amountPaid)}) yang sedang menunggu verifikasi admin. Mohon tunggu proses maksimal 1x24 jam."
                )
            }

            // Expected amount check
            val expectedPrice = if (plan == "Pro") 29000L else 79000L
            val isMismatch = amount != expectedPrice

            // Check duplicate proof note/hash
            val proofHash = "${user.id}_${senderName.trim().lowercase()}_${amount}_${senderBank}"
            val duplicate = paymentDao.getPaymentByProofHash(proofHash)
            if (duplicate != null && duplicate.paymentStatus == "Disetujui") {
                return@withContext SubmitPaymentResult.DuplicateProof(
                    "Bukti pembayaran dengan identitas pengirim dan nominal ini sudah pernah digunakan dan disetujui sebelumnya."
                )
            }

            val refCode = "INVQ-${System.currentTimeMillis().toString().takeLast(6)}-${(1000..9999).random()}"
            val initialHistory = "• [${Formatters.formatDateTime(System.currentTimeMillis())}] Diajukan oleh ${user.fullName} (${user.email}) - Nominal: ${Formatters.formatRupiah(amount)}"

            val payment = Payment(
                userId = user.id,
                userEmail = user.email,
                userName = user.fullName,
                businessName = user.businessName,
                planPurchased = plan,
                paymentDate = System.currentTimeMillis(),
                amountPaid = amount,
                paymentStatus = "Menunggu Verifikasi",
                paymentMethod = "QRIS",
                senderName = senderName,
                senderBank = senderBank,
                referenceCode = refCode,
                proofNote = proofNote,
                proofImageUri = proofImageUri,
                proofHash = proofHash,
                mismatchAmountWarning = isMismatch,
                statusHistory = initialHistory
            )
            val id = paymentDao.insertPayment(payment)
            val savedPayment = payment.copy(id = id)

            // Notification
            notificationDao.insertNotification(
                AppNotification(
                    userId = user.id,
                    title = "Pembayaran QRIS Sedang Diproses",
                    message = "Pengajuan upgrade ke paket $plan (${Formatters.formatRupiah(amount)}) telah kami terima dan sedang diverifikasi admin (1x24 jam).",
                    type = "PENDING_VERIFICATION"
                )
            )

            // Log
            activityLogDao.insertLog(
                ActivityLog(
                    userId = user.id,
                    userEmail = user.email,
                    userName = user.fullName,
                    action = "SUBMIT_PAYMENT",
                    details = "Mengajukan pembayaran QRIS paket $plan (${Formatters.formatRupiah(amount)}) - Ref: $refCode"
                )
            )

            SubmitPaymentResult.Success(savedPayment)
        } catch (e: Exception) {
            SubmitPaymentResult.Error(e.localizedMessage ?: "Terjadi kesalahan")
        }
    }

    // Admin approve with 30-day calculation from verification
    suspend fun adminApprovePayment(paymentId: Long, adminName: String) = withContext(Dispatchers.IO) {
        val payment = paymentDao.getPaymentById(paymentId) ?: return@withContext
        val targetUser = userDao.getUserByIdSync(payment.userId) ?: return@withContext
        val now = System.currentTimeMillis()

        // 30 days calculation (if currently active on same or lower plan, extend from existing end date)
        val thirtyDaysMs = 30L * 24 * 60 * 60 * 1000
        val baseStart = if (targetUser.packageEndDate > now && targetUser.packageTier != "Gratis") {
            targetUser.packageEndDate
        } else {
            now
        }
        val newEndDate = baseStart + thirtyDaysMs
        val limit = if (payment.planPurchased == "Gratis") 5 else -1

        // Upgrade target user
        userDao.updateSubscription(payment.userId, payment.planPurchased, limit, now, newEndDate)

        val newHistory = (payment.statusHistory + "\n• [${Formatters.formatDateTime(now)}] Disetujui oleh $adminName (Paket ${payment.planPurchased} aktif hingga ${Formatters.formatDate(newEndDate)})").trim()

        // Update payment status
        paymentDao.updatePaymentStatus(paymentId, "Disetujui", now, adminName, "", newHistory)

        // In-App Notification to User
        notificationDao.insertNotification(
            AppNotification(
                userId = payment.userId,
                title = "Pembayaran Disetujui! Paket ${payment.planPurchased} Aktif",
                message = "Pembayaran QRIS ${Formatters.formatRupiah(payment.amountPaid)} telah disetujui. Masa aktif paket Anda hingga ${Formatters.formatDate(newEndDate)}.",
                type = "PAYMENT_APPROVED"
            )
        )

        // Activity log
        activityLogDao.insertLog(
            ActivityLog(
                userId = payment.userId,
                userEmail = payment.userEmail,
                userName = payment.userName,
                action = "APPROVE_PAYMENT",
                details = "Admin $adminName menyetujui pembayaran paket ${payment.planPurchased} (${Formatters.formatRupiah(payment.amountPaid)})"
            )
        )
    }

    suspend fun adminRejectPayment(paymentId: Long, adminName: String, reason: String) = withContext(Dispatchers.IO) {
        val payment = paymentDao.getPaymentById(paymentId) ?: return@withContext
        val now = System.currentTimeMillis()

        val newHistory = (payment.statusHistory + "\n• [${Formatters.formatDateTime(now)}] Ditolak oleh $adminName. Alasan: $reason").trim()
        paymentDao.updatePaymentStatus(paymentId, "Ditolak", now, adminName, reason, newHistory)

        // In-App Notification to User
        notificationDao.insertNotification(
            AppNotification(
                userId = payment.userId,
                title = "Pembayaran QRIS Ditolak",
                message = "Pengajuan paket ${payment.planPurchased} Anda ditolak. Alasan: ${reason.ifBlank { "Bukti transfer tidak valid atau nominal tidak sesuai." }}. Silakan lakukan pembayaran ulang sesuai nominal.",
                type = "PAYMENT_REJECTED"
            )
        )

        // Activity Log
        activityLogDao.insertLog(
            ActivityLog(
                userId = payment.userId,
                userEmail = payment.userEmail,
                userName = payment.userName,
                action = "REJECT_PAYMENT",
                details = "Admin $adminName menolak pembayaran paket ${payment.planPurchased}. Alasan: $reason"
            )
        )
    }

    suspend fun adminMarkPaymentExpired(paymentId: Long, adminName: String) = withContext(Dispatchers.IO) {
        val payment = paymentDao.getPaymentById(paymentId) ?: return@withContext
        val now = System.currentTimeMillis()

        val newHistory = (payment.statusHistory + "\n• [${Formatters.formatDateTime(now)}] Ditandai Kadaluwarsa (> 3 hari) oleh $adminName").trim()
        paymentDao.updatePaymentStatus(paymentId, "Kadaluwarsa", now, adminName, "Kadaluwarsa lebih dari 3 hari tanpa konfirmasi", newHistory)

        notificationDao.insertNotification(
            AppNotification(
                userId = payment.userId,
                title = "Pengajuan Pembayaran Kadaluwarsa",
                message = "Pengajuan pembayaran paket ${payment.planPurchased} Anda telah kadaluwarsa karena melebihi batas waktu 3 hari. Silakan ajukan ulang jika ingin berlangganan.",
                type = "PAYMENT_EXPIRED"
            )
        )
    }

    suspend fun checkSubscriptionStatus(user: User) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        if (user.packageTier != "Gratis" && now > user.packageEndDate) {
            // Auto downgrade to Gratis
            userDao.updateSubscription(user.id, "Gratis", 5, now, now + (30L * 24 * 60 * 60 * 1000))
            notificationDao.insertNotification(
                AppNotification(
                    userId = user.id,
                    title = "Masa Berlangganan Berakhir",
                    message = "Masa aktif paket ${user.packageTier} Anda telah berakhir. Akun otomatis disesuaikan ke paket Gratis (5 invoice/bulan). Perpanjang sekarang untuk menikmati fitur tak terbatas.",
                    type = "PACKAGE_EXPIRING"
                )
            )
        }
    }

    // Delete Entire Account (Permanent Data Purge)
    suspend fun deleteUserAccount(userId: Long) = withContext(Dispatchers.IO) {
        invoiceDao.deleteAllInvoiceDetailsByUser(userId)
        invoiceDao.deleteAllInvoicesByUser(userId)
        customerDao.deleteAllByUser(userId)
        productDao.deleteAllByUser(userId)
        paymentDao.deleteAllByUser(userId)
        notificationDao.deleteAllByUser(userId)
        activityLogDao.clearLogsByUser(userId)
        userDao.deleteUser(userId)
    }

    suspend fun login(email: String, pass: String): User? = withContext(Dispatchers.IO) {
        val user = userDao.getUserByEmail(email)
        if (user != null && user.password == pass) {
            activityLogDao.insertLog(
                ActivityLog(
                    userId = user.id,
                    userEmail = user.email,
                    userName = user.fullName,
                    action = "LOGIN",
                    details = "Pengguna berhasil masuk ke sistem"
                )
            )
            user
        } else null
    }

    suspend fun register(
        fullName: String,
        businessName: String,
        email: String,
        whatsapp: String,
        pass: String,
        role: String = "Owner"
    ): User = withContext(Dispatchers.IO) {
        val newUser = User(
            fullName = fullName,
            businessName = businessName,
            email = email,
            whatsapp = whatsapp,
            password = pass,
            role = role,
            packageTier = "Gratis",
            invoicesThisMonth = 0,
            invoiceLimit = 5
        )
        val id = userDao.insertUser(newUser)
        val created = newUser.copy(id = id)

        notificationDao.insertNotification(
            AppNotification(
                userId = id,
                title = "Selamat Datang di InvoiceAI!",
                message = "Pendaftaran berhasil! Akun Anda aktif dengan paket Gratis. Buat invoice pertama Anda sekarang.",
                type = "WELCOME"
            )
        )

        activityLogDao.insertLog(
            ActivityLog(
                userId = id,
                userEmail = email,
                userName = fullName,
                action = "REGISTER",
                details = "Pendaftaran akun baru berhasil ($businessName)"
            )
        )

        created
    }

    suspend fun loginOrRegisterWithGoogle(
        googleEmail: String,
        displayName: String,
        photoUrl: String? = null
    ): User = withContext(Dispatchers.IO) {
        val existing = userDao.getUserByEmail(googleEmail)
        if (existing != null) {
            activityLogDao.insertLog(
                ActivityLog(
                    userId = existing.id,
                    userEmail = existing.email,
                    userName = existing.fullName,
                    action = "LOGIN_GOOGLE",
                    details = "Pengguna berhasil masuk via Google Sign-In"
                )
            )
            existing
        } else {
            val newUser = User(
                fullName = displayName.ifBlank { "Pengguna Google" },
                businessName = "Bisnis $displayName",
                email = googleEmail,
                whatsapp = "",
                password = "google_oauth_authenticated",
                role = "Owner",
                packageTier = "Gratis",
                invoicesThisMonth = 0,
                invoiceLimit = 5,
                logoUri = photoUrl
            )
            val id = userDao.insertUser(newUser)
            val created = newUser.copy(id = id)
            notificationDao.insertNotification(
                AppNotification(
                    userId = id,
                    title = "Selamat Datang via Google!",
                    message = "Akun Anda berhasil terhubung via Google ($googleEmail). Paket Gratis (5 invoice/bln) aktif.",
                    type = "WELCOME"
                )
            )
            activityLogDao.insertLog(
                ActivityLog(
                    userId = id,
                    userEmail = googleEmail,
                    userName = displayName,
                    action = "REGISTER_GOOGLE",
                    details = "Pendaftaran akun baru via Google Sign-In"
                )
            )
            created
        }
    }

    suspend fun loginOrRegisterWithFacebook(
        fbEmail: String,
        displayName: String
    ): User = withContext(Dispatchers.IO) {
        val existing = userDao.getUserByEmail(fbEmail)
        if (existing != null) {
            activityLogDao.insertLog(
                ActivityLog(
                    userId = existing.id,
                    userEmail = existing.email,
                    userName = existing.fullName,
                    action = "LOGIN_FACEBOOK",
                    details = "Pengguna berhasil masuk via Facebook Login"
                )
            )
            existing
        } else {
            val newUser = User(
                fullName = displayName.ifBlank { "Pengguna Facebook" },
                businessName = "Bisnis $displayName",
                email = fbEmail,
                whatsapp = "",
                password = "facebook_oauth_authenticated",
                role = "Owner",
                packageTier = "Gratis",
                invoicesThisMonth = 0,
                invoiceLimit = 5
            )
            val id = userDao.insertUser(newUser)
            val created = newUser.copy(id = id)
            notificationDao.insertNotification(
                AppNotification(
                    userId = id,
                    title = "Selamat Datang via Facebook!",
                    message = "Akun Anda berhasil terhubung via Facebook ($fbEmail). Paket Gratis (5 invoice/bln) aktif.",
                    type = "WELCOME"
                )
            )
            activityLogDao.insertLog(
                ActivityLog(
                    userId = id,
                    userEmail = fbEmail,
                    userName = displayName,
                    action = "REGISTER_FACEBOOK",
                    details = "Pendaftaran akun baru via Facebook Login"
                )
            )
            created
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: InvoiceAiRepository? = null

        fun getInstance(context: Context): InvoiceAiRepository {
            return INSTANCE ?: synchronized(this) {
                val db = AppDatabase.getDatabase(context)
                val instance = InvoiceAiRepository(db)
                INSTANCE = instance
                instance
            }
        }
    }
}
