package com.nt118.joliecafe.ui.fragments.profile

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import coil.load
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
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
import com.nt118.joliecafe.util.NetworkListener
import com.nt118.joliecafe.viewmodels.profile.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

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
    ): View? {

        // Google
        val options = GoogleSignInOptions.Builder(
            GoogleSignInOptions.DEFAULT_SIGN_IN
        ).requestIdToken(Constants.WEBCLIENT_ID)
            .requestEmail()
            .requestProfile()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), options)

        _binding = FragmentProfileBinding.inflate(layoutInflater)

        binding.btnLogout.setOnClickListener {
            if (profileViewModel.networkStatus) {
                FirebaseAuth.getInstance().signOut()
                FirebaseGoogleAuthentication().signOut(requireActivity(), mGoogleSignInClient)
                FirebaseFacebookLogin().facebookLoginSignOut()
                profileViewModel.saveUserToken(userToken = "")
                startActivity(Intent(requireContext(), LoginActivity::class.java))
            } else {
                Toast.makeText(requireContext(), "No internet access", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnProfile.setOnClickListener {
            if(!preventNavigateToProfileActivity && profileViewModel.networkStatus) {
                val action = ProfileFragmentDirections.actionNavigationProfileToProfileActivity(
                    user = user,
                    isFaceOrGGLogin = profileViewModel.isFaceOrGGLogin
                )
                findNavController().navigate(action)
            } else {
                if (!profileViewModel.networkStatus) {
                    Toast.makeText(requireContext(), "No internet access", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Can't get your profile", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnAddressBook.setOnClickListener {
            if (profileViewModel.networkStatus) {
                startActivity(Intent(requireContext(), AddressBookActivity::class.java))
            } else {
                Toast.makeText(requireContext(), "No internet access", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnOrderHistory.setOnClickListener {
            if (profileViewModel.networkStatus) {
                startActivity(Intent(requireContext(), OrderHistoryActivity::class.java))
            } else {
                Toast.makeText(requireContext(), "No internet access", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnSettings.setOnClickListener {
            if (profileViewModel.networkStatus) {
                startActivity(Intent(requireContext(), SettingsActivity::class.java))
            } else {
                Toast.makeText(requireContext(), "No internet access", Toast.LENGTH_SHORT).show()
            }
        }

        profileViewModel.readBackOnline.asLiveData().observe(viewLifecycleOwner) {
            profileViewModel.backOnline = it
        }

        lifecycleScope.launchWhenResumed {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                profileViewModel.readIsUserDataChange.collectLatest {
                    if (it && profileViewModel.networkStatus) {
                        profileViewModel.getUserInfos(
                            token = profileViewModel.userToken
                        )
                        profileViewModel.saveIsUserDataChange(false)
                    } else if(!profileViewModel.networkStatus) {
                        profileViewModel.showNetworkStatus()
                    }
                }
            }
        }


        lifecycleScope.launchWhenStarted {
            networkListener = NetworkListener()
            networkListener.checkNetworkAvailability(requireContext())
                .collect { status ->
                    profileViewModel.networkStatus = status
                    profileViewModel.showNetworkStatus()
                    if(profileViewModel.backOnline) {
                        profileViewModel.getUserInfos(
                            token = profileViewModel.userToken
                        )
                    }
                }
        }

        lifecycleScope.launchWhenStarted {
            profileViewModel.readUserToken.collectLatest { token ->
                profileViewModel.userToken = token
                if(profileViewModel.networkStatus) {
                    profileViewModel.getUserInfos(
                        token = profileViewModel.userToken
                    )
                } else {
                    profileViewModel.showNetworkStatus()
                }

            }
        }

        profileViewModel.getUserInfosResponse.observe(viewLifecycleOwner) { userInfos ->
            when(userInfos) {
                is ApiResult.Loading -> {
                    preventNavigateToProfileActivity = true
                }
                is ApiResult.Success -> {
                    preventNavigateToProfileActivity = false
                    user = userInfos.data!!
                    if(profileViewModel.isFaceOrGGLogin) {
                        binding.userName.text = currentUser?.displayName ?: "You"
                        binding.userImg.load(
                            uri = currentUser?.photoUrl
                        ) {
                            crossfade(600)
                            error(R.drawable.placeholder_image)
                        }
                    } else {
                        binding.userName.text = userInfos.data.fullName
                        binding.userImg.load(
                            uri = userInfos.data.thumbnail
                        ) {
                            crossfade(600)
                            error(R.drawable.placeholder_image)
                        }
                    }
                }
                is ApiResult.Error -> {
                    preventNavigateToProfileActivity = false
                    Toast.makeText(requireContext(), userInfos.message, Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }

        return binding.root
    }
}