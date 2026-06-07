package com.example.electricitybillapp

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.example.electricitybillapp.database.*
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var db: BillDatabase
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.myToolbar)
        setSupportActionBar(toolbar)
        drawerLayout = findViewById(R.id.drawerLayout)
        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val navView = findViewById<NavigationView>(R.id.navView)
        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_history -> startActivity(Intent(this, HistoryActivity::class.java))
                R.id.action_about -> startActivity(Intent(this, AboutActivity::class.java))
            }
            drawerLayout.closeDrawers()
            true
        }


        val spinner = findViewById<Spinner>(R.id.monthSpinner)
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.months_array,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter


        db = BillDatabase.getDatabase(this)
        val unitsInput = findViewById<EditText>(R.id.unitsInput)
        val rebateSeekBar = findViewById<SeekBar>(R.id.rebateSeekBar)
        val rebateLabel = findViewById<TextView>(R.id.rebateLabel)

        rebateSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, f: Boolean) { rebateLabel.text = "Rebate: $p%" }
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {}
        })

        findViewById<Button>(R.id.calcButton).setOnClickListener {
            val units = unitsInput.text.toString().toDoubleOrNull() ?: 0.0
            if (units < 1 || units > 1000) {
                unitsInput.error = "Units must be 1-1000"
                return@setOnClickListener
            }

            val rebatePercent = rebateSeekBar.progress.toDouble() / 100
            val totalCharges = calculateTotal(units)
            val finalCost = totalCharges - (totalCharges * rebatePercent)

            findViewById<TextView>(R.id.totalText).text = "Total: RM %.2f".format(totalCharges)
            findViewById<TextView>(R.id.finalText).text = "Final: RM %.2f".format(finalCost)

            lifecycleScope.launch {
                db.billDao().insert(BillEntity(
                    month = spinner.selectedItem.toString(),
                    units = units,
                    rebate = rebatePercent * 100,
                    total = totalCharges,
                    finalCost = finalCost
                ))
                Toast.makeText(this@MainActivity, "Saved!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun calculateTotal(units: Double): Double = when {
        units <= 200 -> units * 0.218
        units <= 300 -> (200 * 0.218) + ((units - 200) * 0.334)
        units <= 600 -> (200 * 0.218) + (100 * 0.334) + ((units - 300) * 0.516)
        else -> (200 * 0.218) + (100 * 0.334) + (300 * 0.516) + ((units - 600) * 0.546)
    }
}