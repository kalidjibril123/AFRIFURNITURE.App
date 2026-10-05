package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String,
    val address: String,
    val registrationDate: String,
    val notes: String = ""
)

@Entity(tableName = "furniture_orders")
data class FurnitureOrder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val orderNumber: String,
    val customerName: String,
    val phoneNumber: String,
    val registrationDate: String,
    val productType: String,
    val materialType: String,
    val woodType: String,
    val width: Double,
    val height: Double,
    val quantity: Int,
    val m2: Double,
    val pricePerM2: Double,
    val totalPrice: Double,
    val initialPayment: Double,
    val remainingBalance: Double,
    val deliveryDate: String,
    val notes: String = "",
    val photoUri: String = "",
    val status: String = "Pending", // Pending, In Production, Ready, Delivered, Cancelled
    val isSoftDeleted: Boolean = false
)

@Entity(tableName = "metal_orders")
data class MetalOrder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val orderNumber: String,
    val customerName: String,
    val phoneNumber: String,
    val registrationDate: String,
    val productType: String,
    val materialType: String,
    val width: Double,
    val height: Double,
    val quantity: Int,
    val m2: Double,
    val pricePerM2: Double,
    val totalPrice: Double,
    val initialPayment: Double,
    val remainingBalance: Double,
    val deliveryDate: String,
    val notes: String = "",
    val photoUri: String = "",
    val status: String = "Pending", // Pending, In Production, Ready, Delivered, Cancelled
    val isSoftDeleted: Boolean = false
)

@Entity(tableName = "aluminium_orders")
data class AluminiumOrder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val orderNumber: String,
    val customerName: String,
    val phoneNumber: String,
    val registrationDate: String,
    val productType: String,
    val materialType: String,
    val width: Double,
    val height: Double,
    val quantity: Int,
    val m2: Double,
    val pricePerM2: Double,
    val totalPrice: Double,
    val initialPayment: Double,
    val remainingBalance: Double,
    val deliveryDate: String,
    val notes: String = "",
    val photoUri: String = "",
    val status: String = "Pending", // Pending, In Production, Ready, Delivered, Cancelled
    val isSoftDeleted: Boolean = false
)

@Entity(tableName = "workers")
data class Worker(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String,
    val jobSkill: String,
    val registrationDate: String,
    val status: String = "Active" // Active, Inactive, On Leave
)

@Entity(tableName = "work_assignments")
data class WorkAssignment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val workerId: Int,
    val workerName: String,
    val customerName: String,
    val orderNumber: String,
    val workType: String, // Furniture, Metal, Aluminium, Other
    val description: String,
    val dateAssigned: String,
    val expectedCompletionDate: String,
    val actualCompletionDate: String = "",
    val amountAgreed: Double,
    val amountPaid: Double,
    val remainingPayment: Double,
    val workStatus: String = "Assigned" // Assigned, In Progress, Completed, Paid, Pending Payment
)

@Entity(tableName = "material_purchases")
data class MaterialPurchase(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val purchaseNumber: String,
    val date: String,
    val supplierName: String,
    val supplierPhone: String,
    val materialName: String,
    val category: String, // Wood, MDF, Plywood, Metal, Aluminium, Glass, Paint, Accessories, Tools, Other
    val quantity: Double,
    val unit: String,
    val purchasePricePerUnit: Double,
    val totalCost: Double,
    val paymentMade: Double,
    val remainingSupplierBalance: Double,
    val paymentMethod: String, // Cash, Bank, Mobile Money, Other
    val notes: String = "",
    val photoUri: String = ""
)

@Entity(tableName = "sales")
data class Sale(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val saleNumber: String,
    val date: String,
    val customerName: String,
    val phoneNumber: String,
    val materialName: String,
    val category: String,
    val quantity: Double,
    val unit: String,
    val sellingPricePerUnit: Double,
    val totalSale: Double,
    val amountPaid: Double,
    val remainingBalance: Double,
    val paymentMethod: String, // Cash, Bank, Mobile Money, Other
    val photoUri: String = "",
    val notes: String = "",
    val status: String = "Paid" // Paid, Partially Paid, Unpaid, Completed
)

@Entity(tableName = "finance_transactions")
data class FinanceTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val transactionNumber: String,
    val date: String,
    val type: String, // Income, Expense, Tax
    val category: String, // e.g., Furniture Sales, Metal Sales, Aluminium Sales, Material Sales, Material Purchase, Worker Payment, Rent, Electricity, Transport, Maintenance, Tools, Tax Payment, License Payment, Other
    val description: String,
    val amount: Double,
    val paymentMethod: String, // Cash, Bank, Mobile Money, Other
    val reference: String = "",
    val notes: String = ""
)

@Entity(tableName = "product_type_configs")
data class ProductTypeConfig(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String, // Furniture, Metal, Aluminium
    val name: String
)

@Entity(tableName = "material_type_configs")
data class MaterialTypeConfig(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String, // Wood, Metal, Aluminium, Purchase
    val name: String
)

@Entity(tableName = "business_profiles")
data class BusinessProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "AFRI FURNITURE",
    val phone: String = "+251900000000",
    val address: String = "Adama, Ethiopia",
    val email: String = "info@afrifurniture.com",
    val taxNumber: String = "TIN-12345678",
    val bankAccount: String = "CBE - 100023456789",
    val otherInfo: String = "Professional Furniture, Metal & Aluminium Works"
)

@Entity(tableName = "user_accounts")
data class UserAccount(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val password: String,
    val role: String, // ADMIN, MANAGER, WORKER, CASHIER
    val fullName: String,
    val phone: String
)
