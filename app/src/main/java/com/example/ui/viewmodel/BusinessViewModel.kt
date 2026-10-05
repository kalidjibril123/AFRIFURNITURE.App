package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class Language {
    ENGLISH, OROMO
}

enum class UserRole {
    ADMIN, MANAGER, WORKER, CASHIER
}

sealed interface Screen {
    object Dashboard : Screen
    object FurnitureList : Screen
    object NewFurnitureOrder : Screen
    data class EditFurnitureOrder(val orderId: Int) : Screen
    object MetalList : Screen
    object NewMetalOrder : Screen
    data class EditMetalOrder(val orderId: Int) : Screen
    object AluminiumList : Screen
    object NewAluminiumOrder : Screen
    data class EditAluminiumOrder(val orderId: Int) : Screen
    object Workers : Screen
    object AssignWork : Screen
    object MaterialPurchaseList : Screen
    object NewPurchase : Screen
    object SalesList : Screen
    object NewSale : Screen
    object Inventory : Screen
    object Customers : Screen
    object Finance : Screen
    object Reports : Screen
    object Settings : Screen
    data class ReceiptView(val orderId: Int, val orderType: String) : Screen // "Furniture", "Metal", "Aluminium", "Sale"
}

class BusinessViewModel(application: Application) : AndroidViewModel(application) {

    // Database & Repository Initialization
    private val database: AppDatabase = Room.databaseBuilder(
        application,
        AppDatabase::class.java,
        "afri_furniture_db"
    ).fallbackToDestructiveMigration().build()

    private val repository = BusinessRepository(database.appDao())

    // Language & Theme & User Role State
    val currentLanguage = MutableStateFlow(Language.ENGLISH)
    val currentRole = MutableStateFlow(UserRole.ADMIN)

