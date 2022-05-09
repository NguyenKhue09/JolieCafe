package com.nt118.joliecafe.ui.fragments.cart

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.CircularProgressIndicator
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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    private lateinit var progressCart: CircularProgressIndicator
    private lateinit var emptyCartView: LinearLayout
    private lateinit var header2: FrameLayout
    private lateinit var rvCartSuggestion: RecyclerView
    private lateinit var suggestionContainer: LinearLayout
    private var isCartEmpty = true

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
        cartViewModel.numOfSelectedRv.asLiveData().observe(viewLifecycleOwner) {
            binding.cbCheckAll.isChecked = it == 4
        }

        val diffCallback = CartItemComparator
        val rvCoffee: RecyclerView = binding.rvCoffee
        cvCoffee = binding.cvCoffee
        val rvTea: RecyclerView = binding.rvTea
        cvTea= binding.cvTea
        val rvJuice: RecyclerView = binding.rvJuice
        cvJuice = binding.cvJuice
        val rvMilkTea: RecyclerView = binding.rvMilkTea
        cvMilkTea = binding.cvMilkTea
        val btnCheckout: Button = binding.btnCheckout
        progressCart = binding.progressCart
        emptyCartView = binding.emptyCartView
        header2 = binding.header2
        rvCartSuggestion = binding.rvCartSuggestion
        suggestionContainer = binding.suggestionContainer

        rvCartSuggestion.adapter = CartSuggestionAdapter()
        cartCoffeeAdapter = CartAdapter(diffCallback)
        cartTeaAdapter = CartAdapter(diffCallback)
        cartJuiceAdapter = CartAdapter(diffCallback)
        cartMilkTeaAdapter = CartAdapter(diffCallback)
        rvCoffee.adapter = cartCoffeeAdapter
        rvTea.adapter = cartTeaAdapter
        rvJuice.adapter = cartJuiceAdapter
        rvMilkTea.adapter = cartMilkTeaAdapter

        getData()
        checkboxHandler()
        checkAllHandler()

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
        cartViewModel.cartCount.asLiveData().observe(viewLifecycleOwner) {
            cartViewModel.numOfSelectedRv.value = it
            if (it >= 4) {
                cartEmptyHandler()
            }
        }

        cartCoffeeAdapter.addLoadStateListener {
            Log.d("CartFragment", "cartCount: ${cartViewModel.cartCount.value}")
            if (it.refresh is LoadState.Loading)
                return@addLoadStateListener

            if (cartCoffeeAdapter.itemCount != 0) {
                cvCoffee.visibility = View.VISIBLE
                isCartEmpty = false
            }
            else {
                cartViewModel.cartCount.value = cartViewModel.cartCount.value.plus(1)
            }
        }

        cartTeaAdapter.addLoadStateListener {
            Log.d("CartFragment", "cartCount: ${cartViewModel.cartCount.value}")
            if (it.refresh is LoadState.Loading)
                return@addLoadStateListener

            if (cartTeaAdapter.itemCount != 0) {
                cvTea.visibility = View.VISIBLE
                header2.visibility = View.VISIBLE
                isCartEmpty = false
            }
            else {
                cartViewModel.cartCount.value = cartViewModel.cartCount.value.plus(1)
            }
        }

        cartJuiceAdapter.addLoadStateListener {
            Log.d("CartFragment", "cartCount: ${cartViewModel.cartCount.value}")
            if (it.refresh is LoadState.Loading)
                return@addLoadStateListener

            if (cartJuiceAdapter.itemCount != 0) {
                cvJuice.visibility = View.VISIBLE
                header2.visibility = View.VISIBLE
                isCartEmpty = false
            }
            else {
                cartViewModel.cartCount.value = cartViewModel.cartCount.value.plus(1)
            }
        }

        cartMilkTeaAdapter.addLoadStateListener {
            Log.d("CartFragment", "cartCount: ${cartViewModel.cartCount.value}")
            if (it.refresh is LoadState.Loading)
                return@addLoadStateListener

            if (cartMilkTeaAdapter.itemCount != 0) {
                cvMilkTea.visibility = View.VISIBLE
                header2.visibility = View.VISIBLE
                isCartEmpty = false
            }
            else {
                cartViewModel.cartCount.value = cartViewModel.cartCount.value.plus(1)
            }
        }

        lifecycleScope.launch {
            cartViewModel.readUserToken.collectLatest { token ->
                cartViewModel.userToken = token

                withContext(Dispatchers.IO) {
                    val coffee = async { cartViewModel.getCartItems(token, "Coffee") }
                    val tea = async { cartViewModel.getCartItems(token, "Tea") }
                    val juice = async { cartViewModel.getCartItems(token, "Juice") }
                    val milkTea = async { cartViewModel.getCartItems(token, "Milk Tea") }


                    launch(Dispatchers.IO) {
                        tea.await().collect { data ->
                            cartTeaAdapter.submitData(lifecycle, data)
                        }
                    }

                    launch(Dispatchers.IO) {
                        coffee.await().collect { data ->
                            cartCoffeeAdapter.submitData(lifecycle, data)
                        }
                    }

                    launch(Dispatchers.IO) {
                        juice.await().collect { data ->
                            cartJuiceAdapter.submitData(lifecycle, data)
                        }
                    }

                    launch(Dispatchers.IO) {
                        milkTea.await().collect { data ->
                            cartMilkTeaAdapter.submitData(lifecycle, data)
                        }
                    }
                }
            }
        }
    }

    private fun cartEmptyHandler() {
        emptyCartView.visibility = View.VISIBLE
        progressCart.visibility = View.GONE
        rvCartSuggestion.visibility = View.VISIBLE
        suggestionContainer.visibility = View.VISIBLE
    }

    private fun checkboxHandler() {
        val cbTea = binding.cbTea
        val cbCoffee = binding.cbCoffee
        val cbJuice = binding.cbJuice
        val cbMilkTea = binding.cbMilkTea

        cbTea.setOnClickListener {
            if (cvTea.visibility == View.GONE) return@setOnClickListener
            if (cbTea.isChecked) {
                binding.rvTea.itemAnimator = null
                cartTeaAdapter.checkAllCheckbox()
                binding.rvTea.itemAnimator = DefaultItemAnimator()
                cartViewModel.numOfSelectedRv.value = cartViewModel.numOfSelectedRv.value.plus(1)
            } else {
                binding.rvTea.itemAnimator = null
                cartTeaAdapter.uncheckAllCheckbox()
                binding.rvTea.itemAnimator = DefaultItemAnimator()
                cartViewModel.numOfSelectedRv.value = cartViewModel.numOfSelectedRv.value.minus(1)
            }
        }

        cbCoffee.setOnClickListener {
            if (cvCoffee.visibility == View.GONE) return@setOnClickListener
            if (cbCoffee.isChecked) {
                binding.rvCoffee.itemAnimator = null
                cartCoffeeAdapter.checkAllCheckbox()
                binding.rvCoffee.itemAnimator = DefaultItemAnimator()
                cartViewModel.numOfSelectedRv.value = cartViewModel.numOfSelectedRv.value.plus(1)
            } else {
                binding.rvCoffee.itemAnimator = null
                cartCoffeeAdapter.uncheckAllCheckbox()
                binding.rvCoffee.itemAnimator = DefaultItemAnimator()
                cartViewModel.numOfSelectedRv.value = cartViewModel.numOfSelectedRv.value.minus(1)
            }
        }

        cbJuice.setOnClickListener {
            if (cvJuice.visibility == View.GONE) return@setOnClickListener
            if (cbJuice.isChecked) {
                binding.rvJuice.itemAnimator = null
                cartJuiceAdapter.checkAllCheckbox()
                binding.rvJuice.itemAnimator = DefaultItemAnimator()
                cartViewModel.numOfSelectedRv.value = cartViewModel.numOfSelectedRv.value.plus(1)
            } else {
                binding.rvJuice.itemAnimator = null
                cartJuiceAdapter.uncheckAllCheckbox()
                binding.rvJuice.itemAnimator = DefaultItemAnimator()
                cartViewModel.numOfSelectedRv.value = cartViewModel.numOfSelectedRv.value.minus(1)
            }
        }

        cbMilkTea.setOnClickListener {
            if (cvMilkTea.visibility == View.GONE) return@setOnClickListener
            if (cbMilkTea.isChecked) {
                binding.rvMilkTea.itemAnimator = null
                cartMilkTeaAdapter.checkAllCheckbox()
                binding.rvMilkTea.itemAnimator = DefaultItemAnimator()
                cartViewModel.numOfSelectedRv.value = cartViewModel.numOfSelectedRv.value.plus(1)
            } else {
                binding.rvMilkTea.itemAnimator = null
                cartMilkTeaAdapter.uncheckAllCheckbox()
                binding.rvMilkTea.itemAnimator = DefaultItemAnimator()
                cartViewModel.numOfSelectedRv.value = cartViewModel.numOfSelectedRv.value.minus(1)
            }
        }

        cartTeaAdapter.onSelectAllAction = {
            cbTea.isChecked = true
            cartViewModel.numOfSelectedRv.value = cartViewModel.numOfSelectedRv.value.plus(1)
        }
        cartTeaAdapter.onDeselectAllAction = {
            cbTea.isChecked = false
            cartViewModel.numOfSelectedRv.value = cartViewModel.numOfSelectedRv.value.minus(1)
        }
        cartCoffeeAdapter.onSelectAllAction = {
            cbCoffee.isChecked = true
            cartViewModel.numOfSelectedRv.value = cartViewModel.numOfSelectedRv.value.plus(1)
        }
        cartCoffeeAdapter.onDeselectAllAction = {
            cbCoffee.isChecked = false
            cartViewModel.numOfSelectedRv.value = cartViewModel.numOfSelectedRv.value.minus(1)
        }
        cartJuiceAdapter.onSelectAllAction = {
            cbJuice.isChecked = true
            cartViewModel.numOfSelectedRv.value = cartViewModel.numOfSelectedRv.value.plus(1)
        }
        cartJuiceAdapter.onDeselectAllAction = {
            cbJuice.isChecked = false
            cartViewModel.numOfSelectedRv.value = cartViewModel.numOfSelectedRv.value.minus(1)
        }
        cartMilkTeaAdapter.onSelectAllAction = {
            cbMilkTea.isChecked = true
            cartViewModel.numOfSelectedRv.value = cartViewModel.numOfSelectedRv.value.plus(1)
        }
        cartMilkTeaAdapter.onDeselectAllAction = {
            cbMilkTea.isChecked = false
            cartViewModel.numOfSelectedRv.value = cartViewModel.numOfSelectedRv.value.minus(1)
        }
    }

    private fun checkAllHandler() {
        binding.cbCheckAll.setOnClickListener {
            if (binding.cbCheckAll.isChecked) {
                if (!binding.cbCoffee.isChecked)
                    binding.cbCoffee.performClick()
                if (!binding.cbTea.isChecked)
                    binding.cbTea.performClick()
                if (!binding.cbJuice.isChecked)
                    binding.cbJuice.performClick()
                if (!binding.cbMilkTea.isChecked)
                    binding.cbMilkTea.performClick()
            } else {
                if(binding.cbCoffee.isChecked)
                    binding.cbCoffee.performClick()
                if(binding.cbTea.isChecked)
                    binding.cbTea.performClick()
                if(binding.cbJuice.isChecked)
                    binding.cbJuice.performClick()
                if(binding.cbMilkTea.isChecked)
                    binding.cbMilkTea.performClick()
            }
            Log.d("cartCount", "${cartViewModel.cartCount.value}")
            Log.d("checkAll", "${cartViewModel.numOfSelectedRv.value}")
        }
    }
}