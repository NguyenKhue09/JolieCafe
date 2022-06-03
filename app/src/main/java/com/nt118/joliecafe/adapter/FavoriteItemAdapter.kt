package com.nt118.joliecafe.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.nt118.joliecafe.R
import com.nt118.joliecafe.databinding.ItemRowLayoutBinding
import com.nt118.joliecafe.models.FavoriteProduct
import com.nt118.joliecafe.ui.activities.detail.DetailActivity
import com.nt118.joliecafe.ui.fragments.favorite.FavoriteFragment

class FavoriteItemAdapter(
    private val favoriteFragment: FavoriteFragment,
    diffUtil: DiffUtil.ItemCallback<FavoriteProduct>
) : PagingDataAdapter<FavoriteProduct, FavoriteItemAdapter.MyViewHolder>(diffCallback = diffUtil) {

    class MyViewHolder(var binding: ItemRowLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): MyViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemRowLayoutBinding.inflate(layoutInflater)
                return MyViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        return MyViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val favoriteProduct = getItem(position)

        favoriteProduct?.let {
            val product = it.product
            holder.binding.itemCard.setOnClickListener {
                val intent = Intent(favoriteFragment.context, DetailActivity::class.java)
                favoriteFragment.context?.startActivity(intent)
            }

            holder.binding.itemImg.load(product.thumbnail) {
                crossfade(600)
                error(R.drawable.placeholder_image)
            }

            holder.binding.itemName.text = product.name


            favoriteFragment.context?.let { context ->
                holder.binding.itemPrice.text = context.getString(R.string.product_price, product.originPrice.toString())
            }

            holder.binding.btnFavorite.setOnClickListener {
                favoriteFragment.removeUserFavoriteProduct(favoriteProductId = favoriteProduct.id)
            }
        }
    }

}