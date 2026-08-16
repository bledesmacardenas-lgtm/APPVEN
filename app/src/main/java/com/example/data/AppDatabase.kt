package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.InventoryMovementDao
import com.example.data.dao.ProductDao
import com.example.data.dao.SaleDao
import com.example.data.dao.StoreSettingsDao
import com.example.data.entity.InventoryMovementEntity
import com.example.data.entity.ProductEntity
import com.example.data.entity.SaleEntity
import com.example.data.entity.SaleItemEntity
import com.example.data.entity.StoreSettingsEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ProductEntity::class,
        SaleEntity::class,
        SaleItemEntity::class,
        InventoryMovementEntity::class,
        StoreSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun saleDao(): SaleDao
    abstract fun inventoryMovementDao(): InventoryMovementDao
    abstract fun storeSettingsDao(): StoreSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return getDatabase(context, CoroutineScope(Dispatchers.IO))
        }

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "inventario_ventas_db"
                )
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }

            private suspend fun populateInitialData(db: AppDatabase) {
                val settingsDao = db.storeSettingsDao()
                settingsDao.insertOrUpdate(
                    StoreSettingsEntity(
                        id = 1,
                        storeName = "Abarrotes & Comercial El Éxito",
                        businessType = "Supermercado & POS",
                        phone = "+52 (55) 8765-4321",
                        address = "Av. Revolución #450, Centro",
                        taxRatePercent = 16.0,
                        currencySymbol = "$",
                        receiptFooter = "¡Gracias por su preferencia! Facturación en línea disponible.",
                        autoSyncEnabled = true,
                        lastSyncTimestamp = System.currentTimeMillis()
                    )
                )

                SampleData.populateSampleInventoryAndSales(db)
            }
        }
    }
}
