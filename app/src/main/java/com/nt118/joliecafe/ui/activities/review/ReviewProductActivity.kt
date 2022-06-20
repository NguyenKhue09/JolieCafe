package com.nt118.joliecafe.ui.activities.review

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.nt118.joliecafe.R
import com.nt118.joliecafe.adapter.AllReviewProductAdapter
import com.nt118.joliecafe.databinding.ActivityReviewProductBinding
import com.nt118.joliecafe.models.Comment
import com.nt118.joliecafe.util.ApiResult
import com.nt118.joliecafe.util.Constants
import com.nt118.joliecafe.util.NetworkListener
import com.nt118.joliecafe.util.extenstions.setCustomBackground
import com.nt118.joliecafe.util.extenstions.setIcon
import com.nt118.joliecafe.viewmodels.review.ReviewProductViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReviewProductActivity : AppCompatActivity() {

    private val reviewProductViewModel by viewModels<ReviewProductViewModel>()
    private var _binding: ActivityReviewProductBinding? = null
    private val binding get() = _binding!!
    private lateinit var networkListener: NetworkListener
    private lateinit var listComtment : List<Comment>

    private lateinit var reviewProductAdapter: AllReviewProductAdapter

    private lateinit var  productId: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityReviewProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bundle : Bundle? = intent.extras
        productId = bundle!!.getString("productId").toString()
        binding.iconBackHome.setOnClickListener {
            onBackPressed()
            finish()
        }

        networkListener = NetworkListener()

        updateNetworkStatus()
        updateBackOnlineStatus()
        observerNetworkMessage()

        lifecycleScope.launchWhenStarted {
            reviewProductViewModel.getComment(
                token = reviewProductViewModel.userToken,
                productId = productId
            )
        }
        reviewProductViewModel.getCommentProductResponse.observe(this){ response ->
            when(response){
                is ApiResult.Loading -> {
                    binding.categoriesCircularProgressIndicator.visibility = View.VISIBLE
                }
                is ApiResult.Success -> {
                    val data = response.data!!
                    binding.categoriesCircularProgressIndicator.visibility = View.GONE
                    listComtment = data
                    val rvReview = binding.rvReviewProduct
                    reviewProductAdapter = AllReviewProductAdapter(data.sortedByDescending { it.rating })
                    binding.tvReviewProductEmpty.visibility = View.GONE
                    rvReview.layoutManager = LinearLayoutManager(this@ReviewProductActivity,
                        LinearLayoutManager.VERTICAL, false)
                    rvReview.adapter = reviewProductAdapter
                }
                is ApiResult.Error -> {
                    binding.categoriesCircularProgressIndicator.visibility = View.GONE
                    Snackbar.make(binding.root, "Error", Snackbar.LENGTH_LONG).show()
                }
                else -> {}
            }
        }

        binding.chip5Star.setOnClickListener {
            val rvReview = binding.rvReviewProduct
            reviewProductAdapter = AllReviewProductAdapter(
                listComtment.filter { it.rating == 5 }
            )
            if(listComtment.filter { it.rating == 5 }.isEmpty()) {
                binding.tvReviewProductEmpty.visibility = View.VISIBLE
            }
            else {
                binding.tvReviewProductEmpty.visibility = View.GONE
            }
            rvReview.layoutManager = LinearLayoutManager(this@ReviewProductActivity,
                LinearLayoutManager.VERTICAL, false)
            rvReview.adapter = reviewProductAdapter
        }
        binding.chip4Star.setOnClickListener {
            val rvReview = binding.rvReviewProduct
            reviewProductAdapter = AllReviewProductAdapter(
                listComtment.filter { it.rating == 4 }
            )
            if(listComtment.filter { it.rating == 4 }.isEmpty()) {
                binding.tvReviewProductEmpty.visibility = View.VISIBLE
            }
            else {
                binding.tvReviewProductEmpty.visibility = View.GONE
            }
            rvReview.layoutManager = LinearLayoutManager(this@ReviewProductActivity,
                LinearLayoutManager.VERTICAL, false)
            rvReview.adapter = reviewProductAdapter
        }
        binding.chip3Star.setOnClickListener {
            val rvReview = binding.rvReviewProduct
            reviewProductAdapter = AllReviewProductAdapter(
                listComtment.filter { it.rating == 3 }
            )
            if(listComtment.filter { it.rating == 3 }.isEmpty()) {
                binding.tvReviewProductEmpty.visibility = View.VISIBLE
            }
            else {
                binding.tvReviewProductEmpty.visibility = View.GONE
            }
            rvReview.layoutManager = LinearLayoutManager(this@ReviewProductActivity,
                LinearLayoutManager.VERTICAL, false)
            rvReview.adapter = reviewProductAdapter
        }
        binding.chip2Star.setOnClickListener {
            val rvReview = binding.rvReviewProduct
            reviewProductAdapter = AllReviewProductAdapter(
                listComtment.filter { it.rating == 2 }
            )
            if(listComtment.filter { it.rating == 2 }.isEmpty()) {
                binding.tvReviewProductEmpty.visibility = View.VISIBLE
            }
            else {
                binding.tvReviewProductEmpty.visibility = View.GONE
            }
            rvReview.layoutManager = LinearLayoutManager(this@ReviewProductActivity,
                LinearLayoutManager.VERTICAL, false)
            rvReview.adapter = reviewProductAdapter
        }
        binding.chip1Star.setOnClickListener {
            val rvReview = binding.rvReviewProduct
            reviewProductAdapter = AllReviewProductAdapter(
                listComtment.filter { it.rating == 1 }
            )
            if(listComtment.filter { it.rating == 1 }.isEmpty()) {
                binding.tvReviewProductEmpty.visibility = View.VISIBLE
            }
            else {
                binding.tvReviewProductEmpty.visibility = View.GONE
            }
            rvReview.layoutManager = LinearLayoutManager(this@ReviewProductActivity,
                LinearLayoutManager.VERTICAL, false)
            rvReview.adapter = reviewProductAdapter
        }
        binding.chipRankAll.setOnClickListener {
            val rvReview = binding.rvReviewProduct
            reviewProductAdapter = AllReviewProductAdapter(listComtment.sortedByDescending { it.rating })
            binding.tvReviewProductEmpty.visibility = View.GONE
            rvReview.layoutManager = LinearLayoutManager(this@ReviewProductActivity,
                LinearLayoutManager.VERTICAL, false)
            rvReview.adapter = reviewProductAdapter
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding  = null
    }

    private fun updateBackOnlineStatus() {
        reviewProductViewModel.readBackOnline.asLiveData().observe(this) {
            reviewProductViewModel.backOnline = it
        }
    }

    private fun updateNetworkStatus() {
        networkListener.checkNetworkAvailability(this)
            .asLiveData().observe(this) { status ->
                reviewProductViewModel.networkStatus = status
                reviewProductViewModel.showNetworkStatus()
                backOnlineRecallDetailProducts()
            }
    }

    private fun observerNetworkMessage() {
        reviewProductViewModel.networkMessage.observe(this) { message ->
            if (!reviewProductViewModel.networkStatus) {
                showSnackBar(
                    message = message,
                    status = Constants.SNACK_BAR_STATUS_DISABLE,
                    icon = R.drawable.ic_wifi_off
                )
            } else if (reviewProductViewModel.networkStatus) {
                if (reviewProductViewModel.backOnline) {
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
            if (reviewProductViewModel.backOnline) {
                reviewProductViewModel.getComment(reviewProductViewModel.userToken,productId)
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