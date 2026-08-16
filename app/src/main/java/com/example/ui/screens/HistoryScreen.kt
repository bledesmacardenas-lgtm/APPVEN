package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.RemoveShoppingCart
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.InventoryMovementEntity
import com.example.data.model.SaleWithItems
import com.example.ui.theme.BorderOutline
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.PurplePill
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.PurplePrimaryDark
import com.example.ui.theme.StockGreen
import com.example.ui.theme.StockGreenBg
import com.example.ui.theme.StockOrange
import com.example.ui.theme.StockOrangeBg
import com.example.ui.theme.StockRed
import com.example.ui.theme.StockRedBg
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    salesHistory: List<SaleWithItems>,
    movementsHistory: List<InventoryMovementEntity>,
    selectedTab: Int,
    currencySymbol: String,
    onTabSelected: (Int) -> Unit,
    onSaleClick: (SaleWithItems) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Tab Header: Ventas vs Kardex
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = PurplePrimary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { onTabSelected(0) },
                text = {
                    Text(
                        "Ventas Realizadas (${salesHistory.size})",
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { onTabSelected(1) },
                text = {
                    Text(
                        "Kardex / Movimientos (${movementsHistory.size})",
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
        }

        if (selectedTab == 0) {
            // Sales List Tab
            if (salesHistory.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aún no se han registrado ventas.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val dateFormat = remember { SimpleDateFormat("dd/MM/yy hh:mm a", Locale.getDefault()) }

                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(salesHistory, key = { it.sale.id }) { saleWithItems ->
                        val sale = saleWithItems.sale
                        val isCancelled = sale.status == "ANULADA"

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderOutline),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSaleClick(saleWithItems) }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(if (isCancelled) StockRedBg else PurplePill),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            if (isCancelled) Icons.Default.RemoveShoppingCart else Icons.Default.Receipt,
                                            contentDescription = null,
                                            tint = if (isCancelled) StockRed else PurplePrimaryDark,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Column {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = sale.saleNumber,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                            if (isCancelled) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(StockRedBg)
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        "ANULADA",
                                                        color = StockRed,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }

                                        Text(
                                            text = "${sale.customerName} • ${saleWithItems.items.size} prods • ${sale.paymentMethod}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = dateFormat.format(Date(sale.timestamp)),
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Text(
                                    text = "$currencySymbol${String.format(Locale.US, "%.2f", sale.total)}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCancelled) Color.Gray else PurplePrimary
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Kardex Movements Tab
            if (movementsHistory.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay registros de movimientos en el kardex.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val dateFormat = remember { SimpleDateFormat("dd/MM/yy hh:mm a", Locale.getDefault()) }

                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(movementsHistory, key = { it.id }) { movement ->
                        val isPositive = movement.quantityChange > 0
                        val (badgeBg, badgeColor, icon) = when {
                            movement.type == "ENTRADA" -> Triple(StockGreenBg, StockGreen, Icons.Default.ArrowUpward)
                            movement.type == "SALIDA_VENTA" -> Triple(PurplePill, PurplePrimaryDark, Icons.Default.ArrowDownward)
                            movement.type == "MERMA" -> Triple(StockRedBg, StockRed, Icons.Default.ArrowDownward)
                            else -> Triple(StockOrangeBg, StockOrange, Icons.Default.SyncAlt)
                        }

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(badgeBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(icon, contentDescription = null, tint = badgeColor, modifier = Modifier.size(18.dp))
                                    }

                                    Column {
                                        Text(
                                            text = movement.productName,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "${movement.type} • ${movement.reason}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = dateFormat.format(Date(movement.timestamp)),
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${if (isPositive) "+" else ""}${movement.quantityChange}",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isPositive) StockGreen else StockRed
                                    )
                                    Text(
                                        text = "Saldo: ${movement.stockAfter}",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
