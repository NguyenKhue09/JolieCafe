package com.nt118.joliecafe.ui.activities.products

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.nt118.joliecafe.R
import com.nt118.joliecafe.adapter.CategorieAdapter
import com.nt118.joliecafe.adapter.ProductAdapter
import com.nt118.joliecafe.databinding.ActivityProductsBinding
import com.nt118.joliecafe.models.CategorieModel

class products : AppCompatActivity() {
    private var _binding: ActivityProductsBinding? = null
    private val  binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityProductsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bundle : Bundle? = intent.extras
        val position = bundle!!.getInt("position")

        // back home
        binding.iconBackHome.setOnClickListener {
            onBackPressed()
        }

        // RecyclerView Categories
        val recyclerView = binding.recyclerView
        val categorieAdapter = CategorieAdapter(fetData())
        recyclerView.layoutManager = GridLayoutManager(this,4)
        recyclerView.adapter = categorieAdapter
        categorieAdapter.setOnClickListener(object : CategorieAdapter.onItemClickListener{
            override fun onItemClick(position: Int) {

            }
        })

        // RecyclerView product
        val recyclerViewProduct = binding.recyclerViewProduct
        val productAdapter = ProductAdapter(fetDataBestSaler())
        recyclerViewProduct.layoutManager = GridLayoutManager(this,2)
        recyclerViewProduct.adapter = productAdapter
        productAdapter.setOnClickListener(object : ProductAdapter.onItemClickListener{
            override fun onItemClick(position: Int) {

            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    // add image categories
    private fun fetData() : ArrayList<CategorieModel>{
        val item = ArrayList<CategorieModel>()
        item.add(CategorieModel("All",R.drawable.ic_star))
        item.add(CategorieModel("Coffee",R.drawable.ic_coffee))
        item.add(CategorieModel("Tea",R.drawable.ic_leaf))
        item.add(CategorieModel("Juice",R.drawable.ic_watermelon))
        item.add(CategorieModel("Pastry",R.drawable.ic_croissant_svgrepo_com))
        item.add(CategorieModel("Coffee",R.drawable.ic_coffee))
        item.add(CategorieModel("Coffee",R.drawable.ic_coffee))
        item.add(CategorieModel("More",R.drawable.ic_coffee))
        return  item
    }

    // test product
    private fun fetDataBestSaler() : ArrayList<String> {
        val item = ArrayList<String>()
        for (i in 0 until 15) {
            item.add("$i")
        }
        return item
    }
}