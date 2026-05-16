package com.example.grama_khata.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.grama_khata.data.model.Customer
import com.example.grama_khata.data.model.CustomerWithBalance
import com.example.grama_khata.data.model.Transaction
import com.example.grama_khata.repository.GramaKhataRepository
import kotlinx.coroutines.launch
import java.util.Calendar

class GramaKhataViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GramaKhataRepository(application)

    // ========== CUSTOMER DATA ==========

    val allCustomers: LiveData<List<Customer>> = repository.allCustomers
    val customerCount: LiveData<Int> = repository.customerCount
    val totalPendingAmount: LiveData<Double> = repository.totalPendingAmount

    // ========== SORTED CUSTOMERS WITH BALANCE ==========

    private val _customersWithBalance = MediatorLiveData<List<CustomerWithBalance>>()
    val customersWithBalance: LiveData<List<CustomerWithBalance>> = _customersWithBalance

    private var latestCustomers: List<Customer> = emptyList()
    private var latestBalances: Map<Int, Double> = emptyMap()

    init {
        _customersWithBalance.addSource(repository.allCustomers) { customers ->
            latestCustomers = customers
            combineAndSort()
        }
        _customersWithBalance.addSource(repository.allCustomerBalances) { balances ->
            latestBalances = balances.associate { it.customerId to it.balance }
            combineAndSort()
        }
    }

    private fun combineAndSort() {
        val combined = latestCustomers.map { customer ->
            CustomerWithBalance(
                customer = customer,
                netBalance = latestBalances[customer.id] ?: 0.0
            )
        }
        _customersWithBalance.value = combined.sortedByDescending { it.netBalance }
    }

    // ========== SEARCH ==========

    private val _searchQuery = MutableLiveData<String>("")
    val searchQuery: LiveData<String> = _searchQuery

    private val _filteredCustomers = MediatorLiveData<List<CustomerWithBalance>>()
    val filteredCustomers: LiveData<List<CustomerWithBalance>> = _filteredCustomers

    init {
        _filteredCustomers.addSource(_customersWithBalance) { filterCustomers() }
        _filteredCustomers.addSource(_searchQuery) { filterCustomers() }
    }

    private fun filterCustomers() {
        val query = _searchQuery.value?.lowercase() ?: ""
        val all = _customersWithBalance.value ?: emptyList()
        _filteredCustomers.value = if (query.isEmpty()) {
            all
        } else {
            all.filter { cwb ->
                cwb.customer.name.lowercase().contains(query) ||
                        cwb.customer.phone.contains(query)
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // ========== CUSTOMER OPERATIONS ==========

    fun insertCustomer(customer: Customer, onSuccess: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.insertCustomer(customer)
            onSuccess(id)
        }
    }

    fun updateCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.updateCustomer(customer)
        }
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
        }
    }

    fun getCustomerById(customerId: Int): LiveData<Customer> {
        return repository.getCustomerById(customerId)
    }

    // ========== TRANSACTION OPERATIONS ==========

    fun insertTransaction(transaction: Transaction, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.insertTransaction(transaction)
            onSuccess()
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun getTransactionsForCustomer(customerId: Int): LiveData<List<Transaction>> {
        return repository.getTransactionsForCustomer(customerId)
    }

    fun getNetBalanceForCustomer(customerId: Int): LiveData<Double> {
        return repository.getNetBalanceForCustomer(customerId)
    }

    // ========== DAILY REPORT ==========

    private fun getStartOfDay(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    fun getTodayCollection(): LiveData<Double> {
        return repository.getTodayCollection(getStartOfDay())
    }

    fun getTodayTransactions(): LiveData<List<Transaction>> {
        return repository.getTodayTransactions(getStartOfDay())
    }

    fun generateDailyReport(
        customers: List<Customer>,
        todayTransactions: List<Transaction>,
        todayCollection: Double,
        totalPending: Double,
        shopName: String
    ): String {
        val dateFormat = java.text.SimpleDateFormat(
            "dd MMM yyyy",
            java.util.Locale.getDefault()
        )
        val today = dateFormat.format(java.util.Date())
        val sb = StringBuilder()
        sb.appendLine("📊 *Daily Report — $shopName*")
        sb.appendLine("📅 Date: $today")
        sb.appendLine("─────────────────────")
        if (todayTransactions.isEmpty()) {
            sb.appendLine("No transactions today.")
        } else {
            sb.appendLine("📋 *Today's Transactions:*")
            todayTransactions.forEach { transaction ->
                val customerName = customers.find {
                    it.id == transaction.customerId
                }?.name ?: "Unknown"
                val sign = if (transaction.type == "CREDIT") "🔴 +" else "🟢 -"
                val note = if (transaction.note.isNotEmpty())
                    " (${transaction.note})" else ""
                sb.appendLine(
                    "$sign ₹${String.format("%.0f", transaction.amount)}" +
                            " — $customerName$note"
                )
            }
        }
        sb.appendLine("─────────────────────")
        sb.appendLine("💰 *Today's Collection: ₹${String.format("%.0f", todayCollection)}*")
        sb.appendLine("⏳ *Total Pending: ₹${String.format("%.0f", totalPending)}*")
        sb.appendLine("─────────────────────")
        sb.appendLine("Powered by Grama-Khata 🌾")
        return sb.toString()
    }
}