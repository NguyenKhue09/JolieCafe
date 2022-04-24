package com.nt118.joliecafe.ui.fragments.favorite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.tabs.TabLayout
import com.nt118.joliecafe.adapter.BestSallerAdapter
import com.nt118.joliecafe.adapter.FavoriteItemAdapter
import com.nt118.joliecafe.databinding.FragmentFavoriteBinding
import com.nt118.joliecafe.util.Constants.Companion.listTabContentFavorite
import com.nt118.joliecafe.viewmodels.favorite.FavoriteViewModel
import com.nt118.joliecafe.viewmodels.home.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
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
        val favoriteViewModel by viewModels<FavoriteViewModel>()

        _binding = FragmentFavoriteBinding.inflate(inflater, container, false)

        addTabContent()
        onTabSelected()
        recyclerViewLayout()
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

    //add layout

    private fun recyclerViewLayout() {
        val recyclerViewBS = binding.favoriteItemRecyclerView
        val bestSallerAdapter = FavoriteItemAdapter(fetDataBestSaler())
        recyclerViewBS.layoutManager = GridLayoutManager(context,1)
        recyclerViewBS.adapter = bestSallerAdapter
    }
    private fun fetDataBestSaler() : ArrayList<String> {
        val item = ArrayList<String>()
        for (i in 0 until 9) {
            item.add("$i")
        }
        return item
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}