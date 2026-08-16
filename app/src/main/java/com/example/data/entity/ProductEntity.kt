package com.example.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "products",
    indices = [Index(value = ["sku"], unique = false), Index(value = ["barcode"])]
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sku: String,
    val name: String,
    val category: String,
    val costPrice: Double,
    val salePrice: Double,
    val currentStock: Double,
    val minStock: Double = 5.0,
    val unit: String = "pz", // pz, kg, lt, caja, paq, etc.
    val barcode: String = "",
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
) {
    val isLowStock: Boolean
        get() = currentStock > 0 && currentStock <= minStock

    val isOutOfStock: Boolean
        get() = currentStock <= 0

    val profitMarginPercent: Double
        get() = if (costPrice > 0) ((salePrice - costPrice) / costPrice) * 100.0 else 0.0
}
