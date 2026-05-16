package com.example.grama_khata.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.grama_khata.data.model.Customer

@Dao
interface CustomerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer): Long

    @Update
    suspend fun updateCustomer(customer: Customer)

    @Delete
    suspend fun deleteCustomer(customer: Customer)

    @Query("SELECT * FROM customers ORDER BY createdAt DESC")
    fun getAllCustomers(): LiveData<List<Customer>>

    @Query("SELECT * FROM customers WHERE id = :customerId")
    fun getCustomerById(customerId: Int): LiveData<Customer>

    @Query("SELECT COUNT(*) FROM customers")
    fun getCustomerCount(): LiveData<Int>
}