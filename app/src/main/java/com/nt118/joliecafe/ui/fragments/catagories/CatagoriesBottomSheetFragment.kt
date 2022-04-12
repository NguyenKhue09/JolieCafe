package com.nt118.joliecafe.ui.fragments.catagories

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.nt118.joliecafe.R
import com.nt118.joliecafe.databinding.FragmentCatagoriesBottomSheetBinding
import com.nt118.joliecafe.databinding.FragmentProfileBottomSheetBinding

class CatagoriesBottomSheetFragment : BottomSheetDialogFragment() {
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

        val dialog = dialog as BottomSheetDialog
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        dialog.behavior.isDraggable = false

        return binding.root
    }

}
