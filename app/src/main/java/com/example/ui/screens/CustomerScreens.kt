package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Customer
import com.example.ui.viewmodel.BusinessViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CustomersScreen(
    viewModel: BusinessViewModel,
    modifier: Modifier = Modifier
) {
    val customers by viewModel.customers.collectAsState(initial = emptyList())
    val furnitureOrders by viewModel.furnitureOrders.collectAsState(initial = emptyList())
    val metalOrders by viewModel.metalOrders.collectAsState(initial = emptyList())
    val aluminiumOrders by viewModel.aluminiumOrders.collectAsState(initial = emptyList())
    val language by viewModel.currentLanguage.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCustomerForDetails by remember { mutableStateOf<Customer?>(null) }
    var customerToDelete by remember { mutableStateOf<Customer?>(null) }

    val filteredCustomers = customers.filter { c ->
        c.name.contains(searchQuery, ignoreCase = true) || c.phone.contains(searchQuery)
    }

    Scaffold(
        topBar = {
            SimpleTopAppBar(
                title = viewModel.t("customers_btn"),
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
                .padding(16.dp)
        ) {
            // Search input
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
                    .testTag("customer_search_input")
            )

            if (filteredCustomers.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No customers registered yet.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredCustomers) { customer ->
                        // Dynamically compile linked metrics for this specific customer phone
                        val clientPhone = customer.phone.trim()
                        val cFurniture = furnitureOrders.filter { it.phoneNumber.trim() == clientPhone }
                        val cMetal = metalOrders.filter { it.phoneNumber.trim() == clientPhone }
                        val cAluminium = aluminiumOrders.filter { it.phoneNumber.trim() == clientPhone }

                        val totalOrdersCount = cFurniture.size + cMetal.size + cAluminium.size
                        val totalAmount = cFurniture.sumOf { it.totalPrice } + cMetal.sumOf { it.totalPrice } + cAluminium.sumOf { it.totalPrice }
                        val totalPaid = cFurniture.sumOf { it.initialPayment } + cMetal.sumOf { it.initialPayment } + cAluminium.sumOf { it.initialPayment }
                        val remainingBalance = totalAmount - totalPaid

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedCustomerForDetails = customer }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = customer.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    IconButton(
                                        onClick = { customerToDelete = customer },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, "Delete", tint = Color.Red, modifier = Modifier.size(18.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = "Phone: ${customer.phone} | Address: ${customer.address}", fontSize = 13.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(6.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Total Orders", fontSize = 10.sp, color = Color.Gray)
                                        Text("$totalOrdersCount", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                    Column {
                                        Text("Paid Value", fontSize = 10.sp, color = Color.Gray)
                                        Text("ETB ${String.format(Locale.US, "%,.1f", totalPaid)}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1B5E20))
                                    }
                                    Column {
                                        Text("Owes Balance", fontSize = 10.sp, color = Color.Gray)
                                        Text("ETB ${String.format(Locale.US, "%,.1f", remainingBalance)}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (remainingBalance > 0) Color.Red else Color.DarkGray)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- Customer Profile detail with scrollable Orders List dialog ---
    if (selectedCustomerForDetails != null) {
        val c = selectedCustomerForDetails!!
        val clientPhone = c.phone.trim()

        val cFurniture = furnitureOrders.filter { it.phoneNumber.trim() == clientPhone }
        val cMetal = metalOrders.filter { it.phoneNumber.trim() == clientPhone }
        val cAluminium = aluminiumOrders.filter { it.phoneNumber.trim() == clientPhone }

        AlertDialog(
            onDismissRequest = { selectedCustomerForDetails = null },
            title = { Text(text = "Customer Ledger Details", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DetailItem("Client Name", c.name)
                    DetailItem("Phone", c.phone)
                    DetailItem("Address", c.address)
                    DetailItem("Registered On", c.registrationDate)
                    if (c.notes.isNotBlank()) {
                        DetailItem("Internal Notes", c.notes)
                    }

                    HorizontalDivider()
                    Text(
                        text = "Linked Business Orders List:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Combine and show linked orders
                    if (cFurniture.isEmpty() && cMetal.isEmpty() && cAluminium.isEmpty()) {
                        Text("No orders linked to this client's phone.", fontSize = 12.sp, color = Color.Gray)
                    } else {
                        // Furniture
                        cFurniture.forEach { o ->
                            CustomerOrderRowItem(
                                title = "Furniture Order: ${o.orderNumber}",
                                date = o.registrationDate,
                                details = "${o.productType} (${o.materialType}) - ${o.width}m × ${o.height}m",
                                price = o.totalPrice,
                                paid = o.initialPayment,
                                remaining = o.remainingBalance,
                                status = o.status
                            )
                        }

                        // Metal
                        cMetal.forEach { o ->
                            CustomerOrderRowItem(
                                title = "Metal Order: ${o.orderNumber}",
                                date = o.registrationDate,
                                details = "${o.productType} (${o.materialType}) - ${o.width}m × ${o.height}m",
                                price = o.totalPrice,
                                paid = o.initialPayment,
                                remaining = o.remainingBalance,
                                status = o.status
                            )
                        }

                        // Aluminium
                        cAluminium.forEach { o ->
                            CustomerOrderRowItem(
                                title = "Aluminium Order: ${o.orderNumber}",
                                date = o.registrationDate,
                                details = "${o.productType} (${o.materialType}) - ${o.width}m × ${o.height}m",
                                price = o.totalPrice,
                                paid = o.initialPayment,
                                remaining = o.remainingBalance,
                                status = o.status
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedCustomerForDetails = null }) {
                    Text("Close")
                }
            }
        )
    }

    // --- Deletion Confirms ---
    if (customerToDelete != null) {
        AlertDialog(
            onDismissRequest = { customerToDelete = null },
            title = { Text(viewModel.t("sure_delete")) },
            text = { Text("Customer Profile: ${customerToDelete!!.name} will be permanently removed.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCustomer(customerToDelete!!.id)
                        customerToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(viewModel.t("delete"))
                }
            },
            dismissButton = {
                TextButton(onClick = { customerToDelete = null }) {
                    Text(viewModel.t("cancel"))
                }
            }
        )
    }
}

@Composable
fun CustomerOrderRowItem(
    title: String,
    date: String,
    details: String,
    price: Double,
    paid: Double,
    remaining: Double,
    status: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                Text(text = status, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (status == "Delivered") Color(0xFF1B5E20) else Color(0xFFD50000))
            }
            Text(text = "Date: $date | Specs: $details", fontSize = 11.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Total: ETB ${price.toInt()}", fontSize = 11.sp)
                Text(text = "Paid: ETB ${paid.toInt()}", fontSize = 11.sp)
                Text(text = "Owes: ETB ${remaining.toInt()}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (remaining > 0) Color.Red else Color.DarkGray)
            }
        }
    }
}
