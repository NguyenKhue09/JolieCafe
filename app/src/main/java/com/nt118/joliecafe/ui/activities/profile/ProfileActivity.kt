package com.nt118.joliecafe.ui.activities.profile

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.navigation.navArgs
import coil.load
import com.nt118.joliecafe.R
import com.nt118.joliecafe.databinding.ActivityProfileBinding
import com.nt118.joliecafe.ui.fragments.profile_bottom_sheet.ProfileBottomSheetFragment
import com.nt118.joliecafe.util.Constants.Companion.IS_CHANGE_PASSWORD
import com.nt118.joliecafe.util.Constants.Companion.IS_EDIT
import com.nt118.joliecafe.util.Constants.Companion.IS_SAVE_CHANGE_PASSWORD
import com.nt118.joliecafe.viewmodels.profile_activity.ProfileActivityViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileActivity: AppCompatActivity() {

    private var _binding: ActivityProfileBinding? = null
    private val binding get() = _binding!!
    private val profileActivityViewModel: ProfileActivityViewModel by viewModels()
    private val args by navArgs<ProfileActivityArgs>()
    private var isEditProfile = false
    private var isChangePassword = false
    private var isSaveChangePassword = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupActionBar()

        binding.btnGetImage.setOnClickListener {
            if(!args.isFaceOrGGLogin) {
                val bottomSheet = ProfileBottomSheetFragment()
                bottomSheet.show(supportFragmentManager, "TAG")
            } else {
                Toast.makeText(this, "Login by Google or Facebook can't change avatar!", Toast.LENGTH_LONG).show()
            }
        }


        binding.tvEdit.setOnClickListener {
            setEditProfile()
            setEditProfileState()
            // function lưu data
        }

        binding.tvChange.setOnClickListener {
            if(!args.isFaceOrGGLogin) {
                setChangePassword()
                setChangePasswordState()
            } else {
                Toast.makeText(this, "Login by Google or Facebook can't change password!", Toast.LENGTH_LONG).show()
            }
        }

        binding.btnCancel.setOnClickListener {
            if(it.isVisible)  {
                setChangePassword()
                setChangePasswordState()
            }
        }

        binding.btnConfirm.setOnClickListener {
            if (it.isVisible) {
                // Check Password fun here
                setSaveChangePassword()
                setSaveChangePasswordState()
            }
        }

        binding.btnCancelSaveNewPassword.setOnClickListener {
            if (it.isVisible) {
                setSaveChangePassword()
                setSaveChangePasswordState()
            }
        }

        binding.btnSaveNewPassword.setOnClickListener {
            if (it.isVisible) {
                // save new password fun here
                setSaveChangePassword()
                setSaveChangePasswordState()
            }
        }

        setUserData()
    }

    private fun setUserData() {
        val user = args.user
        binding.userImg.load(
            user.thumbnail
        ) {
            crossfade(600)
            error(R.drawable.placeholder_image)
        }
        println(user)
        binding.tvEmail.text = user.email
        binding.etName.setText(user.fullName)
        binding.etPhone.setText(user.phone)
    }

    private fun setEditProfile() {
        isEditProfile = !isEditProfile
    }
    private fun setChangePassword() {
        isChangePassword = !isChangePassword
    }
    private fun setSaveChangePassword() {
        isSaveChangePassword = !isSaveChangePassword
    }

    private fun setEditProfileState() {
        if (isEditProfile) {
            binding.tvEdit.text = getString(R.string.save)
            binding.etNameLayout.isEnabled = true
            binding.etPhoneLayout.isEnabled = true
        } else {
            binding.etNameLayout.isEnabled = false
            binding.etPhoneLayout.isEnabled = false
            binding.tvEdit.text = getString(R.string.edit)
        }
    }
    private fun setChangePasswordState() {

        if( isChangePassword) {
            binding.tvChange.visibility = View.GONE
            binding.tvPassword.text = getString(R.string.current_password)
            binding.etPasswordLayout.isEnabled = true
            binding.btnConfirm.visibility = View.VISIBLE
            binding.btnCancel.visibility = View.VISIBLE
        } else {
            binding.tvChange.visibility = View.VISIBLE
            binding.tvPassword.text = getString(R.string.password)
            binding.etPasswordLayout.isEnabled = false
            binding.btnConfirm.visibility = View.GONE
            binding.btnCancel.visibility = View.GONE
        }
    }
    private fun setSaveChangePasswordState() {

        if( isSaveChangePassword) {
            binding.cardPassword.visibility = View.GONE
            binding.cardSaveNewPassword.visibility = View.VISIBLE
        } else {
            binding.cardPassword.visibility = View.VISIBLE
            binding.cardSaveNewPassword.visibility = View.GONE
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(IS_EDIT, isEditProfile)
        outState.putBoolean(IS_CHANGE_PASSWORD, isChangePassword)
        outState.putBoolean(IS_SAVE_CHANGE_PASSWORD, isSaveChangePassword)
    }


    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        isEditProfile = savedInstanceState.getBoolean(IS_EDIT, false)
        isChangePassword = savedInstanceState.getBoolean(IS_CHANGE_PASSWORD, false)
        isSaveChangePassword = savedInstanceState.getBoolean(IS_SAVE_CHANGE_PASSWORD, false)
        setEditProfileState()
        setChangePasswordState()
        setSaveChangePasswordState()
    }

    private fun setupActionBar() {
        setSupportActionBar(binding.toolbarProfileActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        }

        binding.toolbarProfileActivity.setNavigationOnClickListener { onBackPressed() }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}