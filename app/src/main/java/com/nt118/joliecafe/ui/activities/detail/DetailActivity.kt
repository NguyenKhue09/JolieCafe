package com.nt118.joliecafe.ui.activities.detail

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.nt118.joliecafe.R
import com.nt118.joliecafe.adapter.MoreProductsAdapter
import com.nt118.joliecafe.adapter.ReviewProductAdapter
import com.nt118.joliecafe.databinding.ActivityDetailBinding
import com.nt118.joliecafe.models.CartItem
import com.nt118.joliecafe.ui.activities.checkout.CheckoutActivity
import com.nt118.joliecafe.ui.activities.review.ReviewProductActivity
import com.nt118.joliecafe.util.*
import com.nt118.joliecafe.util.extenstions.setCustomBackground
import com.nt118.joliecafe.util.extenstions.setIcon
import com.nt118.joliecafe.viewmodels.detail_product.DetailProductViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.*

@AndroidEntryPoint
class DetailActivity : AppCompatActivity() {

    private val detailProductViewModel by viewModels<DetailProductViewModel>()

    private var _binding: ActivityDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var networkListener: NetworkListener
    private lateinit var moreProductsAdapter: MoreProductsAdapter

    private lateinit var reviewProductAdapter: ReviewProductAdapter

    private lateinit var  productId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bundle : Bundle? = intent.extras
        productId = bundle!!.getString("productId").toString()
        binding.btnBack.setOnClickListener {
            onBackPressed()
            finish()
        }

        networkListener = NetworkListener()

        recyclerViewMoreProducts()

        updateNetworkStatus()
        updateBackOnlineStatus()
        observerNetworkMessage()

        lifecycleScope.launchWhenStarted {
            detailProductViewModel.getDetailProduct(
                token = detailProductViewModel.userToken,
                productId = productId
            )

            detailProductViewModel.getFavorite(detailProductViewModel.userToken)
            detailProductViewModel.getComment(
                token = detailProductViewModel.userToken,
                productId = productId
            )
            detailProductViewModel.getProducts(
                productQuery = mapOf(
                    "type" to "All"
                ),
                token = detailProductViewModel.userToken
            ).collectLatest { data ->
                moreProductsAdapter.submitData(data)
            }

        }

        detailProductViewModel.getDetailProductResponse.observe(this) { response ->
            when (response) {
                is ApiResult.Loading -> {
                    binding.categoriesCircularProgressIndicator.visibility = View.VISIBLE
                    binding.nestedScrollView.visibility = View.GONE
                }
                is ApiResult.Success -> {
                    binding.categoriesCircularProgressIndicator.visibility = View.GONE
                    binding.nestedScrollView.visibility = View.VISIBLE
                    val data = response.data
                    data?.let {
                        binding.imgProduct.load(data.thumbnail) {
                            crossfade(600)
                            error(R.drawable.placeholder_image)
                        }
                        binding.tvPriceProduct.text = this.getString(
                            R.string.product_price,
                            NumberFormat.getNumberInstance(
                                Locale.US
                            ).format(data.originPrice)
                        )
                        binding.tvNameProduct.text = data.name
                        binding.tvDescriptionContent.text = data.description
                        binding.tvRank.text = data.avgRating.toString()


                        binding.btnAddCard.setOnClickListener {
                            val newCart = mapOf(
                                "productId" to productId,
                                "size" to "M",
                                "quantity" to "1",
                                "price" to data.originPrice.toString(),
                            )
                            addNewCart(cartData = newCart)
                        }

                        binding.btnBuyNow.setOnClickListener {
                            val newCart = mapOf(
                                "productId" to productId,
                                "size" to "M",
                                "quantity" to "1",
                                "price" to data.originPrice.toString(),
                            )
                            addNewCart(cartData = newCart)
                            val intent = Intent(this, CheckoutActivity::class.java)
                            val cartItems = mutableListOf<CartItem>()
                            cartItems.add(CartItem(productId, productId, data,"M",1, data.originPrice))
                            intent.putExtra("cartItems", Gson().toJson(cartItems))
                            startActivity(intent)
                        }
                    }
                }
                is ApiResult.Error -> {
                    binding.categoriesCircularProgressIndicator.visibility = View.VISIBLE
                    binding.nestedScrollView.visibility = View.GONE
                    Log.d("CartFragment", "get cart error: ${response.message}")
                }
                else -> {}
            }
        }

