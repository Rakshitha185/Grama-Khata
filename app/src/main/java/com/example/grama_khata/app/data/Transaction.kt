package com.example.grama_khata.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    foreignKeys = [ForeignKey(
        entity = Customer::class,
        parentColumns = ["id"],
        childColumns = ["customerId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["customerId"])]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "customerId")
    val customerId: Int,
    @ColumnInfo(name = "amount")
    val amount: Double,
    @ColumnInfo(name = "type")
    val type: String,
    @ColumnInfo(name = "note")
    val note: String = "",
    @ColumnInfo(name = "date")
    val date: Long = System.currentTimeMillis()
)

object TransactionType {
    const val CREDIT = "CREDIT"
    const val PAYMENT = "PAYMENT"
}