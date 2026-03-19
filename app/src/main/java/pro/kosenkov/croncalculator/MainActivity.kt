package pro.kosenkov.croncalculator

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import pro.kosenkov.croncalculator.domain.CronExpressionBuilder
import pro.kosenkov.croncalculator.domain.CronHumanReadableFormatter
import pro.kosenkov.croncalculator.domain.CronValidator
import pro.kosenkov.croncalculator.model.CronInput
import pro.kosenkov.croncalculator.utils.ClipboardUtils

class MainActivity : AppCompatActivity() {

    private lateinit var cbIncludeSeconds: CheckBox
    private lateinit var layoutSeconds: LinearLayout
    private lateinit var etSeconds: EditText
    private lateinit var etMinutes: EditText
    private lateinit var etHours: EditText
    private lateinit var etDayOfMonth: EditText
    private lateinit var spinnerMonth: Spinner
    private lateinit var spinnerDayOfWeek: Spinner
    private lateinit var tvResult: TextView
    private lateinit var scrollView: ScrollView
    private lateinit var btnGenerate: Button
    private lateinit var btnDetails: Button
    private lateinit var btnShare: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        bindViews()
        setupSpinners()
        setupButtons()
        setupFormatControls()
        setupInsets()
    }

    private fun bindViews() {
        cbIncludeSeconds = findViewById(R.id.cbIncludeSeconds)
        layoutSeconds = findViewById(R.id.layoutSeconds)
        etSeconds = findViewById(R.id.etSeconds)
        etMinutes = findViewById(R.id.etMinutes)
        etHours = findViewById(R.id.etHours)
        etDayOfMonth = findViewById(R.id.etDayOfMonth)
        spinnerMonth = findViewById(R.id.spinnerMonth)
        spinnerDayOfWeek = findViewById(R.id.spinnerDayOfWeek)
        tvResult = findViewById(R.id.tvResult)
        scrollView = findViewById(R.id.scrollViewMain)
        btnGenerate = findViewById(R.id.btnGenerate)
        btnDetails = findViewById(R.id.btnDetails)
        btnShare = findViewById(R.id.btnShare)
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupSpinners() {
        val monthAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.months_array,
            android.R.layout.simple_spinner_item
        )
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerMonth.adapter = monthAdapter

        val dayAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.days_of_week_array,
            android.R.layout.simple_spinner_item
        )
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDayOfWeek.adapter = dayAdapter
    }

    private fun setupButtons() {
        btnGenerate.setOnClickListener { onGenerateClicked() }
        btnDetails.setOnClickListener { showDetailsDialog() }
        btnShare.setOnClickListener { shareCronExpression() }
    }

    private fun setupFormatControls() {
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

    private fun onGenerateClicked() {
        val input = readCronInput()

        val validationError = CronValidator.validate(input)
        if (validationError != null) {
            showToast(validationError)
            return
        }

        val cronExpression = CronExpressionBuilder.build(input)
        tvResult.text = cronExpression

        ClipboardUtils.copyText(this, "cron_expression", cronExpression)

        scrollView.post {
            scrollView.smoothScrollTo(0, tvResult.bottom)
        }

        showToast("Сгенерировано и скопировано: $cronExpression")
    }

    private fun shareCronExpression() {
        val cron = tvResult.text.toString().trim()

        if (cron.isEmpty()) {
            showToast("Сначала сгенерируйте выражение")
            return
        }

        val humanReadable = CronHumanReadableFormatter.format(cron)

        val shareText = """
            Cron выражение:
            $cron

            В человекочитаемом виде:
            $humanReadable
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }

        startActivity(Intent.createChooser(intent, "Поделиться через"))
    }

    private fun showDetailsDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.details_title))
            .setMessage(getString(R.string.description))
            .setPositiveButton(getString(R.string.ok), null)
            .setNeutralButton(getString(R.string.details_more_about_cron)) { _, _ ->
                startActivity(Intent(this, CronDetailsActivity::class.java))
            }
            .show()
    }

    private fun readCronInput(): CronInput {
        return CronInput(
            includeSeconds = cbIncludeSeconds.isChecked,
            seconds = normalizeField(etSeconds.text.toString()),
            minutes = normalizeField(etMinutes.text.toString()),
            hours = normalizeField(etHours.text.toString()),
            dayOfMonth = normalizeField(etDayOfMonth.text.toString()),
            month = getSelectedMonth(),
            dayOfWeek = getSelectedDayOfWeek()
        )
    }

    private fun normalizeField(value: String): String {
        val trimmed = value.trim()
        return if (trimmed.isEmpty()) "*" else trimmed
    }

    private fun getSelectedMonth(): String {
        val selected = spinnerMonth.selectedItem.toString()
        return if (selected.startsWith("*")) "*" else selected
    }

    private fun getSelectedDayOfWeek(): String {
        val selected = spinnerDayOfWeek.selectedItem.toString()
        return if (selected.startsWith("*")) "*" else selected
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}