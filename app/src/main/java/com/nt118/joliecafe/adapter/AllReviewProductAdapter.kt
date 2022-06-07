package com.nt118.joliecafe.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.nt118.joliecafe.R
import com.nt118.joliecafe.databinding.ItemRvReviewBinding
import com.nt118.joliecafe.models.Comment


class AllReviewProductAdapter(private val item : List<Comment>) : RecyclerView.Adapter<AllReviewProductAdapter.MyViewHolder>() {
    class MyViewHolder(var binding: ItemRvReviewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): MyViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemRvReviewBinding.inflate(layoutInflater, parent, false)
                return MyViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder.from(parent)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = item[position]

        holder.binding.imgAvatar.load(item.userId.thumbnail) {
            crossfade(600)
            error(R.drawable.placeholder_image)
        }
        holder.binding.tvName.text = item.userId.fullName
        holder.binding.tvContentRv.text = item.content
        holder.binding.tvRate.text = item.rating.toString()
    }

    override fun getItemCount(): Int {
        return item.size
    }
}