package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class NavItem(
    val title: String,
    val icon: ImageVector,
    val description: String
) {
    DASHBOARD("Beranda", Icons.Default.Dashboard, "Ringkasan Bisnis & AI"),
    INVOICES("Invoice", Icons.AutoMirrored.Filled.ReceiptLong, "Daftar & Pembuatan Faktur"),
    CUSTOMERS("Pelanggan", Icons.Default.People, "Data & Riwayat Pelanggan"),
    PRODUCTS("Produk & Jasa", Icons.Default.Inventory, "Katalog & Manajemen Stok"),
    REPORTS("Laporan", Icons.Default.Analytics, "Grafik & Analitik AI"),
    SUBSCRIPTION("Langganan", Icons.Default.CardMembership, "Paket & Pembayaran Stripe"),
    SETTINGS("Pengaturan", Icons.Default.Settings, "Profil Bisnis & Akun")
}

sealed class AuthScreen {
    object Login : AuthScreen()
    object Register : AuthScreen()
    object ForgotPassword : AuthScreen()
}
