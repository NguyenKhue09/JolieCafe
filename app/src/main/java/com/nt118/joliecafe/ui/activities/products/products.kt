package com.nt118.joliecafe.ui.activities.products

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.nt118.joliecafe.R
import com.nt118.joliecafe.adapter.CategorieAdapter
import com.nt118.joliecafe.databinding.ActivityProductsBinding
import com.nt118.joliecafe.models.CategorieModel
import com.nt118.joliecafe.ui.fragments.home.HomeFragment

class products : AppCompatActivity() {
    private var _binding: ActivityProductsBinding? = null
    private val  binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityProductsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bundle : Bundle? = intent.extras
        val position = bundle!!.getInt("position")

        //add icon back home
        binding.toolbarHome.btnBackHome.setImageResource(R.drawable.ic_baseline_arrow_back_24)
        binding.toolbarHome.btnBackHome.layoutParams.width = 100
        binding.toolbarHome.btnBackHome.layoutParams.height = 100
        binding.toolbarHome.btnBackHome.setMarginExtensionFunction(0,0,20,0)
        binding.toolbarHome.btnBackHome.setOnClickListener {
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
    }

    //set margin
    fun View.setMarginExtensionFunction(left: Int, top: Int, right: Int, bottom: Int) {
        val params = layoutParams as ViewGroup.MarginLayoutParams
        params.setMargins(left, top, right, bottom)
        layoutParams = params
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
}