package com.example.grama_khata.repository

import android.content.Context
import com.example.grama_khata.data.database.GramaKhataDatabase
import com.example.grama_khata.data.model.Customer
import com.example.grama_khata.data.model.Transaction

class GramaKhataRepository(context: Context) {

    private val database = GramaKhataDatabase.getDatabase(context)
    private val customerDao = database.customerDao()
    private val transactionDao = database.transactionDao()

    // ========== CUSTOMER ==========

    val allCustomers = customerDao.getAllCustomers()
    val customerCount = customerDao.getCustomerCount()
    val totalPendingAmount = transactionDao.getTotalPendingAmount()
    val allCustomerBalances = transactionDao.getAllCustomerBalances()

    suspend fun insertCustomer(customer: Customer): Long {
        return customerDao.insertCustomer(customer)
    }

    suspend fun updateCustomer(customer: Customer) {
        customerDao.updateCustomer(customer)
    }

    suspend fun deleteCustomer(customer: Customer) {
        customerDao.deleteCustomer(customer)
    }

    fun getCustomerById(customerId: Int) =
        customerDao.getCustomerById(customerId)

    // ========== TRANSACTION ==========

    suspend fun insertTransaction(transaction: Transaction) {
        transactionDao.insertTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)
    }

    fun getTransactionsForCustomer(customerId: Int) =
        transactionDao.getTransactionsForCustomer(customerId)

    fun getNetBalanceForCustomer(customerId: Int) =
        transactionDao.getNetBalanceForCustomer(customerId)

    fun getTodayCollection(startOfDay: Long) =
        transactionDao.getTodayCollection(startOfDay)

    fun getTodayTransactions(startOfDay: Long) =
        transactionDao.getTodayTransactions(startOfDay)
}