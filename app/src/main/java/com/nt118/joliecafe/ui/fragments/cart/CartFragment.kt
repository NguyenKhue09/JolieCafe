package com.nt118.joliecafe.ui.fragments.cart

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.nt118.joliecafe.databinding.FragmentCartBinding
import com.nt118.joliecafe.ui.activities.checkout.CheckoutActivity
import com.nt118.joliecafe.viewmodels.cart.CartViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val notificationsViewModel by viewModels<CartViewModel>()

        _binding = FragmentCartBinding.inflate(inflater, container, false)
        val root: View = binding.root

//        val textView: TextView = binding.textNotifications
//        notificationsViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }

        val rvCartSuggestion: RecyclerView = binding.rvCartSuggestion
        val rvCoffee: RecyclerView = binding.rvCoffee
        val rvTea: RecyclerView = binding.rvTea
        val rvJuice: RecyclerView = binding.rvJuice
        val rvMilkTea: RecyclerView = binding.rvMilkTea
        val btnCheckout: Button = binding.btnCheckout

        btnCheckout.setOnClickListener {
            startActivity(Intent(context, CheckoutActivity::class.java))
        }

        rvCartSuggestion.adapter = CartSuggestionAdapter()
        rvCoffee.adapter = CartAdapter()
        rvTea.adapter = CartAdapter()
        rvJuice.adapter = CartAdapter()
        rvMilkTea.adapter = CartAdapter()
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}