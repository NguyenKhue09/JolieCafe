package com.nt118.joliecafe.ui.fragments.home

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import coil.load
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.nt118.joliecafe.R
import com.nt118.joliecafe.adapter.BestSellerAdapter
import com.nt118.joliecafe.adapter.CategorieAdapter
import com.nt118.joliecafe.adapter.DotIndicatorPager2Adapter
import com.nt118.joliecafe.adapter.SlideAdapter
import com.nt118.joliecafe.databinding.FragmentHomeBinding
import com.nt118.joliecafe.models.CategorieModel
import com.nt118.joliecafe.models.SliderItem
import com.nt118.joliecafe.models.User
import com.nt118.joliecafe.ui.activities.login.LoginActivity
import com.nt118.joliecafe.ui.activities.notifications.NotificationActivity
import com.nt118.joliecafe.ui.activities.products.ProductsActivity
import com.nt118.joliecafe.util.ApiResult
import com.nt118.joliecafe.util.Constants
import com.nt118.joliecafe.util.Constants.Companion.SNACK_BAR_STATUS_ERROR
import com.nt118.joliecafe.util.NetworkListener
import com.nt118.joliecafe.util.ProductComparator
import com.nt118.joliecafe.util.extenstions.setCustomBackground
import com.nt118.joliecafe.util.extenstions.setIcon
import com.nt118.joliecafe.viewmodels.home.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.abs


