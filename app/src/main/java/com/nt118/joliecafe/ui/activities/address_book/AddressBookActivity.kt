package com.nt118.joliecafe.ui.activities.address_book

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.nt118.joliecafe.R
import com.nt118.joliecafe.databinding.ActivityAdressBookBinding
import com.nt118.joliecafe.util.Constants
import com.nt118.joliecafe.util.Constants.Companion.IS_ADD_NEW_ADDRESS

class AddressBookActivity : AppCompatActivity() {
    private var _binding: ActivityAdressBookBinding? = null
    private val binding get() = _binding!!

    private var isAddNewAddress = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityAdressBookBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupActionBar()

        binding.btnAddNewAddress.setOnClickListener {
            setAddNewAddress()
            setAddNewAddressState()
        }

        binding.btnCancel.setOnClickListener {
            setAddNewAddress()
            setAddNewAddressState()
        }

        binding.btnSave.setOnClickListener {
            // function save new address here
            setAddNewAddress()
            setAddNewAddressState()
        }
    }

    private fun setupActionBar() {
        setSupportActionBar(binding.toolbarAddressBookActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        }

        binding.toolbarAddressBookActivity.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setAddNewAddress() {
        isAddNewAddress = !isAddNewAddress
    }

    private fun setAddNewAddressState() {
        if (isAddNewAddress) {
            binding.tvAddAddress.visibility = View.GONE
            binding.btnAddNewAddress.visibility = View.GONE
            binding.cardAddAddressBody.visibility = View.VISIBLE
            binding.etNameLayout.isEnabled = true
            binding.etPhoneLayout.isEnabled = true
            binding.etAddressLayout.isEnabled = true
        } else {
            binding.tvAddAddress.visibility = View.VISIBLE
            binding.tvAddAddress.visibility = View.VISIBLE
            binding.btnAddNewAddress.visibility = View.VISIBLE
            binding.cardAddAddressBody.visibility = View.GONE
            binding.etNameLayout.isEnabled = false
            binding.etPhoneLayout.isEnabled = false
            binding.etAddressLayout.isEnabled = false
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(IS_ADD_NEW_ADDRESS, isAddNewAddress)

    }


    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        isAddNewAddress = savedInstanceState.getBoolean(IS_ADD_NEW_ADDRESS, false)
        setAddNewAddressState()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding  = null
    }
}