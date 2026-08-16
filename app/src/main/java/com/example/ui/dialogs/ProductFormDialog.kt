package com.example.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.entity.ProductEntity
import com.example.ui.theme.PurplePrimary

@Composable
fun ProductFormDialog(
    product: ProductEntity?,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (ProductEntity) -> Unit
) {
    var sku by remember { mutableStateOf(product?.sku ?: "PROD-${System.currentTimeMillis() % 10000}") }
    var barcode by remember { mutableStateOf(product?.barcode ?: "") }
    var name by remember { mutableStateOf(product?.name ?: "") }
    var category by remember { mutableStateOf(product?.category ?: "Abarrotes") }
    var costPrice by remember { mutableStateOf(product?.costPrice?.toString() ?: "0.0") }
    var salePrice by remember { mutableStateOf(product?.salePrice?.toString() ?: "0.0") }
    var currentStock by remember { mutableStateOf(product?.currentStock?.toString() ?: "10.0") }
    var minStock by remember { mutableStateOf(product?.minStock?.toString() ?: "5.0") }
    var unit by remember { mutableStateOf(product?.unit ?: "pz") }
    var description by remember { mutableStateOf(product?.description ?: "") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

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
                    Text(
                        text = if (product == null) "Nuevo Producto" else "Editar Producto",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; errorMessage = null },
                    label = { Text("Nombre del Producto *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("product_name_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = sku,
                        onValueChange = { sku = it },
                        label = { Text("SKU / Código") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Categoría") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = barcode,
                        onValueChange = { barcode = it },
                        label = { Text("Código de Barras") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unidad (pz, kg, lt)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = costPrice,
                        onValueChange = { costPrice = it },
                        label = { Text("Costo ($currencySymbol)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = salePrice,
                        onValueChange = { salePrice = it },
                        label = { Text("Precio Venta ($currencySymbol) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = currentStock,
                        onValueChange = { currentStock = it },
                        label = { Text("Stock Actual") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = minStock,
                        onValueChange = { minStock = it },
                        label = { Text("Stock Mínimo") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción / Detalles") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
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
                            if (name.isBlank()) {
                                errorMessage = "El nombre del producto es obligatorio"
                                return@Button
                            }
                            val cost = costPrice.toDoubleOrNull() ?: 0.0
                            val price = salePrice.toDoubleOrNull()
                            if (price == null || price <= 0) {
                                errorMessage = "Ingrese un precio de venta válido mayor a 0"
                                return@Button
                            }
                            val stock = currentStock.toDoubleOrNull() ?: 0.0
                            val min = minStock.toDoubleOrNull() ?: 5.0

                            val updatedProduct = (product ?: ProductEntity(
                                sku = sku.ifBlank { "PROD-${System.currentTimeMillis() % 1000}" },
                                name = name.trim(),
                                category = category.ifBlank { "General" },
                                costPrice = cost,
                                salePrice = price,
                                currentStock = stock,
                                minStock = min,
                                unit = unit.ifBlank { "pz" },
                                barcode = barcode.trim(),
                                description = description.trim(),
                                updatedAt = System.currentTimeMillis()
                            )).copy(
                                sku = sku.ifBlank { product?.sku ?: "PROD" },
                                name = name.trim(),
                                category = category.ifBlank { "General" },
                                costPrice = cost,
                                salePrice = price,
                                currentStock = stock,
                                minStock = min,
                                unit = unit.ifBlank { "pz" },
                                barcode = barcode.trim(),
                                description = description.trim(),
                                updatedAt = System.currentTimeMillis(),
                                isSynced = false
                            )

                            onSave(updatedProduct)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("save_product_button")
                    ) {
                        Text("Guardar Producto")
                    }
                }
            }
        }
    }
}
