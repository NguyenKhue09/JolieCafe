package com.nt118.joliecafe.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.nt118.joliecafe.databinding.OrderHistoryItemLayoutBinding
import com.nt118.joliecafe.models.OrderHistory
import com.nt118.joliecafe.ui.activities.order_history.OrderHistoryActivity
import com.nt118.joliecafe.util.Constants.Companion.LOCAL_TIME_FORMAT
import com.nt118.joliecafe.util.extenstions.formatTo
import com.nt118.joliecafe.util.extenstions.toDate
import java.text.NumberFormat
import java.util.*

class OrderHistoryAdapter(
    val orderHistoryActivity: OrderHistoryActivity,
    diffUtil: DiffUtil.ItemCallback<OrderHistory>
) : PagingDataAdapter<OrderHistory, OrderHistoryAdapter.MyViewHolder>(diffCallback = diffUtil) {

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

        val orderHistory = getItem(position)

        val orderItemInBillAdapter = OrderItemInBillAdapter(orderHistoryActivity.baseContext)
        val orderItemInBillRecyclerView = holder.binding.rvOrderItem
        orderItemInBillRecyclerView.layoutManager = LinearLayoutManager(orderHistoryActivity, LinearLayoutManager.VERTICAL, false)
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

        orderHistory?.let { bill ->

            orderItemInBillAdapter.setData(newData = bill.products)

            holder.binding.tvOrderDate.text = bill.orderDate.toDate()?.formatTo(dateFormat = LOCAL_TIME_FORMAT)
            holder.binding.tvUserName.text = bill.address.userName
            holder.binding.tvUserPhone.text = bill.address.phone
            holder.binding.tvUserAddress.text = bill.address.address
            holder.binding.tvSubtotalCost.text = orderHistoryActivity.getString(
                com.nt118.joliecafe.R.string.product_price,
                NumberFormat.getNumberInstance(Locale.US).format(bill.totalCost - bill.shippingFee)
            )
            holder.binding.tvShippingFee.text = orderHistoryActivity.getString(
                    com.nt118.joliecafe.R.string.product_price,
            NumberFormat.getNumberInstance(Locale.US).format(bill.shippingFee)
            )
            holder.binding.tvTotalCost.text = orderHistoryActivity.getString(
                com.nt118.joliecafe.R.string.product_price,
                NumberFormat.getNumberInstance(Locale.US).format(bill.totalCost)
            )
        }
    }
}