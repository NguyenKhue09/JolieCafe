package com.nt118.joliecafe.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.nt118.joliecafe.R
import com.nt118.joliecafe.databinding.ItemCheckoutBinding
import com.nt118.joliecafe.models.CartItem
import com.nt118.joliecafe.util.NumberUtil

class CheckoutAdapter(
    private val dataset: List<CartItem>,
    private val mContext: Context
) : RecyclerView.Adapter<CheckoutAdapter.ViewHolder>() {
    class ViewHolder(var binding: ItemCheckoutBinding) : RecyclerView.ViewHolder(binding.root) {
        val ivThumbnail = binding.ivThumbnail
        val tvProductName = binding.tvProductName
        val tvProductDescription = binding.tvProductDescription
        val tvQuantity = binding.tvQuantity
        val tvPrice = binding.tvPrice
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemCheckoutBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataset[position]
        holder.ivThumbnail.load(item.productId.thumbnail) {
            crossfade(600)
            error(R.drawable.placeholder_image)
        }
        holder.tvProductName.text = item.productId.name
        holder.tvProductDescription.text = item.productId.description
        holder.tvQuantity.text = mContext.getString(R.string.quantity_checkout, item.quantity)
        holder.tvPrice.text = mContext.getString(R.string.product_price, NumberUtil.addSeparator(item.price))
    }

    override fun getItemCount() = dataset.size

    fun getTotalPrice() = dataset.sumOf { it.price }
}