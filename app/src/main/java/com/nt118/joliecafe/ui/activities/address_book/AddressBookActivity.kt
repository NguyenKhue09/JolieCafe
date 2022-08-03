package com.nt118.joliecafe.ui.activities.address_book

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.nt118.joliecafe.R
import com.nt118.joliecafe.adapter.AddressBookAdapter
import com.nt118.joliecafe.databinding.ActivityAdressBookBinding
import com.nt118.joliecafe.ui.activities.login.LoginActivity
import com.nt118.joliecafe.util.AddressItemComparator
import com.nt118.joliecafe.util.ApiResult
import com.nt118.joliecafe.util.Constants
import com.nt118.joliecafe.util.Constants.Companion.IS_ADD_NEW_ADDRESS
import com.nt118.joliecafe.util.Constants.Companion.SNACK_BAR_STATUS_DISABLE
import com.nt118.joliecafe.util.Constants.Companion.SNACK_BAR_STATUS_ERROR
import com.nt118.joliecafe.util.Constants.Companion.SNACK_BAR_STATUS_SUCCESS
import com.nt118.joliecafe.util.NetworkListener
import com.nt118.joliecafe.util.extenstions.observeOnce
import com.nt118.joliecafe.util.extenstions.setCustomBackground
import com.nt118.joliecafe.util.extenstions.setIcon
import com.nt118.joliecafe.viewmodels.address_book.AddressBookViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

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
    lateinit var readDefaultAddressId: LiveData<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityAdressBookBinding.inflate(layoutInflater)
        setContentView(binding.root)

        updateAddressStatus = addressViewModel.updateAddressStatus
        readDefaultAddressId = addressViewModel.readDefaultAddress.asLiveData()

        if (currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        updateBackOnlineStatus()
        updateNetworkStatus()
        observerNetworkMessage()

        setupAddressBookAdapter()

        initAddressBookAdapterData()


        setupActionBar()

        handleButtonClicked()

        handleApiResponse()
        handlePagingAdapterState()
    }

    private fun handleButtonClicked() {
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
                } else {
                    binding.etAddressLayout.error = null
                }
            }

            setAddNewAddress()
            setAddNewAddressState()
        }
    }

    private fun initAddressBookAdapterData() {
        networkListener = NetworkListener()
        networkListener.checkNetworkAvailability(this)
            .asLiveData().observeOnce(this) { status ->
                addressViewModel.networkStatus = status
                getAddressData()
            }
    }

    private fun recallDataIfBackOnline() {
        if(addressViewModel.backOnline) {
            getAddressData()
        }
    }

    private fun getAddressData() {
        lifecycleScope.launchWhenStarted {
            addressViewModel.getAddresses(addressViewModel.userToken)
                .collectLatest { data ->
                    addressBookAdapter.submitData(data)
                }
        }
    }

    private fun setupAddressBookAdapter() {
        val diffCallBack = AddressItemComparator
        val addressesRecyclerView = binding.addressRecyclerView
        addressBookAdapter =
            AddressBookAdapter(differCallback = diffCallBack, addressBookActivity = this)

        addressesRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        addressesRecyclerView.adapter = addressBookAdapter
    }

    private fun observerNetworkMessage() {
        addressViewModel.networkMessage.observe(this) { message ->
            if (!addressViewModel.networkStatus) {
                showSnackBar(
                    message = message,
                    status = Constants.SNACK_BAR_STATUS_DISABLE,
                    icon = R.drawable.ic_wifi_off
                )
            } else if (addressViewModel.networkStatus) {
                if (addressViewModel.backOnline) {
                    showSnackBar(
                        message = message,
                        status = Constants.SNACK_BAR_STATUS_SUCCESS,
                        icon = R.drawable.ic_wifi
                    )
                }
            }
        }
    }

    private fun updateNetworkStatus() {
        networkListener = NetworkListener()
        networkListener.checkNetworkAvailability(this)
            .asLiveData().observe(this) { status ->
                addressViewModel.networkStatus = status
                addressViewModel.showNetworkStatus()
                recallDataIfBackOnline()
            }
    }

    private fun updateBackOnlineStatus() {
        addressViewModel.readBackOnline.asLiveData().observe(this) {
            addressViewModel.backOnline = it
        }
    }

    private fun saveDefaultAddressId(addressId: String) {
        addressViewModel.saveDefaultAddressId(defaultAddressId = addressId)
    }

    private fun handlePagingAdapterState() {
        addressBookAdapter.addLoadStateListener { loadState ->
            if (loadState.refresh is LoadState.Loading) {
                binding.addressCircularProgressIndicator.visibility = View.VISIBLE
            } else {
                binding.addressCircularProgressIndicator.visibility = View.INVISIBLE
                // getting the error
                val error = when {
                    loadState.prepend is LoadState.Error -> loadState.prepend as LoadState.Error
                    loadState.append is LoadState.Error -> loadState.append as LoadState.Error
                    loadState.refresh is LoadState.Error -> loadState.refresh as LoadState.Error
                    else -> null
                }
                error?.let {
                    if (addressViewModel.networkStatus)
                        showSnackBar(
                            message = "Get address data failed!",
                            status = SNACK_BAR_STATUS_ERROR,
                            icon = R.drawable.ic_error
                        )
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
                    showSnackBar(
                        message = "Add new address successful",
                        status = SNACK_BAR_STATUS_SUCCESS,
                        icon = R.drawable.ic_success
                    )
                    addressBookAdapter.refresh()
                }
                is ApiResult.Error -> {
                    showSnackBar(
                        message = "Add new address failed!",
                        status = SNACK_BAR_STATUS_ERROR,
                        icon = R.drawable.ic_error
                    )
                }
                else -> {}
            }
        }

        addressViewModel.addNewDefaultAddressResponse.observe(this) { response ->
            when (response) {
                is ApiResult.Loading -> {

                }
                is ApiResult.Success -> {
                    showSnackBar(
                        message = "Add new default address successful",
                        status = SNACK_BAR_STATUS_SUCCESS,
                        icon = R.drawable.ic_success
                    )
                    response.data?.defaultAddress?.let {
                        saveDefaultAddressId(it)
                    }

                    addressBookAdapter.refresh()
                }
                is ApiResult.Error -> {
                    showSnackBar(
                        message = "Add new default address failed!",
                        status = SNACK_BAR_STATUS_ERROR,
                        icon = R.drawable.ic_error
                    )
                }
                else -> {}
            }
        }

        addressViewModel.deleteAddressResponse.observe(this) { response ->
            when (response) {
                is ApiResult.Loading -> {

                }
                is ApiResult.Success -> {
                    showSnackBar(
                        message = "Delete address successful",
                        status = SNACK_BAR_STATUS_SUCCESS,
                        icon = R.drawable.ic_success
                    )
                    addressBookAdapter.refresh()
                }
                is ApiResult.Error -> {
                    showSnackBar(
                        message = "Delete address failed!",
                        status = SNACK_BAR_STATUS_ERROR,
                        icon = R.drawable.ic_error
                    )
                }
                else -> {}
            }
        }

        addressViewModel.updateAddressResponse.observe(this) { response ->
            val status = when (response) {
                is ApiResult.Loading -> {
                    false
                }
                is ApiResult.Success -> {
                    showSnackBar(
                        message = "Update address successful",
                        status = SNACK_BAR_STATUS_SUCCESS,
                        icon = R.drawable.ic_success
                    )
                    true
                }
                is ApiResult.Error -> {
                    showSnackBar(
                        message = "Update address failed!",
                        status = SNACK_BAR_STATUS_ERROR,
                        icon = R.drawable.ic_error
                    )
                    false
                }
                else -> {
                    false
                }
            }
            addressViewModel.setUpdateAddressStatus(status = status)
        }

        addressViewModel.updateUserDataResponse.observe(this) { response ->
            when (response) {
                is ApiResult.Loading -> {

                }
                is ApiResult.Success -> {
                    showSnackBar(
                        message = "Update default address successful",
                        status = SNACK_BAR_STATUS_SUCCESS,
                        icon = R.drawable.ic_success
                    )
                    response.data?.defaultAddress?.let {
                        saveDefaultAddressId(it)
                    }
                    addressViewModel.setUpdateAddressStatus(status = true)
                }
                is ApiResult.Error -> {
                    showSnackBar(
                        message = "Update default address failed!",
                        status = SNACK_BAR_STATUS_ERROR,
                        icon = R.drawable.ic_error
                    )
                }
                else -> {}
            }
        }
    }

    fun deleteAddress(addressId: String) {
        if (addressViewModel.networkStatus) {
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

    fun updateUserInfos(newData: Map<String, String>) {
        addressViewModel.updateUserInfos(token = addressViewModel.userToken, newUserData = newData)
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

    private fun showSnackBar(message: String, status: Int, icon: Int) {
        val drawable = getDrawable(icon)

        val snackBarContentColor = when (status) {
            Constants.SNACK_BAR_STATUS_SUCCESS -> R.color.text_color_2
            Constants.SNACK_BAR_STATUS_DISABLE -> R.color.dark_text_color
            Constants.SNACK_BAR_STATUS_ERROR -> R.color.error_color
            else -> R.color.text_color_2
        }


        val snackBar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction("Ok") {
            }
            .setActionTextColor(ContextCompat.getColor(this, R.color.grey_primary))
            .setTextColor(ContextCompat.getColor(this, snackBarContentColor))
            .setIcon(
                drawable = drawable!!,
                colorTint = ContextCompat.getColor(this, snackBarContentColor),
                iconPadding = resources.getDimensionPixelOffset(R.dimen.small_margin)
            )
            .setCustomBackground(getDrawable(R.drawable.snackbar_normal_custom_bg)!!)

        snackBar.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        addressBookAdapter.removeAddressDefaultIdObserver()
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