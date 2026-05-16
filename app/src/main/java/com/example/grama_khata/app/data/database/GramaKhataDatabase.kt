package com.example.grama_khata.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.grama_khata.data.dao.CustomerDao
import com.example.grama_khata.data.dao.TransactionDao
import com.example.grama_khata.data.model.Customer
import com.example.grama_khata.data.model.Transaction

@Database(
    entities = [Customer::class, Transaction::class],
    version = 1,
    exportSchema = false
)
abstract class GramaKhataDatabase : RoomDatabase() {

    abstract fun customerDao(): CustomerDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: GramaKhataDatabase? = null

        fun getDatabase(context: Context): GramaKhataDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GramaKhataDatabase::class.java,
                    "grama_khata_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}