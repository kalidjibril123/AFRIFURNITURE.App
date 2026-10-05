package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.BusinessViewModel
import com.example.ui.viewmodel.Screen
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: BusinessViewModel,
    modifier: Modifier = Modifier
) {
    val summary by viewModel.dashboardSummary.collectAsState()
    val language by viewModel.currentLanguage.collectAsState()
    val activeRole by viewModel.currentRole.collectAsState()
    val alerts by viewModel.notifications.collectAsState()

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // --- Header Banner ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = viewModel.t("app_name"),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Text(
                    text = "Role: ${activeRole.name} | ${if (language.name == "OROMO") "Afaan Oromoo" else "English"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
            IconButton(
                onClick = { viewModel.navigateTo(Screen.Settings) },
                modifier = Modifier.testTag("dashboard_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // --- Low Stock / Pending Balances Alerts ---
        if (alerts.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Alert",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (language.name == "OROMO") "Akeekkachiisota Hojii" else "Active Business Notifications",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    alerts.take(3).forEach { alert ->
                        Text(
                            text = "• $alert",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }

        // --- Quick Actions Header ---
        Text(
            text = if (language.name == "OROMO") "Gochawwan Saffisaa" else "Quick Actions",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // --- Row of Horizontal Quick Actions ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickActionButton(
                label = "+ ${viewModel.t("furniture")}",
                onClick = { viewModel.navigateTo(Screen.NewFurnitureOrder) },
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f).testTag("quick_new_furniture")
            )
            QuickActionButton(
                label = "+ ${viewModel.t("metal")}",
                onClick = { viewModel.navigateTo(Screen.NewMetalOrder) },
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f).testTag("quick_new_metal")
            )
            QuickActionButton(
                label = "+ ${viewModel.t("aluminium")}",
                onClick = { viewModel.navigateTo(Screen.NewAluminiumOrder) },
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f).testTag("quick_new_aluminium")
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickActionButton(
                label = "+ ${viewModel.t("new_worker")}",
                onClick = { viewModel.navigateTo(Screen.Workers) },
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.weight(1f).testTag("quick_new_worker")
            )
            QuickActionButton(
                label = "+ ${viewModel.t("purchase_btn")}",
                onClick = { viewModel.navigateTo(Screen.NewPurchase) },
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.weight(1f).testTag("quick_new_purchase")
            )
            QuickActionButton(
                label = "+ ${viewModel.t("sales_btn")}",
                onClick = { viewModel.navigateTo(Screen.NewSale) },
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.weight(1f).testTag("quick_new_sale")
            )
        }

        // --- Main Navigation Menu ---
        Text(
            text = if (language.name == "OROMO") "Kutale Hojii" else "Business Modules",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Menu Grid
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MenuGridItem(viewModel.t("furniture_btn"), Icons.Default.Chair, MaterialTheme.colorScheme.primary, { viewModel.navigateTo(Screen.FurnitureList) }, Modifier.weight(1f).testTag("menu_furniture"))
                MenuGridItem(viewModel.t("metal_btn"), Icons.Default.Construction, MaterialTheme.colorScheme.secondary, { viewModel.navigateTo(Screen.MetalList) }, Modifier.weight(1f).testTag("menu_metal"))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MenuGridItem(viewModel.t("aluminium_btn"), Icons.Default.GridOn, MaterialTheme.colorScheme.tertiary, { viewModel.navigateTo(Screen.AluminiumList) }, Modifier.weight(1f).testTag("menu_aluminium"))
                MenuGridItem(viewModel.t("workers_btn"), Icons.Default.Engineering, MaterialTheme.colorScheme.outline, { viewModel.navigateTo(Screen.Workers) }, Modifier.weight(1f).testTag("menu_workers"))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MenuGridItem(viewModel.t("purchase_btn"), Icons.Default.ShoppingBag, MaterialTheme.colorScheme.primary, { viewModel.navigateTo(Screen.MaterialPurchaseList) }, Modifier.weight(1f).testTag("menu_purchase"))
                MenuGridItem(viewModel.t("sales_btn"), Icons.Default.Sell, MaterialTheme.colorScheme.secondary, { viewModel.navigateTo(Screen.SalesList) }, Modifier.weight(1f).testTag("menu_sales"))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MenuGridItem(viewModel.t("inventory"), Icons.Default.Inventory, MaterialTheme.colorScheme.tertiary, { viewModel.navigateTo(Screen.Inventory) }, Modifier.weight(1f).testTag("menu_inventory"))
                MenuGridItem(viewModel.t("finance_btn"), Icons.Default.AccountBalanceWallet, MaterialTheme.colorScheme.outline, { viewModel.navigateTo(Screen.Finance) }, Modifier.weight(1f).testTag("menu_finance"))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MenuGridItem(viewModel.t("reports_btn"), Icons.Default.Analytics, MaterialTheme.colorScheme.primary, { viewModel.navigateTo(Screen.Reports) }, Modifier.weight(1f).testTag("menu_reports"))
                MenuGridItem(viewModel.t("customers_btn"), Icons.Default.Group, MaterialTheme.colorScheme.secondary, { viewModel.navigateTo(Screen.Customers) }, Modifier.weight(1f).testTag("menu_customers"))
            }
        }

        // --- Business Overview KPIs ---
        Text(
            text = if (language.name == "OROMO") "Gabatee Bu'aa fi Gali-Baasii" else "Financial & Production KPIs",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // KPI Section
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = viewModel.t("net_profit"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "ETB ${String.format(Locale.US, "%,.2f", summary.netProfit)}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (summary.netProfit >= 0) MaterialTheme.colorScheme.primary else Color.Red
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(viewModel.t("business_profit"), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Text("ETB ${String.format(Locale.US, "%,.1f", summary.businessProfit)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Column {
                        Text(viewModel.t("business_expenses"), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Text("ETB ${String.format(Locale.US, "%,.1f", summary.businessExpenses)}", fontWeight = FontWeight.Bold, color = Color.Red)
                    }
                    Column {
                        Text(viewModel.t("receivables"), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Text("ETB ${String.format(Locale.US, "%,.1f", summary.customerReceivables)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }

        // KPI Detailed Grid
        Text(
            text = if (language.name == "OROMO") "Abaltiiwwan Biroo" else "Operations Summary",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                KpiCard(viewModel.t("total_customers"), "${summary.totalCustomers}", Icons.Default.Group, Modifier.weight(1f))
                KpiCard(viewModel.t("total_orders"), "${summary.totalOrders}", Icons.Default.Receipt, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                KpiCard(if (language.name == "OROMO") "Hojii Jalqabame" else "Active Jobs", "${summary.activeJobs}", Icons.Default.HourglassTop, Modifier.weight(1f))
                KpiCard(if (language.name == "OROMO") "Xumurame" else "Completed Jobs", "${summary.completedJobs}", Icons.Default.CheckCircle, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                KpiCard(viewModel.t("total_purchases"), "ETB ${String.format(Locale.US, "%,.0f", summary.totalPurchases)}", Icons.Default.ShoppingBag, Modifier.weight(1f))
                KpiCard(viewModel.t("tax_payments"), "ETB ${String.format(Locale.US, "%,.0f", summary.taxPayments)}", Icons.Default.LocalAtm, Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun QuickActionButton(
    label: String,
    onClick: () -> Unit,
    color: Color,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
        modifier = modifier.height(44.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun MenuGridItem(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .height(72.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
fun KpiCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
