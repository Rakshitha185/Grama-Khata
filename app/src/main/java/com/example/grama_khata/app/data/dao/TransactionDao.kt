package com.example.grama_khata.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.grama_khata.data.model.Transaction

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("SELECT * FROM transactions WHERE customerId = :customerId ORDER BY date DESC")
    fun getTransactionsForCustomer(customerId: Int): LiveData<List<Transaction>>

    @Query("""
        SELECT COALESCE(SUM(CASE WHEN type = 'CREDIT' THEN amount ELSE 0 END), 0) -
               COALESCE(SUM(CASE WHEN type = 'PAYMENT' THEN amount ELSE 0 END), 0)
        FROM transactions 
        WHERE customerId = :customerId
    """)
    fun getNetBalanceForCustomer(customerId: Int): LiveData<Double>

    @Query("""
        SELECT COALESCE(SUM(amount), 0) 
        FROM transactions 
        WHERE type = 'PAYMENT' AND date >= :startOfDay
    """)
    fun getTodayCollection(startOfDay: Long): LiveData<Double>

    @Query("""
        SELECT COALESCE(SUM(CASE WHEN type = 'CREDIT' THEN amount ELSE 0 END), 0) -
               COALESCE(SUM(CASE WHEN type = 'PAYMENT' THEN amount ELSE 0 END), 0)
        FROM transactions
    """)
    fun getTotalPendingAmount(): LiveData<Double>

    @Query("""
        SELECT * FROM transactions 
        WHERE date >= :startOfDay 
        ORDER BY date DESC
    """)
    fun getTodayTransactions(startOfDay: Long): LiveData<List<Transaction>>

    @Query("""
        SELECT customerId,
        COALESCE(SUM(CASE WHEN type = 'CREDIT' THEN amount ELSE 0 END), 0) -
        COALESCE(SUM(CASE WHEN type = 'PAYMENT' THEN amount ELSE 0 END), 0) as balance
        FROM transactions
        GROUP BY customerId
    """)
    fun getAllCustomerBalances(): LiveData<List<CustomerBalance>>
}

// Helper class for balance query
data class CustomerBalance(
    val customerId: Int,
    val balance: Double
)