package com.example.grama_khata

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.example.grama_khata.data.model.Transaction
import com.example.grama_khata.data.model.TransactionType
import com.example.grama_khata.databinding.DialogAddTransactionBinding

class AddTransactionDialog(
    private val customerId: Int,
    private val onTransactionAdded: (Transaction) -> Unit
) : DialogFragment() {

    private lateinit var binding: DialogAddTransactionBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        binding = DialogAddTransactionBinding.inflate(
            LayoutInflater.from(requireContext())
        )
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(binding.root)
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.92).toInt(),
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawableResource(
            android.R.color.transparent
        )
        setupClickListeners()
        return dialog
    }

    private fun setupClickListeners() {
        binding.btnCancel.setOnClickListener { dismiss() }

        binding.btnSaveTransaction.setOnClickListener {
            val amountText = binding.etAmount.text.toString().trim()
            val note = binding.etNote.text.toString().trim()

            if (amountText.isEmpty()) {
                binding.etAmount.error = "Enter amount"
                return@setOnClickListener
            }

            val amount = amountText.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                binding.etAmount.error = "Enter valid amount"
                return@setOnClickListener
            }

            val type = if (binding.rgTransactionType
                    .checkedRadioButtonId == R.id.rbCredit) {
                TransactionType.CREDIT
            } else {
                TransactionType.PAYMENT
            }

            val transaction = Transaction(
                customerId = customerId,
                amount = amount,
                type = type,
                note = note
            )

            onTransactionAdded(transaction)
            dismiss()
        }
    }
}