        detailProductViewModel.getCommentProductResponse.observe(this){ response ->
            when(response){
                is ApiResult.Loading -> {
                    binding.btnViewAllReview.visibility = View.GONE
                }
                is ApiResult.Success -> {
                    val data = response.data!!
                    if (data.isEmpty()){
                        binding.btnViewAllReview.visibility = View.GONE
                    }else if (data.size > 4){
                        binding.btnViewAllReview.visibility = View.VISIBLE
                        binding.btnViewAllReview.setOnClickListener {
                            val intent = Intent(this, ReviewProductActivity::class.java)
                            intent.putExtra("productId", productId)
                            startActivity(intent)
                        }
                    } else {
                        binding.btnViewAllReview.visibility = View.GONE
                    }
                    data.let {
                        binding.tvNumberReview.text = data.size.toString()
                        val rvReview = binding.rvReview
                        reviewProductAdapter = ReviewProductAdapter(data)
                        rvReview.layoutManager = LinearLayoutManager(this@DetailActivity,
                            LinearLayoutManager.VERTICAL, false)
                        rvReview.adapter = reviewProductAdapter

                    }
                }
                is ApiResult.Error -> {
                    binding.btnViewAllReview.visibility = View.GONE
                }
                else -> {}
            }
        }

        detailProductViewModel.getFavoriteProductResponse.observe(this) { response ->
            when (response) {
                is ApiResult.Loading -> {
                    binding.btnFavorite.visibility = View.VISIBLE
                }
                is ApiResult.Success -> {
                    val data = response.data!!
                    if (data.isEmpty()) {
                        binding.btnFavorite.visibility = View.VISIBLE
                    } else {
                        data.find { it.productId == productId }?.let {
                            binding.btnFavoriteChoose.visibility = View.VISIBLE
                            binding.btnFavorite.visibility = View.GONE
                        }
                    }
                }
                is ApiResult.Error -> {
                    binding.btnFavorite.visibility = View.VISIBLE
                }
                else -> {}
            }
        }

        binding.btnFavorite.setOnClickListener {
            addNewFavorite(productId = productId)
            binding.btnFavoriteChoose.visibility = View.VISIBLE
            binding.btnFavorite.visibility = View.GONE
        }

        binding.btnFavoriteChoose.setOnClickListener {
            removeFavoriteProduct(productId = productId)
            binding.btnFavoriteChoose.visibility = View.GONE
            binding.btnFavorite.visibility = View.VISIBLE
        }
        binding.btnViewAllReview.setOnClickListener {
            val intent = Intent(this, ReviewProductActivity::class.java)
            intent.putExtra("productId", productId)
            startActivity(intent)
        }


