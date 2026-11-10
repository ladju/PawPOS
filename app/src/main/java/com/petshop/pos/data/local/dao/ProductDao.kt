package com.petshop.pos.data.local.dao

import androidx.room.*
import com.petshop.pos.data.local.entity.PetCategory
import com.petshop.pos.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Query("SELECT * FROM products WHERE isActive = 1 ORDER BY name ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE isActive = 1 AND category = :category ORDER BY name ASC")
    fun getProductsByCategory(category: PetCategory): Flow<List<ProductEntity>>

    @Query("""
        SELECT * FROM products 
        WHERE isActive = 1 AND (name LIKE '%' || :query || '%' OR barcode LIKE '%' || :query || '%')
        ORDER BY name ASC
    """)
    fun searchProducts(query: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE barcode = :barcode AND isActive = 1 LIMIT 1")
    suspend fun getProductByBarcode(barcode: String): ProductEntity?

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: Long): ProductEntity?

    @Query("SELECT * FROM products WHERE isActive = 1 AND currentStock <= minStockAlert ORDER BY currentStock ASC")
    fun getLowStockProducts(): Flow<List<ProductEntity>>

    @Query("SELECT COUNT(*) FROM products WHERE isActive = 1 AND currentStock <= minStockAlert")
    fun getLowStockCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM products WHERE isActive = 1")
    suspend fun getProductCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("UPDATE products SET currentStock = :newStock WHERE id = :productId")
    suspend fun updateStock(productId: Long, newStock: Int)
}
