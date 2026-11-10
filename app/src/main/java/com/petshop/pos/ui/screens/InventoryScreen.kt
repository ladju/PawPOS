package com.petshop.pos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.petshop.pos.data.local.entity.PetCategory
import com.petshop.pos.data.local.entity.ProductEntity
import com.petshop.pos.data.local.entity.ProductType
import com.petshop.pos.data.local.entity.StockChangeReason
import com.petshop.pos.ui.components.*
import com.petshop.pos.ui.theme.CoralAccent
import com.petshop.pos.ui.viewmodel.InventoryViewModel
import com.petshop.pos.ui.viewmodel.StockFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val products by viewModel.products.collectAsState()
    val lowStockCount by viewModel.lowStockCount.collectAsState()

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.openAddProductDialog() },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Product") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search field
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                placeholder = { Text("Search product name, barcode...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (uiState.searchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(Icons.Default.Clear, contentDescription = null)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Category filter pills
            PetCategoryPills(
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = { viewModel.selectCategory(it) }
            )

            // Stock Health filter tabs (All, Low Stock, Out of Stock)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.stockFilter == StockFilter.ALL,
                    onClick = { viewModel.selectStockFilter(StockFilter.ALL) },
                    label = { Text("All (${products.size})") }
                )
                FilterChip(
                    selected = uiState.stockFilter == StockFilter.LOW_STOCK,
                    onClick = { viewModel.selectStockFilter(StockFilter.LOW_STOCK) },
                    label = { Text("Low Stock (${lowStockCount})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFFEF3C7),
                        selectedLabelColor = Color(0xFFB45309)
                    )
                )
                FilterChip(
                    selected = uiState.stockFilter == StockFilter.OUT_OF_STOCK,
                    onClick = { viewModel.selectStockFilter(StockFilter.OUT_OF_STOCK) },
                    label = { Text("Out of Stock") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFFFE4E6),
                        selectedLabelColor = CoralAccent
                    )
                )
            }

            // Low Stock Warning Banner
            if (lowStockCount > 0 && uiState.stockFilter == StockFilter.ALL) {
                Surface(
                    color = Color(0xFFFEF3C7),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFB45309),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$lowStockCount items are running low on stock! Tap 'Low Stock' to review.",
                            color = Color(0xFFB45309),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Products List
            if (products.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "📦", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No inventory items found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(products, key = { it.id }) { product ->
                        InventoryItemCard(
                            product = product,
                            onAdjustStock = { viewModel.openAdjustStockDialog(product) },
                            onEdit = { viewModel.openEditProductDialog(product) },
                            onDelete = { viewModel.deleteProduct(product) }
                        )
                    }
                }
            }
        }
    }

    // Add / Edit Product Sheet
    if (uiState.isAddEditSheetOpen) {
        AddEditProductSheet(
            product = uiState.selectedProductForEdit,
            onDismiss = { viewModel.closeAddEditDialog() },
            onSave = { id, barcode, name, cat, type, cost, price, stock, minStock, unit, exp, desc ->
                viewModel.saveProduct(
                    id, barcode, name, cat, type, cost, price, stock, minStock, unit, exp, desc
                )
            }
        )
    }

    // Quick Stock Adjustment Dialog
    if (uiState.isAdjustStockDialogOpen && uiState.selectedProductForStockAdjust != null) {
        AdjustStockDialog(
            product = uiState.selectedProductForStockAdjust!!,
            onDismiss = { viewModel.closeAdjustStockDialog() },
            onConfirm = { delta, reason, note ->
                viewModel.adjustStock(
                    uiState.selectedProductForStockAdjust!!.id,
                    delta,
                    reason,
                    note
                )
            }
        )
    }
}

