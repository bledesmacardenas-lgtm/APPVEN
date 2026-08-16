package com.example.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "inventory_movements",
    indices = [Index(value = ["productId"]), Index(value = ["timestamp"])]
)
data class InventoryMovementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productId: Long,
    val productName: String,
    val type: String, // "ENTRADA", "SALIDA_VENTA", "AJUSTE_POSITIVO", "AJUSTE_NEGATIVO", "MERMA", "ANULACION_VENTA"
    val quantity: Double,
    val previousStock: Double,
    val newStock: Double,
    val reason: String = "",
    val referenceId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
) {
    val quantityChange: Double
        get() = if (type in listOf("ENTRADA", "AJUSTE_POSITIVO", "ANULACION_VENTA")) quantity else -quantity

    val stockAfter: Double
        get() = newStock
}
