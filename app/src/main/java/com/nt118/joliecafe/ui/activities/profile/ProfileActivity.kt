package com.nt118.joliecafe.ui.activities.profile

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.nt118.joliecafe.R
import com.nt118.joliecafe.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {
    private var _binding: ActivityProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupActionBar()
    }

    private fun setupActionBar() {
        setSupportActionBar(binding.toolbarProfileActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        }

        binding.toolbarProfileActivity.setNavigationOnClickListener { onBackPressed() }
    }
}