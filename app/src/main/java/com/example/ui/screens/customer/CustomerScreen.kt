package com.example.ui.screens.customer

import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Customer
import com.example.data.model.InvoiceWithDetails
import com.example.ui.theme.DangerRed
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueDark
import com.example.ui.theme.PrimaryBlueLight
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessGreenLight
import com.example.util.Formatters

import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Warning

@Composable
fun CustomerScreen(
    customers: List<Customer>,
    invoices: List<InvoiceWithDetails>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onSaveCustomer: (Customer) -> Unit,
    onDeleteCustomer: (Customer) -> Unit,
    onSelectCustomerForInvoice: (Customer) -> Unit,
    onToggleFavorite: (Customer) -> Unit = {}
) {
    val context = LocalContext.current
    var editingCustomer by remember { mutableStateOf<Customer?>(null) }
    var isAddingNew by remember { mutableStateOf(false) }
    var customerToDelete by remember { mutableStateOf<Customer?>(null) }
    var viewingCustomerDetail by remember { mutableStateOf<Customer?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .testTag("customer_screen")
        ) {
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Cari nama atau no. telepon pelanggan...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Cari", tint = Slate500) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("customer_search_input"),
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (customers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Belum ada pelanggan terdaftar.",
                            fontSize = 14.sp,
                            color = Slate500
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { isAddingNew = true },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Text("Tambah Pelanggan Pertama")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(customers, key = { it.id }) { customer ->
                        val customerInvoices = invoices.filter { it.invoice.customerId == customer.id }
                        val totalCustomerSpend = customerInvoices.sumOf { it.invoice.grandTotal }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewingCustomerDetail = customer }
                                .testTag("customer_card_${customer.id}"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(PrimaryBlueLight),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = customer.name.take(2).uppercase(),
                                                color = PrimaryBlue,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = customer.name,
                                                fontSize = 14.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Slate900
                                            )
                                            Text(
                                                text = customer.phone.ifEmpty { "Tanpa nomor telp" },
                                                fontSize = 12.sp,
                                                color = Slate500
                                            )
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = Formatters.formatRupiah(totalCustomerSpend),
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryBlueDark
                                        )
                                        Text(
                                            text = "${customerInvoices.size} Transaksi",
                                            fontSize = 11.sp,
                                            color = Slate500
                                        )
                                    }
                                }

                                if (customer.address.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Slate500, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = customer.address,
                                            fontSize = 11.5.sp,
                                            color = Slate700,
                                            maxLines = 1
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = Slate200)
                                Spacer(modifier = Modifier.height(6.dp))

                                // Action Buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Favorite Toggle Button
                                    IconButton(
                                        onClick = { onToggleFavorite(customer) },
                                        modifier = Modifier.size(32.dp).testTag("fav_customer_${customer.id}")
                                    ) {
                                        Icon(
                                            imageVector = if (customer.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                            contentDescription = "Favorit",
                                            tint = if (customer.isFavorite) Color(0xFFF59E0B) else Slate500,
                                            modifier = Modifier.size(19.dp)
                                        )
                                    }

                                    // Direct WhatsApp
                                    IconButton(
                                        onClick = {
                                            val cleanPhone = customer.phone.replace("[^0-9]".toRegex(), "")
                                            val finalPhone = if (cleanPhone.startsWith("0")) "62" + cleanPhone.substring(1) else cleanPhone
                                            val uri = Uri.parse("https://wa.me/$finalPhone")
                                            val intent = Intent(Intent.ACTION_VIEW, uri)
                                            try {
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Tidak dapat membuka WhatsApp", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "WhatsApp", tint = SuccessGreen, modifier = Modifier.size(18.dp))
                                    }

                                    // Edit
                                    IconButton(
                                        onClick = { editingCustomer = customer },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                                    }

                                    // Delete
                                    IconButton(
                                        onClick = { customerToDelete = customer },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = DangerRed, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(70.dp))
                    }
                }
            }
        }

        // FAB
        ExtendedFloatingActionButton(
            onClick = { isAddingNew = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_customer_fab"),
            containerColor = PrimaryBlue,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            text = { Text("Tambah Pelanggan", fontWeight = FontWeight.Bold) }
        )
    }

    // Add / Edit Customer Dialog
    if (isAddingNew || editingCustomer != null) {
        val target = editingCustomer
        var name by remember { mutableStateOf(target?.name ?: "") }
        var phone by remember { mutableStateOf(target?.phone ?: "") }
        var email by remember { mutableStateOf(target?.email ?: "") }
        var address by remember { mutableStateOf(target?.address ?: "") }
        var notes by remember { mutableStateOf(target?.notes ?: "") }
        var error by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = {
                isAddingNew = false
                editingCustomer = null
            },
            title = {
                Text(
                    text = if (target == null) "Tambah Pelanggan Baru" else "Edit Data Pelanggan",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it; error = "" },
                        label = { Text("Nama Lengkap / Nama Bisnis *") },
                        modifier = Modifier.fillMaxWidth().testTag("customer_form_name"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it; error = "" },
                        label = { Text("No. WhatsApp / HP *") },
                        placeholder = { Text("08123456789") },
                        modifier = Modifier.fillMaxWidth().testTag("customer_form_phone"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email (Opsional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Alamat") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Catatan Tambahan") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    if (error.isNotEmpty()) {
                        Text(error, color = DangerRed, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isBlank() || phone.isBlank()) {
                            error = "Nama dan No. WhatsApp wajib diisi!"
                            return@Button
                        }
                        val customerToSave = target?.copy(
                            name = name.trim(),
                            phone = phone.trim(),
                            email = email.trim(),
                            address = address.trim(),
                            notes = notes.trim()
                        ) ?: Customer(
                            name = name.trim(),
                            phone = phone.trim(),
                            email = email.trim(),
                            address = address.trim(),
                            notes = notes.trim()
                        )

                        onSaveCustomer(customerToSave)
                        isAddingNew = false
                        editingCustomer = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Simpan Data")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    isAddingNew = false
                    editingCustomer = null
                }) {
                    Text("Batal")
                }
            }
        )
    }

    // Customer Detail Sheet Dialog
    viewingCustomerDetail?.let { cust ->
        val custInvoices = invoices.filter { it.invoice.customerId == cust.id }
        AlertDialog(
            onDismissRequest = { viewingCustomerDetail = null },
            title = { Text(cust.name, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("WhatsApp: ${cust.phone.ifEmpty { "-" }}", fontSize = 13.sp)
                    if (cust.email.isNotBlank()) Text("Email: ${cust.email}", fontSize = 13.sp)
                    if (cust.address.isNotBlank()) Text("Alamat: ${cust.address}", fontSize = 13.sp)
                    if (cust.notes.isNotBlank()) Text("Catatan: ${cust.notes}", fontSize = 13.sp, color = Slate700)

                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider(color = Slate200)
                    Spacer(modifier = Modifier.height(4.dp))

                    Text("Riwayat Transaksi (${custInvoices.size}):", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = PrimaryBlueDark)
                    if (custInvoices.isEmpty()) {
                        Text("Belum ada faktur yang dibuat untuk pelanggan ini.", fontSize = 12.sp, color = Slate500)
                    } else {
                        custInvoices.take(3).forEach { inv ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${inv.invoice.invoiceNumber} (${inv.invoice.status})", fontSize = 11.5.sp, color = Slate700)
                                Text(Formatters.formatRupiah(inv.invoice.grandTotal), fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Slate900)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val selected = viewingCustomerDetail
                        viewingCustomerDetail = null
                        if (selected != null) onSelectCustomerForInvoice(selected)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Buat Invoice untuk Klien Ini")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewingCustomerDetail = null }) {
                    Text("Tutup")
                }
            }
        )
    }

    // Delete confirmation
    customerToDelete?.let { cust ->
        AlertDialog(
            onDismissRequest = { customerToDelete = null },
            title = { Text("Hapus Pelanggan", fontWeight = FontWeight.Bold) },
            text = { Text("Apakah Anda yakin ingin menghapus pelanggan '${cust.name}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteCustomer(cust)
                        customerToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { customerToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }
}
