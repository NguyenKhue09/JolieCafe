package com.nt118.joliecafe.ui.activities.products

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.nt118.joliecafe.R
import com.nt118.joliecafe.adapter.CategoriesProductsAdapter
import com.nt118.joliecafe.adapter.ProductAdapter
import com.nt118.joliecafe.databinding.ActivityProductsBinding
import com.nt118.joliecafe.models.CategorieModel
import com.nt118.joliecafe.util.ApiResult
import com.nt118.joliecafe.util.Constants
import com.nt118.joliecafe.util.NetworkListener
import com.nt118.joliecafe.util.ProductComparator
import com.nt118.joliecafe.util.extenstions.setCustomBackground
import com.nt118.joliecafe.util.extenstions.setIcon
import com.nt118.joliecafe.viewmodels.products.ProductsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@AndroidEntryPoint
class ProductsActivity : AppCompatActivity() {
    val productsViewModel by viewModels<ProductsViewModel>()

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
         productAdapter = ProductAdapter( this, diffCallBack = diffCallBack )
        recyclerViewProduct.layoutManager = GridLayoutManager(this,2)
        recyclerViewProduct.adapter = productAdapter


        handlePagingAdapterState()
        handleAddFavoriteResponse()
        handleDeleteFavoriteProductResponse()
        handleSearchBox()

        binding.searchView.setOnClickListener {
            if (!binding.searchView.query.isNullOrEmpty()) {
                lifecycleScope.launch {
                    productsViewModel.getProducts(
                        mapOf(
                            "name" to binding.searchView.query.toString(),
                            "type" to selectedTab
                        ),
                        productsViewModel.userToken
                    ).collectLatest { data ->
                        productAdapter.submitData(data)
                    }
                }
            }
            binding.searchView.clearFocus()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


    override fun onResume() {
        super.onResume()
        println("onResume")
        productsViewModel.getFavorite(productsViewModel.userToken)

    }

    private fun handleSearchBox() {
        binding.searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                println("onQueryTextSubmit")
                println(query)
                if(!query.isNullOrEmpty()) {
                    lifecycleScope.launch {
                        productsViewModel.getProducts(
                            mapOf(
                                "name" to query,
                                "type" to selectedTab
                            ),
                            productsViewModel.userToken
                        ).collectLatest { data ->
                            productAdapter.submitData(data)
                        }
                    }
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {

                return false
            }
        })
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
                        productsViewModel.getFavorite(productsViewModel.userToken)
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
                    productsViewModel.getFavorite(productsViewModel.userToken)
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

    //favorite
    private fun handleAddFavoriteResponse() {
        productsViewModel.addFavoriteProductResponse.observe(this@ProductsActivity) { response ->
            when (response) {
                is ApiResult.Loading -> {

                }
                is ApiResult.NullDataSuccess -> {
                    showSnackBar(
                        message = "Add favorite product successful",
                        status = Constants.SNACK_BAR_STATUS_SUCCESS,
                        icon = R.drawable.ic_success
                    )
                    lifecycleScope.launch {
                        productsViewModel.readUserToken.collectLatest { token ->
                            productsViewModel.userToken = token
                            withContext(Dispatchers.IO) {
                                productsViewModel.getFavorite(token)
                            }
                        }
                    }
                }
                is ApiResult.Error -> {
                    Toast.makeText(this, response.message, Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }

    fun addNewFavorite(productId: String) {
        productsViewModel.addFavoriteProduct(
            token = productsViewModel.userToken,
            productId = productId
        )
    }

    fun removeFavoriteProduct(productId: String) {
        productsViewModel.removeFavoriteProduct(
            token = productsViewModel.userToken,
            productId = productId
        )
    }


    private fun handleDeleteFavoriteProductResponse() {
        productsViewModel.deleteFavoriteProductResponse.observe(this) { response ->
            when (response) {
                is ApiResult.Loading -> {
                }
                is ApiResult.NullDataSuccess -> {
                    //Toast.makeText(requireContext(), "Remove favorite product successful", Toast.LENGTH_SHORT).show()
                    showSnackBar(
                        message = "Remove favorite product successful",
                        status = Constants.SNACK_BAR_STATUS_SUCCESS,
                        icon = R.drawable.ic_success
                    )
                    lifecycleScope.launch {
                        productsViewModel.readUserToken.collectLatest { token ->
                            productsViewModel.userToken = token
                            withContext(Dispatchers.IO) {
                                productsViewModel.getFavorite(token)
                            }
                        }
                    }
                }
                is ApiResult.Error -> {
                    //Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                    showSnackBar(
                        message = "Remove favorite product failed!",
                        status = Constants.SNACK_BAR_STATUS_ERROR,
                        icon = R.drawable.ic_error
                    )
                }
                else -> {}
            }
        }
    }

    private fun showSnackBar(message: String, status: Int, icon: Int) {
        val drawable = this.getDrawable(icon)

        val snackBarContentColor = when (status) {
            Constants.SNACK_BAR_STATUS_SUCCESS -> R.color.text_color_2
            Constants.SNACK_BAR_STATUS_DISABLE -> R.color.dark_text_color
            Constants.SNACK_BAR_STATUS_ERROR -> R.color.error_color
            else -> R.color.text_color_2
        }


        val snackBar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction("Ok") {
            }
            .setActionTextColor(ContextCompat.getColor(this, R.color.grey_primary))
            .setTextColor(ContextCompat.getColor(this, snackBarContentColor))
            .setIcon(
                drawable = drawable!!,
                colorTint = ContextCompat.getColor(this, snackBarContentColor),
                iconPadding = resources.getDimensionPixelOffset(R.dimen.small_margin)
            )
            .setCustomBackground(this.getDrawable(R.drawable.snackbar_normal_custom_bg)!!)

        snackBar.show()
    }



}