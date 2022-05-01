package com.nt118.joliecafe.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.nt118.joliecafe.databinding.AddressBookItemLayoutBinding
import com.nt118.joliecafe.models.Address


class AddressBookAdapter(
    differCallback: DiffUtil.ItemCallback<Address>
): PagingDataAdapter<Address, AddressBookAdapter.ViewHolder>(differCallback) {

    class ViewHolder(var binding: AddressBookItemLayoutBinding): RecyclerView.ViewHolder(binding.root) {
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
        address?.let {
            holder.binding.etName.setText(it.userName)
            holder.binding.etPhone.setText(it.phone)
            holder.binding.etAddress.setText(it.address)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }
}