package pro.kosenkov.croncalculator

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import pro.kosenkov.croncalculator.databinding.ActivityMainBinding
import pro.kosenkov.croncalculator.domain.CronExpressionBuilder
import pro.kosenkov.croncalculator.domain.CronHumanReadableFormatter
import pro.kosenkov.croncalculator.domain.CronValidator
import pro.kosenkov.croncalculator.model.CronInput
import pro.kosenkov.croncalculator.utils.ClipboardUtils

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSpinners()
        setupButtons()
        setupFormatControls()
        setupInsets()
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
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
        binding.spinnerMonth.adapter = monthAdapter

        val dayAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.days_of_week_array,
            android.R.layout.simple_spinner_item
        )
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerDayOfWeek.adapter = dayAdapter
    }

    private fun setupButtons() {
        binding.btnGenerate.setOnClickListener { onGenerateClicked() }
        binding.btnDetails.setOnClickListener { showDetailsDialog() }
        binding.btnShare.setOnClickListener { shareCronExpression() }
    }

    private fun setupFormatControls() {
        binding.cbIncludeSeconds.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.layoutSeconds.visibility = View.VISIBLE
                if (binding.etSeconds.text.toString().trim().isEmpty()) {
                    binding.etSeconds.setText("0")
                }
            } else {
                binding.layoutSeconds.visibility = View.GONE
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
        binding.tvResult.text = cronExpression

        ClipboardUtils.copyText(this, "cron_expression", cronExpression)

        binding.scrollViewMain.post {
            binding.scrollViewMain.smoothScrollTo(0, binding.tvResult.bottom)
        }

        showToast("Сгенерировано и скопировано: $cronExpression")
    }

    private fun shareCronExpression() {
        val cron = binding.tvResult.text.toString().trim()

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
            includeSeconds = binding.cbIncludeSeconds.isChecked,
            seconds = normalizeField(binding.etSeconds.text.toString()),
            minutes = normalizeField(binding.etMinutes.text.toString()),
            hours = normalizeField(binding.etHours.text.toString()),
            dayOfMonth = normalizeField(binding.etDayOfMonth.text.toString()),
            month = getSelectedMonth(),
            dayOfWeek = getSelectedDayOfWeek()
        )
    }

    private fun normalizeField(value: String): String {
        val trimmed = value.trim()
        return if (trimmed.isEmpty()) "*" else trimmed
    }

    private fun getSelectedMonth(): String {
        val selected = binding.spinnerMonth.selectedItem.toString()
        return if (selected.startsWith("*")) "*" else selected
    }

    private fun getSelectedDayOfWeek(): String {
        val selected = binding.spinnerDayOfWeek.selectedItem.toString()
        return if (selected.startsWith("*")) "*" else selected
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}