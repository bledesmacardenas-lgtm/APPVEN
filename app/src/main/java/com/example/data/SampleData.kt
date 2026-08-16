package com.example.data

import com.example.data.entity.InventoryMovementEntity
import com.example.data.entity.ProductEntity
import com.example.data.entity.SaleEntity
import com.example.data.entity.SaleItemEntity

object SampleData {
    suspend fun populateSampleInventoryAndSales(db: AppDatabase) {
        val productDao = db.productDao()
        val saleDao = db.saleDao()
        val movementDao = db.inventoryMovementDao()

        val initialProducts = listOf(
            ProductEntity(
                id = 1,
                sku = "ALM-001",
                barcode = "750100012345",
                name = "Arroz Extra Grano Largo 1kg",
                category = "Abarrotes",
                costPrice = 18.50,
                salePrice = 28.00,
                currentStock = 45.0,
                minStock = 10.0,
                unit = "kg",
                description = "Arroz blanco de grano entero premium",
                isSynced = true
            ),
            ProductEntity(
                id = 2,
                sku = "ALM-002",
                barcode = "750100012346",
                name = "Aceite Vegetal Puro 1L",
                category = "Abarrotes",
                costPrice = 32.00,
                salePrice = 46.50,
                currentStock = 28.0,
                minStock = 8.0,
                unit = "lt",
                description = "Aceite 100% puro de soya sin colesterol",
                isSynced = true
            ),
            ProductEntity(
                id = 3,
                sku = "BEB-101",
                barcode = "750100012347",
                name = "Refresco Cola Clásico 600ml",
                category = "Bebidas",
                costPrice = 11.00,
                salePrice = 18.00,
                currentStock = 85.0,
                minStock = 20.0,
                unit = "pz",
                description = "Bebida gaseosa sabor cola botella PET",
                isSynced = true
            ),
            ProductEntity(
                id = 4,
                sku = "BEB-102",
                barcode = "750100012348",
                name = "Agua Mineral Purificada 1.5L",
                category = "Bebidas",
                costPrice = 8.50,
                salePrice = 15.00,
                currentStock = 4.0, // Low stock for alert demo
                minStock = 10.0,
                unit = "pz",
                description = "Agua de manantial purificada",
                isSynced = true
            ),
            ProductEntity(
                id = 5,
                sku = "LAC-201",
                barcode = "750100012349",
                name = "Leche Entera Pasteurizada 1L",
                category = "Lácteos",
                costPrice = 19.00,
                salePrice = 27.50,
                currentStock = 18.0,
                minStock = 12.0,
                unit = "lt",
                description = "Leche de vaca fresca fortificada",
                isSynced = true
            ),
            ProductEntity(
                id = 6,
                sku = "LAC-202",
                barcode = "750100012350",
                name = "Queso Panela Fresco 400g",
                category = "Lácteos",
                costPrice = 38.00,
                salePrice = 58.00,
                currentStock = 2.0, // Low stock
                minStock = 5.0,
                unit = "pz",
                description = "Queso fresco artesanal bajo en grasa",
                isSynced = true
            ),
            ProductEntity(
                id = 7,
                sku = "LIM-301",
                barcode = "750100012351",
                name = "Detergente Multiusos 1kg",
                category = "Limpieza",
                costPrice = 24.00,
                salePrice = 39.90,
                currentStock = 30.0,
                minStock = 6.0,
                unit = "pz",
                description = "Detergente en polvo biodegradable",
                isSynced = true
            ),
            ProductEntity(
                id = 8,
                sku = "LIM-302",
                barcode = "750100012352",
                name = "Desinfectante Lavanda 1L",
                category = "Limpieza",
                costPrice = 14.00,
                salePrice = 22.50,
                currentStock = 0.0, // Out of stock demo
                minStock = 8.0,
                unit = "lt",
                description = "Limpiador aromatizante antibacterial",
                isSynced = true
            ),
            ProductEntity(
                id = 9,
                sku = "PAN-401",
                barcode = "750100012353",
                name = "Pan Blanco de Caja 680g",
                category = "Panadería",
                costPrice = 31.00,
                salePrice = 45.00,
                currentStock = 14.0,
                minStock = 6.0,
                unit = "pz",
                description = "Pan de molde enriquecido con vitaminas",
                isSynced = true
            ),
            ProductEntity(
                id = 10,
                sku = "BOT-501",
                barcode = "750100012354",
                name = "Papas Fritas Saladas 170g",
                category = "Snacks",
                costPrice = 22.00,
                salePrice = 36.00,
                currentStock = 52.0,
                minStock = 15.0,
                unit = "pz",
                description = "Botana crujiente de papa con sal marina",
                isSynced = true
            ),
            ProductEntity(
                id = 11,
                sku = "TEC-601",
                barcode = "750100012355",
                name = "Cable USB-C Carga Rápida 1m",
                category = "Tecnología",
                costPrice = 45.00,
                salePrice = 120.00,
                currentStock = 25.0,
                minStock = 5.0,
                unit = "pz",
                description = "Cable trenzado reforzado 60W",
                isSynced = true
            ),
            ProductEntity(
                id = 12,
                sku = "TEC-602",
                barcode = "750100012356",
                name = "Audífonos In-Ear con Micrófono",
                category = "Tecnología",
                costPrice = 60.00,
                salePrice = 149.00,
                currentStock = 12.0,
                minStock = 4.0,
                unit = "pz",
                description = "Sonido estéreo de alta fidelidad jack 3.5mm",
                isSynced = true
            )
        )

        productDao.insertAll(initialProducts)

        // Seed some initial inventory movements
        val now = System.currentTimeMillis()
        val oneDay = 86_400_000L

        val initialMovements = initialProducts.map { p ->
            InventoryMovementEntity(
                productId = p.id,
                productName = p.name,
                type = "ENTRADA",
                quantity = p.currentStock + 10.0,
                previousStock = 0.0,
                newStock = p.currentStock + 10.0,
                reason = "Inventario inicial de apertura",
                referenceId = "INI-001",
                timestamp = now - (3 * oneDay),
                isSynced = true
            )
        }
        movementDao.insertAll(initialMovements)

        // Seed some realistic recent sales
        val salesData = listOf(
            Triple(
                SaleEntity(
                    id = 1,
                    saleNumber = "VNT-1001",
                    timestamp = now - (2 * oneDay) + 3600000,
                    subtotal = 92.50,
                    discountPercent = 0.0,
                    discountAmount = 0.0,
                    taxPercent = 16.0,
                    taxAmount = 14.80,
                    total = 107.30,
                    totalEstimatedCost = 61.50,
                    paymentMethod = "Efectivo",
                    amountPaid = 120.00,
                    changeGiven = 12.70,
                    customerName = "Carlos Mendoza",
                    status = "COMPLETADA",
                    totalItemsCount = 3,
                    isSynced = true
                ),
                listOf(
                    SaleItemEntity(saleId = 1, productId = 1, productName = "Arroz Extra Grano Largo 1kg", sku = "ALM-001", quantity = 1.0, unitCost = 18.50, unitPrice = 28.00, total = 28.00),
                    SaleItemEntity(saleId = 1, productId = 3, productName = "Refresco Cola Clásico 600ml", sku = "BEB-101", quantity = 2.0, unitCost = 11.00, unitPrice = 18.00, total = 36.00),
                    SaleItemEntity(saleId = 1, productId = 5, productName = "Leche Entera Pasteurizada 1L", sku = "LAC-201", quantity = 1.0, unitCost = 19.00, unitPrice = 28.50, total = 28.50)
                ),
                now - (2 * oneDay) + 3600000
            ),
            Triple(
                SaleEntity(
                    id = 2,
                    saleNumber = "VNT-1002",
                    timestamp = now - (1 * oneDay) + 7200000,
                    subtotal = 205.00,
                    discountPercent = 5.0,
                    discountAmount = 10.25,
                    taxPercent = 16.0,
                    taxAmount = 31.16,
                    total = 225.91,
                    totalEstimatedCost = 120.00,
                    paymentMethod = "Tarjeta",
                    amountPaid = 225.91,
                    changeGiven = 0.0,
                    customerName = "Laura Gómez",
                    status = "COMPLETADA",
                    totalItemsCount = 4,
                    isSynced = true
                ),
                listOf(
                    SaleItemEntity(saleId = 2, productId = 11, productName = "Cable USB-C Carga Rápida 1m", sku = "TEC-601", quantity = 1.0, unitCost = 45.00, unitPrice = 120.00, total = 120.00),
                    SaleItemEntity(saleId = 2, productId = 9, productName = "Pan Blanco de Caja 680g", sku = "PAN-401", quantity = 1.0, unitCost = 31.00, unitPrice = 45.00, total = 45.00),
                    SaleItemEntity(saleId = 2, productId = 10, productName = "Papas Fritas Saladas 170g", sku = "BOT-501", quantity = 1.0, unitCost = 22.00, unitPrice = 36.00, total = 36.00),
                    SaleItemEntity(saleId = 2, productId = 4, productName = "Agua Mineral Purificada 1.5L", sku = "BEB-102", quantity = 1.0, unitCost = 8.50, unitPrice = 15.00, total = 15.00)
                ),
                now - (1 * oneDay) + 7200000
            ),
            Triple(
                SaleEntity(
                    id = 3,
                    saleNumber = "VNT-1003",
                    timestamp = now - 1800000,
                    subtotal = 149.00,
                    discountPercent = 0.0,
                    discountAmount = 0.0,
                    taxPercent = 16.0,
                    taxAmount = 23.84,
                    total = 172.84,
                    totalEstimatedCost = 60.00,
                    paymentMethod = "Transferencia",
                    amountPaid = 172.84,
                    changeGiven = 0.0,
                    customerName = "Cliente Mostrador",
                    status = "COMPLETADA",
                    totalItemsCount = 1,
                    isSynced = true
                ),
                listOf(
                    SaleItemEntity(saleId = 3, productId = 12, productName = "Audífonos In-Ear con Micrófono", sku = "TEC-602", quantity = 1.0, unitCost = 60.00, unitPrice = 149.00, total = 149.00)
                ),
                now - 1800000
            )
        )

        for ((sale, items, _) in salesData) {
            val saleId = saleDao.insertSale(sale)
            val updatedItems = items.map { it.copy(saleId = saleId) }
            saleDao.insertSaleItems(updatedItems)
        }
    }
}
