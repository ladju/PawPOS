package com.petshop.pos.data.local.dao

import androidx.room.*
import com.petshop.pos.data.local.entity.PetCategory
import com.petshop.pos.data.local.entity.TransactionEntity
import com.petshop.pos.data.local.entity.TransactionItemEntity
import kotlinx.coroutines.flow.Flow

data class TopSellerSummary(
    val productName: String,
    val category: PetCategory,
    val totalQuantity: Int,
    val totalRevenue: Double
)

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactionItems(items: List<TransactionItemEntity>)

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Query("SELECT * FROM transaction_items WHERE transactionId = :transactionId")
    suspend fun getItemsForTransaction(transactionId: Long): List<TransactionItemEntity>

    @Query("SELECT * FROM transactions WHERE timestamp >= :sinceTimestamp ORDER BY timestamp DESC")
    suspend fun getTransactionsSince(sinceTimestamp: Long): List<TransactionEntity>

    @Query("""
        SELECT 
            productName,
            category,
            SUM(quantity) as totalQuantity,
            SUM(subtotal) as totalRevenue
        FROM transaction_items
        GROUP BY productName, category
        ORDER BY totalQuantity DESC
        LIMIT :limit
    """)
    suspend fun getTopSellers(limit: Int): List<TopSellerSummary>

    @Query("SELECT COUNT(*) FROM transactions")
    fun getTotalTransactionsCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(grandTotal), 0.0) FROM transactions WHERE timestamp >= :sinceTimestamp")
    suspend fun getRevenueSince(sinceTimestamp: Long): Double
}
