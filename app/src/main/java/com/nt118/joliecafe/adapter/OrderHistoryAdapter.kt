package com.nt118.joliecafe.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nt118.joliecafe.databinding.OrderHistoryItemLayoutBinding

class OrderHistoryAdapter(
    val context: Context
) : RecyclerView.Adapter<OrderHistoryAdapter.MyViewHolder>() {

    class MyViewHolder(var binding: OrderHistoryItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): MyViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = OrderHistoryItemLayoutBinding.inflate(layoutInflater, parent, false)
                return MyViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val orderItemInBillAdapter = OrderItemInBillAdapter()
        val orderItemInBillRecyclerView = holder.binding.rvOrderItem
        orderItemInBillRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        orderItemInBillRecyclerView.adapter = orderItemInBillAdapter
    }

    override fun getItemCount(): Int {
        return 3
    }
}