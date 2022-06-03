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
import android.widget.TextView
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
import com.nt118.joliecafe.models.CartItemByCategory
import com.nt118.joliecafe.ui.activities.checkout.CheckoutActivity
import com.nt118.joliecafe.ui.activities.login.LoginActivity
import com.nt118.joliecafe.util.ApiResult
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
    private val MAX_TYPE = 6
    private val binding get() = _binding!!
    private val currentUser: FirebaseUser? by lazy { FirebaseAuth.getInstance().currentUser }
    private lateinit var networkListener: NetworkListener
    private val cartViewModel by viewModels<CartViewModel>()
    private lateinit var cartCoffeeAdapter: CartAdapter
    private lateinit var cartTeaAdapter: CartAdapter
    private lateinit var cartJuiceAdapter: CartAdapter
    private lateinit var cartMilkTeaAdapter: CartAdapter
    private lateinit var cartMilkShakeAdapter: CartAdapter
    private lateinit var cartPastyAdapter: CartAdapter
    private lateinit var cvTea: CardView
    private lateinit var cvCoffee: CardView
    private lateinit var cvJuice: CardView
    private lateinit var cvMilkTea: CardView
    private lateinit var cvMilkShake: CardView
    private lateinit var cvPasty: CardView
    private lateinit var progressCart: CircularProgressIndicator
    private lateinit var emptyCartView: LinearLayout
    private lateinit var header2: FrameLayout
    private lateinit var rvCartSuggestion: RecyclerView
    private lateinit var suggestionContainer: LinearLayout
    private lateinit var footer: FrameLayout
    private lateinit var tvItemCount: TextView
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
            binding.cbCheckAll.isChecked = it == (MAX_TYPE - cartViewModel.cartEmptyCount.value)
        }
        cartViewModel.itemCount.asLiveData().observe(viewLifecycleOwner) {
            tvItemCount.text = it.toString()
        }
        cartViewModel.deleteCartItemResponse.observe(viewLifecycleOwner) {
            when (it) {
                is ApiResult.Loading -> progressCart.visibility = View.VISIBLE
                is ApiResult.NullDataSuccess -> getData()
                else -> Log.d("CartFragment", "delete cart error: ${it.message}")
            }
        }
        cartViewModel.getCartItemV2Response.observe(viewLifecycleOwner) { response ->
            when (response) {
                is ApiResult.Loading -> {
                    progressCart.visibility = View.VISIBLE
                    cartViewModel.itemCount.value = 0
                    cartViewModel.numOfSelectedRv.value = 0
                    cartViewModel.cartEmptyCount.value = 0
                }
                is ApiResult.Success -> {
                    val data = response.data!!
                    if (data.isEmpty()) {
                        cartEmptyHandler()
                    } else {
                        progressCart.visibility = View.GONE
                        emptyCartView.visibility = View.GONE
                        suggestionContainer.visibility = View.GONE
                        header2.visibility = View.VISIBLE
                        footer.visibility = View.VISIBLE
                        fetchDataFromApi(data)
                    }
                }
                is ApiResult.Error -> {
                    progressCart.visibility = View.GONE
                    Log.d("CartFragment", "get cart error: ${response.message}")
                }
                else -> {}
            }
        }

        val rvCoffee: RecyclerView = binding.rvCoffee
        cvCoffee = binding.cvCoffee
        val rvTea: RecyclerView = binding.rvTea
        cvTea= binding.cvTea
        val rvJuice: RecyclerView = binding.rvJuice
        cvJuice = binding.cvJuice
        val rvMilkTea: RecyclerView = binding.rvMilkTea
        cvMilkTea = binding.cvMilkTea
        val rvMilkShake: RecyclerView = binding.rvMilkShake
        cvMilkShake = binding.cvMilkShake
        val rvPasty: RecyclerView = binding.rvPasty
        cvPasty = binding.cvPasty
        val btnCheckout: Button = binding.btnCheckout
        progressCart = binding.progressCart
        emptyCartView = binding.emptyCartView
        header2 = binding.header2
        footer = binding.footer
        rvCartSuggestion = binding.rvCartSuggestion
        suggestionContainer = binding.suggestionContainer
        tvItemCount = binding.tvItemCount

        rvCartSuggestion.adapter = CartSuggestionAdapter()
        cartCoffeeAdapter = CartAdapter(requireActivity(), cartViewModel, mutableListOf())
        cartTeaAdapter = CartAdapter(requireActivity(), cartViewModel, mutableListOf())
        cartJuiceAdapter = CartAdapter(requireActivity(), cartViewModel, mutableListOf())
        cartMilkTeaAdapter = CartAdapter(requireActivity(), cartViewModel, mutableListOf())
        cartMilkShakeAdapter = CartAdapter(requireActivity(), cartViewModel, mutableListOf())
        cartPastyAdapter = CartAdapter(requireActivity(), cartViewModel, mutableListOf())
        rvCoffee.adapter = cartCoffeeAdapter
        rvTea.adapter = cartTeaAdapter
        rvJuice.adapter = cartJuiceAdapter
        rvMilkTea.adapter = cartMilkTeaAdapter
        rvMilkShake.adapter = cartMilkShakeAdapter
        rvPasty.adapter = cartPastyAdapter

