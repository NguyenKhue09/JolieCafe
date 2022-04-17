package com.nt118.joliecafe.ui.fragments.home

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.nt118.joliecafe.R
import com.nt118.joliecafe.adapter.BestSallerAdapter
import com.nt118.joliecafe.adapter.CategorieAdapter
import com.nt118.joliecafe.adapter.DotIndicatorPager2Adapter
import com.nt118.joliecafe.adapter.SlideAdapter
import com.nt118.joliecafe.databinding.FragmentHomeBinding
import com.nt118.joliecafe.models.CategorieModel
import com.nt118.joliecafe.models.SliderItem
import com.nt118.joliecafe.ui.activities.notifications.Notification
import com.nt118.joliecafe.ui.activities.products.products
import com.nt118.joliecafe.viewmodels.home.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.abs

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var viewPager2: ViewPager2
    private val sliderHandle = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel by viewModels<HomeViewModel>()


        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

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

        viewPager2.clipToPadding = false
        viewPager2.clipChildren = false
        viewPager2.offscreenPageLimit = 3
        viewPager2.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER

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
            val intent = Intent(activity,Notification::class.java)
            startActivity(intent)
        }


        // change color search view


        // RecyclerView Categories
        val recyclerView = binding.recyclerView
        val categorieAdapter = CategorieAdapter(fetData())
        recyclerView.layoutManager = GridLayoutManager(context,4)
        recyclerView.adapter = categorieAdapter
        categorieAdapter.setOnClickListener(object : CategorieAdapter.onItemClickListener{
            override fun onItemClick(position: Int) {
                val intent = Intent(activity,products::class.java)
                intent.putExtra("position",position)
                startActivity(intent)
            }
        })

        // RecyclerView Best Seller
        val recyclerViewBS = binding.recyclerViewBestSeller
        val bestSallerAdapter = BestSallerAdapter(fetDataBestSaler(), requireActivity())
        recyclerViewBS.layoutManager = GridLayoutManager(requireContext(),1)
        recyclerViewBS.adapter = bestSallerAdapter


        return root
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
        item.add(CategorieModel("Coffee",R.drawable.ic_coffee))
        item.add(CategorieModel("Coffee",R.drawable.ic_coffee))
        item.add(CategorieModel("More",R.drawable.ic_coffee))

        return  item
    }

    private fun fetDataBestSaler() : ArrayList<String> {
        val item = ArrayList<String>()
        for (i in 0 until 5) {
            item.add("$i")
        }
        return item
    }

}