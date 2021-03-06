package com.nt118.joliecafe.ui.activities.signup

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Patterns
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.facebook.CallbackManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.nt118.joliecafe.MainActivity
import com.nt118.joliecafe.databinding.ActivitySignUpBinding
import com.nt118.joliecafe.firebase.firebaseauthentication.FirebaseEmailPasswordAuthentication
import com.nt118.joliecafe.firebase.firebaseauthentication.FirebaseFacebookLogin
import com.nt118.joliecafe.firebase.firebaseauthentication.FirebaseGoogleAuthentication
import com.nt118.joliecafe.ui.activities.login.LoginActivity
import com.nt118.joliecafe.util.ApiResult
import com.nt118.joliecafe.util.Constants
import com.nt118.joliecafe.util.NetworkListener
import com.nt118.joliecafe.viewmodels.sign_up.SignUpViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException

@AndroidEntryPoint
class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var facebookLogin: FirebaseFacebookLogin
    lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var callbackManager: CallbackManager
    private lateinit var auth: FirebaseAuth
    private val signUpViewModel: SignUpViewModel by viewModels()
    private lateinit var networkListener: NetworkListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // facebook
        facebookLogin = FirebaseFacebookLogin()
        auth = Firebase.auth
        callbackManager = CallbackManager.Factory.create()

        // google
        val options = GoogleSignInOptions.Builder(
            GoogleSignInOptions.DEFAULT_SIGN_IN
        ).requestIdToken(Constants.WEBCLIENT_ID)
            .requestEmail()
            .requestProfile()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, options)

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // text span create account
        textSpanSignIn()
        // handle click open Forgot Password
        binding.tvBackSignin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        signUpViewModel.readBackOnline.asLiveData().observe(this) {
            signUpViewModel.backOnline = it
        }

        lifecycleScope.launchWhenStarted {
            networkListener = NetworkListener()
            networkListener.checkNetworkAvailability(this@SignUpActivity)
                .collect { status ->
                    signUpViewModel.networkStatus = status
                    signUpViewModel.showNetworkStatus()
                }
        }

        signUpViewModel.createUserResponse.observe(this) { result ->
            when(result) {
                is ApiResult.Success -> {
                    navigateToMainScreen()
                }
                is ApiResult.Error -> {
                    Toast.makeText(this, "${result.message}", Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }

        signUpViewModel.userLoginGGOrFaceResponse.observe(this) { result ->
            when(result) {
                is ApiResult.Success -> {
                    navigateToMainScreen()
                }
                is ApiResult.Error -> {
                    Toast.makeText(this, "${result.message}", Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }

        onClick()
    }

    private fun onClick() {
        // FaceBook Login
        binding.imgFb.setOnClickListener {
            signUpViewModel.saveIsUserFaceOrGGLogin(true)
            facebookLogin.facebookLogin(callbackManager, auth, this)
        }

        // Google Signin
        binding.imgGg.setOnClickListener {
            signUpViewModel.saveIsUserFaceOrGGLogin(true)
            if (!FirebaseGoogleAuthentication().checkUser()) {
                FirebaseGoogleAuthentication().loginGoogle(userSignIn, mGoogleSignInClient)
            }
        }

        binding.btnSignUp.setOnClickListener {
            signUpViewModel.saveIsUserFaceOrGGLogin(false)
            registerUserWithEmailPassword()
        }
    }

    private val userSignIn =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                try {
                    val account = GoogleSignIn.getSignedInAccountFromIntent(data).result
                    account?.let {
                        FirebaseGoogleAuthentication().googleAuthForFirebase(it, this)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }

    private fun registerUserWithEmailPassword() {
        if(validateUserName() && validateEmail() && validatePassword() && validateConfirmPassword()) {
            val email = binding.etEmail.text.toString().trim{it <= ' '}
            val password = binding.etPassword.text.toString().trim{it <= ' '}
            val userName = binding.etUserName.text.toString().trim{it <= ' '}

            FirebaseEmailPasswordAuthentication().registerUser(
                email = email,
                password= password,
                fullName = userName,
                signUpActivity = this
            )
        }
    }

    private fun validateUserName(): Boolean {
        val username = binding.etUserName.text.toString().trim{it <= ' '}

        if (username.isEmpty()) {
            binding.etUserName.requestFocus()
            binding.etUserNameLayout.error = "You must enter your username!"
            return false
        }
        return true
    }

    private fun validatePassword(): Boolean {
        val password = binding.etPassword.text.toString().trim{it <= ' '}

        if (password.isEmpty()) {
            binding.etPassword.requestFocus()
            binding.etPasswordLayout.error = "You must enter your password!"
            return false
        }
        if (password.length < 6) {
            binding.etPassword.requestFocus()
            binding.etPasswordLayout.error = "Your password length less than 6 character!"
            return false
        }

        return true
    }

    private fun validateConfirmPassword(): Boolean {
        val password = binding.etConfirmPassword.text.toString().trim{it <= ' '}

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

        if (password != binding.etPassword.text.toString()) {
            binding.etConfirmPassword.requestFocus()
            binding.etConfirmPasswordLayout.error = "Your confirm password not match your password!"
            return false
        }

        return true
    }

    private fun validateEmail(): Boolean {
        val email = binding.etEmail.text.toString().trim{it <= ' '}

        if (email.isEmpty()) {
            binding.etEmail.requestFocus()
            binding.etEmailLayout.error = "You must enter your email!"
            return false
        }
        if (!isValidEmail(email)) {
            binding.etEmail.requestFocus()
            binding.etEmailLayout.error = "Your email is wrong format!"
            return false
        }
        return true
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun navigateToMainScreen() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        })
        finish()
    }


    private fun textSpanSignIn() {
        val tvCreateAccount : TextView = binding.tvBackSignin

        val spannableString = SpannableString("Already signed up? Sign in here")

        val fColor = ForegroundColorSpan(Color.WHITE)
        spannableString.setSpan(fColor,0,18, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)

        val bColor = ForegroundColorSpan(Color.parseColor("#E7A15C"))
        spannableString.setSpan(bColor,19,31, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)

        tvCreateAccount.text = spannableString
    }

    fun createUser(userData: Map<String, String>) {
        signUpViewModel.createUser(userData = userData)
    }

    fun userLogin(userId: String) {
        signUpViewModel.userLogin(userId = userId)
    }
}