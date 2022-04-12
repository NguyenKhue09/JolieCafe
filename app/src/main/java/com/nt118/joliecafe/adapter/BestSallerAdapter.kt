package com.nt118.joliecafe.adapter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.nt118.joliecafe.R
import com.nt118.joliecafe.databinding.ItemRvBestsallerBinding
import com.nt118.joliecafe.ui.fragments.catagories.CatagoriesBottomSheetFragment
import java.security.AccessController.getContext


class BestSallerAdapter(private val item : ArrayList<String>, private val activity: Activity) : RecyclerView.Adapter<BestSallerAdapter.ViewHolder>() {
    class ViewHolder(var binding: ItemRvBestsallerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemRvBestsallerBinding.inflate(layoutInflater)
                return ViewHolder(binding)
            }
        }
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.itemBestsaller.text = item[position]

        holder.binding.btnAddCard.setOnClickListener {
            val ft = (activity as AppCompatActivity).supportFragmentManager.beginTransaction()
            val bottomSheet = CatagoriesBottomSheetFragment()
            bottomSheet.show(ft, "TAG")
        }

        holder.binding.itemCard.setOnClickListener {
            Toast.makeText( activity, "Halo ${position}", Toast.LENGTH_SHORT).show()
            println("Halo ${position}")
        }
    }

    override fun getItemCount(): Int {
        return item.size
    }
}