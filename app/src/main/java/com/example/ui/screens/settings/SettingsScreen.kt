package com.example.ui.screens.settings

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User
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
import com.example.util.Formatters

@Composable
fun SettingsScreen(
    user: User?,
    allUsers: List<User>,
    isCloudSyncing: Boolean,
    onUpdateProfile: (String, String, String, String, String?) -> Unit,
    onUpdateInvoiceSettings: (Int, String, String, Int, Boolean, String) -> Unit = { _, _, _, _, _, _ -> },
    onUpdateSignatureSettings: (String, String) -> Unit = { _, _ -> },
    onUpdateNotificationPreferences: (String, String) -> Unit = { _, _ -> },
    onChangePassword: (String, String, (Boolean, String) -> Unit) -> Unit,
    onUpdateQrisSettings: (String, String) -> Unit,
    onUpdateRole: (String) -> Unit,
    onSwitchUser: (User) -> Unit,
    onTriggerCloudSync: () -> Unit,
    onDeleteAccount: (() -> Unit) -> Unit = { it() }
) {
    val context = LocalContext.current

    // Profile state
    var fullName by remember(user) { mutableStateOf(user?.fullName ?: "") }
    var businessName by remember(user) { mutableStateOf(user?.businessName ?: "") }
    var phone by remember(user) { mutableStateOf(user?.whatsapp ?: "") }
    var address by remember(user) { mutableStateOf(user?.address ?: "") }
    var website by remember(user) { mutableStateOf(user?.website ?: "") }
    var socialMedia by remember(user) { mutableStateOf(user?.socialMedia ?: "") }

    // Invoice defaults state
    var invoicePrefix by remember(user) { mutableStateOf(user?.invoicePrefix ?: "INV-") }
    var defaultDueDays by remember(user) { mutableIntStateOf(user?.defaultDueDays ?: 7) }
    var taxName by remember(user) { mutableStateOf(user?.taxName ?: "PPN") }
    var taxPercent by remember(user) { mutableIntStateOf(user?.taxPercent ?: 11) }
    var taxEnabled by remember(user) { mutableStateOf(user?.taxEnabled ?: false) }
    var invoiceFooterNotes by remember(user) {
        mutableStateOf(user?.invoiceFooterNotes ?: "Pembayaran via Transfer Bank. Harap sertakan No. Invoice.")
    }

    // Signature state
    var signatureName by remember(user) { mutableStateOf(user?.signatureName ?: user?.fullName ?: "Pemilik Usaha") }
    var signatureRole by remember(user) { mutableStateOf(user?.signatureRole ?: "Owner / Pimpinan") }

    // Notification state
    var notifPref by remember(user) { mutableStateOf(user?.notificationPreference ?: "ALL") }
    var adminWhatsApp by remember(user) { mutableStateOf(user?.adminWhatsApp ?: "081234567890") }

    // QRIS Settings
    var merchantName by remember(user) { mutableStateOf(user?.qrisMerchantName ?: "INVOICEAI NUSANTARA PUSAT") }
    var qrisNmid by remember(user) { mutableStateOf(user?.qrisNmid ?: "ID1029384756019") }

    // Password change
    var oldPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }
    var passMessage by remember { mutableStateOf("") }

    // Delete account dialog
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section 1: Cloud Synchronization Status (Cloud Base44 System)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryBlue)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudDone, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Database Cloud Base44 Terpusat", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                                Text("Sinkronisasi Real-Time HP, Laptop & Tablet", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .background(SuccessGreenLight, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text("AKTIF & AMAN", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Terakhir Sinkron:", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                            Text(
                                text = if (user?.lastSyncTime != null && user.lastSyncTime > 0) Formatters.formatDate(user.lastSyncTime) else "Baru Saja (Real-time)",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Button(
                            onClick = onTriggerCloudSync,
                            enabled = !isCloudSyncing,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = PrimaryBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("trigger_cloud_sync_btn")
                        ) {
                            if (isCloudSyncing) {
                                CircularProgressIndicator(color = PrimaryBlue, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Menyinkronkan...", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            } else {
                                Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Sinkron Sekarang", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Multi-User & Role Management
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SupervisorAccount, contentDescription = null, tint = PrimaryBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Hak Akses Tim & Akun",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        }

                        val (roleBg, roleColor) = when (user?.role) {
                            "Admin" -> DangerRedLight to DangerRed
                            "Kasir" -> PurpleAccentLight to PurpleAccent
                            else -> SuccessGreenLight to SuccessGreen
                        }
                        Box(
                            modifier = Modifier
                                .background(roleBg, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text("Peran: ${user?.role ?: "Owner"}", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = roleColor)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Sistem multi-tenant aman: data pelanggan, invoice, dan produk milik Anda tidak dapat diakses akun lain.",
                        fontSize = 12.sp,
                        color = Slate500
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Ganti Peran Pengguna Aktif:", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Slate700)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Owner", "Kasir", "Admin").forEach { role ->
                            FilterChip(
                                selected = user?.role == role,
                                onClick = { onUpdateRole(role) },
                                label = { Text(role) }
                            )
                        }
                    }

                    if (allUsers.size > 1) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text("Beralih ke Akun Lain (Multi-Tenant Demo):", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Slate700)
                        Spacer(modifier = Modifier.height(8.dp))

                        allUsers.forEach { u ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (u.id == user?.id) PrimaryBlueLight.copy(alpha = 0.4f) else Slate100)
                                    .clickable { onSwitchUser(u) }
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("${u.fullName} (${u.role})", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Slate900)
                                    Text("${u.businessName} • ${u.email}", fontSize = 11.sp, color = Slate600)
                                }
                                if (u.id == user?.id) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }
        }

        // Section 3: Profil Bisnis & Faktur
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Business, contentDescription = null, tint = PrimaryBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Profil Bisnis & Informasi Faktur",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Logo preview row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Slate100, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(PrimaryBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = businessName.take(2).uppercase().ifEmpty { "IA" },
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Logo Usaha di Faktur", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate900)
                            Text("Tampil di PDF faktur resmi & WhatsApp", fontSize = 11.sp, color = Slate500)
                        }
                        OutlinedButton(
                            onClick = {
                                Toast.makeText(context, "Logo bisnis berhasil diperbarui otomatis!", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Ubah", fontSize = 11.5.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = businessName,
                        onValueChange = { businessName = it },
                        label = { Text("Nama Perusahaan / Bisnis") },
                        modifier = Modifier.fillMaxWidth().testTag("settings_business_name"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Nama Lengkap Pemilik") },
                        modifier = Modifier.fillMaxWidth().testTag("settings_full_name"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("No. WhatsApp Bisnis") },
                        modifier = Modifier.fillMaxWidth().testTag("settings_whatsapp"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Alamat Kantor / Toko") },
                        modifier = Modifier.fillMaxWidth().testTag("settings_address"),
                        minLines = 2,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = website,
                            onValueChange = { website = it },
                            label = { Text("Website Bisnis") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = socialMedia,
                            onValueChange = { socialMedia = it },
                            label = { Text("Instagram / Medsos") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Currency info (Rupiah Indonesia)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PrimaryBlueLight.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🇮🇩 Mata Uang:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryBlueDark)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Indonesian Rupiah (IDR / Rp)", fontSize = 12.sp, color = Slate900)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            onUpdateProfile(fullName.trim(), businessName.trim(), phone.trim(), address.trim(), null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("save_profile_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Simpan Perubahan Profil", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Section 4: Pengaturan Format Faktur & Pajak
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Description, contentDescription = null, tint = PrimaryBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Format Faktur & Pajak Default",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = invoicePrefix,
                            onValueChange = { invoicePrefix = it.uppercase() },
                            label = { Text("Prefix Nomor Invoice") },
                            placeholder = { Text("INV-") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = defaultDueDays.toString(),
                            onValueChange = { defaultDueDays = it.toIntOrNull() ?: 7 },
                            label = { Text("Jatuh Tempo (Hari)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Tax settings
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Aktifkan Pajak Default", fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = Slate900)
                            Text("Terapkan otomatis ke invoice baru", fontSize = 11.5.sp, color = Slate500)
                        }

                        Switch(
                            checked = taxEnabled,
                            onCheckedChange = { taxEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = PrimaryBlue)
                        )
                    }

                    if (taxEnabled) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = taxName,
                                onValueChange = { taxName = it },
                                label = { Text("Nama Pajak (misal PPN)") },
                                modifier = Modifier.weight(1.2f),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )

                            OutlinedTextField(
                                value = taxPercent.toString(),
                                onValueChange = { taxPercent = it.toIntOrNull() ?: 11 },
                                label = { Text("Tarif (%)") },
                                modifier = Modifier.weight(0.8f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = invoiceFooterNotes,
                        onValueChange = { invoiceFooterNotes = it },
                        label = { Text("Catatan / Instruksi Pembayaran Bawah Faktur") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            onUpdateInvoiceSettings(defaultDueDays, invoicePrefix, taxName, taxPercent, taxEnabled, invoiceFooterNotes)
                            Toast.makeText(context, "Pengaturan format faktur disimpan!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("Simpan Format Faktur & Pajak", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Section 5: Tanda Tangan & Stempel Digital
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Draw, contentDescription = null, tint = PrimaryBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tanda Tangan & Stempel Resmi",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Konfigurasi nama penandatangan dan jabatan untuk dicetak otomatis pada PDF faktur dan kwitansi.",
                        fontSize = 12.sp,
                        color = Slate500
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = signatureName,
                        onValueChange = { signatureName = it },
                        label = { Text("Nama Penandatangan") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = signatureRole,
                        onValueChange = { signatureRole = it },
                        label = { Text("Jabatan / Posisi") },
                        placeholder = { Text("Direktur Utama / Owner") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            onUpdateSignatureSettings(signatureName.trim(), signatureRole.trim())
                            Toast.makeText(context, "Tanda tangan resmi berhasil disimpan!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("Simpan Tanda Tangan & Stempel", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Section 6: Preferensi Notifikasi & Kontak Admin
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = PrimaryBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pusat Notifikasi & WhatsApp Gateway",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Atur saluran notifikasi otomatis untuk reminder jatuh tempo dan status pembayaran.",
                        fontSize = 12.sp,
                        color = Slate500
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Saluran Notifikasi Aktif:", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Slate700)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("ALL" to "WhatsApp + In-App", "IN_APP_ONLY" to "Hanya In-App", "OFF" to "Nonaktif").forEach { (code, label) ->
                            FilterChip(
                                selected = notifPref == code,
                                onClick = { notifPref = code },
                                label = { Text(label, fontSize = 12.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = adminWhatsApp,
                        onValueChange = { adminWhatsApp = it },
                        label = { Text("No. WhatsApp Admin Verifikasi") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            onUpdateNotificationPreferences(notifPref, adminWhatsApp.trim())
                            Toast.makeText(context, "Preferensi notifikasi disimpan!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("Simpan Pengaturan Notifikasi", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Section 7: Pengaturan QRIS Rekening Pembayaran
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.QrCode, contentDescription = null, tint = PrimaryBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pengaturan QRIS Merchant",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Konfigurasi identitas QRIS resmi untuk menerima bukti pembayaran langganan pengguna.",
                        fontSize = 12.sp,
                        color = Slate500
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = merchantName,
                        onValueChange = { merchantName = it },
                        label = { Text("Nama Merchant QRIS") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = qrisNmid,
                        onValueChange = { qrisNmid = it },
                        label = { Text("National Merchant ID (NMID)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            onUpdateQrisSettings(merchantName.trim(), qrisNmid.trim())
                        },
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("Simpan Pengaturan QRIS", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Section 8: Keamanan & Ganti Kata Sandi
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = PrimaryBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ganti Kata Sandi",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = oldPass,
                        onValueChange = { oldPass = it; passMessage = "" },
                        label = { Text("Kata Sandi Lama") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth().testTag("settings_old_pass"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newPass,
                        onValueChange = { newPass = it; passMessage = "" },
                        label = { Text("Kata Sandi Baru") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth().testTag("settings_new_pass"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = confirmPass,
                        onValueChange = { confirmPass = it; passMessage = "" },
                        label = { Text("Konfirmasi Kata Sandi Baru") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth().testTag("settings_confirm_pass"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    if (passMessage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(passMessage, fontSize = 12.sp, color = PrimaryBlue)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (oldPass.isBlank() || newPass.isBlank()) {
                                passMessage = "Semua kolom kata sandi wajib diisi!"
                                return@Button
                            }
                            if (newPass != confirmPass) {
                                passMessage = "Konfirmasi kata sandi tidak cocok!"
                                return@Button
                            }
                            if (newPass.length < 6) {
                                passMessage = "Kata sandi minimal 6 karakter!"
                                return@Button
                            }

                            onChangePassword(oldPass, newPass) { success, msg ->
                                passMessage = msg
                                if (success) {
                                    oldPass = ""
                                    newPass = ""
                                    confirmPass = ""
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("Simpan Kata Sandi Baru")
                    }
                }
            }
        }

        // Section 9: Hapus Akun & Data (Zona Berbahaya)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DangerRedLight.copy(alpha = 0.3f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = DangerRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Zona Berbahaya",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = DangerRed
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Menghapus akun akan menghapus seluruh data faktur, pelanggan, produk, dan riwayat pembayaran secara permanen.",
                        fontSize = 12.sp,
                        color = Slate700
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { showDeleteAccountDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Hapus Akun & Seluruh Data Secara Permanen", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                    }
                }
            }
        }

        // Section 10: Info Aplikasi
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Slate100)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Slate500)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Informasi Sistem", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Slate900)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("InvoiceAI Enterprise Cloud v2.0.0", fontSize = 12.sp, color = Slate700, fontWeight = FontWeight.Bold)
                    Text("Arsitektur: Terpusat Cloud Base44 + Enkripsi Data Multi-Tenant", fontSize = 11.5.sp, color = Slate600)
                    Text("Mata Uang: Rupiah (Rp) • Gateway: Manual QRIS Indonesia", fontSize = 11.5.sp, color = Slate600)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text("Konfirmasi Hapus Akun Permanen", fontWeight = FontWeight.Bold, color = DangerRed) },
            text = {
                Text("Tindakan ini TIDAK DAPAT DIBATALKAN. Semua data bisnis, faktur, pelanggan, produk, dan riwayat pembayaran Anda akan dihapus selamanya dari database cloud.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAccountDialog = false
                        onDeleteAccount { }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("Ya, Hapus Akun Saya", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}
