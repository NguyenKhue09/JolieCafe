package com.nt118.joliecafe.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.nt118.joliecafe.R
import com.nt118.joliecafe.databinding.ItemRvBestsallerBinding
import com.nt118.joliecafe.models.Product
import com.nt118.joliecafe.ui.activities.detail.DetailActivity
import com.nt118.joliecafe.ui.fragments.catagories.CatagoriesBottomSheetFragment
import com.nt118.joliecafe.util.ItemDiffUtil


class BestSellerAdapter(
    private val activity: Activity,
    diffCallBack: DiffUtil.ItemCallback<Product>
) : PagingDataAdapter<Product, BestSellerAdapter.ViewHolder>(diffCallBack) {

    class ViewHolder(var binding: ItemRvBestsallerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemRvBestsallerBinding.inflate(layoutInflater, parent, false)
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
        holder.binding.btnAddCard.setOnClickListener {
            val ft = (activity as AppCompatActivity).supportFragmentManager.beginTransaction()
            val bottomSheet = CatagoriesBottomSheetFragment()
            bottomSheet.show(ft, "TAG")
        }

        holder.binding.itemCard.setOnClickListener {
            val intent = Intent(activity, DetailActivity::class.java)
            activity.startActivity(intent)
        }

        val product = getItem(position)
        product?.let {
            holder.binding.itemImg.load(product.thumbnail) {
                crossfade(600)
                error(R.drawable.placeholder_image)
            }

            holder.binding.itemName.text = product.name
            holder.binding.itemCategories.text = product.type
            holder.binding.itemPrice.text = product.originPrice.toString()
        }
    }

}