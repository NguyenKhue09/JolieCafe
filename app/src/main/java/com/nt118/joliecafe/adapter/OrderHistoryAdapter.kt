package com.nt118.joliecafe.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.RotateAnimation
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

        var isCollapse = true

        val cartContent = holder.binding.orderItemBody

        holder.binding.btnCollapse.setOnClickListener {
            if (isCollapse) {
                val rotateAnimation = RotateAnimation(0f, -180f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
                rotateAnimation.duration = 300
                rotateAnimation.fillAfter = true
                it.startAnimation(rotateAnimation)
                cartContent.animate().translationY(-cartContent.height.toFloat())
                    .alpha(0.0f).duration = 300
                //cartContent.visibility = View.GONE

            } else {
                val rotateAnimation = RotateAnimation(-180f, 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
                rotateAnimation.duration = 300
                rotateAnimation.fillAfter = true
                it.startAnimation(rotateAnimation)
//                cartContent.animate().translationY(cartContent.height.toFloat())
//                    .alpha(1f).duration = 300
                cartContent.visibility = View.VISIBLE
            }
            isCollapse = !isCollapse
        }
    }

    override fun getItemCount(): Int {
        return 3
    }
}