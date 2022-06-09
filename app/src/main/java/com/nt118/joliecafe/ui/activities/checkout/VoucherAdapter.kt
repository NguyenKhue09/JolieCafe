package com.nt118.joliecafe.ui.activities.checkout

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.recyclerview.widget.RecyclerView
import com.nt118.joliecafe.R
import com.nt118.joliecafe.databinding.ItemVoucherBinding
import com.nt118.joliecafe.models.Voucher
import com.nt118.joliecafe.util.NumberUtil

class VoucherAdapter(
    private val dataset: List<Voucher>,
    private val context: Context,
) : RecyclerView.Adapter<VoucherAdapter.VoucherViewHolder>() {

    private var selectedPosition = -1

    class VoucherViewHolder(val binding: ItemVoucherBinding) : RecyclerView.ViewHolder(binding.root) {
        val rbVoucher: RadioButton = binding.rbVoucher
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VoucherViewHolder {
        return VoucherViewHolder(
            ItemVoucherBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: VoucherViewHolder, position: Int) {
        holder.rbVoucher.isChecked = selectedPosition == holder.absoluteAdapterPosition
        holder.rbVoucher.setOnClickListener { it as RadioButton
            if (it.isChecked && selectedPosition != holder.absoluteAdapterPosition) {
                val prevSelectedPosition = selectedPosition
                selectedPosition = holder.absoluteAdapterPosition
                notifyItemChanged(prevSelectedPosition)
            } else if (it.isChecked && selectedPosition == holder.absoluteAdapterPosition) clearSelection()
        }
        holder.binding.tvVoucherCode.text = dataset[position].code
        holder.binding.tvVoucherDescription.text = context.getString(R.string.voucher_description, NumberUtil.addSeparator(dataset[position].condition.toDouble()))
    }

    override fun getItemCount() = dataset.size

    @SuppressLint("NotifyDataSetChanged")
    fun clearSelection() {
        val prevSelectedPosition = selectedPosition
        selectedPosition = -1
        notifyItemChanged(prevSelectedPosition)
    }

    fun selectVoucher(voucher: Voucher) {
        selectedPosition = dataset.withIndex().first { it.value.code == voucher.code }.index
        notifyItemChanged(selectedPosition)

    }

    fun getSelected(): Voucher? {
        return if (selectedPosition == -1) null else dataset[selectedPosition]
    }

}