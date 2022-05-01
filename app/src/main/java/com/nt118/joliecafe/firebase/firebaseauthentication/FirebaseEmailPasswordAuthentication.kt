package com.nt118.joliecafe.firebase.firebaseauthentication

import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.nt118.joliecafe.ui.activities.forgotpassword.ForgotPasswordActivity
import com.nt118.joliecafe.ui.activities.login.LoginActivity
import com.nt118.joliecafe.ui.activities.signup.SignUpActivity

class FirebaseEmailPasswordAuthentication {

    var mAuth: FirebaseAuth = FirebaseAuth.getInstance()


    fun registerUser(email: String, password: String, signUpActivity: SignUpActivity) {
        val data: HashMap<String, Any> = hashMapOf()
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if(task.isSuccessful) {
                    val firebaseUser: FirebaseUser? = task.result!!.user!!
                    Toast.makeText(signUpActivity, "Register User successful!", Toast.LENGTH_SHORT).show()
                    data["_id"] = task.result.user!!.uid
                    data["fullname"] = task.result.user!!.displayName ?: ""
                    data["email"] = task.result.user!!.email ?: ""
                    signUpActivity.createUser(data = data)
                } else {
                    Toast.makeText(signUpActivity, "Register User failed!", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                println(it)
            }
    }

    fun loginUser(email: String, password: String, loginActivity: LoginActivity) {
        val data: MutableMap<String, String> = mutableMapOf()
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(loginActivity, "Login successful", Toast.LENGTH_SHORT).show()
                    data["_id"] = task.result.user!!.uid
                    data["fullname"] = task.result.user!!.displayName ?: ""
                    data["email"] = task.result.user!!.email ?: ""
                    loginActivity.createUser(data = data)
                } else {
                    Toast.makeText(loginActivity, "Login failed!", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                println(it)
            }
    }

    fun forgotPassword(email: String, forgotPasswordActivity: ForgotPasswordActivity) {
        mAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(forgotPasswordActivity, "Check your email to reset password", Toast.LENGTH_SHORT).show()
                    forgotPasswordActivity.navigateToMainScreen()
                } else {
                    Toast.makeText(forgotPasswordActivity, "Some thing went wrong", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnCompleteListener {
                println(it)
            }
    }
}