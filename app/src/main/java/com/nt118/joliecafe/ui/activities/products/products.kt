package com.nt118.joliecafe.ui.activities.products

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.nt118.joliecafe.R
import com.nt118.joliecafe.databinding.ActivityLoginBinding
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

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