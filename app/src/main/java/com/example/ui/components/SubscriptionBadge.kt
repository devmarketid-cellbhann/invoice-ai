package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueLight
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.PurpleAccentLight
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate700

@Composable
fun SubscriptionBadge(
    tier: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label) = when (tier.lowercase()) {
        "bisnis" -> Triple(PurpleAccentLight, PurpleAccent, "🏢 BISNIS")
        "pro" -> Triple(PrimaryBlueLight, PrimaryBlue, "💎 PRO")
        else -> Triple(Slate100, Slate700, "🆓 GRATIS")
    }

    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
