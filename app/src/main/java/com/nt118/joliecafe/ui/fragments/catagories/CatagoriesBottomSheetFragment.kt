package com.nt118.joliecafe.ui.fragments.catagories

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import coil.load
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.nt118.joliecafe.R
import com.nt118.joliecafe.databinding.FragmentCatagoriesBottomSheetBinding
import com.nt118.joliecafe.databinding.FragmentProfileBottomSheetBinding
import com.nt118.joliecafe.models.Product

class CatagoriesBottomSheetFragment( private val  product: Product) : BottomSheetDialogFragment() {
    private var _binding: FragmentCatagoriesBottomSheetBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCatagoriesBottomSheetBinding.inflate(layoutInflater, container, false)

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


        return binding.root
    }

}
