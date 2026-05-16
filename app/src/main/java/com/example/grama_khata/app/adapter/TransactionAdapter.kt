package com.example.grama_khata.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.grama_khata.R
import com.example.grama_khata.data.model.Transaction
import com.example.grama_khata.data.model.TransactionType
import com.example.grama_khata.databinding.ItemTransactionBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransactionAdapter(
    private val onLongClick: (Transaction) -> Unit = {}
) : ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder>(TransactionDiffCallback()) {

    inner class TransactionViewHolder(
        private val binding: ItemTransactionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: Transaction) {
            val dateFormat = SimpleDateFormat(
                "dd MMM yyyy, hh:mm a",
                Locale.getDefault()
            )
            binding.tvDate.text = dateFormat.format(Date(transaction.date))

            binding.tvNote.text = if (transaction.note.isNotEmpty())
                transaction.note
            else
                if (transaction.type == TransactionType.CREDIT)
                    "Credit (Udhaar)"
                else
                    "Payment Received"

            if (transaction.type == TransactionType.CREDIT) {
                binding.tvAmount.text =
                    "+ ₹${String.format("%.0f", transaction.amount)}"
                binding.tvAmount.setTextColor(
                    binding.root.context.getColor(R.color.credit_red)
                )
                binding.viewIndicator.setBackgroundColor(
                    binding.root.context.getColor(R.color.credit_red)
                )
            } else {
                binding.tvAmount.text =
                    "- ₹${String.format("%.0f", transaction.amount)}"
                binding.tvAmount.setTextColor(
                    binding.root.context.getColor(R.color.payment_green)
                )
                binding.viewIndicator.setBackgroundColor(
                    binding.root.context.getColor(R.color.payment_green)
                )
            }

            binding.root.setOnLongClickListener {
                onLongClick(transaction)
                true
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: TransactionViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position))
    }
}

class TransactionDiffCallback : DiffUtil.ItemCallback<Transaction>() {
    override fun areItemsTheSame(
        oldItem: Transaction,
        newItem: Transaction
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: Transaction,
        newItem: Transaction
    ): Boolean {
        return oldItem == newItem
    }
}