package com.example.ui.screens.invoice

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.InvoiceWithDetails
import com.example.data.model.User
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DangerRedLight
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueDark
import com.example.ui.theme.PrimaryBlueLight
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
import com.example.util.Formatters
import com.example.util.PdfExporter
import com.example.util.WhatsAppHelper

@Composable
fun InvoiceDetailDialog(
    user: User?,
    invoiceWithDetails: InvoiceWithDetails,
    onDismiss: () -> Unit,
    onMarkPaid: (String) -> Unit,
    onDelete: (InvoiceWithDetails) -> Unit
) {
    val context = LocalContext.current
    val inv = invoiceWithDetails.invoice
    val details = invoiceWithDetails.details
    val dummyUser = user ?: User(fullName = "User", businessName = "Bisnis Anda", email = "", whatsapp = "", password = "")

    var showWaOptionsDialog by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(20.dp))
                .testTag("invoice_detail_dialog"),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Bar with Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Rincian Faktur",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Text(
                            text = inv.invoiceNumber,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_invoice_detail_btn")) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Slate700)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Printable Card Container
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate100)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Business & Status
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = user?.businessName ?: "Bisnis Anda",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlueDark
                                )
                                Text(
                                    text = user?.address ?: "Indonesia",
                                    fontSize = 11.5.sp,
                                    color = Slate500
                                )
                            }

                            val (statusBg, statusColor) = when (inv.status) {
                                "Sudah Dibayar" -> SuccessGreenLight to SuccessGreen
                                "Terlambat" -> DangerRedLight to DangerRed
                                else -> WarningOrangeLight to WarningOrange
                            }
                            Box(
                                modifier = Modifier
                                    .background(statusBg, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = inv.status,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = Slate200)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Customer Info
                        Text("DITAGIHKAN KEPADA:", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Slate500)
                        Text(inv.customerName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Slate900)
                        if (inv.customerPhone.isNotBlank()) Text("WhatsApp: ${inv.customerPhone}", fontSize = 12.sp, color = Slate700)
                        if (inv.customerAddress.isNotBlank()) Text("Alamat: ${inv.customerAddress}", fontSize = 12.sp, color = Slate700)

                        Spacer(modifier = Modifier.height(10.dp))

                        // Dates
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Tanggal Terbit: ${Formatters.formatDate(inv.invoiceDate)}", fontSize = 11.5.sp, color = Slate600)
                            Text("Jatuh Tempo: ${Formatters.formatDate(inv.dueDate)}", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = if (inv.status == "Terlambat") DangerRed else Slate600)
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Slate200)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Items Breakdown
                        Text("RINCIAN BARANG / JASA:", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Slate500)
                        Spacer(modifier = Modifier.height(6.dp))

                        details.forEach { detail ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(detail.productName, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Slate900)
                                    Text("${detail.quantity} ${detail.unit} x ${Formatters.formatRupiah(detail.unitPrice)}", fontSize = 11.sp, color = Slate500)
                                }
                                Text(
                                    Formatters.formatRupiah(detail.lineSubtotal),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = Slate200)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Subtotal & Totals
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Subtotal", fontSize = 12.5.sp, color = Slate600)
                            Text(Formatters.formatRupiah(inv.subtotal), fontSize = 12.5.sp, color = Slate900)
                        }
                        if (inv.taxPercent > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Pajak (${inv.taxPercent}%)", fontSize = 12.5.sp, color = Slate600)
                                Text(Formatters.formatRupiah(inv.taxAmount), fontSize = 12.5.sp, color = Slate900)
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("TOTAL AKHIR", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PrimaryBlueDark)
                            Text(
                                Formatters.formatRupiah(inv.grandTotal),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                        }

                        if (inv.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Catatan:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate500)
                            Text(inv.notes, fontSize = 12.sp, color = Slate700)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions Grid
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (inv.status != "Sudah Dibayar") {
                        Button(
                            onClick = {
                                onMarkPaid(inv.invoiceNumber)
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tandai Sudah Lunas / Dibayar", fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                PdfExporter.generateAndSharePdf(context, dummyUser, invoiceWithDetails)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cetak PDF", fontSize = 13.sp)
                        }

                        Button(
                            onClick = { showWaOptionsDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Menu WA", fontSize = 13.sp)
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            onDelete(invoiceWithDetails)
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = DangerRed, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Hapus Faktur Ini", color = DangerRed)
                    }
                }
            }
        }
    }

    // WhatsApp Message Options Modal
    if (showWaOptionsDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showWaOptionsDialog = false },
            title = {
                Text("Kirim Pesan WhatsApp", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Pilih template pesan untuk ${inv.customerName}:", fontSize = 13.sp, color = Slate600)

                    OutlinedButton(
                        onClick = {
                            WhatsAppHelper.sendInvoiceMessage(context, dummyUser, invoiceWithDetails)
                            showWaOptionsDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("🧾 Faktur Tagihan Lengkap", color = Slate900)
                    }

                    if (inv.status != "Sudah Dibayar") {
                        OutlinedButton(
                            onClick = {
                                WhatsAppHelper.sendScheduledReminder(context, dummyUser, invoiceWithDetails, WhatsAppHelper.ReminderTiming.BEFORE_3_DAYS)
                                showWaOptionsDialog = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("⏰ Pengingat H-3 Jatuh Tempo", color = Slate900)
                        }

                        OutlinedButton(
                            onClick = {
                                WhatsAppHelper.sendScheduledReminder(context, dummyUser, invoiceWithDetails, WhatsAppHelper.ReminderTiming.ON_DUE_DATE)
                                showWaOptionsDialog = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("🔔 Pengingat Hari-H Jatuh Tempo", color = Slate900)
                        }

                        OutlinedButton(
                            onClick = {
                                WhatsAppHelper.sendScheduledReminder(context, dummyUser, invoiceWithDetails, WhatsAppHelper.ReminderTiming.AFTER_OVERDUE)
                                showWaOptionsDialog = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("⚠️ Peringatan Keterlambatan (Overdue)", color = DangerRed)
                        }
                    } else {
                        OutlinedButton(
                            onClick = {
                                WhatsAppHelper.sendPaymentReceipt(context, dummyUser, invoiceWithDetails)
                                showWaOptionsDialog = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("✅ Kuitansi Pembayaran Lunas", color = SuccessGreen)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showWaOptionsDialog = false }) {
                    Text("Tutup")
                }
            }
        )
    }
}
