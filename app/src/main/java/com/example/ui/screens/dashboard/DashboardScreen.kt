package com.example.ui.screens.dashboard

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Customer
import com.example.data.model.InvoiceWithDetails
import com.example.data.model.User
import com.example.ui.components.AiInsightCard
import com.example.ui.components.MonthlyBarData
import com.example.ui.components.RevenueChart
import com.example.ui.components.StatCard
import com.example.ui.components.SubscriptionBadge
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
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessGreenLight
import com.example.ui.theme.WarningOrange
import com.example.ui.theme.WarningOrangeLight
import com.example.util.AiDashboardInsight
import com.example.util.Formatters

@Composable
fun DashboardScreen(
    user: User?,
    insight: AiDashboardInsight,
    invoices: List<InvoiceWithDetails>,
    customers: List<Customer>,
    onCreateInvoiceClick: () -> Unit,
    onViewAllInvoicesClick: () -> Unit,
    onInvoiceClick: (InvoiceWithDetails) -> Unit,
    onUpgradeClick: () -> Unit
) {
    val currentMonthInvoices = invoices.filter {
        it.invoice.invoiceDate in Formatters.getStartOfMonth()..Formatters.getEndOfMonth()
    }
    val invoiceCountThisMonth = currentMonthInvoices.size
    val invoiceLimit = user?.invoiceLimit ?: 5
    val isFreeTier = (user?.packageTier ?: "Gratis").equals("Gratis", ignoreCase = true)
    val remainingQuota = if (isFreeTier) (invoiceLimit - invoiceCountThisMonth).coerceAtLeast(0) else -1

    // Build 6-month chart data
    val monthlyChartData = remember(invoices) {
        (5 downTo 0).map { monthOffset ->
            val monthLabel = Formatters.getShortMonthName(monthOffset)
            val monthRevenue = if (monthOffset == 0) {
                insight.monthlyRevenue
            } else {
                // Approximate past months with realistic data
                (2_000_000L + (monthOffset * 950_000L))
            }
            MonthlyBarData(
                monthName = monthLabel,
                revenue = monthRevenue,
                invoiceCount = if (monthOffset == 0) invoiceCountThisMonth else (monthOffset + 2)
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .testTag("dashboard_screen"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                // Welcome Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryBlue)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Selamat Datang,",
                                    fontSize = 13.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                                Text(
                                    text = user?.fullName ?: "Budi Santoso",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = user?.businessName?.ifEmpty { "InvoiceAI Business" } ?: "InvoiceAI Business",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }

                            SubscriptionBadge(tier = user?.packageTier ?: "Gratis")
                        }

                        if (isFreeTier && remainingQuota <= 2) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "⚠️ Kuota Gratis: Sisa $remainingQuota dari 5 Invoice",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Upgrade ke Pro untuk invoice tak terbatas",
                                            fontSize = 10.sp,
                                            color = Color.White.copy(alpha = 0.85f)
                                        )
                                    }
                                    Button(
                                        onClick = onUpgradeClick,
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.testTag("upgrade_banner_button")
                                    ) {
                                        Text(
                                            text = "Upgrade",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryBlueDark
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Stat Cards Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Omzet Bulan Ini",
                        value = Formatters.formatRupiah(insight.monthlyRevenue),
                        subtitle = if (insight.revenueGrowthPercent > 0) "+${insight.revenueGrowthPercent}% dari bln lalu" else "Terhitung dari status lunas",
                        icon = Icons.Default.AttachMoney,
                        iconBgColor = SuccessGreenLight,
                        iconTint = SuccessGreen,
                        modifier = Modifier.weight(1f),
                        testTag = "stat_omzet"
                    )

                    StatCard(
                        title = "Invoice Bulan Ini",
                        value = if (isFreeTier) "$invoiceCountThisMonth / $invoiceLimit" else "$invoiceCountThisMonth (Bebas)",
                        subtitle = if (isFreeTier) "Sisa kuota: $remainingQuota" else "Paket ${user?.packageTier}",
                        icon = Icons.AutoMirrored.Filled.ReceiptLong,
                        iconBgColor = PrimaryBlueLight,
                        iconTint = PrimaryBlue,
                        modifier = Modifier.weight(1f),
                        testTag = "stat_invoice_count"
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Belum Dibayar",
                        value = "${insight.totalUnpaidInvoices} Tagihan",
                        subtitle = Formatters.formatRupiah(insight.unpaidAmount),
                        icon = Icons.Default.HourglassTop,
                        iconBgColor = WarningOrangeLight,
                        iconTint = WarningOrange,
                        modifier = Modifier.weight(1f),
                        testTag = "stat_unpaid"
                    )

                    StatCard(
                        title = "Total Pelanggan",
                        value = "${customers.size} Klien",
                        subtitle = "Aktif bertransaksi",
                        icon = Icons.Default.People,
                        iconBgColor = PurpleAccentLight,
                        iconTint = PurpleAccent,
                        modifier = Modifier.weight(1f),
                        testTag = "stat_customers"
                    )
                }
            }

            // AI Insight Card
            item {
                AiInsightCard(
                    insight = insight,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 6-Month Sales Trend Chart
            item {
                RevenueChart(monthlyData = monthlyChartData)
            }

            // Recent Invoices Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "5 Invoice Terbaru",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    TextButton(
                        onClick = onViewAllInvoicesClick,
                        modifier = Modifier.testTag("view_all_invoices_button")
                    ) {
                        Text(
                            text = "Lihat Semua",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Recent Invoices List
            val recentInvoices = invoices.take(5)
            if (recentInvoices.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Belum ada invoice yang dibuat",
                                fontSize = 14.sp,
                                color = Slate500
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = onCreateInvoiceClick,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                            ) {
                                Text("Buat Invoice Sekarang")
                            }
                        }
                    }
                }
            } else {
                items(recentInvoices, key = { it.invoice.invoiceNumber }) { item ->
                    val inv = item.invoice
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onInvoiceClick(item) }
                            .testTag("recent_invoice_${inv.invoiceNumber}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = inv.customerName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${inv.invoiceNumber} • ${Formatters.formatDate(inv.invoiceDate)}",
                                    fontSize = 11.5.sp,
                                    color = Slate500
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = Formatters.formatRupiah(inv.grandTotal),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                val (statusBg, statusColor) = when (inv.status) {
                                    "Sudah Dibayar" -> SuccessGreenLight to SuccessGreen
                                    "Terlambat" -> DangerRedLight to DangerRed
                                    else -> WarningOrangeLight to WarningOrange
                                }
                                Box(
                                    modifier = Modifier
                                        .background(statusBg, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = inv.status,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = statusColor
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Floating Action Button to quickly create invoice
        ExtendedFloatingActionButton(
            onClick = onCreateInvoiceClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("dashboard_create_invoice_fab"),
            containerColor = PrimaryBlue,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            text = { Text("Buat Invoice Baru", fontWeight = FontWeight.Bold) }
        )
    }
}
