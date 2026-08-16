package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "store_settings")
data class StoreSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val storeName: String = "Mi Negocio Express",
    val businessType: String = "Comercio General",
    val phone: String = "+1 800 123 4567",
    val address: String = "Av. Principal #100",
    val taxRatePercent: Double = 16.0,
    val currencySymbol: String = "$",
    val receiptFooter: String = "¡Gracias por su compra! Vuelva pronto.",
    val autoSyncEnabled: Boolean = true,
    val lastSyncTimestamp: Long = 0L,
    val cloudEndpoint: String = "https://api.inventario-cloud.aistudio.io/v1/sync"
) {
    val autoSyncCloud: Boolean
        get() = autoSyncEnabled
}