@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val homeViewModel by viewModels<HomeViewModel>()
    private var currentUser: FirebaseUser? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var networkListener: NetworkListener

    private lateinit var viewPager2: ViewPager2
    private val sliderHandle = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            startActivity(Intent(requireContext(), LoginActivity::class.java))
        }

        lifecycleScope.launchWhenStarted {
            homeViewModel.readIsUserFaceOrGGLogin.collectLatest {
                homeViewModel.isFaceOrGGLogin = it
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        homeViewModel.readBackOnline.asLiveData().observe(viewLifecycleOwner) {
            homeViewModel.backOnline = it
        }

        binding.toolbarHome.searchView.setOnClickListener {
            if (!binding.toolbarHome.searchView.query.isNullOrEmpty()) {
                val intent = Intent(context, ProductsActivity::class.java)
                intent.putExtra("position", 0)
                intent.putExtra("search", binding.toolbarHome.searchView.query.toString())
                startActivity(intent)
            } else {
                val intent = Intent(context, ProductsActivity::class.java)
                intent.putExtra("position", 0)
                startActivity(intent)
            }
            binding.toolbarHome.searchView.clearFocus()
        }

        binding.toolbarHome.searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                println("onQueryTextSubmit")
                println(query)
                if(!query.isNullOrEmpty()) {
                    val intent = Intent(context, ProductsActivity::class.java)
                    intent.putExtra("position", 0)
                    intent.putExtra("search", query)
                    startActivity(intent)
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {

                return false
            }
        })



        viewPager2 = binding.viewpagerImageSlider
        val dotsIndicator = binding.dotsIndicator
        val adapter = DotIndicatorPager2Adapter()
        viewPager2.adapter = adapter
        dotsIndicator.setViewPager2(viewPager2)

        val silderItems: MutableList<SliderItem> = ArrayList()
        silderItems.add(SliderItem(R.drawable.a))
        silderItems.add(SliderItem(R.drawable.b))
        silderItems.add(SliderItem(R.drawable.c))
        silderItems.add(SliderItem(R.drawable.d))
        silderItems.add(SliderItem(R.drawable.e))

        viewPager2.adapter = SlideAdapter(silderItems, viewPager2)

        viewPager2.apply {
            clipToPadding = false
            clipChildren = false
            offscreenPageLimit = 3
            getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        }

        val compositePageTransformer = CompositePageTransformer()
        compositePageTransformer.addTransformer(MarginPageTransformer(30))
        compositePageTransformer.addTransformer { page, position ->
            val r = 1 - abs(position)
            page.scaleY = 0.85f + r * 0.25f
        }

        viewPager2.setPageTransformer(compositePageTransformer)
        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                sliderHandle.removeCallbacks(sliderRunnable)
                sliderHandle.postDelayed(sliderRunnable, 3000)
            }
        })

        //open  notification
        binding.toolbarHome.imgNotification.setOnClickListener {
            val intent = Intent(activity,NotificationActivity::class.java)
            startActivity(intent)
        }



        // RecyclerView Categories
        val recyclerView = binding.recyclerView
        val categorieAdapter = CategorieAdapter(fetData())
        recyclerView.layoutManager = GridLayoutManager(context,4)
        recyclerView.adapter = categorieAdapter
        categorieAdapter.setOnClickListener(object : CategorieAdapter.onItemClickListener{
            override fun onItemClick(position: Int) {
                if(position == 7)
                {
                    val intent = Intent(activity, ProductsActivity::class.java)
                    intent.putExtra("position", 0)
                    startActivity(intent)
                } else {
                    val intent = Intent(activity, ProductsActivity::class.java)
                    intent.putExtra("position", position)
                    startActivity(intent)
                }
            }
        })

        // RecyclerView Best Seller
        val diffCallBack = ProductComparator
        val recyclerViewBS = binding.recyclerViewBestSeller
        val bestSellerAdapter = BestSellerAdapter(requireActivity(), diffCallBack = diffCallBack, binding.root)
        recyclerViewBS.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerViewBS.adapter = bestSellerAdapter

        lifecycleScope.launchWhenStarted {
            networkListener = NetworkListener()
            networkListener.checkNetworkAvailability(requireContext())
                .collect { status ->
                    homeViewModel.networkStatus = status
                    homeViewModel.showNetworkStatus()
                    if(homeViewModel.backOnline) {
                        homeViewModel.getProducts(
                            productQuery = mapOf(
                                "type" to "All"
                            ),
                            token = homeViewModel.userToken
                        ).collectLatest { data ->
                            bestSellerAdapter.submitData(data)
                        }
                    }
                }
        }

        lifecycleScope.launchWhenStarted {
            homeViewModel.readUserToken.collectLatest { token ->
                homeViewModel.userToken = token
                homeViewModel.getUserInfos(homeViewModel.userToken)
                homeViewModel.getProducts(
                    productQuery = mapOf(
                        "type" to "All"
                    ),
                    token = token
                ).collectLatest { data ->
                    bestSellerAdapter.submitData(data)
                }
            }
        }

        bestSellerAdapter.addLoadStateListener { loadState ->
            if (loadState.refresh is LoadState.Loading){
                binding.homeCircularProgressIndicator.visibility = View.VISIBLE
            }
            else{
                binding.homeCircularProgressIndicator.visibility = View.INVISIBLE
                // getting the error
                val error = when {
                    loadState.prepend is LoadState.Error -> loadState.prepend as LoadState.Error
                    loadState.append is LoadState.Error -> loadState.append as LoadState.Error
                    loadState.refresh is LoadState.Error -> loadState.refresh as LoadState.Error
                    else -> null
                }
                error?.let {
                    showSnackBar(it.error.message!!, SNACK_BAR_STATUS_ERROR, R.drawable.ic_error)
                }
            }
        }


        homeViewModel.getUserInfosResponse.observe(viewLifecycleOwner) { useData ->
            when(useData) {
                is ApiResult.Success -> {
                    setUserData(user = useData.data!!)
                }
                is ApiResult.Error -> {
                    showSnackBar("Get your data infos failed!. Please login again", SNACK_BAR_STATUS_ERROR, R.drawable.ic_error)
                }
                else -> {}
            }
        }

        return root
    }

    private fun setUserData(user: User) {
        val toolbar = binding.toolbarHome
        if (homeViewModel.isFaceOrGGLogin) {
            toolbar.tvName.text = currentUser?.displayName ?: "You"
            toolbar.imgAvatar.load(
                data = currentUser?.photoUrl
            ) {
                placeholder(R.drawable.placeholder_image)
                crossfade(600)
                error(R.drawable.placeholder_image)
            }
        } else {
            toolbar.tvName.text = user.fullName
            toolbar.imgAvatar.load(
                data = user.thumbnail
            ) {
                placeholder(R.drawable.placeholder_image)
                crossfade(600)
                error(R.drawable.placeholder_image)
            }
        }
        toolbar.tvScores.text = user.coins.toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private val sliderRunnable = Runnable {
        if (viewPager2.currentItem == 4) {
            viewPager2.currentItem = 0
        } else {
            viewPager2.currentItem = viewPager2.currentItem + 1
        }
    }

    override fun onPause() {
        super.onPause()
        sliderHandle.postDelayed(sliderRunnable, 3000)
    }

    override fun onResume() {
        super.onResume()
        sliderHandle.postDelayed(sliderRunnable, 3000)
    }

    private fun fetData() : ArrayList<CategorieModel>{
        val item = ArrayList<CategorieModel>()

        item.add(CategorieModel("All",R.drawable.ic_star))
        item.add(CategorieModel("Coffee",R.drawable.ic_coffee))
        item.add(CategorieModel("Tea",R.drawable.ic_leaf))
        item.add(CategorieModel("Juice",R.drawable.ic_watermelon))
        item.add(CategorieModel("Pastry",R.drawable.ic_croissant_svgrepo_com))
        item.add(CategorieModel("Milk shake",R.drawable.milkshake))
        item.add(CategorieModel("Milk tea",R.drawable.bubble_tea))
        item.add(CategorieModel("More",R.drawable.ic_more))

        return  item
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null

    }

    override fun onStop() {
        super.onStop()
        unregisterNetworkCallback()
    }

    private fun unregisterNetworkCallback() {
        try {
            networkListener.unregisterNetworkCallback()
        } catch (e: Exception) {
            // i.e. NetworkCallback was already unregistered
        }
    }

    private fun showSnackBar(message: String, status: Int, icon: Int) {
        val drawable = requireContext().getDrawable(icon)

        val snackBarContentColor = when (status) {
            Constants.SNACK_BAR_STATUS_SUCCESS -> R.color.text_color_2
            Constants.SNACK_BAR_STATUS_DISABLE -> R.color.dark_text_color
            Constants.SNACK_BAR_STATUS_ERROR -> R.color.error_color
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
}