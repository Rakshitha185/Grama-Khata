package com.example.grama_khata.data.model

// This is NOT a database table
// It's a helper class that combines Customer + their balance
// We use this to show each customer with their due amount on Dashboard
data class CustomerWithBalance(
    val customer: Customer,
    val netBalance: Double
)