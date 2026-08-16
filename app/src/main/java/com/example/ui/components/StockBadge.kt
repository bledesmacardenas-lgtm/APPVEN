package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.StockGreen
import com.example.ui.theme.StockGreenBg
import com.example.ui.theme.StockOrange
import com.example.ui.theme.StockOrangeBg
import com.example.ui.theme.StockRed
import com.example.ui.theme.StockRedBg

@Composable
fun StockBadge(
    currentStock: Double,
    minStock: Double,
    unit: String = "pz",
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label) = when {
        currentStock <= 0 -> Triple(StockRedBg, StockRed, "Agotado (0 $unit)")
        currentStock <= minStock -> Triple(StockOrangeBg, StockOrange, "Bajo: $currentStock $unit")
        else -> Triple(StockGreenBg, StockGreen, "$currentStock $unit")
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(100.dp))
            .background(bgColor)
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
