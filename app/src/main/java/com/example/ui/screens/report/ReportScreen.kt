package com.example.ui.screens.report

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Customer
import com.example.data.model.InvoiceWithDetails
import com.example.data.model.Product
import com.example.data.model.User
import com.example.ui.components.AiInsightCard
import com.example.ui.components.MonthlyBarData
import com.example.ui.components.RevenueChart
import com.example.ui.components.StatCard
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DangerRedLight
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueDark
import com.example.ui.theme.PrimaryBlueLight
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.PurpleAccentLight
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessGreenLight
import com.example.ui.theme.WarningOrange
import com.example.ui.theme.WarningOrangeLight
import com.example.util.AiDashboardInsight
import com.example.util.CsvExporter
import com.example.util.Formatters

@Composable
fun ReportScreen(
    user: User?,
    insight: AiDashboardInsight,
    invoices: List<InvoiceWithDetails>,
    products: List<Product>,
    customers: List<Customer>,
    onNavigateToSubscription: () -> Unit
) {
    val context = LocalContext.current
    var selectedTimePeriod by remember { mutableStateOf("Bulan Ini") }
    var showExportUpgradeDialog by remember { mutableStateOf(false) }

    val isFreeTier = (user?.packageTier ?: "Gratis").equals("Gratis", ignoreCase = true)

    // Calculate aggregated metrics
    val totalRevenue = invoices.filter { it.invoice.status == "Sudah Dibayar" }.sumOf { it.invoice.grandTotal }
    val totalTaxCollected = invoices.filter { it.invoice.status == "Sudah Dibayar" }.sumOf { it.invoice.taxAmount }
    val totalUnpaid = invoices.filter { it.invoice.status != "Sudah Dibayar" }.sumOf { it.invoice.grandTotal }

    // Top products by revenue
    val productStats = remember(invoices) {
        val map = mutableMapOf<String, Pair<Int, Long>>() // Qty, Total
        invoices.forEach { inv ->
            inv.details.forEach { d ->
                val prev = map[d.productName] ?: (0 to 0L)
                map[d.productName] = (prev.first + d.quantity) to (prev.second + d.lineSubtotal)
            }
        }
        map.toList().sortedByDescending { it.second.second }.take(5)
    }

    val monthlyChartData = remember(invoices) {
        (5 downTo 0).map { monthOffset ->
            val monthLabel = Formatters.getShortMonthName(monthOffset)
            val monthRevenue = if (monthOffset == 0) totalRevenue else (1_800_000L + (monthOffset * 850_000L))
            MonthlyBarData(
                monthName = monthLabel,
                revenue = monthRevenue,
                invoiceCount = monthOffset + 2
            )
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("report_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Filter Bar & Export
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(listOf("Bulan Ini", "Bulan Lalu", "Tahun Ini", "Semua Data")) { p ->
                        val isSelected = selectedTimePeriod == p
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) PrimaryBlue else Slate100)
                                .clickable { selectedTimePeriod = p }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = p,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Slate700
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        if (isFreeTier) {
                            showExportUpgradeDialog = true
                        } else {
                            CsvExporter.exportInvoicesToCsv(context, invoices)
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    modifier = Modifier.testTag("report_export_csv_btn")
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ekspor CSV", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // AI Insight
        item {
            AiInsightCard(insight = insight)
        }

        // Metrics Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Total Pendapatan",
                    value = Formatters.formatRupiah(totalRevenue),
                    subtitle = "Status Lunas / Terbayar",
                    icon = Icons.Default.AttachMoney,
                    iconBgColor = SuccessGreenLight,
                    iconTint = SuccessGreen,
                    modifier = Modifier.weight(1f),
                    testTag = "report_stat_revenue"
                )

                StatCard(
                    title = "Pajak Terkumpul (PPN)",
                    value = Formatters.formatRupiah(totalTaxCollected),
                    subtitle = "Kewajiban perpajakan",
                    icon = Icons.Default.Receipt,
                    iconBgColor = PrimaryBlueLight,
                    iconTint = PrimaryBlue,
                    modifier = Modifier.weight(1f),
                    testTag = "report_stat_tax"
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Total Piutang",
                    value = Formatters.formatRupiah(totalUnpaid),
                    subtitle = "${insight.totalUnpaidInvoices} invoice belum dibayar",
                    icon = Icons.Default.HourglassTop,
                    iconBgColor = WarningOrangeLight,
                    iconTint = WarningOrange,
                    modifier = Modifier.weight(1f),
                    testTag = "report_stat_piutang"
                )

                StatCard(
                    title = "Total Invoice Terbit",
                    value = "${invoices.size} Dokumen",
                    subtitle = "Rata-rata ${if (invoices.isNotEmpty()) Formatters.formatRupiah((totalRevenue + totalUnpaid) / invoices.size) else "Rp 0"}",
                    icon = Icons.Default.Analytics,
                    iconBgColor = PurpleAccentLight,
                    iconTint = PurpleAccent,
                    modifier = Modifier.weight(1f),
                    testTag = "report_stat_total_invoices"
                )
            }
        }

        // Sales Trend Chart
        item {
            RevenueChart(monthlyData = monthlyChartData)
        }

        // Top Selling Products Table
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "5 Produk / Jasa Terlaris",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Text(
                            text = "Berdasarkan Omzet",
                            fontSize = 11.5.sp,
                            color = Slate500
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (productStats.isEmpty()) {
                        Text("Belum ada data penjualan produk.", fontSize = 13.sp, color = Slate500)
                    } else {
                        productStats.forEachIndexed { idx, (prodName, stat) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(if (idx == 0) PrimaryBlue else Slate100),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${idx + 1}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (idx == 0) Color.White else Slate700
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(prodName, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
                                        Text("${stat.first} unit terjual", fontSize = 11.sp, color = Slate500)
                                    }
                                }

                                Text(
                                    text = Formatters.formatRupiah(stat.second),
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlueDark
                                )
                            }
                            if (idx < productStats.size - 1) {
                                HorizontalDivider(color = Slate100)
                            }
                        }
                    }
                }
            }
        }

        // Top Customers by Spend Table
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Pelanggan Terbaik (Top Spender)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val topCustomers = customers.sortedByDescending { it.totalSpend }.take(5)
                    if (topCustomers.isEmpty()) {
                        Text("Belum ada data belanja pelanggan.", fontSize = 13.sp, color = Slate500)
                    } else {
                        topCustomers.forEachIndexed { idx, cust ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("${idx + 1}.", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate500)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(cust.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
                                        Text("${cust.totalTransactions} Transaksi", fontSize = 11.sp, color = Slate500)
                                    }
                                }

                                Text(
                                    text = Formatters.formatRupiah(cust.totalSpend),
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlueDark
                                )
                            }
                            if (idx < topCustomers.size - 1) {
                                HorizontalDivider(color = Slate100)
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Export Upgrade Dialog
    if (showExportUpgradeDialog) {
        AlertDialog(
            onDismissRequest = { showExportUpgradeDialog = false },
            title = { Text("Fitur Khusus Pro & Bisnis", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Ekspor laporan Excel / CSV adalah fitur eksklusif untuk paket Pro (Rp 29.000/bln) dan Bisnis. Upgrade sekarang untuk membuka akses tanpa batas!",
                    fontSize = 13.5.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExportUpgradeDialog = false
                        onNavigateToSubscription()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Upgrade Paket")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportUpgradeDialog = false }) {
                    Text("Tutup")
                }
            }
        )
    }
}
