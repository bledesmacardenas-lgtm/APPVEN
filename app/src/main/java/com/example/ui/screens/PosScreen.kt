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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.ProductEntity
import com.example.data.model.CartItem
import com.example.ui.components.StockBadge
import com.example.ui.theme.BorderOutline
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DarkChartBg
import com.example.ui.theme.PurplePill
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.PurplePrimaryDark
import com.example.ui.theme.StockGreen
import com.example.ui.theme.StockRed
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosScreen(
    products: List<ProductEntity>,
    categories: List<String>,
    selectedCategory: String,
    searchQuery: String,
    cartItems: List<CartItem>,
    currencySymbol: String,
    taxRatePercent: Double,
    customerName: String,
    customerPhone: String,
    discountPercent: Double,
    paymentMethod: String,
    amountPaid: String,
    notes: String,
    isCheckoutOpen: Boolean,
    onSearchChange: (String) -> Unit,
    onCategorySelect: (String) -> Unit,
    onAddToCart: (ProductEntity) -> Unit,
    onUpdateQuantity: (productId: Long, quantity: Double) -> Unit,
    onRemoveFromCart: (productId: Long) -> Unit,
    onClearCart: () -> Unit,
    onOpenCheckout: () -> Unit,
    onCloseCheckout: () -> Unit,
    onCustomerNameChange: (String) -> Unit,
    onCustomerPhoneChange: (String) -> Unit,
    onDiscountChange: (Double) -> Unit,
    onPaymentMethodChange: (String) -> Unit,
    onAmountPaidChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onProcessSale: () -> Unit
) {
    val totalCartCount = cartItems.sumOf { it.quantity.toInt() }
    val cartSubtotal = cartItems.sumOf { it.total }
    val discountAmount = cartSubtotal * (discountPercent / 100.0)
    val afterDiscount = cartSubtotal - discountAmount
    val taxAmount = afterDiscount * (taxRatePercent / 100.0)
    val cartTotal = afterDiscount + taxAmount

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (cartItems.isNotEmpty()) 80.dp else 0.dp)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Buscar por nombre, SKU o código de barras...") },
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
                    .testTag("pos_search_input")
            )

            // Horizontal Categories Filter
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory.isEmpty(),
                        onClick = { onCategorySelect("") },
                        label = { Text("Todos") },
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

            // Products Grid for Quick Tap Adding
            if (products.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No se encontraron productos coincidentes",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(products, key = { it.id }) { product ->
                        PosProductCard(
                            product = product,
                            currencySymbol = currencySymbol,
                            cartQuantity = cartItems.find { it.product.id == product.id }?.quantity ?: 0.0,
                            onAddToCart = { onAddToCart(product) }
                        )
                    }
                }
            }
        }

        // Floating Sticky Bottom Cart Bar
        if (cartItems.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = DarkChartBg,
                shadowElevation = 12.dp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(PurplePrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.ShoppingCart,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "$totalCartCount artículos",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "$currencySymbol${String.format(Locale.US, "%,.2f", cartTotal)}",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Button(
                        onClick = onOpenCheckout,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.testTag("open_checkout_button")
                    ) {
                        Text(
                            text = "Cobrar",
                            color = PurplePrimaryDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }

    // Checkout Bottom Sheet
    if (isCheckoutOpen) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        ModalBottomSheet(
            onDismissRequest = onCloseCheckout,
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ticket de Venta",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onCloseCheckout) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Cart Items List
                Text(
                    text = "Productos en Carrito (${cartItems.size})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                cartItems.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFEF7FF))
                            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.product.name,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "$currencySymbol${String.format(Locale.US, "%.2f", item.customUnitPrice)} c/u",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(
                                onClick = { onUpdateQuantity(item.product.id, item.quantity - 1.0) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.Remove,
                                    contentDescription = "Restar",
                                    tint = PurplePrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Text(
                                text = "${item.quantity.toInt()}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )

                            IconButton(
                                onClick = { onUpdateQuantity(item.product.id, item.quantity + 1.0) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Sumar",
                                    tint = PurplePrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            IconButton(
                                onClick = { onRemoveFromCart(item.product.id) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Eliminar",
                                    tint = StockRed,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = BorderSubtle)
                Spacer(modifier = Modifier.height(12.dp))

                // Customer Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = onCustomerNameChange,
                        label = { Text("Nombre de Cliente") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = customerPhone,
                        onValueChange = onCustomerPhoneChange,
                        label = { Text("Teléfono") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Payment Method Selector
                Text(
                    text = "Método de Pago",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Efectivo", "Tarjeta", "Transferencia", "Crédito").forEach { method ->
                        FilterChip(
                            selected = paymentMethod == method,
                            onClick = { onPaymentMethodChange(method) },
                            label = { Text(method, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PurplePill,
                                selectedLabelColor = PurplePrimaryDark
                            )
                        )
                    }
                }

                if (paymentMethod == "Efectivo") {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = amountPaid,
                        onValueChange = onAmountPaidChange,
                        label = { Text("Monto Recibido ($currencySymbol)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("cash_received_input")
                    )

                    val paid = amountPaid.toDoubleOrNull() ?: 0.0
                    val change = if (paid > cartTotal) paid - cartTotal else 0.0
                    if (change > 0) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(StockGreen.copy(alpha = 0.1f))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Cambio a entregar:", fontWeight = FontWeight.Bold, color = StockGreen)
                            Text(
                                "$currencySymbol${String.format(Locale.US, "%.2f", change)}",
                                fontWeight = FontWeight.Bold,
                                color = StockGreen
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Summary Totals Breakdown
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Subtotal:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$currencySymbol${String.format(Locale.US, "%.2f", cartSubtotal)}", fontSize = 13.sp)
                }

                if (taxRatePercent > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Impuestos (${taxRatePercent}%):", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$currencySymbol${String.format(Locale.US, "%.2f", taxAmount)}", fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("TOTAL A COBRAR:", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "$currencySymbol${String.format(Locale.US, "%,.2f", cartTotal)}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = PurplePrimary
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onProcessSale,
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("confirm_sale_button")
                ) {
                    Icon(Icons.Default.PointOfSale, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Completar Venta y Generar Ticket",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun PosProductCard(
    product: ProductEntity,
    currencySymbol: String,
    cartQuantity: Double,
    onAddToCart: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderOutline),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = product.currentStock > 0) { onAddToCart() }
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StockBadge(
                    currentStock = product.currentStock,
                    minStock = product.minStock,
                    unit = product.unit
                )

                if (cartQuantity > 0) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(PurplePrimary)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${cartQuantity.toInt()}",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = product.name,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 2,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = product.category,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$currencySymbol${String.format(Locale.US, "%.2f", product.salePrice)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = PurplePrimary
                )

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (product.currentStock > 0) PurplePill else Color(0xFFE2E8F0)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Agregar",
                        tint = if (product.currentStock > 0) PurplePrimaryDark else Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
