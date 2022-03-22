package com.nt118.joliecafe.ui.activities.checkout

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nt118.joliecafe.R

class CheckoutAdapter : RecyclerView.Adapter<CheckoutAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_checkout, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        return
    }

    override fun getItemCount() = 2
}