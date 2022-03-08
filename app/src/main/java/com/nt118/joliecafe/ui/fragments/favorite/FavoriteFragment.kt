package com.nt118.joliecafe.ui.fragments.favorite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.tabs.TabLayout
import com.nt118.joliecafe.databinding.FragmentFavoriteBinding
import com.nt118.joliecafe.util.Constants.Companion.listTabContentFavorite
import com.nt118.joliecafe.viewmodels.FavoriteViewModel

class FavoriteFragment : Fragment() {

    private var _binding: FragmentFavoriteBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this)[FavoriteViewModel::class.java]

        _binding = FragmentFavoriteBinding.inflate(inflater, container, false)

        addTabContent()
        onTabSelected()
        return binding.root
    }

    private fun onTabSelected() {
        binding.tabLayoutFavorite.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab?) {
                println(tab!!.tag)
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // Handle tab reselect
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // Handle tab unselect
            }
        })
    }

    private fun addTabContent() {
        listTabContentFavorite.forEach {
            binding.tabLayoutFavorite.addTab(binding.tabLayoutFavorite.newTab().apply {
                tag = it
                text = it
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}