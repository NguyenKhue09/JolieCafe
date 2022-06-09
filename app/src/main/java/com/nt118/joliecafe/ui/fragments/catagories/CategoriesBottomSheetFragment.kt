package com.nt118.joliecafe.ui.fragments.catagories

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import coil.load
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.nt118.joliecafe.R
import com.nt118.joliecafe.databinding.FragmentCatagoriesBottomSheetBinding
import com.nt118.joliecafe.models.CartItem
import com.nt118.joliecafe.models.Product
import com.nt118.joliecafe.ui.activities.checkout.CheckoutActivity
import com.nt118.joliecafe.ui.activities.login.LoginActivity
import com.nt118.joliecafe.util.ApiResult
import com.nt118.joliecafe.util.Constants
import com.nt118.joliecafe.util.Constants.Companion.SNACK_BAR_STATUS_ERROR
import com.nt118.joliecafe.util.Constants.Companion.SNACK_BAR_STATUS_SUCCESS
import com.nt118.joliecafe.util.NetworkListener
import com.nt118.joliecafe.util.extenstions.setCustomBackground
import com.nt118.joliecafe.util.extenstions.setIcon
import com.nt118.joliecafe.viewmodels.catagories_bottom_sheet.CatagoriesBottomSheetViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CategoriesBottomSheetFragment(private val  product: Product, private val rootView: View) : BottomSheetDialogFragment() {
    private var _binding: FragmentCatagoriesBottomSheetBinding? = null
    private val binding get() = _binding!!
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private lateinit var networkListener: NetworkListener
    private val addCartViewModel by viewModels<CatagoriesBottomSheetViewModel>()

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            println("check")
            dismiss()
            showSnackBar("Create bill successfully", SNACK_BAR_STATUS_SUCCESS, R.drawable.ic_success)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (currentUser == null) {
            startActivity(Intent(requireContext(), LoginActivity::class.java))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCatagoriesBottomSheetBinding.inflate(layoutInflater, container, false)

        addCartViewModel.readBackOnline.asLiveData().observe(viewLifecycleOwner) {
            addCartViewModel.backOnline = it
        }

        binding.btnCancelCategoriesBottomSheet.setOnClickListener {
            this.dismiss()
        }

        binding.itemImgProduct.load(product.thumbnail) {
            crossfade(600)
            error(R.drawable.placeholder_image)
        }
        binding.tvName.text = product.name
        binding.tvCategories.text = product.type
        binding.priceProduct.text = product.originPrice.toString()
        if (product.type == "Milk shake" || product.type == "Milk tea"){

            val dialog = dialog as BottomSheetDialog
            dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
            dialog.behavior.isDraggable = false

            binding.tvTopping.visibility = View.VISIBLE
            binding.bubble.visibility = View.VISIBLE
            binding.priceBubble.visibility = View.VISIBLE
            binding.cheese.visibility = View.VISIBLE
            binding.priceCheese.visibility = View.VISIBLE
            binding.peach.visibility = View.VISIBLE
            binding.pricePeach.visibility = View.VISIBLE
            binding.crunch.visibility = View.VISIBLE
            binding.priceLychee.visibility = View.VISIBLE
            binding.lychee.visibility = View.VISIBLE
            binding.priceCrunch.visibility = View.VISIBLE

        }


        lifecycleScope.launchWhenStarted {
            networkListener = NetworkListener()
            networkListener.checkNetworkAvailability(requireContext()).collect { status ->
                addCartViewModel.networkStatus = status
                addCartViewModel.showNetworkStatus()
            }
        }
        lifecycleScope.launch {
            addCartViewModel.readUserToken.collectLatest { token ->
                addCartViewModel.userToken = token
            }
        }

        binding.btnAddToCard.setOnClickListener {

            val productId = product.id
            val size = if(binding.chipSizeL.isChecked){
                binding.chipSizeL.text.toString()
            } else if (binding.chipSizeS.isChecked) {
                binding.chipSizeS.text.toString()
            } else {
                binding.chipSizeM.text.toString()
            }
            val quantity = "1"
            val price = product.originPrice.toInt().toString()


            if (addCartViewModel.networkStatus) {
                val newCart = mapOf(
                    "productId" to productId,
                    "size" to size,
                    "quantity" to quantity,
                    "price" to price,
                )
                addNewCart(cartData = newCart)
            } else {
                addCartViewModel.showNetworkStatus()
            }
            handleApiResponse()
        }

        binding.btnPurchase.setOnClickListener {
            val productId = product.id
            if(binding.chipSizeL.isChecked){
                binding.chipSizeL.text.toString()
            } else if (binding.chipSizeS.isChecked) {
                binding.chipSizeS.text.toString()
            } else {
                binding.chipSizeM.text.toString()
            }


            if (addCartViewModel.networkStatus) {
                val intent = Intent(context, CheckoutActivity::class.java)
                val cartItems = mutableListOf<CartItem>()
                cartItems.add(CartItem(productId, productId, product,"M",1, product.originPrice))
                intent.putExtra("cartItems", Gson().toJson(cartItems))
                resultLauncher.launch(intent)
            } else {
                addCartViewModel.showNetworkStatus()
            }
            handleApiResponse()
        }

        return binding.root
    }

    private fun handleApiResponse() {
        addCartViewModel.addCartResponse.observe(viewLifecycleOwner) { response ->
            Log.d("Bottom Sheet", "handleApiResponse: call")
            when (response) {
                is ApiResult.Loading -> {

                }
                is ApiResult.NullDataSuccess -> {
                    this@CategoriesBottomSheetFragment.dismiss()
                    showSnackBar(
                        "Add product to cart successfully",
                        SNACK_BAR_STATUS_SUCCESS,
                        R.drawable.ic_success
                    )
                }
                is ApiResult.Error -> {
                    showSnackBar(response.message!!, SNACK_BAR_STATUS_ERROR, R.drawable.ic_error)
                    this@CategoriesBottomSheetFragment.dismiss()
                }
                else -> {}
            }
        }
    }

    private fun addNewCart(cartData: Map<String, String>) {
        Log.d("Bottom Sheet", "addNewCart: ${cartData.values}")
        addCartViewModel.addCart(
                data = cartData,
                token = addCartViewModel.userToken
        )
    }

    private fun showSnackBar(message: String, status: Int, icon: Int) {
        val drawable = requireContext().getDrawable(icon)

        val snackBarContentColor = when (status) {
            Constants.SNACK_BAR_STATUS_SUCCESS -> R.color.text_color_2
            Constants.SNACK_BAR_STATUS_DISABLE -> R.color.dark_text_color
            Constants.SNACK_BAR_STATUS_ERROR -> R.color.error_color
            else -> R.color.text_color_2
        }


        val snackBar = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG)
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

}
