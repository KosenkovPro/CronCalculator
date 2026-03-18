package pro.kosenkov.croncalculator

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.ArrayAdapter
import android.widget.Spinner

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        setupSpinners()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupSpinners() {
        val monthSpinner = findViewById<Spinner>(R.id.spinnerMonth)
        val daySpinner = findViewById<Spinner>(R.id.spinnerDayOfWeek)

        val monthAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.months_array,
            android.R.layout.simple_spinner_item
        )
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        monthSpinner.adapter = monthAdapter

        val dayAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.days_of_week_array,
            android.R.layout.simple_spinner_item
        )
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        daySpinner.adapter = dayAdapter
    }
}