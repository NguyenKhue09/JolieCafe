package com.nt118.joliecafe.ui.fragments.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.nt118.joliecafe.R

class DotIndicatorPager2Adapter : RecyclerView.Adapter<ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return object : ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_slide_home, parent, false)) {}
    }

    override fun getItemCount() = 5

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Empty
    }
}