    // Current Screen / Routing with Backstack
    private val screenStack = mutableListOf<Screen>(Screen.Dashboard)
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Dashboard)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Notification / Reminder State
    val notifications = MutableStateFlow<List<String>>(emptyList())

    // Database Flows
    val customers = repository.allCustomers
    val furnitureOrders = repository.allFurnitureOrders
    val metalOrders = repository.allMetalOrders
    val aluminiumOrders = repository.allAluminiumOrders
    val workers = repository.allWorkers
    val workAssignments = repository.allWorkAssignments
    val materialPurchases = repository.allMaterialPurchases
    val sales = repository.allSales
    val transactions = repository.allTransactions
    val productConfigs = repository.allProductConfigs
    val materialConfigs = repository.allMaterialConfigs
    val businessProfile = repository.businessProfile
    val userAccounts = repository.allUserAccounts

    // Filter/Search States
    val globalSearchQuery = MutableStateFlow("")
    val dateFilterRange = MutableStateFlow<Pair<Long, Long>?>(null) // Start, End timestamp

    // Prepopulate sample category configs on first start if empty
    init {
        viewModelScope.launch {
            // Check if profile exists, if not, create default
            val profile = repository.getBusinessProfileDirect()
            if (profile == null) {
                repository.updateBusinessProfile(BusinessProfile())
            }

            // Populate some default product types if empty
            productConfigs.first().let { list ->
                if (list.isEmpty()) {
                    repository.insertProductConfig(ProductTypeConfig(category = "Furniture", name = "Door"))
                    repository.insertProductConfig(ProductTypeConfig(category = "Furniture", name = "Table"))
                    repository.insertProductConfig(ProductTypeConfig(category = "Furniture", name = "Chair"))
                    repository.insertProductConfig(ProductTypeConfig(category = "Furniture", name = "Bed"))
                    repository.insertProductConfig(ProductTypeConfig(category = "Furniture", name = "Wardrobe"))
                    repository.insertProductConfig(ProductTypeConfig(category = "Furniture", name = "Sofa"))

                    repository.insertProductConfig(ProductTypeConfig(category = "Metal", name = "Metal Door"))
                    repository.insertProductConfig(ProductTypeConfig(category = "Metal", name = "Gate"))
                    repository.insertProductConfig(ProductTypeConfig(category = "Metal", name = "Window"))
                    repository.insertProductConfig(ProductTypeConfig(category = "Metal", name = "Bed"))

                    repository.insertProductConfig(ProductTypeConfig(category = "Aluminium", name = "Aluminium Door"))
                    repository.insertProductConfig(ProductTypeConfig(category = "Aluminium", name = "Aluminium Window"))
                    repository.insertProductConfig(ProductTypeConfig(category = "Aluminium", name = "Office Partition"))
                }
            }

            // Populate some default materials if empty
            materialConfigs.first().let { list ->
                if (list.isEmpty()) {
                    repository.insertMaterialConfig(MaterialTypeConfig(category = "Wood", name = "MDF"))
                    repository.insertMaterialConfig(MaterialTypeConfig(category = "Wood", name = "Plywood"))
                    repository.insertMaterialConfig(MaterialTypeConfig(category = "Wood", name = "Melamine"))
                    repository.insertMaterialConfig(MaterialTypeConfig(category = "Wood", name = "Natural Wood"))

                    repository.insertMaterialConfig(MaterialTypeConfig(category = "Metal", name = "Square Tube"))
                    repository.insertMaterialConfig(MaterialTypeConfig(category = "Metal", name = "Sheet Metal"))

                    repository.insertMaterialConfig(MaterialTypeConfig(category = "Aluminium", name = "Profile Frame"))
                    repository.insertMaterialConfig(MaterialTypeConfig(category = "Aluminium", name = "Glass Sheet"))
                }
            }

            // Check alerts & low stock
            updateNotifications()
        }
    }

    // Navigation logic
    fun navigateTo(screen: Screen) {
        screenStack.add(screen)
        _currentScreen.value = screen
    }

    fun navigateBack() {
        if (screenStack.size > 1) {
            screenStack.removeAt(screenStack.lastIndex)
            _currentScreen.value = screenStack.last()
        }
    }

    fun navigateToDashboard() {
        screenStack.clear()
        screenStack.add(Screen.Dashboard)
        _currentScreen.value = Screen.Dashboard
    }

    // --- Dynamic Computations / Business Logic ---

    // 1. Dashboard Financial Summary
    // We compute everything dynamically from saved records
    val dashboardSummary = combine(
        customers,
        furnitureOrders,
        metalOrders,
        aluminiumOrders,
        workAssignments,
        materialPurchases,
        sales,
        transactions
    ) { params ->
        val custs = params[0] as List<Customer>
        val fur = params[1] as List<FurnitureOrder>
        val met = params[2] as List<MetalOrder>
        val alu = params[3] as List<AluminiumOrder>
        val works = params[4] as List<WorkAssignment>
        val purchs = params[5] as List<MaterialPurchase>
        val sls = params[6] as List<Sale>
        val txs = params[7] as List<FinanceTransaction>

        // Orders metrics
        val totalCustomers = custs.size
        val activeFur = fur.count { it.status != "Delivered" && it.status != "Cancelled" }
        val activeMet = met.count { it.status != "Delivered" && it.status != "Cancelled" }
        val activeAlu = alu.count { it.status != "Delivered" && it.status != "Cancelled" }
        
        val activeJobs = activeFur + activeMet + activeAlu
        val completedJobs = fur.count { it.status == "Ready" || it.status == "Delivered" } +
                met.count { it.status == "Ready" || it.status == "Delivered" } +
                alu.count { it.status == "Ready" || it.status == "Delivered" }
        val pendingJobs = fur.count { it.status == "Pending" } +
                met.count { it.status == "Pending" } +
                alu.count { it.status == "Pending" }

        // Income collected (only direct payments made on orders or sales, or custom income TXs)
        val furIncome = fur.sumOf { it.initialPayment }
        val metIncome = met.sumOf { it.initialPayment }
        val aluIncome = alu.sumOf { it.initialPayment }
        val salesIncome = sls.sumOf { it.amountPaid }
        val customIncome = txs.filter { it.type == "Income" }.sumOf { it.amount }

        val totalIncome = furIncome + metIncome + aluIncome + salesIncome + customIncome

        // Expenses paid (Purchases paid, worker payments paid, custom expenses paid)
        val purchasesExpense = purchs.sumOf { it.paymentMade }
        val workerPaidExpense = works.sumOf { it.amountPaid }
        val customExpense = txs.filter { it.type == "Expense" }.sumOf { it.amount }

        val totalExpense = purchasesExpense + workerPaidExpense + customExpense

        // Tax Payments
        val taxPaid = txs.filter { it.type == "Tax" }.sumOf { it.amount }

        // Receivables (what customers still owe us)
        val furReceivables = fur.sumOf { it.remainingBalance }
        val metReceivables = met.sumOf { it.remainingBalance }
        val aluReceivables = alu.sumOf { it.remainingBalance }
        val salesReceivables = sls.sumOf { it.remainingBalance }
        val customerReceivables = furReceivables + metReceivables + aluReceivables + salesReceivables

        // Net Profit = Received Income - Expenses Paid - Tax Paid
        val netProfit = totalIncome - totalExpense - taxPaid

        DashboardSummaryData(
            totalCustomers = totalCustomers,
            totalOrders = fur.size + met.size + alu.size,
            furnitureOrdersCount = fur.size,
            metalOrdersCount = met.size,
            aluminiumOrdersCount = alu.size,
            activeJobs = activeJobs,
            completedJobs = completedJobs,
            pendingJobs = pendingJobs,
            totalSales = fur.sumOf { it.totalPrice } + met.sumOf { it.totalPrice } + alu.sumOf { it.totalPrice } + sls.sumOf { it.totalSale },
            totalPurchases = purchs.sumOf { it.totalCost },
            workerPaymentsPaid = workerPaidExpense,
            otherExpenses = customExpense,
            taxPayments = taxPaid,
            customerReceivables = customerReceivables,
            businessProfit = totalIncome, // Collected revenues
            businessExpenses = totalExpense, // Paid costs
            netProfit = netProfit
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        DashboardSummaryData()
    )

    // --- Dynamic Inventory ---
    val inventoryList = combine(materialPurchases, sales) { purchs, sls ->
        val map = mutableMapOf<String, InventoryItem>()

        // Process Purchases (Increases quantities and tracks costs)
        purchs.forEach { p ->
            val existing = map[p.materialName] ?: InventoryItem(
                name = p.materialName,
                category = p.category,
                unit = p.unit
            )
            val updatedPurchased = existing.purchasedQuantity + p.quantity
            val totalCostIncrement = p.totalCost
            val opening = existing.openingQuantity
            
            map[p.materialName] = existing.copy(
                purchasedQuantity = updatedPurchased,
                remainingQuantity = opening + updatedPurchased - existing.soldQuantity - existing.usedQuantity,
                totalCostOfPurchases = existing.totalCostOfPurchases + totalCostIncrement
            )
        }

        // Process Sales (Decreases quantities)
        sls.forEach { s ->
            val existing = map[s.materialName] ?: InventoryItem(
                name = s.materialName,
                category = s.category,
                unit = s.unit
            )
            val updatedSold = existing.soldQuantity + s.quantity
            val opening = existing.openingQuantity

            map[s.materialName] = existing.copy(
                soldQuantity = updatedSold,
                remainingQuantity = opening + existing.purchasedQuantity - updatedSold - existing.usedQuantity
            )
        }

        map.values.toList().map { item ->
            val avgCost = if (item.purchasedQuantity > 0) item.totalCostOfPurchases / item.purchasedQuantity else 0.0
            item.copy(
                averageCost = avgCost,
                currentValue = item.remainingQuantity * avgCost
            )
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // --- Reports Builder ---
    fun generateReportData(
        startDate: Date,
        endDate: Date
    ): Flow<ReportSummaryData> = combine(
        furnitureOrders,
        metalOrders,
        aluminiumOrders,
        materialPurchases,
        sales,
        transactions,
        workAssignments
    ) { params ->
        val fur = params[0] as List<FurnitureOrder>
        val met = params[1] as List<MetalOrder>
        val alu = params[2] as List<AluminiumOrder>
        val purchs = params[3] as List<MaterialPurchase>
        val sls = params[4] as List<Sale>
        val txs = params[5] as List<FinanceTransaction>
        val works = params[6] as List<WorkAssignment>

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        fun isWithinRange(dateStr: String): Boolean {
            return try {
                val d = sdf.parse(dateStr)
                if (d != null) {
                    !d.before(startDate) && !d.after(endDate)
                } else false
            } catch (e: Exception) {
                false
            }
        }

        // Filter lists by date
        val fFur = fur.filter { isWithinRange(it.registrationDate) }
        val fMet = met.filter { isWithinRange(it.registrationDate) }
        val fAlu = alu.filter { isWithinRange(it.registrationDate) }
        val fPurchs = purchs.filter { isWithinRange(it.date) }
        val fSls = sls.filter { isWithinRange(it.date) }
        val fTxs = txs.filter { isWithinRange(it.date) }
        val fWorks = works.filter { isWithinRange(it.dateAssigned) }

        val ordersCount = fFur.size + fMet.size + fAlu.size
        val salesValue = fFur.sumOf { it.totalPrice } + fMet.sumOf { it.totalPrice } + fAlu.sumOf { it.totalPrice } + fSls.sumOf { it.totalSale }
        val purchasesValue = fPurchs.sumOf { it.totalCost }

        // Financial Cash Flows in this range
        val collectedIncome = fFur.sumOf { it.initialPayment } + fMet.sumOf { it.initialPayment } + fAlu.sumOf { it.initialPayment } + fSls.sumOf { it.amountPaid } + fTxs.filter { it.type == "Income" }.sumOf { it.amount }
        val expensesPaid = fPurchs.sumOf { it.paymentMade } + fWorks.sumOf { it.amountPaid } + fTxs.filter { it.type == "Expense" }.sumOf { it.amount }
        val taxPaid = fTxs.filter { it.type == "Tax" }.sumOf { it.amount }

        val outstandingCustomerBalance = fFur.sumOf { it.remainingBalance } + fMet.sumOf { it.remainingBalance } + fAlu.sumOf { it.remainingBalance } + fSls.sumOf { it.remainingBalance }
        val supplierBalance = fPurchs.sumOf { it.remainingSupplierBalance }

        ReportSummaryData(
            totalOrders = ordersCount,
            totalSales = salesValue,
            totalPurchases = purchasesValue,
            totalIncome = collectedIncome,
            totalExpenses = expensesPaid,
            workerPayments = fWorks.sumOf { it.amountPaid },
            taxPayments = taxPaid,
            outstandingCustomerBalance = outstandingCustomerBalance,
            supplierBalance = supplierBalance,
            netProfit = collectedIncome - expensesPaid - taxPaid,
            furnitureCount = fFur.size,
            metalCount = fMet.size,
            aluminiumCount = fAlu.size,
            salesHistoryCount = fSls.size,
            purchasesHistoryCount = fPurchs.size
        )
    }

    // --- Notifications logic ---
    fun updateNotifications() {
        viewModelScope.launch {
            val alerts = mutableListOf<String>()
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val today = Date()

            // 1. Delivery dates near (today or in next 3 days)
            furnitureOrders.first().forEach { o ->
                if (o.status != "Delivered" && o.status != "Cancelled") {
                    try {
                        val d = sdf.parse(o.deliveryDate)
                        if (d != null) {
                            val diff = d.time - today.time
                            val days = diff / (1000 * 60 * 60 * 24)
                            if (days in 0..3) {
                                alerts.add("Furniture Order ${o.orderNumber} is due in $days days! (Customer: ${o.customerName})")
                            }
                        }
                    } catch (e: Exception) {}
                }
            }
            metalOrders.first().forEach { o ->
                if (o.status != "Delivered" && o.status != "Cancelled") {
                    try {
                        val d = sdf.parse(o.deliveryDate)
                        if (d != null) {
                            val diff = d.time - today.time
                            val days = diff / (1000 * 60 * 60 * 24)
                            if (days in 0..3) {
                                alerts.add("Metal Order ${o.orderNumber} is due in $days days! (Customer: ${o.customerName})")
                            }
                        }
                    } catch (e: Exception) {}
                }
            }
            aluminiumOrders.first().forEach { o ->
                if (o.status != "Delivered" && o.status != "Cancelled") {
                    try {
                        val d = sdf.parse(o.deliveryDate)
                        if (d != null) {
                            val diff = d.time - today.time
                            val days = diff / (1000 * 60 * 60 * 24)
                            if (days in 0..3) {
                                alerts.add("Aluminium Order ${o.orderNumber} is due in $days days! (Customer: ${o.customerName})")
                            }
                        }
                    } catch (e: Exception) {}
                }
            }

            // 2. Unpaid balances
            val furUnpaid = furnitureOrders.first().filter { it.remainingBalance > 0 }
            if (furUnpaid.isNotEmpty()) {
                alerts.add("There are ${furUnpaid.size} furniture orders with unpaid customer balances!")
            }

            val metUnpaid = metalOrders.first().filter { it.remainingBalance > 0 }
            if (metUnpaid.isNotEmpty()) {
                alerts.add("There are ${metUnpaid.size} metal orders with unpaid customer balances!")
            }

            val aluUnpaid = aluminiumOrders.first().filter { it.remainingBalance > 0 }
            if (aluUnpaid.isNotEmpty()) {
                alerts.add("There are ${aluUnpaid.size} aluminium orders with unpaid customer balances!")
            }

            // 3. Low Stock Alerts
            inventoryList.first().forEach { item ->
                if (item.remainingQuantity < 5.0) {
                    alerts.add("LOW STOCK: Material '${item.name}' is down to ${item.remainingQuantity} ${item.unit}!")
                }
            }

            // 4. Worker pending payments
            val pendingWorkPayments = workAssignments.first().filter { it.remainingPayment > 0 }
            if (pendingWorkPayments.isNotEmpty()) {
                alerts.add("There are ${pendingWorkPayments.size} worker assignments with pending payments!")
            }

            notifications.value = alerts
        }
    }


    // --- DB Write Operations ---

    // Automatically check / create customer on any order save
    private suspend fun checkAndRegisterCustomer(name: String, phone: String) {
        val list = customers.first()
        val exists = list.any { it.phone.trim() == phone.trim() }
        if (!exists && name.isNotBlank() && phone.isNotBlank()) {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            repository.insertCustomer(
                Customer(
                    name = name,
                    phone = phone,
                    address = "Adama, Ethiopia",
                    registrationDate = sdf.format(Date()),
                    notes = "Registered automatically via order creation."
                )
            )
        }
    }

    // Customer CRUD deletion
    fun deleteCustomer(id: Int) {
        viewModelScope.launch {
            repository.deleteCustomer(id)
            updateNotifications()
        }
    }

    // Furniture Order CRUD
    fun saveFurnitureOrder(order: FurnitureOrder) {
        viewModelScope.launch {
            checkAndRegisterCustomer(order.customerName, order.phoneNumber)
            if (order.id == 0) {
                repository.insertFurnitureOrder(order)
            } else {
                repository.updateFurnitureOrder(order)
            }
            updateNotifications()
        }
    }

    fun deleteFurnitureOrder(id: Int, isHard: Boolean = false) {
        viewModelScope.launch {
            if (isHard) {
                repository.deleteFurnitureOrderHard(id)
            } else {
                repository.softDeleteFurnitureOrder(id)
            }
            updateNotifications()
        }
    }

    // Metal Order CRUD
    fun saveMetalOrder(order: MetalOrder) {
        viewModelScope.launch {
            checkAndRegisterCustomer(order.customerName, order.phoneNumber)
            if (order.id == 0) {
                repository.insertMetalOrder(order)
            } else {
                repository.updateMetalOrder(order)
            }
            updateNotifications()
        }
    }

    fun deleteMetalOrder(id: Int, isHard: Boolean = false) {
        viewModelScope.launch {
            if (isHard) {
                repository.deleteMetalOrderHard(id)
            } else {
                repository.softDeleteMetalOrder(id)
            }
            updateNotifications()
        }
    }

    // Aluminium Order CRUD
    fun saveAluminiumOrder(order: AluminiumOrder) {
        viewModelScope.launch {
            checkAndRegisterCustomer(order.customerName, order.phoneNumber)
            if (order.id == 0) {
                repository.insertAluminiumOrder(order)
            } else {
                repository.updateAluminiumOrder(order)
            }
            updateNotifications()
        }
    }

    fun deleteAluminiumOrder(id: Int, isHard: Boolean = false) {
        viewModelScope.launch {
            if (isHard) {
                repository.deleteAluminiumOrderHard(id)
            } else {
                repository.softDeleteAluminiumOrder(id)
            }
            updateNotifications()
        }
    }

    // Worker CRUD
    fun saveWorker(worker: Worker) {
        viewModelScope.launch {
            if (worker.id == 0) {
                repository.insertWorker(worker)
            } else {
                repository.updateWorker(worker)
            }
            updateNotifications()
        }
    }

    fun deleteWorker(id: Int) {
        viewModelScope.launch {
            repository.deleteWorker(id)
            updateNotifications()
        }
    }

    // Work Assignment CRUD
    fun saveWorkAssignment(assignment: WorkAssignment) {
        viewModelScope.launch {
            if (assignment.id == 0) {
                repository.insertWorkAssignment(assignment)
            } else {
                repository.updateWorkAssignment(assignment)
            }
            updateNotifications()
        }
    }

    fun deleteWorkAssignment(id: Int) {
        viewModelScope.launch {
            repository.deleteWorkAssignment(id)
            updateNotifications()
        }
    }

    // Material Purchase CRUD
    fun saveMaterialPurchase(purchase: MaterialPurchase) {
        viewModelScope.launch {
            if (purchase.id == 0) {
                repository.insertMaterialPurchase(purchase)
            } else {
                repository.updateMaterialPurchase(purchase)
            }
            updateNotifications()
        }
    }

    fun deleteMaterialPurchase(id: Int) {
        viewModelScope.launch {
            repository.deleteMaterialPurchase(id)
            updateNotifications()
        }
    }

    // Sale CRUD
    fun saveSale(sale: Sale) {
        viewModelScope.launch {
            if (sale.id == 0) {
                repository.insertSale(sale)
            } else {
                repository.updateSale(sale)
            }
            updateNotifications()
        }
    }

    fun deleteSale(id: Int) {
        viewModelScope.launch {
            repository.deleteSale(id)
            updateNotifications()
        }
    }

    // Finance Transactions
    fun saveTransaction(tx: FinanceTransaction) {
        viewModelScope.launch {
            repository.insertTransaction(tx)
            updateNotifications()
        }
    }

    fun deleteTransaction(id: Int) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
            updateNotifications()
        }
    }

    // Product Type Settings
    fun addProductConfig(category: String, name: String) {
        viewModelScope.launch {
            repository.insertProductConfig(ProductTypeConfig(category = category, name = name))
        }
    }

    fun deleteProductConfig(id: Int) {
        viewModelScope.launch {
            repository.deleteProductConfig(id)
        }
    }

    // Material Type Settings
    fun addMaterialConfig(category: String, name: String) {
        viewModelScope.launch {
            repository.insertMaterialConfig(MaterialTypeConfig(category = category, name = name))
        }
    }

    fun deleteMaterialConfig(id: Int) {
        viewModelScope.launch {
            repository.deleteMaterialConfig(id)
        }
    }

    // Business Profile Saving
    fun saveBusinessProfile(profile: BusinessProfile) {
        viewModelScope.launch {
            repository.updateBusinessProfile(profile)
        }
    }

    // User Account
    fun saveUserAccount(user: UserAccount) {
        viewModelScope.launch {
            repository.insertUserAccount(user)
        }
    }

    fun deleteUserAccount(id: Int) {
        viewModelScope.launch {
            repository.deleteUserAccount(id)
        }
    }

    // Translation Map
    fun t(key: String): String {
        val lang = currentLanguage.value
        val dict = if (lang == Language.OROMO) oromoTranslations else englishTranslations
        return dict[key] ?: key
    }

    // Bilingual assets
    private val englishTranslations = mapOf(
        "app_name" to "AFRI FURNITURE",
        "dashboard" to "Dashboard",
        "total_customers" to "Total Customers",
        "total_orders" to "Total Orders",
        "furniture" to "Furniture",
        "metal" to "Metal Work",
        "aluminium" to "Aluminium Work",
        "active_jobs" to "Active Jobs",
        "completed_jobs" to "Completed Jobs",
        "pending_jobs" to "Pending Jobs",
        "total_sales" to "Total Sales",
        "total_purchases" to "Total Purchases",
        "worker_payments" to "Worker Payments",
        "other_expenses" to "Other Expenses",
        "tax_payments" to "Tax Payments",
        "receivables" to "Receivables",
        "business_profit" to "Business Income",
        "business_expenses" to "Business Expenses",
        "net_profit" to "Net Profit",
        "furniture_btn" to "FURNITURE",
        "metal_btn" to "METAL",
        "aluminium_btn" to "ALUMINIUM",
        "workers_btn" to "WORKERS",
        "purchase_btn" to "PURCHASES",
        "sales_btn" to "SALES",
        "finance_btn" to "FINANCE",
        "reports_btn" to "REPORTS",
        "customers_btn" to "CUSTOMERS",
        "settings_btn" to "SETTINGS",
        "new_furniture" to "New Furniture Order",
        "new_metal" to "New Metal Order",
        "new_aluminium" to "New Aluminium Order",
        "new_worker" to "New Worker",
        "new_purchase" to "New Purchase",
        "new_sale" to "New Sale",
        "new_payment" to "New Financial Payment",
        "save" to "Save",
        "delete" to "Delete",
        "edit" to "Edit",
        "cancel" to "Cancel",
        "search_hint" to "Search globally...",
        "low_stock" to "Low Stock Warning",
        "unpaid_bal" to "Unpaid Balances",
        "expected_date" to "Expected Date",
        "order_no" to "Order Number",
        "cust_name" to "Customer Name",
        "phone_no" to "Phone Number",
        "prod_type" to "Product Type",
        "mat_type" to "Material Type",
        "wood_type" to "Wood/Material Name",
        "width" to "Width (m)",
        "height" to "Height (m)",
        "quantity" to "Quantity",
        "price_per_m2" to "Price per M2",
        "initial_pay" to "Initial Payment",
        "notes" to "Notes/Specs",
        "delivery_date" to "Delivery Date",
        "status" to "Status",
        "remaining_bal" to "Remaining Balance",
        "supplier_name" to "Supplier Name",
        "supplier_phone" to "Supplier Phone",
        "unit_price" to "Price per Unit",
        "amount_paid" to "Amount Paid",
        "total_cost" to "Total Cost",
        "worker_name" to "Worker Name",
        "job_skill" to "Job / Skill",
        "date_assigned" to "Date Assigned",
        "amount_agreed" to "Amount Agreed",
        "work_status" to "Work Status",
        "income" to "Income",
        "expense" to "Expense",
        "tax" to "Tax",
        "language" to "Language",
        "receipt" to "Receipt",
        "share" to "Share",
        "print" to "Print",
        "generate_pdf" to "Generate PDF",
        "inventory" to "Inventory",
        "reports" to "Reports",
        "admin_panel" to "Admin Panel",
        "business_profile" to "Business Profile",
        "role_access" to "Active Role",
        "role" to "Role",
        "sure_delete" to "Are you sure you want to delete this record?"
    )

    private val oromoTranslations = mapOf(
        "app_name" to "MEEBLII AFRI",
        "dashboard" to "Dajii Gurguddoo",
        "total_customers" to "Ida'ama Maamiltootaa",
        "total_orders" to "Ida'ama Ajajawwan",
        "furniture" to "Meeblii",
        "metal" to "Hojii Sibilaa",
        "aluminium" to "Hojii Alumiiniyamii",
        "active_jobs" to "Hojiiwwan Jalqabaman",
        "completed_jobs" to "Hojii Xumurame",
        "pending_jobs" to "Hojii Hafe",
        "total_sales" to "Gurgurta Guutuu",
        "total_purchases" to "Bitta Guutuu",
        "worker_payments" to "Kaffaltii Hojjettootaa",
        "other_expenses" to "Baasiiwwan Biroo",
        "tax_payments" to "Kaffaltii Gibiraa",
        "receivables" to "Kaffaltii Hambaa",
        "business_profit" to "Galii Daldalaa",
        "business_expenses" to "Baasii Daldalaa",
        "net_profit" to "Bu'aa Qulqulluu",
        "furniture_btn" to "MEEBLII",
        "metal_btn" to "SIBILAA",
        "aluminium_btn" to "ALUMIINIYAM",
        "workers_btn" to "HOJJETTOOTA",
        "purchase_btn" to "BITTAA",
        "sales_btn" to "GURGURTAA",
        "finance_btn" to "MAALLAQA",
        "reports_btn" to "GABASAA",
        "customers_btn" to "MAAMILTOOTA",
        "settings_btn" to "KAA'AMAN",
        "new_furniture" to "Ajaja Meeblii Haaraa",
        "new_metal" to "Ajaja Sibilaa Haaraa",
        "new_aluminium" to "Ajaja Alumiiniyamii Haaraa",
        "new_worker" to "Hojjetaa Haaraa",
        "new_purchase" to "Bittaa Haaraa",
        "new_sale" to "Gurgurtaa Haaraa",
        "new_payment" to "Kaffaltii Haaraa",
        "save" to "Olkaa'i",
        "delete" to "Haqi",
        "edit" to "Gulaali",
        "cancel" to "Dhiisi",
        "search_hint" to "Barbaadi...",
        "low_stock" to "Akeekkachiisa Meeshaa Xiqqaa",
        "unpaid_bal" to "Maallaqa Maamilaa Hambaa",
        "expected_date" to "Guyyaa Eegamu",
        "order_no" to "Lakkoofsa Ajajaa",
        "cust_name" to "Maqaa Maamilaa",
        "phone_no" to "Lakkoofsa Bilbilaa",
        "prod_type" to "Gosa Meeshaa",
        "mat_type" to "Gosa Meeshaa Keessaa",
        "wood_type" to "Maqaa Mukaa/Meeshaa",
        "width" to "Bal'ina (m)",
        "height" to "Hojja (m)",
        "quantity" to "Baay'ina",
        "price_per_m2" to "Gatii m2 tokkoo",
        "initial_pay" to "Kaffaltii Jalqabaa",
        "notes" to "Yaada Dabalataa",
        "delivery_date" to "Guyyaa Deebisamu",
        "status" to "Haala",
        "remaining_bal" to "Hambaa Kaffaltii",
        "supplier_name" to "Maqaa Dhiyeessaa",
        "supplier_phone" to "Bilbila Dhiyeessaa",
        "unit_price" to "Gatii Tokkoo",
        "amount_paid" to "Kaffaltii Raawwatame",
        "total_cost" to "Baasii Guutuu",
        "worker_name" to "Maqaa Hojjetaa",
        "job_skill" to "Ogummaa Hojii",
        "date_assigned" to "Guyyaa Kenname",
        "amount_agreed" to "Maallaqa Waliigalame",
        "work_status" to "Haala Hojii",
        "income" to "Galii",
        "expense" to "Baasii",
        "tax" to "Gibira",
        "language" to "Afaan",
        "receipt" to "Nagahee",
        "share" to "Qoodi",
        "print" to "Maxxansi",
        "generate_pdf" to "PDF Hoji",
        "inventory" to "Meeshaalee Kuusa",
        "reports" to "Gabasawwan",
        "admin_panel" to "Gubbaa Admin",
        "business_profile" to "Profaayilii Daldalaa",
        "role_access" to "Gahee Hojii",
        "role" to "Gahee",
        "sure_delete" to "Dhuguma galmee kana haquu barbaaddaa?"
    )
}

