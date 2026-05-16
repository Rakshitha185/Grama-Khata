package com.example.grama_khata.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "phone")
    val phone: String,
    @ColumnInfo(name = "village")
    val village: String = "",
    @ColumnInfo(name = "photoPath")
    val photoPath: String = "",
    @ColumnInfo(name = "createdAt")
    val createdAt: Long = System.currentTimeMillis()
)