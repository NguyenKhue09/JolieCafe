package com.nt118.joliecafe.ui.activities.products

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import com.nt118.joliecafe.R
import com.nt118.joliecafe.adapter.CategorieAdapter
import com.nt118.joliecafe.adapter.CategoriesProductsAdapter
import com.nt118.joliecafe.adapter.ProductAdapter
import com.nt118.joliecafe.databinding.ActivityProductsBinding
import com.nt118.joliecafe.models.CategorieModel
import com.nt118.joliecafe.util.NetworkListener
import com.nt118.joliecafe.util.ProductComparator
import com.nt118.joliecafe.viewmodels.products.ProductsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest


@AndroidEntryPoint
class ProductsActivity : AppCompatActivity() {
    private val productsViewModel by viewModels<ProductsViewModel>()

    private var _binding: ActivityProductsBinding? = null
    private val  binding get() = _binding!!

    private lateinit var networkListener: NetworkListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityProductsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        productsViewModel.readBackOnline.asLiveData().observe(this) {
            productsViewModel.backOnline = it
        }

        val bundle : Bundle? = intent.extras
        val position = bundle!!.getInt("position")

        // back home
        binding.iconBackHome.setOnClickListener {
            onBackPressed()
        }

        // RecyclerView Categories
        val recyclerView = binding.recyclerView
        val categorieAdapter = CategoriesProductsAdapter(fetData(),position){
            Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT).show()
        }
        recyclerView.layoutManager = GridLayoutManager(this,4)
        recyclerView.adapter = categorieAdapter



        // RecyclerView product
        val diffCallBack = ProductComparator
        val recyclerViewProduct = binding.recyclerViewProduct
        val productAdapter = ProductAdapter( this, diffCallBack = diffCallBack)
        recyclerViewProduct.layoutManager = GridLayoutManager(this,2)
        recyclerViewProduct.adapter = productAdapter

        lifecycleScope.launchWhenStarted {
            networkListener = NetworkListener()
            networkListener.checkNetworkAvailability(this@ProductsActivity)
                .collect { status ->
                    productsViewModel.networkStatus = status
                    productsViewModel.showNetworkStatus()
                    if(productsViewModel.backOnline) {
                        productsViewModel.getProducts(
                            productQuery = mapOf(
                                "type" to "Coffee"
                            ),
                            token = productsViewModel.userToken
                        ).collectLatest { data ->
                            productAdapter.submitData(data)
                        }
                    }
                }
        }

        lifecycleScope.launchWhenStarted {
            productsViewModel.readUserToken.collectLatest { token ->
                productsViewModel.userToken = token
                productsViewModel.getProducts(
                    productQuery = mapOf(
                        "type" to "Coffee"
                    ),
                    token = token
                ).collectLatest { data ->
                    productAdapter.submitData(data)
                }
            }
        }

        productAdapter.addLoadStateListener { loadState ->
            if (loadState.refresh is LoadState.Loading){
                binding.categoriesCircularProgressIndicator.visibility = View.VISIBLE
            }
            else{
                binding.categoriesCircularProgressIndicator.visibility = View.GONE
                // getting the error
                val error = when {
                    loadState.prepend is LoadState.Error -> loadState.prepend as LoadState.Error
                    loadState.append is LoadState.Error -> loadState.append as LoadState.Error
                    loadState.refresh is LoadState.Error -> loadState.refresh as LoadState.Error
                    else -> null
                }
                error?.let {
                    Toast.makeText(this, it.error.message, Toast.LENGTH_LONG).show()
                }
            }
        }

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
        item.add(CategorieModel("Milk shake",R.drawable.milkshake))
        item.add(CategorieModel("Milk tea",R.drawable.bubble_tea))
        return  item
    }

}