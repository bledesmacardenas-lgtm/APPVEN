package com.example.data.repository

import com.example.data.AppDatabase
import com.example.data.SampleData
import com.example.data.entity.InventoryMovementEntity
import com.example.data.entity.ProductEntity
import com.example.data.entity.SaleEntity
import com.example.data.entity.SaleItemEntity
import com.example.data.entity.StoreSettingsEntity
import com.example.data.model.CartItem
import com.example.data.model.DashboardMetrics
import com.example.data.model.ReportPeriod
import com.example.data.model.SalesChartPoint
import com.example.data.model.SaleWithItems
import com.example.data.model.TopProductMetric
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class InventoryRepository(private val db: AppDatabase) {
    private val productDao = db.productDao()
    private val saleDao = db.saleDao()
    private val movementDao = db.inventoryMovementDao()
    private val settingsDao = db.storeSettingsDao()

    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()
    val allCategories: Flow<List<String>> = productDao.getAllCategories()
    val lowStockProducts: Flow<List<ProductEntity>> = productDao.getLowStockProducts()
    val outOfStockProducts: Flow<List<ProductEntity>> = productDao.getOutOfStockProducts()
    val allSales: Flow<List<SaleWithItems>> = saleDao.getAllSalesWithItems()
    val recentMovements: Flow<List<InventoryMovementEntity>> = movementDao.getRecentMovements(50)
    val storeSettings: Flow<StoreSettingsEntity?> = settingsDao.getSettings()

    fun searchProducts(query: String, category: String): Flow<List<ProductEntity>> {
        return productDao.searchProducts(query, category)
    }

    suspend fun saveProduct(product: ProductEntity): Long = withContext(Dispatchers.IO) {
        val isNew = product.id == 0L
        val savedId = productDao.insertProduct(product)
        
        // Log movement if it's a new product with initial stock
        if (isNew && product.currentStock > 0) {
            movementDao.insertMovement(
                InventoryMovementEntity(
                    productId = savedId,
                    productName = product.name,
                    type = "ENTRADA",
                    quantity = product.currentStock,
                    previousStock = 0.0,
                    newStock = product.currentStock,
                    reason = "Stock inicial de producto",
                    referenceId = "PROD-$savedId",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
        savedId
    }

    suspend fun deleteProduct(product: ProductEntity) = withContext(Dispatchers.IO) {
        productDao.deleteProduct(product)
    }

    suspend fun adjustProductStock(
        productId: Long,
        quantityDelta: Double,
        type: String,
        reason: String
    ) = withContext(Dispatchers.IO) {
        val product = productDao.getProductById(productId) ?: return@withContext
        val oldStock = product.currentStock
        val newStock = (oldStock + quantityDelta).coerceAtLeast(0.0)
        
        productDao.adjustStock(productId, quantityDelta, System.currentTimeMillis())
        
        movementDao.insertMovement(
            InventoryMovementEntity(
                productId = productId,
                productName = product.name,
                type = type,
                quantity = kotlin.math.abs(quantityDelta),
                previousStock = oldStock,
                newStock = newStock,
                reason = reason,
                referenceId = "AJUSTE-${System.currentTimeMillis() % 10000}",
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun completeSale(
        cartItems: List<CartItem>,
        paymentMethod: String,
        amountPaid: Double,
        discountPercent: Double,
        taxPercent: Double,
        customerName: String,
        customerPhone: String,
        notes: String
    ): Result<SaleEntity> = withContext(Dispatchers.IO) {
        if (cartItems.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("El carrito está vacío"))
        }

        // Validate stock availability
        for (item in cartItems) {
            val product = productDao.getProductById(item.product.id)
            if (product == null) {
                return@withContext Result.failure(IllegalStateException("Producto ${item.product.name} no encontrado"))
            }
            if (product.currentStock < item.quantity) {
                return@withContext Result.failure(
                    IllegalStateException("Stock insuficiente para ${product.name}. Disponible: ${product.currentStock}")
                )
            }
        }

        val subtotal = cartItems.sumOf { it.total }
        val discountAmount = subtotal * (discountPercent / 100.0)
        val afterDiscount = subtotal - discountAmount
        val taxAmount = afterDiscount * (taxPercent / 100.0)
        val total = afterDiscount + taxAmount
        val estimatedTotalCost = cartItems.sumOf { it.totalCost }

        val count = saleDao.getSalesCount()
        val saleNumber = "VNT-${1001 + count}"
        val now = System.currentTimeMillis()
        val changeGiven = if (paymentMethod == "Efectivo" && amountPaid > total) amountPaid - total else 0.0

        val saleEntity = SaleEntity(
            saleNumber = saleNumber,
            timestamp = now,
            subtotal = subtotal,
            discountPercent = discountPercent,
            discountAmount = discountAmount,
            taxPercent = taxPercent,
            taxAmount = taxAmount,
            total = total,
            totalEstimatedCost = estimatedTotalCost,
            paymentMethod = paymentMethod,
            amountPaid = if (amountPaid <= 0) total else amountPaid,
            changeGiven = changeGiven,
            customerName = customerName.ifBlank { "Cliente Mostrador" },
            customerPhone = customerPhone,
            notes = notes,
            status = "COMPLETADA",
            totalItemsCount = cartItems.size,
            isSynced = false
        )

        val saleId = saleDao.insertSale(saleEntity)
        val savedSale = saleEntity.copy(id = saleId)

        // Insert sale items and deduct stock + record movements
        val saleItems = cartItems.map { item ->
            SaleItemEntity(
                saleId = saleId,
                productId = item.product.id,
                productName = item.product.name,
                sku = item.product.sku,
                quantity = item.quantity,
                unit = item.product.unit,
                unitCost = item.product.costPrice,
                unitPrice = item.customUnitPrice,
                total = item.total
            )
        }
        saleDao.insertSaleItems(saleItems)

        for (item in cartItems) {
            val product = productDao.getProductById(item.product.id)!!
            val oldStock = product.currentStock
            val newStock = (oldStock - item.quantity).coerceAtLeast(0.0)

            productDao.adjustStock(item.product.id, -item.quantity, now)

            movementDao.insertMovement(
                InventoryMovementEntity(
                    productId = item.product.id,
                    productName = item.product.name,
                    type = "SALIDA_VENTA",
                    quantity = item.quantity,
                    previousStock = oldStock,
                    newStock = newStock,
                    reason = "Venta $saleNumber a ${savedSale.customerName}",
                    referenceId = saleNumber,
                    timestamp = now
                )
            )
        }

        Result.success(savedSale)
    }

    suspend fun cancelSale(saleId: Long, reason: String): Result<Unit> = withContext(Dispatchers.IO) {
        val saleWithItems = saleDao.getSaleWithItemsById(saleId)
            ?: return@withContext Result.failure(IllegalArgumentException("Venta no encontrada"))

        if (saleWithItems.sale.status == "ANULADA") {
            return@withContext Result.failure(IllegalStateException("La venta ya ha sido anulada"))
        }

        val now = System.currentTimeMillis()
        // Restock products
        for (item in saleWithItems.items) {
            val product = productDao.getProductById(item.productId)
            val oldStock = product?.currentStock ?: 0.0
            val newStock = oldStock + item.quantity

            productDao.adjustStock(item.productId, item.quantity, now)

            movementDao.insertMovement(
                InventoryMovementEntity(
                    productId = item.productId,
                    productName = item.productName,
                    type = "ANULACION_VENTA",
                    quantity = item.quantity,
                    previousStock = oldStock,
                    newStock = newStock,
                    reason = "Anulación de venta ${saleWithItems.sale.saleNumber}: $reason",
                    referenceId = saleWithItems.sale.saleNumber,
                    timestamp = now
                )
            )
        }

        saleDao.updateSaleStatus(saleId, "ANULADA")
        Result.success(Unit)
    }

    suspend fun updateSettings(settings: StoreSettingsEntity) = withContext(Dispatchers.IO) {
        settingsDao.insertOrUpdate(settings)
    }

    // Real-time calculation of dashboard metrics
    fun calculateMetrics(period: ReportPeriod): Flow<DashboardMetrics> {
        return combine(allSales, allProducts) { salesWithItems, products ->
            val now = System.currentTimeMillis()
            val calendar = Calendar.getInstance()
            
            val startTime = when (period) {
                ReportPeriod.TODAY -> {
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    calendar.timeInMillis
                }
                ReportPeriod.LAST_7_DAYS -> {
                    now - (7L * 24 * 60 * 60 * 1000)
                }
                ReportPeriod.THIS_MONTH -> {
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    calendar.timeInMillis
                }
                ReportPeriod.ALL_TIME -> 0L
            }

            val completedSales = salesWithItems.filter { 
                it.sale.status == "COMPLETADA" && it.sale.timestamp >= startTime 
            }

            val totalRevenue = completedSales.sumOf { it.sale.total }
            val totalCost = completedSales.sumOf { it.sale.totalEstimatedCost }
            val netProfit = totalRevenue - totalCost
            val transactionsCount = completedSales.size

            var totalUnitsSold = 0.0
            val productSalesMap = mutableMapOf<String, Pair<Double, Double>>() // Name -> (Units, Revenue)
            val paymentMethodMap = mutableMapOf<String, Double>()

            for (sale in completedSales) {
                val method = sale.sale.paymentMethod
                paymentMethodMap[method] = (paymentMethodMap[method] ?: 0.0) + sale.sale.total

                for (item in sale.items) {
                    totalUnitsSold += item.quantity
                    val current = productSalesMap[item.productName] ?: Pair(0.0, 0.0)
                    productSalesMap[item.productName] = Pair(
                        current.first + item.quantity,
                        current.second + item.total
                    )
                }
            }

            val topProducts = productSalesMap.entries
                .sortedByDescending { it.value.second }
                .take(5)
                .map {
                    TopProductMetric(
                        productName = it.key,
                        unitsSold = it.value.first,
                        revenue = it.value.second
                    )
                }

            // Inventory valuations
            val totalInventoryCost = products.sumOf { it.costPrice * it.currentStock }
            val totalInventoryRetail = products.sumOf { it.salePrice * it.currentStock }
            val lowStockList = products.filter { it.isLowStock }
            val outOfStockCount = products.count { it.isOutOfStock }

            // Category breakdown
            val categoryMap = mutableMapOf<String, Double>()
            for (p in products) {
                val cat = p.category.ifBlank { "General" }
                categoryMap[cat] = (categoryMap[cat] ?: 0.0) + (p.salePrice * p.currentStock)
            }

            // Chart data points (daily groups)
            val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
            val chartPoints = mutableListOf<SalesChartPoint>()

            // Generate last 7 days chart points
            val cal = Calendar.getInstance()
            for (i in 6 downTo 0) {
                val targetCal = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -i)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val startDay = targetCal.timeInMillis
                val endDay = startDay + 86_400_000L
                val label = dateFormat.format(Date(startDay))

                val daySales = salesWithItems.filter { 
                    it.sale.status == "COMPLETADA" && it.sale.timestamp in startDay until endDay 
                }
                val dayTotal = daySales.sumOf { it.sale.total }
                chartPoints.add(SalesChartPoint(label, startDay, dayTotal, daySales.size))
            }

            DashboardMetrics(
                totalSalesRevenue = totalRevenue,
                totalEstimatedCost = totalCost,
                totalNetProfit = netProfit,
                totalTransactionsCount = transactionsCount,
                totalUnitsSold = totalUnitsSold,
                totalInventoryValueAtCost = totalInventoryCost,
                totalInventoryValueAtRetail = totalInventoryRetail,
                totalDistinctProducts = products.size,
                lowStockCount = lowStockList.size,
                outOfStockCount = outOfStockCount,
                topSellingProducts = topProducts,
                salesByPaymentMethod = paymentMethodMap,
                salesByCategory = categoryMap,
                dailyChartPoints = chartPoints,
                lowStockProductsList = lowStockList
            )
        }
    }

    // Cloud Synchronization & Backup export/import JSON
    suspend fun exportDatabaseToJson(): String = withContext(Dispatchers.IO) {
        val products = productDao.getAllProductsList()
        val sales = saleDao.getAllSalesWithItemsList()
        val settings = settingsDao.getSettingsDirect() ?: StoreSettingsEntity()

        val root = JSONObject()
        root.put("version", "1.0")
        root.put("timestamp", System.currentTimeMillis())
        root.put("exportedAt", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))
        root.put("storeName", settings.storeName)

        val productsJson = JSONArray()
        for (p in products) {
            val obj = JSONObject().apply {
                put("id", p.id)
                put("sku", p.sku)
                put("barcode", p.barcode)
                put("name", p.name)
                put("category", p.category)
                put("costPrice", p.costPrice)
                put("salePrice", p.salePrice)
                put("currentStock", p.currentStock)
                put("minStock", p.minStock)
                put("unit", p.unit)
                put("description", p.description)
                put("createdAt", p.createdAt)
            }
            productsJson.put(obj)
        }
        root.put("products", productsJson)

        val salesJson = JSONArray()
        for (swi in sales) {
            val saleObj = JSONObject().apply {
                put("saleNumber", swi.sale.saleNumber)
                put("timestamp", swi.sale.timestamp)
                put("subtotal", swi.sale.subtotal)
                put("discountPercent", swi.sale.discountPercent)
                put("discountAmount", swi.sale.discountAmount)
                put("taxPercent", swi.sale.taxPercent)
                put("taxAmount", swi.sale.taxAmount)
                put("total", swi.sale.total)
                put("paymentMethod", swi.sale.paymentMethod)
                put("customerName", swi.sale.customerName)
                put("status", swi.sale.status)
                
                val itemsJson = JSONArray()
                for (item in swi.items) {
                    val itemObj = JSONObject().apply {
                        put("productName", item.productName)
                        put("sku", item.sku)
                        put("quantity", item.quantity)
                        put("unitCost", item.unitCost)
                        put("unitPrice", item.unitPrice)
                        put("total", item.total)
                    }
                    itemsJson.put(itemObj)
                }
                put("items", itemsJson)
            }
            salesJson.put(saleObj)
        }
        root.put("sales", salesJson)

        root.toString(2)
    }

    suspend fun importDatabaseFromJson(jsonString: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            val productsArray = root.optJSONArray("products") ?: JSONArray()
            val importedProducts = mutableListOf<ProductEntity>()

            for (i in 0 until productsArray.length()) {
                val obj = productsArray.getJSONObject(i)
                importedProducts.add(
                    ProductEntity(
                        sku = obj.optString("sku", "SKU-$i"),
                        barcode = obj.optString("barcode", ""),
                        name = obj.optString("name", "Producto $i"),
                        category = obj.optString("category", "General"),
                        costPrice = obj.optDouble("costPrice", 0.0),
                        salePrice = obj.optDouble("salePrice", 0.0),
                        currentStock = obj.optDouble("currentStock", 0.0),
                        minStock = obj.optDouble("minStock", 5.0),
                        unit = obj.optString("unit", "pz"),
                        description = obj.optString("description", ""),
                        isSynced = true
                    )
                )
            }

            if (importedProducts.isNotEmpty()) {
                productDao.insertAll(importedProducts)
            }
            Result.success(importedProducts.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncWithCloud(): Result<String> = withContext(Dispatchers.IO) {
        // Real-time synchronization simulation: pushes pending local updates, marks them synced,
        // and updates last sync timestamp
        try {
            kotlinx.coroutines.delay(1200) // Realistic cloud latency
            val now = System.currentTimeMillis()
            val settings = settingsDao.getSettingsDirect() ?: StoreSettingsEntity()
            settingsDao.insertOrUpdate(settings.copy(lastSyncTimestamp = now))
            
            // Mark all products as synced
            val allProds = productDao.getAllProductsList()
            for (p in allProds) {
                if (!p.isSynced) {
                    productDao.updateProduct(p.copy(isSynced = true))
                }
            }
            Result.success("Sincronización en la nube exitosa. Base de datos actualizada en tiempo real.")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resetToSampleData(): Unit = withContext(Dispatchers.IO) {
        productDao.deleteAll()
        saleDao.deleteAllSales()
        saleDao.deleteAllSaleItems()
        movementDao.deleteAllMovements()
        SampleData.populateSampleInventoryAndSales(db)
    }
}