@Composable
fun InventoryItemCard(
    product: ProductEntity,
    onAdjustStock: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = product.category.icon,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(0.68f)
                    )
                }

                StockBadge(
                    currentStock = product.currentStock,
                    minAlert = product.minStockAlert,
                    isService = product.productType == ProductType.SERVICE || product.category == PetCategory.SERVICES
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Barcode & Category info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Barcode: ${product.barcode}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                if (product.expiryDate != null) {
                    Text(
                        text = "Exp: ${product.expiryDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFB45309)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Pricing & Profit margin
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Cost: ${formatCurrency(product.costPrice)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "Price: ${formatCurrency(product.sellingPrice)}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Margin: %.1f%%".format(product.profitMargin),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (product.profitMargin > 30) Color(0xFF047857) else Color(0xFFB45309)
                    )
                    Text(
                        text = "${product.currentStock} ${product.unit}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onAdjustStock,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.SwapVert, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Adjust Stock", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(onClick = onEdit, modifier = Modifier.size(34.dp)) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductSheet(
    product: ProductEntity?,
    onDismiss: () -> Unit,
    onSave: (Long, String, String, PetCategory, ProductType, Double, Double, Int, Int, String, String?, String) -> Unit
) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var barcode by remember { mutableStateOf(product?.barcode ?: "") }
    var selectedCategory by remember { mutableStateOf(product?.category ?: PetCategory.DOGS) }
    var selectedType by remember { mutableStateOf(product?.productType ?: ProductType.FOOD) }
    var costPrice by remember { mutableStateOf(product?.costPrice?.toString() ?: "") }
    var sellingPrice by remember { mutableStateOf(product?.sellingPrice?.toString() ?: "") }
    var currentStock by remember { mutableStateOf(product?.currentStock?.toString() ?: "10") }
    var minStockAlert by remember { mutableStateOf(product?.minStockAlert?.toString() ?: "5") }
    var unit by remember { mutableStateOf(product?.unit ?: "pcs") }
    var expiryDate by remember { mutableStateOf(product?.expiryDate ?: "") }
    var description by remember { mutableStateOf(product?.description ?: "") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = if (product == null) "Add New Product" else "Edit Product",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Product Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = barcode,
                onValueChange = { barcode = it },
                label = { Text("Barcode / SKU *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Category selector
            Text(text = "Pet Category", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(4.dp))
            PetCategoryPills(
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Pricing Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = costPrice,
                    onValueChange = { costPrice = it },
                    label = { Text("Cost Price (Rp)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = sellingPrice,
                    onValueChange = { sellingPrice = it },
                    label = { Text("Selling Price (Rp) *") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Stock row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = currentStock,
                    onValueChange = { currentStock = it },
                    label = { Text("Stock Qty") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = minStockAlert,
                    onValueChange = { minStockAlert = it },
                    label = { Text("Min Stock Alert") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text("Unit (pcs/kg)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = expiryDate,
                onValueChange = { expiryDate = it },
                label = { Text("Expiry Date (YYYY-MM-DD, optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description / Notes") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    val cost = costPrice.toDoubleOrNull() ?: 0.0
                    val sell = sellingPrice.toDoubleOrNull() ?: 0.0
                    val stock = currentStock.toIntOrNull() ?: 0
                    val alert = minStockAlert.toIntOrNull() ?: 5
                    onSave(
                        product?.id ?: 0L,
                        barcode,
                        name,
                        selectedCategory,
                        selectedType,
                        cost,
                        sell,
                        stock,
                        alert,
                        unit,
                        expiryDate,
                        description
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Product", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun AdjustStockDialog(
    product: ProductEntity,
    onDismiss: () -> Unit,
    onConfirm: (Int, StockChangeReason, String) -> Unit
) {
    var deltaInput by remember { mutableStateOf("10") }
    var selectedReason by remember { mutableStateOf(StockChangeReason.RESTOCK) }
    var note by remember { mutableStateOf("") }
    var isAddition by remember { mutableStateOf(true) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Adjust Stock",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = "Current Stock: ${product.currentStock} ${product.unit}",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Toggle Add / Deduct
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = isAddition,
                        onClick = {
                            isAddition = true
                            selectedReason = StockChangeReason.RESTOCK
                        },
                        label = { Text("+ Add Stock (In)") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = !isAddition,
                        onClick = {
                            isAddition = false
                            selectedReason = StockChangeReason.DAMAGED
                        },
                        label = { Text("- Deduct Stock (Out)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = deltaInput,
                    onValueChange = { deltaInput = it },
                    label = { Text("Quantity Amount") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(text = "Adjustment Reason:", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(4.dp))
                StockChangeReason.entries
                    .filter { if (isAddition) it == StockChangeReason.RESTOCK || it == StockChangeReason.AUDIT else it != StockChangeReason.RESTOCK }
                    .forEach { reason ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedReason = reason }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedReason == reason,
                                onClick = { selectedReason = reason }
                            )
                            Text(text = reason.displayName, fontSize = 13.sp)
                        }
                    }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (e.g. Supplier invoice #, expiry batch)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            val qty = deltaInput.toIntOrNull() ?: 0
                            val signedQty = if (isAddition) qty else -qty
                            onConfirm(signedQty, selectedReason, note)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Confirm")
                    }
                }
            }
        }
    }
}
