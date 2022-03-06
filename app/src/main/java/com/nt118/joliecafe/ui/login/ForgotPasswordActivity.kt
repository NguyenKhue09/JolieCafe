package com.nt118.joliecafe.ui.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.nt118.joliecafe.R
import com.nt118.joliecafe.databinding.ActivityForgotPasswordBinding

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // handle click open cancel
        binding.btnCancle.setOnClickListener {
            startActivity(Intent(this,LoginActivity::class.java))
        }
    }
}