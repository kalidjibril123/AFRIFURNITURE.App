package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MaterialPurchase
import com.example.data.Sale
import com.example.ui.viewmodel.BusinessViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MaterialManagementScreen(
    viewModel: BusinessViewModel,
    initialTab: Int = 0,
    modifier: Modifier = Modifier
) {
    val purchases by viewModel.materialPurchases.collectAsState(initial = emptyList())
    val sales by viewModel.sales.collectAsState(initial = emptyList())
    val inventory by viewModel.inventoryList.collectAsState(initial = emptyList())
    val language by viewModel.currentLanguage.collectAsState()

    var selectedTab by remember { mutableStateOf(initialTab) } // 0: Purchases, 1: Sales, 2: Inventory Balance

    var showAddPurchaseDialog by remember { mutableStateOf(false) }
    var showAddSaleDialog by remember { mutableStateOf(false) }

    var purchaseToDelete by remember { mutableStateOf<MaterialPurchase?>(null) }
    var saleToDelete by remember { mutableStateOf<Sale?>(null) }

    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            SimpleTopAppBar(
                title = viewModel.t("inventory"),
                onBackClick = { viewModel.navigateBack() },
                actionIcon = if (selectedTab == 0) Icons.Default.AddShoppingCart else if (selectedTab == 1) Icons.Default.AddCard else null,
                onActionClick = {
                    if (selectedTab == 0) showAddPurchaseDialog = true else if (selectedTab == 1) showAddSaleDialog = true
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Three-tab Header
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text(viewModel.t("purchase_btn"), fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(viewModel.t("sales_btn"), fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text(if (language.name == "OROMO") "Kuusa" else "Stock Balance", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Search Bar for Purchases/Sales
                if (selectedTab != 2) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text(viewModel.t("search_hint")) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .testTag("material_search_input")
                    )
                }

                when (selectedTab) {
                    0 -> {
                        // --- MATERIAL PURCHASES HISTORICAL LIST ---
                        val filteredPurchases = purchases.filter {
                            it.materialName.contains(searchQuery, ignoreCase = true) ||
                                    it.supplierName.contains(searchQuery, ignoreCase = true) ||
                                    it.category.contains(searchQuery, ignoreCase = true)
                        }

                        if (filteredPurchases.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No purchase records found.", color = Color.Gray)
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(filteredPurchases) { p ->
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = p.purchaseNumber,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Text(text = p.date, fontSize = 12.sp, color = Color.Gray)
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(text = "${p.materialName} (${p.category})", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                            Text(text = "Supplier: ${p.supplierName} | Tel: ${p.supplierPhone}", fontSize = 13.sp, color = Color.Gray)
                                            Text(text = "Qty: ${p.quantity} ${p.unit} × ETB ${p.purchasePricePerUnit}/${p.unit}", fontSize = 13.sp, color = Color.Gray)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                                            Spacer(modifier = Modifier.height(8.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column {
                                                    Text(viewModel.t("total_cost"), fontSize = 10.sp, color = Color.Gray)
                                                    Text("ETB ${String.format(Locale.US, "%,.1f", p.totalCost)}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                }
                                                Column {
                                                    Text(viewModel.t("amount_paid"), fontSize = 10.sp, color = Color.Gray)
                                                    Text("ETB ${String.format(Locale.US, "%,.1f", p.paymentMade)}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1B5E20))
                                                }
                                                Column {
                                                    Text("Supplier Balance", fontSize = 10.sp, color = Color.Gray)
                                                    Text("ETB ${String.format(Locale.US, "%,.1f", p.remainingSupplierBalance)}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (p.remainingSupplierBalance > 0) Color.Red else Color.DarkGray)
                                                }
                                                IconButton(
                                                    onClick = { purchaseToDelete = p },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(Icons.Default.Delete, "Delete", tint = Color.Red, modifier = Modifier.size(20.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        // --- SALES / MATERIAL SELLING HISTORICAL LIST ---
                        val filteredSales = sales.filter {
                            it.materialName.contains(searchQuery, ignoreCase = true) ||
                                    it.customerName.contains(searchQuery, ignoreCase = true) ||
                                    it.category.contains(searchQuery, ignoreCase = true)
                        }

                        // Computes today's, weekly, and monthly totals
                        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                        val todaySalesSum = sales.filter { it.date == todayStr }.sumOf { it.totalSale }
                        val totalSalesSum = sales.sumOf { it.totalSale }

                        Column(modifier = Modifier.fillMaxSize()) {
                            // Sales Quick metrics Header
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(text = "Today's Sales", fontSize = 11.sp, color = Color.Gray)
                                        Text(text = "ETB ${String.format(Locale.US, "%,.1f", todaySalesSum)}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                    Column {
                                        Text(text = "All Material Sales", fontSize = 11.sp, color = Color.Gray)
                                        Text(text = "ETB ${String.format(Locale.US, "%,.1f", totalSalesSum)}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }

                            if (filteredSales.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("No sales records found.", color = Color.Gray)
                                }
                            } else {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(filteredSales) { s ->
                                        Card(
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = s.saleNumber,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = MaterialTheme.colorScheme.secondary
                                                    )
                                                    Text(text = s.date, fontSize = 12.sp, color = Color.Gray)
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(text = "${s.materialName} (${s.category})", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                                Text(text = "Client: ${s.customerName} | Tel: ${s.phoneNumber}", fontSize = 13.sp, color = Color.Gray)
                                                Text(text = "Qty: ${s.quantity} ${s.unit} × ETB ${s.sellingPricePerUnit}/${s.unit}", fontSize = 13.sp, color = Color.Gray)
                                                Spacer(modifier = Modifier.height(8.dp))
                                                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                                                Spacer(modifier = Modifier.height(8.dp))

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Column {
                                                        Text("Total Sale", fontSize = 10.sp, color = Color.Gray)
                                                        Text("ETB ${String.format(Locale.US, "%,.1f", s.totalSale)}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                    }
                                                    Column {
                                                        Text("Amount Paid", fontSize = 10.sp, color = Color.Gray)
                                                        Text("ETB ${String.format(Locale.US, "%,.1f", s.amountPaid)}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1B5E20))
                                                    }
                                                    Column {
                                                        Text("Remaining", fontSize = 10.sp, color = Color.Gray)
                                                        Text("ETB ${String.format(Locale.US, "%,.1f", s.remainingBalance)}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (s.remainingBalance > 0) Color.Red else Color.DarkGray)
                                                    }
                                                    IconButton(
                                                        onClick = { saleToDelete = s },
                                                        modifier = Modifier.size(24.dp)
                                                    ) {
                                                        Icon(Icons.Default.Delete, "Delete", tint = Color.Red, modifier = Modifier.size(20.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        // --- DYNAMIC STOCK INVENTORY BALANCE ---
                        if (inventory.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No inventory catalog items active.", color = Color.Gray)
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(inventory) { item ->
                                    val isLowStock = item.remainingQuantity < 5.0
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isLowStock) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                                        ),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = item.name,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp,
                                                    color = if (isLowStock) Color.Red else Color.Unspecified
                                                )
                                                if (isLowStock) {
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(Color.Red.copy(alpha = 0.15f))
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text("LOW STOCK", color = Color.Red, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                } else {
                                                    Text(text = item.category, fontSize = 12.sp, color = Color.Gray)
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(text = "Opening stock: ${item.openingQuantity} ${item.unit}", fontSize = 12.sp, color = Color.Gray)
                                                Text(text = "Purchased: +${item.purchasedQuantity}", fontSize = 12.sp, color = Color(0xFF1B5E20))
                                                Text(text = "Sold: -${item.soldQuantity}", fontSize = 12.sp, color = Color.Red)
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                                            Spacer(modifier = Modifier.height(6.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text("In Stock Quantity", fontSize = 10.sp, color = Color.Gray)
                                                    Text("${String.format(Locale.US, "%.1f", item.remainingQuantity)} ${item.unit}", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                                                }
                                                Column {
                                                    Text("Average Cost", fontSize = 10.sp, color = Color.Gray)
                                                    Text("ETB ${String.format(Locale.US, "%,.1f", item.averageCost)}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                }
                                                Column {
                                                    Text("Asset Value", fontSize = 10.sp, color = Color.Gray)
                                                    Text("ETB ${String.format(Locale.US, "%,.1f", item.currentValue)}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- Create Material Purchase Dialog ---
    if (showAddPurchaseDialog) {
        var materialName by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("Wood") }
        var supplierName by remember { mutableStateOf("") }
        var supplierPhone by remember { mutableStateOf("") }
        var qtyStr by remember { mutableStateOf("10") }
        var unit by remember { mutableStateOf("pcs") }
        var unitPriceStr by remember { mutableStateOf("300") }
        var paymentMadeStr by remember { mutableStateOf("1500") }
        var payMethod by remember { mutableStateOf("Cash") }
        var notes by remember { mutableStateOf("") }

        var categoryExpanded by remember { mutableStateOf(false) }
        var methodExpanded by remember { mutableStateOf(false) }

        val qtyVal = qtyStr.toDoubleOrNull() ?: 0.0
        val priceVal = unitPriceStr.toDoubleOrNull() ?: 0.0
        val paymentVal = paymentMadeStr.toDoubleOrNull() ?: 0.0

        val validatedQty = if (qtyVal < 0) 0.0 else qtyVal
        val validatedPrice = if (priceVal < 0) 0.0 else priceVal
        val validatedPayment = if (paymentVal < 0) 0.0 else paymentVal

        val calculatedTotal = validatedQty * validatedPrice
        val calculatedBalance = calculatedTotal - validatedPayment

        val isFormValid = materialName.isNotBlank() && supplierName.isNotBlank() && validatedQty > 0 && validatedPrice > 0

        AlertDialog(
            onDismissRequest = { showAddPurchaseDialog = false },
            title = { Text(viewModel.t("new_purchase")) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = materialName,
                        onValueChange = { materialName = it },
                        label = { Text("Material Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("pur_material_name")
                    )

                    // Categories dropdown Selector
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = { IconButton(onClick = { categoryExpanded = !categoryExpanded }) { Icon(Icons.Default.ArrowDropDown, null) } },
                            modifier = Modifier.fillMaxWidth().clickable { categoryExpanded = !categoryExpanded }
                        )
                        DropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                            listOf("Wood", "MDF", "Plywood", "Metal", "Aluminium", "Glass", "Paint", "Accessories", "Tools", "Other").forEach { item ->
                                DropdownMenuItem(text = { Text(item) }, onClick = { category = item; categoryExpanded = false })
                            }
                        }
                    }

                    OutlinedTextField(
                        value = supplierName,
                        onValueChange = { supplierName = it },
                        label = { Text(viewModel.t("supplier_name")) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = supplierPhone,
                        onValueChange = { supplierPhone = it },
                        label = { Text(viewModel.t("supplier_phone")) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = qtyStr,
                            onValueChange = { qtyStr = it },
                            label = { Text(viewModel.t("quantity")) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1.2f)
                        )
                        OutlinedTextField(
                            value = unit,
                            onValueChange = { unit = it },
                            label = { Text("Unit") },
                            singleLine = true,
                            modifier = Modifier.weight(0.8f)
                        )
                    }

                    OutlinedTextField(
                        value = unitPriceStr,
                        onValueChange = { unitPriceStr = it },
                        label = { Text(viewModel.t("unit_price")) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Calculation breakdown
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("Total Purchase Cost: ETB ${String.format(Locale.US, "%,.2f", calculatedTotal)}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Supplier Balance: ETB ${String.format(Locale.US, "%,.2f", calculatedBalance)}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (calculatedBalance > 0) Color.Red else Color.DarkGray)
                        }
                    }

                    OutlinedTextField(
                        value = paymentMadeStr,
                        onValueChange = { paymentMadeStr = it },
                        label = { Text("Payment Made") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Payment Methods dropdown
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = payMethod,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Payment Method") },
                            trailingIcon = { IconButton(onClick = { methodExpanded = !methodExpanded }) { Icon(Icons.Default.ArrowDropDown, null) } },
                            modifier = Modifier.fillMaxWidth().clickable { methodExpanded = !methodExpanded }
                        )
                        DropdownMenu(expanded = methodExpanded, onDismissRequest = { methodExpanded = false }) {
                            listOf("Cash", "Bank", "Mobile Money", "Other").forEach { item ->
                                DropdownMenuItem(text = { Text(item) }, onClick = { payMethod = item; methodExpanded = false })
                            }
                        }
                    }

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text(viewModel.t("notes")) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val autoPurchaseNo = "PUR-${System.currentTimeMillis().toString().takeLast(5)}"
                        val p = MaterialPurchase(
                            purchaseNumber = autoPurchaseNo,
                            date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()),
                            supplierName = supplierName.trim(),
                            supplierPhone = supplierPhone.trim(),
                            materialName = materialName.trim(),
                            category = category,
                            quantity = validatedQty,
                            unit = unit.trim(),
                            purchasePricePerUnit = validatedPrice,
                            totalCost = calculatedTotal,
                            paymentMade = validatedPayment,
                            remainingSupplierBalance = calculatedBalance,
                            paymentMethod = payMethod,
                            notes = notes
                        )
                        viewModel.saveMaterialPurchase(p)
                        showAddPurchaseDialog = false
                    },
                    enabled = isFormValid,
                    modifier = Modifier.testTag("purchase_save_confirm")
                ) {
                    Text(viewModel.t("save"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddPurchaseDialog = false }) {
                    Text(viewModel.t("cancel"))
                }
            }
        )
    }

    // --- Create Material Sale Dialog ---
    if (showAddSaleDialog) {
        var clientName by remember { mutableStateOf("") }
        var clientPhone by remember { mutableStateOf("") }
        var materialName by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("Wood") }
        var qtyStr by remember { mutableStateOf("1") }
        var unit by remember { mutableStateOf("pcs") }
        var sellingPriceStr by remember { mutableStateOf("500") }
        var amountPaidStr by remember { mutableStateOf("500") }
        var payMethod by remember { mutableStateOf("Cash") }
        var notes by remember { mutableStateOf("") }

        var categoryExpanded by remember { mutableStateOf(false) }
        var methodExpanded by remember { mutableStateOf(false) }

        val qtyVal = qtyStr.toDoubleOrNull() ?: 0.0
        val priceVal = sellingPriceStr.toDoubleOrNull() ?: 0.0
        val paidVal = amountPaidStr.toDoubleOrNull() ?: 0.0

        val validatedQty = if (qtyVal < 0) 0.0 else qtyVal
        val validatedPrice = if (priceVal < 0) 0.0 else priceVal
        val validatedPaid = if (paidVal < 0) 0.0 else paidVal

        val calculatedTotal = validatedQty * validatedPrice
        val calculatedBalance = calculatedTotal - validatedPaid

        val isFormValid = materialName.isNotBlank() && clientName.isNotBlank() && validatedQty > 0 && validatedPrice > 0

        AlertDialog(
            onDismissRequest = { showAddSaleDialog = false },
            title = { Text(viewModel.t("new_sale")) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = clientName,
                        onValueChange = { clientName = it },
                        label = { Text("Buyer / Customer Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("sale_client_name")
                    )

                    OutlinedTextField(
                        value = clientPhone,
                        onValueChange = { clientPhone = it },
                        label = { Text("Buyer Phone Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = materialName,
                        onValueChange = { materialName = it },
                        label = { Text("Material Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Categories Selector
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = { IconButton(onClick = { categoryExpanded = !categoryExpanded }) { Icon(Icons.Default.ArrowDropDown, null) } },
                            modifier = Modifier.fillMaxWidth().clickable { categoryExpanded = !categoryExpanded }
                        )
                        DropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                            listOf("Wood", "MDF", "Plywood", "Metal", "Aluminium", "Glass", "Paint", "Accessories", "Tools", "Other").forEach { item ->
                                DropdownMenuItem(text = { Text(item) }, onClick = { category = item; categoryExpanded = false })
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = qtyStr,
                            onValueChange = { qtyStr = it },
                            label = { Text(viewModel.t("quantity")) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1.2f)
                        )
                        OutlinedTextField(
                            value = unit,
                            onValueChange = { unit = it },
                            label = { Text("Unit") },
                            singleLine = true,
                            modifier = Modifier.weight(0.8f)
                        )
                    }

                    OutlinedTextField(
                        value = sellingPriceStr,
                        onValueChange = { sellingPriceStr = it },
                        label = { Text("Selling Price per Unit") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Calculations
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("Total Sale Value: ETB ${String.format(Locale.US, "%,.2f", calculatedTotal)}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Customer Balance: ETB ${String.format(Locale.US, "%,.2f", calculatedBalance)}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (calculatedBalance > 0) Color.Red else Color.DarkGray)
                        }
                    }

                    OutlinedTextField(
                        value = amountPaidStr,
                        onValueChange = { amountPaidStr = it },
                        label = { Text("Amount Paid") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Payment Method selector
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = payMethod,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Payment Method") },
                            trailingIcon = { IconButton(onClick = { methodExpanded = !methodExpanded }) { Icon(Icons.Default.ArrowDropDown, null) } },
                            modifier = Modifier.fillMaxWidth().clickable { methodExpanded = !methodExpanded }
                        )
                        DropdownMenu(expanded = methodExpanded, onDismissRequest = { methodExpanded = false }) {
                            listOf("Cash", "Bank", "Mobile Money", "Other").forEach { item ->
                                DropdownMenuItem(text = { Text(item) }, onClick = { payMethod = item; methodExpanded = false })
                            }
                        }
                    }

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text(viewModel.t("notes")) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val autoSaleNo = "SAL-${System.currentTimeMillis().toString().takeLast(5)}"
                        val s = Sale(
                            saleNumber = autoSaleNo,
                            date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()),
                            customerName = clientName.trim(),
                            phoneNumber = clientPhone.trim(),
                            materialName = materialName.trim(),
                            category = category,
                            quantity = validatedQty,
                            unit = unit.trim(),
                            sellingPricePerUnit = validatedPrice,
                            totalSale = calculatedTotal,
                            amountPaid = validatedPaid,
                            remainingBalance = calculatedBalance,
                            paymentMethod = payMethod,
                            notes = notes,
                            status = if (calculatedBalance <= 0) "Paid" else if (validatedPaid > 0) "Partially Paid" else "Unpaid"
                        )
                        viewModel.saveSale(s)
                        showAddSaleDialog = false
                    },
                    enabled = isFormValid,
                    modifier = Modifier.testTag("sale_save_confirm")
                ) {
                    Text(viewModel.t("save"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSaleDialog = false }) {
                    Text(viewModel.t("cancel"))
                }
            }
        )
    }

    // --- Deletion Confirms ---
    if (purchaseToDelete != null) {
        AlertDialog(
            onDismissRequest = { purchaseToDelete = null },
            title = { Text(viewModel.t("sure_delete")) },
            text = { Text("Material Purchase: ${purchaseToDelete!!.purchaseNumber} (${purchaseToDelete!!.materialName}) will be permanently deleted.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteMaterialPurchase(purchaseToDelete!!.id)
                        purchaseToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(viewModel.t("delete"))
                }
            },
            dismissButton = {
                TextButton(onClick = { purchaseToDelete = null }) {
                    Text(viewModel.t("cancel"))
                }
            }
        )
    }

    if (saleToDelete != null) {
        AlertDialog(
            onDismissRequest = { saleToDelete = null },
            title = { Text(viewModel.t("sure_delete")) },
            text = { Text("Material Sale: ${saleToDelete!!.saleNumber} (${saleToDelete!!.materialName}) will be permanently deleted.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSale(saleToDelete!!.id)
                        saleToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(viewModel.t("delete"))
                }
            },
            dismissButton = {
                TextButton(onClick = { saleToDelete = null }) {
                    Text(viewModel.t("cancel"))
                }
            }
        )
    }
}
