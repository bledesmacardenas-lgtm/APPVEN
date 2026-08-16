package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.ProductEntity
import com.example.data.model.DashboardMetrics
import com.example.data.model.ReportPeriod
import com.example.data.model.SalesChartPoint
import com.example.ui.theme.BlueMetricCard
import com.example.ui.theme.BorderOutline
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DarkChartBar
import com.example.ui.theme.DarkChartBarActive
import com.example.ui.theme.DarkChartBg
import com.example.ui.theme.OnBlueMetric
import com.example.ui.theme.OnPurpleMetric
import com.example.ui.theme.PurpleMetricCard
import com.example.ui.theme.PurplePill
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.PurplePrimaryDark
import com.example.ui.theme.StockGreen
import com.example.ui.theme.StockOrange
import com.example.ui.theme.StockOrangeBg
import com.example.ui.theme.StockRed
import com.example.ui.theme.StockRedBg
import java.util.Locale

@Composable
fun DashboardScreen(
    metrics: DashboardMetrics,
    currencySymbol: String,
    selectedPeriod: ReportPeriod,
    onPeriodSelected: (ReportPeriod) -> Unit,
    onNavigateToInventory: () -> Unit,
    onProductClick: (ProductEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Period Selector Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ReportPeriod.values().forEach { period ->
                FilterChip(
                    selected = selectedPeriod == period,
                    onClick = { onPeriodSelected(period) },
                    label = { Text(period.label, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PurplePill,
                        selectedLabelColor = PurplePrimaryDark
                    ),
                    modifier = Modifier.testTag("period_chip_${period.name}")
                )
            }
        }

        // Top Metrics Grid (Ventas Hoy + Total Productos)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Ventas Hoy Card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(130.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(BlueMetricCard)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        Icons.Default.Payments,
                        contentDescription = null,
                        tint = OnBlueMetric,
                        modifier = Modifier.size(28.dp)
                    )
                    Column {
                        Text(
                            text = "Ventas ${selectedPeriod.label}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = OnBlueMetric.copy(alpha = 0.75f)
                        )
                        Text(
                            text = "$currencySymbol${String.format(Locale.US, "%,.2f", metrics.totalSalesRevenue)}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnBlueMetric
                        )
                    }
                }
            }

            // Productos Card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(130.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(PurpleMetricCard)
                    .clickable { onNavigateToInventory() }
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        Icons.Default.Inventory2,
                        contentDescription = null,
                        tint = OnPurpleMetric,
                        modifier = Modifier.size(28.dp)
                    )
                    Column {
                        Text(
                            text = "Catálogo Total",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = OnPurpleMetric.copy(alpha = 0.75f)
                        )
                        Text(
                            text = "${metrics.totalDistinctProducts} prods",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnPurpleMetric
                        )
                    }
                }
            }
        }

        // Alertas de Stock Section
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderOutline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Alertas de Stock",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    val totalCritical = metrics.lowStockCount + metrics.outOfStockCount
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(if (totalCritical > 0) StockRedBg else PurplePill)
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (totalCritical > 0) "$totalCritical Críticos" else "Stock Óptimo",
                            color = if (totalCritical > 0) StockRed else PurplePrimaryDark,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (metrics.lowStockProductsList.isEmpty() && metrics.outOfStockCount == 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 6.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = StockGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Todos los productos cuentan con niveles de stock saludables.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    metrics.lowStockProductsList.take(3).forEach { product ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFFEF7FF))
                                .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                                .clickable { onProductClick(product) }
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (product.currentStock <= 0) StockRedBg else StockOrangeBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (product.currentStock <= 0) StockRed else StockOrange,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = product.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (product.currentStock <= 0) "¡Agotado! 0 ${product.unit} disponibles"
                                           else "Solo ${product.currentStock} ${product.unit} restantes (Mín: ${product.minStock})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (product.currentStock <= 0) StockRed else StockOrange
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "SKU: ${product.sku}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Reporte en Tiempo Real (Dark Card Container)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(DarkChartBg)
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "REPORTE EN TIEMPO REAL",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.6f),
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Tendencia de Ventas",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Interactive Chart Bars
                RealTimeBarChart(
                    points = metrics.dailyChartPoints,
                    currencySymbol = currencySymbol
                )

                // Summary Financial Highlights Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Ganancia Neta",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.65f),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "$currencySymbol${String.format(Locale.US, "%,.2f", metrics.totalNetProfit)}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF86EFAC)
                        )
                    }

                    Column {
                        Text(
                            text = "Transacciones",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.65f),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${metrics.totalTransactionsCount}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Column {
                        Text(
                            text = "Unidades Vendidas",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.65f),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${metrics.totalUnitsSold}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Top Selling Products Section
        if (metrics.topSellingProducts.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderOutline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Productos Más Vendidos",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    metrics.topSellingProducts.forEachIndexed { index, topProd ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(PurplePill),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PurplePrimaryDark
                                    )
                                }
                                Column {
                                    Text(
                                        text = topProd.productName,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${topProd.unitsSold} unidades vendidas",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Text(
                                text = "$currencySymbol${String.format(Locale.US, "%.2f", topProd.revenue)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = PurplePrimary
                            )
                        }
                    }
                }
            }
        }

        // Valor de Inventario Card
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderOutline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Valoración de Stock",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Costo: $currencySymbol${String.format(Locale.US, "%,.2f", metrics.totalInventoryValueAtCost)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Valor Comercial",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$currencySymbol${String.format(Locale.US, "%,.2f", metrics.totalInventoryValueAtRetail)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PurplePrimary
                    )
                }
            }
        }
    }
}

@Composable
fun RealTimeBarChart(
    points: List<SalesChartPoint>,
    currencySymbol: String
) {
    if (points.isEmpty()) return

    val maxAmount = points.maxOfOrNull { it.amount }?.coerceAtLeast(10.0) ?: 10.0

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            points.forEach { point ->
                val ratio = (point.amount / maxAmount).toFloat().coerceIn(0.12f, 1f)
                val isHighest = point.amount == maxAmount && point.amount > 0

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    Box(
                        modifier = Modifier
                            .width(22.dp)
                            .fillMaxHeight(ratio)
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            .background(if (isHighest) DarkChartBarActive else DarkChartBar)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            points.forEach { point ->
                Text(
                    text = point.label,
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}
