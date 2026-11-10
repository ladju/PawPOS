package com.petshop.pos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petshop.pos.data.local.entity.PetCategory
import com.petshop.pos.data.local.entity.ProductEntity
import com.petshop.pos.data.local.entity.ProductType
import com.petshop.pos.data.local.entity.StockChangeReason
import com.petshop.pos.data.repository.PetShopRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class StockFilter {
    ALL,
    LOW_STOCK,
    OUT_OF_STOCK
}

data class InventoryUiState(
    val searchQuery: String = "",
    val selectedCategory: PetCategory = PetCategory.ALL,
    val stockFilter: StockFilter = StockFilter.ALL,
    val isAddEditSheetOpen: Boolean = false,
    val isAdjustStockDialogOpen: Boolean = false,
    val selectedProductForEdit: ProductEntity? = null,
    val selectedProductForStockAdjust: ProductEntity? = null,
    val infoMessage: String? = null,
    val errorMessage: String? = null
)

class InventoryViewModel(
    private val repository: PetShopRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InventoryUiState())
    val uiState: StateFlow<InventoryUiState> = _uiState.asStateFlow()

    val lowStockCount: StateFlow<Int> = repository.lowStockCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val products: StateFlow<List<ProductEntity>> = combine(
        repository.allProducts,
        _uiState
    ) { all, state ->
        all.filter { prod ->
            val matchesCategory = (state.selectedCategory == PetCategory.ALL || prod.category == state.selectedCategory)
            val matchesSearch = state.searchQuery.isBlank() ||
                    prod.name.contains(state.searchQuery, ignoreCase = true) ||
                    prod.barcode.contains(state.searchQuery, ignoreCase = true)
            val matchesStock = when (state.stockFilter) {
                StockFilter.ALL -> true
                StockFilter.LOW_STOCK -> prod.isLowStock
                StockFilter.OUT_OF_STOCK -> prod.isOutOfStock
            }
            matchesCategory && matchesSearch && matchesStock
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun selectCategory(category: PetCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun selectStockFilter(filter: StockFilter) {
        _uiState.update { it.copy(stockFilter = filter) }
    }

    fun openAddProductDialog() {
        _uiState.update { 
            it.copy(
                isAddEditSheetOpen = true, 
                selectedProductForEdit = null 
            ) 
        }
    }

    fun openEditProductDialog(product: ProductEntity) {
        _uiState.update { 
            it.copy(
                isAddEditSheetOpen = true, 
                selectedProductForEdit = product 
            ) 
        }
    }

    fun closeAddEditDialog() {
        _uiState.update { it.copy(isAddEditSheetOpen = false, selectedProductForEdit = null) }
    }

    fun openAdjustStockDialog(product: ProductEntity) {
        _uiState.update {
            it.copy(
                isAdjustStockDialogOpen = true,
                selectedProductForStockAdjust = product
            )
        }
    }

    fun closeAdjustStockDialog() {
        _uiState.update { 
            it.copy(
                isAdjustStockDialogOpen = false, 
                selectedProductForStockAdjust = null 
            ) 
        }
    }

    fun saveProduct(
        id: Long,
        barcode: String,
        name: String,
        category: PetCategory,
        productType: ProductType,
        costPrice: Double,
        sellingPrice: Double,
        currentStock: Int,
        minStockAlert: Int,
        unit: String,
        expiryDate: String?,
        description: String
    ) {
        if (name.isBlank() || barcode.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Product name and barcode are required.") }
            return
        }

        viewModelScope.launch {
            val product = ProductEntity(
                id = id,
                barcode = barcode.trim(),
                name = name.trim(),
                category = category,
                productType = productType,
                costPrice = costPrice,
                sellingPrice = sellingPrice,
                currentStock = currentStock,
                minStockAlert = minStockAlert,
                unit = unit.ifBlank { "pcs" },
                expiryDate = expiryDate?.ifBlank { null },
                description = description.trim()
            )
            repository.saveProduct(product)
            _uiState.update {
                it.copy(
                    isAddEditSheetOpen = false,
                    selectedProductForEdit = null,
                    infoMessage = if (id == 0L) "Product created successfully!" else "Product updated successfully!"
                )
            }
        }
    }

    fun adjustStock(productId: Long, delta: Int, reason: StockChangeReason, note: String) {
        viewModelScope.launch {
            val success = repository.adjustStock(productId, delta, reason, note)
            if (success) {
                _uiState.update { 
                    it.copy(
                        isAdjustStockDialogOpen = false,
                        selectedProductForStockAdjust = null,
                        infoMessage = "Stock updated successfully!"
                    ) 
                }
            } else {
                _uiState.update { it.copy(errorMessage = "Failed to update stock.") }
            }
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            _uiState.update { it.copy(infoMessage = "Product '${product.name}' removed.") }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(infoMessage = null, errorMessage = null) }
    }
}
