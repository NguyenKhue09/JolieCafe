package com.nt118.joliecafe.ui.activities.checkout

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.widget.SwitchCompat
import androidx.cardview.widget.CardView
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.firebase.auth.FirebaseAuth
import com.nt118.joliecafe.R
import com.nt118.joliecafe.adapter.CheckoutAdapter
import com.nt118.joliecafe.databinding.ActivityCheckoutBinding
import com.nt118.joliecafe.models.Address
import com.nt118.joliecafe.models.Bill
import com.nt118.joliecafe.models.BillProduct
import com.nt118.joliecafe.models.CartItem
import com.nt118.joliecafe.ui.activities.order_detail.OrderDetailActivity
import com.nt118.joliecafe.util.ApiResult
import com.nt118.joliecafe.util.DateTimeUtil
import com.nt118.joliecafe.util.NetworkListener
import com.nt118.joliecafe.util.NumberUtil
import com.nt118.joliecafe.viewmodels.checkout.CheckoutViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class CheckoutActivity : AppCompatActivity() {

    private var _binding: ActivityCheckoutBinding? = null
    private val binding get() = _binding!!
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private lateinit var networkListener: NetworkListener
    private val checkoutViewModel by viewModels<CheckoutViewModel>()
    private lateinit var rvProduct: RecyclerView
    private lateinit var btnNavBack: ImageButton
    private lateinit var btnOrder: MaterialButton
    private lateinit var btnCancel: MaterialButton
    private lateinit var voucherContainer: CardView
    private lateinit var tvUseJolieCoin: TextView
    private lateinit var progressCart: CircularProgressIndicator
    private lateinit var progressAddress: CircularProgressIndicator
    private lateinit var tvSubtotalDetail: TextView
    private lateinit var tvName: TextView
    private lateinit var tvPhoneNumber: TextView
    private lateinit var tvAddress: TextView
    private lateinit var addressContainer: LinearLayout
    private lateinit var swUseJolieCoin: SwitchCompat

    private val cartItems: List<CartItem> get() = checkoutViewModel.cartItems
    private var isUseJolieCoin
        get() = checkoutViewModel.isUseJolieCoin
        set(value) { checkoutViewModel.isUseJolieCoin = value }
    private var userAddress
        get() = checkoutViewModel.userAddress
        set(value) { checkoutViewModel.userAddress = value }
    private var totalPrice
        get() = checkoutViewModel.totalPrice
        set(value) { checkoutViewModel.totalPrice = value }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        readDataStore()
        observe()

        lifecycleScope.launchWhenStarted {
            checkoutViewModel.readUserToken.collectLatest { token ->
                checkoutViewModel.userToken = token
                checkoutViewModel.getAllCartItems(token)
                checkoutViewModel.getAddressById(token, checkoutViewModel.userDefaultAddressId)
            }
        }
        setListeners()

        tvUseJolieCoin.text = resources.getString(R.string.use_jolie_coin, 200)
    }

    private fun initViews() {
        rvProduct = binding.rvProduct
        btnNavBack = binding.btnNavBack
        btnOrder = binding.btnOrder
        btnCancel = binding.btnCancel
        voucherContainer = binding.voucherContainer
        tvUseJolieCoin= binding.tvUseJolieCoin
        progressCart = binding.progressCart
        progressAddress = binding.progressAddress
        tvSubtotalDetail = binding.tvSubtotalDetail
        addressContainer = binding.addressContainer
        tvName = binding.tvName
        tvPhoneNumber = binding.tvPhoneNumber
        tvAddress = binding.tvAddress
        swUseJolieCoin = binding.swUseJolieCoin
        swUseJolieCoin.isChecked = isUseJolieCoin
    }

    private fun readDataStore() {
        checkoutViewModel.readBackOnline.asLiveData().observe(this) {
            checkoutViewModel.backOnline = it
        }

        checkoutViewModel.readUserDefaultAddressId.asLiveData().observe(this) { addressId ->
            checkoutViewModel.userDefaultAddressId = addressId
        }
    }

    private fun observe() = checkoutViewModel.apply {
        getCartResponse.observe(this@CheckoutActivity) { response ->
            when (response) {
                is ApiResult.Loading -> progressCart.visibility = View.VISIBLE
                is ApiResult.Success -> {
                    progressCart.visibility = View.GONE
                    val adapter = CheckoutAdapter(response.data!!, this@CheckoutActivity)
                    rvProduct.adapter = adapter
                    totalPrice = adapter.getTotalPrice()
                    tvSubtotalDetail.text = getString(R.string.product_price, NumberUtil.addSeparator(adapter.getTotalPrice()))
                }
                is ApiResult.Error -> {
                    progressCart.visibility = View.GONE
                    Toast.makeText(this@CheckoutActivity, "Failed to get cart items", Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }

        getAddressByIdResponse.observe(this@CheckoutActivity) { response ->
            when (response) {
                is ApiResult.Loading -> {
                    progressAddress.visibility = View.VISIBLE
                    addressContainer.visibility = View.GONE
                }
                is ApiResult.Success -> {
                    progressAddress.visibility = View.GONE
                    addressContainer.visibility = View.VISIBLE
                    setShippingAddress(response.data!!)
                }
                is ApiResult.Error -> {
                    progressAddress.visibility = View.GONE
                    Toast.makeText(this@CheckoutActivity, "Failed to get address", Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }

    private fun setListeners() {
        btnNavBack.setOnClickListener {
            finish()
        }

        btnOrder.setOnClickListener {
            val bill = createBill()
            startActivity(Intent(this, OrderDetailActivity::class.java))
        }

        btnCancel.setOnClickListener {
            finish()
        }

        voucherContainer.setOnClickListener {
            val intent = Intent(this, VoucherDialog::class.java)
            intent.putExtra("screenWidth", pxToDp(370f, this).toInt())
            startActivity(intent)
        }

        swUseJolieCoin.setOnClickListener {
            isUseJolieCoin = swUseJolieCoin.isChecked
        }
    }

    private fun setShippingAddress(address: Address) {
        userAddress = address
        tvName.text = address.userName
        tvPhoneNumber.text = address.phone
        tvAddress.text = address.address
    }

    private fun calculateDiscount(): Double {
        // discount chưa có nên tạm thời coi như JolieCoin là 200

        return if (isUseJolieCoin) {
            200.0
        } else {
            0.0
        }
    }

    private fun calculateShippingFee() = 30000.0 // K tính được nên

    private fun createBill(): Bill {
        val billProductList = mutableListOf<BillProduct>()
        cartItems.forEach { cartItem ->
            billProductList.add(BillProduct(
                cartItem.productDetail,
                cartItem.size,
                cartItem.quantity,
                cartItem.price
            ))
        }

        return Bill(
            id = null,
            userInfo = currentUser!!.uid,
            products = billProductList.toList(),
            address = userAddress!!,
            totalCost = totalPrice,
            calculateDiscount(),
            calculateShippingFee(),
            emptyList(),
            scoreApply = 20, // Coi như mỗi lần mua là được 20 điểm
            paid = false, // Chưa thanh toán
            paymentMethod = "COD", // Tạm cho ntn nhé
            orderDate = DateTimeUtil.getCurrentDate(),
            status = "Pending"
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun pxToDp(px: Float, context: Context) =
        px / (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)

}