package com.example.grama_khata

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.grama_khata.adapter.TransactionAdapter
import com.example.grama_khata.data.model.Transaction
import com.example.grama_khata.databinding.ActivityCustomerDetailBinding
import com.example.grama_khata.viewmodel.GramaKhataViewModel
import com.example.grama_khata.viewmodel.GramaKhataViewModelFactory
import java.io.File

class CustomerDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCustomerDetailBinding
    private lateinit var viewModel: GramaKhataViewModel
    private lateinit var transactionAdapter: TransactionAdapter

    private var customerId: Int = -1
    private var customerName: String = ""
    private var customerPhone: String = ""
    private var currentBalance: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomerDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        customerId = intent.getIntExtra("CUSTOMER_ID", -1)
        customerName = intent.getStringExtra("CUSTOMER_NAME") ?: ""

        if (customerId == -1) {
            Toast.makeText(this, "Error: Customer not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = customerName
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        val factory = GramaKhataViewModelFactory(application)
        viewModel = ViewModelProvider(this, factory)[GramaKhataViewModel::class.java]

        setupRecyclerView()
        observeData()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter { transaction ->
            showDeleteTransactionDialog(transaction)
        }
        binding.recyclerTransactions.apply {
            layoutManager = LinearLayoutManager(this@CustomerDetailActivity)
            adapter = transactionAdapter
        }
    }

    private fun observeData() {
        viewModel.getCustomerById(customerId).observe(this) { customer ->
            customer?.let {
                customerName = it.name
                customerPhone = it.phone
                binding.tvCustomerName.text = it.name
                binding.tvPhone.text = "📞 ${it.phone}"
                binding.tvVillage.text =
                    if (it.village.isNotEmpty()) "📍 ${it.village}" else ""
                if (it.photoPath.isNotEmpty() && File(it.photoPath).exists()) {
                    Glide.with(this)
                        .load(File(it.photoPath))
                        .circleCrop()
                        .placeholder(R.drawable.ic_person_placeholder)
                        .into(binding.ivCustomerPhoto)
                }
            }
        }

        viewModel.getNetBalanceForCustomer(customerId).observe(this) { balance ->
            currentBalance = balance ?: 0.0
            updateBalanceDisplay(currentBalance)
        }

        viewModel.getTransactionsForCustomer(customerId).observe(this) { transactions ->
            transactionAdapter.submitList(transactions)
        }
    }

    private fun updateBalanceDisplay(balance: Double) {
        val absBalance = Math.abs(balance)
        binding.tvNetBalance.text = "₹${String.format("%.0f", absBalance)}"
        when {
            balance > 0 -> {
                binding.tvNetBalance.setTextColor(getColor(R.color.credit_red))
                binding.cardBalance.setCardBackgroundColor(
                    getColor(R.color.credit_red_light)
                )
            }
            balance < 0 -> {
                binding.tvNetBalance.setTextColor(getColor(R.color.payment_green))
                binding.cardBalance.setCardBackgroundColor(
                    getColor(R.color.payment_green_light)
                )
            }
            else -> {
                binding.tvNetBalance.setTextColor(getColor(R.color.text_medium))
                binding.cardBalance.setCardBackgroundColor(getColor(R.color.white))
                binding.tvNetBalance.text = "₹0 (Settled)"
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnAddTransaction.setOnClickListener {
            val dialog = AddTransactionDialog(customerId) { transaction ->
                viewModel.insertTransaction(transaction) {
                    runOnUiThread {
                        val typeText =
                            if (transaction.type == "CREDIT") "Credit" else "Payment"
                        Toast.makeText(
                            this,
                            "✅ $typeText of ₹${transaction.amount.toInt()} added!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            dialog.show(supportFragmentManager, "AddTransaction")
        }

        binding.btnWhatsApp.setOnClickListener {
            sendWhatsAppReminder()
        }
    }

    private fun sendWhatsAppReminder() {
        if (currentBalance <= 0) {
            Toast.makeText(this, "No due amount!", Toast.LENGTH_SHORT).show()
            return
        }
        val message = "Namaskara $customerName, your due at My Shop is " +
                "₹${String.format("%.0f", currentBalance)}. " +
                "Please pay at your earliest convenience. 🙏"
        val phone = "91${customerPhone}"
        try {
            val uri = Uri.parse(
                "https://api.whatsapp.com/send?phone=$phone&text=${Uri.encode(message)}"
            )
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        } catch (e: Exception) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
            }
            startActivity(Intent.createChooser(shareIntent, "Send Reminder via"))
        }
    }

    private fun showDeleteTransactionDialog(transaction: Transaction) {
        AlertDialog.Builder(this)
            .setTitle("Delete Transaction?")
            .setMessage("Delete this ₹${transaction.amount.toInt()} entry?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteTransaction(transaction)
                Toast.makeText(this, "Deleted!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}