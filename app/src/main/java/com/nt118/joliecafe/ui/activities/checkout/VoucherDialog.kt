package com.nt118.joliecafe.ui.activities.checkout

import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nt118.joliecafe.databinding.DialogVoucherBinding
import com.nt118.joliecafe.models.Voucher
import com.nt118.joliecafe.util.ApiResult
import com.nt118.joliecafe.viewmodels.checkout.CheckoutViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VoucherDialog : AppCompatActivity() {

    private var _binding: DialogVoucherBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<CheckoutViewModel>()
    private var subTotal = 0
    private lateinit var discountAdapter: VoucherAdapter
    private lateinit var freeShipAdapter: VoucherAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = DialogVoucherBinding.inflate(layoutInflater)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)
        window.setLayout(dpToPx(350), ViewGroup.LayoutParams.WRAP_CONTENT)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val rvDiscount = binding.rvDiscount
        val rvFreeShip = binding.rvFreeShip

        getSubTotal()
        observeResponse()
        viewModel.getVouchers()
        listeners()

        rvDiscount.itemAnimator = null
        rvFreeShip.itemAnimator = null
    }

    private fun listeners() {
        binding.btnApply.setOnClickListener {
            val voucherList: MutableList<Voucher> = mutableListOf()
            if (discountAdapter.getSelected() != null) {
                voucherList.add(discountAdapter.getSelected()!!)
            }
            if (freeShipAdapter.getSelected() != null) {
                voucherList.add(freeShipAdapter.getSelected()!!)
            }
            val voucherJson = Gson().toJson(voucherList)
            val intent = Intent()
            intent.putExtra("voucher", voucherJson)
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    private fun getSubTotal() {
        subTotal = intent.getIntExtra("subTotal", 0)
    }

    private fun observeResponse() {
        viewModel.getVoucherResponse.observe(this) { response ->
            when (response) {
                is ApiResult.Loading -> {
                    binding.progressIndicator.visibility = View.VISIBLE
                }
                is ApiResult.Success -> {
                    binding.progressIndicator.visibility = View.GONE
                    response.data?.let { data ->
                        val discountData = data.filter { it.type == "Discount" && it.condition <= subTotal }
                        val freeShipData = data.filter { it.type == "Ship" && it.condition <= subTotal }
                        discountAdapter = VoucherAdapter(discountData, this)
                        freeShipAdapter = VoucherAdapter(freeShipData, this)
                        binding.rvDiscount.adapter = discountAdapter
                        binding.rvFreeShip.adapter = freeShipAdapter
                        if (discountData.isEmpty()) {
                            binding.tvDiscountLabel.visibility = View.GONE
                            binding.tvFreeShipLabel.visibility = View.GONE
                        }
                    }
                }
                is ApiResult.Error -> {
                    println(response.message)
                    finish()
                }
                else -> {}
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    fun dpToPx(dp: Int): Int {
        return (dp * Resources.getSystem().displayMetrics.density).toInt()
    }
}