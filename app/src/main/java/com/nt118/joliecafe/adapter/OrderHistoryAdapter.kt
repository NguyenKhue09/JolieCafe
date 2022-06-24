package com.nt118.joliecafe.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.nt118.joliecafe.R
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
    private val onBillReviewClicked: (String, List<String>) -> Unit,
    diffUtil: DiffUtil.ItemCallback<OrderHistory>
) : PagingDataAdapter<OrderHistory, OrderHistoryAdapter.MyViewHolder>(diffCallback = diffUtil) {

    private lateinit var orderListIdObserver: Observer<MutableList<String>>

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
        var isExpanded: Boolean
        val orderHistory = getItem(position)

        val orderItemInBillAdapter = OrderItemInBillAdapter(orderHistoryActivity.baseContext)
        val orderItemInBillRecyclerView = holder.binding.rvOrderItem
        orderItemInBillRecyclerView.layoutManager = LinearLayoutManager(orderHistoryActivity, LinearLayoutManager.VERTICAL, false)
        orderItemInBillRecyclerView.adapter = orderItemInBillAdapter

        val orderItem = holder.binding.cardOrderHistory
        val cardContent = holder.binding.orderItemBody
        val btnCollapse = holder.binding.btnCollapse

        orderHistory?.let { bill ->

            orderListIdObserver = Observer<MutableList<String>> { listId ->
                isExpanded =  listId.contains(bill.id)

                if (isExpanded) {
                    cardContent.visibility = View.VISIBLE
                    holder.binding.tvOrderNumberItem.visibility = View.GONE
                    holder.binding.tvOrderTempTotalCost.visibility = View.GONE
                    btnCollapse.setImageResource(R.drawable.ic_arrow_up)
                } else {
                    holder.binding.tvOrderNumberItem.visibility = View.VISIBLE
                    holder.binding.tvOrderTempTotalCost.visibility = View.VISIBLE
                    cardContent.visibility = View.GONE
                    btnCollapse.setImageResource(R.drawable.ic_arrow_down)
                }
                TransitionManager.beginDelayedTransition(
                    orderItem,
                    AutoTransition()
                )
            }

            orderHistoryActivity.orderHistoryClickedList.observeForever(orderListIdObserver)


            btnCollapse.setOnClickListener {
                orderHistoryActivity.addNewOrderToClickList(bill.id)
            }

            orderItemInBillAdapter.setData(newData = bill.products)

            val totalItem = bill.products.sumOf { it.quantity }

            holder.binding.tvOrderNumberItem.text = orderHistoryActivity.getString(R.string.product_quantity, totalItem.toString())
            holder.binding.tvOrderDate.text = bill.orderDate.toDate()?.formatTo(dateFormat = LOCAL_TIME_FORMAT)
            holder.binding.tvUserName.text = bill.address.userName
            holder.binding.tvUserPhone.text = bill.address.phone
            holder.binding.tvUserAddress.text = bill.address.address
            holder.binding.tvSubtotalCost.text = orderHistoryActivity.getString(
                R.string.product_price,
                NumberFormat.getNumberInstance(Locale.US).format(bill.totalCost - bill.shippingFee)
            )
            holder.binding.tvShippingFee.text = orderHistoryActivity.getString(
                    R.string.product_price,
            NumberFormat.getNumberInstance(Locale.US).format(bill.shippingFee)
            )
            holder.binding.tvTotalCost.text = orderHistoryActivity.getString(
                R.string.product_price,
                NumberFormat.getNumberInstance(Locale.US).format(bill.totalCost)
            )
            holder.binding.tvOrderTempTotalCost.text = orderHistoryActivity.getString(
                R.string.product_price,
                NumberFormat.getNumberInstance(Locale.US).format(bill.totalCost)
            )
            holder.binding.tvOrderStatus.text = orderHistoryActivity.getString(
                R.string.status,
                bill.status
            )

            holder.binding.tvDiscountCost.text = orderHistoryActivity.getString(
                R.string.product_price,
                NumberFormat.getNumberInstance(Locale.US).format(if(bill.discountCost == 0.0) 0 else -bill.discountCost)
            )

            holder.binding.tvOrderId.text = orderHistoryActivity.getString(R.string.order_id, bill.orderId)

            holder.binding.tvPaidStatus.text = if(bill.paid) "You paid this bill" else "You haven't pay yet!"
            holder.binding.btnReview.visibility = if (bill.paid && !bill.isRated) View.VISIBLE else View.GONE
            holder.binding.btnReview.setOnClickListener {
                onBillReviewClicked(bill.id, bill.products.map { it.product.id })
            }
        }
    }

    fun removeOrderListIdObserver() {
        if(::orderListIdObserver.isInitialized) orderHistoryActivity.orderHistoryClickedList.removeObserver(orderListIdObserver)
    }
}


//                rotateAnimation = if(isExpanded) {
//                    println("anim1")
//                    RotateAnimation(0f, -180f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f).apply {
//                        duration = 300
//                        fillAfter = true
//                    }
//                } else {
//                    println("anim2")
//                    RotateAnimation(-180f, 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f).apply {
//                        duration = 300
//                        fillAfter = true
//                    }
//                }
//btnCollapse.startAnimation(rotateAnimation)