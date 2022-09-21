package com.nt118.joliecafe.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.nt118.joliecafe.R
import com.nt118.joliecafe.databinding.AddressBookItemLayoutBinding
import com.nt118.joliecafe.models.Address
import com.nt118.joliecafe.ui.activities.address_book.AddressBookActivity


class AddressBookAdapter(
    differCallback: DiffUtil.ItemCallback<Address>,
    private val addressBookActivity: AddressBookActivity
) : PagingDataAdapter<Address, AddressBookAdapter.ViewHolder>(differCallback) {

    private lateinit var updateStatusObserver: Observer<Boolean>
    private lateinit var defaultAddressIdObserver: Observer<String>
    private var error = false

    class ViewHolder(var binding: AddressBookItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = AddressBookItemLayoutBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val address = getItem(position)
        address?.let { addressItem ->
            println("address item id: ${addressItem.id}")
            defaultAddressIdObserver = Observer<String> { id ->
                holder.binding.isDefaultAddress.isChecked = (id == addressItem.id)
                holder.binding.isDefaultAddress.isClickable = id != addressItem.id
                if(id == addressItem.id) {
                    holder.binding.cardAddAddress.strokeWidth = 4
                } else {
                    holder.binding.cardAddAddress.strokeWidth = 0
                }
            }

            addressBookActivity.readDefaultAddressId.observeForever(defaultAddressIdObserver)

            holder.binding.isDefaultAddress.setOnCheckedChangeListener { _, isChecked ->
                //addressBookActivity.updateAddressStatus.observeForever(updateStatusObserver)
                if (isChecked) {
                    createObserverEdit(holder = holder)
                    println("address item id default: ${addressItem.id}")
                    addressBookActivity.updateUserInfos(newData = mapOf(
                        "defaultAddress" to addressItem.id
                    ))
                }
            }

            setAddressData(holder, addressItem)

            holder.binding.btnEdit.setOnClickListener {
                setActiveElementForUpdateAddress(
                    holder = holder,
                    isEnable = true,
                    isVisible = View.VISIBLE
                )
            }
            holder.binding.btnCancel.setOnClickListener {
                setAddressData(holder, addressItem)
                setActiveElementForUpdateAddress(
                    holder = holder,
                    isEnable = false,
                    isVisible = View.GONE
                )
            }
            holder.binding.btnSave.setOnClickListener {
                createObserverEdit(holder = holder)
                validateAddressNewData(holder = holder, addressId = address.id)
            }

            holder.binding.btnDelete.setOnClickListener {
                addressBookActivity.deleteAddress(addressItem.id)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    private fun setActiveElementForUpdateAddress(
        holder: ViewHolder,
        isVisible: Int,
        isEnable: Boolean
    ) {
        holder.binding.tvNameTitle.visibility = isVisible
        holder.binding.tvNamePhone.visibility = isVisible
        holder.binding.tvAddress.visibility = isVisible
        holder.binding.btnSave.visibility = isVisible
        holder.binding.btnCancel.visibility = isVisible
        holder.binding.isDefaultAddress.visibility = isVisible
        holder.binding.tvDefaultAddress.visibility = isVisible

        holder.binding.etNameLayout.isEnabled = isEnable
        holder.binding.etPhoneLayout.isEnabled = isEnable
        holder.binding.etAddressLayout.isEnabled = isEnable

        removeObserverEdit(isEnable = isEnable)
    }

    private fun setAddressData(holder: ViewHolder, addressItem: Address) {
        holder.binding.etName.setText(addressItem.userName)
        holder.binding.etPhone.setText(addressItem.phone)
        holder.binding.etAddress.setText(addressItem.address)
    }

    private fun validateAddressNewData(holder: ViewHolder, addressId: String) {
        val name = holder.binding.etName.text.toString()
        val phone = holder.binding.etPhone.text.toString()
        val address = holder.binding.etAddress.text.toString()

        val nameErr = addressBookActivity.validateUserName(name = name)
        val phoneErr = addressBookActivity.validatePhone(phone = phone)
        val addressErr = addressBookActivity.validateAddress(address = address)

        error = listOf(nameErr, phoneErr, addressErr).any { it != null }

        if (!error) {
            val newAddressData = mapOf(
                "userName" to name,
                "phone" to phone,
                "address" to address,
                "addressId" to addressId,
            )
            addressBookActivity.updateAddress(newAddressData = newAddressData)
            clearError(holder)
        } else {
            if (nameErr != null) {
                holder.binding.etNameLayout.error = nameErr
                holder.binding.etName.requestFocus()
            } else {
                holder.binding.etNameLayout.error = null
            }
            if (phoneErr != null) {
                holder.binding.etPhoneLayout.error = phoneErr
                holder.binding.etPhone.requestFocus()
            } else {
                holder.binding.etPhoneLayout.error = null
            }
            if (addressErr != null) {
                holder.binding.etAddressLayout.error = addressErr
                holder.binding.etAddress.requestFocus()
            } else {
                holder.binding.etAddressLayout.error = null
            }
        }
    }

    private fun clearError(holder: ViewHolder) {
        holder.binding.etNameLayout.error = null
        holder.binding.etPhoneLayout.error = null
        holder.binding.etAddressLayout.error = null
    }

    private  fun createObserverEdit(holder: ViewHolder){
        updateStatusObserver = Observer<Boolean> {
            if(it) {
                setActiveElementForUpdateAddress(
                    holder = holder,
                    isEnable = false,
                    isVisible = View.GONE
                )
            }
        }
        addressBookActivity.updateAddressStatus.observeForever(updateStatusObserver)
    }

    private fun removeObserverEdit(isEnable: Boolean) {
        if(!isEnable && ::updateStatusObserver.isInitialized) addressBookActivity.updateAddressStatus.removeObserver(updateStatusObserver)
    }

    fun removeAddressDefaultIdObserver() {
        if(::defaultAddressIdObserver.isInitialized) addressBookActivity.readDefaultAddressId.removeObserver(defaultAddressIdObserver)
    }
}