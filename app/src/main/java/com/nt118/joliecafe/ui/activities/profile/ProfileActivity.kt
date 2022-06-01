package com.nt118.joliecafe.ui.activities.profile

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.navArgs
import coil.load
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.nt118.joliecafe.R
import com.nt118.joliecafe.databinding.ActivityProfileBinding
import com.nt118.joliecafe.firebase.firebaseauthentication.FirebaseFacebookLogin
import com.nt118.joliecafe.firebase.firebaseauthentication.FirebaseGoogleAuthentication
import com.nt118.joliecafe.firebase.firebasefirestore.FirebaseStorage
import com.nt118.joliecafe.ui.activities.login.LoginActivity
import com.nt118.joliecafe.ui.fragments.profile_bottom_sheet.ProfileBottomSheetFragment
import com.nt118.joliecafe.util.ApiResult
import com.nt118.joliecafe.util.Constants
import com.nt118.joliecafe.util.Constants.Companion.IS_CHANGE_PASSWORD
import com.nt118.joliecafe.util.Constants.Companion.IS_EDIT
import com.nt118.joliecafe.util.Constants.Companion.IS_SAVE_CHANGE_PASSWORD
import com.nt118.joliecafe.util.Constants.Companion.SNACK_BAR_STATUS_DISABLE
import com.nt118.joliecafe.util.Constants.Companion.SNACK_BAR_STATUS_ERROR
import com.nt118.joliecafe.util.Constants.Companion.SNACK_BAR_STATUS_SUCCESS
import com.nt118.joliecafe.util.NetworkListener
import com.nt118.joliecafe.util.extenstions.observeOnce
import com.nt118.joliecafe.util.extenstions.setCustomBackground
import com.nt118.joliecafe.util.extenstions.setIcon
import com.nt118.joliecafe.viewmodels.profile_activity.ProfileActivityViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class ProfileActivity : AppCompatActivity() {

    private var _binding: ActivityProfileBinding? = null
    private val binding get() = _binding!!
    private val profileActivityViewModel: ProfileActivityViewModel by viewModels()
    private val args by navArgs<ProfileActivityArgs>()
    private lateinit var networkListener: NetworkListener
    private var isEditProfile = false
    private var isChangePassword = false
    private var isSaveChangePassword = false
    private val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupActionBar()

        updateNetworkStatus()
        updateBackOnlineStatus()
        observerNetworkMessage()

        observerUserToken()


        handlerButtonClicked()


        setUserData()

        observerUpdateUserDateResponse()

    }

    private fun handlerButtonClicked() {
        binding.btnGetImage.setOnClickListener {
            if (!args.isFaceOrGGLogin) {
                val bottomSheet =
                    ProfileBottomSheetFragment(profileActivityViewModel = profileActivityViewModel)
                bottomSheet.show(supportFragmentManager, "TAG")
                bottomSheet.userImageUri.observeOnce(this) {
                    it?.let {
                        binding.userImg.load(
                            it
                        ) {
                            crossfade(600)
                            error(R.drawable.placeholder_image)
                        }
                    }
                }
            } else {
                showSnackBar(
                    message = "Login by Google or Facebook can't change avatar!",
                    status = SNACK_BAR_STATUS_DISABLE,
                    icon = R.drawable.ic_sad
                )
            }
        }

        binding.tvEdit.setOnClickListener {
            setEditProfile()
            setEditProfileState()
            if (!isEditProfile && profileActivityViewModel.networkStatus) {
                updateUserData()
            }
        }

        binding.tvChange.setOnClickListener {
            if (!args.isFaceOrGGLogin) {
                setChangePassword()
                setChangePasswordState()
            } else {
                showSnackBar(
                    message = "Login by Google or Facebook can't change password!",
                    status = SNACK_BAR_STATUS_DISABLE,
                    icon = R.drawable.ic_sad
                )
            }
        }

        binding.btnCancel.setOnClickListener {
            if (it.isVisible) {
                setChangePassword()
                setChangePasswordState()
            }
        }

        binding.btnConfirm.setOnClickListener {
            if (it.isVisible) {
                val credential = EmailAuthProvider
                    .getCredential(args.user.email, binding.etPassword.text.toString())
                currentUser?.let { firebaseUser ->
                    firebaseUser.reauthenticate(credential).addOnSuccessListener {
                        binding.etConfirmPasswordLayout.error = null
                        setSaveChangePassword()
                        setSaveChangePasswordState()
                    }.addOnFailureListener { err ->
                        binding.etPasswordLayout.error = err.message
                    }
                }

            }
        }

        binding.btnCancelSaveNewPassword.setOnClickListener {
            if (it.isVisible) {
                setSaveChangePassword()
                setSaveChangePasswordState()
            }
        }

        binding.btnSaveNewPassword.setOnClickListener { btnSaveNewPassword ->
            if (btnSaveNewPassword.isVisible) {
                // save new password fun here
                val password = binding.etNewPassword.text.toString().trim { it <= ' ' }
                val confirmPassword = binding.etConfirmPassword.text.toString().trim { it <= ' ' }

                if (validatePassword(password) && validateConfirmPassword(
                        password,
                        confirmPassword
                    )
                ) {
                    if (profileActivityViewModel.networkStatus) {
                        currentUser?.let { firebaseUser ->
                            firebaseUser.updatePassword(password)
                                .addOnSuccessListener {

                                    showSnackBar(
                                        message = "Change password success!",
                                        status = SNACK_BAR_STATUS_SUCCESS,
                                        icon = R.drawable.ic_success
                                    )

                                    setSaveChangePassword()
                                    setSaveChangePasswordState()

                                    // Google
                                    val options = GoogleSignInOptions.Builder(
                                        GoogleSignInOptions.DEFAULT_SIGN_IN
                                    ).requestIdToken(Constants.WEBCLIENT_ID)
                                        .requestEmail()
                                        .requestProfile()
                                        .build()
                                    val mGoogleSignInClient = GoogleSignIn.getClient(this, options)

                                    // logout
                                    FirebaseAuth.getInstance().signOut()
                                    FirebaseGoogleAuthentication().signOut(
                                        this,
                                        mGoogleSignInClient
                                    )
                                    FirebaseFacebookLogin().facebookLoginSignOut()
                                    profileActivityViewModel.saveUserToken(userToken = "")

                                    startActivity(Intent(this, LoginActivity::class.java).apply {
                                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                                    })
                                }
                                .addOnFailureListener { _ ->
                                    showSnackBar(
                                        message = "Change password failed!",
                                        status = SNACK_BAR_STATUS_ERROR,
                                        icon = R.drawable.ic_sad
                                    )
                                }
                        }
                    }
                }
            }
        }
    }

    private fun observerUpdateUserDateResponse() {
        profileActivityViewModel.updateUserDataResponse.observe(this) { userInfos ->
            when (userInfos) {
                is ApiResult.Loading -> {

                }
                is ApiResult.Success -> {
                    showSnackBar(message = "Update data success", status = SNACK_BAR_STATUS_SUCCESS, icon = R.drawable.ic_success)
                    profileActivityViewModel.saveIsUserDataChange(true)
                }
                is ApiResult.Error -> {
                    showSnackBar(message = "Update data failed!", status = SNACK_BAR_STATUS_ERROR, icon = R.drawable.ic_error)
                    profileActivityViewModel.saveIsUserDataChange(false)
                }
                else -> {}
            }
        }
    }

    private fun observerUserToken() {
        lifecycleScope.launchWhenStarted {
            profileActivityViewModel.readUserToken.collectLatest { token ->
                profileActivityViewModel.userToken = token
            }
        }
    }

    private fun observerNetworkMessage() {
        profileActivityViewModel.networkMessage.observe(this) { message ->
            if (!profileActivityViewModel.networkStatus) {
                showSnackBar(
                    message = message,
                    status = Constants.SNACK_BAR_STATUS_DISABLE,
                    icon = R.drawable.ic_wifi_off
                )
            } else if (profileActivityViewModel.networkStatus) {
                if (profileActivityViewModel.backOnline) {
                    showSnackBar(
                        message = message,
                        status = Constants.SNACK_BAR_STATUS_SUCCESS,
                        icon = R.drawable.ic_wifi
                    )
                }
            }
        }
    }

    private fun updateNetworkStatus() {
        networkListener = NetworkListener()
        networkListener.checkNetworkAvailability(this)
            .asLiveData().observe(this) { status ->
                profileActivityViewModel.networkStatus = status
                profileActivityViewModel.showNetworkStatus()
            }
    }

    private fun updateBackOnlineStatus() {
        profileActivityViewModel.readBackOnline.asLiveData().observe(this) {
            profileActivityViewModel.backOnline = it
        }
    }

    private fun setUserData() {
        val user = args.user
        if (!args.isFaceOrGGLogin) {
            binding.userImg.load(
                user.thumbnail ?: ""
            ) {
                crossfade(600)
                error(R.drawable.placeholder_image)
            }
        } else {
            val firebaseUser = FirebaseAuth.getInstance().currentUser
            firebaseUser?.let {
                binding.userImg.load(
                    it.photoUrl
                ) {
                    crossfade(600)
                    error(R.drawable.placeholder_image)
                }
            }
        }

        binding.tvEmail.text = user.email
        binding.etName.setText(user.fullName)
        binding.etPhone.setText(user.phone ?: "Update your phone number")
    }

    private fun updateUserData() {
        val username = binding.etName.text.toString().trim { it <= ' ' }
        val userPhone = binding.etPhone.text.toString()

        if (validateUserName(username = username) && validateUserPhone(phone = userPhone)) {
            profileActivityViewModel.updateUserInfos(
                token = profileActivityViewModel.userToken,
                newUserData = mapOf(
                    "fullname" to username,
                    "phone" to userPhone
                )
            )
        }

    }

    private fun validateUserName(username: String): Boolean {
        if (username.isEmpty()) {
            binding.etName.requestFocus()
            binding.etNameLayout.error = "Your username is blank!"
            return false
        }
        return true
    }

    private fun validateUserPhone(phone: String): Boolean {
        if (phone.isBlank()) {
            binding.etPhone.requestFocus()
            binding.etPhoneLayout.error = "Phone is blank!"
            return false
        }
        if (phone.length != 10) {
            binding.etPhone.requestFocus()
            binding.etPhoneLayout.error = "This is a valid phone number!"
            return false
        }
        return true
    }

    private fun validatePassword(password: String): Boolean {

        if (password.isEmpty()) {
            binding.etNewPassword.requestFocus()
            binding.etNewPasswordLayout.error = "You must enter your password!"
            return false
        }
        if (password.length < 6) {
            binding.etNewPassword.requestFocus()
            binding.etNewPasswordLayout.error = "Your password length less than 6 character!"
            return false
        }

        return true
    }

    private fun validateConfirmPassword(password: String, confirmPassword: String): Boolean {

        if (password.isEmpty()) {
            binding.etConfirmPassword.requestFocus()
            binding.etConfirmPasswordLayout.error = "You must enter your password!"
            return false
        }
        if (password.length < 6) {
            binding.etConfirmPassword.requestFocus()
            binding.etConfirmPasswordLayout.error = "Your password length less than 6 character!"
            return false
        }

        if (password != confirmPassword) {
            binding.etConfirmPassword.requestFocus()
            binding.etConfirmPasswordLayout.error = "Your confirm password not match your password!"
            return false
        }

        return true
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

        if (isChangePassword) {
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

        if (isSaveChangePassword) {
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

        binding.toolbarProfileActivity.setNavigationOnClickListener {
            onBackPressed()
        }
    }

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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}