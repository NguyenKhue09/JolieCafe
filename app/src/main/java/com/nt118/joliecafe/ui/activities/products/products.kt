package com.nt118.joliecafe.ui.activities.products

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.nt118.joliecafe.R
import com.nt118.joliecafe.databinding.ActivityLoginBinding
import com.nt118.joliecafe.databinding.ActivityProductsBinding

class products : AppCompatActivity() {
    private var _binding: ActivityProductsBinding? = null
    private val  binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityProductsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bundle : Bundle? = intent.extras
        val position = bundle!!.getInt("position")
        binding.tvTest.text = position.toString()
    }
}