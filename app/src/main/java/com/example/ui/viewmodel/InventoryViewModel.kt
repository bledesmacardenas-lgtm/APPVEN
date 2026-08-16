package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.entity.InventoryMovementEntity
import com.example.data.entity.ProductEntity
import com.example.data.entity.SaleEntity
import com.example.data.entity.StoreSettingsEntity
import com.example.data.model.CartItem
import com.example.data.model.DashboardMetrics
import com.example.data.model.ReportPeriod
import com.example.data.model.SaleWithItems
import com.example.data.model.SyncStatusState
import com.example.data.repository.InventoryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppTab(val title: String) {
    DASHBOARD("Reportes"),
    POS("Ventas / POS"),
    INVENTORY("Inventario"),
    HISTORY("Historial"),
    SETTINGS_CLOUD("Nube & Ajustes");

    companion object {
        val CLOUD = SETTINGS_CLOUD
    }
}

class InventoryViewModel(
    private val repository: InventoryRepository
) : ViewModel() {

    constructor(application: Application) : this(
        InventoryRepository(AppDatabase.getInstance(application))
    )

    // Active Navigation Tab
    private val _currentTab = MutableStateFlow(AppTab.DASHBOARD)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    // Store Settings
    val storeSettings: StateFlow<StoreSettingsEntity> = repository.storeSettings
        .map { it ?: StoreSettingsEntity() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = StoreSettingsEntity()
        )

    // Inventory & Catalog Search & Filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _stockFilter = MutableStateFlow("TODOS") // TODOS, BAJO_STOCK, AGOTADOS
    val stockFilter: StateFlow<String> = _stockFilter.asStateFlow()

    val allCategories: StateFlow<List<String>> = repository.allCategories
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    val categories: StateFlow<List<String>> = allCategories

    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredProducts: StateFlow<List<ProductEntity>> = combine(
        _searchQuery,
        _selectedCategory,
        _stockFilter
    ) { query, category, stockFilterState ->
        Triple(query, category, stockFilterState)
    }.flatMapLatest { (query, category, stockFilterState) ->
        repository.searchProducts(query, category).combine(repository.allProducts) { searchResults, _ ->
            when (stockFilterState) {
                "BAJO_STOCK" -> searchResults.filter { it.isLowStock }
                "AGOTADOS" -> searchResults.filter { it.isOutOfStock }
                else -> searchResults
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allProducts: StateFlow<List<ProductEntity>> = repository.allProducts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // POS Cart State
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _posCustomerName = MutableStateFlow("Cliente Mostrador")
    val posCustomerName: StateFlow<String> = _posCustomerName.asStateFlow()
    val customerName: StateFlow<String> = _posCustomerName.asStateFlow()

    private val _posCustomerPhone = MutableStateFlow("")
    val posCustomerPhone: StateFlow<String> = _posCustomerPhone.asStateFlow()
    val customerPhone: StateFlow<String> = _posCustomerPhone.asStateFlow()

    private val _posDiscountPercent = MutableStateFlow(0.0)
    val posDiscountPercent: StateFlow<Double> = _posDiscountPercent.asStateFlow()
    val discountPercent: StateFlow<Double> = _posDiscountPercent.asStateFlow()

    private val _posPaymentMethod = MutableStateFlow("Efectivo")
    val posPaymentMethod: StateFlow<String> = _posPaymentMethod.asStateFlow()
    val paymentMethod: StateFlow<String> = _posPaymentMethod.asStateFlow()

    private val _posAmountPaid = MutableStateFlow("")
    val posAmountPaid: StateFlow<String> = _posAmountPaid.asStateFlow()
    val amountPaid: StateFlow<String> = _posAmountPaid.asStateFlow()

    private val _posNotes = MutableStateFlow("")
    val posNotes: StateFlow<String> = _posNotes.asStateFlow()
    val saleNotes: StateFlow<String> = _posNotes.asStateFlow()

    private val _isCheckoutSheetOpen = MutableStateFlow(false)
    val isCheckoutSheetOpen: StateFlow<Boolean> = _isCheckoutSheetOpen.asStateFlow()
    val isCheckoutOpen: StateFlow<Boolean> = _isCheckoutSheetOpen.asStateFlow()

    // Completed Sale Ticket Modal
    private val _completedSale = MutableStateFlow<SaleEntity?>(null)
    val completedSale: StateFlow<SaleEntity?> = _completedSale.asStateFlow()

    private val _completedSaleItems = MutableStateFlow<List<CartItem>>(emptyList())
    val completedSaleItems: StateFlow<List<CartItem>> = _completedSaleItems.asStateFlow()

    val lastCompletedSaleWithItems: StateFlow<Pair<SaleEntity, List<CartItem>>?> = combine(
        _completedSale,
        _completedSaleItems
    ) { sale, items ->
        if (sale != null && items.isNotEmpty()) Pair(sale, items) else null
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // Sales History
    val allSales: StateFlow<List<SaleWithItems>> = repository.allSales
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    val salesHistory: StateFlow<List<SaleWithItems>> = allSales

    val recentMovements: StateFlow<List<InventoryMovementEntity>> = repository.recentMovements
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    val movementsHistory: StateFlow<List<InventoryMovementEntity>> = recentMovements

    private val _historyTab = MutableStateFlow(0) // 0 = Sales, 1 = Kardex Movements
    val historyTab: StateFlow<Int> = _historyTab.asStateFlow()

    private val _selectedSaleDetail = MutableStateFlow<SaleWithItems?>(null)
    val selectedSaleDetail: StateFlow<SaleWithItems?> = _selectedSaleDetail.asStateFlow()
    val activeSaleDetail: StateFlow<SaleWithItems?> = _selectedSaleDetail.asStateFlow()

    // Dashboard Analytics State
    private val _reportPeriod = MutableStateFlow(ReportPeriod.TODAY)
    val reportPeriod: StateFlow<ReportPeriod> = _reportPeriod.asStateFlow()
    val selectedReportPeriod: StateFlow<ReportPeriod> = _reportPeriod.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val dashboardMetrics: StateFlow<DashboardMetrics> = _reportPeriod
        .flatMapLatest { period -> repository.calculateMetrics(period) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardMetrics()
        )

    // Product Dialog State (Add / Edit)
    private val _isProductDialogVisible = MutableStateFlow(false)
    val isProductDialogVisible: StateFlow<Boolean> = _isProductDialogVisible.asStateFlow()
    val isProductFormOpen: StateFlow<Boolean> = _isProductDialogVisible.asStateFlow()

    private val _editingProduct = MutableStateFlow<ProductEntity?>(null)
    val editingProduct: StateFlow<ProductEntity?> = _editingProduct.asStateFlow()

    // Stock Adjustment Dialog State
    private val _isStockAdjustmentDialogVisible = MutableStateFlow(false)
    val isStockAdjustmentDialogVisible: StateFlow<Boolean> = _isStockAdjustmentDialogVisible.asStateFlow()
    val isStockAdjustOpen: StateFlow<Boolean> = _isStockAdjustmentDialogVisible.asStateFlow()

    private val _adjustingProduct = MutableStateFlow<ProductEntity?>(null)
    val adjustingProduct: StateFlow<ProductEntity?> = _adjustingProduct.asStateFlow()

    // Cloud Synchronization State
    private val _syncState = MutableStateFlow(SyncStatusState())
    val syncState: StateFlow<SyncStatusState> = _syncState.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage: StateFlow<String?> = _syncMessage.asStateFlow()

    private val _jsonBackupText = MutableStateFlow<String?>(null)
    val jsonBackupText: StateFlow<String?> = _jsonBackupText.asStateFlow()
    val exportJsonContent: StateFlow<String> = _jsonBackupText.map { it ?: "" }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ""
    )
    val isExportJsonOpen: StateFlow<Boolean> = _jsonBackupText.map { it != null }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    private val _isImportDialogVisible = MutableStateFlow(false)
    val isImportDialogVisible: StateFlow<Boolean> = _isImportDialogVisible.asStateFlow()
    val isImportJsonOpen: StateFlow<Boolean> = _isImportDialogVisible.asStateFlow()

    // Snackbars / Feedback Messages
    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()
    val eventFlow: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    // Navigation Action
    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
    }
    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    // Filter Actions
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = if (_selectedCategory.value == category) "" else category
    }

    fun setStockFilter(filter: String) {
        _stockFilter.value = filter
    }

    fun setReportPeriod(period: ReportPeriod) {
        _reportPeriod.value = period
    }

    fun setHistoryTab(tab: Int) {
        _historyTab.value = tab
    }

    // POS Cart Actions
    fun addToCart(product: ProductEntity) {
        val currentList = _cartItems.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == product.id }
        if (index != -1) {
            val existing = currentList[index]
            if (existing.quantity + 1.0 > product.currentStock) {
                showSnackbar("Stock máximo disponible alcanzado (${product.currentStock} ${product.unit})")
                return
            }
            currentList[index] = existing.copy(quantity = existing.quantity + 1.0)
        } else {
            if (product.currentStock <= 0) {
                showSnackbar("Producto agotado sin existencias")
                return
            }
            currentList.add(CartItem(product = product, quantity = 1.0))
        }
        _cartItems.value = currentList
    }

    fun updateCartQuantity(productId: Long, quantity: Double) {
        updateCartItemQuantity(productId, quantity)
    }

    fun updateCartItemQuantity(productId: Long, newQuantity: Double) {
        if (newQuantity <= 0) {
            removeFromCart(productId)
            return
        }
        val currentList = _cartItems.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == productId }
        if (index != -1) {
            val item = currentList[index]
            if (newQuantity > item.product.currentStock) {
                showSnackbar("Stock insuficiente: Disponible ${item.product.currentStock} ${item.product.unit}")
                return
            }
            currentList[index] = item.copy(quantity = newQuantity)
            _cartItems.value = currentList
        }
    }

    fun removeFromCart(productId: Long) {
        _cartItems.value = _cartItems.value.filter { it.product.id != productId }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        _posAmountPaid.value = ""
        _posDiscountPercent.value = 0.0
        _posNotes.value = ""
    }

    fun openCheckout() {
        openCheckoutSheet()
    }

    fun openCheckoutSheet() {
        if (_cartItems.value.isEmpty()) {
            showSnackbar("Agregue al menos un producto para cobrar")
            return
        }
        _isCheckoutSheetOpen.value = true
    }

    fun closeCheckout() {
        closeCheckoutSheet()
    }

    fun closeCheckoutSheet() {
        _isCheckoutSheetOpen.value = false
    }

    fun setCustomerName(name: String) { _posCustomerName.value = name }
    fun setPosCustomerName(name: String) { _posCustomerName.value = name }

    fun setCustomerPhone(phone: String) { _posCustomerPhone.value = phone }
    fun setPosCustomerPhone(phone: String) { _posCustomerPhone.value = phone }

    fun setDiscountPercent(discount: Double) { _posDiscountPercent.value = discount }
    fun setPosDiscountPercent(discount: Double) { _posDiscountPercent.value = discount }

    fun setPaymentMethod(method: String) { _posPaymentMethod.value = method }
    fun setPosPaymentMethod(method: String) { _posPaymentMethod.value = method }

    fun setAmountPaid(amount: String) { _posAmountPaid.value = amount }
    fun setPosAmountPaid(amount: String) { _posAmountPaid.value = amount }

    fun setSaleNotes(notes: String) { _posNotes.value = notes }
    fun setPosNotes(notes: String) { _posNotes.value = notes }

    fun processSale() {
        viewModelScope.launch {
            val items = _cartItems.value
            if (items.isEmpty()) {
                showSnackbar("El carrito está vacío")
                return@launch
            }
            val taxRate = storeSettings.value.taxRatePercent
            val paidAmount = _posAmountPaid.value.toDoubleOrNull() ?: 0.0

            val result = repository.completeSale(
                cartItems = items,
                paymentMethod = _posPaymentMethod.value,
                amountPaid = paidAmount,
                discountPercent = _posDiscountPercent.value,
                taxPercent = taxRate,
                customerName = _posCustomerName.value,
                customerPhone = _posCustomerPhone.value,
                notes = _posNotes.value
            )

            result.onSuccess { sale ->
                _completedSaleItems.value = items
                _completedSale.value = sale
                _isCheckoutSheetOpen.value = false
                clearCart()
                showSnackbar("¡Venta ${sale.saleNumber} realizada con éxito!")
            }.onFailure { error ->
                showSnackbar("Error al procesar venta: ${error.message}")
            }
        }
    }

    fun dismissReceiptDialog() {
        dismissCompletedSaleDialog()
    }

    fun dismissCompletedSaleDialog() {
        _completedSale.value = null
        _completedSaleItems.value = emptyList()
    }

    // Product Add/Edit Dialog Actions
    fun openProductForm(product: ProductEntity?) {
        _editingProduct.value = product
        _isProductDialogVisible.value = true
    }

    fun openAddProductDialog() {
        _editingProduct.value = null
        _isProductDialogVisible.value = true
    }

    fun openEditProductDialog(product: ProductEntity) {
        _editingProduct.value = product
        _isProductDialogVisible.value = true
    }

    fun closeProductForm() {
        closeProductDialog()
    }

    fun closeProductDialog() {
        _isProductDialogVisible.value = false
        _editingProduct.value = null
    }

    fun saveProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.saveProduct(product)
            closeProductDialog()
            showSnackbar(if (product.id == 0L) "Producto agregado correctamente" else "Producto actualizado")
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            showSnackbar("Producto eliminado: ${product.name}")
        }
    }

    // Stock Adjustment Actions
    fun openStockAdjustment(product: ProductEntity) {
        openStockAdjustmentDialog(product)
    }

    fun openStockAdjustmentDialog(product: ProductEntity) {
        _adjustingProduct.value = product
        _isStockAdjustmentDialogVisible.value = true
    }

    fun closeStockAdjustment() {
        closeStockAdjustmentDialog()
    }

    fun closeStockAdjustmentDialog() {
        _isStockAdjustmentDialogVisible.value = false
        _adjustingProduct.value = null
    }

    fun applyStockAdjustment(
        product: ProductEntity,
        quantityDelta: Double,
        type: String,
        reason: String
    ) {
        applyStockAdjustment(product.id, quantityDelta, type, reason)
    }

    fun applyStockAdjustment(
        productId: Long,
        quantityDelta: Double,
        type: String,
        reason: String
    ) {
        viewModelScope.launch {
            repository.adjustProductStock(productId, quantityDelta, type, reason)
            closeStockAdjustmentDialog()
            showSnackbar("Stock actualizado con éxito")
        }
    }

    // Sale History Details & Cancellation
    fun openSaleDetail(saleWithItems: SaleWithItems) {
        showSaleDetails(saleWithItems)
    }

    fun showSaleDetails(saleWithItems: SaleWithItems) {
        _selectedSaleDetail.value = saleWithItems
    }

    fun closeSaleDetail() {
        dismissSaleDetails()
    }

    fun dismissSaleDetails() {
        _selectedSaleDetail.value = null
    }

    fun cancelSale(saleId: Long, reason: String) {
        viewModelScope.launch {
            val result = repository.cancelSale(saleId, reason)
            result.onSuccess {
                dismissSaleDetails()
                showSnackbar("Venta anulada y stock devuelto al inventario")
            }.onFailure { error ->
                showSnackbar("Error al anular venta: ${error.message}")
            }
        }
    }

    // Cloud Sync & Settings Actions
    fun triggerManualSync() {
        triggerCloudSync()
    }

    fun triggerCloudSync() {
        viewModelScope.launch {
            _isSyncing.value = true
            _syncState.value = _syncState.value.copy(isSyncing = true, errorMessage = null)
            _syncMessage.value = "Sincronizando productos, ventas y movimientos con la nube..."
            val result = repository.syncWithCloud()
            _isSyncing.value = false
            val now = System.currentTimeMillis()
            result.onSuccess { msg ->
                _syncMessage.value = msg
                _syncState.value = _syncState.value.copy(
                    isSyncing = false,
                    lastSyncTime = now,
                    pendingSyncCount = 0,
                    errorMessage = null
                )
                showSnackbar("Sincronización en la nube completada")
            }.onFailure { err ->
                _syncMessage.value = "Error de sincronización: ${err.message}"
                _syncState.value = _syncState.value.copy(
                    isSyncing = false,
                    errorMessage = err.message
                )
                showSnackbar("Fallo en sincronización: ${err.message}")
            }
        }
    }

    fun openExportJson() {
        exportDatabaseJson()
    }

    fun exportDatabaseJson() {
        viewModelScope.launch {
            val json = repository.exportDatabaseToJson()
            _jsonBackupText.value = json
        }
    }

    fun closeExportJson() {
        dismissExportDialog()
    }

    fun dismissExportDialog() {
        _jsonBackupText.value = null
    }

    fun openImportJson() {
        openImportDialog()
    }

    fun openImportDialog() {
        _isImportDialogVisible.value = true
    }

    fun closeImportJson() {
        closeImportDialog()
    }

    fun closeImportDialog() {
        _isImportDialogVisible.value = false
    }

    fun importJson(json: String) {
        importDatabaseJson(json)
    }

    fun importDatabaseJson(json: String) {
        viewModelScope.launch {
            val result = repository.importDatabaseFromJson(json)
            closeImportDialog()
            result.onSuccess { count ->
                showSnackbar("Se importaron $count productos correctamente")
            }.onFailure { err ->
                showSnackbar("Error al importar datos: ${err.message}")
            }
        }
    }

    fun resetDemoData() {
        resetToDemoData()
    }

    fun resetToDemoData() {
        viewModelScope.launch {
            repository.resetToSampleData()
            showSnackbar("Datos de demostración restablecidos")
        }
    }

    fun updateSettings(settings: StoreSettingsEntity) {
        updateStoreSettings(settings)
    }

    fun updateStoreSettings(settings: StoreSettingsEntity) {
        viewModelScope.launch {
            repository.updateSettings(settings)
            showSnackbar("Configuración de tienda guardada")
        }
    }

    private fun showSnackbar(message: String) {
        viewModelScope.launch {
            _snackbarEvent.emit(message)
        }
    }
}

class InventoryViewModelFactory(
    private val repository: InventoryRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InventoryViewModel::class.java)) {
            return InventoryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
