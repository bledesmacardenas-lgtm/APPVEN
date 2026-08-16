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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.entity.ProductEntity
import com.example.ui.theme.PurplePill
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.PurplePrimaryDark

@Composable
fun StockAdjustmentDialog(
    product: ProductEntity,
    onDismiss: () -> Unit,
    onConfirm: (quantityDelta: Double, type: String, reason: String) -> Unit
) {
    var adjustmentType by remember { mutableStateOf("ENTRADA") } // ENTRADA, AJUSTE_NEGATIVO, MERMA
    var quantityText by remember { mutableStateOf("1.0") }
    var reason by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Ajustar Stock",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(PurplePill)
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = product.name,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = PurplePrimaryDark
                        )
                        Text(
                            text = "Stock Actual: ${product.currentStock} ${product.unit} | SKU: ${product.sku}",
                            fontSize = 12.sp,
                            color = PurplePrimaryDark.copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Tipo de Movimiento",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = adjustmentType == "ENTRADA",
                        onClick = { adjustmentType = "ENTRADA" },
                        label = { Text("+ Entrada", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PurplePill,
                            selectedLabelColor = PurplePrimaryDark
                        )
                    )
                    FilterChip(
                        selected = adjustmentType == "AJUSTE_NEGATIVO",
                        onClick = { adjustmentType = "AJUSTE_NEGATIVO" },
                        label = { Text("- Salida", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PurplePill,
                            selectedLabelColor = PurplePrimaryDark
                        )
                    )
                    FilterChip(
                        selected = adjustmentType == "MERMA",
                        onClick = { adjustmentType = "MERMA" },
                        label = { Text("Merma / Daño", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PurplePill,
                            selectedLabelColor = PurplePrimaryDark
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it; errorMessage = null },
                    label = { Text("Cantidad a ${if (adjustmentType == "ENTRADA") "Sumar" else "Restar"} (${product.unit})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("adjustment_qty_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Motivo / Observación") },
                    placeholder = { Text("Ej. Compra a proveedor, conteo físico, producto dañado") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancelar")
                    }

                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))

                    Button(
                        onClick = {
                            val qty = quantityText.toDoubleOrNull()
                            if (qty == null || qty <= 0) {
                                errorMessage = "Ingrese una cantidad válida mayor a 0"
                                return@Button
                            }

                            val delta = if (adjustmentType == "ENTRADA") qty else -qty
                            if (delta < 0 && (product.currentStock + delta) < 0) {
                                errorMessage = "El stock resultante no puede ser negativo"
                                return@Button
                            }

                            val defaultReason = when (adjustmentType) {
                                "ENTRADA" -> "Reabastecimiento de inventario"
                                "MERMA" -> "Baja por merma / vencimiento / daño"
                                else -> "Ajuste manual de inventario"
                            }

                            onConfirm(delta, adjustmentType, reason.ifBlank { defaultReason })
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("confirm_adjustment_button")
                    ) {
                        Text("Aplicar Ajuste")
                    }
                }
            }
        }
    }
}
