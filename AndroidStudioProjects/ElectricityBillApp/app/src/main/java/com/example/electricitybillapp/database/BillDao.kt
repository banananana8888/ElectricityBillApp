package com.example.electricitybillapp.database

import androidx.lifecycle.LiveData
import androidx.room.*


@Dao
interface BillDao {

    @Insert
    suspend fun insert(bill: BillEntity)

    @Update
    suspend fun update(bill: BillEntity)

    @Delete
    suspend fun delete(bill: BillEntity)

    @Query("SELECT * FROM bill_table ORDER BY id DESC")
    fun getAllBills(): LiveData<List<BillEntity>>
}