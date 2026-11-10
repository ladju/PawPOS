package com.petshop.pos.data.local.dao

import androidx.room.*
import com.petshop.pos.data.local.entity.StockMovementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StockMovementDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovement(movement: StockMovementEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovements(movements: List<StockMovementEntity>)

    @Query("SELECT * FROM stock_movements WHERE productId = :productId ORDER BY timestamp DESC")
    fun getMovementsForProduct(productId: Long): Flow<List<StockMovementEntity>>

    @Query("SELECT * FROM stock_movements ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentMovements(limit: Int = 50): Flow<List<StockMovementEntity>>
}
