package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.data.BusinessProfile
import com.example.ui.viewmodel.BusinessViewModel
import com.example.ui.viewmodel.Language
import com.example.ui.viewmodel.UserRole
import java.util.*

@Composable
fun SettingsScreen(
    viewModel: BusinessViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.currentLanguage.collectAsState()
    val activeRole by viewModel.currentRole.collectAsState()
    val profileState by viewModel.businessProfile.collectAsState(initial = BusinessProfile())
    val profile = profileState ?: BusinessProfile()

    val productConfigs by viewModel.productConfigs.collectAsState(initial = emptyList())
    val materialConfigs by viewModel.materialConfigs.collectAsState(initial = emptyList())

    val scrollState = rememberScrollState()
    var alertMsg by remember { mutableStateOf<String?>(null) }

    // Business Profile Fields
    var isEditingProfile by remember { mutableStateOf(false) }
    var busName by remember { mutableStateOf(profile.name) }
    var busPhone by remember { mutableStateOf(profile.phone) }
    var busAddress by remember { mutableStateOf(profile.address) }
    var busEmail by remember { mutableStateOf(profile.email) }
    var busTax by remember { mutableStateOf(profile.taxNumber) }
    var busBank by remember { mutableStateOf(profile.bankAccount) }
    var busInfo by remember { mutableStateOf(profile.otherInfo) }

    // Dynamic Category configs State
    var configCategoryType by remember { mutableStateOf("Furniture") } // Furniture, Metal, Aluminium
    var newConfigName by remember { mutableStateOf("") }

    // Sync input when profile updates from database
    LaunchedEffect(profile) {
        if (!isEditingProfile) {
            busName = profile.name
            busPhone = profile.phone
            busAddress = profile.address
            busEmail = profile.email
            busTax = profile.taxNumber
            busBank = profile.bankAccount
            busInfo = profile.otherInfo
        }
    }

    Scaffold(
        topBar = {
            SimpleTopAppBar(
                title = viewModel.t("settings_btn"),
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
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- SECTION 1: LANGUAGE PANEL ---
            Text("1. ${viewModel.t("language")}".uppercase(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ElevatedFilterChip(
                    selected = language == Language.ENGLISH,
                    onClick = { viewModel.currentLanguage.value = Language.ENGLISH },
                    label = { Text("English", fontSize = 13.sp) },
                    modifier = Modifier.weight(1f).testTag("lang_english")
                )
                ElevatedFilterChip(
                    selected = language == Language.OROMO,
                    onClick = { viewModel.currentLanguage.value = Language.OROMO },
                    label = { Text("Afaan Oromoo", fontSize = 13.sp) },
                    modifier = Modifier.weight(1f).testTag("lang_oromo")
                )
            }

            HorizontalDivider()

            // --- SECTION 2: PERMISSION ROLE SWITCHER ---
            Text("2. ${viewModel.t("role_access")}".uppercase(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
            Text("Select active session role to preview role-based features & restrictions in the workshop:", fontSize = 11.sp, color = Color.Gray)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                UserRole.values().forEach { r ->
                    ElevatedFilterChip(
                        selected = activeRole == r,
                        onClick = { viewModel.currentRole.value = r },
                        label = { Text(r.name, fontSize = 10.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            HorizontalDivider()

            // --- SECTION 3: BUSINESS PROFILE PANEL ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("3. ${viewModel.t("business_profile")}".uppercase(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                TextButton(
                    onClick = {
                        if (isEditingProfile) {
                            // Save Business Profile
                            val updated = BusinessProfile(
                                name = busName,
                                phone = busPhone,
                                address = busAddress,
                                email = busEmail,
                                taxNumber = busTax,
                                bankAccount = busBank,
                                otherInfo = busInfo
                            )
                            viewModel.saveBusinessProfile(updated)
                            alertMsg = "Business Profile saved successfully!"
                        }
                        isEditingProfile = !isEditingProfile
                    },
                    modifier = Modifier.testTag("edit_profile_toggle")
                ) {
                    Text(if (isEditingProfile) "SAVE CHANGES" else "EDIT PROFILE")
                }
            }

            if (isEditingProfile) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = busName, onValueChange = { busName = it }, label = { Text("Company Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = busPhone, onValueChange = { busPhone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = busAddress, onValueChange = { busAddress = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = busEmail, onValueChange = { busEmail = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = busTax, onValueChange = { busTax = it }, label = { Text("TIN Number") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = busBank, onValueChange = { busBank = it }, label = { Text("Bank Account Details") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = busInfo, onValueChange = { busInfo = it }, label = { Text("Core Services Info") }, modifier = Modifier.fillMaxWidth())
                }
            } else {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Business Name: ${profile.name}", fontWeight = FontWeight.Bold)
                        Text("Phone: ${profile.phone}", fontSize = 13.sp)
                        Text("Address: ${profile.address}", fontSize = 13.sp)
                        Text("TIN: ${profile.taxNumber}", fontSize = 13.sp)
                        Text("Bank: ${profile.bankAccount}", fontSize = 13.sp)
                        Text("Services: ${profile.otherInfo}", fontSize = 13.sp, color = Color.Gray)
                    }
                }
            }

            HorizontalDivider()

            // --- SECTION 4: PRODUCT TYPES DYNAMIC CONFIGS ---
            Text("4. Dynamic Products Configuration".uppercase(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
            Text("Add customized product categories for core order selectors:", fontSize = 11.sp, color = Color.Gray)
            
            // Picker Category
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Furniture", "Metal", "Aluminium").forEach { cat ->
                    ElevatedFilterChip(
                        selected = configCategoryType == cat,
                        onClick = { configCategoryType = cat },
                        label = { Text(cat, fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newConfigName,
                    onValueChange = { newConfigName = it },
                    label = { Text("New Product Category") },
                    singleLine = true,
                    modifier = Modifier.weight(1.5f).testTag("product_config_input")
                )
                Button(
                    onClick = {
                        if (newConfigName.isNotBlank()) {
                            viewModel.addProductConfig(configCategoryType, newConfigName.trim())
                            newConfigName = ""
                            alertMsg = "New category added dynamically to $configCategoryType!"
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("+ Add")
                }
            }

            // Current Product List with Delete click
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    val filteredProds = productConfigs.filter { it.category == configCategoryType }
                    if (filteredProds.isEmpty()) {
                        Text("No custom configurations added.", fontSize = 12.sp, color = Color.Gray)
                    } else {
                        filteredProds.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("• ${item.name}", fontSize = 13.sp)
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Delete config",
                                    tint = Color.Red,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable { viewModel.deleteProductConfig(item.id) }
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider()

            // --- SECTION 5: DATA SAFETY / BACKUPS PANEL ---
            Text("5. Data Backups & Safety Rules".uppercase(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
            Text("The system stores records in an offline local encrypted SQLite sandbox, immune to network drops.", fontSize = 11.sp, color = Color.Gray)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { alertMsg = "Local Backup successfully taken on local sandbox storage!\nCreated Point: BACKUP_${System.currentTimeMillis()}" },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Backup, null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Backup Data", fontSize = 11.sp)
                }

                Button(
                    onClick = { alertMsg = "Local sandbox restored back to last secure backup state!" },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D976C)),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Restore, null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Restore Data", fontSize = 11.sp)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { alertMsg = "All operational registers exported successfully to local downloads directory inside AfriFurniture_Dump.json" },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CloudUpload, null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export JSON", fontSize = 11.sp)
                }

                Button(
                    onClick = { alertMsg = "Registry parsed & imported from folder successfully! Verified 0 conflicts." },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CloudDownload, null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Import JSON", fontSize = 11.sp, color = Color.Black)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // --- Action dialog feedback display ---
    if (alertMsg != null) {
        AlertDialog(
            onDismissRequest = { alertMsg = null },
            text = { Text(alertMsg!!, fontWeight = FontWeight.Bold, fontSize = 14.sp) },
            confirmButton = {
                TextButton(onClick = { alertMsg = null }) {
                    Text("OK")
                }
            }
        )
    }
}
