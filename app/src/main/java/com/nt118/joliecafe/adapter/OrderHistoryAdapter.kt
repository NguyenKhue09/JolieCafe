package com.nt118.joliecafe.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.nt118.joliecafe.databinding.OrderHistoryItemLayoutBinding

class OrderHistoryAdapter(
    val context: Context
) : RecyclerView.Adapter<OrderHistoryAdapter.MyViewHolder>() {

    private var isViewExpanded = false

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



        val orderItem = holder.binding.cardAddAddress
        val cardContent = holder.binding.orderItemBody

        holder.binding.btnCollapse.setOnClickListener {
            if (isViewExpanded) {
                val rotateAnimation = RotateAnimation(0f, -180f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
                rotateAnimation.duration = 200
                rotateAnimation.fillAfter = true
                it.startAnimation(rotateAnimation)
                TransitionManager.beginDelayedTransition(
                    orderItem,
                    AutoTransition()
                )
                cardContent.visibility = View.GONE

            } else {
                val rotateAnimation = RotateAnimation(-180f, 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
                rotateAnimation.duration = 200
                rotateAnimation.fillAfter = true
                it.startAnimation(rotateAnimation)
                TransitionManager.beginDelayedTransition(
                    orderItem,
                    AutoTransition()
                )
                cardContent.visibility = View.VISIBLE
            }
            isViewExpanded = !isViewExpanded
        }
    }

    override fun getItemCount(): Int {
        return 15
    }
}