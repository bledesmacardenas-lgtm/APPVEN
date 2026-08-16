package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.SaleWithItems
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.StockGreen
import com.example.ui.theme.StockGreenBg
import com.example.ui.theme.StockRed
import com.example.ui.theme.StockRedBg
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SaleDetailDialog(
    saleWithItems: SaleWithItems,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onCancelSale: (saleId: Long, reason: String) -> Unit
) {
    val sale = saleWithItems.sale
    val items = saleWithItems.items
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault()) }
    var showCancelPrompt by remember { mutableStateOf(false) }
    var cancelReason by remember { mutableStateOf("") }

    if (showCancelPrompt) {
        AlertDialog(
            onDismissRequest = { showCancelPrompt = false },
            title = { Text("¿Anular Venta ${sale.saleNumber}?") },
            text = {
                Column {
                    Text(
                        "Esta acción revertirá las existencias de todos los productos vendidos de regreso al inventario.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = cancelReason,
                        onValueChange = { cancelReason = it },
                        label = { Text("Motivo de anulación") },
                        placeholder = { Text("Ej. Devolución de cliente, error de cobro") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCancelPrompt = false
                        onCancelSale(sale.id, cancelReason.ifBlank { "Devolución / Cancelación de venta" })
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StockRed),
                    modifier = Modifier.testTag("confirm_cancel_sale_button")
                ) {
                    Text("Anular y Restablecer Stock")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCancelPrompt = false }) {
                    Text("Volver")
                }
            }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = sale.saleNumber,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = dateFormat.format(Date(sale.timestamp)),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val isCancelled = sale.status == "ANULADA"
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isCancelled) StockRedBg else StockGreenBg)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isCancelled) "ESTADO: ANULADA (STOCK DEVUELTO)" else "ESTADO: COMPLETADA",
                        color = if (isCancelled) StockRed else StockGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Cliente: ${sale.customerName}",
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp
                )
                Text(
                    text = "Método de Pago: ${sale.paymentMethod}",
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = BorderSubtle)
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Detalle de Productos",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                items.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.productName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${item.quantity} ${item.unit} x $currencySymbol${String.format(Locale.US, "%.2f", item.unitPrice)}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "$currencySymbol${String.format(Locale.US, "%.2f", item.total)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = BorderSubtle)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Subtotal", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$currencySymbol${String.format(Locale.US, "%.2f", sale.subtotal)}", fontSize = 13.sp)
                }

                if (sale.discountAmount > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Descuento (${sale.discountPercent}%)", fontSize = 13.sp, color = StockRed)
                        Text("-$currencySymbol${String.format(Locale.US, "%.2f", sale.discountAmount)}", fontSize = 13.sp, color = StockRed)
                    }
                }

                if (sale.taxAmount > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Impuesto (${sale.taxPercent}%)", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$currencySymbol${String.format(Locale.US, "%.2f", sale.taxAmount)}", fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("TOTAL PAGADO", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("$currencySymbol${String.format(Locale.US, "%.2f", sale.total)}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                if (sale.changeGiven > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Cambio entregado", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$currencySymbol${String.format(Locale.US, "%.2f", sale.changeGiven)}", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (!isCancelled) {
                    OutlinedButton(
                        onClick = { showCancelPrompt = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StockRed),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Anular Venta y Devolver Stock")
                    }
                }
            }
        }
    }
}
