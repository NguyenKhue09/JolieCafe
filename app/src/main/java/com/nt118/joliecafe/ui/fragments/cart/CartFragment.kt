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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.gson.Gson
import com.nt118.joliecafe.R
import com.nt118.joliecafe.adapter.CartAdapter
import com.nt118.joliecafe.databinding.FragmentCartBinding
import com.nt118.joliecafe.models.CartItem
import com.nt118.joliecafe.models.CartItemByCategory
import com.nt118.joliecafe.ui.activities.checkout.CheckoutActivity
import com.nt118.joliecafe.ui.activities.login.LoginActivity
import com.nt118.joliecafe.util.*
import com.nt118.joliecafe.util.Constants.Companion.SNACK_BAR_STATUS_DISABLE
import com.nt118.joliecafe.util.Constants.Companion.SNACK_BAR_STATUS_SUCCESS
import com.nt118.joliecafe.util.extenstions.setCustomBackground
import com.nt118.joliecafe.util.extenstions.setIcon
import com.nt118.joliecafe.viewmodels.cart.CartViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest

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
    private lateinit var tvTotalPrice: TextView
    private lateinit var tvDelete: TextView
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
        cartViewModel.totalCost.observe(viewLifecycleOwner) {
            tvTotalPrice.text = NumberUtil.addSeparator(it)
            binding.btnCheckout.isEnabled = it > 0
            tvDelete.visibility = if (it > 0) View.VISIBLE else View.GONE
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
                        cartViewModel.totalCost.value = 0.0
                        cartViewModel.cartEmptyCount.value = 0
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
        tvTotalPrice = binding.tvTotalPrice
        tvDelete = binding.tvDelete

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
        observeNetworkMessage()
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
                }
            }
        }

        btnCheckout.setOnClickListener {
            val intent = Intent(context, CheckoutActivity::class.java)
            val selectedCartItems = getSelectedCartItem()
            intent.putExtra("cartItems", Gson().toJson(selectedCartItems))
            startActivity(intent)
        }

        tvDelete.setOnClickListener {
            val selectedCartItems = getSelectedCartItem()
            val selectedCartItemIds = selectedCartItems.map { it.productId }
            cartViewModel.deleteCartItems(selectedCartItemIds, cartViewModel.userToken)
        }

        rvCartSuggestion.adapter = CartSuggestionAdapter()

        return root
    }

    private fun observeNetworkMessage() {
        cartViewModel.networkMessage.observe(viewLifecycleOwner) { message ->
            if (!cartViewModel.networkStatus) {
                showSnackBar(
                    message = message,
                    status = SNACK_BAR_STATUS_DISABLE,
                    icon = R.drawable.ic_wifi_off
                )
            } else if (cartViewModel.networkStatus) {
                if (cartViewModel.backOnline) {
                    showSnackBar(
                        message = message,
                        status = SNACK_BAR_STATUS_SUCCESS,
                        icon = R.drawable.ic_wifi
                    )
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun fetchDataFromApi(data: List<CartItemByCategory>) {
        data.find { it.type == "Coffee" }?.let { cartItemByCategory ->
            cvCoffee.visibility = View.VISIBLE
            cartCoffeeAdapter.fetchData(cartItemByCategory.products)
            cartCoffeeAdapter.uncheckAllCheckbox()
            cartViewModel.itemCount.value += cartCoffeeAdapter.itemCount
        } ?: run { cartViewModel.cartEmptyCount.value += 1 }

        data.find { it.type == "Tea" }?.let { cartItemByCategory ->
            cvTea.visibility = View.VISIBLE
            cartTeaAdapter.fetchData(cartItemByCategory.products)
            cartTeaAdapter.uncheckAllCheckbox()
            cartViewModel.itemCount.value += cartTeaAdapter.itemCount
        } ?: run { cartViewModel.cartEmptyCount.value += 1 }

        data.find { it.type == "Juice" }?.let { cartItemByCategory ->
            cvJuice.visibility = View.VISIBLE
            cartJuiceAdapter.fetchData(cartItemByCategory.products)
            cartJuiceAdapter.uncheckAllCheckbox()
            cartViewModel.itemCount.value += cartJuiceAdapter.itemCount
        } ?: run { cartViewModel.cartEmptyCount.value += 1 }

        data.find { it.type == "Milk tea" }?.let { cartItemByCategory ->
            cvMilkTea.visibility = View.VISIBLE
            cartMilkTeaAdapter.fetchData(cartItemByCategory.products)
            cartMilkTeaAdapter.uncheckAllCheckbox()
            cartViewModel.itemCount.value += cartMilkTeaAdapter.itemCount
        } ?: run { cartViewModel.cartEmptyCount.value += 1 }

        data.find { it.type == "Milk shake" }?.let { cartItemByCategory ->
            cvMilkShake.visibility = View.VISIBLE
            cartMilkShakeAdapter.fetchData(cartItemByCategory.products)
            cartMilkShakeAdapter.uncheckAllCheckbox()
            cartViewModel.itemCount.value += cartMilkShakeAdapter.itemCount
        } ?: run { cartViewModel.cartEmptyCount.value += 1 }

        data.find { it.type == "Pasty" }?.let { cartItemByCategory ->
            cvPasty.visibility = View.VISIBLE
            cartPastyAdapter.fetchData(cartItemByCategory.products)
            cartPastyAdapter.uncheckAllCheckbox()
            cartViewModel.itemCount.value += cartPastyAdapter.itemCount
        } ?: run { cartViewModel.cartEmptyCount.value += 1 }
    }

    private fun getSelectedCartItem(): List<CartItem> {
        val cartItems = mutableListOf<CartItem>()
        cartCoffeeAdapter.getSelectedCartItem().let {
            if (it.isNotEmpty()) {
                cartItems.addAll(it)
            }
        }
        cartTeaAdapter.getSelectedCartItem().let {
            if (it.isNotEmpty()) {
                cartItems.addAll(it)
            }
        }
        cartJuiceAdapter.getSelectedCartItem().let {
            if (it.isNotEmpty()) {
                cartItems.addAll(it)
            }
        }
        cartMilkTeaAdapter.getSelectedCartItem().let {
            if (it.isNotEmpty()) {
                cartItems.addAll(it)
            }
        }
        cartMilkShakeAdapter.getSelectedCartItem().let {
            if (it.isNotEmpty()) {
                cartItems.addAll(it)
            }
        }
        cartPastyAdapter.getSelectedCartItem().let {
            if (it.isNotEmpty()) {
                cartItems.addAll(it)
            }
        }
        return cartItems.toList()
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

    override fun onStop() {
        super.onStop()
        networkListener.unregisterNetworkCallback()
    }
}