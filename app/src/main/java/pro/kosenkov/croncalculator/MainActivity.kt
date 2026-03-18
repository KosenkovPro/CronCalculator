package pro.kosenkov.croncalculator

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        setupSpinners()
        setupButtons()

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

    private fun setupButtons() {
        val btnGenerate = findViewById<Button>(R.id.btnGenerate)
        val btnCopy = findViewById<Button>(R.id.btnCopy)

        btnGenerate.setOnClickListener {
            generateCronExpression()
        }

        btnCopy.setOnClickListener {
            copyResultToClipboard()
        }
    }

    private fun generateCronExpression() {
        val etSeconds = findViewById<EditText>(R.id.etSeconds)
        val etMinutes = findViewById<EditText>(R.id.etMinutes)
        val etHours = findViewById<EditText>(R.id.etHours)
        val etDayOfMonth = findViewById<EditText>(R.id.etDayOfMonth)

        val tvResult = findViewById<TextView>(R.id.tvResult)

        val seconds = normalizeField(etSeconds.text.toString())
        val minutes = normalizeField(etMinutes.text.toString())
        val hours = normalizeField(etHours.text.toString())
        val dayOfMonth = normalizeField(etDayOfMonth.text.toString())

        val month = getSelectedMonth()
        val dayOfWeek = getSelectedDayOfWeek()

        val cronExpression = "$seconds $minutes $hours $dayOfMonth $month $dayOfWeek"
        tvResult.text = cronExpression
    }

    private fun normalizeField(value: String): String {
        val trimmed = value.trim()
        return if (trimmed.isEmpty()) "*" else trimmed
    }

    private fun getSelectedMonth(): String {
        val spinnerMonth = findViewById<Spinner>(R.id.spinnerMonth)
        val selected = spinnerMonth.selectedItem.toString()

        return when {
            selected.startsWith("*") -> "*"
            else -> selected
        }
    }

    private fun getSelectedDayOfWeek(): String {
        val spinnerDayOfWeek = findViewById<Spinner>(R.id.spinnerDayOfWeek)
        val selected = spinnerDayOfWeek.selectedItem.toString()

        return when {
            selected.startsWith("*") -> "*"
            else -> selected
        }
    }

    private fun copyResultToClipboard() {
        val tvResult = findViewById<TextView>(R.id.tvResult)
        val text = tvResult.text.toString().trim()

        if (text.isEmpty()) {
            Toast.makeText(this, "Нет результата для копирования", Toast.LENGTH_SHORT).show()
            return
        }

        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("cron_expression", text)
        clipboard.setPrimaryClip(clip)

        Toast.makeText(this, "Скопировано: $text", Toast.LENGTH_SHORT).show()
    }
}