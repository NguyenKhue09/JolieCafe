package com.nt118.joliecafe.ui.activities.checkout

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.recyclerview.widget.RecyclerView
import com.nt118.joliecafe.R

class VoucherAdapter : RecyclerView.Adapter<VoucherAdapter.VoucherViewHolder>() {

    private var selectedPosition = -1

    class VoucherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val rbVoucher: RadioButton = itemView.findViewById<View>(R.id.rb_voucher)!! as RadioButton
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VoucherViewHolder {
        return VoucherViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_voucher,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: VoucherViewHolder, position: Int) {
        holder.rbVoucher.isChecked = selectedPosition == holder.absoluteAdapterPosition
        holder.rbVoucher.setOnCheckedChangeListener { _, b ->
            if (b) {
                val prevSelectedPosition = selectedPosition
                selectedPosition = holder.absoluteAdapterPosition
                notifyItemChanged(prevSelectedPosition)
            }
        }
    }

    override fun getItemCount() = 2

}