package com.example.grama_khata.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.grama_khata.R
import com.example.grama_khata.data.model.CustomerWithBalance
import com.example.grama_khata.databinding.ItemCustomerBinding
import java.io.File

class CustomerAdapter(
    private val onCustomerClick: (CustomerWithBalance) -> Unit
) : ListAdapter<CustomerWithBalance, CustomerAdapter.CustomerViewHolder>(CustomerDiffCallback()) {

    inner class CustomerViewHolder(
        private val binding: ItemCustomerBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(customerWithBalance: CustomerWithBalance) {
            val customer = customerWithBalance.customer
            val balance = customerWithBalance.netBalance

            binding.tvCustomerName.text = customer.name
            binding.tvPhone.text = customer.phone

            val absBalance = Math.abs(balance)
            binding.tvBalance.text = "₹${String.format("%.0f", absBalance)}"

            when {
                balance > 0 -> {
                    binding.tvBalance.setTextColor(
                        binding.root.context.getColor(R.color.credit_red)
                    )
                    binding.tvBalanceLabel.text = "Due"
                }
                balance < 0 -> {
                    binding.tvBalance.setTextColor(
                        binding.root.context.getColor(R.color.payment_green)
                    )
                    binding.tvBalanceLabel.text = "Advance"
                }
                else -> {
                    binding.tvBalance.setTextColor(
                        binding.root.context.getColor(R.color.text_light)
                    )
                    binding.tvBalanceLabel.text = "Settled"
                    binding.tvBalance.text = "₹0"
                }
            }

            if (customer.photoPath.isNotEmpty() &&
                File(customer.photoPath).exists()) {
                Glide.with(binding.root.context)
                    .load(File(customer.photoPath))
                    .circleCrop()
                    .placeholder(R.drawable.ic_person_placeholder)
                    .into(binding.ivCustomerPhoto)
            } else {
                binding.ivCustomerPhoto.setImageResource(
                    R.drawable.ic_person_placeholder
                )
            }

            binding.root.setOnClickListener {
                onCustomerClick(customerWithBalance)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CustomerViewHolder {
        val binding = ItemCustomerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CustomerViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: CustomerViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position))
    }
}

class CustomerDiffCallback : DiffUtil.ItemCallback<CustomerWithBalance>() {
    override fun areItemsTheSame(
        oldItem: CustomerWithBalance,
        newItem: CustomerWithBalance
    ): Boolean {
        return oldItem.customer.id == newItem.customer.id
    }

    override fun areContentsTheSame(
        oldItem: CustomerWithBalance,
        newItem: CustomerWithBalance
    ): Boolean {
        return oldItem == newItem
    }
}