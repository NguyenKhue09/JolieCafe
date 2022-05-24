package com.nt118.joliecafe.ui.activities.checkout

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.cardview.widget.CardView
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.nt118.joliecafe.R
import com.nt118.joliecafe.adapter.CheckoutAdapter
import com.nt118.joliecafe.databinding.ActivityCheckoutBinding
import com.nt118.joliecafe.ui.activities.order_detail.OrderDetailActivity
import com.nt118.joliecafe.util.ApiResult
import com.nt118.joliecafe.util.NetworkListener
import com.nt118.joliecafe.util.NumberUtil
import com.nt118.joliecafe.viewmodels.checkout.CheckoutViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class CheckoutActivity : AppCompatActivity() {

    private var _binding: ActivityCheckoutBinding? = null
    private val binding get() = _binding!!
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private lateinit var networkListener: NetworkListener
    private val checkoutViewModel by viewModels<CheckoutViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val rvProduct: RecyclerView = binding.rvProduct
        val btnNavBack: ImageButton = binding.btnNavBack
        val btnOrder: MaterialButton = binding.btnOrder
        val btnCancel: MaterialButton = binding.btnCancel
        val voucherContainer: CardView = binding.voucherContainer
        val tvUseJolieCoin: TextView = binding.tvUseJolieCoin
        val progressCart = binding.progressCart
        val tvSubtotalDetail = binding.tvSubtotalDetail

        checkoutViewModel.readBackOnline.asLiveData().observe(this) {
            checkoutViewModel.backOnline = it
        }

        checkoutViewModel.getCartResponse.observe(this) { response ->
            when (response) {
                is ApiResult.Loading -> progressCart.visibility = View.VISIBLE
                is ApiResult.Success -> {
                    progressCart.visibility = View.GONE
                    val adapter = CheckoutAdapter(response.data!!, this)
                    rvProduct.adapter = adapter
                    tvSubtotalDetail.text = getString(R.string.product_price, NumberUtil.addSeparator(adapter.getTotalPrice()))
                }
                is ApiResult.Error -> {
                    progressCart.visibility = View.GONE
                    Toast.makeText(this, "Failed to get cart items", Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }

        lifecycleScope.launchWhenStarted {
            checkoutViewModel.readUserToken.collectLatest { token ->
                checkoutViewModel.userToken = token
                checkoutViewModel.getAllCartItems(token)
            }
        }

        btnNavBack.setOnClickListener {
            finish()
        }

        btnOrder.setOnClickListener {
            startActivity(Intent(this, OrderDetailActivity::class.java))
        }

        btnCancel.setOnClickListener {
            finish()
        }

        voucherContainer.setOnClickListener {
            val intent = Intent(this, VoucherDialog::class.java)
            intent.putExtra("screenWidth", pxToDp(370f, this).toInt())
            startActivity(intent)
        }

        tvUseJolieCoin.text = resources.getString(R.string.use_jolie_coin, 200)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun pxToDp(px: Float, context: Context) =
        px / (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)

}