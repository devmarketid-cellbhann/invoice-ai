package com.example.ui.screens.subscription

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Payment
import com.example.data.model.User
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
import com.example.ui.theme.Slate50
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessGreenLight
import com.example.ui.theme.WarningOrange
import com.example.ui.theme.WarningOrangeLight
import com.example.util.Formatters
import com.example.util.WhatsAppHelper

data class TierPlan(
    val tierName: String,
    val priceRupiah: Long,
    val billingPeriod: String,
    val description: String,
    val features: List<String>,
    val isPopular: Boolean = false,
    val accentColor: Color = PrimaryBlue
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    user: User?,
    payments: List<Payment>,
    allPaymentsForAdmin: List<Payment>,
    onSubmitQrisPayment: (String, Long, String, String, String, (Boolean, String) -> Unit) -> Unit,
    onAdminApprove: (Payment) -> Unit,
    onAdminReject: (Payment, String) -> Unit,
    onAdminMarkExpired: (Payment) -> Unit = {},
    onCancelSubscription: () -> Unit
) {
    val context = LocalContext.current
    val currentTier = user?.packageTier ?: "Gratis"
    val isAdmin = user?.role == "Admin"

    var selectedTab by remember { mutableIntStateOf(0) }
    var planToCheckout by remember { mutableStateOf<TierPlan?>(null) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var showGuideDialog by remember { mutableStateOf(false) }

    // Admin dialog states
    var paymentToReject by remember { mutableStateOf<Payment?>(null) }
    var rejectionReasonText by remember { mutableStateOf("") }
    var paymentDetailToShow by remember { mutableStateOf<Payment?>(null) }

    val plans = listOf(
        TierPlan(
            tierName = "Gratis",
            priceRupiah = 0L,
            billingPeriod = "Selamanya",
            description = "Cocok untuk UMKM pemula & uji coba",
            features = listOf(
                "Maksimal 5 Invoice / bulan",
                "Cetak & Simpan PDF Faktur",
                "Kirim WhatsApp Faktur Manual",
                "1 Pengguna Akun",
                "AI Ringkasan Keuangan Dasar"
            ),
            accentColor = Slate700
        ),
        TierPlan(
            tierName = "Pro",
            priceRupiah = 29000L,
            billingPeriod = "/ 30 Hari",
            description = "Paling populer untuk Bisnis & UMKM Bertumbuh",
            features = listOf(
                "Semua fitur paket Gratis",
                "Invoice & Laporan Tak Terbatas",
                "Laporan Penjualan Lengkap",
                "Ekspor PDF + CSV Laporan",
                "Pengingat Tagihan Otomatis via WhatsApp",
                "QRIS Dynamic di Lembar Faktur",
                "Bebas Watermark & Iklan"
            ),
            isPopular = true,
            accentColor = PrimaryBlue
        ),
        TierPlan(
            tierName = "Bisnis",
            priceRupiah = 79000L,
            billingPeriod = "/ 30 Hari",
            description = "Untuk Perusahaan & Tim Multi-Pengguna",
            features = listOf(
                "Semua fitur paket Pro",
                "Hingga 5 Akun Pengguna / Staf",
                "AI Prediksi Penjualan & Analisis Tren",
                "Akses API & Database Multi-Cabang",
                "Dukungan Prioritas 24/7 via WhatsApp VIP"
            ),
            accentColor = PurpleAccent
        )
    )

    // Check if user has an existing pending payment
    val pendingPayment = remember(payments) {
        payments.firstOrNull { it.paymentStatus == "Menunggu Verifikasi" }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("subscription_screen")
    ) {
        if (isAdmin) {
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Pilihan Paket & Status", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        val pendingAdminCount = allPaymentsForAdmin.count { it.paymentStatus == "Menunggu Verifikasi" }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Panel Admin QRIS", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            if (pendingAdminCount > 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(DangerRed, CircleShape)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = pendingAdminCount.toString(),
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                )
            }
        }

        if (selectedTab == 0) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Banner
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = PrimaryBlue)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color.White.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VerifiedUser,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Langganan & Pembayaran QRIS",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Tingkatkan produktivitas bisnis Anda tanpa batas",
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Paket Saat Ini:", fontSize = 11.5.sp, color = Color.White.copy(alpha = 0.85f))
                                    Text(
                                        text = "Paket $currentTier",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    TextButton(
                                        onClick = { showGuideDialog = true },
                                        colors = androidx.compose.material3.ButtonDefaults.textButtonColors(contentColor = Color.White)
                                    ) {
                                        Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Panduan QRIS", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }

                                    if (currentTier != "Gratis") {
                                        OutlinedButton(
                                            onClick = { showCancelDialog = true },
                                            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                                                contentColor = Color.White
                                            ),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Batalkan", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Active Subscription Details & 30-Day Countdown Card
                item {
                    val now = System.currentTimeMillis()
                    val daysRemaining = if (user?.packageEndDate != null && user.packageEndDate > now) {
                        ((user.packageEndDate - now) / (1000 * 60 * 60 * 24)).coerceAtLeast(0)
                    } else 0

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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = null,
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Status Masa Aktif Langganan",
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate900
                                    )
                                }

                                if (currentTier != "Gratis" && daysRemaining > 0) {
                                    Box(
                                        modifier = Modifier
                                            .background(SuccessGreenLight, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "$daysRemaining Hari Tersisa",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SuccessGreen
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = Slate100)
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Mulai Aktif:", fontSize = 11.sp, color = Slate500)
                                    Text(
                                        text = if (user?.packageStartDate != null) Formatters.formatDate(user.packageStartDate) else "Saat Akun Dibuat",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Slate700
                                    )
                                }

                                Column {
                                    Text("Berlaku Sampai:", fontSize = 11.sp, color = Slate500)
                                    Text(
                                        text = if (currentTier == "Gratis") "Selamanya (Maks 5 Inv/bln)"
                                        else if (user?.packageEndDate != null) Formatters.formatDate(user.packageEndDate)
                                        else "-",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (daysRemaining <= 3 && currentTier != "Gratis") DangerRed else Slate700
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Kuota Bulan Ini:", fontSize = 11.sp, color = Slate500)
                                    Text(
                                        text = if (currentTier == "Gratis") "${user?.invoicesThisMonth ?: 0} / 5 Faktur" else "Tak Terbatas",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (currentTier == "Gratis" && (user?.invoicesThisMonth ?: 0) >= 5) DangerRed else SuccessGreen
                                    )
                                }
                            }

                            if (currentTier != "Gratis") {
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(PrimaryBlueLight.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = "ℹ️ Perpanjang sebelum habis akan menambahkan 30 hari penuh ke sisa masa aktif Anda saat ini.",
                                        fontSize = 11.sp,
                                        color = PrimaryBlueDark
                                    )
                                }
                            }
                        }
                    }
                }

                // Pending Payment Warning Banner
                if (pendingPayment != null) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = WarningOrangeLight),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, WarningOrange)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HourglassEmpty,
                                    contentDescription = null,
                                    tint = WarningOrange,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Pengajuan Pembayaran Sedang Diproses",
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate900
                                    )
                                    Text(
                                        text = "Kode Ref #${pendingPayment.referenceCode} (${pendingPayment.planPurchased} - ${Formatters.formatRupiah(pendingPayment.amountPaid)}) menunggu persetujuan admin.",
                                        fontSize = 11.5.sp,
                                        color = Slate700
                                    )
                                }
                            }
                        }
                    }
                }

                // Pricing Cards Title
                item {
                    Text(
                        text = "Pilih Paket yang Sesuai",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                }

                // Tier Plan Cards
                items(plans) { plan ->
                    val isCurrent = currentTier.equals(plan.tierName, ignoreCase = true)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tier_card_${plan.tierName.lowercase()}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (plan.isPopular) 4.dp else 2.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (plan.isPopular) 2.dp else 1.dp,
                            color = if (plan.isPopular) PrimaryBlue else Slate200
                        )
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Paket ${plan.tierName}",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate900
                                    )
                                    Text(
                                        text = plan.description,
                                        fontSize = 11.5.sp,
                                        color = Slate500
                                    )
                                }

                                if (plan.isPopular) {
                                    Box(
                                        modifier = Modifier
                                            .background(PrimaryBlue, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "TERPOPULER",
                                            color = Color.White,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = if (plan.priceRupiah == 0L) "Gratis" else Formatters.formatRupiah(plan.priceRupiah),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = plan.accentColor
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = plan.billingPeriod,
                                    fontSize = 12.sp,
                                    color = Slate500,
                                    modifier = Modifier.padding(bottom = 3.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = Slate100)
                            Spacer(modifier = Modifier.height(12.dp))

                            // Features list
                            plan.features.forEach { feat ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = SuccessGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = feat,
                                        fontSize = 12.5.sp,
                                        color = Slate700
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            if (isCurrent) {
                                Button(
                                    onClick = {},
                                    enabled = false,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        disabledContainerColor = Slate100,
                                        disabledContentColor = Slate500
                                    )
                                ) {
                                    Text("Paket Saat Ini Aktif", fontWeight = FontWeight.Bold)
                                }
                            } else {
                                val isPending = pendingPayment != null
                                Button(
                                    onClick = { planToCheckout = plan },
                                    enabled = !isPending,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(46.dp)
                                        .testTag("choose_plan_${plan.tierName.lowercase()}"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = plan.accentColor)
                                ) {
                                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isPending) "Ada Pengajuan Menunggu" else "Bayar via QRIS (${if (plan.priceRupiah == 0L) "Gratis" else Formatters.formatRupiah(plan.priceRupiah)})",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Payment History Section
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Riwayat Pembayaran QRIS Saya",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            if (payments.isEmpty()) {
                                Text(
                                    text = "Belum ada riwayat transaksi pembayaran QRIS.",
                                    fontSize = 12.5.sp,
                                    color = Slate500
                                )
                            } else {
                                payments.forEachIndexed { idx, p ->
                                    val (statusBg, statusColor, statusIcon) = when (p.paymentStatus) {
                                        "Disetujui" -> Triple(SuccessGreenLight, SuccessGreen, "✅ Disetujui")
                                        "Ditolak" -> Triple(DangerRedLight, DangerRed, "❌ Ditolak")
                                        "Kadaluwarsa" -> Triple(Slate100, Slate500, "⚠️ Kadaluwarsa")
                                        else -> Triple(WarningOrangeLight, WarningOrange, "⏳ Menunggu Verifikasi")
                                    }

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text("Paket ${p.planPurchased}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Slate900)
                                                Text("Ref: ${p.referenceCode} • ${Formatters.formatDate(p.paymentDate)}", fontSize = 11.5.sp, color = Slate500)
                                                Text("Pengirim: ${p.senderName} (${p.senderBank})", fontSize = 11.5.sp, color = Slate700)
                                            }
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(Formatters.formatRupiah(p.amountPaid), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PrimaryBlueDark)
                                                Spacer(modifier = Modifier.height(3.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .background(statusBg, RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(statusIcon, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = statusColor)
                                                }
                                            }
                                        }

                                        if (p.rejectionReason.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("Alasan penolakan: ${p.rejectionReason}", fontSize = 11.sp, color = DangerRed)
                                        }
                                    }
                                    if (idx < payments.size - 1) HorizontalDivider(color = Slate100)
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        } else {
            // ADMIN PANEL VIEW
            AdminPaymentVerificationView(
                payments = allPaymentsForAdmin,
                onApprove = onAdminApprove,
                onRejectClick = { p ->
                    paymentToReject = p
                    rejectionReasonText = ""
                },
                onMarkExpired = onAdminMarkExpired,
                onViewDetail = { paymentDetailToShow = it }
            )
        }
    }

    // QRIS Checkout Modal with FRAUD WARNING
    planToCheckout?.let { plan ->
        var senderName by remember { mutableStateOf(user?.fullName ?: "") }
        var selectedBank by remember { mutableStateOf("BCA") }
        var transferNote by remember { mutableStateOf("") }
        var isSubmitting by remember { mutableStateOf(false) }
        var submittedRefCode by remember { mutableStateOf<String?>(null) }

        val bankOptions = listOf("BCA", "Mandiri", "BRI", "BNI", "GoPay", "OVO", "DANA", "ShopeePay", "LinkAja", "BSI", "SeaBank")

        Dialog(
            onDismissRequest = { if (!isSubmitting) planToCheckout = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .clip(RoundedCornerShape(20.dp))
                    .testTag("qris_checkout_dialog"),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (submittedRefCode != null) {
                        // Success confirmation screen
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Bukti Pembayaran Terkirim!",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Kode Referensi: $submittedRefCode",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Admin kami sedang memverifikasi mutasi transfer Anda. Paket akan aktif otomatis dalam maks 1x24 jam.",
                                fontSize = 12.5.sp,
                                textAlign = TextAlign.Center,
                                color = Slate600
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = {
                                    val adminPhone = user?.adminWhatsApp?.ifBlank { "081234567890" } ?: "081234567890"
                                    val mockPayment = Payment(
                                        userId = user?.id ?: 1L,
                                        planPurchased = plan.tierName,
                                        amountPaid = plan.priceRupiah,
                                        referenceCode = submittedRefCode ?: "",
                                        senderName = senderName,
                                        senderBank = selectedBank,
                                        proofNote = transferNote
                                    )
                                    WhatsAppHelper.sendQrisConfirmationToAdmin(
                                        context,
                                        adminPhone,
                                        user ?: User(fullName = senderName, businessName = "", email = "", whatsapp = "", password = ""),
                                        mockPayment
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Konfirmasi ke WhatsApp Admin")
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            TextButton(
                                onClick = {
                                    planToCheckout = null
                                    submittedRefCode = null
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Selesai", color = Slate700)
                            }
                        }
                    } else {
                        // QRIS Payment instructions and form
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Pembayaran Manual QRIS", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Slate900)
                                Text("Paket ${plan.tierName} • ${Formatters.formatRupiah(plan.priceRupiah)}", fontSize = 12.5.sp, color = PrimaryBlue, fontWeight = FontWeight.Bold)
                            }

                            IconButton(onClick = { planToCheckout = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Slate700)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // STRICT FRAUD WARNING BANNER
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = WarningOrangeLight),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, WarningOrange)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "⚠️ HARAP BAYAR SESUAI NOMINAL = ${Formatters.formatRupiah(plan.priceRupiah)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFB45309)
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "⚠️ TIDAK BOLEH KURANG / LEBIH — TIDAK AKAN DIPROSES",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DangerRed
                                )
                                Text(
                                    text = "⚠️ JANGAN BAYAR NOMINAL LAIN",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB45309)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Indonesian QRIS Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Slate100),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // QRIS Top Header Banner
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(DangerRed, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("QRIS", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp, letterSpacing = 1.sp)
                                    Text("NATIONAL QR STANDARD", color = Color.White.copy(alpha = 0.9f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(user?.qrisMerchantName ?: "INVOICEAI NUSANTARA PUSAT", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Slate900)
                                Text("NMID: ${user?.qrisNmid ?: "ID1029384756019"}", fontSize = 10.5.sp, color = Slate500, fontFamily = FontFamily.Monospace)

                                Spacer(modifier = Modifier.height(10.dp))

                                // QR Code Graphic Illustration
                                Box(
                                    modifier = Modifier
                                        .size(160.dp)
                                        .background(Color.White, RoundedCornerShape(10.dp))
                                        .border(1.dp, Slate200, RoundedCornerShape(10.dp))
                                        .padding(12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.QrCode,
                                            contentDescription = "Kode QRIS",
                                            tint = Slate900,
                                            modifier = Modifier.size(110.dp)
                                        )
                                        Text("Scan via M-Banking / E-Wallet", fontSize = 9.sp, color = Slate500, fontWeight = FontWeight.Medium)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text("Total Tagihan Transfer:", fontSize = 11.5.sp, color = Slate600)
                                Text(
                                    text = Formatters.formatRupiah(plan.priceRupiah),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlueDark
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Form Section
                        Text("Konfirmasi Pembayaran Anda:", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = Slate900)
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = senderName,
                            onValueChange = { senderName = it },
                            label = { Text("Nama Pengirim / Pemilik Rekening") },
                            placeholder = { Text("Contoh: Budi Santoso") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Pilih Bank / E-Wallet Pengirim:", fontSize = 11.5.sp, color = Slate600)
                        Spacer(modifier = Modifier.height(4.dp))

                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(bankOptions) { bank ->
                                val isSelected = selectedBank == bank
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) PrimaryBlue else Slate100)
                                        .clickable { selectedBank = bank }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = bank,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else Slate700
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = transferNote,
                            onValueChange = { transferNote = it },
                            label = { Text("Catatan / No. Referensi Transfer (Opsional)") },
                            placeholder = { Text("Contoh: Transfer via M-BCA jam 14:30") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (senderName.isBlank()) return@Button
                                isSubmitting = true
                                onSubmitQrisPayment(
                                    plan.tierName,
                                    plan.priceRupiah,
                                    senderName,
                                    selectedBank,
                                    transferNote
                                ) { success, refCode ->
                                    isSubmitting = false
                                    if (success) {
                                        submittedRefCode = refCode
                                    }
                                }
                            },
                            enabled = senderName.isNotBlank() && !isSubmitting,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("submit_qris_payment_btn"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                            } else {
                                Text("Kirim Bukti Pembayaran", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // Step-by-Step Payment Guide Dialog
    if (showGuideDialog) {
        AlertDialog(
            onDismissRequest = { showGuideDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = PrimaryBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Panduan Pembayaran QRIS", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "1. Buka aplikasi m-Banking (BCA, Mandiri, BRI, BNI) atau E-Wallet (GoPay, OVO, DANA, ShopeePay).",
                        "2. Pilih menu Bayar / Scan QRIS pada aplikasi Anda.",
                        "3. Arahkan kamera HP ke QR Code yang ditampilkan.",
                        "4. Pastikan nama penerima/merchant sesuai dengan InvoiceAI.",
                        "5. Masukkan NOMINAL TEPAT sesuai harga paket — jangan kurang atau lebih.",
                        "6. Konfirmasi pembayaran & masukkan PIN Anda. Simpan bukti transfer dan kirimkan di aplikasi ini."
                    ).forEach { step ->
                        Text(step, fontSize = 12.5.sp, color = Slate700, lineHeight = 17.sp)
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showGuideDialog = false }) {
                    Text("Mengerti")
                }
            }
        )
    }

    // Rejection Reason Dialog
    paymentToReject?.let { p ->
        AlertDialog(
            onDismissRequest = { paymentToReject = null },
            title = { Text("Tolak Pembayaran #${p.referenceCode}", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Pengguna: ${p.userName} • ${Formatters.formatRupiah(p.amountPaid)} (${p.planPurchased})", fontSize = 12.5.sp, color = Slate600)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = rejectionReasonText,
                        onValueChange = { rejectionReasonText = it },
                        label = { Text("Alasan Penolakan") },
                        placeholder = { Text("Contoh: Mutasi belum masuk / nominal tidak sesuai") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onAdminReject(p, rejectionReasonText.ifBlank { "Bukti pembayaran tidak sesuai atau mutasi belum masuk" })
                        paymentToReject = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("Tolak Transaksi")
                }
            },
            dismissButton = {
                TextButton(onClick = { paymentToReject = null }) {
                    Text("Batal")
                }
            }
        )
    }

    // Cancel Subscription Dialog
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Batalkan Langganan?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Akun Anda akan diturunkan ke paket Gratis (maksimal 5 invoice/bulan) dan fitur ekspor CSV akan dinonaktifkan.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCancelSubscription()
                        showCancelDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("Ya, Batalkan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Tetap Berlangganan")
                }
            }
        )
    }
}

@Composable
fun AdminPaymentVerificationView(
    payments: List<Payment>,
    onApprove: (Payment) -> Unit,
    onRejectClick: (Payment) -> Unit,
    onMarkExpired: (Payment) -> Unit,
    onViewDetail: (Payment) -> Unit
) {
    var statusFilter by remember { mutableStateOf("Menunggu Verifikasi") }
    val filteredPayments = when (statusFilter) {
        "Menunggu Verifikasi" -> payments.filter { it.paymentStatus == "Menunggu Verifikasi" }
        "Disetujui" -> payments.filter { it.paymentStatus == "Disetujui" }
        "Ditolak" -> payments.filter { it.paymentStatus == "Ditolak" }
        "Kadaluwarsa" -> payments.filter { it.paymentStatus == "Kadaluwarsa" }
        else -> payments
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Slate100)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Panel Admin & Verifikasi Pembayaran", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Slate900)
                        Text("Tinjau transaksi QRIS, periksa nominal mutasi, dan setujui upgrade paket 30 hari.", fontSize = 11.5.sp, color = Slate600)
                    }
                }
            }
        }

        // Filter chips
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Menunggu Verifikasi", "Disetujui", "Ditolak", "Kadaluwarsa", "Semua").forEach { status ->
                    item {
                        FilterChip(
                            selected = statusFilter == status,
                            onClick = { statusFilter = status },
                            label = { Text(status) }
                        )
                    }
                }
            }
        }

        if (filteredPayments.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Tidak ada transaksi pembayaran dengan status '$statusFilter'", color = Slate500, fontSize = 13.sp)
                }
            }
        } else {
            items(filteredPayments, key = { it.id }) { pay ->
                val (statusBg, statusColor) = when (pay.paymentStatus) {
                    "Disetujui" -> SuccessGreenLight to SuccessGreen
                    "Ditolak" -> DangerRedLight to DangerRed
                    "Kadaluwarsa" -> Slate100 to Slate500
                    else -> WarningOrangeLight to WarningOrange
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Ref: #${pay.referenceCode}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PrimaryBlue)
                                Text("Pemesan: ${pay.userName.ifBlank { "User #" + pay.userId }} (${pay.userEmail})", fontSize = 12.sp, color = Slate700)
                            }
                            Box(
                                modifier = Modifier
                                    .background(statusBg, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                Text(pay.paymentStatus, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = statusColor)
                            }
                        }

                        // Nominal Mismatch Alert Tag
                        if (pay.mismatchAmountWarning) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(DangerRedLight, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "⚠️ PERINGATAN: Nominal transfer berpotensi tidak cocok dengan harga paket!",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DangerRed
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = Slate100)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Paket: ${pay.planPurchased}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate900)
                                Text("Bank Pengirim: ${pay.senderBank} • a.n ${pay.senderName}", fontSize = 12.sp, color = Slate600)
                                if (pay.proofNote.isNotBlank()) {
                                    Text("Catatan: ${pay.proofNote}", fontSize = 11.sp, color = Slate500)
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(Formatters.formatRupiah(pay.amountPaid), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = PrimaryBlueDark)
                                Text(Formatters.formatDate(pay.paymentDate), fontSize = 11.sp, color = Slate500)
                            }
                        }

                        // Status History & Audit Trail
                        if (pay.statusHistory.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Riwayat Status:\n${pay.statusHistory}",
                                fontSize = 10.sp,
                                color = Slate500,
                                lineHeight = 14.sp
                            )
                        }

                        if (pay.paymentStatus == "Menunggu Verifikasi") {
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { onRejectClick(pay) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed)
                                ) {
                                    Text("Tolak", color = DangerRed)
                                }

                                Button(
                                    onClick = { onApprove(pay) },
                                    modifier = Modifier.weight(1.5f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Setujui (30 Hari)", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
