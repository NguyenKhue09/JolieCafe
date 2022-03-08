package com.nt118.joliecafe.ui.fragments.cart

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.nt118.joliecafe.R
import com.nt118.joliecafe.viewmodels.CartViewModel
import com.nt118.joliecafe.databinding.FragmentCartBinding

class CartFragment : Fragment() {

    private lateinit var _binding: FragmentCartBinding

    private val binding get() = _binding

    private lateinit var viewModel: CartViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[CartViewModel::class.java]

        _binding = FragmentCartBinding.inflate(inflater, container, false)
        val root: View = _binding.root

        val tvCartFrag: TextView = binding.tvCartFrag
        viewModel.text.observe(viewLifecycleOwner) {
            tvCartFrag.text = it
        }

        return root
    }

}