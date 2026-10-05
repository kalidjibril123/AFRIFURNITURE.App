package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.BusinessViewModel
import com.example.ui.viewmodel.Screen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: BusinessViewModel = viewModel()
                val currentScreen by viewModel.currentScreen.collectAsState()

                // Dynamic Android Back button interceptor
                BackHandler(enabled = currentScreen != Screen.Dashboard) {
                    viewModel.navigateBack()
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    val modifier = Modifier.padding(innerPadding)
                    when (val screen = currentScreen) {
                        is Screen.Dashboard -> DashboardScreen(viewModel = viewModel, modifier = modifier)
                        
                        is Screen.FurnitureList -> FurnitureListScreen(viewModel = viewModel, modifier = modifier)
                        is Screen.NewFurnitureOrder -> NewFurnitureOrderScreen(viewModel = viewModel, modifier = modifier)
                        is Screen.EditFurnitureOrder -> NewFurnitureOrderScreen(viewModel = viewModel, orderIdToEdit = screen.orderId, modifier = modifier)
                        
                        is Screen.MetalList -> MetalListScreen(viewModel = viewModel, modifier = modifier)
                        is Screen.NewMetalOrder -> NewMetalOrderScreen(viewModel = viewModel, modifier = modifier)
                        is Screen.EditMetalOrder -> NewMetalOrderScreen(viewModel = viewModel, orderIdToEdit = screen.orderId, modifier = modifier)
                        
                        is Screen.AluminiumList -> AluminiumListScreen(viewModel = viewModel, modifier = modifier)
                        is Screen.NewAluminiumOrder -> NewAluminiumOrderScreen(viewModel = viewModel, modifier = modifier)
                        is Screen.EditAluminiumOrder -> NewAluminiumOrderScreen(viewModel = viewModel, orderIdToEdit = screen.orderId, modifier = modifier)
                        
                        is Screen.Workers -> WorkerManagementScreen(viewModel = viewModel, modifier = modifier)
                        is Screen.AssignWork -> WorkerManagementScreen(viewModel = viewModel, initialTab = 1, modifier = modifier)
                        
                        is Screen.MaterialPurchaseList -> MaterialManagementScreen(viewModel = viewModel, initialTab = 0, modifier = modifier)
                        is Screen.NewPurchase -> MaterialManagementScreen(viewModel = viewModel, initialTab = 0, modifier = modifier)
                        is Screen.SalesList -> MaterialManagementScreen(viewModel = viewModel, initialTab = 1, modifier = modifier)
                        is Screen.NewSale -> MaterialManagementScreen(viewModel = viewModel, initialTab = 1, modifier = modifier)
                        is Screen.Inventory -> MaterialManagementScreen(viewModel = viewModel, initialTab = 2, modifier = modifier)
                        
                        is Screen.Customers -> CustomersScreen(viewModel = viewModel, modifier = modifier)
                        
                        is Screen.Finance -> FinanceScreen(viewModel = viewModel, initialTab = 0, modifier = modifier)
                        is Screen.Reports -> FinanceScreen(viewModel = viewModel, initialTab = 1, modifier = modifier)
                        
                        is Screen.Settings -> SettingsScreen(viewModel = viewModel, modifier = modifier)
                        
                        is Screen.ReceiptView -> ReceiptScreen(
                            viewModel = viewModel,
                            orderId = screen.orderId,
                            orderType = screen.orderType,
                            modifier = modifier
                        )
                    }
                }
            }
        }
    }
}
