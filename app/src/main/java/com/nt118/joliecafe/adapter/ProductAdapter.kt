package com.nt118.joliecafe.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.nt118.joliecafe.R
import com.nt118.joliecafe.databinding.ItemRvProductBinding
import com.nt118.joliecafe.models.Product
import com.nt118.joliecafe.ui.activities.detail.DetailActivity
import com.nt118.joliecafe.ui.activities.products.ProductsActivity
import com.nt118.joliecafe.util.ApiResult
import com.nt118.joliecafe.viewmodels.products.ProductsViewModel
import java.text.NumberFormat
import java.util.*

class ProductAdapter(
        private val productsActivity: ProductsActivity,
        diffCallBack: DiffUtil.ItemCallback<Product>
    ) : PagingDataAdapter<Product, ProductAdapter.ViewHolder>(diffCallBack) {
    class ViewHolder(var binding: ItemRvProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemRvProductBinding.inflate(layoutInflater)
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

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val product = getItem(position)
        product?.let {
            holder.binding.itemImgProduct.load(product.thumbnail) {
                crossfade(600)
                error(R.drawable.placeholder_image)
            }

            productsActivity.productsViewModel.getFavoriteProductResponse.observe(productsActivity) { response ->
                when (response) {
                    is ApiResult.Loading -> {
                        holder.binding.imgFavorite.visibility = View.VISIBLE
                    }
                    is ApiResult.Success -> {
                        val data = response.data!!
                        if (data.isEmpty()) {
                            holder.binding.imgFavorite.visibility = View.VISIBLE
                        } else {
                            data.find { it.productId == product.id }?.let {
                                holder.binding.imgFavoriteChoose.visibility = View.VISIBLE
                                holder.binding.imgFavorite.visibility = View.GONE
                            }
                        }
                    }
                    is ApiResult.Error -> {
                        holder.binding.imgFavorite.visibility = View.VISIBLE
                        Log.d("CartFragment", "get cart error: ${response.message}")
                    }
                    else -> {}
                }
            }

            holder.binding.tvNameProduct.text = product.name

            holder.binding.itemPriceProduct.text = productsActivity.getString(
                R.string.product_price,
                NumberFormat.getNumberInstance(
                    Locale.US
                ).format(product.originPrice)
            )

            holder.binding.tvCategoriesProduct.text = product.type

            holder.binding.itemCard.setOnClickListener {
                val intent = Intent(productsActivity, DetailActivity::class.java)
                intent.putExtra("productId", product.id)
                productsActivity.startActivity(intent)
            }

            holder.binding.imgFavorite.setOnClickListener {
                productsActivity.addNewFavorite(productId = product.id)
                holder.binding.imgFavoriteChoose.visibility = View.VISIBLE
                holder.binding.imgFavorite.visibility = View.GONE
            }

            holder.binding.imgFavoriteChoose.setOnClickListener {
                productsActivity.removeFavoriteProduct(productId = product.id)
                holder.binding.imgFavoriteChoose.visibility = View.GONE
                holder.binding.imgFavorite.visibility = View.VISIBLE
            }
        }
    }


}