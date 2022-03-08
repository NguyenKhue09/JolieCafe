package com.nt118.joliecafe.ui.activities.login

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.facebook.FacebookSdk.setAdvertiserIDCollectionEnabled
import com.facebook.FacebookSdk.setAutoLogAppEventsEnabled
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.nt118.joliecafe.MainActivity
import com.nt118.joliecafe.databinding.ActivityLoginBinding
import com.nt118.joliecafe.firebase.firebaseauthentication.FirebaseFacebookLogin
import com.nt118.joliecafe.firebase.firebaseauthentication.FirebaseGoogleAuthentication
import com.nt118.joliecafe.util.Constants
import java.io.IOException


class LoginActivity : AppCompatActivity() {

    private var _binding: ActivityLoginBinding? = null
    private val  binding get() = _binding!!
    private lateinit var facebookLogin: FirebaseFacebookLogin
    lateinit var mGoogleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // setup facebook login
        setAutoLogAppEventsEnabled(true)
        setAdvertiserIDCollectionEnabled(true)
        facebookLogin = FirebaseFacebookLogin()

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
            facebookLogin.facebookLogin(this)
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
                    startActivity(Intent(this, MainActivity::class.java))
                    Toast.makeText(this@LoginActivity, "Google sign in successfully", Toast.LENGTH_LONG).show()
                    finish()
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this@LoginActivity, "Google sign in failed", Toast.LENGTH_LONG).show()
                }

            }
        }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}