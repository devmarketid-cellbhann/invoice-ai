package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.Customer
import com.example.data.model.InvoiceWithDetails
import com.example.data.model.Product
import com.example.data.model.User
import java.io.File
import java.io.FileOutputStream

object PdfExporter {

    fun generateAndSharePdf(
        context: Context,
        user: User,
        invoiceWithDetails: InvoiceWithDetails,
        orientation: String = "PORTRAIT" // "PORTRAIT" or "LANDSCAPE"
    ) {
        val invoice = invoiceWithDetails.invoice
        val details = invoiceWithDetails.details

        val isLandscape = orientation.equals("LANDSCAPE", ignoreCase = true)
        val pageWidth = if (isLandscape) 842 else 595
        val pageHeight = if (isLandscape) 595 else 842

        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint()
        val primaryColor = Color.rgb(22, 93, 255) // #165DFF
        val darkTextColor = Color.rgb(30, 41, 59)
        val grayTextColor = Color.rgb(100, 116, 139)
        val lightBgColor = Color.rgb(241, 245, 249)

        // Draw Header background accent
        paint.color = primaryColor
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), 95f, paint)

        // Business Title
        paint.color = Color.WHITE
        paint.textSize = 22f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(user.businessName.ifEmpty { "InvoiceAI Business" }, 40f, 40f, paint)

        paint.textSize = 9.5f
        paint.typeface = Typeface.DEFAULT
        val contactLine = listOfNotNull(
            user.address.takeIf { it.isNotBlank() },
            user.whatsapp.takeIf { it.isNotBlank() }?.let { "WA: $it" },
            user.email.takeIf { it.isNotBlank() },
            user.website.takeIf { it.isNotBlank() }?.let { "Web: $it" }
        ).joinToString(" | ")
        canvas.drawText(contactLine.ifEmpty { "Sistem Faktur & Penjualan AI" }, 40f, 58f, paint)

        if (user.socialMedia.isNotBlank()) {
            canvas.drawText("Sosial Media: ${user.socialMedia}", 40f, 74f, paint)
        }

        // Top Right Invoice Label
        paint.color = Color.WHITE
        paint.textSize = 18f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("FAKTUR RESMI", (pageWidth - 40).toFloat(), 42f, paint)

        paint.textSize = 11f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText(invoice.invoiceNumber, (pageWidth - 40).toFloat(), 62f, paint)
        paint.textAlign = Paint.Align.LEFT

        // Invoice Meta Info & Customer Info
        var yPos = 120f
        val boxWidth = (pageWidth - 80).toFloat()
        val rightColX = if (isLandscape) 460f else 340f

        // Box for Customer & Invoice Details
        paint.color = lightBgColor
        canvas.drawRoundRect(40f, yPos, (pageWidth - 40).toFloat(), yPos + 82f, 8f, 8f, paint)

        paint.color = primaryColor
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("TAGIHAN KEPADA:", 55f, yPos + 22f, paint)
        canvas.drawText("INFORMASI FAKTUR:", rightColX, yPos + 22f, paint)

        paint.color = darkTextColor
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(invoice.customerName, 55f, yPos + 38f, paint)

        paint.typeface = Typeface.DEFAULT
        paint.color = grayTextColor
        paint.textSize = 9.5f
        if (invoice.customerAddress.isNotBlank()) {
            canvas.drawText(invoice.customerAddress, 55f, yPos + 53f, paint)
        }
        if (invoice.customerPhone.isNotBlank()) {
            canvas.drawText("Telp/WA: ${invoice.customerPhone}", 55f, yPos + 68f, paint)
        }

        // Meta right column
        paint.color = grayTextColor
        canvas.drawText("Tanggal Terbit: ${Formatters.formatDate(invoice.invoiceDate)}", rightColX, yPos + 38f, paint)
        canvas.drawText("Jatuh Tempo: ${Formatters.formatDate(invoice.dueDate)}", rightColX, yPos + 53f, paint)

        // Status badge
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        when (invoice.status) {
            "Sudah Dibayar" -> {
                paint.color = Color.rgb(0, 180, 42)
                canvas.drawText("Status: LUNAS (SUDAH DIBAYAR) ✅", rightColX, yPos + 68f, paint)
            }
            "Terlambat" -> {
                paint.color = Color.rgb(245, 63, 63)
                canvas.drawText("Status: TERLAMBAT JATUH TEMPO ⚠️", rightColX, yPos + 68f, paint)
            }
            else -> {
                paint.color = Color.rgb(255, 125, 0)
                canvas.drawText("Status: BELUM DIBAYAR", rightColX, yPos + 68f, paint)
            }
        }

        yPos += 105f

        // Table Header
        paint.color = Color.rgb(226, 232, 240)
        canvas.drawRect(40f, yPos, (pageWidth - 40).toFloat(), yPos + 26f, paint)

        paint.color = darkTextColor
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("NO", 50f, yPos + 17f, paint)
        canvas.drawText("DESKRIPSI PRODUK / JASA", 85f, yPos + 17f, paint)

        val qtyX = if (isLandscape) 520f else 370f
        val priceX = if (isLandscape) 660f else 460f
        val totalX = (pageWidth - 55).toFloat()

        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("QTY", qtyX, yPos + 17f, paint)
        canvas.drawText("HARGA SATUAN", priceX, yPos + 17f, paint)
        canvas.drawText("SUBTOTAL", totalX, yPos + 17f, paint)
        paint.textAlign = Paint.Align.LEFT

        yPos += 30f

        // Table Rows
        paint.typeface = Typeface.DEFAULT
        paint.textSize = 9.5f
        details.forEachIndexed { index, item ->
            paint.color = darkTextColor
            canvas.drawText("${index + 1}", 50f, yPos + 14f, paint)
            canvas.drawText(item.productName, 85f, yPos + 14f, paint)

            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText("${item.quantity} ${item.unit}", qtyX, yPos + 14f, paint)
            canvas.drawText(Formatters.formatRupiah(item.unitPrice), priceX, yPos + 14f, paint)
            canvas.drawText(Formatters.formatRupiah(item.lineSubtotal), totalX, yPos + 14f, paint)
            paint.textAlign = Paint.Align.LEFT

            // Row line
            paint.color = Color.rgb(241, 245, 249)
            canvas.drawLine(40f, yPos + 22f, (pageWidth - 40).toFloat(), yPos + 22f, paint)

            yPos += 26f
        }

        yPos += 15f

        // Summary calculations on the right
        val rightX = totalX
        val labelX = if (isLandscape) 560f else 400f

        paint.color = grayTextColor
        paint.textSize = 10f
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("Subtotal:", labelX, yPos, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(Formatters.formatRupiah(invoice.subtotal), rightX, yPos, paint)
        yPos += 18f

        if (invoice.taxPercent > 0) {
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText("${user.taxName.ifEmpty { "Pajak" }} (${invoice.taxPercent}%):", labelX, yPos, paint)
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(Formatters.formatRupiah(invoice.taxAmount), rightX, yPos, paint)
            yPos += 18f
        }

        // Total Box
        paint.color = Color.rgb(232, 243, 255)
        canvas.drawRoundRect(labelX - 10f, yPos - 5f, (pageWidth - 40).toFloat(), yPos + 28f, 6f, 6f, paint)

        paint.color = primaryColor
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 12f
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("TOTAL AKHIR:", labelX, yPos + 18f, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(Formatters.formatRupiah(invoice.grandTotal), rightX, yPos + 18f, paint)

        yPos += 45f

        // Notes & Payment Instructions (Left column)
        val noteText = invoice.notes.ifBlank { user.invoiceFooterNotes }
        if (noteText.isNotBlank()) {
            paint.textAlign = Paint.Align.LEFT
            paint.color = darkTextColor
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 10f
            canvas.drawText("Catatan & Syarat Pembayaran:", 40f, yPos, paint)

            paint.typeface = Typeface.DEFAULT
            paint.color = grayTextColor
            paint.textSize = 8.5f

            val noteLines = noteText.chunked(if (isLandscape) 70 else 45)
            var noteY = yPos + 16f
            for (line in noteLines) {
                canvas.drawText(line, 40f, noteY, paint)
                noteY += 12f
            }
        }

        // Signature & Business Stamp Block (Right column)
        val sigX = if (isLandscape) 680f else 460f
        paint.textAlign = Paint.Align.CENTER
        paint.color = darkTextColor
        paint.typeface = Typeface.DEFAULT
        paint.textSize = 9.5f
        canvas.drawText("Hormat Kami,", sigX, yPos, paint)
        canvas.drawText(user.businessName.ifEmpty { "Pihak Pengelola" }, sigX, yPos + 14f, paint)

        // Stamp / Signature placeholder box
        paint.color = Color.rgb(241, 245, 249)
        canvas.drawRoundRect(sigX - 60f, yPos + 22f, sigX + 60f, yPos + 68f, 6f, 6f, paint)
        paint.color = Color.rgb(22, 93, 255)
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        canvas.drawText("[ Stempel & TTD Digital ]", sigX, yPos + 48f, paint)

        // Signer name & role
        paint.color = darkTextColor
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 10f
        canvas.drawText(user.signatureName.ifEmpty { user.fullName }, sigX, yPos + 80f, paint)

        paint.typeface = Typeface.DEFAULT
        paint.color = grayTextColor
        paint.textSize = 8.5f
        canvas.drawText(user.signatureRole.ifEmpty { "Pimpinan Bisnis" }, sigX, yPos + 92f, paint)

        // Footer
        paint.color = Color.rgb(148, 163, 184)
        paint.textSize = 8f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("Dokumen ini sah dan diterbitkan secara digital oleh InvoiceAI Enterprise. Tanggal: ${Formatters.formatDate(System.currentTimeMillis())}", (pageWidth / 2).toFloat(), (pageHeight - 25).toFloat(), paint)

        pdfDocument.finishPage(page)

        try {
            val cachePath = File(context.cacheDir, "invoices")
            cachePath.mkdirs()
            val file = File(cachePath, "${invoice.invoiceNumber}.pdf")
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            outputStream.close()
            pdfDocument.close()

            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "Faktur Tagihan ${invoice.invoiceNumber}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Bagikan Faktur PDF ($orientation)"))
        } catch (e: Exception) {
            pdfDocument.close()
            Toast.makeText(context, "Gagal membuat PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    // Export All User Data in Single Summary PDF ("Unduh Data Saya")
    fun exportAllUserDataPdf(
        context: Context,
        user: User,
        customers: List<Customer>,
        products: List<Product>,
        invoices: List<InvoiceWithDetails>
    ) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint()
        val primaryColor = Color.rgb(22, 93, 255)
        val darkTextColor = Color.rgb(30, 41, 59)
        val grayTextColor = Color.rgb(100, 116, 139)

        // Header
        paint.color = primaryColor
        canvas.drawRect(0f, 0f, 595f, 75f, paint)

        paint.color = Color.WHITE
        paint.textSize = 18f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("RINGKASAN & CADANGAN DATA USAHA", 40f, 35f, paint)

        paint.textSize = 10f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("${user.businessName} (${user.fullName}) | Diunduh: ${Formatters.formatDate(System.currentTimeMillis())}", 40f, 55f, paint)

        var yPos = 100f

        // Stats Summary
        paint.color = Color.rgb(241, 245, 249)
        canvas.drawRoundRect(40f, yPos, 555f, yPos + 55f, 8f, 8f, paint)

        paint.color = darkTextColor
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Ringkasan Statistik Akun:", 55f, yPos + 20f, paint)

        paint.typeface = Typeface.DEFAULT
        paint.textSize = 9.5f
        paint.color = grayTextColor
        val totalRevenue = invoices.filter { it.invoice.status == "Sudah Dibayar" }.sumOf { it.invoice.grandTotal }
        canvas.drawText("• Total Pelanggan: ${customers.size} Kontak   • Total Produk/Jasa: ${products.size} Item", 55f, yPos + 35f, paint)
        canvas.drawText("• Total Faktur: ${invoices.size} Dokumen   • Total Omzet Lunas: ${Formatters.formatRupiah(totalRevenue)}", 55f, yPos + 48f, paint)

        yPos += 75f

        // Section: Invoices Summary
        paint.color = primaryColor
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("DAFTAR FAKTUR (${invoices.size})", 40f, yPos, paint)
        yPos += 12f

        paint.color = Color.rgb(226, 232, 240)
        canvas.drawRect(40f, yPos, 555f, yPos + 18f, paint)

        paint.color = darkTextColor
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("NO INVOICE", 45f, yPos + 13f, paint)
        canvas.drawText("PELANGGAN", 160f, yPos + 13f, paint)
        canvas.drawText("STATUS", 330f, yPos + 13f, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("TOTAL", 545f, yPos + 13f, paint)
        paint.textAlign = Paint.Align.LEFT
        yPos += 24f

        paint.typeface = Typeface.DEFAULT
        paint.textSize = 8.5f
        invoices.take(12).forEach { item ->
            paint.color = darkTextColor
            canvas.drawText(item.invoice.invoiceNumber, 45f, yPos, paint)
            canvas.drawText(item.invoice.customerName.take(20), 160f, yPos, paint)
            canvas.drawText(item.invoice.status, 330f, yPos, paint)
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(Formatters.formatRupiah(item.invoice.grandTotal), 545f, yPos, paint)
            paint.textAlign = Paint.Align.LEFT
            yPos += 15f
        }

        yPos += 15f

        // Section: Customers
        paint.color = primaryColor
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("DAFTAR PELANGGAN (${customers.size})", 40f, yPos, paint)
        yPos += 12f

        paint.color = Color.rgb(226, 232, 240)
        canvas.drawRect(40f, yPos, 555f, yPos + 18f, paint)

        paint.color = darkTextColor
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("NAMA PELANGGAN", 45f, yPos + 13f, paint)
        canvas.drawText("NO TELEPON / WA", 250f, yPos + 13f, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("TOTAL BELANJA", 545f, yPos + 13f, paint)
        paint.textAlign = Paint.Align.LEFT
        yPos += 24f

        customers.take(8).forEach { c ->
            paint.color = darkTextColor
            canvas.drawText(c.name, 45f, yPos, paint)
            canvas.drawText(c.phone, 250f, yPos, paint)
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(Formatters.formatRupiah(c.totalSpend), 545f, yPos, paint)
            paint.textAlign = Paint.Align.LEFT
            yPos += 15f
        }

        // Footer
        paint.color = Color.rgb(148, 163, 184)
        paint.textSize = 8f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("Dokumen ini merupakan arsip resmi data usaha Anda yang diunduh dari InvoiceAI Enterprise.", 297f, 810f, paint)

        pdfDocument.finishPage(page)

        try {
            val cachePath = File(context.cacheDir, "backups")
            cachePath.mkdirs()
            val file = File(cachePath, "Cadangan_Data_${user.businessName.replace(" ", "_")}_${System.currentTimeMillis()}.pdf")
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            outputStream.close()
            pdfDocument.close()

            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "Cadangan Data Usaha Lengkap (PDF)")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Unduh / Bagikan Cadangan Data PDF"))
        } catch (e: Exception) {
            pdfDocument.close()
            Toast.makeText(context, "Gagal membuat PDF Cadangan: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}
