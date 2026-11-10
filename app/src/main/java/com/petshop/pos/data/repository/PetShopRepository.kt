package com.petshop.pos.data.repository

import androidx.room.withTransaction
import com.petshop.pos.data.local.PetShopDatabase
import com.petshop.pos.data.local.dao.TopSellerSummary
import com.petshop.pos.data.local.entity.*
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

data class CartItem(
    val product: ProductEntity,
    val quantity: Int = 1
) {
    val subtotal: Double
        get() = product.sellingPrice * quantity
}

data class DailySummary(
    val totalRevenue: Double,
    val totalProfit: Double,
    val transactionCount: Int,
    val itemsSold: Int
)

class PetShopRepository(
    private val database: PetShopDatabase
) {
    private val productDao = database.productDao()
    private val transactionDao = database.transactionDao()
    private val stockMovementDao = database.stockMovementDao()

    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()
    val lowStockProducts: Flow<List<ProductEntity>> = productDao.getLowStockProducts()
    val lowStockCount: Flow<Int> = productDao.getLowStockCount()
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val totalTransactionsCount: Flow<Int> = transactionDao.getTotalTransactionsCount()

    fun getProductsByCategory(category: PetCategory): Flow<List<ProductEntity>> {
        return if (category == PetCategory.ALL) {
            productDao.getAllProducts()
        } else {
            productDao.getProductsByCategory(category)
        }
    }

    fun searchProducts(query: String): Flow<List<ProductEntity>> {
        return productDao.searchProducts(query)
    }

    suspend fun getProductByBarcode(barcode: String): ProductEntity? {
        return productDao.getProductByBarcode(barcode)
    }

    suspend fun getProductById(id: Long): ProductEntity? {
        return productDao.getProductById(id)
    }

    suspend fun saveProduct(product: ProductEntity): Long {
        return if (product.id == 0L) {
            val id = productDao.insertProduct(product)
            // Log initial stock movement
            if (product.currentStock > 0) {
                stockMovementDao.insertMovement(
                    StockMovementEntity(
                        productId = id,
                        productName = product.name,
                        quantityChange = product.currentStock,
                        previousStock = 0,
                        resultingStock = product.currentStock,
                        reason = StockChangeReason.RESTOCK,
                        note = "Initial product catalog setup"
                    )
                )
            }
            id
        } else {
            productDao.updateProduct(product)
            product.id
        }
    }

    suspend fun adjustStock(
        productId: Long,
        delta: Int,
        reason: StockChangeReason,
        note: String
    ): Boolean {
        val product = productDao.getProductById(productId) ?: return false
        val newStock = (product.currentStock + delta).coerceAtLeast(0)

        productDao.updateStock(productId, newStock)
        stockMovementDao.insertMovement(
            StockMovementEntity(
                productId = productId,
                productName = product.name,
                quantityChange = delta,
                previousStock = product.currentStock,
                resultingStock = newStock,
                reason = reason,
                note = note
            )
        )
        return true
    }

    suspend fun deleteProduct(product: ProductEntity) {
        productDao.deleteProduct(product)
    }

    /**
     * Executes atomic POS checkout:
     * 1. Inserts the main sale transaction
     * 2. Inserts all individual sale line items
     * 3. Automatically decrements inventory stocks
     * 4. Logs stock movements
     */
    suspend fun processCheckout(
        cartItems: List<CartItem>,
        paymentMethod: PaymentMethod,
        discountAmount: Double = 0.0,
        taxRate: Double = 0.0,
        cashTendered: Double = 0.0,
        customerName: String? = null,
        customerPhone: String? = null,
        cashierName: String = "Staff",
        notes: String = ""
    ): Result<TransactionEntity> {
        if (cartItems.isEmpty()) {
            return Result.failure(IllegalArgumentException("Cart is empty"))
        }

        val subtotal = cartItems.sumOf { it.subtotal }
        val discountedSubtotal = (subtotal - discountAmount).coerceAtLeast(0.0)
        val taxAmount = discountedSubtotal * taxRate
        val grandTotal = discountedSubtotal + taxAmount
        val changeGiven = if (paymentMethod == PaymentMethod.CASH) {
            (cashTendered - grandTotal).coerceAtLeast(0.0)
        } else 0.0

        val dateFormat = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault())
        val invoiceNumber = "INV-${dateFormat.format(Date())}"

        val transaction = TransactionEntity(
            invoiceNumber = invoiceNumber,
            timestamp = System.currentTimeMillis(),
            subtotal = subtotal,
            taxRate = taxRate,
            taxAmount = taxAmount,
            discountAmount = discountAmount,
            grandTotal = grandTotal,
            paymentMethod = paymentMethod,
            cashTendered = cashTendered,
            changeGiven = changeGiven,
            customerName = customerName,
            customerPhone = customerPhone,
            cashierName = cashierName,
            notes = notes
        )

        return try {
            var savedTransaction = transaction
            database.withTransaction {
                val transactionId = transactionDao.insertTransaction(transaction)
                savedTransaction = transaction.copy(id = transactionId)

                val lineItems = cartItems.map { item ->
                    TransactionItemEntity(
                        transactionId = transactionId,
                        productId = item.product.id,
                        productName = item.product.name,
                        productBarcode = item.product.barcode,
                        category = item.product.category,
                        unitPrice = item.product.sellingPrice,
                        costPrice = item.product.costPrice,
                        quantity = item.quantity,
                        subtotal = item.subtotal
                    )
                }
                transactionDao.insertTransactionItems(lineItems)

                // Deduct stock for inventory items (services with unlimited stock like 999 are not depleted)
                for (item in cartItems) {
                    if (item.product.productType != ProductType.SERVICE) {
                        val currentProd = productDao.getProductById(item.product.id)
                        if (currentProd != null) {
                            val newStock = (currentProd.currentStock - item.quantity).coerceAtLeast(0)
                            productDao.updateStock(item.product.id, newStock)
                            stockMovementDao.insertMovement(
                                StockMovementEntity(
                                    productId = item.product.id,
                                    productName = item.product.name,
                                    quantityChange = -item.quantity,
                                    previousStock = currentProd.currentStock,
                                    resultingStock = newStock,
                                    reason = StockChangeReason.SALE,
                                    referenceInvoice = invoiceNumber,
                                    note = "POS Checkout #$invoiceNumber"
                                )
                            )
                        }
                    }
                }
            }
            Result.success(savedTransaction)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTransactionItems(transactionId: Long): List<TransactionItemEntity> {
        return transactionDao.getItemsForTransaction(transactionId)
    }

    suspend fun getTopSellers(limit: Int = 5): List<TopSellerSummary> {
        return transactionDao.getTopSellers(limit)
    }

    suspend fun getTodayAnalytics(): DailySummary {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = calendar.timeInMillis
        val transactions = transactionDao.getTransactionsSince(startOfDay)

        var totalRevenue = 0.0
        var totalProfit = 0.0
        var totalItems = 0

        for (tx in transactions) {
            totalRevenue += tx.grandTotal
            val items = transactionDao.getItemsForTransaction(tx.id)
            for (item in items) {
                totalItems += item.quantity
                val itemCost = item.costPrice * item.quantity
                val itemRev = item.subtotal
                totalProfit += (itemRev - itemCost)
            }
        }

        return DailySummary(
            totalRevenue = totalRevenue,
            totalProfit = totalProfit.coerceAtLeast(0.0),
            transactionCount = transactions.size,
            itemsSold = totalItems
        )
    }
}
