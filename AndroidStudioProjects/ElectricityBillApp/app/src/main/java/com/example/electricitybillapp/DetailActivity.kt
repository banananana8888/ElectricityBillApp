package com.example.electricitybillapp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.electricitybillapp.database.*
import com.example.electricitybillapp.databinding.ActivityDetailBinding
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class DetailActivity : AppCompatActivity() {
    private lateinit var db: BillDatabase
    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = BillDatabase.getDatabase(this)


        val id = intent.getIntExtra(HistoryActivity.EXTRA_ID, 0)
        val originalUnits = intent.getDoubleExtra(HistoryActivity.EXTRA_UNITS, 0.0)
        val originalMonth = intent.getStringExtra(HistoryActivity.EXTRA_MONTH) ?: "January"
        val originalRebatePercent = intent.getDoubleExtra(HistoryActivity.EXTRA_REBATE_PERCENT, 0.0)


        val displayUnits = if (originalUnits % 1.0 == 0.0) originalUnits.toInt().toString() else originalUnits.toString()
        binding.editUnitsInput.setText(displayUnits)

        val adapter = ArrayAdapter.createFromResource(this, R.array.months_array, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.editMonthSpinner.adapter = adapter
        binding.editMonthSpinner.setSelection(adapter.getPosition(originalMonth).coerceAtLeast(0))

        binding.editRebateSlider.progress = originalRebatePercent.roundToInt().coerceIn(0, 5)
        binding.rebateText.text = "Rebate: ${binding.editRebateSlider.progress}%"


        binding.editUnitsInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { updatePreview() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.editRebateSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, fromUser: Boolean) {
                binding.rebateText.text = "Rebate: $p%"
                updatePreview()
            }
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {}
        })


        binding.editBtn.setOnClickListener {
            val u = binding.editUnitsInput.text.toString().toDoubleOrNull() ?: 0.0
            val selectedMonth = binding.editMonthSpinner.selectedItem.toString()
            val rebateProgress = binding.editRebateSlider.progress

            if (u < 1 || u > 1000) {
                binding.editUnitsInput.error = "Units must be 1-1000"
                return@setOnClickListener
            }

            val t = calculateTotal(u)
            val r = t * (rebateProgress / 100.0)
            val f = t - r

            lifecycleScope.launch {
                db.billDao().update(BillEntity(id, selectedMonth, u, rebateProgress.toDouble(), t, f))
                Toast.makeText(this@DetailActivity, "Bill updated!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        binding.deleteBtn.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete Bill")
                .setMessage("Are you sure?")
                .setPositiveButton("Delete") { _, _ ->
                    lifecycleScope.launch {
                        db.billDao().delete(BillEntity(id, "", 0.0, 0.0, 0.0, 0.0))
                        Toast.makeText(this@DetailActivity, "Deleted", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        binding.cancelBtn.setOnClickListener { finish() }
        updatePreview()
    }

    private fun calculateTotal(u: Double): Double = when {
        u <= 200 -> u * 0.218
        u <= 300 -> (200 * 0.218) + ((u - 200) * 0.334)
        u <= 600 -> (200 * 0.218) + (100 * 0.334) + ((u - 300) * 0.516)
        else -> (200 * 0.218) + (100 * 0.334) + (300 * 0.516) + ((u - 600) * 0.546)
    }

    private fun updatePreview() {
        val u = binding.editUnitsInput.text.toString().toDoubleOrNull() ?: 0.0
        val t = calculateTotal(u)
        val rPercent = binding.editRebateSlider.progress / 100.0
        val r = t * rPercent
        val f = t - r
        binding.resultPreview.text = "Rebate: RM ${"%.2f".format(r)} | Final: RM ${"%.2f".format(f)}"
    }
}