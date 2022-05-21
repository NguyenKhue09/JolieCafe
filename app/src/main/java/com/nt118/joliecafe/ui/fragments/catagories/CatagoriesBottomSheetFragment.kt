package com.nt118.joliecafe.ui.fragments.catagories

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.children
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import coil.load
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.nt118.joliecafe.R
import com.nt118.joliecafe.databinding.FragmentCatagoriesBottomSheetBinding
import com.nt118.joliecafe.databinding.FragmentProfileBottomSheetBinding
import com.nt118.joliecafe.models.Product
import com.nt118.joliecafe.ui.activities.login.LoginActivity
import com.nt118.joliecafe.util.ApiResult
import com.nt118.joliecafe.util.NetworkListener
import com.nt118.joliecafe.viewmodels.address_book.AddressBookViewModel
import com.nt118.joliecafe.viewmodels.catagories_bottom_sheet.CatagoriesBottomSheetViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class CatagoriesBottomSheetFragment( private val  product: Product) : BottomSheetDialogFragment() {
    private var _binding: FragmentCatagoriesBottomSheetBinding? = null
    private val binding get() = _binding!!
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private lateinit var networkListener: NetworkListener
    private val addCartViewModel by viewModels<CatagoriesBottomSheetViewModel>()

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

        binding.btnAddToCard.setOnClickListener {

            val productId = product.id
            val size = binding.chipGroupSize.children.filter { (it as Chip).isChecked }.map { (it as Chip).text.toString()}.toString()
            val quantity = "1"
            val price = product.originPrice.toString()

            if (addCartViewModel.networkStatus) {
                val newCart = mapOf(
                    "productId" to productId,
                    "size" to size,
                    "quantity" to quantity,
                    "price" to price,
                )
                addNewCart(addressData = newCart)
            } else {
                addCartViewModel.showNetworkStatus()
            }

            this.dismiss()
        }

        handleApiResponse()
        return binding.root
    }

    private fun handleApiResponse() {
        addCartViewModel.addCartResponse.observe(this) { response ->
            when (response) {
                is ApiResult.Loading -> {

                }
                is ApiResult.Success -> {
                    Toast.makeText(
                        context,
                        "Add new cart successful",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is ApiResult.Error -> {
                    Toast.makeText(context, response.message, Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }

    private fun addNewCart(addressData: Map<String, String>) {
        addCartViewModel.addCart(
                data = addressData,
                token = addCartViewModel.userToken
            )
    }

}
