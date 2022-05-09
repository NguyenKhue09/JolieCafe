package com.nt118.joliecafe.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nt118.joliecafe.databinding.ItemCartBinding
import com.nt118.joliecafe.models.CartItem
import androidx.recyclerview.widget.DiffUtil
import coil.load
import com.nt118.joliecafe.R

class CartAdapter(
    diffCallback: DiffUtil.ItemCallback<CartItem>,
) : PagingDataAdapter<CartItem, CartAdapter.ViewHolder>(diffCallback) {

    private var isSelectAll = false
    private var isDeselectAll = false
    var onSelectAllAction: (() -> Unit)? = null
    var onDeselectAllAction: (() -> Unit)? = null
    private var numOfCheckedItem = 0

    class ViewHolder(var binding: ItemCartBinding) : RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemCartBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cartItem = getItem(position)
        cartItem?.let {
            holder.binding.ivThumbnail.load(cartItem.productDetail.thumbnail) {
                crossfade(600)
                error(R.drawable.placeholder_image)
            }
            holder.binding.tvProductName.text = cartItem.productDetail.name
            holder.binding.tvProductDescription.text = cartItem.productDetail.description
            holder.binding.tvAmount.text = cartItem.quantity.toString()
            if (isSelectAll) {
                holder.binding.cbItemSelect.isChecked = true
            } else if (isDeselectAll) {
                holder.binding.cbItemSelect.isChecked = false
            }
            holder.binding.cbItemSelect.setOnClickListener {
                isSelectAll = false
                isDeselectAll = false
                if ((it as CheckBox).isChecked) {
                    numOfCheckedItem++
                } else {
                    numOfCheckedItem--
                }

                when (numOfCheckedItem) {
                    itemCount -> {
                        isSelectAll = true
                        onSelectAllAction?.invoke()
                    }
                    0 -> {
                        isDeselectAll = true
                        onDeselectAllAction?.invoke()
                    }
                    else -> onDeselectAllAction?.invoke()
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun checkAllCheckbox() {
        isSelectAll = true
        isDeselectAll = false
        numOfCheckedItem = itemCount
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun uncheckAllCheckbox() {
        isDeselectAll = true
        isSelectAll = false
        numOfCheckedItem = 0
        notifyDataSetChanged()
    }

    fun resetCounter() {
        isSelectAll = false
        isDeselectAll = false
        numOfCheckedItem = 0
    }
}