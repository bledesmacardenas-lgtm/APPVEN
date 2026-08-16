package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sales")
data class SaleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val saleNumber: String, // e.g. "VNT-1001"
    val timestamp: Long = System.currentTimeMillis(),
    val subtotal: Double,
    val discountPercent: Double = 0.0,
    val discountAmount: Double = 0.0,
    val taxPercent: Double = 0.0,
    val taxAmount: Double = 0.0,
    val total: Double,
    val totalEstimatedCost: Double = 0.0,
    val paymentMethod: String = "Efectivo", // Efectivo, Tarjeta, Transferencia, Crédito
    val amountPaid: Double = 0.0,
    val changeGiven: Double = 0.0,
    val customerName: String = "Cliente Mostrador",
    val customerPhone: String = "",
    val notes: String = "",
    val status: String = "COMPLETADA", // COMPLETADA, ANULADA
    val totalItemsCount: Int = 1,
    val isSynced: Boolean = false
) {
    val netProfit: Double
        get() = total - totalEstimatedCost
}
