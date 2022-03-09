package com.nt118.joliecafe.ui.fragments.home

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.nt118.joliecafe.R
import com.nt118.joliecafe.databinding.FragmentHomeBinding
import com.nt118.joliecafe.viewmodels.HomeViewModel
import kotlin.math.abs


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
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

//        val textView: TextView = binding.textHome
//        homeViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }

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


        // change color search view


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
}