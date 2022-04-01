package com.nt118.joliecafe.ui.activities.notifications

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import com.nt118.joliecafe.R
import com.nt118.joliecafe.adapter.FavoriteItemAdapter
import com.nt118.joliecafe.adapter.NotificationAdapter
import com.nt118.joliecafe.databinding.ActivityNotificationBinding
import com.nt118.joliecafe.databinding.ActivityProfileBinding

class Notification : AppCompatActivity() {
    private var _binding: ActivityNotificationBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        recyclerViewLayout()

        //back home
        binding.iconBackHome.setOnClickListener {
            onBackPressed()
        }
    }

    private fun recyclerViewLayout() {
        val recyclerViewNotificationAdapter = binding.recyclerViewNotification
        val notificationAdapter = NotificationAdapter(fetDataBestSaler())
        recyclerViewNotificationAdapter.layoutManager = GridLayoutManager(this,1)
        recyclerViewNotificationAdapter.adapter = notificationAdapter
    }

    private fun fetDataBestSaler() : ArrayList<String> {
        val item = ArrayList<String>()
        for (i in 0 until 9) {
            item.add("$i")
        }
        return item
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}