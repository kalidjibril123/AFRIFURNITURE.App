package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BusinessProfile
import com.example.ui.viewmodel.BusinessViewModel
import java.util.*

@Composable
fun ReceiptScreen(
    viewModel: BusinessViewModel,
    orderId: Int,
    orderType: String, // "Furniture", "Metal", "Aluminium"
    modifier: Modifier = Modifier
) {
    val profileState by viewModel.businessProfile.collectAsState(initial = BusinessProfile())
    val profile = profileState ?: BusinessProfile()

    val furnitureOrders by viewModel.furnitureOrders.collectAsState(initial = emptyList())
    val metalOrders by viewModel.metalOrders.collectAsState(initial = emptyList())
    val aluminiumOrders by viewModel.aluminiumOrders.collectAsState(initial = emptyList())

    // Simulated receipt actions popup
    var toastMessage by remember { mutableStateOf<String?>(null) }

    // Resolve target order
    val orderData = remember(orderId, orderType, furnitureOrders, metalOrders, aluminiumOrders) {
        when (orderType) {
            "Furniture" -> {
                furnitureOrders.find { it.id == orderId }?.let {
                    ReceiptOrderDetails(
                        orderNo = it.orderNumber,
                        customerName = it.customerName,
                        phone = it.phoneNumber,
                        product = it.productType,
                        material = it.materialType,
                        width = it.width,
                        height = it.height,
                        quantity = it.quantity,
                        m2 = it.m2,
                        pricePerM2 = it.pricePerM2,
                        totalPrice = it.totalPrice,
                        paymentMade = it.initialPayment,
                        remainingBalance = it.remainingBalance,
                        date = it.registrationDate,
                        deliveryDate = it.deliveryDate,
                        status = it.status
                    )
                }
            }
            "Metal" -> {
                metalOrders.find { it.id == orderId }?.let {
                    ReceiptOrderDetails(
                        orderNo = it.orderNumber,
                        customerName = it.customerName,
                        phone = it.phoneNumber,
                        product = it.productType,
                        material = it.materialType,
                        width = it.width,
                        height = it.height,
                        quantity = it.quantity,
                        m2 = it.m2,
                        pricePerM2 = it.pricePerM2,
                        totalPrice = it.totalPrice,
                        paymentMade = it.initialPayment,
                        remainingBalance = it.remainingBalance,
                        date = it.registrationDate,
                        deliveryDate = it.deliveryDate,
                        status = it.status
                    )
                }
            }
            "Aluminium" -> {
                aluminiumOrders.find { it.id == orderId }?.let {
                    ReceiptOrderDetails(
                        orderNo = it.orderNumber,
                        customerName = it.customerName,
                        phone = it.phoneNumber,
                        product = it.productType,
                        material = it.materialType,
                        width = it.width,
                        height = it.height,
                        quantity = it.quantity,
                        m2 = it.m2,
                        pricePerM2 = it.pricePerM2,
                        totalPrice = it.totalPrice,
                        paymentMade = it.initialPayment,
                        remainingBalance = it.remainingBalance,
                        date = it.registrationDate,
                        deliveryDate = it.deliveryDate,
                        status = it.status
                    )
                }
            }
            else -> null
        }
    }

    Scaffold(
        topBar = {
            SimpleTopAppBar(
                title = viewModel.t("receipt"),
                onBackClick = { viewModel.navigateBack() }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        if (orderData == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Error: Order record not found.")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color(0xFFE5E5E5))
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- PRINTABLE RECEIPT CONTAINER ---
                Card(
                    shape = RoundedCornerShape(4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.LightGray)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Logo Placeholder / Business Header
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(28.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalAtm,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = profile.name.uppercase(),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = profile.otherInfo,
                                fontSize = 11.sp,
                                color = Color.Gray,
                                fontStyle = FontStyle.Italic
                            )
                            Text(
                                text = "Tel: ${profile.phone} | TIN: ${profile.taxNumber}",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = profile.address,
                                fontSize = 11.sp,
                                color = Color.Gray,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        DashedLine(color = Color.Gray)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Receipt metadata
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("RECEIPT NO: ${orderData.orderNo}", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                            Text("DATE: ${orderData.date}", fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Customer specs
                        Text("CLIENT INFO:", fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.primary)
                        Text("Name: ${orderData.customerName}", fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                        Text("Phone: ${orderData.phone}", fontSize = 13.sp, fontFamily = FontFamily.Monospace)

                        Spacer(modifier = Modifier.height(8.dp))
                        DashedLine(color = Color.LightGray)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Product specifications
                        Text("ORDER DETAILS:", fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.primary)
                        Text("Category: $orderType Work", fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                        Text("Product Type: ${orderData.product}", fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                        Text("Material/Core: ${orderData.material}", fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                        Text("Dimensions: ${orderData.width}m × ${orderData.height}m (Qty: ${orderData.quantity})", fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                        Text("Total Area: ${String.format(Locale.US, "%.3f", orderData.m2)} m²", fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                        Text("Rate per M2: ETB ${String.format(Locale.US, "%,.1f", orderData.pricePerM2)}", fontSize = 13.sp, fontFamily = FontFamily.Monospace)

                        Spacer(modifier = Modifier.height(8.dp))
                        DashedLine(color = Color.LightGray)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Billing metrics
                        Text("BILLING LEDGER:", fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.primary)
                        ReceiptValueRow("SUBTOTAL", "ETB ${String.format(Locale.US, "%,.1f", orderData.totalPrice)}")
                        ReceiptValueRow("PAYMENT MADE", "ETB ${String.format(Locale.US, "%,.1f", orderData.paymentMade)}")
                        ReceiptValueRow("BALANCE DUE", "ETB ${String.format(Locale.US, "%,.1f", orderData.remainingBalance)}", isBold = true, isRed = orderData.remainingBalance > 0)
                        ReceiptValueRow("DELIVERY DATE", orderData.deliveryDate)
                        ReceiptValueRow("ORDER STATUS", orderData.status)

                        Spacer(modifier = Modifier.height(14.dp))
                        DashedLine(color = Color.Gray)
                        Spacer(modifier = Modifier.height(14.dp))

                        // Signatures & QR Code
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Mock QR Code Box
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .border(1.dp, Color.Gray)
                                    .background(Color(0xFFFAFAFA)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCode2,
                                    contentDescription = "QR Code Scan",
                                    tint = Color.Black,
                                    modifier = Modifier.size(60.dp)
                                )
                            }

                            // Signature Lines
                            Column(horizontalAlignment = Alignment.End) {
                                Spacer(modifier = Modifier.height(28.dp))
                                Box(
                                    modifier = Modifier
                                        .width(120.dp)
                                        .height(1.dp)
                                        .background(Color.Black)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Manager Signature",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    textAlign = TextAlign.Right
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Thank you for choosing AFRI FURNITURE!\nGalatoomaa!",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // --- RECEIPT ACTION TRIGGERS ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { toastMessage = "Receipt PDF file saved to downloads successfully!" },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share Receipt", fontSize = 12.sp)
                    }

                    Button(
                        onClick = { toastMessage = "Connecting to thermal Bluetooth printer..." },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Print, null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Print Receipt", fontSize = 12.sp)
                    }
                }
            }
        }
    }

    // Toast dialog display
    if (toastMessage != null) {
        AlertDialog(
            onDismissRequest = { toastMessage = null },
            text = { Text(toastMessage!!, fontWeight = FontWeight.Bold) },
            confirmButton = {
                TextButton(onClick = { toastMessage = null }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun ReceiptValueRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    isRed: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = Color.Gray)
        Text(
            text = value,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = if (isRed) Color.Red else Color.Unspecified
        )
    }
}

@Composable
fun DashedLine(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
    ) {
        drawLine(
            color = color,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        )
    }
}

// Receipt details wrapper data class
data class ReceiptOrderDetails(
    val orderNo: String,
    val customerName: String,
    val phone: String,
    val product: String,
    val material: String,
    val width: Double,
    val height: Double,
    val quantity: Int,
    val m2: Double,
    val pricePerM2: Double,
    val totalPrice: Double,
    val paymentMade: Double,
    val remainingBalance: Double,
    val date: String,
    val deliveryDate: String,
    val status: String
)
