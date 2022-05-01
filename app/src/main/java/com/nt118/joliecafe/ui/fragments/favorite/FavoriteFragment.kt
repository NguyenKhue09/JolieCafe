package com.nt118.joliecafe.ui.fragments.favorite

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.nt118.joliecafe.adapter.FavoriteItemAdapter
import com.nt118.joliecafe.databinding.FragmentFavoriteBinding
import com.nt118.joliecafe.ui.activities.login.LoginActivity
import com.nt118.joliecafe.util.Constants.Companion.listTabContentFavorite
import com.nt118.joliecafe.util.NetworkListener
import com.nt118.joliecafe.util.ProductComparator
import com.nt118.joliecafe.viewmodels.favorite.FavoriteViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class FavoriteFragment : Fragment() {


    val favoriteViewModel by viewModels<FavoriteViewModel>()

    private var _binding: FragmentFavoriteBinding? = null
    private val binding get() = _binding!!

    private lateinit var networkListener: NetworkListener
    private lateinit var favoriteItemAdapter: FavoriteItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            startActivity(Intent(requireContext(), LoginActivity::class.java))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFavoriteBinding.inflate(inflater, container, false)

        favoriteViewModel.readBackOnline.asLiveData().observe(viewLifecycleOwner) {
            favoriteViewModel.backOnline = it
        }



        val diffUtil = ProductComparator
        favoriteItemAdapter = FavoriteItemAdapter(
            context = requireContext(),
            diffUtil = diffUtil
        )

        addTabContent()
        onTabSelected()
        recyclerViewLayout()
        return binding.root
    }

    private fun onTabSelected() {
        binding.tabLayoutFavorite.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab?) {
                setFavoriteAdapterProducts()
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                setFavoriteAdapterProducts()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                setFavoriteAdapterProducts()
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
        setFavoriteAdapterProducts()
        recyclerViewBS.layoutManager = GridLayoutManager(context,1)
        recyclerViewBS.adapter = favoriteItemAdapter
    }

    private fun setFavoriteAdapterProducts() {
        lifecycleScope.launchWhenStarted {
            networkListener = NetworkListener()
            networkListener.checkNetworkAvailability(requireContext())
                .collect { status ->
                    favoriteViewModel.networkStatus = status
                    favoriteViewModel.showNetworkStatus()
                    if(favoriteViewModel.backOnline) {
                        favoriteViewModel.getProducts(
                            productQuery = mapOf(
                                "type" to "Coffee"
                            ),
                            token = favoriteViewModel.userToken
                        ).collectLatest { data ->
                            favoriteItemAdapter.submitData(data)
                        }
                    }
                }
        }

        lifecycleScope.launchWhenStarted {
            favoriteViewModel.readUserToken.collectLatest { token ->
                favoriteViewModel.userToken = token
                favoriteViewModel.getProducts(
                    productQuery = mapOf(
                        "type" to "Coffee"
                    ),
                    token = token
                ).collectLatest { data ->
                    favoriteItemAdapter.submitData(data)
                }
            }
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}