package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.ui.components.AppNavigationLayout
import com.example.ui.components.NotificationDialog
import com.example.ui.navigation.AuthScreen
import com.example.ui.navigation.NavItem
import com.example.ui.screens.auth.ForgotPasswordScreen
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.auth.RegisterScreen
import com.example.ui.screens.customer.CustomerScreen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.invoice.CreateInvoiceScreen
import com.example.ui.screens.invoice.InvoiceDetailDialog
import com.example.ui.screens.invoice.InvoiceListScreen
import com.example.ui.screens.product.ProductScreen
import com.example.ui.screens.report.ReportScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.subscription.SubscriptionScreen
import com.example.ui.theme.InvoiceAITheme
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InvoiceAITheme {
                val context = LocalContext.current

                LaunchedEffect(Unit) {
                    viewModel.toastMessage.collect { msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                }

                InvoiceAiApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun InvoiceAiApp(viewModel: MainViewModel) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val authScreen by viewModel.authScreen.collectAsState()
    val currentNav by viewModel.currentNav.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val rawInvoices by viewModel.rawInvoices.collectAsState()
    val filteredInvoices by viewModel.filteredInvoices.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val products by viewModel.products.collectAsState()
    val payments by viewModel.payments.collectAsState()
    val allPaymentsForAdmin by viewModel.allPaymentsForAdmin.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val isCloudSyncing by viewModel.isCloudSyncing.collectAsState()
    val aiInsight by viewModel.aiInsight.collectAsState()

    val invoiceFilter by viewModel.invoiceFilter.collectAsState()
    val invoiceArchiveFilter by viewModel.invoiceArchiveFilter.collectAsState()
    val invoiceSortOrder by viewModel.invoiceSortOrder.collectAsState()
    val invoiceQuery by viewModel.invoiceSearchQuery.collectAsState()
    val customerQuery by viewModel.customerSearchQuery.collectAsState()
    val productQuery by viewModel.productSearchQuery.collectAsState()

    val notifications by viewModel.notifications.collectAsState()
    val unreadNotificationCount by viewModel.unreadNotificationCount.collectAsState()
    val showNotificationDialog by viewModel.showNotificationDialog.collectAsState()

    val selectedInvoice by viewModel.selectedInvoice.collectAsState()

    var isCreatingInvoice by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize()) {
        if (!isLoggedIn) {
            when (authScreen) {
                is AuthScreen.Login -> {
                    LoginScreen(
                        onLoginSuccess = { viewModel.setNav(NavItem.DASHBOARD) },
                        onNavigateToRegister = { viewModel.setAuthScreen(AuthScreen.Register) },
                        onNavigateToForgotPassword = { viewModel.setAuthScreen(AuthScreen.ForgotPassword) },
                        onLoginAction = { email, pass, onRes ->
                            viewModel.login(email, pass, onRes)
                        },
                        onGoogleLoginAction = { email, name, onRes ->
                            viewModel.loginWithGoogle(email, name, onRes)
                        },
                        onFacebookLoginAction = { email, name, onRes ->
                            viewModel.loginWithFacebook(email, name, onRes)
                        }
                    )
                }
                is AuthScreen.Register -> {
                    RegisterScreen(
                        onRegisterSuccess = { viewModel.setNav(NavItem.DASHBOARD) },
                        onNavigateToLogin = { viewModel.setAuthScreen(AuthScreen.Login) },
                        onRegisterAction = { fullName, businessName, email, whatsapp, pass, onRes ->
                            viewModel.register(fullName, businessName, email, whatsapp, pass, onRes)
                        }
                    )
                }
                is AuthScreen.ForgotPassword -> {
                    ForgotPasswordScreen(
                        onNavigateBackToLogin = { viewModel.setAuthScreen(AuthScreen.Login) },
                        onSendResetLink = { email, onRes ->
                            viewModel.forgotPassword(email, onRes)
                        }
                    )
                }
            }
        } else if (isCreatingInvoice) {
            CreateInvoiceScreen(
                user = currentUser,
                customers = customers,
                products = products,
                onBackClick = { isCreatingInvoice = false },
                onSaveSuccess = { invWithDetails ->
                    viewModel.createInvoice(
                        customer = customers.firstOrNull { it.id == invWithDetails.invoice.customerId }
                            ?: com.example.data.model.Customer(id = invWithDetails.invoice.customerId, name = invWithDetails.invoice.customerName, phone = invWithDetails.invoice.customerPhone),
                        invoiceDate = invWithDetails.invoice.invoiceDate,
                        dueDate = invWithDetails.invoice.dueDate,
                        items = invWithDetails.details.map { d ->
                            val p = products.firstOrNull { it.id == d.productId } ?: com.example.data.model.Product(id = d.productId ?: 0L, name = d.productName, price = d.unitPrice, unit = d.unit)
                            Triple(p, d.quantity, d.unitPrice)
                        },
                        taxPercent = invWithDetails.invoice.taxPercent,
                        notes = invWithDetails.invoice.notes
                    ) { success, _ ->
                        if (success) {
                            isCreatingInvoice = false
                            viewModel.setNav(NavItem.INVOICES)
                        }
                    }
                },
                onNavigateToSubscription = {
                    isCreatingInvoice = false
                    viewModel.setNav(NavItem.SUBSCRIPTION)
                },
                onSaveCustomerQuick = { newCust ->
                    viewModel.saveCustomer(newCust)
                }
            )
        } else {
            AppNavigationLayout(
                user = currentUser,
                currentNav = currentNav,
                unreadNotificationCount = unreadNotificationCount,
                onNavSelected = { nav -> viewModel.setNav(nav) },
                onLogoutClick = { viewModel.logout() },
                onNotificationClick = { viewModel.setShowNotificationDialog(true) }
            ) {
                when (currentNav) {
                    NavItem.DASHBOARD -> {
                        DashboardScreen(
                            user = currentUser,
                            insight = aiInsight,
                            invoices = rawInvoices,
                            customers = customers,
                            onCreateInvoiceClick = { isCreatingInvoice = true },
                            onViewAllInvoicesClick = { viewModel.setNav(NavItem.INVOICES) },
                            onInvoiceClick = { inv -> viewModel.selectInvoiceForDetail(inv) },
                            onUpgradeClick = { viewModel.setNav(NavItem.SUBSCRIPTION) }
                        )
                    }
                    NavItem.INVOICES -> {
                        InvoiceListScreen(
                            user = currentUser,
                            invoices = filteredInvoices,
                            currentFilter = invoiceFilter,
                            searchQuery = invoiceQuery,
                            archiveFilter = invoiceArchiveFilter,
                            sortOrder = invoiceSortOrder,
                            onFilterChange = { filter -> viewModel.setInvoiceFilter(filter) },
                            onArchiveFilterChange = { arch -> viewModel.setInvoiceArchiveFilter(arch) },
                            onSortOrderChange = { sort -> viewModel.setInvoiceSortOrder(sort) },
                            onSearchChange = { query -> viewModel.setInvoiceSearchQuery(query) },
                            onCreateInvoiceClick = { isCreatingInvoice = true },
                            onInvoiceClick = { inv -> viewModel.selectInvoiceForDetail(inv) },
                            onMarkPaidClick = { invNum -> viewModel.markInvoicePaid(invNum) },
                            onDuplicateInvoice = { inv -> viewModel.duplicateInvoice(inv) },
                            onToggleArchive = { inv -> viewModel.toggleInvoiceArchive(inv) },
                            onDeleteClick = { inv -> viewModel.deleteInvoice(inv) },
                            onNavigateToSubscription = { viewModel.setNav(NavItem.SUBSCRIPTION) }
                        )
                    }
                    NavItem.CUSTOMERS -> {
                        CustomerScreen(
                            customers = customers,
                            invoices = rawInvoices,
                            searchQuery = customerQuery,
                            onSearchChange = { query -> viewModel.setCustomerSearchQuery(query) },
                            onSaveCustomer = { cust -> viewModel.saveCustomer(cust) },
                            onDeleteCustomer = { cust -> viewModel.deleteCustomer(cust) },
                            onToggleFavorite = { cust -> viewModel.toggleCustomerFavorite(cust) },
                            onSelectCustomerForInvoice = { cust ->
                                viewModel.selectCustomerForDetail(cust)
                                isCreatingInvoice = true
                            }
                        )
                    }
                    NavItem.PRODUCTS -> {
                        ProductScreen(
                            products = products,
                            searchQuery = productQuery,
                            onSearchChange = { query -> viewModel.setProductSearchQuery(query) },
                            onSaveProduct = { prod -> viewModel.saveProduct(prod) },
                            onDeleteProduct = { prod -> viewModel.deleteProduct(prod) },
                            onToggleFavorite = { prod -> viewModel.toggleProductFavorite(prod) }
                        )
                    }
                    NavItem.REPORTS -> {
                        ReportScreen(
                            user = currentUser,
                            insight = aiInsight,
                            invoices = rawInvoices,
                            products = products,
                            customers = customers,
                            onNavigateToSubscription = { viewModel.setNav(NavItem.SUBSCRIPTION) }
                        )
                    }
                    NavItem.SUBSCRIPTION -> {
                        SubscriptionScreen(
                            user = currentUser,
                            payments = payments,
                            allPaymentsForAdmin = allPaymentsForAdmin,
                            onSubmitQrisPayment = { plan, amt, senderName, senderBank, note, onDone ->
                                viewModel.submitQrisPayment(plan, amt, senderName, senderBank, note, onDone)
                            },
                            onAdminApprove = { pay -> viewModel.adminApprovePayment(pay) },
                            onAdminReject = { pay, reason -> viewModel.adminRejectPayment(pay, reason) },
                            onCancelSubscription = { viewModel.cancelSubscription() }
                        )
                    }
                    NavItem.SETTINGS -> {
                        SettingsScreen(
                            user = currentUser,
                            allUsers = allUsers,
                            isCloudSyncing = isCloudSyncing,
                            onUpdateProfile = { name, bName, phone, addr, logo ->
                                viewModel.updateProfile(name, bName, phone, addr, logoUri = logo)
                            },
                            onUpdateInvoiceSettings = { days, prefix, taxName, taxPercent, taxEnabled, footerNotes ->
                                viewModel.updateInvoiceSettings(days, prefix, taxName, taxPercent, taxEnabled, footerNotes)
                            },
                            onUpdateSignatureSettings = { sigName, sigRole ->
                                viewModel.updateSignatureSettings(sigName, sigRole)
                            },
                            onUpdateNotificationPreferences = { pref, adminWa ->
                                viewModel.updateNotificationPreferences(pref, adminWa)
                            },
                            onChangePassword = { oldP, newP, onRes ->
                                viewModel.changePassword(oldP, newP, onRes)
                            },
                            onUpdateQrisSettings = { mName, nmid ->
                                viewModel.updateQrisMerchantSettings(mName, nmid)
                            },
                            onUpdateRole = { r ->
                                viewModel.updateUserRole(r)
                            },
                            onSwitchUser = { u ->
                                viewModel.switchActiveUser(u)
                            },
                            onTriggerCloudSync = {
                                viewModel.triggerCloudSync()
                            },
                            onDeleteAccount = { onDone ->
                                viewModel.deleteAccount(onDone)
                            }
                        )
                    }
                }
            }

            // Detail Dialog
            selectedInvoice?.let { invDetail ->
                InvoiceDetailDialog(
                    user = currentUser,
                    invoiceWithDetails = invDetail,
                    onDismiss = { viewModel.selectInvoiceForDetail(null) },
                    onMarkPaid = { invNum -> viewModel.markInvoicePaid(invNum) },
                    onDelete = { inv -> viewModel.deleteInvoice(inv) }
                )
            }

            // Notification Center Modal Dialog
            if (showNotificationDialog) {
                NotificationDialog(
                    notifications = notifications,
                    onDismiss = { viewModel.setShowNotificationDialog(false) },
                    onMarkAsRead = { notifId -> viewModel.markNotificationRead(notifId) },
                    onMarkAllRead = { viewModel.markAllNotificationsRead() },
                    onDeleteNotification = { notif -> viewModel.deleteNotification(notif) },
                    onClearAll = { viewModel.clearAllNotifications() }
                )
            }
        }
    }
}
