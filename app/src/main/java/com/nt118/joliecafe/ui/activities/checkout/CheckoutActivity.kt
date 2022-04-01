package com.nt118.joliecafe.ui.activities.checkout

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.widget.ImageButton
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.nt118.joliecafe.databinding.ActivityCheckoutBinding
import com.nt118.joliecafe.ui.activities.order_detail.OrderDetailActivity

class CheckoutActivity : AppCompatActivity() {

    private var _binding: ActivityCheckoutBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val rvProduct: RecyclerView = binding.rvProduct
        val btnNavBack: ImageButton = binding.btnNavBack
        val btnOrder: MaterialButton = binding.btnOrder
        val voucherContainer: CardView = binding.voucherContainer

        rvProduct.adapter = CheckoutAdapter()

        btnNavBack.setOnClickListener {
            finish()
        }

        btnOrder.setOnClickListener {
            startActivity(Intent(this, OrderDetailActivity::class.java))
        }

        voucherContainer.setOnClickListener {
            val intent = Intent(this, VoucherDialog::class.java)
            intent.putExtra("screenWidth", pxToDp(370f, this).toInt())
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun pxToDp(px: Float, context: Context) =
        px / (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)

}