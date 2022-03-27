package com.nt118.joliecafe.ui.fragments.profile_bottom_sheet

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.nt118.joliecafe.R
import com.nt118.joliecafe.databinding.FragmentProfileBottomSheetBinding

class ProfileBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentProfileBottomSheetBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBottomSheetBinding.inflate(layoutInflater, container, false)

        binding.btnDismiss.setOnClickListener {
            this.dismiss()
        }

        binding.btnCancel.setOnClickListener {
            this.dismiss()
        }

        return binding.root
    }

}