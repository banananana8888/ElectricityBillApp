package com.example.electricitybillapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bill_table")
data class BillEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val month: String,
    val units: Double,
    val rebate: Double,
    val total: Double,
    val finalCost: Double
)