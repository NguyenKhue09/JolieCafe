package com.nt118.joliecafe.ui.activities.order_history

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.nt118.joliecafe.R
import com.nt118.joliecafe.adapter.OrderHistoryAdapter
import com.nt118.joliecafe.databinding.ActivityOrderHistoryBinding

class OrderHistoryActivity : AppCompatActivity() {

    private var _binding: ActivityOrderHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var orderHistoryAdapter: OrderHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityOrderHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupActionBar()
        setUpOrderHistoryRecyclerView()
    }

    private fun setUpOrderHistoryRecyclerView() {
        val orderHistoryRecyclerView = binding.rvOrderHistory
        orderHistoryAdapter = OrderHistoryAdapter(this)
        orderHistoryRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        orderHistoryRecyclerView.adapter = orderHistoryAdapter
    }

    private fun setupActionBar() {
        setSupportActionBar(binding.toolbarOrderHistoryActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        }

        binding.toolbarOrderHistoryActivity.setNavigationOnClickListener { onBackPressed() }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}