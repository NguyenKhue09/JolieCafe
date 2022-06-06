package com.nt118.joliecafe.ui.activities.detail

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.google.android.material.snackbar.Snackbar
import com.nt118.joliecafe.R
import com.nt118.joliecafe.adapter.MoreProductsAdapter
import com.nt118.joliecafe.adapter.ReviewProductAdapter
import com.nt118.joliecafe.databinding.ActivityDetailBinding
import com.nt118.joliecafe.util.ApiResult
import com.nt118.joliecafe.util.Constants
import com.nt118.joliecafe.util.NetworkListener
import com.nt118.joliecafe.util.extenstions.setCustomBackground
import com.nt118.joliecafe.util.extenstions.setIcon
import com.nt118.joliecafe.viewmodels.detail_product.DetailProductViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList

@AndroidEntryPoint
class DetailActivity : AppCompatActivity() {

    private val detailProductViewModel by viewModels<DetailProductViewModel>()

    private var _binding: ActivityDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var networkListener: NetworkListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bundle : Bundle? = intent.extras
        val productId = bundle!!.getString("productId")

        networkListener = NetworkListener()

        recyclerViewReview()
        recyclerViewMoreProducts()

        updateNetworkStatus()
        updateBackOnlineStatus()
        observerNetworkMessage()

        lifecycleScope.launchWhenStarted {
            detailProductViewModel.getDetailProduct(
                token = detailProductViewModel.userToken,
                productId = "6266aaa2de6570302a415602"
            )
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
                        binding.tvNumberReview.text = data.comments?.size.toString()
                        if(data.comments?.size == 0) {
                            binding.btnViewAllReview.visibility = View.GONE
                            binding.rvReview.visibility = View.GONE
                        }
                        println("hiện lên nào em ơi")
                        println(data.comments)
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

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding  = null
    }

    private fun recyclerViewReview() {
        val rvReview = binding.rvReview
        val reviewProductAdapter = ReviewProductAdapter(fetDataBestSaler())
        rvReview.layoutManager = LinearLayoutManager(this,
            LinearLayoutManager.VERTICAL, false)
        rvReview.adapter = reviewProductAdapter
    }

    private fun recyclerViewMoreProducts() {
        val rvMoreProduct = binding.rvMoreProducts
        val moreProductsAdapter = MoreProductsAdapter(fetDataBestSaler())
        rvMoreProduct.layoutManager = LinearLayoutManager(this,
            LinearLayoutManager.HORIZONTAL, false)
        rvMoreProduct.adapter = moreProductsAdapter
    }

    private fun fetDataBestSaler() : ArrayList<String> {
        val item = ArrayList<String>()
        for (i in 0 until 5) {
            item.add("$i")
        }
        return item
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
                    productId = "6267fb7a02095fbefdd3cbb7"
                )
            }
        }
    }


    //


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