package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.data.model.Customer
import com.example.data.model.InvoiceWithDetails
import com.example.data.model.Payment
import com.example.data.model.Product
import com.example.data.model.User
import java.net.URLEncoder

object WhatsAppHelper {

    fun sendInvoiceMessage(context: Context, user: User, invoiceWithDetails: InvoiceWithDetails) {
        val invoice = invoiceWithDetails.invoice
        val details = invoiceWithDetails.details

        val sb = StringBuilder()
        sb.append("🧾 *FAKTUR TAGIHAN / INVOICE*\n")
        sb.append("*${user.businessName.ifEmpty { "InvoiceAI" }}*\n")
        if (user.address.isNotBlank()) sb.append("📍 ${user.address}\n")
        if (user.whatsapp.isNotBlank()) sb.append("📞 WhatsApp: ${user.whatsapp}\n")
        if (user.website.isNotBlank()) sb.append("🌐 Web: ${user.website}\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━\n\n")

        sb.append("Kepada Yth:\n")
        sb.append("*${invoice.customerName}*\n")
        if (invoice.customerAddress.isNotBlank()) sb.append("Alamat: ${invoice.customerAddress}\n")
        sb.append("\n")

        sb.append("📋 *No. Invoice:* ${invoice.invoiceNumber}\n")
        sb.append("📅 *Tanggal:* ${Formatters.formatDate(invoice.invoiceDate)}\n")
        sb.append("⏰ *Jatuh Tempo:* ${Formatters.formatDate(invoice.dueDate)}\n")
        sb.append("📌 *Status:* ${invoice.status.uppercase()}\n\n")

        sb.append("📦 *RINCIAN BARANG / JASA:*\n")
        details.forEachIndexed { index, item ->
            sb.append("${index + 1}. *${item.productName}*\n")
            sb.append("   ${item.quantity} ${item.unit} × ${Formatters.formatRupiah(item.unitPrice)} = *${Formatters.formatRupiah(item.lineSubtotal)}*\n")
        }
        sb.append("━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("Subtotal: ${Formatters.formatRupiah(invoice.subtotal)}\n")
        if (invoice.taxPercent > 0) {
            sb.append("${user.taxName.ifEmpty { "Pajak" }} (${invoice.taxPercent}%): ${Formatters.formatRupiah(invoice.taxAmount)}\n")
        }
        sb.append("💰 *TOTAL AKHIR: ${Formatters.formatRupiah(invoice.grandTotal)}*\n\n")

        if (invoice.notes.isNotBlank()) {
            sb.append("📝 *Catatan & Instruksi Pembayaran:*\n${invoice.notes}\n\n")
        } else if (user.invoiceFooterNotes.isNotBlank()) {
            sb.append("📝 *Instruksi Pembayaran:*\n${user.invoiceFooterNotes}\n\n")
        }

        sb.append("Terima kasih atas kepercayaan dan kerjasamanya! 🙏\n")
        sb.append("_Dibuat otomatis dengan sistem InvoiceAI Enterprise_")

        openWhatsApp(context, invoice.customerPhone, sb.toString())
    }

    enum class ReminderTiming(val title: String) {
        BEFORE_3_DAYS("Pengingat H-3 Sebelum Jatuh Tempo"),
        ON_DUE_DATE("Pengingat Hari-H Jatuh Tempo"),
        AFTER_OVERDUE("Pemberitahuan Terlambat / Melewati Jatuh Tempo")
    }

    fun sendScheduledReminder(
        context: Context,
        user: User,
        invoiceWithDetails: InvoiceWithDetails,
        timing: ReminderTiming
    ) {
        val invoice = invoiceWithDetails.invoice
        val sb = StringBuilder()

        when (timing) {
            ReminderTiming.BEFORE_3_DAYS -> {
                sb.append("⏰ *PENGINGAT JATUH TEMPO (H-3)*\n")
                sb.append("*${user.businessName.ifEmpty { "InvoiceAI" }}*\n")
                sb.append("━━━━━━━━━━━━━━━━━━━━\n\n")
                sb.append("Halo Yth. *${invoice.customerName}*,\n\n")
                sb.append("Kami ingin menginformasikan bahwa faktur *${invoice.invoiceNumber}* sebesar *${Formatters.formatRupiah(invoice.grandTotal)}* akan jatuh tempo pada *${Formatters.formatDate(invoice.dueDate)}* (3 hari lagi).\n\n")
                sb.append("Mohon dapat dipersiapkan pembayarannya sebelum tanggal tersebut. Terima kasih banyak atas kerjasamanya! 🙏\n\n")
            }
            ReminderTiming.ON_DUE_DATE -> {
                sb.append("🔔 *PENGINGAT PEMBAYARAN HARI INI*\n")
                sb.append("*${user.businessName.ifEmpty { "InvoiceAI" }}*\n")
                sb.append("━━━━━━━━━━━━━━━━━━━━\n\n")
                sb.append("Halo Yth. *${invoice.customerName}*,\n\n")
                sb.append("Hari ini adalah batas waktu jatuh tempo untuk invoice *${invoice.invoiceNumber}* sebesar *${Formatters.formatRupiah(invoice.grandTotal)}*.\n\n")
                sb.append("Mohon konfirmasikan pembayaran Anda jika sudah melakukan transfer. Terima kasih! 🙏\n\n")
            }
            ReminderTiming.AFTER_OVERDUE -> {
                sb.append("⚠️ *PEMBERITAHUAN KETERLAMBATAN PEMBAYARAN*\n")
                sb.append("*${user.businessName.ifEmpty { "InvoiceAI" }}*\n")
                sb.append("━━━━━━━━━━━━━━━━━━━━\n\n")
                sb.append("Halo Yth. *${invoice.customerName}*,\n\n")
                sb.append("Tagihan Anda dengan No. Faktur *${invoice.invoiceNumber}* sebesar *${Formatters.formatRupiah(invoice.grandTotal)}* telah melewati tanggal jatuh tempo (${Formatters.formatDate(invoice.dueDate)}).\n\n")
                sb.append("Mohon kesediaannya untuk segera menyelesaikan pembayaran demi kelancaran administrasi dan operasional bersama. 🙏\n\n")
            }
        }

        sb.append("Salam hangat,\n*${user.businessName}*")
        openWhatsApp(context, invoice.customerPhone, sb.toString())
    }

    fun sendPaymentReceipt(context: Context, user: User, invoiceWithDetails: InvoiceWithDetails) {
        val invoice = invoiceWithDetails.invoice
        val sb = StringBuilder()
        sb.append("✅ *KUITANSI PEMBAYARAN LUNAS*\n")
        sb.append("*${user.businessName.ifEmpty { "InvoiceAI" }}*\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━\n\n")
        sb.append("Kepada Yth. *${invoice.customerName}*,\n\n")
        sb.append("Terima kasih! Pembayaran untuk faktur berikut telah kami terima dan berstatus *LUNAS*:\n\n")
        sb.append("• *No. Invoice:* ${invoice.invoiceNumber}\n")
        sb.append("• *Tanggal Bayar:* ${Formatters.formatDate(invoice.paidDate ?: System.currentTimeMillis())}\n")
        sb.append("• *Jumlah Dibayar:* *${Formatters.formatRupiah(invoice.grandTotal)}*\n")
        sb.append("• *Status:* SUDAH LUNAS ✅\n\n")
        sb.append("Senang dapat bekerja sama dengan Anda. Semoga sukses selalu untuk bisnis Anda! 🙏\n\n")
        sb.append("Salam,\n*${user.businessName}*")

        openWhatsApp(context, invoice.customerPhone, sb.toString())
    }

    fun sendProductCatalog(context: Context, user: User, customerPhone: String, products: List<Product>) {
        val sb = StringBuilder()
        sb.append("📋 *KATALOG PRODUK & LAYANAN*\n")
        sb.append("*${user.businessName.ifEmpty { "InvoiceAI" }}*\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━\n\n")
        sb.append("Berikut daftar produk dan layanan resmi yang kami sediakan:\n\n")

        products.forEachIndexed { idx, p ->
            sb.append("${idx + 1}. *${p.name}*\n")
            sb.append("   • Kategori: ${p.category}\n")
            sb.append("   • Harga: *${Formatters.formatRupiah(p.price)}* / ${p.unit}\n")
            if (p.notes.isNotBlank()) sb.append("   • Keterangan: ${p.notes}\n")
            sb.append("\n")
        }

        sb.append("Untuk pemesanan atau informasi lebih lanjut, silakan balas pesan ini langsung. Terima kasih! 🙏\n")
        openWhatsApp(context, customerPhone, sb.toString())
    }

    // WhatsApp notification to User on Payment Approved
    fun sendPaymentApprovedToUser(context: Context, user: User, plan: String, expiryDate: Long) {
        val message = "Halo ${user.fullName} ✅ Pembayaran Anda diterima! Paket [${plan}] aktif sampai ${Formatters.formatDate(expiryDate)}. Selamat menggunakan semua fitur tak terbatas InvoiceAI!"
        openWhatsApp(context, user.whatsapp, message)
    }

    // WhatsApp notification to User on Payment Rejected
    fun sendPaymentRejectedToUser(context: Context, user: User, reason: String) {
        val message = "Halo ${user.fullName} ❌ Pembayaran QRIS Anda ditolak. Alasan: ${reason.ifBlank { "Bukti bayar tidak valid atau nominal tidak sesuai" }}. Silakan bayar sesuai nominal & kirim ulang bukti via aplikasi."
        openWhatsApp(context, user.whatsapp, message)
    }

    // WhatsApp notification to User on 3 days before expiration
    fun sendPackageExpiringToUser(context: Context, user: User, daysLeft: Int) {
        val message = "Halo ${user.fullName} ⚠️ Paket ${user.packageTier} Anda akan habis dalam $daysLeft hari! Segera perpanjang agar tetap bisa menggunakan seluruh fitur tanpa batas."
        openWhatsApp(context, user.whatsapp, message)
    }

    // WhatsApp notification to User on invoice created
    fun sendInvoiceCreatedToUser(context: Context, user: User, invoiceNumber: String, grandTotal: Long) {
        val message = "Halo ${user.fullName} ✅ Invoice [$invoiceNumber] berhasil dibuat. Total ${Formatters.formatRupiah(grandTotal)}. Silakan bagikan ke pelanggan Anda."
        openWhatsApp(context, user.whatsapp, message)
    }

    // WhatsApp notification to Admin on New Payment Upload
    fun sendQrisConfirmationToAdmin(context: Context, adminPhone: String, user: User, payment: Payment) {
        val sb = StringBuilder()
        sb.append("🔴 *PEMBAYARAN BARU MASUK*\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("• *Nama:* ${user.fullName}\n")
        sb.append("• *Bisnis:* ${user.businessName}\n")
        sb.append("• *Paket:* ${payment.planPurchased}\n")
        sb.append("• *Nominal:* ${Formatters.formatRupiah(payment.amountPaid)}\n")
        sb.append("• *Pengirim:* ${payment.senderName} (${payment.senderBank})\n")
        sb.append("• *Ref Code:* ${payment.referenceCode}\n")
        if (payment.mismatchAmountWarning) sb.append("⚠️ *PERINGATAN:* Nominal berbeda dari harga paket resmi!\n")
        sb.append("\nSilakan periksa di Panel Admin Verifikasi InvoiceAI.")

        openWhatsApp(context, adminPhone, sb.toString())
    }

    // WhatsApp notification to Admin on New User Registration
    fun sendNewUserToAdmin(context: Context, adminPhone: String, newUser: User) {
        val sb = StringBuilder()
        sb.append("🟢 *PENGGUNA BARU DAFTAR*\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("• *Nama:* ${newUser.fullName}\n")
        sb.append("• *Bisnis:* ${newUser.businessName}\n")
        sb.append("• *Email:* ${newUser.email}\n")
        sb.append("• *WhatsApp:* ${newUser.whatsapp}\n")
        sb.append("• *Tanggal:* ${Formatters.formatDate(newUser.createdAt)}")

        openWhatsApp(context, adminPhone, sb.toString())
    }

    // WhatsApp message to Admin on issue / user support
    fun contactAdminSupport(context: Context, adminPhone: String, user: User, issueText: String = "") {
        val sb = StringBuilder()
        sb.append("Halo Admin InvoiceAI,\n")
        sb.append("Saya *${user.fullName}* (${user.businessName})\n")
        sb.append("Email: ${user.email} | HP: ${user.whatsapp}\n\n")
        if (issueText.isNotBlank()) {
            sb.append("Butuh bantuan terkait:\n$issueText\n\n")
        } else {
            sb.append("Saya membutuhkan bantuan mengenai aplikasi InvoiceAI. Mohon panduannya.\n\n")
        }
        sb.append("Terima kasih! 🙏")

        openWhatsApp(context, adminPhone, sb.toString())
    }

    fun openWhatsApp(context: Context, phoneNumber: String, message: String) {
        try {
            var formattedPhone = phoneNumber.replace(Regex("[^0-9]"), "")
            if (formattedPhone.startsWith("0")) {
                formattedPhone = "62" + formattedPhone.substring(1)
            }

            val encodedMessage = URLEncoder.encode(message, "UTF-8")
            val uriString = if (formattedPhone.isNotBlank()) {
                "https://api.whatsapp.com/send?phone=$formattedPhone&text=$encodedMessage"
            } else {
                "https://api.whatsapp.com/send?text=$encodedMessage"
            }

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriString))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Kirim via WhatsApp"))
        }
    }
}
