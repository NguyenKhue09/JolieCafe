package com.nt118.joliecafe.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.nt118.joliecafe.R
import com.nt118.joliecafe.databinding.ItemRvBestsallerBinding
import com.nt118.joliecafe.databinding.ItemRvProductBinding
import com.nt118.joliecafe.models.Product
import com.nt118.joliecafe.ui.activities.detail.DetailActivity

class ProductAdapter(
        private val activity: Activity,
        diffCallBack: DiffUtil.ItemCallback<Product>
    ) : PagingDataAdapter<Product, ProductAdapter.ViewHolder>(diffCallBack) {
    class ViewHolder(var binding: ItemRvProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): ProductAdapter.ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemRvProductBinding.inflate(layoutInflater)
                return ProductAdapter.ViewHolder(binding)
            }
        }
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProductAdapter.ViewHolder {
        return ProductAdapter.ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ProductAdapter.ViewHolder, position: Int) {


        holder.binding.itemCard.setOnClickListener {
            val intent = Intent(activity, DetailActivity::class.java)
            activity.startActivity(intent)
        }

        val product = getItem(position)
        product?.let {
            holder.binding.itemImgProduct.load(product.thumbnail) {
                crossfade(600)
                error(R.drawable.placeholder_image)
            }

            holder.binding.tvNameProduct.text = product.name
            holder.binding.itemPriceProduct.text = product.originPrice.toString()
            holder.binding.tvCategoriesProduct.text = product.type
        }
    }


}