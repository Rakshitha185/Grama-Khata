package com.example.grama_khata

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.grama_khata.adapter.CustomerAdapter
import com.example.grama_khata.databinding.ActivityDashboardBinding
import com.example.grama_khata.viewmodel.GramaKhataViewModel
import com.example.grama_khata.viewmodel.GramaKhataViewModelFactory

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var viewModel: GramaKhataViewModel
    private lateinit var customerAdapter: CustomerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val factory = GramaKhataViewModelFactory(application)
        viewModel = ViewModelProvider(this, factory)[GramaKhataViewModel::class.java]

        setupRecyclerView()
        observeData()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        customerAdapter = CustomerAdapter { customer ->
            val intent = Intent(this, CustomerDetailActivity::class.java)
            intent.putExtra("CUSTOMER_ID", customer.customer.id)
            intent.putExtra("CUSTOMER_NAME", customer.customer.name)
            startActivity(intent)
        }
        binding.recyclerCustomers.apply {
            layoutManager = LinearLayoutManager(this@DashboardActivity)
            adapter = customerAdapter
        }
    }

    private fun observeData() {
        viewModel.filteredCustomers.observe(this) { customers ->
            customerAdapter.submitList(customers)
            if (customers.isEmpty()) {
                binding.layoutEmpty.visibility = View.VISIBLE
                binding.recyclerCustomers.visibility = View.GONE
            } else {
                binding.layoutEmpty.visibility = View.GONE
                binding.recyclerCustomers.visibility = View.VISIBLE
            }
        }

        viewModel.customerCount.observe(this) { count ->
            binding.tvTotalCustomers.text = count.toString()
        }

        viewModel.totalPendingAmount.observe(this) { amount ->
            binding.tvTotalDue.text = "₹${String.format("%.0f", amount ?: 0.0)}"
        }
    }

    private fun setupClickListeners() {
        binding.fabAddCustomer.setOnClickListener {
            startActivity(Intent(this, AddCustomerActivity::class.java))
        }

        binding.btnDailyReport.setOnClickListener {
            showDailyReport()
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setSearchQuery(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun showDailyReport() {
        viewModel.getTodayTransactions().observe(this) { todayTransactions ->
            viewModel.getTodayCollection().observe(this) { todayCollection ->
                viewModel.totalPendingAmount.observe(this) { totalPending ->
                    viewModel.allCustomers.observe(this) { customers ->
                        val report = viewModel.generateDailyReport(
                            customers = customers ?: emptyList(),
                            todayTransactions = todayTransactions ?: emptyList(),
                            todayCollection = todayCollection ?: 0.0,
                            totalPending = totalPending ?: 0.0,
                            shopName = "My Shop"
                        )
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, report)
                        }
                        startActivity(
                            Intent.createChooser(shareIntent, "Share Daily Report")
                        )
                    }
                }
            }
        }
    }
}