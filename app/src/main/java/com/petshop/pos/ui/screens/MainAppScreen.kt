package com.petshop.pos.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import com.petshop.pos.ui.viewmodel.AnalyticsViewModel
import com.petshop.pos.ui.viewmodel.InventoryViewModel
import com.petshop.pos.ui.viewmodel.PosViewModel

enum class MainNavigationTab(
    val title: String,
    val icon: ImageVector
) {
    POS("Cashier", Icons.Default.PointOfSale),
    INVENTORY("Inventory", Icons.Default.Inventory2),
    SCANNER("Scanner", Icons.Default.QrCodeScanner),
    ANALYTICS("Analytics", Icons.Default.BarChart),
    SETTINGS("Settings", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    posViewModel: PosViewModel,
    inventoryViewModel: InventoryViewModel,
    analyticsViewModel: AnalyticsViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(MainNavigationTab.POS) }
    val lowStockCount by inventoryViewModel.lowStockCount.collectAsState()
    val posUiState by posViewModel.uiState.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar(
                tonalElevation = NavigationBarDefaults.Elevation
            ) {
                MainNavigationTab.entries.forEach { tab ->
                    val isSelected = selectedTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            selectedTab = tab
                            if (tab == MainNavigationTab.ANALYTICS) {
                                analyticsViewModel.refreshAnalytics()
                            }
                        },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (tab == MainNavigationTab.INVENTORY && lowStockCount > 0) {
                                        Badge { Text("$lowStockCount") }
                                    } else if (tab == MainNavigationTab.POS && posUiState.totalItemsCount > 0) {
                                        Badge { Text("${posUiState.totalItemsCount}") }
                                    }
                                }
                            ) {
                                Icon(tab.icon, contentDescription = tab.title)
                            }
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        when (selectedTab) {
            MainNavigationTab.POS -> {
                PosScreen(
                    viewModel = posViewModel,
                    onNavigateToScanner = { selectedTab = MainNavigationTab.SCANNER },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            MainNavigationTab.INVENTORY -> {
                InventoryScreen(
                    viewModel = inventoryViewModel,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            MainNavigationTab.SCANNER -> {
                ScannerScreen(
                    posViewModel = posViewModel,
                    onNavigateBack = { selectedTab = MainNavigationTab.POS },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            MainNavigationTab.ANALYTICS -> {
                AnalyticsScreen(
                    viewModel = analyticsViewModel,
                    onRestockClick = { selectedTab = MainNavigationTab.INVENTORY },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            MainNavigationTab.SETTINGS -> {
                SettingsScreen(
                    inventoryViewModel = inventoryViewModel,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}