        handleAddFavoriteResponse()
        handleDeleteFavoriteProductResponse()
        handleApiAddCartResponse()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding  = null
    }


    private fun recyclerViewMoreProducts() {
        val diffCallBack = ProductComparator
        val rvMoreProduct = binding.rvMoreProducts
        moreProductsAdapter = MoreProductsAdapter(this, diffCallBack = diffCallBack)
        rvMoreProduct.layoutManager = LinearLayoutManager(this,
            LinearLayoutManager.HORIZONTAL, false)
        rvMoreProduct.adapter = moreProductsAdapter
    }


    //test

    private fun updateBackOnlineStatus() {
        detailProductViewModel.readBackOnline.asLiveData().observe(this) {
            detailProductViewModel.backOnline = it
        }
    }

    private fun updateNetworkStatus() {
        networkListener.checkNetworkAvailability(this)
            .asLiveData().observe(this) { status ->
                detailProductViewModel.networkStatus = status
                detailProductViewModel.showNetworkStatus()
                backOnlineRecallDetailProducts()
            }
    }

    private fun observerNetworkMessage() {
        detailProductViewModel.networkMessage.observe(this) { message ->
            if (!detailProductViewModel.networkStatus) {
                showSnackBar(
                    message = message,
                    status = Constants.SNACK_BAR_STATUS_DISABLE,
                    icon = R.drawable.ic_wifi_off
                )
            } else if (detailProductViewModel.networkStatus) {
                if (detailProductViewModel.backOnline) {
                    showSnackBar(
                        message = message,
                        status = Constants.SNACK_BAR_STATUS_SUCCESS,
                        icon = R.drawable.ic_wifi
                    )
                }
            }
        }
    }

    private fun backOnlineRecallDetailProducts() {
        lifecycleScope.launchWhenStarted {
            if (detailProductViewModel.backOnline) {
                detailProductViewModel.getDetailProduct(
                    token = detailProductViewModel.userToken,
                    productId = productId
                )
                detailProductViewModel.getProducts(
                    productQuery = mapOf(
                        "type" to "All"
                    ),
                    token = detailProductViewModel.userToken
                ).collectLatest { data ->
                    moreProductsAdapter.submitData(data)
                }
                detailProductViewModel.getFavorite(detailProductViewModel.userToken)
            }
        }
    }


    // favorite
    private fun handleAddFavoriteResponse() {
        detailProductViewModel.addFavoriteProductResponse.observe(this) { response ->
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
                        detailProductViewModel.readUserToken.collectLatest { token ->
                            detailProductViewModel.userToken = token
                            withContext(Dispatchers.IO) {
                                detailProductViewModel.getFavorite(token)
                            }
                        }
                    }
                }
                is ApiResult.Error -> {
                    Toast.makeText(this@DetailActivity, response.message, Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }

    private fun addNewFavorite(productId: String) {
        detailProductViewModel.addFavoriteProduct(
            token = detailProductViewModel.userToken,
            productId = productId
        )
    }

    private fun removeFavoriteProduct(productId: String) {
        detailProductViewModel.removeFavoriteProduct(
            token = detailProductViewModel.userToken,
            productId = productId
        )
    }


    private fun handleDeleteFavoriteProductResponse() {
        detailProductViewModel.deleteFavoriteProductResponse.observe(this) { response ->
            when (response) {
                is ApiResult.Loading -> {
                }
                is ApiResult.NullDataSuccess -> {
                    showSnackBar(
                        message = "Remove favorite product successful",
                        status = Constants.SNACK_BAR_STATUS_SUCCESS,
                        icon = R.drawable.ic_success
                    )
                    lifecycleScope.launch {
                        detailProductViewModel.readUserToken.collectLatest { token ->
                            detailProductViewModel.userToken = token
                            withContext(Dispatchers.IO) {
                                detailProductViewModel.getFavorite(token)
                            }
                        }
                    }
                }
                is ApiResult.Error -> {
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

    // add cart

    private fun handleApiAddCartResponse() {
        detailProductViewModel.addCartResponse.observe(this) { response ->
            Log.d("Bottom Shit", "handleApiResponse: call")
            when (response) {
                is ApiResult.Loading -> {

                }
                is ApiResult.NullDataSuccess -> {
                    showSnackBar(
                        message = "Add new cart successfully",
                        status = Constants.SNACK_BAR_STATUS_SUCCESS,
                        icon = R.drawable.ic_success
                    )
                }
                is ApiResult.Error -> {
                    showSnackBar(
                        message = "Add new cart failed!",
                        status = Constants.SNACK_BAR_STATUS_ERROR,
                        icon = R.drawable.ic_error
                    )
                }
                else -> {}
            }
        }
    }

    private fun addNewCart(cartData: Map<String, String>) {
        Log.d("Bottom Shit", "addNewCart: ${cartData.values}")
        detailProductViewModel.addCart(
            data = cartData,
            token = detailProductViewModel.userToken
        )
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