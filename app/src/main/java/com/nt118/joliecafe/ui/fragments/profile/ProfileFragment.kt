package com.nt118.joliecafe.ui.fragments.profile

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.nt118.joliecafe.databinding.FragmentProfileBinding
import com.nt118.joliecafe.firebase.firebaseauthentication.FirebaseFacebookLogin
import com.nt118.joliecafe.firebase.firebaseauthentication.FirebaseGoogleAuthentication
import com.nt118.joliecafe.ui.activities.address_book.AddressBookActivity
import com.nt118.joliecafe.ui.activities.login.LoginActivity
import com.nt118.joliecafe.ui.activities.order_history.OrderHistoryActivity
import com.nt118.joliecafe.ui.activities.profile.ProfileActivity
import com.nt118.joliecafe.util.Constants


class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    lateinit var mGoogleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            FirebaseGoogleAuthentication().signOut(requireActivity(), mGoogleSignInClient)
            FirebaseFacebookLogin().facebookLoginSignOut()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
        }

        binding.btnProfile.setOnClickListener {
            startActivity(Intent(requireContext(), ProfileActivity::class.java))
        }

        binding.btnAddressBook.setOnClickListener {
            startActivity(Intent(requireContext(), AddressBookActivity::class.java))
        }

        binding.btnOrderHistory.setOnClickListener {
            startActivity(Intent(requireContext(), OrderHistoryActivity::class.java))
        }

        return binding.root
    }
}