package com.nt118.joliecafe.ui.activities.checkout

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.nt118.joliecafe.databinding.ActivityCheckoutBinding
import com.nt118.joliecafe.ui.activities.order_detail.OrderDetailActivity

class CheckoutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCheckoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val rvProduct: RecyclerView = binding.rvProduct
        val btnNavBack: ImageButton = binding.btnNavBack
        val btnOrder: MaterialButton = binding.btnOrder

        rvProduct.adapter = CheckoutAdapter()

        btnNavBack.setOnClickListener {
            finish()
        }

        btnOrder.setOnClickListener {
            startActivity(Intent(this, OrderDetailActivity::class.java))
        }
    }
}