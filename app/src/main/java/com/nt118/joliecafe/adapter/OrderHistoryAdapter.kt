package com.nt118.joliecafe.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.nt118.joliecafe.databinding.OrderHistoryItemLayoutBinding

class OrderHistoryAdapter(
    val context: Context
) : RecyclerView.Adapter<OrderHistoryAdapter.MyViewHolder>() {

    private var mPosition = -1

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

        val orderItem = holder.binding.cardOrderHistory
        val cardContent = holder.binding.orderItemBody
        val btnCollapse = holder.binding.btnCollapse
        val isViewExpanded = position == mPosition

        if (isViewExpanded) {
            holder.binding.tvOrderNumberItem.visibility = View.GONE
            holder.binding.tvOrderTempTotalCost.visibility = View.GONE
            TransitionManager.beginDelayedTransition(
                orderItem,
                AutoTransition()
            )
            cardContent.visibility = View.VISIBLE
        } else {
            TransitionManager.beginDelayedTransition(
                orderItem,
                AutoTransition()
            )
            cardContent.visibility = View.GONE
            holder.binding.tvOrderNumberItem.visibility = View.VISIBLE
            holder.binding.tvOrderTempTotalCost.visibility = View.VISIBLE
        }

        val rotateAnimation: RotateAnimation = if(isViewExpanded) {
            println("anim1")
            RotateAnimation(0f, -180f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f).apply {
                duration = 300
                fillAfter = true
            }
        } else {
            println("anim2")
            RotateAnimation(-180f, 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f).apply {
                duration = 300
                fillAfter = true
            }
        }

        if (mPosition == position) btnCollapse.startAnimation(rotateAnimation)

        btnCollapse.setOnClickListener {
            mPosition = if(isViewExpanded) -1 else position
            notifyItemChanged(position)
        }
    }

    override fun getItemCount(): Int {
        return 15
    }
}