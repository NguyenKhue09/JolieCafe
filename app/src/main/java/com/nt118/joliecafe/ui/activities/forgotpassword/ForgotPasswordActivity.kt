package com.nt118.joliecafe.ui.activities.forgotpassword

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import com.nt118.joliecafe.databinding.ActivityForgotPasswordBinding
import com.nt118.joliecafe.firebase.firebaseauthentication.FirebaseEmailPasswordAuthentication
import com.nt118.joliecafe.ui.activities.login.LoginActivity

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // handle click open cancel
        binding.btnCancel.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.btnConfirm.setOnClickListener {
            if (validateEmail()) {
                val email = binding.etUserName.text.toString().trim{it <= ' '}
                FirebaseEmailPasswordAuthentication().forgotPassword(
                    email = email,
                    forgotPasswordActivity = this
                )
            }
        }
    }

    private fun validateEmail(): Boolean {
        val email = binding.etUserName.text.toString().trim{it <= ' '}

        if (email.isEmpty()) {
            binding.etUserName.requestFocus()
            binding.etUserNameLayout.error = "You must enter your email!"
            return false
        }
        if (!isValidEmail(email)) {
            binding.etUserName.requestFocus()
            binding.etUserNameLayout.error = "Your email is wrong format!"
            return false
        }
        return true
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun navigateToMainScreen() {
        finish()
    }
}