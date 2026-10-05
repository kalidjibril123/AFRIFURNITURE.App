package com.example.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FinanceTransaction
import com.example.ui.viewmodel.BusinessViewModel
import com.example.ui.viewmodel.ReportSummaryData
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FinanceScreen(
    viewModel: BusinessViewModel,
    initialTab: Int = 0,
    modifier: Modifier = Modifier
) {
    val txs by viewModel.transactions.collectAsState(initial = emptyList())
    val summary by viewModel.dashboardSummary.collectAsState()
    val language by viewModel.currentLanguage.collectAsState()

    var selectedTab by remember { mutableStateOf(initialTab) } // 0: Ledger, 1: Reports, 2: Analytics Charts

    var showAddTxDialog by remember { mutableStateOf(false) }
    var txToDelete by remember { mutableStateOf<FinanceTransaction?>(null) }

    // Reports filters
    var reportRangeType by remember { mutableStateOf("Monthly") } // Daily, Weekly, Monthly, Yearly
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    // Compute report date ranges dynamically
    val reportDates = remember(reportRangeType) {
        val cal = Calendar.getInstance()
        val end = cal.time
        when (reportRangeType) {
            "Daily" -> {
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                Pair(cal.time, end)
            }
            "Weekly" -> {
                cal.add(Calendar.DAY_OF_YEAR, -7)
                Pair(cal.time, end)
            }
            "Monthly" -> {
                cal.add(Calendar.MONTH, -1)
                Pair(cal.time, end)
            }
            "Yearly" -> {
                cal.add(Calendar.YEAR, -1)
                Pair(cal.time, end)
            }
            else -> Pair(Date(0), end)
        }
    }

    // Reactively collect report data based on selected range
    val reportDataFlow = remember(reportDates) {
        viewModel.generateReportData(reportDates.first, reportDates.second)
    }
    val reportSummary by reportDataFlow.collectAsState(initial = ReportSummaryData())

    Scaffold(
        topBar = {
            SimpleTopAppBar(
                title = viewModel.t("finance_btn"),
                onBackClick = { viewModel.navigateBack() },
                actionIcon = if (selectedTab == 0) Icons.Default.AddCard else null,
                onActionClick = { if (selectedTab == 0) showAddTxDialog = true }
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
            // Tab Header
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text(if (language.name == "OROMO") "Kaffaltiiwwan" else "Cash Ledger", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(viewModel.t("reports_btn"), fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text(if (language.name == "OROMO") "Tarsiimoo" else "Charts", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        // --- FINANCE CASH TRANSACTION LEDGER ---
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = if (language.name == "OROMO") "Malleen Maallaqa Kuusaa" else "Direct Collected Revenues & Expenses:",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Collected Cash: ETB ${String.format(Locale.US, "%,.1f", summary.businessProfit)}", color = Color(0xFF1B5E20), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text("Paid Expenses: ETB ${String.format(Locale.US, "%,.1f", summary.businessExpenses)}", color = Color.Red, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        if (txs.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No custom transactions logged yet.", color = Color.Gray)
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(txs) { tx ->
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(8.dp)
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(
                                                                when (tx.type) {
                                                                    "Income" -> Color(0xFF1B5E20)
                                                                    "Expense" -> Color.Red
                                                                    else -> Color(0xFFD9B310)
                                                                }
                                                            )
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(text = tx.category, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(text = tx.description, fontSize = 12.sp, color = Color.Gray)
                                                Text(text = "Date: ${tx.date} | Ref: ${tx.reference}", fontSize = 11.sp, color = Color.Gray)
                                            }
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(
                                                    text = "${if (tx.type == "Income") "+" else "-"} ETB ${String.format(Locale.US, "%,.0f", tx.amount)}",
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 14.sp,
                                                    color = if (tx.type == "Income") Color(0xFF1B5E20) else Color.Red
                                                )
                                                IconButton(
                                                    onClick = { txToDelete = tx },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(Icons.Default.Delete, "Delete", tint = Color.Red, modifier = Modifier.size(18.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        // --- REPORTS VIEW PANEL ---
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Date Range Selection Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Daily", "Weekly", "Monthly", "Yearly").forEach { item ->
                                    ElevatedFilterChip(
                                        selected = reportRangeType == item,
                                        onClick = { reportRangeType = item },
                                        label = { Text(item, fontSize = 12.sp) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            Text(
                                text = "Stats from ${sdf.format(reportDates.first)} to ${sdf.format(reportDates.second)}:",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            // Reports metrics grid
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    ReportRowItem("Total Orders Generated", "${reportSummary.totalOrders}")
                                    ReportRowItem("Furniture Orders Count", "${reportSummary.furnitureCount}")
                                    ReportRowItem("Metal Orders Count", "${reportSummary.metalCount}")
                                    ReportRowItem("Aluminium Orders Count", "${reportSummary.aluminiumCount}")
                                    ReportRowItem("Total Material Sales Value", "ETB ${String.format(Locale.US, "%,.1f", reportSummary.totalSales)}")
                                    ReportRowItem("Material Purchases Value", "ETB ${String.format(Locale.US, "%,.1f", reportSummary.totalPurchases)}")
                                    HorizontalDivider()
                                    ReportRowItem("Revenues Collected (Cash In)", "ETB ${String.format(Locale.US, "%,.1f", reportSummary.totalIncome)}", color = Color(0xFF1B5E20))
                                    ReportRowItem("Expenses Paid (Cash Out)", "ETB ${String.format(Locale.US, "%,.1f", reportSummary.totalExpenses)}", color = Color.Red)
                                    ReportRowItem("Government Taxes Paid", "ETB ${String.format(Locale.US, "%,.1f", reportSummary.taxPayments)}", color = Color(0xFFD9B310))
                                    HorizontalDivider()
                                    ReportRowItem("Outstanding Customer Balances", "ETB ${String.format(Locale.US, "%,.1f", reportSummary.outstandingCustomerBalance)}", color = MaterialTheme.colorScheme.primary, isBold = true)
                                    ReportRowItem("Outstanding Supplier Balances", "ETB ${String.format(Locale.US, "%,.1f", reportSummary.supplierBalance)}", color = Color.Red, isBold = true)
                                    HorizontalDivider()
                                    ReportRowItem("Calculated Net Profit", "ETB ${String.format(Locale.US, "%,.1f", reportSummary.netProfit)}", color = if (reportSummary.netProfit >= 0) Color(0xFF1B5E20) else Color.Red, isBold = true)
                                }
                            }

                            // Share / Print Actions
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { /* Share PDF Mock */ },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Share, null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(viewModel.t("share"), fontSize = 12.sp)
                                }
                                Button(
                                    onClick = { /* Print Report Mock */ },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Print, null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(viewModel.t("print"), fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    2 -> {
                        // --- ANALYTICS CHARTS (CANVAS DRAWN) ---
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Business Performance Charts:",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            // 1. Orders Categories Comparison Chart
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(text = "Orders Allocation (Furniture vs Metal vs Aluminium)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    OrdersAllocationChart(
                                        furCount = reportSummary.furnitureCount,
                                        metCount = reportSummary.metalCount,
                                        aluCount = reportSummary.aluminiumCount
                                    )
                                }
                            }

                            // 2. Financial In vs Out Cash flow chart
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(text = "Cash Flow (Income vs Expenses vs Taxes)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    CashFlowBarChart(
                                        income = reportSummary.totalIncome,
                                        expenses = reportSummary.totalExpenses,
                                        taxes = reportSummary.taxPayments
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- Add Transaction Dialog ---
    if (showAddTxDialog) {
        var txType by remember { mutableStateOf("Income") } // Income, Expense, Tax
        var category by remember { mutableStateOf("Furniture Sales") }
        var description by remember { mutableStateOf("") }
        var amountStr by remember { mutableStateOf("") }
        var payMethod by remember { mutableStateOf("Cash") }
        var reference by remember { mutableStateOf("") }

        var typeExpanded by remember { mutableStateOf(false) }
        var catExpanded by remember { mutableStateOf(false) }
        var methodExpanded by remember { mutableStateOf(false) }

        val amountVal = amountStr.toDoubleOrNull() ?: 0.0
        val validatedAmount = if (amountVal < 0) 0.0 else amountVal

        val isFormValid = description.isNotBlank() && validatedAmount > 0

        AlertDialog(
            onDismissRequest = { showAddTxDialog = false },
            title = { Text(viewModel.t("new_payment")) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Type Selection (Income, Expense, Tax)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = txType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Transaction Type") },
                            trailingIcon = { IconButton(onClick = { typeExpanded = !typeExpanded }) { Icon(Icons.Default.ArrowDropDown, null) } },
                            modifier = Modifier.fillMaxWidth().clickable { typeExpanded = !typeExpanded }
                        )
                        DropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                            listOf("Income", "Expense", "Tax").forEach { item ->
                                DropdownMenuItem(text = { Text(item) }, onClick = { txType = item; typeExpanded = false })
                            }
                        }
                    }

                    // Category picker
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = { IconButton(onClick = { catExpanded = !catExpanded }) { Icon(Icons.Default.ArrowDropDown, null) } },
                            modifier = Modifier.fillMaxWidth().clickable { catExpanded = !catExpanded }
                        )
                        DropdownMenu(expanded = catExpanded, onDismissRequest = { catExpanded = false }) {
                            val list = if (txType == "Income") {
                                listOf("Furniture Sales", "Metal Sales", "Aluminium Sales", "Material Sales", "Other Income")
                            } else if (txType == "Expense") {
                                listOf("Material Purchase", "Worker Payment", "Rent", "Electricity", "Transport", "Maintenance", "Tools", "Other Expenses")
                            } else {
                                listOf("Tax Payment", "License Payment", "Other Government Payment")
                            }
                            list.forEach { item ->
                                DropdownMenuItem(text = { Text(item) }, onClick = { category = item; catExpanded = false })
                            }
                        }
                    }

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("tx_description")
                    )

                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it },
                        label = { Text("Amount (ETB)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("tx_amount")
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
                        value = reference,
                        onValueChange = { reference = it },
                        label = { Text("Reference / Receipt No") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val tNo = "TX-${System.currentTimeMillis().toString().takeLast(5)}"
                        val tx = FinanceTransaction(
                            transactionNumber = tNo,
                            date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()),
                            type = txType,
                            category = category,
                            description = description.trim(),
                            amount = validatedAmount,
                            paymentMethod = payMethod,
                            reference = reference.trim()
                        )
                        viewModel.saveTransaction(tx)
                        showAddTxDialog = false
                    },
                    enabled = isFormValid,
                    modifier = Modifier.testTag("tx_save_confirm")
                ) {
                    Text(viewModel.t("save"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTxDialog = false }) {
                    Text(viewModel.t("cancel"))
                }
            }
        )
    }

    // --- Delete Tx Dialogue ---
    if (txToDelete != null) {
        AlertDialog(
            onDismissRequest = { txToDelete = null },
            title = { Text(viewModel.t("sure_delete")) },
            text = { Text("Transaction: ${txToDelete!!.transactionNumber} (${txToDelete!!.category}) of ETB ${txToDelete!!.amount} will be deleted.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTransaction(txToDelete!!.id)
                        txToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(viewModel.t("delete"))
                }
            },
            dismissButton = {
                TextButton(onClick = { txToDelete = null }) {
                    Text(viewModel.t("cancel"))
                }
            }
        )
    }
}

@Composable
fun ReportRowItem(
    label: String,
    value: String,
    color: Color = Color.Unspecified,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray, modifier = Modifier.weight(1.5f))
        Text(
            text = value,
            style = if (isBold) MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodyMedium,
            color = color,
            modifier = Modifier.weight(1f),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}

// Custom Canvas chart drawing Orders categories side by side
@Composable
fun OrdersAllocationChart(
    furCount: Int,
    metCount: Int,
    aluCount: Int,
    modifier: Modifier = Modifier
) {
    val total = (furCount + metCount + aluCount).toFloat()
    if (total <= 0) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No data to display in chart.", color = Color.Gray, fontSize = 12.sp)
        }
        return
    }

    val furPct = furCount.toFloat() / total
    val metPct = metCount.toFloat() / total
    val aluPct = aluCount.toFloat() / total

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        // Furniture Column
        ChartBarColumn(
            label = "Furniture",
            pct = furPct,
            count = furCount,
            color = Color(0xFF0B3C5D),
            modifier = Modifier.weight(1f)
        )
        // Metal Column
        ChartBarColumn(
            label = "Metal Work",
            pct = metPct,
            count = metCount,
            color = Color(0xFF328CC1),
            modifier = Modifier.weight(1f)
        )
        // Aluminium Column
        ChartBarColumn(
            label = "Aluminium",
            pct = aluPct,
            count = aluCount,
            color = Color(0xFF1D976C),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ChartBarColumn(
    label: String,
    pct: Float,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Text(text = "$count", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        // Canvas drawn Bar
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
        ) {
            val barHeight = size.height * pct
            drawRoundRect(
                color = color,
                topLeft = Offset(0f, size.height - barHeight),
                size = Size(size.width, barHeight),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, fontSize = 10.sp, color = Color.Gray, maxLines = 1)
    }
}

// Canvas drawn financial bar charts
@Composable
fun CashFlowBarChart(
    income: Double,
    expenses: Double,
    taxes: Double,
    modifier: Modifier = Modifier
) {
    val maxVal = maxOf(income, expenses, taxes)
    if (maxVal <= 0.0) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No cash flow logs found.", color = Color.Gray, fontSize = 12.sp)
        }
        return
    }

    val incPct = (income / maxVal).toFloat()
    val expPct = (expenses / maxVal).toFloat()
    val taxPct = (taxes / maxVal).toFloat()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        ChartBarColumn(
            label = "Income",
            pct = incPct,
            count = income.toInt(),
            color = Color(0xFF1B5E20),
            modifier = Modifier.weight(1f)
        )
        ChartBarColumn(
            label = "Expenses",
            pct = expPct,
            count = expenses.toInt(),
            color = Color.Red,
            modifier = Modifier.weight(1f)
        )
        ChartBarColumn(
            label = "Taxes Paid",
            pct = taxPct,
            count = taxes.toInt(),
            color = Color(0xFFD9B310),
            modifier = Modifier.weight(1f)
        )
    }
}
