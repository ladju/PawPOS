package com.petshop.pos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val invoiceNumber: String,
    val timestamp: Long = System.currentTimeMillis(),
    val subtotal: Double,
    val taxRate: Double = 0.0, // e.g. 0.05 for 5%
    val taxAmount: Double = 0.0,
    val discountAmount: Double = 0.0,
    val grandTotal: Double,
    val paymentMethod: PaymentMethod,
    val cashTendered: Double = 0.0,
    val changeGiven: Double = 0.0,
    val customerName: String? = null,
    val customerPhone: String? = null,
    val cashierName: String = "Staff",
    val notes: String = ""
)
