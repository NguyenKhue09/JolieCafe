package com.nt118.joliecafe.ui.activities.checkout

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SwitchCompat
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nt118.joliecafe.R
import com.nt118.joliecafe.adapter.CheckoutAdapter
import com.nt118.joliecafe.databinding.ActivityCheckoutBinding
import com.nt118.joliecafe.models.*
import com.nt118.joliecafe.payments.momo_payment.AppMoMoLibKotlinVersion
import com.nt118.joliecafe.ui.activities.address_book.AddressBookActivity
import com.nt118.joliecafe.util.*
import com.nt118.joliecafe.util.Constants.Companion.MERCHANT_CODE
import com.nt118.joliecafe.util.Constants.Companion.MERCHANT_NAME
import com.nt118.joliecafe.util.Constants.Companion.MERCHANT_NAME_LABEL
import com.nt118.joliecafe.util.Constants.Companion.SNACK_BAR_STATUS_ERROR
import com.nt118.joliecafe.util.Constants.Companion.SNACK_BAR_STATUS_SUCCESS
import com.nt118.joliecafe.util.extenstions.setCustomBackground
import com.nt118.joliecafe.util.extenstions.setIcon
import com.nt118.joliecafe.viewmodels.checkout.CheckoutViewModel
import dagger.hilt.android.AndroidEntryPoint
import vn.momo.momo_partner.MoMoParameterNamePayment

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
    private lateinit var progressAddress: CircularProgressIndicator
    private lateinit var tvSubtotalDetail: TextView
    private lateinit var tvName: TextView
    private lateinit var tvPhoneNumber: TextView
    private lateinit var tvAddress: TextView
    private lateinit var addressContainer: LinearLayout
    private lateinit var swUseJolieCoin: SwitchCompat
    private lateinit var tvChangeAddress: TextView
    private lateinit var tvJolieCoinLabel: TextView
    private lateinit var tvJolieCoinDetail: TextView
    private lateinit var tvTotalDetail: TextView

    private lateinit var momoBillDescription: String
    private lateinit var bill: Bill

    private val cartItems: List<CartItem> get() = checkoutViewModel.cartItems
    private var isUseJolieCoin
        get() = checkoutViewModel.isUseJolieCoin.value
        set(value) { checkoutViewModel.isUseJolieCoin.value = value }
    private var userAddress
        get() = checkoutViewModel.userAddress
        set(value) { checkoutViewModel.userAddress = value }
    private var subTotalPrice
        get() = checkoutViewModel.subTotalPrice.value
        set(value) { checkoutViewModel.subTotalPrice.value = value }
    private var totalPrice
        get() = checkoutViewModel.totalPrice.value
        set(value) { checkoutViewModel.totalPrice.value = value }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        initMoMoPayment()
        configMoMo()
        readDataStore()
        observe()
        setListeners()
    }

    private fun initViews() {
        rvProduct = binding.rvProduct
        btnNavBack = binding.btnNavBack
        btnOrder = binding.btnOrder
        btnCancel = binding.btnCancel
        voucherContainer = binding.voucherContainer
        tvUseJolieCoin= binding.tvUseJolieCoin
        progressAddress = binding.progressAddress
        tvSubtotalDetail = binding.tvSubtotalDetail
        addressContainer = binding.addressContainer
        tvName = binding.tvName
        tvPhoneNumber = binding.tvPhoneNumber
        tvAddress = binding.tvAddress
        swUseJolieCoin = binding.swUseJolieCoin
        tvChangeAddress = binding.tvChangeAddress
        tvJolieCoinLabel = binding.tvJolieCoinLabel
        tvJolieCoinDetail = binding.tvJolieCoinDetail
        tvTotalDetail = binding.tvTotalDetail

        swUseJolieCoin.isChecked = isUseJolieCoin!!
        tvUseJolieCoin.text = resources.getString(R.string.use_jolie_coin, 200)

        if (isUseJolieCoin!!) {
            tvJolieCoinLabel.visibility = View.VISIBLE
            tvJolieCoinDetail.visibility = View.VISIBLE
            totalPrice = subTotalPrice!! - 200.0
        } else {
            tvJolieCoinLabel.visibility = View.GONE
            tvJolieCoinDetail.visibility = View.GONE
            totalPrice = subTotalPrice!!
        }

        // Set adapter

        val cartItemsJson = intent.getStringExtra("cartItems")
        val cartItems = Gson().fromJson<List<CartItem>>(cartItemsJson, object : TypeToken<List<CartItem>>() {}.type)
        rvProduct.adapter = CheckoutAdapter(cartItems, this)
        subTotalPrice = cartItems.sumOf { it.price }
    }

    private fun initMoMoPayment() {
        AppMoMoLibKotlinVersion.getMoMoKotlinInstance().setEnvironment(AppMoMoLibKotlinVersion.Companion.ENVIRONMENT.DEVELOPMENT)
    }

    private fun configMoMo() {
        AppMoMoLibKotlinVersion.getMoMoKotlinInstance().setAction(AppMoMoLibKotlinVersion.Companion.ACTION.PAYMENT)
        AppMoMoLibKotlinVersion.getMoMoKotlinInstance().setActionType(AppMoMoLibKotlinVersion.Companion.ACTION_TYPE.GET_TOKEN)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun requestMoMoPayment() {
        bill = createBill()

        momoBillDescription = "Thanh toán đơn hàng ${bill.orderId} qua MoMo"

        val eventValue = mutableMapOf<String, Any>(
            "merchantname" to MERCHANT_NAME,
            "merchantcode" to MERCHANT_CODE,
            "amount" to bill.totalCost.toInt(),
            "orderId" to bill.orderId,
            "orderLabel" to bill.orderId,
            "merchantnamelabel" to MERCHANT_NAME_LABEL,
            "fee" to "0",
            "description" to momoBillDescription,
            "requestId" to MERCHANT_CODE + "merchant_billId_" + System.currentTimeMillis(),
            "partnerCode" to MERCHANT_CODE,
            MoMoParameterNamePayment.REQUEST_TYPE to "payment",
            MoMoParameterNamePayment.LANGUAGE to "vi",
            MoMoParameterNamePayment.EXTRA to ""
        )

        //Request momo app
        AppMoMoLibKotlinVersion.getMoMoKotlinInstance().requestMoMoCallBack(this, momoLaunchResultCallBack, eventValue)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private val momoLaunchResultCallBack =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if ( result.resultCode == Activity.RESULT_OK) {
                if (result.data != null) {
                    if (result.data!!.getIntExtra("status", -1) == 0) {
                        //TOKEN IS AVAILABLE

                        val token = result.data!!.getStringExtra("data") //Token response
                        val phoneNumber = result.data!!.getStringExtra("phonenumber")
                        var env = result.data!!.getStringExtra("env")
                        if (env == null) {
                            env = "app"
                        }

                        if (token != null && token != "") {
                            println(token)
                            println(phoneNumber)

                            val body = MomoPaymentRequestBody(
                                customerNumber = phoneNumber!!,
                                partnerRefId = bill.orderId,
                                appData = token,
                                description = momoBillDescription,
                                bill = bill
                            )

                            checkoutViewModel.momoPaymentRequest(token = checkoutViewModel.userToken, data = body)


                            // TODO: send phoneNumber & token to your server side to process payment with MoMo server
                            // IF Momo popup success, continue to process your order
                        } else {
                            showSnackBar(message = getString(R.string.not_receive_info), status = SNACK_BAR_STATUS_ERROR, icon = R.drawable.ic_error)
                        }
                    } else if (result.data!!.getIntExtra("status", -1) == 1) {
                        //TOKEN FAIL
                        val message =
                            if (result.data!!.getStringExtra("message") != null) result.data!!.getStringExtra("message") else "Thất bại"
                        showSnackBar(message = message!!, status = SNACK_BAR_STATUS_ERROR, icon = R.drawable.ic_error)
                    } else if (result.data!!.getIntExtra("status", -1) == 2) {
                        //TOKEN FAIL
                        showSnackBar(message = getString(R.string.not_receive_info), status = SNACK_BAR_STATUS_ERROR, icon = R.drawable.ic_error)
                    } else {
                        //TOKEN FAIL
                        showSnackBar(message = getString(R.string.not_receive_info), status = SNACK_BAR_STATUS_ERROR, icon = R.drawable.ic_error)
                    }
                } else {
                    showSnackBar(message = getString(R.string.not_receive_info), status = SNACK_BAR_STATUS_ERROR, icon = R.drawable.ic_error)
                }
            } else {
                showSnackBar(message = getString(R.string.not_receive_info_err), status = SNACK_BAR_STATUS_ERROR, icon = R.drawable.ic_error)
            }
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
//        getCartResponse.observe(this@CheckoutActivity) { response ->
//            when (response) {
//                is ApiResult.Loading -> progressCart.visibility = View.VISIBLE
//                is ApiResult.Success -> {
//                    progressCart.visibility = View.GONE
//                    cartItems = response.data!!
//                    val adapter = CheckoutAdapter(response.data, this@CheckoutActivity)
//                    rvProduct.adapter = adapter
//                    subTotalPrice.value = adapter.getTotalPrice()
//                    totalPrice.value = subTotalPrice.value!! + 30000.0 - (if (isUseJolieCoin.value!!) 200.0 else 0.0)
//                }
//                is ApiResult.Error -> {
//                    progressCart.visibility = View.GONE
//                    Toast.makeText(this@CheckoutActivity, "Failed to get cart items", Toast.LENGTH_SHORT).show()
//                }
//                else -> {}
//            }
//        }

        getAddressByIdResponse.observe(this@CheckoutActivity) { response ->
            when (response) {
                is ApiResult.Loading -> {
                    progressAddress.visibility = View.VISIBLE
                    addressContainer.visibility = View.GONE
                    btnOrder.isEnabled = false
                }
                is ApiResult.Success -> {
                    progressAddress.visibility = View.GONE
                    addressContainer.visibility = View.VISIBLE
                    btnOrder.isEnabled = true
                    setShippingAddress(response.data!!)
                }
                is ApiResult.Error -> {
                    progressAddress.visibility = View.GONE
//                    Toast.makeText(this@CheckoutActivity, "Failed to get address", Toast.LENGTH_SHORT).show()
                    showSnackBar(message = "Failed to get address", status = SNACK_BAR_STATUS_ERROR, icon = R.drawable.ic_error)
                }
                else -> {}
            }
        }

        momoPaymentRequestResponse.observe(this@CheckoutActivity) { response ->
            when (response) {
                is ApiResult.Loading -> {
                    binding.checkoutCircularProgressIndicator.visibility = View.VISIBLE
                    disableButton()
                }
                is ApiResult.NullDataSuccess -> {
                    binding.checkoutCircularProgressIndicator.visibility = View.GONE
                    enableButton()
                    showSnackBar(message = "Momo payment successfully", status = SNACK_BAR_STATUS_SUCCESS, icon = R.drawable.ic_success)
                }
                is ApiResult.Error -> {
                    binding.checkoutCircularProgressIndicator.visibility = View.GONE
                    enableButton()
                    showSnackBar(message = response.message!!, status = SNACK_BAR_STATUS_ERROR, icon = R.drawable.ic_error)
                }
                else -> {}
            }
        }

        subTotalPrice.observe(this@CheckoutActivity) {
            tvSubtotalDetail.text = getString(R.string.product_price, NumberUtil.addSeparator(it))
        }

        totalPrice.observe(this@CheckoutActivity) {
            tvTotalDetail.text = getString(R.string.product_price, NumberUtil.addSeparator(it))
        }

        isUseJolieCoin.observe(this@CheckoutActivity) {
            if (it) {
                tvJolieCoinLabel.visibility = View.VISIBLE
                tvJolieCoinDetail.visibility = View.VISIBLE
                totalPrice.value = subTotalPrice.value!! + 30000.0 - 200.0
            } else {
                tvJolieCoinLabel.visibility = View.GONE
                tvJolieCoinDetail.visibility = View.GONE
                totalPrice.value = subTotalPrice.value!! + 30000.0
            }
        }
    }

    private fun setListeners() {
        btnNavBack.setOnClickListener {
            finish()
        }

        btnOrder.setOnClickListener {
            requestMoMoPayment()
            //startActivity(Intent(this, OrderDetailActivity::class.java))
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

        tvChangeAddress.setOnClickListener {
            val intent = Intent(this, AddressBookActivity::class.java)
            startActivity(intent)
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

        return if (isUseJolieCoin!!) {
            200.0
        } else {
            0.0
        }
    }

    private fun calculateShippingFee() = 30000.0 // K tính được nên tạm thời coi như là 30k

    private fun createBill(): Bill {
        val billProductList = mutableListOf<BillProduct>()
        cartItems.forEach { cartItem ->
            billProductList.add(BillProduct(
                cartItem.productDetail.id,
                cartItem.size,
                cartItem.quantity,
                cartItem.price
            ))
        }

        println(billProductList)

        val orderId = RandomString.generateRandomString()
        return Bill(
            id = null,
            userInfo = currentUser!!.uid,
            products = billProductList.toList(),
            address = userAddress!!,
            totalCost = totalPrice!!,
            discountCost = calculateDiscount(),
            shippingFee = calculateShippingFee(),
            voucherApply = emptyList(),
            scoreApply = 20, // Coi như mỗi lần mua là được 20 điểm
            paid = false, // Chưa thanh toán
            paymentMethod = "MoMo", // Tạm cho ntn nhé
            orderDate = DateTimeUtil.getCurrentDate(),
            status = "Pending",
            orderId = orderId
        )
    }

    private fun disableButton() {
        btnOrder.isEnabled = false
        btnCancel.isEnabled = false
        btnNavBack.isEnabled = false
    }

    private fun enableButton() {
        btnOrder.isEnabled = true
        btnCancel.isEnabled = true
        btnNavBack.isEnabled = true
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        checkoutViewModel.getAddressById(checkoutViewModel.userDefaultAddressId)
    }

    private fun pxToDp(px: Float, context: Context) =
        px / (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)

    private fun showSnackBar(message: String, status: Int, icon: Int) {
        val drawable = getDrawable(icon)

        val snackBarContentColor = when (status) {
            Constants.SNACK_BAR_STATUS_SUCCESS -> R.color.text_color_2
            Constants.SNACK_BAR_STATUS_DISABLE -> R.color.dark_text_color
            Constants.SNACK_BAR_STATUS_ERROR -> R.color.error_color
            else -> R.color.text_color_2
        }


        val snackBar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction("Ok") {
            }
            .setActionTextColor(ContextCompat.getColor(this, R.color.grey_primary))
            .setTextColor(ContextCompat.getColor(this, snackBarContentColor))
            .setIcon(
                drawable = drawable!!,
                colorTint = ContextCompat.getColor(this, snackBarContentColor),
                iconPadding = resources.getDimensionPixelOffset(R.dimen.small_margin)
            )
            .setCustomBackground(getDrawable(R.drawable.snackbar_normal_custom_bg)!!)

        snackBar.show()
    }

}