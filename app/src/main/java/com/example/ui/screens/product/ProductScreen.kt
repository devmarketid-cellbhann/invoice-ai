package com.example.ui.screens.product

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Product
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DangerRedLight
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueDark
import com.example.ui.theme.PrimaryBlueLight
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.WarningOrange
import com.example.ui.theme.WarningOrangeLight
import com.example.util.Formatters

import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder

@Composable
fun ProductScreen(
    products: List<Product>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onSaveProduct: (Product) -> Unit,
    onDeleteProduct: (Product) -> Unit,
    onToggleFavorite: (Product) -> Unit = {}
) {
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    var isAddingNew by remember { mutableStateOf(false) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }
    var selectedCategoryFilter by remember { mutableStateOf("Semua") }

    val categories = listOf("Semua", "Jasa Digital", "Jasa IT", "Desain", "Perangkat Keras", "Lainnya")

    val displayedProducts = products.filter { prod ->
        val matchesCat = if (selectedCategoryFilter == "Semua") true else prod.category.equals(selectedCategoryFilter, ignoreCase = true)
        val matchesQuery = if (searchQuery.isBlank()) true else prod.name.contains(searchQuery, ignoreCase = true) || prod.category.contains(searchQuery, ignoreCase = true)
        matchesCat && matchesQuery
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .testTag("product_screen")
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Cari produk, jasa, kategori...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Cari", tint = Slate500) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("product_search_input"),
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Category Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategoryFilter == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) PrimaryBlue else Slate100)
                            .clickable { selectedCategoryFilter = cat }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = cat,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else Slate700
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (displayedProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Belum ada produk atau jasa di kategori ini.",
                            fontSize = 14.sp,
                            color = Slate500
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { isAddingNew = true },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Text("Tambah Produk Baru")
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
                    items(displayedProducts, key = { it.id }) { product ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { editingProduct = product }
                                .testTag("product_card_${product.id}"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
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
                                        text = product.name,
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate900
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .background(Slate100, RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = product.category,
                                                fontSize = 10.5.sp,
                                                color = Slate700
                                            )
                                        }

                                        if (product.stock <= 5) {
                                            Box(
                                                modifier = Modifier
                                                    .background(DangerRedLight, RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "⚠️ Stok Sisa ${product.stock}",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = DangerRed
                                                )
                                            }
                                        } else {
                                            Text(
                                                text = "Stok: ${product.stock} ${product.unit}",
                                                fontSize = 11.sp,
                                                color = Slate500
                                            )
                                        }
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = Formatters.formatRupiah(product.price),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryBlueDark
                                    )
                                    Text(
                                        text = "/ ${product.unit}",
                                        fontSize = 11.sp,
                                        color = Slate500
                                    )

                                    Row {
                                        IconButton(
                                            onClick = { onToggleFavorite(product) },
                                            modifier = Modifier.size(28.dp).testTag("fav_product_${product.id}")
                                        ) {
                                            Icon(
                                                imageVector = if (product.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                                contentDescription = "Favorit",
                                                tint = if (product.isFavorite) Color(0xFFF59E0B) else Slate500,
                                                modifier = Modifier.size(17.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = { editingProduct = product },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                                        }
                                        IconButton(
                                            onClick = { productToDelete = product },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = DangerRed, modifier = Modifier.size(16.dp))
                                        }
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
                .testTag("add_product_fab"),
            containerColor = PrimaryBlue,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            text = { Text("Tambah Produk", fontWeight = FontWeight.Bold) }
        )
    }

    // Add / Edit Product Dialog
    if (isAddingNew || editingProduct != null) {
        val target = editingProduct
        var name by remember { mutableStateOf(target?.name ?: "") }
        var priceStr by remember { mutableStateOf(target?.price?.toString() ?: "") }
        var unit by remember { mutableStateOf(target?.unit ?: "Pcs") }
        var category by remember { mutableStateOf(target?.category ?: "Jasa Digital") }
        var stockStr by remember { mutableStateOf(target?.stock?.toString() ?: "99") }
        var notes by remember { mutableStateOf(target?.notes ?: "") }
        var error by remember { mutableStateOf("") }

        val commonUnits = listOf("Pcs", "Paket", "Bulan", "Jam", "Hari", "Unit")

        AlertDialog(
            onDismissRequest = {
                isAddingNew = false
                editingProduct = null
            },
            title = {
                Text(
                    text = if (target == null) "Tambah Produk / Jasa" else "Edit Produk / Jasa",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it; error = "" },
                        label = { Text("Nama Produk atau Jasa *") },
                        modifier = Modifier.fillMaxWidth().testTag("product_form_name"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it; error = "" },
                        label = { Text("Harga Satuan (Rp) *") },
                        modifier = Modifier.fillMaxWidth().testTag("product_form_price"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Unit selector
                    Column {
                        Text("Satuan: $unit", fontSize = 12.sp, color = Slate700, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(commonUnits) { u ->
                                val isSelected = unit == u
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) PrimaryBlue else Slate100)
                                        .clickable { unit = u }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = u,
                                        fontSize = 11.sp,
                                        color = if (isSelected) Color.White else Slate700,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Kategori") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = stockStr,
                        onValueChange = { stockStr = it },
                        label = { Text("Jumlah Stok") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Deskripsi / Catatan") },
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
                        val parsedPrice = priceStr.toLongOrNull()
                        val parsedStock = stockStr.toIntOrNull() ?: 0

                        if (name.isBlank() || parsedPrice == null) {
                            error = "Nama dan harga produk wajib diisi dengan benar!"
                            return@Button
                        }

                        val prodToSave = target?.copy(
                            name = name.trim(),
                            price = parsedPrice,
                            unit = unit,
                            category = category.trim(),
                            stock = parsedStock,
                            notes = notes.trim()
                        ) ?: Product(
                            name = name.trim(),
                            price = parsedPrice,
                            unit = unit,
                            category = category.trim(),
                            stock = parsedStock,
                            notes = notes.trim()
                        )

                        onSaveProduct(prodToSave)
                        isAddingNew = false
                        editingProduct = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    isAddingNew = false
                    editingProduct = null
                }) {
                    Text("Batal")
                }
            }
        )
    }

    // Delete confirmation
    productToDelete?.let { prod ->
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text("Hapus Produk", fontWeight = FontWeight.Bold) },
            text = { Text("Apakah Anda yakin ingin menghapus '${prod.name}' dari katalog?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteProduct(prod)
                        productToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }
}
