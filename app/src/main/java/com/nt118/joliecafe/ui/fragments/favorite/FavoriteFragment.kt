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
import com.nt118.joliecafe.util.FavoriteProductComparator
import com.nt118.joliecafe.util.NetworkListener
import com.nt118.joliecafe.util.ProductComparator
import com.nt118.joliecafe.util.extenstions.observeOnce
import com.nt118.joliecafe.viewmodels.favorite.FavoriteViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class FavoriteFragment : Fragment() {


    private val favoriteViewModel by viewModels<FavoriteViewModel>()

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


        val diffUtil = FavoriteProductComparator
        favoriteItemAdapter = FavoriteItemAdapter(
            context = requireContext(),
            diffUtil = diffUtil
        )

        addTabContent()
        onTabSelected()
        recyclerViewLayout()
        setFavoriteAdapterProducts()
        initTabPageData()

        return binding.root
    }

    private fun initTabPageData() {
        networkListener = NetworkListener()
        networkListener.checkNetworkAvailability(requireContext())
            .asLiveData().observeOnce(viewLifecycleOwner) { status ->
                favoriteViewModel.networkStatus = status
                if (favoriteViewModel.networkStatus) {
                    lifecycleScope.launchWhenStarted {
                        favoriteViewModel.getUserFavoriteProducts(
                            productQuery = mapOf(
                                "type" to listTabContentFavorite[0]
                            ),
                            token = favoriteViewModel.userToken
                        ).collectLatest { data ->
                            favoriteItemAdapter.submitData(data)
                        }
                    }
                } else {
                    favoriteViewModel.showNetworkStatus()
                }
            }
    }

    private fun onTabSelected() {
        binding.tabLayoutFavorite.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab != null) {
                    favoriteViewModel.setTabSelected(tab = tab.text.toString())
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
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
        recyclerViewBS.layoutManager = GridLayoutManager(context, 1)
        recyclerViewBS.adapter = favoriteItemAdapter
    }

    private fun setFavoriteAdapterProducts() {
        lifecycleScope.launchWhenStarted {
            networkListener = NetworkListener()
            networkListener.checkNetworkAvailability(requireContext())
                .collect { status ->
                    favoriteViewModel.networkStatus = status
                    favoriteViewModel.showNetworkStatus()
                    if (favoriteViewModel.backOnline) {
                        favoriteViewModel.getUserFavoriteProducts(
                            productQuery = mapOf(
                                "type" to listTabContentFavorite[binding.tabLayoutFavorite.selectedTabPosition]
                            ),
                            token = favoriteViewModel.userToken
                        ).collectLatest { data ->
                            favoriteItemAdapter.submitData(data)
                        }
                    }
                }
        }

        favoriteViewModel.tabSelected.observe(viewLifecycleOwner) { tab ->
            if (favoriteViewModel.networkStatus) {
                lifecycleScope.launchWhenStarted {
                    favoriteViewModel.getUserFavoriteProducts(
                        productQuery = mapOf(
                            "type" to tab
                        ),
                        token = favoriteViewModel.userToken
                    ).collectLatest { data ->
                        favoriteItemAdapter.submitData(data)
                    }
                }
            } else {
                favoriteViewModel.showNetworkStatus()
            }
        }

        lifecycleScope.launchWhenStarted {
            favoriteViewModel.readUserToken.collectLatest { token ->
                favoriteViewModel.userToken = token
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}