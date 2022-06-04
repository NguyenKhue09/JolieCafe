package com.nt118.joliecafe.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nt118.joliecafe.databinding.ItemCartBinding
import com.nt118.joliecafe.models.CartItem
import androidx.recyclerview.widget.DiffUtil
import coil.load
import com.nt118.joliecafe.R
import com.nt118.joliecafe.viewmodels.cart.CartViewModel
import java.text.NumberFormat
import java.util.*

class CartAdapter(
    private val mActivity: Activity,
    val viewModel: CartViewModel,
    private val dataset: MutableList<CartItem>,
) : RecyclerView.Adapter<CartAdapter.ViewHolder>() {

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

        var cartItem: CartItem? = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.cartItem = dataset[position]
        holder.cartItem?.let { cartItem ->
            holder.binding.ivThumbnail.load(cartItem.productDetail.thumbnail) {
                crossfade(600)
                error(R.drawable.placeholder_image)
                placeholder(R.drawable.placeholder_image)
            }
            holder.binding.tvProductName.text = cartItem.productDetail.name
            holder.binding.tvProductDescription.text = cartItem.productDetail.description
            holder.binding.tvAmount.text = cartItem.quantity.toString()
            holder.binding.tvPrice.text = mActivity.getString(
                R.string.product_price,
                NumberFormat.getNumberInstance(Locale.US).format(cartItem.price)
            )
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

            holder.binding.btnDecreaseQuantity.setOnClickListener {
                handleQuantityChange(cartItem.quantity - 1, cartItem)
                holder.binding.tvAmount.text = cartItem.quantity.toString()
            }

            holder.binding.btnIncreaseQuantity.setOnClickListener {
                handleQuantityChange(cartItem.quantity + 1, cartItem)
                holder.binding.tvAmount.text = cartItem.quantity.toString()
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

    private fun handleQuantityChange(quantity: Int, cartItem: CartItem) {
        if (quantity < 1) {
            viewModel.deleteCartItem(cartItem.productId, token = viewModel.userToken)
        } else {
            cartItem.quantity = quantity
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun fetchData(data: List<CartItem>?) {
        dataset.clear()
        dataset.addAll(data?.toMutableList() ?: mutableListOf())
        notifyDataSetChanged()
    }

    override fun getItemCount() = dataset.size

}