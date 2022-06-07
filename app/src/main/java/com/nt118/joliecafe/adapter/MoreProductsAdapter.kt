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
import com.nt118.joliecafe.databinding.ItemCartSuggestionBinding
import com.nt118.joliecafe.models.Product
import com.nt118.joliecafe.ui.activities.detail.DetailActivity
import com.nt118.joliecafe.ui.fragments.catagories.CategoriesBottomSheetFragment
import java.text.NumberFormat
import java.util.*


class MoreProductsAdapter(
    private val activity: Activity,
    diffCallBack: DiffUtil.ItemCallback<Product>
) : PagingDataAdapter<Product, MoreProductsAdapter.MyViewHolder>(diffCallBack) {
    class MyViewHolder(var binding: ItemCartSuggestionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): MyViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemCartSuggestionBinding.inflate(layoutInflater, parent, false)
                return MyViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val product = getItem(position)
        product?.let {

            holder.binding.cartSuggestionContainer.setOnClickListener {
                val intent = Intent(activity, DetailActivity::class.java)
                intent.putExtra("productId", product.id)
                activity.startActivity(intent)
            }


            holder.binding.imgProduct.load(product.thumbnail) {
                crossfade(600)
                error(R.drawable.placeholder_image)
            }

            holder.binding.tvNameProduct.text = product.name
            holder.binding.tvTypeProduct.text = product.type
            holder.binding.tvPriceProduct.text = activity.getString(
                R.string.product_price, NumberFormat.getNumberInstance(
                    Locale.US
                ).format(product.originPrice)
            )
        }
    }

}