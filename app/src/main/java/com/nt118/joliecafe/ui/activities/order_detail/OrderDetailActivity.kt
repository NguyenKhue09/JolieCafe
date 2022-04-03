package com.nt118.joliecafe.ui.activities.order_detail

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.nt118.joliecafe.R
import com.nt118.joliecafe.databinding.ActivityOrderDetailBinding
import com.nt118.joliecafe.ui.fragments.cart.CartSuggestionAdapter

class OrderDetailActivity : AppCompatActivity(), OnMapReadyCallback {
    private var _binding: ActivityOrderDetailBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityOrderDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val btnNavBack: ImageButton = binding.btnNavBack
        val rvCartSuggestion: RecyclerView = binding.rvCartSuggestion
        val tvDistanceDetail: TextView = binding.tvDistanceDetail
        val tvTimeDetail: TextView = binding.tvTimeDetail

        btnNavBack.setOnClickListener {
            finish()
        }

        tvDistanceDetail.text = resources.getString(R.string.distance_left, 100)
        tvTimeDetail.text = resources.getString(R.string.time_left, 5)

        rvCartSuggestion.adapter = CartSuggestionAdapter()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val dhcntt = LatLng(10.8700460960419, 106.80308186835796)
        googleMap.addMarker(
            MarkerOptions()
                .position(dhcntt)
                .title("Your order")
        )
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(dhcntt, 20f))
//        googleMap.moveCamera(CameraUpdateFactory.zoomBy(20f))
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}