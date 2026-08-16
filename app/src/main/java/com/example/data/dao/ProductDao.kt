package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products ORDER BY name ASC")
    suspend fun getAllProductsList(): List<ProductEntity>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Long): ProductEntity?

    @Query("SELECT * FROM products WHERE sku = :sku OR barcode = :barcode LIMIT 1")
    suspend fun getProductBySkuOrBarcode(sku: String, barcode: String): ProductEntity?

    @Query("""
        SELECT * FROM products 
        WHERE (name LIKE '%' || :query || '%' OR sku LIKE '%' || :query || '%' OR barcode LIKE '%' || :query || '%')
        AND (:category = '' OR category = :category)
        ORDER BY name ASC
    """)
    fun searchProducts(query: String, category: String): Flow<List<ProductEntity>>

    @Query("SELECT DISTINCT category FROM products WHERE category != '' ORDER BY category ASC")
    fun getAllCategories(): Flow<List<String>>

    @Query("SELECT * FROM products WHERE currentStock > 0 AND currentStock <= minStock ORDER BY currentStock ASC")
    fun getLowStockProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE currentStock <= 0 ORDER BY name ASC")
    fun getOutOfStockProducts(): Flow<List<ProductEntity>>

    @Query("SELECT COUNT(*) FROM products")
    fun getProductsCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<ProductEntity>)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Query("UPDATE products SET currentStock = currentStock + :quantityDelta, updatedAt = :timestamp WHERE id = :productId")
    suspend fun adjustStock(productId: Long, quantityDelta: Double, timestamp: Long)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("DELETE FROM products WHERE id = :productId")
    suspend fun deleteProductById(productId: Long)

    @Query("DELETE FROM products")
    suspend fun deleteAll()
}
