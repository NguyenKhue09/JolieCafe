package com.nt118.joliecafe.ui.activities.products

import android.content.Context
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
import com.nt118.joliecafe.adapter.FavoriteItemAdapter
import com.nt118.joliecafe.adapter.ProductAdapter
import com.nt118.joliecafe.databinding.ActivityProductsBinding
import com.nt118.joliecafe.models.CategorieModel
import com.nt118.joliecafe.util.Constants
import com.nt118.joliecafe.util.NetworkListener
import com.nt118.joliecafe.util.ProductComparator
import com.nt118.joliecafe.util.extenstions.observeOnce
import com.nt118.joliecafe.viewmodels.products.ProductsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest


@AndroidEntryPoint
class ProductsActivity : AppCompatActivity() {
    private val productsViewModel by viewModels<ProductsViewModel>()

    private var _binding: ActivityProductsBinding? = null
    private val  binding get() = _binding!!

    private lateinit var networkListener: NetworkListener
    private lateinit var productAdapter: ProductAdapter
    private lateinit var selectedTab: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityProductsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        productsViewModel.readBackOnline.asLiveData().observe(this) {
            productsViewModel.backOnline = it
        }

        val bundle : Bundle? = intent.extras
        val position = bundle!!.getInt("position")

        productsViewModel.setTabSelected(tab = Constants.listTabContentFavorite[position])
        setCatagoriesAdapterProducts(position)

        // back home
        binding.iconBackHome.setOnClickListener {
            onBackPressed()
        }

        // RecyclerView Categories
        val recyclerView = binding.recyclerView
        val categorieAdapter = CategoriesProductsAdapter(fetData(),position){
            productsViewModel.setTabSelected(tab = Constants.listTabContentFavorite[it])
            setCatagoriesAdapterProducts(it)
        }
        recyclerView.layoutManager = GridLayoutManager(this,4)
        recyclerView.adapter = categorieAdapter



        // RecyclerView product
        val diffCallBack = ProductComparator
        val recyclerViewProduct = binding.recyclerViewProduct
         productAdapter = ProductAdapter( this, diffCallBack = diffCallBack, productsViewModel = productsViewModel)
        recyclerViewProduct.layoutManager = GridLayoutManager(this,2)
        recyclerViewProduct.adapter = productAdapter


        handlePagingAdapterState()


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

    private fun handlePagingAdapterState() {
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



    private fun setCatagoriesAdapterProducts(position:Int) {

        lifecycleScope.launchWhenStarted {
            productsViewModel.readUserToken.collectLatest { token ->
                productsViewModel.userToken = token
            }
        }

        lifecycleScope.launchWhenStarted {
            networkListener = NetworkListener()
            networkListener.checkNetworkAvailability(this@ProductsActivity)
                .collect { status ->
                    productsViewModel.networkStatus = status
                    productsViewModel.showNetworkStatus()
                    if(productsViewModel.backOnline) {
                        productsViewModel.getProducts(
                            productQuery = mapOf(
                                "type" to Constants.listTabContentFavorite[position]
                            ),
                            token = productsViewModel.userToken
                        ).collectLatest { data ->
                            selectedTab = Constants.listTabContentFavorite[position]
                            productAdapter.submitData(data)
                        }
                    }
                }
        }

        productsViewModel.tabSelected.observe(this) { tab ->
            if (productsViewModel.networkStatus) {
                lifecycleScope.launchWhenStarted {
                    productsViewModel.getProducts(
                        productQuery = mapOf(
                            "type" to tab
                        ),
                        token = productsViewModel.userToken
                    ).collectLatest { data ->
                        selectedTab = tab
                        productAdapter.submitData(data)
                    }
                }
            } else {
                productsViewModel.showNetworkStatus()
            }
        }
    }

}