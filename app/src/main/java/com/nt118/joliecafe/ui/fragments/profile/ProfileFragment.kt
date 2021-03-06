package com.nt118.joliecafe.ui.fragments.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.nt118.joliecafe.R
import com.nt118.joliecafe.databinding.FragmentProfileBinding
import com.nt118.joliecafe.firebase.firebaseauthentication.FirebaseFacebookLogin
import com.nt118.joliecafe.firebase.firebaseauthentication.FirebaseGoogleAuthentication
import com.nt118.joliecafe.models.User
import com.nt118.joliecafe.ui.activities.address_book.AddressBookActivity
import com.nt118.joliecafe.ui.activities.login.LoginActivity
import com.nt118.joliecafe.ui.activities.order_history.OrderHistoryActivity
import com.nt118.joliecafe.ui.activities.settings.SettingsActivity
import com.nt118.joliecafe.util.ApiResult
import com.nt118.joliecafe.util.Constants
import com.nt118.joliecafe.util.Constants.Companion.SNACK_BAR_STATUS_DISABLE
import com.nt118.joliecafe.util.Constants.Companion.SNACK_BAR_STATUS_ERROR
import com.nt118.joliecafe.util.Constants.Companion.SNACK_BAR_STATUS_SUCCESS
import com.nt118.joliecafe.util.NetworkListener
import com.nt118.joliecafe.util.extenstions.observeOnce
import com.nt118.joliecafe.util.extenstions.setCustomBackground
import com.nt118.joliecafe.util.extenstions.setIcon
import com.nt118.joliecafe.viewmodels.profile.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    lateinit var mGoogleSignInClient: GoogleSignInClient
    private val profileViewModel by viewModels<ProfileViewModel>()
    private lateinit var networkListener: NetworkListener
    private val currentUser = FirebaseAuth.getInstance().currentUser

    private var preventNavigateToProfileActivity = true
    private lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (currentUser == null) {
            startActivity(Intent(requireContext(), LoginActivity::class.java))
        }

        lifecycleScope.launchWhenStarted {
            profileViewModel.readIsUserFaceOrGGLogin.collectLatest {
                profileViewModel.isFaceOrGGLogin = it
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(layoutInflater)

        observerUserDataChangeStatus()
        observerGetUserResponse()
        observerNetworkMessage()
        observerRemoveUserNoticeToken()

        initUserData()

        updateNetworkStatus()
        updateBackOnlineStatus()

        buildGoogleClient()
        tabClickHandler()

        return binding.root
    }

    private fun tabClickHandler() {
        binding.btnLogout.setOnClickListener {
            if (profileViewModel.networkStatus) {
                FirebaseAuth.getInstance().signOut()
                FirebaseGoogleAuthentication().signOut(requireActivity(), mGoogleSignInClient)
                FirebaseFacebookLogin().facebookLoginSignOut()
                profileViewModel.removeUserNoticeToken()
                profileViewModel.saveUserToken(userToken = "")
                startActivity(Intent(requireContext(), LoginActivity::class.java))
            } else {
                showSnackBar(
                    message = "You are offline",
                    status = SNACK_BAR_STATUS_DISABLE,
                    icon = R.drawable.ic_wifi_off
                )
            }
        }

        binding.btnProfile.setOnClickListener {
            if (!preventNavigateToProfileActivity && profileViewModel.networkStatus) {
                val action = ProfileFragmentDirections.actionNavigationProfileToProfileActivity(
                    user = user,
                    isFaceOrGGLogin = profileViewModel.isFaceOrGGLogin
                )
                findNavController().navigate(action)
            } else {
                if (!profileViewModel.networkStatus) {
                    showSnackBar(
                        message = "You are offline",
                        status = SNACK_BAR_STATUS_DISABLE,
                        icon = R.drawable.ic_wifi_off
                    )
                } else {
                    showSnackBar(
                        message = "Your profile data is loading",
                        status = SNACK_BAR_STATUS_SUCCESS,
                        icon = R.drawable.ic_satisfied
                    )
                }
            }
        }

        binding.btnAddressBook.setOnClickListener {
            if (profileViewModel.networkStatus) {
                startActivity(Intent(requireContext(), AddressBookActivity::class.java))
            } else {
                showSnackBar(
                    message = "You are offline",
                    status = SNACK_BAR_STATUS_DISABLE,
                    icon = R.drawable.ic_wifi_off
                )
            }
        }

        binding.btnOrderHistory.setOnClickListener {
            if (profileViewModel.networkStatus) {
                startActivity(Intent(requireContext(), OrderHistoryActivity::class.java))
            } else {
                showSnackBar(
                    message = "You are offline",
                    status = SNACK_BAR_STATUS_DISABLE,
                    icon = R.drawable.ic_wifi_off
                )
            }
        }

        binding.btnSettings.setOnClickListener {
            if (profileViewModel.networkStatus) {
                startActivity(Intent(requireContext(), SettingsActivity::class.java))
            } else {
                showSnackBar(
                    message = "You are offline",
                    status = SNACK_BAR_STATUS_DISABLE,
                    icon = R.drawable.ic_wifi_off
                )
            }
        }
    }

    private fun buildGoogleClient() {
        // Google
        val options = GoogleSignInOptions.Builder(
            GoogleSignInOptions.DEFAULT_SIGN_IN
        ).requestIdToken(Constants.WEBCLIENT_ID)
            .requestEmail()
            .requestProfile()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), options)
    }

    private fun backOnlineRecallUserData() {
        if (profileViewModel.backOnline) {
            println("Network")
            profileViewModel.getUserInfos(
                token = profileViewModel.userToken
            )
        }
    }

    private fun observerUserDataChangeStatus() {
        lifecycleScope.launchWhenResumed {
            profileViewModel.readIsUserDataChange.collect {
                if (it && profileViewModel.networkStatus) {
                    profileViewModel.getUserInfos(
                        token = profileViewModel.userToken
                    )
                    Log.d("get", "Data Change")
                    profileViewModel.saveIsUserDataChange(false)
                }
            }
        }
    }

    private fun initUserData() {
        networkListener = NetworkListener()
        networkListener.checkNetworkAvailability(requireContext())
            .asLiveData().observeOnce(viewLifecycleOwner) { status ->
                profileViewModel.networkStatus = status
                if (profileViewModel.networkStatus) {
                    profileViewModel.getUserInfos(
                        token = profileViewModel.userToken
                    )
                }
            }
        networkListener.unregisterNetworkCallback()
    }

    private fun observerGetUserResponse() {
        profileViewModel.getUserInfosResponse.observe(viewLifecycleOwner) { userInfos ->
            when (userInfos) {
                is ApiResult.Loading -> {
                    preventNavigateToProfileActivity = true
                }
                is ApiResult.Success -> {
                    preventNavigateToProfileActivity = false
                    user = userInfos.data!!
                    if (profileViewModel.isFaceOrGGLogin) {
                        binding.userName.text = currentUser?.displayName ?: "You"
                        binding.userImg.load(
                            data = currentUser?.photoUrl
                        ) {
                            crossfade(600)
                            error(R.drawable.placeholder_image)
                        }
                    } else {
                        binding.userName.text = userInfos.data.fullName
                        binding.userImg.load(
                            data = userInfos.data.thumbnail
                        ) {
                            crossfade(600)
                            error(R.drawable.placeholder_image)
                        }
                    }
                }
                is ApiResult.Error -> {
                    preventNavigateToProfileActivity = false
                    if (profileViewModel.networkStatus) showSnackBar(
                        message = userInfos.message!!,
                        status = SNACK_BAR_STATUS_ERROR,
                        icon = R.drawable.ic_error
                    )
                }
                else -> {}
            }
        }
    }

    private fun observerRemoveUserNoticeToken() {
        profileViewModel.removeUserNoticeTokenResponse.observe(viewLifecycleOwner) { result ->
            when (result) {
                is ApiResult.NullDataSuccess -> {
                    println("Remove User Notice Token Success")
                }
                is ApiResult.Error -> {
                    println("Remove User Notice Token Error")
                }
                else -> {}
            }
        }
    }

    private fun observerNetworkMessage() {
        profileViewModel.networkMessage.observe(viewLifecycleOwner) { message ->
            if (!profileViewModel.networkStatus) {
                showSnackBar(
                    message = message,
                    status = Constants.SNACK_BAR_STATUS_DISABLE,
                    icon = R.drawable.ic_wifi_off
                )
            } else if (profileViewModel.networkStatus) {
                if (profileViewModel.backOnline) {
                    showSnackBar(
                        message = message,
                        status = Constants.SNACK_BAR_STATUS_SUCCESS,
                        icon = R.drawable.ic_wifi
                    )
                }
            }
        }
    }

    private fun updateBackOnlineStatus() {
        profileViewModel.readBackOnline.asLiveData().observe(viewLifecycleOwner) {
            profileViewModel.backOnline = it
        }
    }

    private fun updateNetworkStatus() {
        networkListener.checkNetworkAvailability(requireContext())
            .asLiveData().observe(viewLifecycleOwner) { status ->
                profileViewModel.networkStatus = status
                profileViewModel.showNetworkStatus()
                backOnlineRecallUserData()
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
        unregisterNetworkCallback()
    }

    private fun unregisterNetworkCallback() {
        try {
            networkListener.unregisterNetworkCallback()
        } catch (e: Exception) {
            // i.e. NetworkCallback was already unregistered
        }
    }
}