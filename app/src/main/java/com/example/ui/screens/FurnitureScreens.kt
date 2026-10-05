package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FurnitureOrder
import com.example.ui.viewmodel.BusinessViewModel
import com.example.ui.viewmodel.Screen
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FurnitureListScreen(
    viewModel: BusinessViewModel,
    modifier: Modifier = Modifier
) {
    val orders by viewModel.furnitureOrders.collectAsState(initial = emptyList())
    val language by viewModel.currentLanguage.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var orderToDelete by remember { mutableStateOf<FurnitureOrder?>(null) }
    var selectedOrderForDetails by remember { mutableStateOf<FurnitureOrder?>(null) }

    // Filtered orders list
    val filteredOrders = orders.filter { o ->
        val matchesSearch = o.customerName.contains(searchQuery, ignoreCase = true) ||
                o.phoneNumber.contains(searchQuery) ||
                o.orderNumber.contains(searchQuery, ignoreCase = true) ||
                o.productType.contains(searchQuery, ignoreCase = true)
        val matchesFilter = selectedFilter == "All" || o.status.equals(selectedFilter, ignoreCase = true)
        matchesSearch && matchesFilter
    }

    Scaffold(
        topBar = {
            SimpleTopAppBar(
                title = viewModel.t("furniture_btn"),
                onBackClick = { viewModel.navigateBack() },
                actionIcon = Icons.Default.Add,
                onActionClick = { viewModel.navigateTo(Screen.NewFurnitureOrder) }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            // Search Bar
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
                    .testTag("furniture_search_input")
            )

            // Status Filter Tabs
            ScrollableTabRow(
                selectedTabIndex = when (selectedFilter) {
                    "All" -> 0
                    "Pending" -> 1
                    "In Production" -> 2
                    "Ready" -> 3
                    "Delivered" -> 4
                    else -> 0
                },
                edgePadding = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                listOf("All", "Pending", "In Production", "Ready", "Delivered").forEach { filterName ->
                    Tab(
                        selected = selectedFilter == filterName,
                        onClick = { selectedFilter = filterName },
                        text = { Text(filterName, fontSize = 12.sp) }
                    )
                }
            }

            if (filteredOrders.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Inbox,
                            contentDescription = null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (language.name == "OROMO") "Ajajni hin argamne" else "No orders found.",
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredOrders) { order ->
                        FurnitureOrderRow(
                            order = order,
                            viewModel = viewModel,
                            onViewDetails = { selectedOrderForDetails = order },
                            onEdit = { viewModel.navigateTo(Screen.EditFurnitureOrder(order.id)) },
                            onDelete = { orderToDelete = order }
                        )
                    }
                }
            }
        }
    }

    // --- Order Details Popup Dialog ---
    if (selectedOrderForDetails != null) {
        val o = selectedOrderForDetails!!
        AlertDialog(
            onDismissRequest = { selectedOrderForDetails = null },
            title = { Text("${viewModel.t("order_no")}: ${o.orderNumber}", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DetailItem(viewModel.t("cust_name"), o.customerName)
                    DetailItem(viewModel.t("phone_no"), o.phoneNumber)
                    DetailItem(viewModel.t("prod_type"), o.productType)
                    DetailItem(viewModel.t("mat_type"), o.materialType)
                    DetailItem(viewModel.t("wood_type"), o.woodType)
                    DetailItem("${viewModel.t("width")} × ${viewModel.t("height")} × ${viewModel.t("quantity")}", "${o.width}m × ${o.height}m × ${o.quantity}")
                    DetailItem("Total M2", "${String.format(Locale.US, "%.3f", o.m2)} m²")
                    DetailItem(viewModel.t("price_per_m2"), "ETB ${String.format(Locale.US, "%,.2f", o.pricePerM2)}")
                    DetailItem(if (language.name == "OROMO") "Gatii Guutuu" else "Total Price", "ETB ${String.format(Locale.US, "%,.2f", o.totalPrice)}", isBoldValue = true)
                    DetailItem(viewModel.t("initial_pay"), "ETB ${String.format(Locale.US, "%,.2f", o.initialPayment)}")
                    DetailItem(viewModel.t("remaining_bal"), "ETB ${String.format(Locale.US, "%,.2f", o.remainingBalance)}", valueColor = Color.Red, isBoldValue = true)
                    DetailItem(viewModel.t("delivery_date"), o.deliveryDate)
                    DetailItem(viewModel.t("status"), o.status)
                    if (o.notes.isNotBlank()) {
                        DetailItem(viewModel.t("notes"), o.notes)
                    }
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.navigateTo(Screen.ReceiptView(o.id, "Furniture"))
                            selectedOrderForDetails = null
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Receipt, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(viewModel.t("receipt"), fontSize = 11.sp)
                    }
                    TextButton(onClick = { selectedOrderForDetails = null }) {
                        Text("Close")
                    }
                }
            }
        )
    }

    // --- Delete Confirmation Dialog ---
    if (orderToDelete != null) {
        AlertDialog(
            onDismissRequest = { orderToDelete = null },
            title = { Text(viewModel.t("sure_delete")) },
            text = { Text("Order ${orderToDelete!!.orderNumber} will be deleted.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteFurnitureOrder(orderToDelete!!.id, isHard = false)
                        orderToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(viewModel.t("delete"))
                }
            },
            dismissButton = {
                TextButton(onClick = { orderToDelete = null }) {
                    Text(viewModel.t("cancel"))
                }
            }
        )
    }
}

