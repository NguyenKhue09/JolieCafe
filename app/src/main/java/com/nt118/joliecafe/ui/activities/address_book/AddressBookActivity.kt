package com.nt118.joliecafe.ui.activities.address_book

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.nt118.joliecafe.R
import com.nt118.joliecafe.adapter.AddressBookAdapter
import com.nt118.joliecafe.databinding.ActivityAdressBookBinding
import com.nt118.joliecafe.ui.activities.login.LoginActivity
import com.nt118.joliecafe.util.AddressItemComparator
import com.nt118.joliecafe.util.ApiResult
import com.nt118.joliecafe.util.Constants.Companion.IS_ADD_NEW_ADDRESS
import com.nt118.joliecafe.util.NetworkListener
import com.nt118.joliecafe.viewmodels.address_book.AddressBookViewModel
import com.nt118.joliecafe.viewmodels.profile.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class AddressBookActivity : AppCompatActivity() {
    private var _binding: ActivityAdressBookBinding? = null
    private val binding get() = _binding!!
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private lateinit var networkListener: NetworkListener
    private val addressViewModel by viewModels<AddressBookViewModel>()
    private var isAddNewAddress = false
    private lateinit var addressBookAdapter: AddressBookAdapter
    lateinit var updateAddressStatus: LiveData<Boolean>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityAdressBookBinding.inflate(layoutInflater)
        setContentView(binding.root)

        updateAddressStatus = addressViewModel.updateAddressStatus

        if (currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        addressViewModel.readBackOnline.asLiveData().observe(this) {
            addressViewModel.backOnline = it
        }

        val diffCallBack = AddressItemComparator
        val addressesRecyclerView = binding.addressRecyclerView
        addressBookAdapter = AddressBookAdapter(differCallback = diffCallBack, addressBookActivity = this)
        addressesRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        addressesRecyclerView.adapter = addressBookAdapter

        lifecycleScope.launchWhenStarted {
            addressViewModel.readUserToken.collectLatest { token ->
                addressViewModel.userToken = token
                addressViewModel.getAddresses(addressViewModel.userToken).collectLatest {  data ->
                    addressBookAdapter.submitData(data)
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            networkListener = NetworkListener()
            networkListener.checkNetworkAvailability(this@AddressBookActivity)
                .collect { status ->
                    addressViewModel.networkStatus = status
                    addressViewModel.showNetworkStatus()
                    if(addressViewModel.backOnline) {
                        addressViewModel.getAddresses(addressViewModel.userToken).collectLatest {  data ->
                            addressBookAdapter.submitData(data)
                        }
                    }
                }
        }

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

            val name = binding.etName.text.toString()
            val phone = binding.etPhone.text.toString()
            val address = binding.etAddress.text.toString()

            val nameErr = validateUserName(name = name)
            val phoneErr = validatePhone(phone = phone)
            val addressErr = validateAddress(address = address)

            val error = listOf(nameErr, phoneErr, addressErr).any { it != null }

            if (!error) {
                if (addressViewModel.networkStatus) {
                    val addressData = mapOf(
                        "phone" to phone,
                        "userName" to name,
                        "address" to address
                    )
                    addNewAddress(addressData = addressData)
                } else {
                    addressViewModel.showNetworkStatus()
                }
            } else {
                if (nameErr != null) {
                    binding.etNameLayout.error = nameErr
                    binding.etName.requestFocus()
                } else {
                    binding.etNameLayout.error = null
                }
                if (phoneErr != null) {
                    binding.etPhoneLayout.error = phoneErr
                    binding.etPhone.requestFocus()
                } else {
                    binding.etPhoneLayout.error = null
                }
                if (addressErr != null) {
                    binding.etAddressLayout.error = addressErr
                    binding.etAddress.requestFocus()
                }else {
                    binding.etAddressLayout.error = null
                }
            }

            setAddNewAddress()
            setAddNewAddressState()
        }

        handleApiResponse()
        handlePagingAdapterState()
    }

    private fun handlePagingAdapterState() {
        addressBookAdapter.addLoadStateListener { loadState ->
            if (loadState.refresh is LoadState.Loading){
                binding.addressCircularProgressIndicator.visibility = View.VISIBLE
            }
            else{
                binding.addressCircularProgressIndicator.visibility = View.INVISIBLE
                // getting the error
                val error = when {
                    loadState.prepend is LoadState.Error -> loadState.prepend as LoadState.Error
                    loadState.append is LoadState.Error -> loadState.append as LoadState.Error
                    loadState.refresh is LoadState.Error -> loadState.refresh as LoadState.Error
                    else -> null
                }
                error?.let {
                    Toast.makeText(this, it.error.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun handleApiResponse() {
        addressViewModel.addNewAddressResponse.observe(this) { response ->
            when (response) {
                is ApiResult.Loading -> {

                }
                is ApiResult.Success -> {
                    Toast.makeText(this, "Add new address successful", Toast.LENGTH_SHORT).show()
                    addressBookAdapter.refresh()
                }
                is ApiResult.Error -> {
                    Toast.makeText(this, response.message, Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }

        addressViewModel.addNewDefaultAddressResponse.observe(this) { response ->
            when (response) {
                is ApiResult.Loading -> {

                }
                is ApiResult.Success -> {
                    Toast.makeText(this, "Add new default address successful", Toast.LENGTH_SHORT).show()
                    addressBookAdapter.refresh()
                }
                is ApiResult.Error -> {
                    Toast.makeText(this, response.message, Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }

        addressViewModel.deleteAddressResponse.observe(this) { response ->
            when (response) {
                is ApiResult.Loading -> {

                }
                is ApiResult.Success -> {
                    Toast.makeText(this, "Delete address successful", Toast.LENGTH_SHORT).show()
                    addressBookAdapter.refresh()
                }
                is ApiResult.Error -> {
                    Toast.makeText(this, response.message, Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }

        addressViewModel.updateAddressResponse.observe(this) { response ->
            val status =  when (response) {
                is ApiResult.Loading -> {
                    false
                }
                is ApiResult.Success -> {
                    Toast.makeText(this, "Updated address successful", Toast.LENGTH_SHORT).show()
                    true
                }
                is ApiResult.Error -> {
                    Toast.makeText(this, response.message, Toast.LENGTH_SHORT).show()
                    false
                }
                else -> {false}
            }
            addressViewModel.setUpdateAddressStatus(status = status)
        }
    }

    fun deleteAddress(addressId: String) {
        if(addressViewModel.networkStatus) {
            addressViewModel.deleteAddress(
                addressId = addressId,
                token = addressViewModel.userToken
            )
        } else {
            addressViewModel.showNetworkStatus()
        }

    }

    private fun addNewAddress(addressData: Map<String, String>) {
        if (binding.isDefaultAddress.isChecked) {
            addressViewModel.addNewDefaultAddress(
                data = addressData,
                token = addressViewModel.userToken
            )
        } else {
            addressViewModel.addNewAddress(
                data = addressData,
                token = addressViewModel.userToken
            )
        }
    }

    fun updateAddress(newAddressData: Map<String, String>) {
        if (addressViewModel.networkStatus) {
            addressViewModel.updateAddress(
                newAddressData = newAddressData,
                token = addressViewModel.userToken
            )
        } else {
            addressViewModel.showNetworkStatus()
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
        _binding = null
    }

    fun validateUserName(name: String): String? {
        if (name.isBlank()) {
            return "Name is blank!"
        }
        return null
    }

    fun validateAddress(address: String): String? {
        if (address.isBlank()) {
            return "Address is blank!"
        }
        return null
    }

    fun validatePhone(phone: String): String? {
        if (phone.isBlank()) {
            return "Phone is blank!"
        }
        if (phone.length != 10) {
            return "This is a valid phone number!"
        }
        return null
    }
}