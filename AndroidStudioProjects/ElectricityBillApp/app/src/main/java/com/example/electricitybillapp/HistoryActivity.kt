package com.example.electricitybillapp

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.electricitybillapp.database.*

class HistoryActivity : AppCompatActivity() {
    private lateinit var db: BillDatabase
    private var listData: List<BillEntity> = emptyList()

    companion object {
        const val EXTRA_ID = "com.example.electricitybillapp.ID"
        const val EXTRA_UNITS = "com.example.electricitybillapp.UNITS"
        const val EXTRA_MONTH = "com.example.electricitybillapp.MONTH"
        const val EXTRA_REBATE_PERCENT = "com.example.electricitybillapp.REBATE_PERCENT"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val listView = findViewById<ListView>(R.id.listView)
        db = BillDatabase.getDatabase(this)

        db.billDao().getAllBills().observe(this, Observer { list ->
            listData = list ?: emptyList()
            val display = listData.map { "${it.month} (${it.units.toInt()} kWh) - RM %.2f".format(it.finalCost) }
            listView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, display)
        })

        listView.setOnItemClickListener { _, _, i, _ ->
            if (i < listData.size) {
                val bill = listData[i]

                val percent = bill.rebate

                val intent = Intent(this, DetailActivity::class.java).apply {
                    putExtra(EXTRA_ID, bill.id)
                    putExtra(EXTRA_MONTH, bill.month)
                    putExtra(EXTRA_UNITS, bill.units)
                    putExtra(EXTRA_REBATE_PERCENT, percent)
                }
                startActivity(intent)
            }
        }
    }
}