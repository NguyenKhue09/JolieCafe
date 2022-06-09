package com.nt118.joliecafe.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
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
import com.nt118.joliecafe.ui.fragments.catagories.CategoriesBottomSheetFragment
import java.text.NumberFormat
import java.util.*


class BestSellerAdapter(
    private val activity: Activity,
    diffCallBack: DiffUtil.ItemCallback<Product>,
    private val rootView: View
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




        val product = getItem(position)
        product?.let {

            holder.binding.itemCard.setOnClickListener {
                val intent = Intent(activity, DetailActivity::class.java)
                intent.putExtra("productId", product.id)
                activity.startActivity(intent)
            }

            holder.binding.btnAddCard.setOnClickListener {
                val ft = (activity as AppCompatActivity).supportFragmentManager.beginTransaction()
                val bottomSheet = CategoriesBottomSheetFragment(product, rootView)
                bottomSheet.show(ft, "TAG")
            }

            holder.binding.itemImg.load(product.thumbnail) {
                crossfade(600)
                error(R.drawable.placeholder_image)
            }

            holder.binding.itemName.text = product.name
            holder.binding.itemCategories.text = product.type
            holder.binding.itemPrice.text = activity.getString(
                R.string.product_price, NumberFormat.getNumberInstance(
                    Locale.US
                ).format(product.originPrice)
            )
        }
    }

}