package com.petshop.pos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petshop.pos.data.local.dao.TopSellerSummary
import com.petshop.pos.data.local.entity.ProductEntity
import com.petshop.pos.data.local.entity.TransactionEntity
import com.petshop.pos.data.local.entity.TransactionItemEntity
import com.petshop.pos.data.repository.DailySummary
import com.petshop.pos.data.repository.PetShopRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AnalyticsUiState(
    val dailySummary: DailySummary = DailySummary(0.0, 0.0, 0, 0),
    val topSellers: List<TopSellerSummary> = emptyList(),
    val lowStockItems: List<ProductEntity> = emptyList(),
    val selectedTransaction: TransactionEntity? = null,
    val selectedTransactionItems: List<TransactionItemEntity> = emptyList(),
    val isReceiptDetailOpen: Boolean = false,
    val isLoading: Boolean = false
)

class AnalyticsViewModel(
    private val repository: PetShopRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    val transactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        refreshAnalytics()
        observeLowStock()
    }

    private fun observeLowStock() {
        viewModelScope.launch {
            repository.lowStockProducts.collect { lowItems ->
                _uiState.update { it.copy(lowStockItems = lowItems) }
            }
        }
    }

    fun refreshAnalytics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val summary = repository.getTodayAnalytics()
            val topSellers = repository.getTopSellers(limit = 6)
            _uiState.update {
                it.copy(
                    dailySummary = summary,
                    topSellers = topSellers,
                    isLoading = false
                )
            }
        }
    }

    fun viewTransactionDetails(transaction: TransactionEntity) {
        viewModelScope.launch {
            val items = repository.getTransactionItems(transaction.id)
            _uiState.update {
                it.copy(
                    selectedTransaction = transaction,
                    selectedTransactionItems = items,
                    isReceiptDetailOpen = true
                )
            }
        }
    }

    fun closeReceiptDetail() {
        _uiState.update {
            it.copy(
                isReceiptDetailOpen = false,
                selectedTransaction = null,
                selectedTransactionItems = emptyList()
            )
        }
    }
}
