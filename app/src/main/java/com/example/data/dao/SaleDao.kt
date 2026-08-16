package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.entity.SaleEntity
import com.example.data.entity.SaleItemEntity
import com.example.data.model.SaleWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {
    @Transaction
    @Query("SELECT * FROM sales ORDER BY timestamp DESC")
    fun getAllSalesWithItems(): Flow<List<SaleWithItems>>

    @Transaction
    @Query("SELECT * FROM sales ORDER BY timestamp DESC")
    suspend fun getAllSalesWithItemsList(): List<SaleWithItems>

    @Transaction
    @Query("SELECT * FROM sales WHERE id = :saleId")
    suspend fun getSaleWithItemsById(saleId: Long): SaleWithItems?

    @Transaction
    @Query("""
        SELECT * FROM sales 
        WHERE timestamp >= :startTime AND timestamp <= :endTime AND status = 'COMPLETADA'
        ORDER BY timestamp DESC
    """)
    fun getCompletedSalesInRange(startTime: Long, endTime: Long): Flow<List<SaleWithItems>>

    @Query("SELECT * FROM sales WHERE timestamp >= :startTime AND timestamp <= :endTime AND status = 'COMPLETADA'")
    suspend fun getCompletedSalesListInRange(startTime: Long, endTime: Long): List<SaleEntity>

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    suspend fun getItemsForSale(saleId: Long): List<SaleItemEntity>

    @Query("SELECT * FROM sale_items")
    suspend fun getAllSaleItems(): List<SaleItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SaleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleItems(items: List<SaleItemEntity>)

    @Update
    suspend fun updateSale(sale: SaleEntity)

    @Query("UPDATE sales SET status = :status, isSynced = 0 WHERE id = :saleId")
    suspend fun updateSaleStatus(saleId: Long, status: String)

    @Query("SELECT COUNT(*) FROM sales")
    suspend fun getSalesCount(): Int

    @Query("DELETE FROM sales")
    suspend fun deleteAllSales()

    @Query("DELETE FROM sale_items")
    suspend fun deleteAllSaleItems()
}
