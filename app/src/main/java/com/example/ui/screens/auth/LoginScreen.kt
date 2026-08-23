package com.example.ui.screens.auth

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueDark
import com.example.ui.theme.PrimaryBlueLight
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate50
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate900

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onLoginAction: (String, String, (Boolean, String) -> Unit) -> Unit,
    onGoogleLoginAction: (String, String, (Boolean, String) -> Unit) -> Unit,
    onFacebookLoginAction: (String, String, (Boolean, String) -> Unit) -> Unit
) {
    var email by remember { mutableStateOf("devmarketid@gmail.com") }
    var password by remember { mutableStateOf("Subhan1211") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showGoogleDialog by remember { mutableStateOf(false) }
    var showFacebookDialog by remember { mutableStateOf(false) }
    var socialCustomEmail by remember { mutableStateOf("") }
    var socialCustomName by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate50)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .width(440.dp)
                .verticalScroll(rememberScrollState()),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(26.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // App Logo Badge
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(PrimaryBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "InvoiceAI Logo",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "InvoiceAI",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlueDark
                )

                Text(
                    text = "Masuk ke Panel Akun Anda",
                    fontSize = 13.sp,
                    color = Slate500
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Social Login Buttons for Player / User
                Text(
                    text = "MASUK SEBAGAI PENGGUNA / PLAYER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate500,
                    letterSpacing = 0.8.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Google Sign In Button
                Button(
                    onClick = {
                        socialCustomEmail = "budi.kreasidigital@gmail.com"
                        socialCustomName = "Budi Santoso"
                        showGoogleDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("google_login_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDADCE0))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Google 'G' Symbol
                        Text(
                            text = "G",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF4285F4),
                            modifier = Modifier.padding(end = 10.dp)
                        )
                        Text(
                            text = "Lanjutkan dengan Google",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF3C4043)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Facebook Sign In Button
                Button(
                    onClick = {
                        socialCustomEmail = "budi.santoso@facebook.com"
                        socialCustomName = "Budi Santoso"
                        showFacebookDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("facebook_login_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "f",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            modifier = Modifier.padding(end = 10.dp)
                        )
                        Text(
                            text = "Lanjutkan dengan Facebook",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Divider Or
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(Color(0xFFE2E8F0)))
                    Text(
                        text = " atau login manual / Admin ",
                        fontSize = 11.5.sp,
                        color = Slate500,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(Color(0xFFE2E8F0)))
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Email Field
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        errorMessage = ""
                    },
                    label = { Text("Alamat Email") },
                    placeholder = { Text("devmarketid@gmail.com") },
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = "Email", tint = PrimaryBlue)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_email_input"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        focusedLabelColor = PrimaryBlue
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Password Field
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = ""
                    },
                    label = { Text("Kata Sandi") },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = "Kata Sandi", tint = PrimaryBlue)
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (passwordVisible) "Sembunyikan" else "Tampilkan"
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_password_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        focusedLabelColor = PrimaryBlue
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                // Forgot Password Link
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(
                        onClick = onNavigateToForgotPassword,
                        modifier = Modifier.testTag("forgot_password_button")
                    ) {
                        Text(
                            text = "Lupa Kata Sandi?",
                            fontSize = 12.sp,
                            color = PrimaryBlue,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = Color(0xFFF53F3F),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Login Button
                Button(
                    onClick = {
                        if (email.isBlank() || password.isBlank()) {
                            errorMessage = "Email dan kata sandi tidak boleh kosong!"
                            return@Button
                        }
                        if (!email.contains("@") || !email.contains(".")) {
                            errorMessage = "Format email tidak valid!"
                            return@Button
                        }

                        isLoading = true
                        onLoginAction(email.trim(), password) { success, msg ->
                            isLoading = false
                            if (success) {
                                onLoginSuccess()
                            } else {
                                errorMessage = msg
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("login_submit_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                    } else {
                        Text(
                            text = "Masuk ke Akun",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quick Demo Selector for Admin & User Panel
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Slate100, RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "⚡ Akses Cepat Panel:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Button for Admin Panel (devmarketid@gmail.com / Subhan1211)
                        Button(
                            onClick = {
                                email = "devmarketid@gmail.com"
                                password = "Subhan1211"
                                isLoading = true
                                onLoginAction("devmarketid@gmail.com", "Subhan1211") { success, _ ->
                                    isLoading = false
                                    if (success) onLoginSuccess()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B), contentColor = Color.White)
                        ) {
                            Text("🛡️ Panel Admin", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // Button for User / Player Panel
                        Button(
                            onClick = {
                                email = "budi@kreasidigital.id"
                                password = "password123"
                                isLoading = true
                                onLoginAction("budi@kreasidigital.id", "password123") { success, _ ->
                                    isLoading = false
                                    if (success) onLoginSuccess()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlueLight, contentColor = PrimaryBlueDark)
                        ) {
                            Text("👤 Panel Player", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Belum punya akun? ",
                        fontSize = 13.sp,
                        color = Slate500
                    )
                    TextButton(
                        onClick = onNavigateToRegister,
                        modifier = Modifier.testTag("register_navigation_link")
                    ) {
                        Text(
                            text = "Daftar dengan OTP",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                    }
                }
            }
        }
    }

    // Google Sign In Dialog
    if (showGoogleDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showGoogleDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "G",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF4285F4),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Masuk dengan Google", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column {
                    Text("Pilih akun Google Anda atau masukkan email Google aktif:")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = socialCustomName,
                        onValueChange = { socialCustomName = it },
                        label = { Text("Nama Akun Google") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = socialCustomEmail,
                        onValueChange = { socialCustomEmail = it },
                        label = { Text("Email Google (@gmail.com)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (socialCustomEmail.isNotBlank()) {
                            showGoogleDialog = false
                            isLoading = true
                            onGoogleLoginAction(socialCustomEmail.trim(), socialCustomName.trim()) { success, _ ->
                                isLoading = false
                                if (success) onLoginSuccess()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4))
                ) {
                    Text("Lanjutkan Masuk")
                }
            },
            dismissButton = {
                TextButton(onClick = { showGoogleDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Facebook Sign In Dialog
    if (showFacebookDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showFacebookDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "f",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1877F2),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Masuk dengan Facebook", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column {
                    Text("Pilih akun Facebook Anda atau masukkan email Facebook aktif:")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = socialCustomName,
                        onValueChange = { socialCustomName = it },
                        label = { Text("Nama Profil Facebook") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = socialCustomEmail,
                        onValueChange = { socialCustomEmail = it },
                        label = { Text("Email Facebook") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (socialCustomEmail.isNotBlank()) {
                            showFacebookDialog = false
                            isLoading = true
                            onFacebookLoginAction(socialCustomEmail.trim(), socialCustomName.trim()) { success, _ ->
                                isLoading = false
                                if (success) onLoginSuccess()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2))
                ) {
                    Text("Lanjutkan Masuk")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFacebookDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}
