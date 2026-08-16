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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
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
import com.example.data.entity.ProductEntity
import com.example.ui.components.StockBadge
import com.example.ui.theme.BorderOutline
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.PurplePill
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.PurplePrimaryDark
import com.example.ui.theme.StockGreen
import com.example.ui.theme.StockRed
import java.util.Locale

@Composable
fun InventoryScreen(
    products: List<ProductEntity>,
    categories: List<String>,
    selectedCategory: String,
    searchQuery: String,
    stockFilter: String,
    currencySymbol: String,
    onSearchChange: (String) -> Unit,
    onCategorySelect: (String) -> Unit,
    onStockFilterChange: (String) -> Unit,
    onAddProductClick: () -> Unit,
    onEditProductClick: (ProductEntity) -> Unit,
    onAdjustStockClick: (ProductEntity) -> Unit,
    onDeleteProductClick: (ProductEntity) -> Unit
) {
    var productToDelete by remember { mutableStateOf<ProductEntity?>(null) }

    if (productToDelete != null) {
        val prod = productToDelete!!
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text("Eliminar Producto") },
            text = { Text("¿Está seguro de eliminar '${prod.name}'? Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteProductClick(prod)
                        productToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StockRed)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { productToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 72.dp)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Buscar en inventario (nombre, SKU, código)...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PurplePrimary) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("inventory_search_input")
            )

            // Stock Status Filter Row (Todos, Bajo Stock, Agotados)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = stockFilter == "TODOS",
                    onClick = { onStockFilterChange("TODOS") },
                    label = { Text("Todos") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PurplePill,
                        selectedLabelColor = PurplePrimaryDark
                    )
                )
                FilterChip(
                    selected = stockFilter == "BAJO_STOCK",
                    onClick = { onStockFilterChange("BAJO_STOCK") },
                    label = { Text("Bajo Stock") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PurplePill,
                        selectedLabelColor = PurplePrimaryDark
                    )
                )
                FilterChip(
                    selected = stockFilter == "AGOTADOS",
                    onClick = { onStockFilterChange("AGOTADOS") },
                    label = { Text("Agotados") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PurplePill,
                        selectedLabelColor = PurplePrimaryDark
                    )
                )
            }

            // Categories Filter Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory.isEmpty(),
                        onClick = { onCategorySelect("") },
                        label = { Text("Todas las categorías") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PurplePill,
                            selectedLabelColor = PurplePrimaryDark
                        )
                    )
                }
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { onCategorySelect(cat) },
                        label = { Text(cat) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PurplePill,
                            selectedLabelColor = PurplePrimaryDark
                        )
                    )
                }
            }

            // Product List
            if (products.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay productos que coincidan con los filtros.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(products, key = { it.id }) { product ->
                        InventoryProductCard(
                            product = product,
                            currencySymbol = currencySymbol,
                            onEdit = { onEditProductClick(product) },
                            onAdjustStock = { onAdjustStockClick(product) },
                            onDelete = { productToDelete = product }
                        )
                    }
                }
            }
        }

        // Floating Action Button to Add Product
        ExtendedFloatingActionButton(
            onClick = onAddProductClick,
            containerColor = PurplePrimary,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("add_product_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.size(8.dp))
            Text("Nuevo Producto", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun InventoryProductCard(
    product: ProductEntity,
    currencySymbol: String,
    onEdit: () -> Unit,
    onAdjustStock: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderOutline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(PurplePill)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = product.sku,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PurplePrimaryDark
                        )
                    }
                    Text(
                        text = product.category,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                StockBadge(
                    currentStock = product.currentStock,
                    minStock = product.minStock,
                    unit = product.unit
                )
            }

            Text(
                text = product.name,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (product.barcode.isNotEmpty()) {
                Text(
                    text = "Código de barras: ${product.barcode}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Financial pricing badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Precio Venta",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$currencySymbol${String.format(Locale.US, "%.2f", product.salePrice)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = PurplePrimary
                    )
                }

                Column {
                    Text(
                        text = "Costo",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$currencySymbol${String.format(Locale.US, "%.2f", product.costPrice)}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column {
                    Text(
                        text = "Margen",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "+${String.format(Locale.US, "%.1f", product.profitMarginPercent)}%",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = StockGreen
                    )
                }
            }

            // Quick Actions Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onAdjustStock,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.size(4.dp))
                    Text("Ajustar Stock", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.size(6.dp))

                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = PurplePrimary, modifier = Modifier.size(18.dp))
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = StockRed, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