//        init()
        getData()
        checkboxHandler()
        checkAllHandler()

        lifecycleScope.launchWhenStarted {
            networkListener = NetworkListener()
            networkListener.checkNetworkAvailability(requireContext()).collect { status ->
                cartViewModel.networkStatus = status
                cartViewModel.showNetworkStatus()
                if (cartViewModel.backOnline) {
                    cartViewModel.getCartItemV2(cartViewModel.userToken)

//                    cartViewModel.getCartItems(cartViewModel.userToken, "Coffee").collectLatest { data ->
//                        cartCoffeeAdapter.submitData(lifecycle, data)
//                    }
//
//                    cartViewModel.getCartItems(cartViewModel.userToken, "Tea").collectLatest { data ->
//                        cartTeaAdapter.submitData(lifecycle, data)
//                    }
//
//                    cartViewModel.getCartItems(cartViewModel.userToken, "Juice").collectLatest { data ->
//                        cartJuiceAdapter.submitData(lifecycle, data)
//                    }
//
//                    cartViewModel.getCartItems(cartViewModel.userToken, "MilkTea").collectLatest { data ->
//                        cartMilkTeaAdapter.submitData(lifecycle, data)
//                    }
//
//                    cartViewModel.getCartItems(cartViewModel.userToken, "MilkShake").collectLatest { data ->
//                        cartMilkShakeAdapter.submitData(lifecycle, data)
//                    }
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

    private fun fetchDataFromApi(data: List<CartItemByCategory>) {
        data.find { it.type == "Coffee" }?.let {
            cvCoffee.visibility = View.VISIBLE
            cartCoffeeAdapter.fetchData(it.products)
            cartViewModel.itemCount.value += cartCoffeeAdapter.itemCount
        } ?: run { cartViewModel.cartEmptyCount.value += 1 }

        data.find { it.type == "Tea" }?.let {
            cvTea.visibility = View.VISIBLE
            cartTeaAdapter.fetchData(it.products)
            cartViewModel.itemCount.value += cartTeaAdapter.itemCount
        } ?: run { cartViewModel.cartEmptyCount.value += 1 }

        data.find { it.type == "Juice" }?.let {
            cvJuice.visibility = View.VISIBLE
            cartJuiceAdapter.fetchData(it.products)
            cartViewModel.itemCount.value += cartJuiceAdapter.itemCount
        } ?: run { cartViewModel.cartEmptyCount.value += 1 }

        data.find { it.type == "Milk tea" }?.let {
            cvMilkTea.visibility = View.VISIBLE
            cartMilkTeaAdapter.fetchData(it.products)
            cartViewModel.itemCount.value += cartMilkTeaAdapter.itemCount
        } ?: run { cartViewModel.cartEmptyCount.value += 1 }

        data.find { it.type == "Milk shake" }?.let {
            cvMilkShake.visibility = View.VISIBLE
            cartMilkShakeAdapter.fetchData(it.products)
            cartViewModel.itemCount.value += cartMilkShakeAdapter.itemCount
        } ?: run { cartViewModel.cartEmptyCount.value += 1 }

        data.find { it.type == "Pasty" }?.let {
            cvPasty.visibility = View.VISIBLE
            cartPastyAdapter.fetchData(it.products)
            cartViewModel.itemCount.value += cartPastyAdapter.itemCount
        } ?: run { cartViewModel.cartEmptyCount.value += 1 }
    }

    private fun init() {


//        cartViewModel.cartCount.asLiveData().observe(viewLifecycleOwner) {
//            if (it >= MAX_TYPE) {
//                progressCart.visibility = View.GONE
//            }
//        }
//
//        cartCoffeeAdapter.addLoadStateListener {
//            Log.d("CartFragment", "cartCount: ${cartViewModel.cartEmptyCount.value}")
//            if (it.refresh is LoadState.Loading)
//                return@addLoadStateListener
//
//            cartViewModel.cartCount.value = cartViewModel.cartCount.value.plus(1)
//            if (cartCoffeeAdapter.itemCount != 0) {
//                cvCoffee.visibility = View.VISIBLE
//                header2.visibility = View.VISIBLE
//                footer.visibility =View.VISIBLE
//                isCartEmpty = false
//                cartViewModel.itemCount.value = cartViewModel.itemCount.value.plus(cartCoffeeAdapter.itemCount)
//            }
//            else {
//                cartViewModel.cartEmptyCount.value = cartViewModel.cartEmptyCount.value.plus(1)
//            }
//        }
//
//        cartTeaAdapter.addLoadStateListener {
//            Log.d("CartFragment", "cartCount: ${cartViewModel.cartEmptyCount.value}")
//            if (it.refresh is LoadState.Loading)
//                return@addLoadStateListener
//
//            cartViewModel.cartCount.value = cartViewModel.cartCount.value.plus(1)
//            if (cartTeaAdapter.itemCount != 0) {
//                cvTea.visibility = View.VISIBLE
//                header2.visibility = View.VISIBLE
//                footer.visibility =View.VISIBLE
//                isCartEmpty = false
//                cartViewModel.itemCount.value = cartViewModel.itemCount.value.plus(cartTeaAdapter.itemCount)
//            }
//            else {
//                cartViewModel.cartEmptyCount.value = cartViewModel.cartEmptyCount.value.plus(1)
//            }
//        }
//
//        cartJuiceAdapter.addLoadStateListener {
//            Log.d("CartFragment", "cartCount: ${cartViewModel.cartEmptyCount.value}")
//            if (it.refresh is LoadState.Loading)
//                return@addLoadStateListener
//
//            cartViewModel.cartCount.value = cartViewModel.cartCount.value.plus(1)
//            if (cartJuiceAdapter.itemCount != 0) {
//                cvJuice.visibility = View.VISIBLE
//                header2.visibility = View.VISIBLE
//                footer.visibility =View.VISIBLE
//                isCartEmpty = false
//                cartViewModel.itemCount.value = cartViewModel.itemCount.value.plus(cartJuiceAdapter.itemCount)
//            }
//            else {
//                cartViewModel.cartEmptyCount.value = cartViewModel.cartEmptyCount.value.plus(1)
//            }
//        }
//
//        cartMilkTeaAdapter.addLoadStateListener {
//            Log.d("CartFragment", "cartCount: ${cartViewModel.cartEmptyCount.value}")
//            if (it.refresh is LoadState.Loading)
//                return@addLoadStateListener
//
//            cartViewModel.cartCount.value = cartViewModel.cartCount.value.plus(1)
//            if (cartMilkTeaAdapter.itemCount != 0) {
//                cvMilkTea.visibility = View.VISIBLE
//                header2.visibility = View.VISIBLE
//                footer.visibility =View.VISIBLE
//                isCartEmpty = false
//                cartViewModel.itemCount.value = cartViewModel.itemCount.value.plus(cartMilkTeaAdapter.itemCount)
//            }
//            else {
//                cartViewModel.cartEmptyCount.value = cartViewModel.cartEmptyCount.value.plus(1)
//            }
//        }
    }

    private fun getData() {

        cvJuice.visibility = View.GONE
        cvMilkTea.visibility = View.GONE
        cvTea.visibility = View.GONE
        cvCoffee.visibility = View.GONE
        cvMilkShake.visibility = View.GONE
        cvPasty.visibility = View.GONE

        lifecycleScope.launch {
            cartViewModel.readUserToken.collectLatest { token ->
                cartViewModel.itemCount.value = 0
//                cartViewModel.cartEmptyCount.value = 0
                cartViewModel.numOfSelectedRv.value = 0
//                cartViewModel.cartCount.value = 0
                cartViewModel.userToken = token

                withContext(Dispatchers.IO) {
                    cartViewModel.getCartItemV2(token)
//                    val coffee = async { cartViewModel.getCartItems(token, "Coffee") }
//                    val tea = async { cartViewModel.getCartItems(token, "Tea") }
//                    val juice = async { cartViewModel.getCartItems(token, "Juice") }
//                    val milkTea = async { cartViewModel.getCartItems(token, "Milk Tea") }
//
//
//                    launch(Dispatchers.IO) {
//                        tea.await().collect { data ->
//                            cartTeaAdapter.submitData(lifecycle, data)
//                        }
//                    }
//
//                    launch(Dispatchers.IO) {
//                        coffee.await().collect { data ->
//                            cartCoffeeAdapter.submitData(lifecycle, data)
//                        }
//                    }
//
//                    launch(Dispatchers.IO) {
//                        juice.await().collect { data ->
//                            cartJuiceAdapter.submitData(lifecycle, data)
//                        }
//                    }
//
//                    launch(Dispatchers.IO) {
//                        milkTea.await().collect { data ->
//                            cartMilkTeaAdapter.submitData(lifecycle, data)
//                        }
//                    }
                }
            }
        }
    }

    private fun cartEmptyHandler() {
        emptyCartView.visibility = View.VISIBLE
        rvCartSuggestion.visibility = View.VISIBLE
        suggestionContainer.visibility = View.VISIBLE
        header2.visibility = View.GONE
        footer.visibility = View.GONE
        progressCart.visibility = View.GONE
    }

    private fun checkboxHandler() {
        val cbTea = binding.cbTea
        val cbCoffee = binding.cbCoffee
        val cbJuice = binding.cbJuice
        val cbMilkTea = binding.cbMilkTea
        val cbMilkShake = binding.cbMilkShake
        val cbPasty = binding.cbPasty

        cbTea.isChecked = false
        cbCoffee.isChecked = false
        cbJuice.isChecked = false
        cbMilkTea.isChecked = false
        cbMilkShake.isChecked = false
        cbPasty.isChecked = false

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

        cbMilkShake.setOnClickListener {
            if (cvMilkShake.visibility == View.GONE) return@setOnClickListener
            if (cbMilkShake.isChecked) {
                binding.rvMilkShake.itemAnimator = null
                cartMilkShakeAdapter.checkAllCheckbox()
                binding.rvMilkShake.itemAnimator = DefaultItemAnimator()
                cartViewModel.numOfSelectedRv.value = cartViewModel.numOfSelectedRv.value.plus(1)
            } else {
                binding.rvMilkShake.itemAnimator = null
                cartMilkShakeAdapter.uncheckAllCheckbox()
                binding.rvMilkShake.itemAnimator = DefaultItemAnimator()
                cartViewModel.numOfSelectedRv.value = cartViewModel.numOfSelectedRv.value.minus(1)
            }
        }

        cbPasty.setOnClickListener {
            if (cvPasty.visibility == View.GONE) return@setOnClickListener
            if (cbPasty.isChecked) {
                binding.rvPasty.itemAnimator = null
                cartPastyAdapter.checkAllCheckbox()
                binding.rvPasty.itemAnimator = DefaultItemAnimator()
                cartViewModel.numOfSelectedRv.value = cartViewModel.numOfSelectedRv.value.plus(1)
            } else {
                binding.rvPasty.itemAnimator = null
                cartPastyAdapter.uncheckAllCheckbox()
                binding.rvPasty.itemAnimator = DefaultItemAnimator()
                cartViewModel.numOfSelectedRv.value = cartViewModel.numOfSelectedRv.value.minus(1)
            }
        }

        cartTeaAdapter.onSelectAllAction = {
            cbTea.isChecked = true
            cartViewModel.numOfSelectedRv.value = cartViewModel.numOfSelectedRv.value.plus(1)
        }
        cartTeaAdapter.onDeselectAllAction = {
            if (cbTea.isChecked)
                cartViewModel.numOfSelectedRv.value = cartViewModel.numOfSelectedRv.value.minus(1)
            cbTea.isChecked = false
        }
        cartCoffeeAdapter.onSelectAllAction = {
            cbCoffee.isChecked = true
            cartViewModel.numOfSelectedRv.value = cartViewModel.numOfSelectedRv.value.plus(1)
        }
        cartCoffeeAdapter.onDeselectAllAction = {
            if (cbCoffee.isChecked)
                cartViewModel.numOfSelectedRv.value = cartViewModel.numOfSelectedRv.value.minus(1)
            cbCoffee.isChecked = false
        }
        cartJuiceAdapter.onSelectAllAction = {
            cbJuice.isChecked = true
            cartViewModel.numOfSelectedRv.value = cartViewModel.numOfSelectedRv.value.plus(1)
        }
        cartJuiceAdapter.onDeselectAllAction = {
            if (cbJuice.isChecked)
                cartViewModel.numOfSelectedRv.value = cartViewModel.numOfSelectedRv.value.minus(1)
            cbJuice.isChecked = false
        }
        cartMilkTeaAdapter.onSelectAllAction = {
            cbMilkTea.isChecked = true
            cartViewModel.numOfSelectedRv.value = cartViewModel.numOfSelectedRv.value.plus(1)
        }
        cartMilkTeaAdapter.onDeselectAllAction = {
            if (cbMilkTea.isChecked)
                cartViewModel.numOfSelectedRv.value = cartViewModel.numOfSelectedRv.value.minus(1)
            cbMilkTea.isChecked = false
        }
        cartMilkShakeAdapter.onSelectAllAction = {
            cbMilkShake.isChecked = true
            cartViewModel.numOfSelectedRv.value = cartViewModel.numOfSelectedRv.value.plus(1)
        }
        cartMilkShakeAdapter.onDeselectAllAction = {
            if (cbMilkShake.isChecked)
                cartViewModel.numOfSelectedRv.value = cartViewModel.numOfSelectedRv.value.minus(1)
            cbMilkShake.isChecked = false
        }
        cartPastyAdapter.onSelectAllAction = {
            cbPasty.isChecked = true
            cartViewModel.numOfSelectedRv.value = cartViewModel.numOfSelectedRv.value.plus(1)
        }
        cartPastyAdapter.onDeselectAllAction = {
            if (cbPasty.isChecked)
                cartViewModel.numOfSelectedRv.value = cartViewModel.numOfSelectedRv.value.minus(1)
            cbPasty.isChecked = false
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
                if (!binding.cbMilkShake.isChecked)
                    binding.cbMilkShake.performClick()
                if (!binding.cbPasty.isChecked)
                    binding.cbPasty.performClick()
                Log.d("checkAll", "numOfSelectedRv: ${cartViewModel.numOfSelectedRv.value}")
                Log.d("checkAll", "emptyCount: ${cartViewModel.cartEmptyCount.value}")
            } else {
                if(binding.cbCoffee.isChecked)
                    binding.cbCoffee.performClick()
                if(binding.cbTea.isChecked)
                    binding.cbTea.performClick()
                if(binding.cbJuice.isChecked)
                    binding.cbJuice.performClick()
                if(binding.cbMilkTea.isChecked)
                    binding.cbMilkTea.performClick()
                if(binding.cbMilkShake.isChecked)
                    binding.cbMilkShake.performClick()
                if(binding.cbPasty.isChecked)
                    binding.cbPasty.performClick()
                Log.d("checkAll", "numOfSelectedRv: ${cartViewModel.numOfSelectedRv.value}")
                Log.d("checkAll", "emptyCount: ${cartViewModel.cartEmptyCount.value}")
            }
//            Log.d("cartCount", "${cartViewModel.cartEmptyCount.value}")
            Log.d("checkAll", "${cartViewModel.numOfSelectedRv.value}")
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        binding.cbTea.apply {
            isChecked = false
            jumpDrawablesToCurrentState()
        }
        binding.cbCoffee.apply {
            isChecked = false
            jumpDrawablesToCurrentState()
        }
        binding.cbJuice.apply {
            isChecked = false
            jumpDrawablesToCurrentState()
        }
        binding.cbMilkTea.apply {
            isChecked = false
            jumpDrawablesToCurrentState()
        }
        binding.cbMilkShake.apply {
            isChecked = false
            jumpDrawablesToCurrentState()
        }
        binding.cbPasty.apply {
            isChecked = false
            jumpDrawablesToCurrentState()
        }
    }

}