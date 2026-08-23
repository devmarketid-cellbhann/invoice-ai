package com.example.ui.screens.invoice

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import com.example.data.model.Invoice
import com.example.data.model.InvoiceWithDetails
import com.example.data.model.User
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DangerRedLight
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueLight
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessGreenLight
import com.example.ui.theme.WarningOrange
import com.example.ui.theme.WarningOrangeLight
import com.example.util.CsvExporter
import com.example.util.Formatters
import com.example.util.PdfExporter
import com.example.util.WhatsAppHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceListScreen(
    user: User?,
    invoices: List<InvoiceWithDetails>,
    currentFilter: String,
    searchQuery: String,
    archiveFilter: String = "Aktif",
    sortOrder: String = "Terbaru",
    onFilterChange: (String) -> Unit,
    onArchiveFilterChange: (String) -> Unit = {},
    onSortOrderChange: (String) -> Unit = {},
    onSearchChange: (String) -> Unit,
    onCreateInvoiceClick: () -> Unit,
    onInvoiceClick: (InvoiceWithDetails) -> Unit,
    onMarkPaidClick: (String) -> Unit,
    onDuplicateInvoice: (InvoiceWithDetails) -> Unit = {},
    onToggleArchive: (Invoice) -> Unit = {},
    onDeleteClick: (InvoiceWithDetails) -> Unit,
    onNavigateToSubscription: () -> Unit
) {
    val context = LocalContext.current
    var showExportUpgradeDialog by remember { mutableStateOf(false) }
    var invoiceToDelete by remember { mutableStateOf<InvoiceWithDetails?>(null) }
    var showSortMenu by remember { mutableStateOf(false) }

    val isFreeTier = (user?.packageTier ?: "Gratis").equals("Gratis", ignoreCase = true)

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .testTag("invoice_list_screen")
        ) {
            // Search & Actions Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text("Cari No. Invoice / Pelanggan...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Cari", tint = Slate500) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("invoice_search_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                // Sort Dropdown
                Box {
                    OutlinedButton(
                        onClick = { showSortMenu = true },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.Sort, contentDescription = "Urutkan", tint = Slate700, modifier = Modifier.size(18.dp))
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        listOf("Terbaru", "Terlama", "Nominal Tertinggi", "Nominal Terendah", "Pelanggan").forEach { sort ->
                            DropdownMenuItem(
                                text = { Text(sort, fontSize = 13.sp, fontWeight = if (sortOrder == sort) FontWeight.Bold else FontWeight.Normal) },
                                onClick = {
                                    onSortOrderChange(sort)
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }

                // Export to Excel / CSV Button
                OutlinedButton(
                    onClick = {
                        if (isFreeTier) {
                            showExportUpgradeDialog = true
                        } else {
                            CsvExporter.exportInvoicesToCsv(context, invoices)
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                    modifier = Modifier.testTag("export_csv_button")
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ekspor", fontSize = 13.sp, color = PrimaryBlue, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips (Semua, Belum Dibayar, Sudah Dibayar, Terlambat)
            val filters = listOf("Semua", "Belum Dibayar", "Sudah Dibayar", "Terlambat")
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filters) { filter ->
                    val isSelected = currentFilter == filter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) PrimaryBlue else Slate100)
                            .clickable { onFilterChange(filter) }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                            .testTag("filter_chip_$filter")
                    ) {
                        Text(
                            text = filter,
                            fontSize = 12.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else Slate700
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.width(4.dp))
                    // Archive filter chip
                    listOf("Aktif", "Diarsipkan").forEach { arch ->
                        val isArchSelected = archiveFilter == arch
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isArchSelected) Slate700 else Slate100)
                                .clickable { onArchiveFilterChange(arch) }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = arch,
                                fontSize = 12.sp,
                                fontWeight = if (isArchSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isArchSelected) Color.White else Slate700
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Invoices List
            if (invoices.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Tidak Ada Faktur Ditemukan",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate700
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "Coba ubah kata kunci pencarian Anda" else "Buat faktur pertama Anda sekarang dengan tombol di bawah.",
                            fontSize = 12.5.sp,
                            color = Slate500
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(invoices, key = { it.invoice.invoiceNumber }) { item ->
                        InvoiceListItemCard(
                            invoiceWithDetails = item,
                            user = user,
                            onInvoiceClick = { onInvoiceClick(item) },
                            onMarkPaid = { onMarkPaidClick(item.invoice.invoiceNumber) },
                            onDuplicate = { onDuplicateInvoice(item) },
                            onToggleArchive = { onToggleArchive(item.invoice) },
                            onShareWhatsApp = {
                                WhatsAppHelper.sendInvoiceMessage(context, user ?: User(fullName = "", businessName = "", email = "", whatsapp = "", password = ""), item)
                            },
                            onPrintPdf = {
                                PdfExporter.generateAndSharePdf(context, user ?: User(fullName = "", businessName = "", email = "", whatsapp = "", password = ""), item)
                            },
                            onDelete = { invoiceToDelete = item }
                        )
                    }
                }
            }
        }

        // Floating Action Button to create invoice
        ExtendedFloatingActionButton(
            onClick = onCreateInvoiceClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("create_invoice_fab"),
            containerColor = PrimaryBlue,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            icon = { Icon(Icons.Default.Add, contentDescription = "Buat Faktur") },
            text = { Text("Buat Faktur", fontWeight = FontWeight.Bold) }
        )
    }

    // Delete Invoice Dialog
    invoiceToDelete?.let { inv ->
        AlertDialog(
            onDismissRequest = { invoiceToDelete = null },
            title = { Text("Hapus Faktur?", fontWeight = FontWeight.Bold) },
            text = { Text("Apakah Anda yakin ingin menghapus faktur #${inv.invoice.invoiceNumber} untuk ${inv.invoice.customerName}?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteClick(inv)
                        invoiceToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { invoiceToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }

    // Export upgrade prompt dialog for Free Tier
    if (showExportUpgradeDialog) {
        AlertDialog(
            onDismissRequest = { showExportUpgradeDialog = false },
            title = { Text("Fitur Khusus Paket Pro & Bisnis", fontWeight = FontWeight.Bold) },
            text = {
                Text("Ekspor laporan faktur ke file Excel/CSV lengkap adalah fitur premium untuk paket Pro dan Bisnis. Upgrade sekarang untuk membuka fitur ini!")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExportUpgradeDialog = false
                        onNavigateToSubscription()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Lihat Paket Upgrade")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportUpgradeDialog = false }) {
                    Text("Nanti Saja")
                }
            }
        )
    }
}

@Composable
fun InvoiceListItemCard(
    invoiceWithDetails: InvoiceWithDetails,
    user: User?,
    onInvoiceClick: () -> Unit,
    onMarkPaid: () -> Unit,
    onDuplicate: () -> Unit,
    onToggleArchive: () -> Unit,
    onShareWhatsApp: () -> Unit,
    onPrintPdf: () -> Unit,
    onDelete: () -> Unit
) {
    val inv = invoiceWithDetails.invoice
    val (statusBg, statusColor) = when (inv.status) {
        "Sudah Dibayar" -> SuccessGreenLight to SuccessGreen
        "Terlambat" -> DangerRedLight to DangerRed
        else -> WarningOrangeLight to WarningOrange
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onInvoiceClick() }
            .testTag("invoice_card_${inv.invoiceNumber}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Invoice No, Status Badge, and Archive Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "#${inv.invoiceNumber}",
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                    if (inv.isArchived) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(Slate100, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Arsip", fontSize = 10.sp, color = Slate500, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .background(statusBg, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = inv.status,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Body: Customer Info & Grand Total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = inv.customerName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate900
                    )
                    Text(
                        text = "Jatuh Tempo: ${Formatters.formatDate(inv.dueDate)}",
                        fontSize = 11.5.sp,
                        color = Slate500
                    )
                    Text(
                        text = "${invoiceWithDetails.details.size} item produk",
                        fontSize = 11.sp,
                        color = Slate500
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = Formatters.formatRupiah(inv.grandTotal),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Text(
                        text = Formatters.formatDate(inv.invoiceDate),
                        fontSize = 11.sp,
                        color = Slate500
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // PDF button
                IconButton(
                    onClick = onPrintPdf,
                    modifier = Modifier.size(34.dp).testTag("action_pdf_${inv.invoiceNumber}")
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = "Cetak PDF", tint = DangerRed, modifier = Modifier.size(19.dp))
                }

                // WhatsApp button
                IconButton(
                    onClick = onShareWhatsApp,
                    modifier = Modifier.size(34.dp).testTag("action_wa_${inv.invoiceNumber}")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Kirim WhatsApp", tint = SuccessGreen, modifier = Modifier.size(19.dp))
                }

                // Duplicate Button
                IconButton(
                    onClick = onDuplicate,
                    modifier = Modifier.size(34.dp).testTag("action_duplicate_${inv.invoiceNumber}")
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Duplikasi", tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                }

                // Archive / Unarchive Button
                IconButton(
                    onClick = onToggleArchive,
                    modifier = Modifier.size(34.dp).testTag("action_archive_${inv.invoiceNumber}")
                ) {
                    Icon(
                        imageVector = if (inv.isArchived) Icons.Default.Unarchive else Icons.Default.Archive,
                        contentDescription = "Arsipkan",
                        tint = Slate500,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Mark Paid Button (if not paid)
                if (inv.status != "Sudah Dibayar") {
                    IconButton(
                        onClick = onMarkPaid,
                        modifier = Modifier.size(34.dp).testTag("action_mark_paid_${inv.invoiceNumber}")
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Tandai Lunas", tint = SuccessGreen, modifier = Modifier.size(19.dp))
                    }
                }

                // Delete button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(34.dp).testTag("action_delete_${inv.invoiceNumber}")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Slate500, modifier = Modifier.size(19.dp))
                }
            }
        }
    }
}
