package com.nt118.joliecafe.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.nt118.joliecafe.R
import com.nt118.joliecafe.databinding.OrderItemInBillLayoutBinding
import com.nt118.joliecafe.models.OrderHistory
import com.nt118.joliecafe.models.OrderHistoryProduct
import com.nt118.joliecafe.util.ItemDiffUtil
import java.text.NumberFormat
import java.util.*

class OrderItemInBillAdapter(
    val context: Context
) : RecyclerView.Adapter<OrderItemInBillAdapter.MyViewHolder>() {

    var products = emptyList<OrderHistoryProduct>()

    class MyViewHolder(var binding: OrderItemInBillLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): MyViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = OrderItemInBillLayoutBinding.inflate(layoutInflater, parent, false)
                return MyViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val product = products[position]
        holder.binding.itemImg.load(
            uri = product.product.thumbnail
        ) {
            crossfade(600)
            error(R.drawable.placeholder_image)
            placeholder(R.drawable.placeholder_image)
        }
        holder.binding.oderItemPrice.text = context.getString(
            R.string.product_price,
            NumberFormat.getNumberInstance(Locale.US).format(product.price)
        )
        holder.binding.oderItemQuantityNumber.text = product.quantity.toString()
        holder.binding.tvOrderItemName.text = product.product.name
        holder.binding.oderItemSizeText.text = product.size
    }

    override fun getItemCount(): Int {
        return products.size
    }

    fun setData(newData: List<OrderHistoryProduct>) {
        val recipesDiffUtil = ItemDiffUtil(products, newData)
        val diffUtilResult = DiffUtil.calculateDiff(recipesDiffUtil)
        products = newData
        diffUtilResult.dispatchUpdatesTo(this)
    }
}