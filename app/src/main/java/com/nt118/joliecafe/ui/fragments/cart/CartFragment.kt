package com.nt118.joliecafe.ui.fragments.cart

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.nt118.joliecafe.adapter.CartAdapter
import com.nt118.joliecafe.databinding.FragmentCartBinding
import com.nt118.joliecafe.ui.activities.checkout.CheckoutActivity
import com.nt118.joliecafe.ui.activities.login.LoginActivity
import com.nt118.joliecafe.util.CartItemComparator
import com.nt118.joliecafe.util.NetworkListener
import com.nt118.joliecafe.viewmodels.cart.CartViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!
    private val currentUser: FirebaseUser? by lazy { FirebaseAuth.getInstance().currentUser }
    private lateinit var networkListener: NetworkListener
    private val cartViewModel by viewModels<CartViewModel>()
    private lateinit var cartAdapter: CartAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (currentUser == null) {
            startActivity(Intent(requireContext(), LoginActivity::class.java))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        val root: View = binding.root

        cartViewModel.readBackOnline.asLiveData().observe(viewLifecycleOwner) {
            cartViewModel.backOnline = it
        }

        val diffCallback = CartItemComparator
        val rvCartSuggestion: RecyclerView = binding.rvCartSuggestion
        val rvCoffee: RecyclerView = binding.rvCoffee
        val rvTea: RecyclerView = binding.rvTea
        val rvJuice: RecyclerView = binding.rvJuice
        val rvMilkTea: RecyclerView = binding.rvMilkTea
        val btnCheckout: Button = binding.btnCheckout
        cartAdapter = CartAdapter(requireActivity(), diffCallback)
        rvCoffee.adapter = cartAdapter
        //        rvTea.adapter = CartAdapter()
//        rvJuice.adapter = CartAdapter()
//        rvMilkTea.adapter = CartAdapter()

        lifecycleScope.launchWhenStarted {
            cartViewModel.readUserToken.collectLatest { token ->
                cartViewModel.userToken = token
                cartViewModel.getCartItems(cartViewModel.userToken).collectLatest { data ->
                    cartAdapter.submitData(data)
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            networkListener = NetworkListener()
            networkListener.checkNetworkAvailability(requireContext()).collect { status ->
                cartViewModel.networkStatus = status
                cartViewModel.showNetworkStatus()
                if (cartViewModel.backOnline) {
                    cartViewModel.getCartItems(cartViewModel.userToken).collectLatest { data ->
                        cartAdapter.submitData(data)
                    }
                }
            }
        }

        btnCheckout.setOnClickListener {
            startActivity(Intent(context, CheckoutActivity::class.java))
        }

        rvCartSuggestion.adapter = CartSuggestionAdapter()

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}