package com.nt118.joliecafe.ui.activities.order_detail

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.nt118.joliecafe.databinding.ActivityOrderDetailBinding

class OrderDetailActivity : AppCompatActivity() {
    private var _binding: ActivityOrderDetailBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityOrderDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}