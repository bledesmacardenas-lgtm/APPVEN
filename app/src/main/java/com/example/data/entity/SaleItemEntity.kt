package com.example.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sale_items",
    indices = [Index(value = ["saleId"]), Index(value = ["productId"])]
)
data class SaleItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val saleId: Long,
    val productId: Long,
    val productName: String,
    val sku: String,
    val quantity: Double,
    val unit: String = "pz",
    val unitCost: Double,
    val unitPrice: Double,
    val total: Double
) {
    val totalCost: Double
        get() = unitCost * quantity

    val itemProfit: Double
        get() = total - totalCost
}
