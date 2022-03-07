package com.nt118.joliecafe.ui.activities.login

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.facebook.FacebookSdk.setAdvertiserIDCollectionEnabled
import com.facebook.FacebookSdk.setAutoLogAppEventsEnabled
import com.nt118.joliecafe.databinding.ActivityLoginBinding
import com.nt118.joliecafe.firebase.firebaseauthentication.FirebaseFacebookLogin


class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var facebookLogin: FirebaseFacebookLogin

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // setup facebook login
        setAutoLogAppEventsEnabled(true)
        setAdvertiserIDCollectionEnabled(true)
        facebookLogin = FirebaseFacebookLogin()

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
    }


//    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//        if (result.resultCode == Activity.RESULT_OK) {
//            callbackManager.onActivityResult(requestCode, resultCode, data)
//        }
//    }
//
//    fun openSomeActivityForResult() {
//        val intent = Intent(this, SomeActivity::class.java)
//        resultLauncher.launch(intent)
//    }


//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        callbackManager.onActivityResult(requestCode, resultCode, data)
//        // Pass the activity result back to the Facebook SDK
//    }




    private fun textSpanCreateAccount() {
        val tvCreateAccount : TextView = binding.tvCreateAccount

        val spannableString = SpannableString("Not signed up yet? Sign up here")

        val fColor = ForegroundColorSpan(Color.WHITE)
        spannableString.setSpan(fColor,0,18,Spannable.SPAN_INCLUSIVE_EXCLUSIVE)

        val bColor = ForegroundColorSpan(Color.parseColor("#E7A15C"))
        spannableString.setSpan(bColor,19,31,Spannable.SPAN_INCLUSIVE_EXCLUSIVE)

        tvCreateAccount.text = spannableString
    }
}