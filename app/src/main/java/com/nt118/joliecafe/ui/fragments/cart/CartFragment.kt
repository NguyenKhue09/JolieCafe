package com.nt118.joliecafe.ui.fragments.cart

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.cardview.widget.CardView
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!
    private val currentUser: FirebaseUser? by lazy { FirebaseAuth.getInstance().currentUser }
    private lateinit var networkListener: NetworkListener
    private val cartViewModel by viewModels<CartViewModel>()
    private lateinit var cartCoffeeAdapter: CartAdapter
    private lateinit var cartTeaAdapter: CartAdapter
    private lateinit var cartJuiceAdapter: CartAdapter
    private lateinit var cartMilkTeaAdapter: CartAdapter
    private lateinit var cvTea: CardView
    private lateinit var cvCoffee: CardView
    private lateinit var cvJuice: CardView
    private lateinit var cvMilkTea: CardView

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
        cvCoffee = binding.cvCoffee
        val rvTea: RecyclerView = binding.rvTea
        cvTea= binding.cvTea
        val rvJuice: RecyclerView = binding.rvJuice
        cvJuice = binding.cvJuice
        val rvMilkTea: RecyclerView = binding.rvMilkTea
        cvMilkTea = binding.cvMilkTea
        val btnCheckout: Button = binding.btnCheckout
        cartCoffeeAdapter = CartAdapter(requireActivity(), diffCallback)
        cartTeaAdapter = CartAdapter(requireActivity(), diffCallback)
        cartJuiceAdapter = CartAdapter(requireActivity(), diffCallback)
        cartMilkTeaAdapter = CartAdapter(requireActivity(), diffCallback)
        rvCoffee.adapter = cartCoffeeAdapter
        rvTea.adapter = cartTeaAdapter
        rvJuice.adapter = cartJuiceAdapter
        rvMilkTea.adapter = cartMilkTeaAdapter

        getData()

        lifecycleScope.launchWhenStarted {
            networkListener = NetworkListener()
            networkListener.checkNetworkAvailability(requireContext()).collect { status ->
                cartViewModel.networkStatus = status
                cartViewModel.showNetworkStatus()
                if (cartViewModel.backOnline) {
                    cartViewModel.getCartItems(cartViewModel.userToken, "Coffee").collectLatest { data ->
                        cartCoffeeAdapter.submitData(data)
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

    private fun getData() {
        lifecycleScope.launch {
            cartViewModel.readUserToken.collectLatest { token ->
                cartViewModel.userToken = token

                coroutineScope {
                    val coffee = async { cartViewModel.getCartItems(token, "Coffee") }
                    val tea = async { cartViewModel.getCartItems(token, "Tea") }
                    val juice = async { cartViewModel.getCartItems(token, "Juice") }
                    val milkTea = async { cartViewModel.getCartItems(token, "Milk Tea") }


                    launch(Dispatchers.IO) {
                        tea.await().collect { data ->
                            cartTeaAdapter.submitData(lifecycle, data)
                            cartTeaAdapter.addLoadStateListener {
                                if (cartTeaAdapter.itemCount != 0) {
                                    cvTea.visibility = View.VISIBLE
                                }
                            }
                        }
                    }

                    launch(Dispatchers.IO) {
                        coffee.await().collect { data ->
                            cartCoffeeAdapter.submitData(lifecycle, data)
                            cartCoffeeAdapter.addLoadStateListener {
                                if (cartCoffeeAdapter.itemCount != 0) {
                                    cvCoffee.visibility = View.VISIBLE
                                }
                            }
                        }
                    }

                    launch(Dispatchers.IO) {
                        juice.await().collect { data ->
                            cartJuiceAdapter.submitData(lifecycle, data)
                            cartJuiceAdapter.addLoadStateListener {
                                if (cartJuiceAdapter.itemCount != 0) {
                                    cvJuice.visibility = View.VISIBLE
                                }
                            }
                        }
                    }

                    launch(Dispatchers.IO) {
                        milkTea.await().collect { data ->
                            cartMilkTeaAdapter.submitData(lifecycle, data)
                            cartMilkTeaAdapter.addLoadStateListener {
                                if (cartMilkTeaAdapter.itemCount != 0) {
                                    cvMilkTea.visibility = View.VISIBLE
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}