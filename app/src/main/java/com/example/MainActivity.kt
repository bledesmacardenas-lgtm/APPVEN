package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppDatabase
import com.example.data.repository.InventoryRepository
import com.example.ui.dialogs.JsonExportDialog
import com.example.ui.dialogs.JsonImportDialog
import com.example.ui.dialogs.ProductFormDialog
import com.example.ui.dialogs.ReceiptDialog
import com.example.ui.dialogs.SaleDetailDialog
import com.example.ui.dialogs.StockAdjustmentDialog
import com.example.ui.screens.CloudSyncScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.PosScreen
import com.example.ui.theme.DarkChartBg
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NavBackground
import com.example.ui.theme.NavIndicator
import com.example.ui.theme.PurplePill
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.PurplePrimaryDark
import com.example.ui.theme.WarmBackground
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.InventoryViewModel
import com.example.ui.viewmodel.InventoryViewModelFactory
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getInstance(applicationContext)
        val repository = InventoryRepository(database)
        val viewModelFactory = InventoryViewModelFactory(repository)

        setContent {
            MyApplicationTheme {
                val viewModel: InventoryViewModel = viewModel(factory = viewModelFactory)
                MainInventoryApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainInventoryApp(viewModel: InventoryViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val dashboardMetrics by viewModel.dashboardMetrics.collectAsState()
    val filteredProducts by viewModel.filteredProducts.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val stockFilter by viewModel.stockFilter.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val isCheckoutOpen by viewModel.isCheckoutOpen.collectAsState()
    val customerName by viewModel.customerName.collectAsState()
    val customerPhone by viewModel.customerPhone.collectAsState()
    val discountPercent by viewModel.discountPercent.collectAsState()
    val paymentMethod by viewModel.paymentMethod.collectAsState()
    val amountPaid by viewModel.amountPaid.collectAsState()
    val saleNotes by viewModel.saleNotes.collectAsState()
    val salesHistory by viewModel.salesHistory.collectAsState()
    val movementsHistory by viewModel.movementsHistory.collectAsState()
    val historyTab by viewModel.historyTab.collectAsState()
    val syncState by viewModel.syncState.collectAsState()
    val storeSettings by viewModel.storeSettings.collectAsState()
    val selectedReportPeriod by viewModel.selectedReportPeriod.collectAsState()

    // Dialog state collectors
    val isProductFormOpen by viewModel.isProductFormOpen.collectAsState()
    val editingProduct by viewModel.editingProduct.collectAsState()
    val isStockAdjustOpen by viewModel.isStockAdjustOpen.collectAsState()
    val adjustingProduct by viewModel.adjustingProduct.collectAsState()
    val activeSaleDetail by viewModel.activeSaleDetail.collectAsState()
    val lastCompletedSaleWithItems by viewModel.lastCompletedSaleWithItems.collectAsState()
    val isExportJsonOpen by viewModel.isExportJsonOpen.collectAsState()
    val exportJsonContent by viewModel.exportJsonContent.collectAsState()
    val isImportJsonOpen by viewModel.isImportJsonOpen.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = WarmBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = when (currentTab) {
                                AppTab.DASHBOARD -> "Panel de Control"
                                AppTab.POS -> "Punto de Venta"
                                AppTab.INVENTORY -> "Inventario de Productos"
                                AppTab.HISTORY -> "Historial y Movimientos"
                                AppTab.SETTINGS_CLOUD -> "Nube y Ajustes"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    // Quick cloud sync icon badge
                    IconButton(
                        onClick = { viewModel.triggerManualSync() },
                        modifier = Modifier.testTag("topbar_sync_button")
                    ) {
                        if (syncState.isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = PurplePrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            BadgedBox(
                                badge = {
                                    if (syncState.pendingSyncCount > 0) {
                                        Badge(
                                            containerColor = PurplePrimaryDark,
                                            contentColor = Color.White
                                        ) {
                                            Text("${syncState.pendingSyncCount}")
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Default.CloudSync,
                                    contentDescription = "Sincronizar",
                                    tint = PurplePrimary
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = WarmBackground
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = NavBackground,
                modifier = Modifier.testTag("main_bottom_nav")
            ) {
                // Inicio / Dashboard
                NavigationBarItem(
                    selected = currentTab == AppTab.DASHBOARD,
                    onClick = { viewModel.setTab(AppTab.DASHBOARD) },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = NavIndicator,
                        selectedIconColor = PurplePrimaryDark,
                        selectedTextColor = PurplePrimaryDark
                    ),
                    modifier = Modifier.testTag("nav_dashboard")
                )

                // POS / Vender
                val cartCount = cartItems.sumOf { it.quantity.toInt() }
                NavigationBarItem(
                    selected = currentTab == AppTab.POS,
                    onClick = { viewModel.setTab(AppTab.POS) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (cartCount > 0) {
                                    Badge(
                                        containerColor = PurplePrimary,
                                        contentColor = Color.White
                                    ) {
                                        Text("$cartCount")
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.PointOfSale, contentDescription = "POS")
                        }
                    },
                    label = { Text("Vender", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = NavIndicator,
                        selectedIconColor = PurplePrimaryDark,
                        selectedTextColor = PurplePrimaryDark
                    ),
                    modifier = Modifier.testTag("nav_pos")
                )

                // Inventario
                val lowStockTotal = dashboardMetrics.lowStockCount + dashboardMetrics.outOfStockCount
                NavigationBarItem(
                    selected = currentTab == AppTab.INVENTORY,
                    onClick = { viewModel.setTab(AppTab.INVENTORY) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (lowStockTotal > 0) {
                                    Badge(
                                        containerColor = Color(0xFFB91C1C),
                                        contentColor = Color.White
                                    ) {
                                        Text("$lowStockTotal")
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Inventory2, contentDescription = "Inventario")
                        }
                    },
                    label = { Text("Stock", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = NavIndicator,
                        selectedIconColor = PurplePrimaryDark,
                        selectedTextColor = PurplePrimaryDark
                    ),
                    modifier = Modifier.testTag("nav_inventory")
                )

                // Historial / Kardex
                NavigationBarItem(
                    selected = currentTab == AppTab.HISTORY,
                    onClick = { viewModel.setTab(AppTab.HISTORY) },
                    icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Historial") },
                    label = { Text("Historial", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = NavIndicator,
                        selectedIconColor = PurplePrimaryDark,
                        selectedTextColor = PurplePrimaryDark
                    ),
                    modifier = Modifier.testTag("nav_history")
                )

                // Sincronización / Ajustes
                NavigationBarItem(
                    selected = currentTab == AppTab.SETTINGS_CLOUD,
                    onClick = { viewModel.setTab(AppTab.SETTINGS_CLOUD) },
                    icon = { Icon(Icons.Default.CloudDone, contentDescription = "Ajustes") },
                    label = { Text("Nube", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = NavIndicator,
                        selectedIconColor = PurplePrimaryDark,
                        selectedTextColor = PurplePrimaryDark
                    ),
                    modifier = Modifier.testTag("nav_cloud")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppTab.DASHBOARD -> DashboardScreen(
                    metrics = dashboardMetrics,
                    currencySymbol = storeSettings.currencySymbol,
                    selectedPeriod = selectedReportPeriod,
                    onPeriodSelected = { viewModel.setReportPeriod(it) },
                    onNavigateToInventory = { viewModel.setTab(AppTab.INVENTORY) },
                    onProductClick = { viewModel.openStockAdjustment(it) }
                )
                AppTab.POS -> PosScreen(
                    products = filteredProducts,
                    categories = categories,
                    selectedCategory = selectedCategory,
                    searchQuery = searchQuery,
                    cartItems = cartItems,
                    currencySymbol = storeSettings.currencySymbol,
                    taxRatePercent = storeSettings.taxRatePercent,
                    customerName = customerName,
                    customerPhone = customerPhone,
                    discountPercent = discountPercent,
                    paymentMethod = paymentMethod,
                    amountPaid = amountPaid,
                    notes = saleNotes,
                    isCheckoutOpen = isCheckoutOpen,
                    onSearchChange = { viewModel.setSearchQuery(it) },
                    onCategorySelect = { viewModel.setSelectedCategory(it) },
                    onAddToCart = { viewModel.addToCart(it) },
                    onUpdateQuantity = { id, qty -> viewModel.updateCartQuantity(id, qty) },
                    onRemoveFromCart = { viewModel.removeFromCart(it) },
                    onClearCart = { viewModel.clearCart() },
                    onOpenCheckout = { viewModel.openCheckout() },
                    onCloseCheckout = { viewModel.closeCheckout() },
                    onCustomerNameChange = { viewModel.setCustomerName(it) },
                    onCustomerPhoneChange = { viewModel.setCustomerPhone(it) },
                    onDiscountChange = { viewModel.setDiscountPercent(it) },
                    onPaymentMethodChange = { viewModel.setPaymentMethod(it) },
                    onAmountPaidChange = { viewModel.setAmountPaid(it) },
                    onNotesChange = { viewModel.setSaleNotes(it) },
                    onProcessSale = { viewModel.processSale() }
                )
                AppTab.INVENTORY -> InventoryScreen(
                    products = filteredProducts,
                    categories = categories,
                    selectedCategory = selectedCategory,
                    searchQuery = searchQuery,
                    stockFilter = stockFilter,
                    currencySymbol = storeSettings.currencySymbol,
                    onSearchChange = { viewModel.setSearchQuery(it) },
                    onCategorySelect = { viewModel.setSelectedCategory(it) },
                    onStockFilterChange = { viewModel.setStockFilter(it) },
                    onAddProductClick = { viewModel.openProductForm(null) },
                    onEditProductClick = { viewModel.openProductForm(it) },
                    onAdjustStockClick = { viewModel.openStockAdjustment(it) },
                    onDeleteProductClick = { viewModel.deleteProduct(it) }
                )
                AppTab.HISTORY -> HistoryScreen(
                    salesHistory = salesHistory,
                    movementsHistory = movementsHistory,
                    selectedTab = historyTab,
                    currencySymbol = storeSettings.currencySymbol,
                    onTabSelected = { viewModel.setHistoryTab(it) },
                    onSaleClick = { viewModel.openSaleDetail(it) }
                )
                AppTab.SETTINGS_CLOUD -> CloudSyncScreen(
                    syncState = syncState,
                    settings = storeSettings,
                    onManualSync = { viewModel.triggerManualSync() },
                    onSaveSettings = { viewModel.updateSettings(it) },
                    onExportJson = { viewModel.openExportJson() },
                    onImportJson = { viewModel.openImportJson() },
                    onResetDemoData = { viewModel.resetDemoData() }
                )
            }
        }
    }

    // Modal Dialogs
    if (isProductFormOpen) {
        ProductFormDialog(
            product = editingProduct,
            currencySymbol = storeSettings.currencySymbol,
            onDismiss = { viewModel.closeProductForm() },
            onSave = { product -> viewModel.saveProduct(product) }
        )
    }

    if (isStockAdjustOpen && adjustingProduct != null) {
        StockAdjustmentDialog(
            product = adjustingProduct!!,
            onDismiss = { viewModel.closeStockAdjustment() },
            onConfirm = { delta, type, reason ->
                viewModel.applyStockAdjustment(adjustingProduct!!, delta, type, reason)
            }
        )
    }

    if (activeSaleDetail != null) {
        SaleDetailDialog(
            saleWithItems = activeSaleDetail!!,
            currencySymbol = storeSettings.currencySymbol,
            onDismiss = { viewModel.closeSaleDetail() },
            onCancelSale = { saleId, reason ->
                viewModel.cancelSale(saleId, reason)
            }
        )
    }

    if (lastCompletedSaleWithItems != null) {
        val (sale, items) = lastCompletedSaleWithItems!!
        ReceiptDialog(
            sale = sale,
            items = items,
            settings = storeSettings,
            onDismiss = { viewModel.dismissReceiptDialog() }
        )
    }

    if (isExportJsonOpen) {
        JsonExportDialog(
            jsonContent = exportJsonContent,
            onDismiss = { viewModel.closeExportJson() }
        )
    }

    if (isImportJsonOpen) {
        JsonImportDialog(
            onDismiss = { viewModel.closeImportJson() },
            onImport = { json -> viewModel.importJson(json) }
        )
    }
}
