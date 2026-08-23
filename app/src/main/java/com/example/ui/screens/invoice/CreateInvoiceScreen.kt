package com.example.ui.screens.invoice

import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.example.data.model.Invoice
import com.example.data.model.InvoiceDetail
import com.example.data.model.InvoiceWithDetails
import com.example.data.model.Product
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
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessGreenLight
import com.example.util.Formatters
import com.example.util.PdfExporter
import com.example.util.WhatsAppHelper

data class InvoiceItemRowState(
    var product: Product,
    var quantity: Int = 1,
    var customPrice: Long = product.price
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateInvoiceScreen(
    user: User?,
    customers: List<Customer>,
    products: List<Product>,
    onBackClick: () -> Unit,
    onSaveSuccess: (InvoiceWithDetails) -> Unit,
    onNavigateToSubscription: () -> Unit,
    onSaveCustomerQuick: (Customer) -> Unit
) {
    val context = LocalContext.current

    // Check Quota condition
    val isFreeTier = (user?.packageTier ?: "Gratis").equals("Gratis", ignoreCase = true)
    val isQuotaExceeded = isFreeTier && ((user?.invoicesThisMonth ?: 0) >= 5)

    var invoiceNumber by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        val today = Formatters.getTodayDatePrefix()
        invoiceNumber = "INV-$today-${(1..999).random().toString().padStart(4, '0')}"
    }

    var selectedCustomer by remember { mutableStateOf<Customer?>(customers.firstOrNull()) }
    var customerDropdownExpanded by remember { mutableStateOf(false) }
    var showAddCustomerDialog by remember { mutableStateOf(false) }

    val invoiceDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var dueDaysOffset by remember { mutableStateOf(7) } // 7 days default
    val dueDate by remember(invoiceDate, dueDaysOffset) {
        derivedStateOf { invoiceDate + (dueDaysOffset.toLong() * 24 * 60 * 60 * 1000) }
    }

    // Invoice Item Lines
    val itemRows = remember {
        mutableStateListOf<InvoiceItemRowState>().apply {
            products.firstOrNull()?.let { add(InvoiceItemRowState(it, 1, it.price)) }
        }
    }

    var taxPercent by remember { mutableStateOf(11) } // 11% PPN Indonesia default
    var notes by remember {
        mutableStateOf("Pembayaran dapat ditransfer melalui rekening BCA/Mandiri. Terima kasih atas kerjasamanya!")
    }

    // Calculations
    val subtotal by remember {
        derivedStateOf { itemRows.sumOf { it.customPrice * it.quantity } }
    }
    val taxAmount by remember(subtotal, taxPercent) {
        derivedStateOf { (subtotal * taxPercent) / 100 }
    }
    val grandTotal by remember(subtotal, taxAmount) {
        derivedStateOf { subtotal + taxAmount }
    }

    // Quota Exceeded Block Dialog
    if (isQuotaExceeded) {
        AlertDialog(
            onDismissRequest = onBackClick,
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = DangerRed, modifier = Modifier.size(36.dp)) },
            title = { Text("Batas Kuota Tercapai", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Anda telah mencapai batas 5 invoice per bulan untuk paket GRATIS. Upgrade ke paket Pro untuk membuat invoice tak terbatas dan membuka fitur ekspor!",
                    fontSize = 13.5.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = onNavigateToSubscription,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Lihat Paket Langganan")
                }
            },
            dismissButton = {
                TextButton(onClick = onBackClick) {
                    Text("Kembali")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick, modifier = Modifier.testTag("create_invoice_back_btn")) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = Slate900)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Buat Faktur / Invoice Baru",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Text(
                    text = "No. Otomatis: $invoiceNumber",
                    fontSize = 11.5.sp,
                    color = PrimaryBlue,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        HorizontalDivider(color = Slate200)

        // Form & Real-time Preview in Scrollable Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // STEP 1: Pilih Pelanggan (AI Auto-Fill)
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
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("1", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Informasi Pelanggan",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        }

                        TextButton(
                            onClick = { showAddCustomerDialog = true },
                            modifier = Modifier.testTag("quick_add_customer_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tambah Baru", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (customers.isEmpty()) {
                        Text(
                            text = "Belum ada data pelanggan. Klik 'Tambah Baru' di atas.",
                            fontSize = 12.5.sp,
                            color = Slate500
                        )
                    } else {
                        ExposedDropdownMenuBox(
                            expanded = customerDropdownExpanded,
                            onExpandedChange = { customerDropdownExpanded = !customerDropdownExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedCustomer?.name ?: "Pilih Pelanggan",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Pilih Pelanggan") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerDropdownExpanded) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                                    .testTag("customer_dropdown_field"),
                                shape = RoundedCornerShape(10.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = customerDropdownExpanded,
                                onDismissRequest = { customerDropdownExpanded = false }
                            ) {
                                customers.forEach { cust ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(cust.name, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                                                Text(cust.phone + (if (cust.address.isNotBlank()) " • ${cust.address}" else ""), fontSize = 11.sp, color = Slate500)
                                            }
                                        },
                                        onClick = {
                                            selectedCustomer = cust
                                            customerDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Auto-fill info card
                        selectedCustomer?.let { cust ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(PrimaryBlueLight.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("AI Terisi Otomatis:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryBlueDark)
                                    }
                                    Text("Telp: ${cust.phone.ifEmpty { "-" }}", fontSize = 11.5.sp, color = Slate700)
                                    if (cust.address.isNotBlank()) {
                                        Text("Alamat: ${cust.address}", fontSize = 11.5.sp, color = Slate700)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // STEP 2: Tanggal & Jatuh Tempo
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(PrimaryBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("2", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tanggal & Jatuh Tempo",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Tanggal Faktur", fontSize = 11.5.sp, color = Slate500, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Slate200, RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                            ) {
                                Text(Formatters.formatDate(invoiceDate), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate900)
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("Jatuh Tempo (+${dueDaysOffset} Hari)", fontSize = 11.5.sp, color = Slate500, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, PrimaryBlueLight, RoundedCornerShape(8.dp))
                                    .background(PrimaryBlueLight.copy(alpha = 0.3f))
                                    .padding(12.dp)
                            ) {
                                Text(Formatters.formatDate(dueDate), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PrimaryBlueDark)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Preset days selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(3, 7, 14, 30).forEach { days ->
                            val isSelected = dueDaysOffset == days
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) PrimaryBlue else Slate100)
                                    .clickable { dueDaysOffset = days }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "$days Hari",
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else Slate700
                                )
                            }
                        }
                    }
                }
            }

            // STEP 3: Rincian Barang / Jasa (AI Otomatis Hitung)
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
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("3", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Barang / Jasa",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        }

                        Button(
                            onClick = {
                                val prodToAdd = products.firstOrNull() ?: Product(name = "Produk Kustom", price = 100000L, unit = "Pcs")
                                itemRows.add(InvoiceItemRowState(prodToAdd, 1, prodToAdd.price))
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlueLight),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("add_item_line_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tambah Baris", color = PrimaryBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    itemRows.forEachIndexed { index, rowState ->
                        var productDropdownOpen by remember { mutableStateOf(false) }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Slate100)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Baris #${index + 1}", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = PrimaryBlueDark)
                                    if (itemRows.size > 1) {
                                        IconButton(
                                            onClick = { itemRows.removeAt(index) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = DangerRed, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Product selector
                                if (products.isNotEmpty()) {
                                    ExposedDropdownMenuBox(
                                        expanded = productDropdownOpen,
                                        onExpandedChange = { productDropdownOpen = !productDropdownOpen },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        OutlinedTextField(
                                            value = rowState.product.name,
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text("Pilih Produk/Jasa") },
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = productDropdownOpen) },
                                            modifier = Modifier
                                                .menuAnchor()
                                                .fillMaxWidth(),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                unfocusedContainerColor = Color.White,
                                                focusedContainerColor = Color.White
                                            )
                                        )
                                        ExposedDropdownMenu(
                                            expanded = productDropdownOpen,
                                            onDismissRequest = { productDropdownOpen = false }
                                        ) {
                                            products.forEach { p ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween
                                                        ) {
                                                            Text(p.name, fontSize = 13.sp)
                                                            Text(Formatters.formatRupiah(p.price), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PrimaryBlue)
                                                        }
                                                    },
                                                    onClick = {
                                                        rowState.product = p
                                                        rowState.customPrice = p.price
                                                        productDropdownOpen = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Qty
                                    OutlinedTextField(
                                        value = rowState.quantity.toString(),
                                        onValueChange = {
                                            val q = it.toIntOrNull() ?: 1
                                            rowState.quantity = q.coerceAtLeast(1)
                                        },
                                        label = { Text("Jumlah") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            unfocusedContainerColor = Color.White,
                                            focusedContainerColor = Color.White
                                        )
                                    )

                                    // Price
                                    OutlinedTextField(
                                        value = rowState.customPrice.toString(),
                                        onValueChange = {
                                            val p = it.toLongOrNull() ?: 0L
                                            rowState.customPrice = p
                                        },
                                        label = { Text("Harga (Rp)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1.5f),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            unfocusedContainerColor = Color.White,
                                            focusedContainerColor = Color.White
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Subtotal Baris:", fontSize = 11.5.sp, color = Slate600)
                                    Text(
                                        Formatters.formatRupiah(rowState.customPrice * rowState.quantity),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate900
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // STEP 4: Pajak (0 - 25%)
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Persen Pajak (PPN):", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
                            Text("$taxPercent%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                        }
                        Slider(
                            value = taxPercent.toFloat(),
                            onValueChange = { taxPercent = it.toInt() },
                            valueRange = 0f..25f,
                            steps = 24,
                            colors = SliderDefaults.colors(
                                thumbColor = PrimaryBlue,
                                activeTrackColor = PrimaryBlue
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(0, 11, 12).forEach { p ->
                                val isSelected = taxPercent == p
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) PrimaryBlue else Slate100)
                                        .clickable { taxPercent = p }
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "$p%",
                                        fontSize = 11.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else Slate700
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // STEP 5: Catatan
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Catatan & Syarat Ketentuan") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            // Real-Time Preview Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("invoice_realtime_preview_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Pratinjau Faktur (Live Preview)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Box(
                            modifier = Modifier
                                .background(SuccessGreenLight, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("SIAP CETAK", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF1F5F9), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(user?.businessName ?: "Bisnis Anda", fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = PrimaryBlueDark)
                                    Text("No: $invoiceNumber", fontSize = 11.sp, color = Slate600)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Kepada:", fontSize = 10.sp, color = Slate500)
                                    Text(selectedCustomer?.name ?: "-", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = Slate900)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = Slate300)
                            Spacer(modifier = Modifier.height(8.dp))

                            itemRows.forEach { row ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("${row.quantity}x ${row.product.name}", fontSize = 12.sp, color = Slate900)
                                    Text(Formatters.formatRupiah(row.customPrice * row.quantity), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate900)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = Slate300)
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Subtotal:", fontSize = 11.5.sp, color = Slate600)
                                Text(Formatters.formatRupiah(subtotal), fontSize = 11.5.sp, color = Slate900)
                            }
                            if (taxPercent > 0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Pajak ($taxPercent%):", fontSize = 11.5.sp, color = Slate600)
                                    Text(Formatters.formatRupiah(taxAmount), fontSize = 11.5.sp, color = Slate900)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("TOTAL AKHIR:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PrimaryBlueDark)
                                Text(
                                    Formatters.formatRupiah(grandTotal),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlue
                                )
                            }
                        }
                    }
                }
            }

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Primary Save Button
                Button(
                    onClick = {
                        val cust = selectedCustomer
                        if (cust == null) {
                            Toast.makeText(context, "Pilih pelanggan terlebih dahulu!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (itemRows.isEmpty()) {
                            Toast.makeText(context, "Tambahkan minimal 1 baris barang/jasa!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val invoice = Invoice(
                            invoiceNumber = invoiceNumber,
                            userId = user?.id ?: 1L,
                            customerId = cust.id,
                            customerName = cust.name,
                            customerPhone = cust.phone,
                            customerAddress = cust.address,
                            invoiceDate = invoiceDate,
                            dueDate = dueDate,
                            status = "Belum Dibayar",
                            notes = notes,
                            subtotal = subtotal,
                            taxPercent = taxPercent,
                            taxAmount = taxAmount,
                            grandTotal = grandTotal
                        )
                        val details = itemRows.map { r ->
                            InvoiceDetail(
                                invoiceNumber = invoiceNumber,
                                productId = r.product.id.takeIf { it > 0 },
                                productName = r.product.name,
                                unitPrice = r.customPrice,
                                quantity = r.quantity,
                                unit = r.product.unit,
                                lineSubtotal = r.customPrice * r.quantity
                            )
                        }
                        onSaveSuccess(InvoiceWithDetails(invoice, details))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_invoice_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Simpan Invoice", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // PDF Button
                    OutlinedButton(
                        onClick = {
                            val cust = selectedCustomer ?: return@OutlinedButton
                            val invoice = Invoice(
                                invoiceNumber = invoiceNumber,
                                userId = user?.id ?: 1L,
                                customerId = cust.id,
                                customerName = cust.name,
                                customerPhone = cust.phone,
                                customerAddress = cust.address,
                                invoiceDate = invoiceDate,
                                dueDate = dueDate,
                                status = "Belum Dibayar",
                                notes = notes,
                                subtotal = subtotal,
                                taxPercent = taxPercent,
                                taxAmount = taxAmount,
                                grandTotal = grandTotal
                            )
                            val details = itemRows.map { r ->
                                InvoiceDetail(
                                    invoiceNumber = invoiceNumber,
                                    productId = r.product.id.takeIf { it > 0 },
                                    productName = r.product.name,
                                    unitPrice = r.customPrice,
                                    quantity = r.quantity,
                                    unit = r.product.unit,
                                    lineSubtotal = r.customPrice * r.quantity
                                )
                            }
                            val invWithDetails = InvoiceWithDetails(invoice, details)
                            PdfExporter.generateAndSharePdf(context, user ?: User(fullName = "User", businessName = "Bisnis", email = "", whatsapp = "", password = ""), invWithDetails)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("save_pdf_invoice_btn"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Buat PDF", fontSize = 13.sp, color = PrimaryBlue)
                    }

                    // WhatsApp Button
                    OutlinedButton(
                        onClick = {
                            val cust = selectedCustomer ?: return@OutlinedButton
                            val invoice = Invoice(
                                invoiceNumber = invoiceNumber,
                                userId = user?.id ?: 1L,
                                customerId = cust.id,
                                customerName = cust.name,
                                customerPhone = cust.phone,
                                customerAddress = cust.address,
                                invoiceDate = invoiceDate,
                                dueDate = dueDate,
                                status = "Belum Dibayar",
                                notes = notes,
                                subtotal = subtotal,
                                taxPercent = taxPercent,
                                taxAmount = taxAmount,
                                grandTotal = grandTotal
                            )
                            val details = itemRows.map { r ->
                                InvoiceDetail(
                                    invoiceNumber = invoiceNumber,
                                    productId = r.product.id.takeIf { it > 0 },
                                    productName = r.product.name,
                                    unitPrice = r.customPrice,
                                    quantity = r.quantity,
                                    unit = r.product.unit,
                                    lineSubtotal = r.customPrice * r.quantity
                                )
                            }
                            val invWithDetails = InvoiceWithDetails(invoice, details)
                            WhatsAppHelper.sendInvoiceMessage(context, user ?: User(fullName = "User", businessName = "Bisnis", email = "", whatsapp = "", password = ""), invWithDetails)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("send_whatsapp_invoice_btn"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("WhatsApp", fontSize = 13.sp, color = SuccessGreen)
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Quick Add Customer Dialog
    if (showAddCustomerDialog) {
        var newCustName by remember { mutableStateOf("") }
        var newCustPhone by remember { mutableStateOf("") }
        var newCustAddress by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddCustomerDialog = false },
            title = { Text("Tambah Pelanggan Cepat", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newCustName,
                        onValueChange = { newCustName = it },
                        label = { Text("Nama Pelanggan / PT") },
                        modifier = Modifier.fillMaxWidth().testTag("quick_cust_name_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newCustPhone,
                        onValueChange = { newCustPhone = it },
                        label = { Text("No. WhatsApp / HP") },
                        modifier = Modifier.fillMaxWidth().testTag("quick_cust_phone_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                    OutlinedTextField(
                        value = newCustAddress,
                        onValueChange = { newCustAddress = it },
                        label = { Text("Alamat") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCustName.isNotBlank() && newCustPhone.isNotBlank()) {
                            val newCustomer = Customer(
                                userId = user?.id ?: 1L,
                                name = newCustName.trim(),
                                phone = newCustPhone.trim(),
                                address = newCustAddress.trim()
                            )
                            onSaveCustomerQuick(newCustomer)
                            selectedCustomer = newCustomer
                            showAddCustomerDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCustomerDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}
