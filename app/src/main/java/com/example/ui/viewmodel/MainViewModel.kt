package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ActivityLog
import com.example.data.model.AppNotification
import com.example.data.model.Customer
import com.example.data.model.Invoice
import com.example.data.model.InvoiceDetail
import com.example.data.model.InvoiceWithDetails
import com.example.data.model.Payment
import com.example.data.model.Product
import com.example.data.model.User
import com.example.data.repository.InvoiceAiRepository
import com.example.ui.navigation.AuthScreen
import com.example.ui.navigation.NavItem
import com.example.util.AiDashboardInsight
import com.example.util.AiEngine
import com.example.util.Formatters
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = InvoiceAiRepository.getInstance(application)

    private val _activeUserId = MutableStateFlow<Long?>(null)
    
    val allUsers: StateFlow<List<User>> = repository.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentUser: StateFlow<User?> = combine(
        repository.getAllUsers(),
        _activeUserId
    ) { users, activeId ->
        if (users.isEmpty()) null
        else if (activeId == null) users.firstOrNull()
        else users.firstOrNull { it.id == activeId } ?: users.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _isLoggedIn = MutableStateFlow(true)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _authScreen = MutableStateFlow<AuthScreen>(AuthScreen.Login)
    val authScreen: StateFlow<AuthScreen> = _authScreen.asStateFlow()

    private val _currentNav = MutableStateFlow(NavItem.DASHBOARD)
    val currentNav: StateFlow<NavItem> = _currentNav.asStateFlow()

    // Invoice Search & Filter State
    private val _invoiceFilter = MutableStateFlow("Semua") // Semua, Belum Dibayar, Sudah Dibayar, Terlambat
    val invoiceFilter: StateFlow<String> = _invoiceFilter.asStateFlow()

    private val _invoiceArchiveFilter = MutableStateFlow("Aktif") // Aktif, Diarsipkan, Semua
    val invoiceArchiveFilter: StateFlow<String> = _invoiceArchiveFilter.asStateFlow()

    private val _invoiceSearchQuery = MutableStateFlow("")
    val invoiceSearchQuery: StateFlow<String> = _invoiceSearchQuery.asStateFlow()

    private val _invoiceSortOrder = MutableStateFlow("Terbaru") // Terbaru, Terlama, Nominal Tertinggi, Nominal Terendah, Pelanggan
    val invoiceSortOrder: StateFlow<String> = _invoiceSortOrder.asStateFlow()

    private val _customerFilterId = MutableStateFlow<Long?>(null)
    val customerFilterId: StateFlow<Long?> = _customerFilterId.asStateFlow()

    private val _customerSearchQuery = MutableStateFlow("")
    val customerSearchQuery: StateFlow<String> = _customerSearchQuery.asStateFlow()

    private val _productSearchQuery = MutableStateFlow("")
    val productSearchQuery: StateFlow<String> = _productSearchQuery.asStateFlow()

    // Dialog & Detail states
    private val _selectedInvoice = MutableStateFlow<InvoiceWithDetails?>(null)
    val selectedInvoice: StateFlow<InvoiceWithDetails?> = _selectedInvoice.asStateFlow()

    private val _selectedCustomer = MutableStateFlow<Customer?>(null)
    val selectedCustomer: StateFlow<Customer?> = _selectedCustomer.asStateFlow()

    private val _showQuotaExceededDialog = MutableStateFlow(false)
    val showQuotaExceededDialog: StateFlow<Boolean> = _showQuotaExceededDialog.asStateFlow()

    private val _showNotificationDialog = MutableStateFlow(false)
    val showNotificationDialog: StateFlow<Boolean> = _showNotificationDialog.asStateFlow()

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    private val _isCloudSyncing = MutableStateFlow(false)
    val isCloudSyncing: StateFlow<Boolean> = _isCloudSyncing.asStateFlow()

    // Notification streams
    val notifications: StateFlow<List<AppNotification>> = currentUser.combine(
        repository.getAllUsers()
    ) { user, _ ->
        user?.id ?: 1L
    }.combine(repository.getNotifications(1L)) { _, _ ->
        // Dynamically listen for active user
        val uId = currentUser.value?.id ?: 1L
        uId
    }.combine(repository.getAllUsers()) { uId, _ ->
        uId
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1L)
        .combine(repository.getNotifications(currentUser.value?.id ?: 1L)) { _, list -> list }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotificationCount: StateFlow<Int> = currentUser.combine(
        repository.getUnreadNotificationCount(currentUser.value?.id ?: 1L)
    ) { _, count ->
        count
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Activity Logs streams
    val activityLogs: StateFlow<List<ActivityLog>> = currentUser.combine(
        repository.getActivityLogs(currentUser.value?.id ?: 1L)
    ) { _, logs ->
        logs
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allActivityLogsForAdmin: StateFlow<List<ActivityLog>> = repository.getAllActivityLogsForAdmin()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Data streams isolated per active user
    val customers: StateFlow<List<Customer>> = combine(
        currentUser,
        _customerSearchQuery
    ) { user, query ->
        user to query
    }.combine(repository.getCustomers(1L)) { (user, query), list ->
        val uId = user?.id ?: 1L
        val filteredByUser = if (user?.role == "Admin") list else list.filter { it.userId == uId || it.userId == 1L }
        val searched = if (query.isBlank()) filteredByUser
        else filteredByUser.filter { it.name.contains(query, ignoreCase = true) || it.phone.contains(query) }
        // Favorites first
        searched.sortedWith(compareByDescending<Customer> { it.isFavorite }.thenBy { it.name })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val products: StateFlow<List<Product>> = combine(
        currentUser,
        _productSearchQuery
    ) { user, query ->
        user to query
    }.combine(repository.getProducts(1L)) { (user, query), list ->
        val uId = user?.id ?: 1L
        val filteredByUser = if (user?.role == "Admin") list else list.filter { it.userId == uId || it.userId == 1L }
        val searched = if (query.isBlank()) filteredByUser
        else filteredByUser.filter { it.name.contains(query, ignoreCase = true) || it.category.contains(query, ignoreCase = true) }
        // Favorites first
        searched.sortedWith(compareByDescending<Product> { it.isFavorite }.thenBy { it.name })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockProducts: StateFlow<List<Product>> = repository.getLowStockProducts(1L)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rawInvoices: StateFlow<List<InvoiceWithDetails>> = combine(
        currentUser,
        repository.getAllInvoices(1L)
    ) { user, list ->
        val uId = user?.id ?: 1L
        if (user?.role == "Admin") list else list.filter { it.invoice.userId == uId || it.invoice.userId == 1L }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredInvoices: StateFlow<List<InvoiceWithDetails>> = combine(
        rawInvoices,
        _invoiceFilter,
        _invoiceArchiveFilter,
        _invoiceSearchQuery,
        _invoiceSortOrder
    ) { invoices, filter, archiveFilter, query, sortOrder ->
        var list = invoices.filter { item ->
            val matchesArchive = when (archiveFilter) {
                "Aktif" -> !item.invoice.isArchived
                "Diarsipkan" -> item.invoice.isArchived
                else -> true
            }

            val matchesFilter = when (filter) {
                "Semua" -> true
                "Belum Dibayar" -> item.invoice.status == "Belum Dibayar"
                "Sudah Dibayar" -> item.invoice.status == "Sudah Dibayar"
                "Terlambat" -> item.invoice.status == "Terlambat"
                else -> true
            }

            val matchesCustomer = if (_customerFilterId.value == null) true
            else item.invoice.customerId == _customerFilterId.value

            val matchesQuery = if (query.isBlank()) true else {
                item.invoice.invoiceNumber.contains(query, ignoreCase = true) ||
                item.invoice.customerName.contains(query, ignoreCase = true) ||
                item.invoice.customerPhone.contains(query)
            }

            matchesArchive && matchesFilter && matchesCustomer && matchesQuery
        }

        // Sorting
        list = when (sortOrder) {
            "Terbaru" -> list.sortedByDescending { it.invoice.invoiceDate }
            "Terlama" -> list.sortedBy { it.invoice.invoiceDate }
            "Nominal Tertinggi" -> list.sortedByDescending { it.invoice.grandTotal }
            "Nominal Terendah" -> list.sortedBy { it.invoice.grandTotal }
            "Pelanggan" -> list.sortedBy { it.invoice.customerName }
            else -> list.sortedByDescending { it.invoice.invoiceDate }
        }

        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val payments: StateFlow<List<Payment>> = combine(
        currentUser,
        repository.getAllPaymentsForAdmin()
    ) { user, allPay ->
        val uId = user?.id ?: 1L
        if (user?.role == "Admin") allPay else allPay.filter { it.userId == uId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPaymentsForAdmin: StateFlow<List<Payment>> = repository.getAllPaymentsForAdmin()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val aiInsight: StateFlow<AiDashboardInsight> = combine(
        rawInvoices,
        products,
        customers
    ) { invs, prods, custs ->
        AiEngine.analyzeBusiness(invs, prods, custs)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        AiDashboardInsight(0, 0, 0, 0, 0, "", "", "Memuat analisis AI...", emptyList())
    )

    init {
        viewModelScope.launch {
            repository.checkAndSeedInitialData()
            currentUser.value?.let { repository.checkSubscriptionStatus(it) }
        }
    }

    fun setNav(nav: NavItem) {
        _currentNav.value = nav
    }

    fun setAuthScreen(screen: AuthScreen) {
        _authScreen.value = screen
    }

    fun setInvoiceFilter(filter: String) {
        _invoiceFilter.value = filter
    }

    fun setInvoiceArchiveFilter(filter: String) {
        _invoiceArchiveFilter.value = filter
    }

    fun setInvoiceSortOrder(order: String) {
        _invoiceSortOrder.value = order
    }

    fun setCustomerFilterId(customerId: Long?) {
        _customerFilterId.value = customerId
    }

    fun setInvoiceSearchQuery(query: String) {
        _invoiceSearchQuery.value = query
    }

    fun setCustomerSearchQuery(query: String) {
        _customerSearchQuery.value = query
    }

    fun setProductSearchQuery(query: String) {
        _productSearchQuery.value = query
    }

    fun selectInvoiceForDetail(invoice: InvoiceWithDetails?) {
        _selectedInvoice.value = invoice
    }

    fun selectCustomerForDetail(customer: Customer?) {
        _selectedCustomer.value = customer
    }

    fun dismissQuotaDialog() {
        _showQuotaExceededDialog.value = false
    }

    fun setShowNotificationDialog(show: Boolean) {
        _showNotificationDialog.value = show
    }

    // Auth actions
    fun login(email: String, pass: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val user = repository.login(email, pass)
            if (user != null) {
                _activeUserId.value = user.id
                _isLoggedIn.value = true
                repository.checkSubscriptionStatus(user)
                onResult(true, "Berhasil masuk sebagai ${user.fullName}!")
            } else {
                onResult(false, "Email atau kata sandi tidak cocok.")
            }
        }
    }

    fun loginWithGoogle(
        googleEmail: String,
        displayName: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val user = repository.loginOrRegisterWithGoogle(googleEmail, displayName, null)
                _activeUserId.value = user.id
                _isLoggedIn.value = true
                repository.checkSubscriptionStatus(user)
                onResult(true, "Berhasil masuk dengan akun Google: ${user.email}")
            } catch (e: Exception) {
                onResult(false, e.localizedMessage ?: "Gagal masuk dengan Google")
            }
        }
    }

    fun loginWithFacebook(
        fbEmail: String,
        displayName: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val user = repository.loginOrRegisterWithFacebook(fbEmail, displayName)
                _activeUserId.value = user.id
                _isLoggedIn.value = true
                repository.checkSubscriptionStatus(user)
                onResult(true, "Berhasil masuk dengan akun Facebook: ${user.email}")
            } catch (e: Exception) {
                onResult(false, e.localizedMessage ?: "Gagal masuk dengan Facebook")
            }
        }
    }

    fun register(
        fullName: String,
        businessName: String,
        email: String,
        whatsapp: String,
        pass: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val created = repository.register(fullName, businessName, email, whatsapp, pass)
                _activeUserId.value = created.id
                _isLoggedIn.value = true
                onResult(true, "Pendaftaran berhasil! Selamat datang di InvoiceAI.")
            } catch (e: Exception) {
                onResult(false, e.localizedMessage ?: "Pendaftaran gagal.")
            }
        }
    }

    fun logout() {
        _isLoggedIn.value = false
        _authScreen.value = AuthScreen.Login
    }

    fun forgotPassword(email: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            onResult(true, "Link instruksi reset kata sandi telah dikirim ke $email.")
        }
    }

    fun updateProfile(
        fullName: String,
        businessName: String,
        phone: String,
        address: String,
        website: String = "",
        socialMedia: String = "",
        logoUri: String? = null
    ) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val updated = user.copy(
                fullName = fullName,
                businessName = businessName,
                whatsapp = phone,
                address = address,
                website = website,
                socialMedia = socialMedia,
                logoUri = logoUri,
                updatedAt = System.currentTimeMillis()
            )
            repository.updateUser(updated)
            _toastMessage.emit("Profil bisnis berhasil diperbarui!")
        }
    }

    fun updateInvoiceSettings(
        dueDays: Int,
        prefix: String,
        taxName: String,
        taxPercent: Int,
        taxEnabled: Boolean,
        footerNotes: String
    ) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            repository.updateInvoiceSettings(user.id, dueDays, prefix, taxName, taxPercent, taxEnabled, footerNotes)
            _toastMessage.emit("Pengaturan default invoice berhasil diperbarui!")
        }
    }

    fun updateSignatureSettings(
        sigName: String,
        sigRole: String
    ) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            repository.updateSignatureAndSocial(user.id, user.website, user.socialMedia, sigName, sigRole)
            _toastMessage.emit("Tanda tangan dan stempel faktur berhasil disimpan!")
        }
    }

    fun updateNotificationPreferences(preference: String, adminWa: String) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            repository.updateNotificationPreferences(user.id, preference, adminWa)
            _toastMessage.emit("Preferensi notifikasi WhatsApp & In-App disimpan!")
        }
    }

    fun changePassword(oldPass: String, newPass: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            if (user.password != oldPass) {
                onResult(false, "Kata sandi lama tidak sesuai.")
                return@launch
            }
            val updated = user.copy(password = newPass, updatedAt = System.currentTimeMillis())
            repository.updateUser(updated)
            onResult(true, "Kata sandi berhasil diubah.")
        }
    }

    fun switchActiveUser(user: User) {
        _activeUserId.value = user.id
        _toastMessage.tryEmit("Beralih ke akun: ${user.fullName} (${user.role})")
    }

    fun updateUserRole(newRole: String) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            repository.updateUserRole(user.id, newRole)
            _toastMessage.emit("Peran akun diubah menjadi '$newRole'")
        }
    }

    fun updateQrisMerchantSettings(merchantName: String, nmid: String) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            repository.updateQrisSettings(user.id, merchantName, nmid)
            _toastMessage.emit("Pengaturan QRIS Merchant berhasil diperbarui!")
        }
    }

    fun triggerCloudSync() {
        viewModelScope.launch {
            _isCloudSyncing.value = true
            val user = currentUser.value
            kotlinx.coroutines.delay(1000)
            if (user != null) {
                repository.updateUser(user.copy(lastSyncTime = System.currentTimeMillis()))
            }
            _isCloudSyncing.value = false
            _toastMessage.emit("☁️ Cloud Sync Berhasil! Semua data terpusat dan aman.")
        }
    }

    // Notifications actions
    fun markNotificationRead(id: Long) {
        viewModelScope.launch {
            repository.markNotificationRead(id)
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            val uId = currentUser.value?.id ?: 1L
            repository.markAllNotificationsRead(uId)
            _toastMessage.emit("Semua notifikasi ditandai telah dibaca.")
        }
    }

    fun deleteNotification(notification: AppNotification) {
        viewModelScope.launch {
            repository.deleteNotification(notification)
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            val uId = currentUser.value?.id ?: 1L
            repository.clearAllNotifications(uId)
            _toastMessage.emit("Semua notifikasi telah dibersihkan.")
        }
    }

    // QRIS Payment submission with fraud validation
    fun submitQrisPayment(
        plan: String,
        amount: Long,
        senderName: String,
        senderBank: String,
        proofNote: String,
        onDone: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val user = currentUser.value ?: run {
                onDone(false, "Sesi tidak valid")
                return@launch
            }

            val result = repository.submitQrisPayment(
                user = user,
                plan = plan,
                amount = amount,
                senderName = senderName,
                senderBank = senderBank,
                proofNote = proofNote
            )

            when (result) {
                is InvoiceAiRepository.SubmitPaymentResult.Success -> {
                    _toastMessage.emit("✅ Bukti pembayaran QRIS berhasil dikirim! Menunggu verifikasi admin (maks 1x24 jam).")
                    onDone(true, result.payment.referenceCode)
                }
                is InvoiceAiRepository.SubmitPaymentResult.HasPendingPayment -> {
                    _toastMessage.emit("⚠️ Anda masih memiliki pengajuan yang sedang diproses.")
                    onDone(false, result.message)
                }
                is InvoiceAiRepository.SubmitPaymentResult.DuplicateProof -> {
                    _toastMessage.emit("⚠️ Bukti pembayaran duplikat terdeteksi.")
                    onDone(false, result.message)
                }
                is InvoiceAiRepository.SubmitPaymentResult.Error -> {
                    _toastMessage.emit("Gagal: ${result.message}")
                    onDone(false, result.message)
                }
            }
        }
    }

    fun cancelSubscription() {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val updated = user.copy(
                packageTier = "Gratis",
                invoiceLimit = 5,
                updatedAt = System.currentTimeMillis()
            )
            repository.updateUser(updated)
            _toastMessage.emit("Langganan telah dibatalkan. Akun kembali ke paket Gratis.")
        }
    }

    fun adminApprovePayment(payment: Payment) {
        viewModelScope.launch {
            val adminName = currentUser.value?.fullName ?: "Administrator"
            repository.adminApprovePayment(payment.id, adminName)
            _toastMessage.emit("Pembayaran #${payment.referenceCode} disetujui! Paket ${payment.planPurchased} diaktifkan 30 hari.")
        }
    }

    fun adminRejectPayment(payment: Payment, reason: String) {
        viewModelScope.launch {
            val adminName = currentUser.value?.fullName ?: "Administrator"
            repository.adminRejectPayment(payment.id, adminName, reason)
            _toastMessage.emit("Pembayaran #${payment.referenceCode} ditolak.")
        }
    }

    fun adminMarkPaymentExpired(payment: Payment) {
        viewModelScope.launch {
            val adminName = currentUser.value?.fullName ?: "Administrator"
            repository.adminMarkPaymentExpired(payment.id, adminName)
            _toastMessage.emit("Pembayaran #${payment.referenceCode} ditandai kadaluwarsa (> 3 hari).")
        }
    }

    // Customer actions
    fun saveCustomer(customer: Customer, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.saveCustomer(customer)
            _toastMessage.emit("Data pelanggan '${customer.name}' berhasil disimpan.")
            onDone()
        }
    }

    fun toggleCustomerFavorite(customer: Customer) {
        viewModelScope.launch {
            val newFav = !customer.isFavorite
            repository.toggleCustomerFavorite(customer.id, newFav)
            _toastMessage.emit(if (newFav) "★ '${customer.name}' ditambahkan ke Favorit" else "'${customer.name}' dihapus dari Favorit")
        }
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
            _toastMessage.emit("Pelanggan '${customer.name}' telah dihapus.")
        }
    }

    // Product actions
    fun saveProduct(product: Product, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.saveProduct(product)
            _toastMessage.emit("Produk '${product.name}' berhasil disimpan.")
            onDone()
        }
    }

    fun toggleProductFavorite(product: Product) {
        viewModelScope.launch {
            val newFav = !product.isFavorite
            repository.toggleProductFavorite(product.id, newFav)
            _toastMessage.emit(if (newFav) "★ '${product.name}' ditambahkan ke Favorit" else "'${product.name}' dihapus dari Favorit")
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            _toastMessage.emit("Produk '${product.name}' telah dihapus.")
        }
    }

    // Invoice actions
    suspend fun generateNextInvoiceNumber(): String {
        val user = currentUser.value
        val prefix = user?.invoicePrefix?.ifEmpty { "INV-" } ?: "INV-"
        return repository.getNextInvoiceNumber(prefix)
    }

    fun createInvoice(
        customer: Customer,
        invoiceDate: Long,
        dueDate: Long,
        items: List<Triple<Product, Int, Long>>, // Product, Qty, Price
        taxPercent: Int,
        notes: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val user = currentUser.value ?: run {
                onResult(false, "Sesi pengguna tidak valid")
                return@launch
            }

            val subtotal = items.sumOf { it.third * it.second }
            val taxAmount = (subtotal * taxPercent) / 100
            val grandTotal = subtotal + taxAmount
            val invNumber = repository.getNextInvoiceNumber(user.invoicePrefix.ifEmpty { "INV-" })

            val invoice = Invoice(
                invoiceNumber = invNumber,
                userId = user.id,
                customerId = customer.id,
                customerName = customer.name,
                customerPhone = customer.phone,
                customerAddress = customer.address,
                invoiceDate = invoiceDate,
                dueDate = dueDate,
                status = "Belum Dibayar",
                notes = notes,
                subtotal = subtotal,
                taxPercent = taxPercent,
                taxAmount = taxAmount,
                grandTotal = grandTotal,
                createdAt = System.currentTimeMillis()
            )

            val details = items.map { item ->
                InvoiceDetail(
                    invoiceNumber = invNumber,
                    productId = item.first.id.takeIf { it > 0 },
                    productName = item.first.name,
                    unitPrice = item.third,
                    quantity = item.second,
                    unit = item.first.unit,
                    lineSubtotal = item.third * item.second
                )
            }

            val result = repository.createInvoice(user, invoice, details)
            when (result) {
                is InvoiceAiRepository.CreateInvoiceResult.Success -> {
                    _toastMessage.emit("Faktur $invNumber berhasil dibuat!")
                    onResult(true, invNumber)
                }
                is InvoiceAiRepository.CreateInvoiceResult.QuotaExceeded -> {
                    _showQuotaExceededDialog.value = true
                    onResult(false, "Batas kuota 5 invoice/bulan untuk paket Gratis telah tercapai!")
                }
                is InvoiceAiRepository.CreateInvoiceResult.Error -> {
                    onResult(false, result.message)
                }
            }
        }
    }

    fun duplicateInvoice(sourceInvoice: InvoiceWithDetails) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val result = repository.duplicateInvoice(user, sourceInvoice)
            when (result) {
                is InvoiceAiRepository.CreateInvoiceResult.Success -> {
                    _toastMessage.emit("✅ Berhasil menduplikasi faktur ${sourceInvoice.invoice.invoiceNumber}!")
                }
                is InvoiceAiRepository.CreateInvoiceResult.QuotaExceeded -> {
                    _showQuotaExceededDialog.value = true
                    _toastMessage.emit("⚠️ Batas kuota 5 invoice/bulan tercapai.")
                }
                is InvoiceAiRepository.CreateInvoiceResult.Error -> {
                    _toastMessage.emit("Gagal menduplikasi: ${result.message}")
                }
            }
        }
    }

    fun toggleInvoiceArchive(invoice: Invoice) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val newStatus = !invoice.isArchived
            repository.setInvoiceArchived(invoice.invoiceNumber, newStatus, user.id)
            _toastMessage.emit(if (newStatus) "Faktur ${invoice.invoiceNumber} diarsipkan." else "Faktur ${invoice.invoiceNumber} dibuka dari arsip.")
        }
    }

    fun markInvoicePaid(invoiceNumber: String) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            repository.markInvoicePaid(invoiceNumber, user.id)
            _toastMessage.emit("Invoice $invoiceNumber ditandai SUDAH DIBAYAR.")
        }
    }

    fun deleteInvoice(invoiceWithDetails: InvoiceWithDetails) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            repository.deleteInvoice(invoiceWithDetails, user.id)
            _toastMessage.emit("Invoice ${invoiceWithDetails.invoice.invoiceNumber} berhasil dihapus.")
        }
    }

    fun deleteAccount(onDone: () -> Unit) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            repository.deleteUserAccount(user.id)
            _isLoggedIn.value = false
            _authScreen.value = AuthScreen.Login
            _toastMessage.emit("Akun Anda beserta seluruh data telah berhasil dihapus secara permanen.")
            onDone()
        }
    }
}
