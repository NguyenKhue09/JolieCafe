package com.nt118.joliecafe.ui.fragments.favorite

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.nt118.joliecafe.R
import com.nt118.joliecafe.adapter.FavoriteItemAdapter
import com.nt118.joliecafe.databinding.FragmentFavoriteBinding
import com.nt118.joliecafe.models.FavoriteProduct
import com.nt118.joliecafe.ui.activities.login.LoginActivity
import com.nt118.joliecafe.util.ApiResult
import com.nt118.joliecafe.util.Constants.Companion.SNACK_BAR_STATUS_DISABLE
import com.nt118.joliecafe.util.Constants.Companion.SNACK_BAR_STATUS_ERROR
import com.nt118.joliecafe.util.Constants.Companion.SNACK_BAR_STATUS_SUCCESS
import com.nt118.joliecafe.util.Constants.Companion.listTabContentFavorite
import com.nt118.joliecafe.util.FavoriteProductComparator
import com.nt118.joliecafe.util.NetworkListener
import com.nt118.joliecafe.util.ProductComparator
import com.nt118.joliecafe.util.extenstions.observeOnce
import com.nt118.joliecafe.util.extenstions.setCustomBackground
import com.nt118.joliecafe.util.extenstions.setIcon
import com.nt118.joliecafe.viewmodels.favorite.FavoriteViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FavoriteFragment : Fragment() {


    private val favoriteViewModel by viewModels<FavoriteViewModel>()

    private var _binding: FragmentFavoriteBinding? = null
    private val binding get() = _binding!!

    private lateinit var networkListener: NetworkListener
    private lateinit var favoriteItemAdapter: FavoriteItemAdapter
    private lateinit var selectedTab: String

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

        networkListener = NetworkListener()

        val diffUtil = FavoriteProductComparator
        favoriteItemAdapter = FavoriteItemAdapter(
            favoriteFragment = this,
            diffUtil = diffUtil
        )

        updateNetworkStatus()
        updateBackOnlineStatus()
        observerNetworkMessage()

        addTabContent()
        onTabSelected()
        recyclerViewLayout()
        setFavoriteAdapterProducts()
        initTabPageData()
        handlePagingAdapterState()
        handleRemoveUserFavoriteProductResponse()

        return binding.root
    }

    private fun updateBackOnlineStatus() {
        favoriteViewModel.readBackOnline.asLiveData().observe(viewLifecycleOwner) {
            favoriteViewModel.backOnline = it
        }
    }

    private fun updateNetworkStatus() {
        networkListener.checkNetworkAvailability(requireContext())
            .asLiveData().observe(viewLifecycleOwner) { status ->
                favoriteViewModel.networkStatus = status
                favoriteViewModel.showNetworkStatus()
                backOnlineRecallFavoriteProducts()
            }
    }

    private fun initTabPageData() {
        lifecycleScope.launchWhenStarted {
            favoriteViewModel.getUserFavoriteProducts(
                productQuery = mapOf(
                    "type" to listTabContentFavorite[0]
                ),
                token = favoriteViewModel.userToken
            ).collectLatest { data ->
                selectedTab = listTabContentFavorite[0]
                submitFavoriteData(data = data)
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

    private fun observerNetworkMessage() {
        favoriteViewModel.networkMessage.observe(viewLifecycleOwner) { message ->
            if (!favoriteViewModel.networkStatus) {
                showSnackBar(
                    message = message,
                    status = SNACK_BAR_STATUS_DISABLE,
                    icon = R.drawable.ic_wifi_off
                )
            } else if (favoriteViewModel.networkStatus) {
                if (favoriteViewModel.backOnline) {
                    showSnackBar(
                        message = message,
                        status = SNACK_BAR_STATUS_SUCCESS,
                        icon = R.drawable.ic_wifi
                    )
                }
            }
        }
    }

    //add layout
    private fun recyclerViewLayout() {
        val recyclerViewBS = binding.favoriteItemRecyclerView
        recyclerViewBS.layoutManager = GridLayoutManager(context, 1)
        recyclerViewBS.adapter = favoriteItemAdapter
    }

    private fun backOnlineRecallFavoriteProducts() {
        lifecycleScope.launchWhenStarted {
            if (favoriteViewModel.backOnline) {
                favoriteViewModel.getUserFavoriteProducts(
                    productQuery = mapOf(
                        "type" to listTabContentFavorite[binding.tabLayoutFavorite.selectedTabPosition]
                    ),
                    token = favoriteViewModel.userToken
                ).collectLatest { data ->
                    selectedTab =
                        listTabContentFavorite[binding.tabLayoutFavorite.selectedTabPosition]
                    submitFavoriteData(data = data)
                }
            }
        }
    }

    private fun setFavoriteAdapterProducts() {
        favoriteViewModel.tabSelected.observe(viewLifecycleOwner) { tab ->
            if (favoriteViewModel.networkStatus) {
                lifecycleScope.launchWhenStarted {
                    favoriteViewModel.getUserFavoriteProducts(
                        productQuery = mapOf(
                            "type" to tab
                        ),
                        token = favoriteViewModel.userToken
                    ).collectLatest { data ->
                        selectedTab = tab
                        submitFavoriteData(data = data)
                    }
                }
            }
        }
    }

    private suspend fun submitFavoriteData(data: PagingData<FavoriteProduct>) {
        favoriteItemAdapter.submitData(data)
    }

    fun removeUserFavoriteProduct(favoriteProductId: String) {
        favoriteViewModel.removeUserFavoriteProduct(
            token = favoriteViewModel.userToken,
            favoriteProductId = favoriteProductId
        )
    }

    private fun handlePagingAdapterState() {
        favoriteItemAdapter.addLoadStateListener { loadState ->
            if (loadState.refresh is LoadState.Loading) {
                binding.favCircularProgressIndicator.visibility = View.VISIBLE
                binding.emptyFavList.root.visibility = View.INVISIBLE
            } else {
                checkFavItemEmpty()
                tabAppearance(tab = selectedTab)
                binding.favCircularProgressIndicator.visibility = View.INVISIBLE

                // getting the error
                val error = when {
                    loadState.prepend is LoadState.Error -> loadState.prepend as LoadState.Error
                    loadState.append is LoadState.Error -> loadState.append as LoadState.Error
                    loadState.refresh is LoadState.Error -> loadState.refresh as LoadState.Error
                    else -> null
                }
                error?.let {
                    //Toast.makeText(requireContext(), it.error.message, Toast.LENGTH_LONG).show()
                    if(favoriteViewModel.networkStatus) showSnackBar(
                        message = it.error.message!!,
                        status = SNACK_BAR_STATUS_ERROR,
                        icon = R.drawable.ic_error
                    )
                }
            }
        }
    }

    private fun tabAppearance(tab: String) {
        if (tab == listTabContentFavorite[0] && favoriteItemAdapter.itemCount == 0) {
            binding.tabLayoutFavorite.visibility = View.GONE
        } else {
            binding.tabLayoutFavorite.visibility = View.VISIBLE
        }
    }

    private fun checkFavItemEmpty() {
        if (favoriteItemAdapter.itemCount == 0) {
            binding.emptyFavList.root.visibility = View.VISIBLE
        } else {
            binding.emptyFavList.root.visibility = View.INVISIBLE
        }
    }

    private fun handleRemoveUserFavoriteProductResponse() {
        favoriteViewModel.removeUserFavoriteProductResponse.observe(viewLifecycleOwner) { result ->
            when (result) {
                is ApiResult.Loading -> {
                }
                is ApiResult.NullDataSuccess -> {
                    //Toast.makeText(requireContext(), "Remove favorite product successful", Toast.LENGTH_SHORT).show()
                    showSnackBar(
                        message = "Remove favorite product successful",
                        status = SNACK_BAR_STATUS_SUCCESS,
                        icon = R.drawable.ic_success
                    )
                    favoriteItemAdapter.refresh()
                }
                is ApiResult.Error -> {
                    //Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                    showSnackBar(
                        message = "Remove favorite product failed!",
                        status = SNACK_BAR_STATUS_ERROR,
                        icon = R.drawable.ic_error
                    )
                }
                else -> {}
            }
        }
    }

    private fun showSnackBar(message: String, status: Int, icon: Int) {
        val drawable = requireContext().getDrawable(icon)

        val snackBarContentColor = when (status) {
            SNACK_BAR_STATUS_SUCCESS -> R.color.text_color_2
            SNACK_BAR_STATUS_DISABLE -> R.color.dark_text_color
            SNACK_BAR_STATUS_ERROR -> R.color.error_color
            else -> R.color.text_color_2
        }


        val snackBar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction("Ok") {
            }
            .setActionTextColor(ContextCompat.getColor(requireContext(), R.color.grey_primary))
            .setTextColor(ContextCompat.getColor(requireContext(), snackBarContentColor))
            .setIcon(
                drawable = drawable!!,
                colorTint = ContextCompat.getColor(requireContext(), snackBarContentColor),
                iconPadding = resources.getDimensionPixelOffset(R.dimen.small_margin)
            )
            .setCustomBackground(requireContext().getDrawable(R.drawable.snackbar_normal_custom_bg)!!)

        snackBar.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStop() {
        super.onStop()
        networkListener.unregisterNetworkCallback()
    }
}