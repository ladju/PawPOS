package com.petshop.pos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.petshop.pos.data.local.entity.PetCategory
import com.petshop.pos.data.local.entity.ProductEntity
import com.petshop.pos.data.local.entity.ProductType
import com.petshop.pos.ui.components.PetCategoryPills
import com.petshop.pos.ui.components.formatCurrency
import com.petshop.pos.ui.viewmodel.InventoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    inventoryViewModel: InventoryViewModel,
    modifier: Modifier = Modifier
) {
    val products by inventoryViewModel.products.collectAsState()

    // Quick Add Product Form State
    var newName by remember { mutableStateOf("") }
    var newBarcode by remember { mutableStateOf("899" + (1000..9999).random()) }
    var newCategory by remember { mutableStateOf(PetCategory.DOGS) }
    var newCostPrice by remember { mutableStateOf("") }
    var newSellingPrice by remember { mutableStateOf("") }
    var newStock by remember { mutableStateOf("10") }
    var newUnit by remember { mutableStateOf("pcs") }
    var feedbackBanner by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Settings, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Settings & Product Manager (IDR)", fontWeight = FontWeight.Bold)
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Currency & Store Configuration Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Store Currency",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "🇮🇩 Indonesian Rupiah (IDR - Rp)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "ACTIVE",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Feedback Banner
            feedbackBanner?.let { msg ->
                item {
                    Surface(
                        color = Color(0xFFD1FAE5),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = msg,
                                color = Color(0xFF047857),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { feedbackBanner = null }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF047857))
                            }
                        }
                    }
                }
            }

            // Section 1: Add New Product Form
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "➕ Add New Product",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Create a new pet product and set its initial price (IDR) and stock",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = newName,
                            onValueChange = { newName = it },
                            label = { Text("Product Name *") },
                            placeholder = { Text("e.g. Bolt Tuna Cat Food 1kg") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = newBarcode,
                                onValueChange = { newBarcode = it },
                                label = { Text("Barcode / SKU *") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = newUnit,
                                onValueChange = { newUnit = it },
                                label = { Text("Unit (bag/pcs)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Category", style = MaterialTheme.typography.labelMedium)
                        PetCategoryPills(
                            selectedCategory = newCategory,
                            onCategorySelected = { if (it != PetCategory.ALL) newCategory = it }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = newCostPrice,
                                onValueChange = { newCostPrice = it },
                                label = { Text("Cost Price (Rp)") },
                                placeholder = { Text("18000") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = newSellingPrice,
                                onValueChange = { newSellingPrice = it },
                                label = { Text("Selling Price (Rp) *") },
                                placeholder = { Text("28000") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = newStock,
                                onValueChange = { newStock = it },
                                label = { Text("Initial Stock *") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(0.8f)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                val sell = newSellingPrice.toDoubleOrNull() ?: 0.0
                                val cost = newCostPrice.toDoubleOrNull() ?: (sell * 0.7)
                                val stock = newStock.toIntOrNull() ?: 0
                                if (newName.isNotBlank() && sell > 0) {
                                    inventoryViewModel.saveProduct(
                                        id = 0L,
                                        barcode = newBarcode.ifBlank { "899" + (1000..9999).random() },
                                        name = newName,
                                        category = newCategory,
                                        productType = if (newCategory == PetCategory.SERVICES) ProductType.SERVICE else ProductType.FOOD,
                                        costPrice = cost,
                                        sellingPrice = sell,
                                        currentStock = stock,
                                        minStockAlert = 5,
                                        unit = newUnit.ifBlank { "pcs" },
                                        expiryDate = null,
                                        description = ""
                                    )
                                    feedbackBanner = "✓ Added '${newName}' at ${formatCurrency(sell)} (Stock: $stock)"
                                    newName = ""
                                    newCostPrice = ""
                                    newSellingPrice = ""
                                    newStock = "10"
                                    newBarcode = "899" + (1000..9999).random()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.AddCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Product to Catalog", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Section 2: Quick Price & Stock Editor List
            item {
                Text(
                    text = "⚡ Quick Price & Stock Editor (${products.size} Products)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Directly edit Selling Price (Rp) and Stock Qty for any item below",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            items(products, key = { it.id }) { product ->
                QuickEditProductRow(
                    product = product,
                    onUpdatePriceAndStock = { updatedPrice, updatedStock ->
                        inventoryViewModel.saveProduct(
                            id = product.id,
                            barcode = product.barcode,
                            name = product.name,
                            category = product.category,
                            productType = product.productType,
                            costPrice = product.costPrice,
                            sellingPrice = updatedPrice,
                            currentStock = updatedStock,
                            minStockAlert = product.minStockAlert,
                            unit = product.unit,
                            expiryDate = product.expiryDate,
                            description = product.description
                        )
                        feedbackBanner = "✓ Updated '${product.name}' -> ${formatCurrency(updatedPrice)} | Stock: $updatedStock"
                    }
                )
            }
        }
    }
}

@Composable
private fun QuickEditProductRow(
    product: ProductEntity,
    onUpdatePriceAndStock: (Double, Int) -> Unit
) {
    var priceInput by remember(product.sellingPrice) {
        mutableStateOf(product.sellingPrice.toLong().toString())
    }
    var stockInput by remember(product.currentStock) {
        mutableStateOf(product.currentStock.toString())
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${product.category.icon} ${product.name}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "#${product.barcode}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = priceInput,
                    onValueChange = { priceInput = it },
                    label = { Text("Price (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1.2f)
                )

                OutlinedTextField(
                    value = stockInput,
                    onValueChange = { stockInput = it },
                    label = { Text("Stock (${product.unit})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(0.9f)
                )

                Button(
                    onClick = {
                        val parsedPrice = priceInput.toDoubleOrNull() ?: product.sellingPrice
                        val parsedStock = stockInput.toIntOrNull() ?: product.currentStock
                        onUpdatePriceAndStock(parsedPrice, parsedStock)
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(54.dp)
                ) {
                    Text("Save", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
