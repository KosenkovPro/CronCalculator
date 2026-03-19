package pro.kosenkov.croncalculator

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import pro.kosenkov.croncalculator.databinding.ActivityCronDetailsBinding

class CronDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCronDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityCronDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupInsets()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarCron)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.cron_details_screen_title)
        }
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.cronDetailsRoot) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}