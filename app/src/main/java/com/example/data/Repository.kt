package com.example.data

import kotlinx.coroutines.flow.Flow

class BusinessRepository(private val appDao: AppDao) {

    // Customers
    val allCustomers: Flow<List<Customer>> = appDao.getAllCustomers()
    suspend fun insertCustomer(customer: Customer): Long = appDao.insertCustomer(customer)
    suspend fun updateCustomer(customer: Customer) = appDao.updateCustomer(customer)
    suspend fun deleteCustomer(id: Int) = appDao.deleteCustomerById(id)

    // Furniture Orders
    val allFurnitureOrders: Flow<List<FurnitureOrder>> = appDao.getAllFurnitureOrders()
    suspend fun insertFurnitureOrder(order: FurnitureOrder): Long = appDao.insertFurnitureOrder(order)
    suspend fun updateFurnitureOrder(order: FurnitureOrder) = appDao.updateFurnitureOrder(order)
    suspend fun softDeleteFurnitureOrder(id: Int) = appDao.softDeleteFurnitureOrder(id)
    suspend fun deleteFurnitureOrderHard(id: Int) = appDao.deleteFurnitureOrderHard(id)

    // Metal Orders
    val allMetalOrders: Flow<List<MetalOrder>> = appDao.getAllMetalOrders()
    suspend fun insertMetalOrder(order: MetalOrder): Long = appDao.insertMetalOrder(order)
    suspend fun updateMetalOrder(order: MetalOrder) = appDao.updateMetalOrder(order)
    suspend fun softDeleteMetalOrder(id: Int) = appDao.softDeleteMetalOrder(id)
    suspend fun deleteMetalOrderHard(id: Int) = appDao.deleteMetalOrderHard(id)

    // Aluminium Orders
    val allAluminiumOrders: Flow<List<AluminiumOrder>> = appDao.getAllAluminiumOrders()
    suspend fun insertAluminiumOrder(order: AluminiumOrder): Long = appDao.insertAluminiumOrder(order)
    suspend fun updateAluminiumOrder(order: AluminiumOrder) = appDao.updateAluminiumOrder(order)
    suspend fun softDeleteAluminiumOrder(id: Int) = appDao.softDeleteAluminiumOrder(id)
    suspend fun deleteAluminiumOrderHard(id: Int) = appDao.deleteAluminiumOrderHard(id)

    // Workers
    val allWorkers: Flow<List<Worker>> = appDao.getAllWorkers()
    suspend fun insertWorker(worker: Worker): Long = appDao.insertWorker(worker)
    suspend fun updateWorker(worker: Worker) = appDao.updateWorker(worker)
    suspend fun deleteWorker(id: Int) = appDao.deleteWorkerById(id)

    // Work Assignments
    val allWorkAssignments: Flow<List<WorkAssignment>> = appDao.getAllWorkAssignments()
    suspend fun insertWorkAssignment(assignment: WorkAssignment): Long = appDao.insertWorkAssignment(assignment)
    suspend fun updateWorkAssignment(assignment: WorkAssignment) = appDao.updateWorkAssignment(assignment)
    suspend fun deleteWorkAssignment(id: Int) = appDao.deleteWorkAssignmentById(id)

    // Material Purchases
    val allMaterialPurchases: Flow<List<MaterialPurchase>> = appDao.getAllMaterialPurchases()
    suspend fun insertMaterialPurchase(purchase: MaterialPurchase): Long = appDao.insertMaterialPurchase(purchase)
    suspend fun updateMaterialPurchase(purchase: MaterialPurchase) = appDao.updateMaterialPurchase(purchase)
    suspend fun deleteMaterialPurchase(id: Int) = appDao.deleteMaterialPurchaseById(id)

    // Sales
    val allSales: Flow<List<Sale>> = appDao.getAllSales()
    suspend fun insertSale(sale: Sale): Long = appDao.insertSale(sale)
    suspend fun updateSale(sale: Sale) = appDao.updateSale(sale)
    suspend fun deleteSale(id: Int) = appDao.deleteSaleById(id)

    // Transactions
    val allTransactions: Flow<List<FinanceTransaction>> = appDao.getAllTransactions()
    suspend fun insertTransaction(transaction: FinanceTransaction): Long = appDao.insertTransaction(transaction)
    suspend fun deleteTransaction(id: Int) = appDao.deleteTransactionById(id)

    // Product Type Configs
    val allProductConfigs: Flow<List<ProductTypeConfig>> = appDao.getAllProductTypeConfigs()
    suspend fun insertProductConfig(config: ProductTypeConfig): Long = appDao.insertProductTypeConfig(config)
    suspend fun deleteProductConfig(id: Int) = appDao.deleteProductTypeConfigById(id)

    // Material Type Configs
    val allMaterialConfigs: Flow<List<MaterialTypeConfig>> = appDao.getAllMaterialTypeConfigs()
    suspend fun insertMaterialConfig(config: MaterialTypeConfig): Long = appDao.insertMaterialTypeConfig(config)
    suspend fun deleteMaterialConfig(id: Int) = appDao.deleteMaterialTypeConfigById(id)

    // Business Profile
    val businessProfile: Flow<BusinessProfile?> = appDao.getBusinessProfileFlow()
    suspend fun getBusinessProfileDirect(): BusinessProfile? = appDao.getBusinessProfileDirect()
    suspend fun updateBusinessProfile(profile: BusinessProfile) = appDao.insertBusinessProfile(profile)

    // User Accounts
    val allUserAccounts: Flow<List<UserAccount>> = appDao.getAllUserAccounts()
    suspend fun insertUserAccount(user: UserAccount): Long = appDao.insertUserAccount(user)
    suspend fun deleteUserAccount(id: Int) = appDao.deleteUserAccountById(id)
}
