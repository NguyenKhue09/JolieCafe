package com.nt118.joliecafe.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.nt118.joliecafe.databinding.ItemRvBestsallerBinding
import com.nt118.joliecafe.ui.activities.detail.DetailActivity
import com.nt118.joliecafe.ui.fragments.catagories.CatagoriesBottomSheetFragment


class BestSallerAdapter(private val item : ArrayList<String>, private val activity: Activity,private val context: Context) : RecyclerView.Adapter<BestSallerAdapter.ViewHolder>() {
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
            val intent = Intent(context, DetailActivity::class.java)
            activity.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return item.size
    }
}