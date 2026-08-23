package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueDark
import com.example.ui.theme.PrimaryBlueLight
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate900
import com.example.util.Formatters

data class MonthlyBarData(
    val monthName: String,
    val revenue: Long,
    val invoiceCount: Int
)

@Composable
fun RevenueChart(
    monthlyData: List<MonthlyBarData>,
    modifier: Modifier = Modifier
) {
    val maxRevenue = monthlyData.maxOfOrNull { it.revenue }?.coerceAtLeast(1000000L) ?: 10000000L

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("revenue_chart_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Grafik Omzet & Tren Penjualan",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Text(
                        text = "Performa 6 bulan terakhir",
                        fontSize = 11.5.sp,
                        color = Slate500
                    )
                }

                Box(
                    modifier = Modifier
                        .background(PrimaryBlueLight, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Rupiah (Rp)",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Canvas Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val w = size.width
                    val h = size.height
                    val barCount = monthlyData.size
                    if (barCount == 0) return@Canvas

                    val barWidth = (w / barCount) * 0.45f
                    val slotWidth = w / barCount

                    // Draw subtle grid lines
                    val gridLines = 3
                    for (i in 1..gridLines) {
                        val y = (h / gridLines) * i
                        drawLine(
                            color = Slate200,
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1f
                        )
                    }

                    // Draw Bars
                    monthlyData.forEachIndexed { index, item ->
                        val ratio = (item.revenue.toFloat() / maxRevenue.toFloat()).coerceIn(0.08f, 1f)
                        val barHeight = h * ratio
                        val x = (index * slotWidth) + (slotWidth - barWidth) / 2f
                        val y = h - barHeight

                        // Bar gradient
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(PrimaryBlue, PrimaryBlueDark)
                            ),
                            topLeft = Offset(x, y),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(8f, 8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Month labels under bars
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                monthlyData.forEach { item ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = item.monthName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Slate500
                        )
                        Text(
                            text = if (item.revenue >= 1_000_000) "${item.revenue / 1_000_000}jt" else "${item.revenue / 1_000}rb",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    }
                }
            }
        }
    }
}
