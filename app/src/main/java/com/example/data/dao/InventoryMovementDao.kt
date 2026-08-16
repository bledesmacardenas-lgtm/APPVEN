package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.InventoryMovementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryMovementDao {
    @Query("SELECT * FROM inventory_movements ORDER BY timestamp DESC")
    fun getAllMovements(): Flow<List<InventoryMovementEntity>>

    @Query("SELECT * FROM inventory_movements ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentMovements(limit: Int = 50): Flow<List<InventoryMovementEntity>>

    @Query("SELECT * FROM inventory_movements WHERE productId = :productId ORDER BY timestamp DESC")
    fun getMovementsForProduct(productId: Long): Flow<List<InventoryMovementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovement(movement: InventoryMovementEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(movements: List<InventoryMovementEntity>)

    @Query("DELETE FROM inventory_movements")
    suspend fun deleteAllMovements()
}