// Data Classes for views
data class DashboardSummaryData(
    val totalCustomers: Int = 0,
    val totalOrders: Int = 0,
    val furnitureOrdersCount: Int = 0,
    val metalOrdersCount: Int = 0,
    val aluminiumOrdersCount: Int = 0,
    val activeJobs: Int = 0,
    val completedJobs: Int = 0,
    val pendingJobs: Int = 0,
    val totalSales: Double = 0.0,
    val totalPurchases: Double = 0.0,
    val workerPaymentsPaid: Double = 0.0,
    val otherExpenses: Double = 0.0,
    val taxPayments: Double = 0.0,
    val customerReceivables: Double = 0.0,
    val businessProfit: Double = 0.0,
    val businessExpenses: Double = 0.0,
    val netProfit: Double = 0.0
)

data class InventoryItem(
    val name: String,
    val category: String,
    val unit: String,
    val openingQuantity: Double = 10.0, // Default base stock for safety
    val purchasedQuantity: Double = 0.0,
    val usedQuantity: Double = 0.0,
    val soldQuantity: Double = 0.0,
    val remainingQuantity: Double = 10.0,
    val averageCost: Double = 0.0,
    val currentValue: Double = 0.0,
    val totalCostOfPurchases: Double = 0.0
)

data class ReportSummaryData(
    val totalOrders: Int = 0,
    val totalSales: Double = 0.0,
    val totalPurchases: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val workerPayments: Double = 0.0,
    val taxPayments: Double = 0.0,
    val outstandingCustomerBalance: Double = 0.0,
    val supplierBalance: Double = 0.0,
    val netProfit: Double = 0.0,
    val furnitureCount: Int = 0,
    val metalCount: Int = 0,
    val aluminiumCount: Int = 0,
    val salesHistoryCount: Int = 0,
    val purchasesHistoryCount: Int = 0
)
