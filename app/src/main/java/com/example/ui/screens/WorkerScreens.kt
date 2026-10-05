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
import com.example.data.WorkAssignment
import com.example.data.Worker
import com.example.ui.viewmodel.BusinessViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WorkerManagementScreen(
    viewModel: BusinessViewModel,
    initialTab: Int = 0,
    modifier: Modifier = Modifier
) {
    val workers by viewModel.workers.collectAsState(initial = emptyList())
    val assignments by viewModel.workAssignments.collectAsState(initial = emptyList())
    val language by viewModel.currentLanguage.collectAsState()

    var selectedTab by remember { mutableStateOf(initialTab) } // 0: Workers list, 1: Work Assignments

    // Form showing toggles
    var showAddWorkerDialog by remember { mutableStateOf(false) }
    var showAddAssignmentDialog by remember { mutableStateOf(false) }

    // Dialog state holders for Edit
    var workerToEdit by remember { mutableStateOf<Worker?>(null) }
    var assignmentToEdit by remember { mutableStateOf<WorkAssignment?>(null) }
    
    var workerToDelete by remember { mutableStateOf<Worker?>(null) }
    var assignmentToDelete by remember { mutableStateOf<WorkAssignment?>(null) }

    Scaffold(
        topBar = {
            SimpleTopAppBar(
                title = viewModel.t("workers_btn"),
                onBackClick = { viewModel.navigateBack() },
                actionIcon = if (selectedTab == 0) Icons.Default.PersonAdd else Icons.Default.AddTask,
                onActionClick = {
                    if (selectedTab == 0) showAddWorkerDialog = true else showAddAssignmentDialog = true
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
            // Tab Header
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text(if (language.name == "OROMO") "Hojjettoota" else "Workers & Performance", fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(if (language.name == "OROMO") "Hojii Kennu" else "Work Tracking", fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                if (selectedTab == 0) {
                    // --- WORKERS LIST WITH PERFORMANCE SUMMARY ---
                    if (workers.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No workers registered yet.", color = Color.Gray)
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(workers) { worker ->
                                // Calculate Performance Summary
                                val workerJobs = assignments.filter { it.workerId == worker.id || it.workerName.trim().equals(worker.name.trim(), ignoreCase = true) }
                                val totalJobs = workerJobs.size
                                val completedJobs = workerJobs.count { it.workStatus == "Completed" || it.workStatus == "Paid" }
                                val pendingJobs = totalJobs - completedJobs
                                val totalPayment = workerJobs.sumOf { it.amountAgreed }
                                val paidAmount = workerJobs.sumOf { it.amountPaid }
                                val remainingPayment = workerJobs.sumOf { it.remainingPayment }

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
                                                text = worker.name,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(
                                                        when (worker.status) {
                                                            "Active" -> Color(0xFF81C784).copy(alpha = 0.2f)
                                                            "Inactive" -> Color.Red.copy(alpha = 0.1f)
                                                            else -> Color.LightGray.copy(alpha = 0.2f)
                                                        }
                                                    )
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = worker.status,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = when (worker.status) {
                                                        "Active" -> Color(0xFF1B5E20)
                                                        "Inactive" -> Color.Red
                                                        else -> Color.DarkGray
                                                    }
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = "Skill: ${worker.jobSkill} | Tel: ${worker.phone}", fontSize = 13.sp, color = Color.Gray)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Performance Mini Matrix
                                        Text(
                                            text = if (language.name == "OROMO") "Gabaasa Hojii Hojjetaa" else "Worker Performance Summary:",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text("Total Jobs", fontSize = 10.sp, color = Color.Gray)
                                                Text("$totalJobs", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }
                                            Column {
                                                Text("Completed / Pending", fontSize = 10.sp, color = Color.Gray)
                                                Text("$completedJobs / $pendingJobs", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (pendingJobs > 0) Color(0xFFD50000) else Color(0xFF1B5E20))
                                            }
                                            Column {
                                                Text("Paid / Remaining", fontSize = 10.sp, color = Color.Gray)
                                                Text("ETB ${String.format(Locale.US, "%.0f", paidAmount)} / ${String.format(Locale.US, "%.0f", remainingPayment)}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (remainingPayment > 0) Color.Red else Color.DarkGray)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            IconButton(onClick = { workerToEdit = worker }) {
                                                Icon(Icons.Default.Edit, "Edit", tint = MaterialTheme.colorScheme.primary)
                                            }
                                            IconButton(onClick = { workerToDelete = worker }) {
                                                Icon(Icons.Default.Delete, "Delete", tint = Color.Red)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // --- WORK ASSIGNMENTS LIST ---
                    if (assignments.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No work assignments registered.", color = Color.Gray)
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(assignments) { item ->
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
                                                text = "Job: ${item.orderNumber}",
                                                fontWeight = FontWeight.ExtraBold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(
                                                        when (item.workStatus) {
                                                            "Completed" -> Color(0xFF81C784).copy(alpha = 0.2f)
                                                            "Paid" -> Color(0xFF4DB6AC).copy(alpha = 0.2f)
                                                            "In Progress" -> Color(0xFF64B5F6).copy(alpha = 0.2f)
                                                            else -> Color(0xFFFFD54F).copy(alpha = 0.2f)
                                                        }
                                                    )
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = item.workStatus,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = when (item.workStatus) {
                                                        "Completed" -> Color(0xFF1B5E20)
                                                        "Paid" -> Color(0xFF004D40)
                                                        "In Progress" -> Color(0xFF0D47A1)
                                                        else -> Color(0xFFD50000)
                                                    }
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = "Worker: ${item.workerName}", fontWeight = FontWeight.Bold)
                                        Text(text = "Customer: ${item.customerName} | Type: ${item.workType}", fontSize = 13.sp, color = Color.Gray)
                                        Text(text = "Description: ${item.description}", fontSize = 13.sp, color = Color.Gray)
                                        Text(text = "Dates: ${item.dateAssigned} to ${item.expectedCompletionDate}", fontSize = 12.sp, color = Color.Gray)
                                        if (item.actualCompletionDate.isNotBlank()) {
                                            Text(text = "Actual Completion: ${item.actualCompletionDate}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text("Agreed Amount", fontSize = 10.sp, color = Color.Gray)
                                                Text("ETB ${String.format(Locale.US, "%,.0f", item.amountAgreed)}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            }
                                            Column {
                                                Text("Paid Amount", fontSize = 10.sp, color = Color.Gray)
                                                Text("ETB ${String.format(Locale.US, "%,.0f", item.amountPaid)}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1B5E20))
                                            }
                                            Column {
                                                Text("Remaining Payment", fontSize = 10.sp, color = Color.Gray)
                                                Text("ETB ${String.format(Locale.US, "%,.0f", item.remainingPayment)}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (item.remainingPayment > 0) Color.Red else Color.DarkGray)
                                            }
                                        }

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 8.dp),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            IconButton(onClick = { assignmentToEdit = item }) {
                                                Icon(Icons.Default.Edit, "Edit", tint = MaterialTheme.colorScheme.primary)
                                            }
                                            IconButton(onClick = { assignmentToDelete = item }) {
                                                Icon(Icons.Default.Delete, "Delete", tint = Color.Red)
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

    // --- Add/Edit Worker Dialog ---
    if (showAddWorkerDialog || workerToEdit != null) {
        val editing = workerToEdit
        var name by remember { mutableStateOf(editing?.name ?: "") }
        var phone by remember { mutableStateOf(editing?.phone ?: "") }
        var skill by remember { mutableStateOf(editing?.jobSkill ?: "Woodworker") }
        var status by remember { mutableStateOf(editing?.status ?: "Active") }
        var skillExpanded by remember { mutableStateOf(false) }
        var statusExpanded by remember { mutableStateOf(false) }

        val isFormValid = name.isNotBlank() && phone.isNotBlank()

        AlertDialog(
            onDismissRequest = {
                showAddWorkerDialog = false
                workerToEdit = null
            },
            title = { Text(if (editing != null) "Edit Worker Profile" else viewModel.t("new_worker")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(viewModel.t("worker_name")) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("worker_name_input")
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text(viewModel.t("phone_no")) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("worker_phone_input")
                    )

                    // Job skill dropdown selector
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = skill,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(viewModel.t("job_skill")) },
                            trailingIcon = { IconButton(onClick = { skillExpanded = !skillExpanded }) { Icon(Icons.Default.ArrowDropDown, null) } },
                            modifier = Modifier.fillMaxWidth().clickable { skillExpanded = !skillExpanded }
                        )
                        DropdownMenu(expanded = skillExpanded, onDismissRequest = { skillExpanded = false }) {
                            listOf("Woodworker", "Metal Welder", "Aluminium Assembler", "Glass Cutter", "Painter", "Helper", "Other").forEach { item ->
                                DropdownMenuItem(text = { Text(item) }, onClick = { skill = item; skillExpanded = false })
                            }
                        }
                    }

                    // Status Dropdown
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
                            listOf("Active", "Inactive", "On Leave").forEach { item ->
                                DropdownMenuItem(text = { Text(item) }, onClick = { status = item; statusExpanded = false })
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val w = Worker(
                            id = editing?.id ?: 0,
                            name = name.trim(),
                            phone = phone.trim(),
                            jobSkill = skill,
                            registrationDate = editing?.registrationDate ?: SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()),
                            status = status
                        )
                        viewModel.saveWorker(w)
                        showAddWorkerDialog = false
                        workerToEdit = null
                    },
                    enabled = isFormValid,
                    modifier = Modifier.testTag("worker_save_dialog_btn")
                ) {
                    Text(viewModel.t("save"))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddWorkerDialog = false
                    workerToEdit = null
                }) {
                    Text(viewModel.t("cancel"))
                }
            }
        )
    }

    // --- Add/Edit Work Assignment Dialog ---
    if (showAddAssignmentDialog || assignmentToEdit != null) {
        val editing = assignmentToEdit
        var workerName by remember { mutableStateOf(editing?.workerName ?: "") }
        var customerName by remember { mutableStateOf(editing?.customerName ?: "") }
        var orderNumber by remember { mutableStateOf(editing?.orderNumber ?: "") }
        var workType by remember { mutableStateOf(editing?.workType ?: "Furniture") }
        var description by remember { mutableStateOf(editing?.description ?: "") }
        var dateAssigned by remember { mutableStateOf(editing?.dateAssigned ?: SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())) }
        var expectedCompletion by remember { mutableStateOf(editing?.expectedCompletionDate ?: SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(System.currentTimeMillis() + 86400000 * 5))) }
        var actualCompletion by remember { mutableStateOf(editing?.actualCompletionDate ?: "") }
        var amountAgreedStr by remember { mutableStateOf(editing?.amountAgreed?.toString() ?: "1000") }
        var amountPaidStr by remember { mutableStateOf(editing?.amountPaid?.toString() ?: "0") }
        var workStatus by remember { mutableStateOf(editing?.workStatus ?: "Assigned") }

        var workerNameExpanded by remember { mutableStateOf(false) }
        var typeExpanded by remember { mutableStateOf(false) }
        var statusExpanded by remember { mutableStateOf(false) }

        val agreedVal = amountAgreedStr.toDoubleOrNull() ?: 0.0
        val paidVal = amountPaidStr.toDoubleOrNull() ?: 0.0
        val validatedAgreed = if (agreedVal < 0) 0.0 else agreedVal
        val validatedPaid = if (paidVal < 0) 0.0 else paidVal
        val calculatedRemaining = validatedAgreed - validatedPaid

        val isFormValid = workerName.isNotBlank() && customerName.isNotBlank() && orderNumber.isNotBlank() && agreedVal > 0

        AlertDialog(
            onDismissRequest = {
                showAddAssignmentDialog = false
                assignmentToEdit = null
            },
            title = { Text(if (editing != null) "Edit Work Tracking" else "Create Work Assignment") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Selectable Workers list in dropdown
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = workerName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Worker Name") },
                            trailingIcon = { IconButton(onClick = { workerNameExpanded = !workerNameExpanded }) { Icon(Icons.Default.ArrowDropDown, null) } },
                            modifier = Modifier.fillMaxWidth().clickable { workerNameExpanded = !workerNameExpanded }
                        )
                        DropdownMenu(expanded = workerNameExpanded, onDismissRequest = { workerNameExpanded = false }) {
                            if (workers.isEmpty()) {
                                DropdownMenuItem(text = { Text("No active workers") }, onClick = {})
                            } else {
                                workers.forEach { w ->
                                    DropdownMenuItem(text = { Text(w.name) }, onClick = { workerName = w.name; workerNameExpanded = false })
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Customer Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = orderNumber,
                        onValueChange = { orderNumber = it },
                        label = { Text("Related Order No") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Work Type Selector
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = workType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Work Category") },
                            trailingIcon = { IconButton(onClick = { typeExpanded = !typeExpanded }) { Icon(Icons.Default.ArrowDropDown, null) } },
                            modifier = Modifier.fillMaxWidth().clickable { typeExpanded = !typeExpanded }
                        )
                        DropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                            listOf("Furniture", "Metal", "Aluminium", "Other").forEach { item ->
                                DropdownMenuItem(text = { Text(item) }, onClick = { workType = item; typeExpanded = false })
                            }
                        }
                    }

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Job Description") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = dateAssigned,
                        onValueChange = { dateAssigned = it },
                        label = { Text("Date Assigned") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = expectedCompletion,
                        onValueChange = { expectedCompletion = it },
                        label = { Text("Expected Completion Date") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (editing != null) {
                        OutlinedTextField(
                            value = actualCompletion,
                            onValueChange = { actualCompletion = it },
                            label = { Text("Actual Completion Date") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    OutlinedTextField(
                        value = amountAgreedStr,
                        onValueChange = { amountAgreedStr = it },
                        label = { Text("Agreed Worker Payment") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = amountPaidStr,
                        onValueChange = { amountPaidStr = it },
                        label = { Text("Payment Made So Far") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Calculated Worker Remaining
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Remaining Worker Bal: ETB ${String.format(Locale.US, "%,.2f", calculatedRemaining)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }

                    // Status Dropdown
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = workStatus,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Work Status") },
                            trailingIcon = { IconButton(onClick = { statusExpanded = !statusExpanded }) { Icon(Icons.Default.ArrowDropDown, null) } },
                            modifier = Modifier.fillMaxWidth().clickable { statusExpanded = !statusExpanded }
                        )
                        DropdownMenu(expanded = statusExpanded, onDismissRequest = { statusExpanded = false }) {
                            listOf("Assigned", "In Progress", "Completed", "Paid", "Pending Payment").forEach { item ->
                                DropdownMenuItem(text = { Text(item) }, onClick = { workStatus = item; statusExpanded = false })
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val activeWorker = workers.find { it.name.trim().equals(workerName.trim(), ignoreCase = true) }
                        val a = WorkAssignment(
                            id = editing?.id ?: 0,
                            workerId = activeWorker?.id ?: 0,
                            workerName = workerName.trim(),
                            customerName = customerName.trim(),
                            orderNumber = orderNumber.trim(),
                            workType = workType,
                            description = description,
                            dateAssigned = dateAssigned,
                            expectedCompletionDate = expectedCompletion,
                            actualCompletionDate = actualCompletion,
                            amountAgreed = validatedAgreed,
                            amountPaid = validatedPaid,
                            remainingPayment = calculatedRemaining,
                            workStatus = workStatus
                        )
                        viewModel.saveWorkAssignment(a)
                        showAddAssignmentDialog = false
                        assignmentToEdit = null
                    },
                    enabled = isFormValid,
                    modifier = Modifier.testTag("assignment_save_btn")
                ) {
                    Text(viewModel.t("save"))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddAssignmentDialog = false
                    assignmentToEdit = null
                }) {
                    Text(viewModel.t("cancel"))
                }
            }
        )
    }

    // --- Deletion confirmation dialogs ---
    if (workerToDelete != null) {
        AlertDialog(
            onDismissRequest = { workerToDelete = null },
            title = { Text(viewModel.t("sure_delete")) },
            text = { Text("Worker Profile: ${workerToDelete!!.name} and all related summary data will be deleted.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteWorker(workerToDelete!!.id)
                        workerToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(viewModel.t("delete"))
                }
            },
            dismissButton = {
                TextButton(onClick = { workerToDelete = null }) {
                    Text(viewModel.t("cancel"))
                }
            }
        )
    }

    if (assignmentToDelete != null) {
        AlertDialog(
            onDismissRequest = { assignmentToDelete = null },
            title = { Text(viewModel.t("sure_delete")) },
            text = { Text("Work tracking assignment for ${assignmentToDelete!!.workerName} on order ${assignmentToDelete!!.orderNumber} will be deleted.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteWorkAssignment(assignmentToDelete!!.id)
                        assignmentToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(viewModel.t("delete"))
                }
            },
            dismissButton = {
                TextButton(onClick = { assignmentToDelete = null }) {
                    Text(viewModel.t("cancel"))
                }
            }
        )
    }
}
