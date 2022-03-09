package com.nt118.joliecafe.ui.activities.login

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.FacebookSdk.setAdvertiserIDCollectionEnabled
import com.facebook.FacebookSdk.setAutoLogAppEventsEnabled
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.nt118.joliecafe.databinding.ActivityLoginBinding
import com.nt118.joliecafe.firebase.firebaseauthentication.FirebaseFacebookLogin
import com.nt118.joliecafe.firebase.firebaseauthentication.FirebaseGoogleAuthentication
import com.nt118.joliecafe.util.Constants
import java.io.IOException

val TAG = "Face"
class LoginActivity : AppCompatActivity() {

    private var _binding: ActivityLoginBinding? = null
    private val  binding get() = _binding!!
    private lateinit var facebookLogin: FirebaseFacebookLogin
    lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var callbackManager: CallbackManager
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // setup facebook login
        setAutoLogAppEventsEnabled(true)
        setAdvertiserIDCollectionEnabled(true)
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

        _binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onClick()
    }

    private fun onClick() {
        // FaceBook Login
        binding.imgFb.setOnClickListener {
            facebookLogin.facebookLogin(this, callbackManager, auth)
        }


        // text span create account
        textSpanCreateAccount()
        // handle click open Forgot Password
        binding.tvForgotPassword.setOnClickListener {
            startActivity(Intent(this,ForgotPasswordActivity::class.java))
        }

        // handle click open Sign Up
        binding.tvCreateAccount.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
            finish()
        }

        // Google Signin
        binding.imgGg.setOnClickListener {
            if (!FirebaseGoogleAuthentication().checkUser()) {
                FirebaseGoogleAuthentication().loginGoogle(userSignIn, mGoogleSignInClient)
            }
        }
    }


    private fun textSpanCreateAccount() {
        val tvCreateAccount : TextView = binding.tvCreateAccount

        val spannableString = SpannableString("Not signed up yet? Sign up here")

        val fColor = ForegroundColorSpan(Color.WHITE)
        spannableString.setSpan(fColor,0,18,Spannable.SPAN_INCLUSIVE_EXCLUSIVE)

        val bColor = ForegroundColorSpan(Color.parseColor("#E7A15C"))
        spannableString.setSpan(bColor,19,31,Spannable.SPAN_INCLUSIVE_EXCLUSIVE)

        tvCreateAccount.text = spannableString
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Pass the activity result back to the Facebook SDK
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }


    fun navigateToMainScreen() {
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
        finishAffinity()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}