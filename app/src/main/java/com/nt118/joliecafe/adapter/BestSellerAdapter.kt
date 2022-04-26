package com.nt118.joliecafe.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.nt118.joliecafe.R
import com.nt118.joliecafe.databinding.ItemRvBestsallerBinding
import com.nt118.joliecafe.models.Product
import com.nt118.joliecafe.ui.fragments.catagories.CatagoriesBottomSheetFragment
import com.nt118.joliecafe.util.ItemDiffUtil


class BestSellerAdapter(private val activity: Activity) : RecyclerView.Adapter<BestSellerAdapter.ViewHolder>() {

    private var products = emptyList<Product>()

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
        holder.binding.btnAddCard.setOnClickListener {
            val ft = (activity as AppCompatActivity).supportFragmentManager.beginTransaction()
            val bottomSheet = CatagoriesBottomSheetFragment()
            bottomSheet.show(ft, "TAG")
        }

        holder.binding.itemCard.setOnClickListener {
            Toast.makeText( activity, "Halo ${position}", Toast.LENGTH_SHORT).show()
            println("Halo ${position}")
        }

        val product = products[position]

        holder.binding.itemImg.load(product.thumbnail) {
            crossfade(600)
            error(R.drawable.ic_coffee)
        }

        holder.binding.itemName.text = product.name
        holder.binding.itemPrice.text = product.originPrice.toString()
    }

    override fun getItemCount(): Int {
        return products.size
    }

    fun setData(newProducts: List<Product>) {
        val productsDiffUtil =
            ItemDiffUtil(products, newProducts)
        val diffUtilResult = DiffUtil.calculateDiff(productsDiffUtil)
        products = newProducts
        diffUtilResult.dispatchUpdatesTo(this)
    }
}