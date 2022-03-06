package com.nt118.joliecafe.ui.activities.login

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import com.nt118.joliecafe.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // text span create account
        textSpanSignIn()
        // handle click open Forgot Password
        binding.tvBackSignin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
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
}