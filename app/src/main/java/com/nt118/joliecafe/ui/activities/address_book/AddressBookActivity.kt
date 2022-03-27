package com.nt118.joliecafe.ui.activities.address_book

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.nt118.joliecafe.R
import com.nt118.joliecafe.databinding.ActivityAdressBookBinding

class AddressBookActivity : AppCompatActivity() {
    private var _binding: ActivityAdressBookBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityAdressBookBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupActionBar()
    }

    private fun setupActionBar() {
        setSupportActionBar(binding.toolbarAddressBookActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        }

        binding.toolbarAddressBookActivity.setNavigationOnClickListener { onBackPressed() }

        binding.btnAddNewAddress.setOnClickListener {
            binding.tvAddAddress.visibility = View.GONE
            it.visibility = View.GONE
            binding.cardAddAddressBody.visibility = View.VISIBLE
        }

        binding.btnCancel.setOnClickListener {
            binding.tvAddAddress.visibility = View.VISIBLE
            binding.tvAddAddress.visibility = View.VISIBLE
            binding.btnAddNewAddress.visibility = View.VISIBLE
            binding.cardAddAddressBody.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding  = null
    }
}