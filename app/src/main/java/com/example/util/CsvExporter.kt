package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.Customer
import com.example.data.model.InvoiceWithDetails
import com.example.data.model.Product
import java.io.File
import java.io.FileWriter

object CsvExporter {

    fun exportInvoicesToCsv(context: Context, invoices: List<InvoiceWithDetails>) {
        try {
            val cachePath = File(context.cacheDir, "exports")
            cachePath.mkdirs()
            val file = File(cachePath, "Laporan_Invoice_${System.currentTimeMillis()}.csv")
            val writer = FileWriter(file)

            // CSV Header
            writer.append("No Invoice,Tanggal,Jatuh Tempo,Nama Pelanggan,No Telp,Status,Subtotal,Pajak,Total Akhir,Item Rincian\n")

            for (item in invoices) {
                val inv = item.invoice
                val itemsSummary = item.details.joinToString(";") { "${it.productName} (${it.quantity} ${it.unit})" }

                val line = listOf(
                    "\"${inv.invoiceNumber}\"",
                    "\"${Formatters.formatDate(inv.invoiceDate)}\"",
                    "\"${Formatters.formatDate(inv.dueDate)}\"",
                    "\"${inv.customerName}\"",
                    "\"${inv.customerPhone}\"",
                    "\"${inv.status}\"",
                    inv.subtotal.toString(),
                    inv.taxAmount.toString(),
                    inv.grandTotal.toString(),
                    "\"$itemsSummary\""
                ).joinToString(",")

                writer.append(line).append("\n")
            }

            writer.flush()
            writer.close()

            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "Ekspor Data Invoice (Excel/CSV)")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Ekspor ke Excel / CSV"))
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal mengekspor CSV: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    // Complete Backup of all entities to single CSV file
    fun exportAllDataToCsv(
        context: Context,
        customers: List<Customer>,
        products: List<Product>,
        invoices: List<InvoiceWithDetails>
    ) {
        try {
            val cachePath = File(context.cacheDir, "exports")
            cachePath.mkdirs()
            val file = File(cachePath, "Cadangan_Lengkap_InvoiceAI_${System.currentTimeMillis()}.csv")
            val writer = FileWriter(file)

            // Section 1: Customers
            writer.append("=== DATA PELANGGAN ===\n")
            writer.append("ID,Nama,Telepon/WA,Email,Alamat,Favorit,Total Transaksi,Total Belanja\n")
            customers.forEach { c ->
                writer.append("${c.id},\"${c.name}\",\"${c.phone}\",\"${c.email}\",\"${c.address}\",${c.isFavorite},${c.totalTransactions},${c.totalSpend}\n")
            }
            writer.append("\n")

            // Section 2: Products
            writer.append("=== DATA PRODUK & JASA ===\n")
            writer.append("ID,Nama Produk,Harga,Satuan,Kategori,Stok,Favorit,Catatan\n")
            products.forEach { p ->
                writer.append("${p.id},\"${p.name}\",${p.price},\"${p.unit}\",\"${p.category}\",${p.stock},${p.isFavorite},\"${p.notes}\"\n")
            }
            writer.append("\n")

            // Section 3: Invoices
            writer.append("=== DATA FAKTUR / INVOICE ===\n")
            writer.append("No Invoice,Tanggal,Jatuh Tempo,Pelanggan,Status,Subtotal,Pajak,Total Akhir,Diarsipkan,Item Rincian\n")
            invoices.forEach { item ->
                val inv = item.invoice
                val itemsSummary = item.details.joinToString(";") { "${it.productName} (${it.quantity} ${it.unit} x ${it.unitPrice})" }
                writer.append("\"${inv.invoiceNumber}\",\"${Formatters.formatDate(inv.invoiceDate)}\",\"${Formatters.formatDate(inv.dueDate)}\",\"${inv.customerName}\",\"${inv.status}\",${inv.subtotal},${inv.taxAmount},${inv.grandTotal},${inv.isArchived},\"$itemsSummary\"\n")
            }

            writer.flush()
            writer.close()

            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "Cadangan Lengkap Data Usaha (CSV/Excel)")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Unduh / Bagikan File Cadangan CSV"))
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal mengekspor Cadangan CSV: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}
