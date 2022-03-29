package com.nt118.joliecafe.ui.activities.settings

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.nt118.joliecafe.R
import com.nt118.joliecafe.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    private var _binding: ActivitySettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivitySettingsBinding.inflate(layoutInflater)

        setContentView(binding.root)

        setupActionBar()
    }

    private fun setupActionBar() {
        setSupportActionBar(binding.toolbarSettingActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        }

        binding.toolbarSettingActivity.setNavigationOnClickListener { onBackPressed() }
    }
}