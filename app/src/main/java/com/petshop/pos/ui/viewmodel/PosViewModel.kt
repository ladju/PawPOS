package com.petshop.pos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petshop.pos.data.local.entity.PaymentMethod
import com.petshop.pos.data.local.entity.PetCategory
import com.petshop.pos.data.local.entity.ProductEntity
import com.petshop.pos.data.local.entity.TransactionEntity
import com.petshop.pos.data.local.entity.TransactionItemEntity
import com.petshop.pos.data.repository.CartItem
import com.petshop.pos.data.repository.PetShopRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class PosUiState(
    val selectedCategory: PetCategory = PetCategory.ALL,
    val searchQuery: String = "",
    val cartItems: List<CartItem> = emptyList(),
    val discountAmount: Double = 0.0,
    val taxRate: Double = 0.05, // 5% default sales tax
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val cashTendered: Double = 0.0,
    val customerName: String = "",
    val customerPhone: String = "",
    val isCheckoutDialogOpen: Boolean = false,
    val isReceiptDialogOpen: Boolean = false,
    val lastTransaction: TransactionEntity? = null,
    val lastTransactionItems: List<TransactionItemEntity> = emptyList(),
    val errorMessage: String? = null
) {
    val subtotal: Double
        get() = cartItems.sumOf { it.subtotal }

    val discountedSubtotal: Double
        get() = (subtotal - discountAmount).coerceAtLeast(0.0)

    val taxAmount: Double
        get() = discountedSubtotal * taxRate

    val grandTotal: Double
        get() = discountedSubtotal + taxAmount

    val totalItemsCount: Int
        get() = cartItems.sumOf { it.quantity }

    val changeDue: Double
        get() = if (paymentMethod == PaymentMethod.CASH) {
            (cashTendered - grandTotal).coerceAtLeast(0.0)
        } else 0.0
}

@OptIn(ExperimentalCoroutinesApi::class)
class PosViewModel(
    private val repository: PetShopRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PosUiState())
    val uiState: StateFlow<PosUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow(PetCategory.ALL)

    val products: StateFlow<List<ProductEntity>> = combine(
        _searchQuery,
        _selectedCategory
    ) { query, category ->
        Pair(query, category)
    }.flatMapLatest { (query, category) ->
        if (query.isNotBlank()) {
            repository.searchProducts(query)
        } else {
            repository.getProductsByCategory(category)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectCategory(category: PetCategory) {
        _selectedCategory.value = category
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun addToCart(product: ProductEntity) {
        _uiState.update { state ->
            val existingIndex = state.cartItems.indexOfFirst { it.product.id == product.id }
            val updatedList = state.cartItems.toMutableList()

            if (existingIndex >= 0) {
                val current = updatedList[existingIndex]
                // Check stock limit for tangible products
                if (current.quantity < product.currentStock || product.category == PetCategory.SERVICES) {
                    updatedList[existingIndex] = current.copy(quantity = current.quantity + 1)
                }
            } else {
                if (product.currentStock > 0 || product.category == PetCategory.SERVICES) {
                    updatedList.add(CartItem(product = product, quantity = 1))
                }
            }
            state.copy(cartItems = updatedList, errorMessage = null)
        }
    }

    fun scanBarcode(barcode: String) {
        viewModelScope.launch {
            val product = repository.getProductByBarcode(barcode.trim())
            if (product != null) {
                addToCart(product)
            } else {
                _uiState.update { it.copy(errorMessage = "Barcode '$barcode' not found in inventory!") }
            }
        }
    }

    fun updateQuantity(productId: Long, newQuantity: Int) {
        _uiState.update { state ->
            val updatedList = state.cartItems.mapNotNull { item ->
                if (item.product.id == productId) {
                    if (newQuantity <= 0) null
                    else item.copy(quantity = newQuantity.coerceAtMost(item.product.currentStock))
                } else item
            }
            state.copy(cartItems = updatedList)
        }
    }

    fun removeFromCart(productId: Long) {
        _uiState.update { state ->
            state.copy(cartItems = state.cartItems.filterNot { it.product.id == productId })
        }
    }

    fun clearCart() {
        _uiState.update { it.copy(cartItems = emptyList(), discountAmount = 0.0) }
    }

    fun setDiscount(amount: Double) {
        _uiState.update { it.copy(discountAmount = amount) }
    }

    fun setPaymentMethod(method: PaymentMethod) {
        _uiState.update { it.copy(paymentMethod = method) }
    }

    fun setCashTendered(amount: Double) {
        _uiState.update { it.copy(cashTendered = amount) }
    }

    fun setCustomerDetails(name: String, phone: String) {
        _uiState.update { it.copy(customerName = name, customerPhone = phone) }
    }

    fun openCheckoutDialog() {
        if (_uiState.value.cartItems.isNotEmpty()) {
            _uiState.update { 
                it.copy(
                    isCheckoutDialogOpen = true,
                    cashTendered = it.grandTotal // default to exact amount
                ) 
            }
        }
    }

    fun closeCheckoutDialog() {
        _uiState.update { it.copy(isCheckoutDialogOpen = false) }
    }

    fun completeCheckout() {
        val state = _uiState.value
        if (state.cartItems.isEmpty()) return

        if (state.paymentMethod == PaymentMethod.CASH && state.cashTendered < state.grandTotal) {
            _uiState.update { it.copy(errorMessage = "Tendered cash is less than grand total!") }
            return
        }

        viewModelScope.launch {
            val result = repository.processCheckout(
                cartItems = state.cartItems,
                paymentMethod = state.paymentMethod,
                discountAmount = state.discountAmount,
                taxRate = state.taxRate,
                cashTendered = state.cashTendered,
                customerName = state.customerName.ifBlank { null },
                customerPhone = state.customerPhone.ifBlank { null },
                cashierName = "Cashier #1"
            )

            result.onSuccess { transaction ->
                val items = repository.getTransactionItems(transaction.id)
                _uiState.update {
                    it.copy(
                        cartItems = emptyList(),
                        discountAmount = 0.0,
                        isCheckoutDialogOpen = false,
                        isReceiptDialogOpen = true,
                        lastTransaction = transaction,
                        lastTransactionItems = items,
                        customerName = "",
                        customerPhone = "",
                        errorMessage = null
                    )
                }
            }.onFailure { error ->
                _uiState.update { it.copy(errorMessage = error.localizedMessage ?: "Checkout failed") }
            }
        }
    }

    fun dismissReceipt() {
        _uiState.update { it.copy(isReceiptDialogOpen = false) }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