@Composable
fun FurnitureOrderRow(
    order: FurnitureOrder,
    viewModel: BusinessViewModel,
    onViewDetails: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onViewDetails)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.orderNumber,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when (order.status) {
                                "Pending" -> Color(0xFFFFD54F).copy(alpha = 0.2f)
                                "In Production" -> Color(0xFF64B5F6).copy(alpha = 0.2f)
                                "Ready" -> Color(0xFF81C784).copy(alpha = 0.2f)
                                "Delivered" -> Color(0xFF4DB6AC).copy(alpha = 0.2f)
                                else -> Color.LightGray.copy(alpha = 0.2f)
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = order.status,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (order.status) {
                            "Pending" -> Color(0xFFD50000)
                            "In Production" -> Color(0xFF0D47A1)
                            "Ready" -> Color(0xFF1B5E20)
                            "Delivered" -> Color(0xFF004D40)
                            else -> Color.DarkGray
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${order.customerName} | ${order.productType}",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Total Price", fontSize = 10.sp, color = Color.Gray)
                    Text("ETB ${String.format(Locale.US, "%,.1f", order.totalPrice)}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("Remaining Balance", fontSize = 10.sp, color = Color.Gray)
                    Text("ETB ${String.format(Locale.US, "%,.1f", order.remainingBalance)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (order.remainingBalance > 0) Color.Red else Color.DarkGray)
                }
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

// Order Form Screen (Create / Edit)
@Composable
fun NewFurnitureOrderScreen(
    viewModel: BusinessViewModel,
    orderIdToEdit: Int? = null,
    modifier: Modifier = Modifier
) {
    val orders by viewModel.furnitureOrders.collectAsState(initial = emptyList())
    val productConfigs by viewModel.productConfigs.collectAsState(initial = emptyList())
    val materialConfigs by viewModel.materialConfigs.collectAsState(initial = emptyList())
    val language by viewModel.currentLanguage.collectAsState()

    val editingOrder = remember(orderIdToEdit, orders) {
        orders.find { it.id == orderIdToEdit }
    }

    // Auto generate sequential order number
    val autoOrderNo = remember(orders, editingOrder) {
        editingOrder?.orderNumber ?: "FUR-${String.format(Locale.US, "%04d", orders.size + 1)}"
    }

    // Form inputs
    var customerName by remember { mutableStateOf(editingOrder?.customerName ?: "") }
    var phoneNumber by remember { mutableStateOf(editingOrder?.phoneNumber ?: "") }
    var registrationDate by remember { mutableStateOf(editingOrder?.registrationDate ?: SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())) }
    var productType by remember { mutableStateOf(editingOrder?.productType ?: "Door") }
    var materialType by remember { mutableStateOf(editingOrder?.materialType ?: "MDF") }
    var woodType by remember { mutableStateOf(editingOrder?.woodType ?: "MDF") }
    var widthStr by remember { mutableStateOf(editingOrder?.width?.toString() ?: "1.0") }
    var heightStr by remember { mutableStateOf(editingOrder?.height?.toString() ?: "2.0") }
    var quantityStr by remember { mutableStateOf(editingOrder?.quantity?.toString() ?: "1") }
    var pricePerM2Str by remember { mutableStateOf(editingOrder?.pricePerM2?.toString() ?: "1500") }
    var initialPayStr by remember { mutableStateOf(editingOrder?.initialPayment?.toString() ?: "500") }
    var deliveryDate by remember { mutableStateOf(editingOrder?.deliveryDate ?: SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(System.currentTimeMillis() + 86400000 * 7))) }
    var notes by remember { mutableStateOf(editingOrder?.notes ?: "") }
    var status by remember { mutableStateOf(editingOrder?.status ?: "Pending") }

    // Dropdown expanding state
    var productExpanded by remember { mutableStateOf(false) }
    var materialExpanded by remember { mutableStateOf(false) }
    var woodExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }

    // Validation/Calculations
    val widthVal = widthStr.toDoubleOrNull() ?: 0.0
    val heightVal = heightStr.toDoubleOrNull() ?: 0.0
    val quantityVal = quantityStr.toIntOrNull() ?: 0
    val priceVal = pricePerM2Str.toDoubleOrNull() ?: 0.0
    val initialPayVal = initialPayStr.toDoubleOrNull() ?: 0.0

    // Prevent negative parameters
    val validatedWidth = if (widthVal < 0) 0.0 else widthVal
    val validatedHeight = if (heightVal < 0) 0.0 else heightVal
    val validatedQuantity = if (quantityVal < 0) 0 else quantityVal
    val validatedPrice = if (priceVal < 0) 0.0 else priceVal
    val validatedInitialPay = if (initialPayVal < 0) 0.0 else initialPayVal

    val calculatedM2 = validatedWidth * validatedHeight * validatedQuantity
    val calculatedTotal = calculatedM2 * validatedPrice
    val calculatedRemaining = calculatedTotal - validatedInitialPay

    val isFormValid = customerName.isNotBlank() && phoneNumber.isNotBlank() && widthVal > 0 && heightVal > 0 && quantityVal > 0 && priceVal > 0

    Scaffold(
        topBar = {
            SimpleTopAppBar(
                title = if (editingOrder != null) viewModel.t("edit") else viewModel.t("new_furniture"),
                onBackClick = { viewModel.navigateBack() }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // General Details
            OutlinedTextField(
                value = autoOrderNo,
                onValueChange = {},
                readOnly = true,
                label = { Text(viewModel.t("order_no")) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = customerName,
                onValueChange = { customerName = it },
                label = { Text(viewModel.t("cust_name")) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("furn_customer_name")
            )

            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text(viewModel.t("phone_no")) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("furn_phone")
            )

            // Selectable Dynamic Product Type Configuration Dropdown
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = productType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(viewModel.t("prod_type")) },
                    trailingIcon = { IconButton(onClick = { productExpanded = !productExpanded }) { Icon(Icons.Default.ArrowDropDown, null) } },
                    modifier = Modifier.fillMaxWidth().clickable { productExpanded = !productExpanded }
                )
                DropdownMenu(expanded = productExpanded, onDismissRequest = { productExpanded = false }) {
                    val list = productConfigs.filter { it.category == "Furniture" }
                    if (list.isEmpty()) {
                        DropdownMenuItem(text = { Text("Door") }, onClick = { productType = "Door"; productExpanded = false })
                        DropdownMenuItem(text = { Text("Table") }, onClick = { productType = "Table"; productExpanded = false })
                    } else {
                        list.forEach { item ->
                            DropdownMenuItem(text = { Text(item.name) }, onClick = { productType = item.name; productExpanded = false })
                        }
                    }
                }
            }

            // Material Wood Type Selectable Dropdown
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = woodType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(viewModel.t("wood_type")) },
                    trailingIcon = { IconButton(onClick = { woodExpanded = !woodExpanded }) { Icon(Icons.Default.ArrowDropDown, null) } },
                    modifier = Modifier.fillMaxWidth().clickable { woodExpanded = !woodExpanded }
                )
                DropdownMenu(expanded = woodExpanded, onDismissRequest = { woodExpanded = false }) {
                    val list = materialConfigs.filter { it.category == "Wood" }
                    if (list.isEmpty()) {
                        DropdownMenuItem(text = { Text("MDF") }, onClick = { woodType = "MDF"; woodExpanded = false })
                        DropdownMenuItem(text = { Text("Natural Wood") }, onClick = { woodType = "Natural Wood"; woodExpanded = false })
                    } else {
                        list.forEach { item ->
                            DropdownMenuItem(text = { Text(item.name) }, onClick = { woodType = item.name; woodExpanded = false })
                        }
                    }
                }
            }

            // Measurements
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = widthStr,
                    onValueChange = { widthStr = it },
                    label = { Text(viewModel.t("width")) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f).testTag("furn_width")
                )
                OutlinedTextField(
                    value = heightStr,
                    onValueChange = { heightStr = it },
                    label = { Text(viewModel.t("height")) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f).testTag("furn_height")
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = quantityStr,
                    onValueChange = { quantityStr = it },
                    label = { Text(viewModel.t("quantity")) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f).testTag("furn_qty")
                )
                OutlinedTextField(
                    value = pricePerM2Str,
                    onValueChange = { pricePerM2Str = it },
                    label = { Text(viewModel.t("price_per_m2")) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f).testTag("furn_price")
                )
            }

            // Automatic calculation metrics
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = if (language.name == "OROMO") "Herrega Of-Awtomaatikii" else "Automated Calculations:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(text = "Total M2: ${String.format(Locale.US, "%.3f", calculatedM2)} m²", fontSize = 14.sp)
                    Text(text = "${viewModel.t("initial_pay")}: ETB ${String.format(Locale.US, "%,.2f", validatedInitialPay)}", fontSize = 14.sp)
                    Text(text = "Total Price: ETB ${String.format(Locale.US, "%,.2f", calculatedTotal)}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(text = "${viewModel.t("remaining_bal")}: ETB ${String.format(Locale.US, "%,.2f", calculatedRemaining)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (calculatedRemaining > 0) Color.Red else Color.DarkGray)
                }
            }

            OutlinedTextField(
                value = initialPayStr,
                onValueChange = { initialPayStr = it },
                label = { Text(viewModel.t("initial_pay")) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().testTag("furn_initial_pay")
            )

            // Delivery Date
            OutlinedTextField(
                value = deliveryDate,
                onValueChange = { deliveryDate = it },
                label = { Text(viewModel.t("delivery_date") + " (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth().testTag("furn_delivery_date")
            )

            // Status Dropdown Selection
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = status,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(viewModel.t("status")) },
                    trailingIcon = { IconButton(onClick = { statusExpanded = !statusExpanded }) { Icon(Icons.Default.ArrowDropDown, null) } },
                    modifier = Modifier.fillMaxWidth().clickable { statusExpanded = !statusExpanded }
                )
                DropdownMenu(expanded = statusExpanded, onDismissRequest = { statusExpanded = false }) {
                    listOf("Pending", "In Production", "Ready", "Delivered", "Cancelled").forEach { statusName ->
                        DropdownMenuItem(text = { Text(statusName) }, onClick = { status = statusName; statusExpanded = false })
                    }
                }
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text(viewModel.t("notes")) },
                modifier = Modifier.fillMaxWidth()
            )

            // Save Button
            Button(
                onClick = {
                    val order = FurnitureOrder(
                        id = editingOrder?.id ?: 0,
                        orderNumber = autoOrderNo,
                        customerName = customerName.trim(),
                        phoneNumber = phoneNumber.trim(),
                        registrationDate = registrationDate,
                        productType = productType,
                        materialType = materialType,
                        woodType = woodType,
                        width = validatedWidth,
                        height = validatedHeight,
                        quantity = validatedQuantity,
                        m2 = calculatedM2,
                        pricePerM2 = validatedPrice,
                        totalPrice = calculatedTotal,
                        initialPayment = validatedInitialPay,
                        remainingBalance = calculatedRemaining,
                        deliveryDate = deliveryDate,
                        notes = notes,
                        status = status
                    )
                    viewModel.saveFurnitureOrder(order)
                    viewModel.navigateBack()
                },
                enabled = isFormValid,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("furn_save_button")
            ) {
                Text(viewModel.t("save"), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

// Global Detail Item Helper
@Composable
fun DetailItem(
    label: String,
    value: String,
    isBoldValue: Boolean = false,
    valueColor: Color = Color.Unspecified
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = Color.Gray, fontSize = 13.sp, modifier = Modifier.weight(1f))
        Text(
            text = value,
            fontWeight = if (isBoldValue) FontWeight.Bold else FontWeight.Normal,
            fontSize = 14.sp,
            color = valueColor,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.5f)
        )
    }
}
