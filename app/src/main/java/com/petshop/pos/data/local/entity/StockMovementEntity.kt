package com.petshop.pos.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "stock_movements",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("productId")]
)
data class StockMovementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productId: Long,
    val productName: String,
    val quantityChange: Int, // positive for restock, negative for sale/damaged
    val previousStock: Int,
    val resultingStock: Int,
    val reason: StockChangeReason,
    val timestamp: Long = System.currentTimeMillis(),
    val referenceInvoice: String? = null,
    val note: String = ""
)
