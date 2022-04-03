package com.nt118.joliecafe.ui.activities.order_detail

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.nt118.joliecafe.R
import com.nt118.joliecafe.databinding.ActivityOrderDetailBinding

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

    }

    override fun onMapReady(googleMap: GoogleMap) {
        googleMap.addMarker(
            MarkerOptions()
                .position(LatLng(-34.0, 151.0))
                .title("Marker")
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}