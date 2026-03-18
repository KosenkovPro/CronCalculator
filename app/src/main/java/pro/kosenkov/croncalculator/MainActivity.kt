package pro.kosenkov.croncalculator

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.appcompat.app.AlertDialog
import android.widget.ScrollView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        setupSpinners()
        setupButtons()
        setupFormatControls()

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
        val btnDetails = findViewById<Button>(R.id.btnDetails)

        btnGenerate.setOnClickListener {
            generateCronExpressionAndCopy()
        }

        btnDetails.setOnClickListener {
            showDetailsDialog()
        }
    }

    private fun generateCronExpressionAndCopy() {
        val cbIncludeSeconds = findViewById<CheckBox>(R.id.cbIncludeSeconds)

        val etSeconds = findViewById<EditText>(R.id.etSeconds)
        val etMinutes = findViewById<EditText>(R.id.etMinutes)
        val etHours = findViewById<EditText>(R.id.etHours)
        val etDayOfMonth = findViewById<EditText>(R.id.etDayOfMonth)
        val tvResult = findViewById<TextView>(R.id.tvResult)
        val scrollView = findViewById<ScrollView>(R.id.scrollViewMain)

        val seconds = normalizeField(etSeconds.text.toString())
        val minutes = normalizeField(etMinutes.text.toString())
        val hours = normalizeField(etHours.text.toString())
        val dayOfMonth = normalizeField(etDayOfMonth.text.toString())
        val month = getSelectedMonth()
        val dayOfWeek = getSelectedDayOfWeek()

        val validationError = validateCronFields(
            includeSeconds = cbIncludeSeconds.isChecked,
            seconds = seconds,
            minutes = minutes,
            hours = hours,
            dayOfMonth = dayOfMonth
        )

        if (validationError != null) {
            Toast.makeText(this, validationError, Toast.LENGTH_LONG).show()
            return
        }

        val cronExpression = if (cbIncludeSeconds.isChecked) {
            val finalSeconds = if (seconds == "*") "0" else seconds
            "$finalSeconds $minutes $hours $dayOfMonth $month $dayOfWeek"
        } else {
            "$minutes $hours $dayOfMonth $month $dayOfWeek"
        }

        tvResult.text = cronExpression
        copyTextToClipboard(cronExpression)

        scrollView.post {
            scrollView.smoothScrollTo(0, tvResult.bottom)
        }

        Toast.makeText(this, "Сгенерировано и скопировано: $cronExpression", Toast.LENGTH_SHORT).show()
    }

    private fun copyTextToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("cron_expression", text)
        clipboard.setPrimaryClip(clip)
    }



    private fun showDetailsDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.details_title))
            .setMessage(getString(R.string.description))
            .setPositiveButton(getString(R.string.ok), null)
            .show()
    }

    private fun setupFormatControls() {
        val cbIncludeSeconds = findViewById<CheckBox>(R.id.cbIncludeSeconds)
        val layoutSeconds = findViewById<LinearLayout>(R.id.layoutSeconds)
        val etSeconds = findViewById<EditText>(R.id.etSeconds)

        cbIncludeSeconds.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                layoutSeconds.visibility = View.VISIBLE
                if (etSeconds.text.toString().trim().isEmpty()) {
                    etSeconds.setText("0")
                }
            } else {
                layoutSeconds.visibility = View.GONE
            }
        }
    }

    private fun generateCronExpression() {
        val cbIncludeSeconds = findViewById<CheckBox>(R.id.cbIncludeSeconds)

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

        val validationError = validateCronFields(
            includeSeconds = cbIncludeSeconds.isChecked,
            seconds = seconds,
            minutes = minutes,
            hours = hours,
            dayOfMonth = dayOfMonth
        )

        if (validationError != null) {
            Toast.makeText(this, validationError, Toast.LENGTH_LONG).show()
            return
        }

        val cronExpression = if (cbIncludeSeconds.isChecked) {
            val finalSeconds = if (seconds == "*") "0" else seconds
            "$finalSeconds $minutes $hours $dayOfMonth $month $dayOfWeek"
        } else {
            "$minutes $hours $dayOfMonth $month $dayOfWeek"
        }

        tvResult.text = cronExpression
    }

    private fun normalizeField(value: String): String {
        val trimmed = value.trim()
        return if (trimmed.isEmpty()) "*" else trimmed
    }

    private fun getSelectedMonth(): String {
        val spinnerMonth = findViewById<Spinner>(R.id.spinnerMonth)
        val selected = spinnerMonth.selectedItem.toString()
        return if (selected.startsWith("*")) "*" else selected
    }

    private fun getSelectedDayOfWeek(): String {
        val spinnerDayOfWeek = findViewById<Spinner>(R.id.spinnerDayOfWeek)
        val selected = spinnerDayOfWeek.selectedItem.toString()
        return if (selected.startsWith("*")) "*" else selected
    }

    private fun validateCronFields(
        includeSeconds: Boolean,
        seconds: String,
        minutes: String,
        hours: String,
        dayOfMonth: String
    ): String? {
        if (includeSeconds) {
            val error = validateSimpleCronPart(
                value = seconds,
                min = 0,
                max = 59,
                fieldName = "Секунды"
            )
            if (error != null) return error
        }

        validateSimpleCronPart(
            value = minutes,
            min = 0,
            max = 59,
            fieldName = "Минуты"
        )?.let { return it }

        validateSimpleCronPart(
            value = hours,
            min = 0,
            max = 23,
            fieldName = "Часы"
        )?.let { return it }

        validateSimpleCronPart(
            value = dayOfMonth,
            min = 1,
            max = 31,
            fieldName = "День месяца"
        )?.let { return it }

        return null
    }

    private fun validateSimpleCronPart(
        value: String,
        min: Int,
        max: Int,
        fieldName: String
    ): String? {
        if (value == "*") {
            return null
        }

        if (value.startsWith("*/")) {
            val stepPart = value.removePrefix("*/")
            val step = stepPart.toIntOrNull()
            if (step == null || step <= 0) {
                return "$fieldName: шаг должен быть положительным числом, например */5"
            }
            return null
        }

        val number = value.toIntOrNull()
        if (number == null) {
            return "$fieldName: допустимы только *, число или шаг вида */5"
        }

        if (number !in min..max) {
            return "$fieldName: допустимый диапазон $min..$max"
        }

        return null
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