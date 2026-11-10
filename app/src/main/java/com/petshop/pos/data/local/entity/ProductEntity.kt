package com.petshop.pos.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "products",
    indices = [
        Index(value = ["barcode"], unique = false),
        Index(value = ["category"])
    ]
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val barcode: String,
    val name: String,
    val category: PetCategory,
    val productType: ProductType,
    val costPrice: Double,
    val sellingPrice: Double,
    val currentStock: Int,
    val minStockAlert: Int = 5,
    val unit: String = "pcs",
    val expiryDate: String? = null,
    val description: String = "",
    val isActive: Boolean = true
) {
    val isLowStock: Boolean
        get() = currentStock <= minStockAlert && currentStock > 0

    val isOutOfStock: Boolean
        get() = currentStock <= 0

    val profitMargin: Double
        get() = if (sellingPrice > 0) ((sellingPrice - costPrice) / sellingPrice) * 100.0 else 0.0
}
