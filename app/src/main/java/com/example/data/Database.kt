package com.example.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // --- Customers ---
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<Customer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer): Long

    @Update
    suspend fun updateCustomer(customer: Customer)

    @Query("DELETE FROM customers WHERE id = :id")
    suspend fun deleteCustomerById(id: Int)


    // --- Furniture Orders ---
    @Query("SELECT * FROM furniture_orders WHERE isSoftDeleted = 0 ORDER BY id DESC")
    fun getAllFurnitureOrders(): Flow<List<FurnitureOrder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFurnitureOrder(order: FurnitureOrder): Long

    @Update
    suspend fun updateFurnitureOrder(order: FurnitureOrder)

    @Query("UPDATE furniture_orders SET isSoftDeleted = 1 WHERE id = :id")
    suspend fun softDeleteFurnitureOrder(id: Int)

    @Query("DELETE FROM furniture_orders WHERE id = :id")
    suspend fun deleteFurnitureOrderHard(id: Int)


    // --- Metal Orders ---
    @Query("SELECT * FROM metal_orders WHERE isSoftDeleted = 0 ORDER BY id DESC")
    fun getAllMetalOrders(): Flow<List<MetalOrder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMetalOrder(order: MetalOrder): Long

    @Update
    suspend fun updateMetalOrder(order: MetalOrder)

    @Query("UPDATE metal_orders SET isSoftDeleted = 1 WHERE id = :id")
    suspend fun softDeleteMetalOrder(id: Int)

    @Query("DELETE FROM metal_orders WHERE id = :id")
    suspend fun deleteMetalOrderHard(id: Int)


    // --- Aluminium Orders ---
    @Query("SELECT * FROM aluminium_orders WHERE isSoftDeleted = 0 ORDER BY id DESC")
    fun getAllAluminiumOrders(): Flow<List<AluminiumOrder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAluminiumOrder(order: AluminiumOrder): Long

    @Update
    suspend fun updateAluminiumOrder(order: AluminiumOrder)

    @Query("UPDATE aluminium_orders SET isSoftDeleted = 1 WHERE id = :id")
    suspend fun softDeleteAluminiumOrder(id: Int)

    @Query("DELETE FROM aluminium_orders WHERE id = :id")
    suspend fun deleteAluminiumOrderHard(id: Int)


    // --- Workers ---
    @Query("SELECT * FROM workers ORDER BY name ASC")
    fun getAllWorkers(): Flow<List<Worker>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorker(worker: Worker): Long

    @Update
    suspend fun updateWorker(worker: Worker)

    @Query("DELETE FROM workers WHERE id = :id")
    suspend fun deleteWorkerById(id: Int)


    // --- Work Assignments ---
    @Query("SELECT * FROM work_assignments ORDER BY id DESC")
    fun getAllWorkAssignments(): Flow<List<WorkAssignment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkAssignment(assignment: WorkAssignment): Long

    @Update
    suspend fun updateWorkAssignment(assignment: WorkAssignment)

    @Query("DELETE FROM work_assignments WHERE id = :id")
    suspend fun deleteWorkAssignmentById(id: Int)


    // --- Material Purchases ---
    @Query("SELECT * FROM material_purchases ORDER BY id DESC")
    fun getAllMaterialPurchases(): Flow<List<MaterialPurchase>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterialPurchase(purchase: MaterialPurchase): Long

    @Update
    suspend fun updateMaterialPurchase(purchase: MaterialPurchase)

    @Query("DELETE FROM material_purchases WHERE id = :id")
    suspend fun deleteMaterialPurchaseById(id: Int)


    // --- Sales ---
    @Query("SELECT * FROM sales ORDER BY id DESC")
    fun getAllSales(): Flow<List<Sale>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: Sale): Long

    @Update
    suspend fun updateSale(sale: Sale)

    @Query("DELETE FROM sales WHERE id = :id")
    suspend fun deleteSaleById(id: Int)


    // --- Finance Transactions ---
    @Query("SELECT * FROM finance_transactions ORDER BY id DESC")
    fun getAllTransactions(): Flow<List<FinanceTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: FinanceTransaction): Long

    @Query("DELETE FROM finance_transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Int)


    // --- Product Type Configs ---
    @Query("SELECT * FROM product_type_configs ORDER BY name ASC")
    fun getAllProductTypeConfigs(): Flow<List<ProductTypeConfig>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProductTypeConfig(config: ProductTypeConfig): Long

    @Query("DELETE FROM product_type_configs WHERE id = :id")
    suspend fun deleteProductTypeConfigById(id: Int)


    // --- Material Type Configs ---
    @Query("SELECT * FROM material_type_configs ORDER BY name ASC")
    fun getAllMaterialTypeConfigs(): Flow<List<MaterialTypeConfig>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterialTypeConfig(config: MaterialTypeConfig): Long

    @Query("DELETE FROM material_type_configs WHERE id = :id")
    suspend fun deleteMaterialTypeConfigById(id: Int)


    // --- Business Profile (Single Row) ---
    @Query("SELECT * FROM business_profiles WHERE id = 1 LIMIT 1")
    fun getBusinessProfileFlow(): Flow<BusinessProfile?>

    @Query("SELECT * FROM business_profiles WHERE id = 1 LIMIT 1")
    suspend fun getBusinessProfileDirect(): BusinessProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBusinessProfile(profile: BusinessProfile)


    // --- User Accounts ---
    @Query("SELECT * FROM user_accounts ORDER BY username ASC")
    fun getAllUserAccounts(): Flow<List<UserAccount>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserAccount(user: UserAccount): Long

    @Query("DELETE FROM user_accounts WHERE id = :id")
    suspend fun deleteUserAccountById(id: Int)
}

@Database(
    entities = [
        Customer::class,
        FurnitureOrder::class,
        MetalOrder::class,
        AluminiumOrder::class,
        Worker::class,
        WorkAssignment::class,
        MaterialPurchase::class,
        Sale::class,
        FinanceTransaction::class,
        ProductTypeConfig::class,
        MaterialTypeConfig::class,
        BusinessProfile::class,
        UserAccount::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
}
