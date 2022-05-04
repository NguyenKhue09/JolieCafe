package com.nt118.joliecafe.firebase.firebaseauthentication

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import com.nt118.joliecafe.ui.activities.login.LoginActivity
import com.nt118.joliecafe.ui.activities.signup.SignUpActivity

class FirebaseGoogleAuthentication {

    var mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    fun signOut(activity: Activity, mGoogleSignInClient: GoogleSignInClient) {
        mGoogleSignInClient.signOut().addOnCompleteListener(activity,
            OnCompleteListener<Void?> { })
    }

    fun googleAuthForFirebase(account: GoogleSignInAccount, activity: Activity) {
        val credentials = GoogleAuthProvider.getCredential(account.idToken, null)
        val data: MutableMap<String, String> = mutableMapOf()

        try {
            mAuth.signInWithCredential(credentials).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(activity, "Google sign in successfully", Toast.LENGTH_LONG)
                        .show()

                    data["_id"] = task.result.user!!.uid
                    data["fullname"] = task.result.user!!.displayName ?: ""
                    data["email"] = task.result.user!!.email ?: ""

                    val isNewUser = task.result.additionalUserInfo?.isNewUser ?: false

                    when(activity) {
                        is LoginActivity -> {
                            if(isNewUser) {
                                activity.createUser(userData = data)
                            } else {
                                activity.userLogin(userId = task.result.user!!.uid)
                            }
                        }
                        is SignUpActivity -> {
                            if(isNewUser) {
                                activity.createUser(userData = data)
                            } else {
                                activity.userLogin(userId = task.result.user!!.uid)
                            }
                        }
                    }
                } else {
                    try {
                        throw  task.exception!!
                    } catch (e: FirebaseAuthUserCollisionException) {
                        Toast.makeText(activity, "This email is already use in the other account", Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        Toast.makeText(activity, "Google sign in failed", Toast.LENGTH_LONG).show()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(activity, e.message, Toast.LENGTH_LONG).show()
        }
    }

    fun loginGoogle(
        loginGGRequest: ActivityResultLauncher<Intent>,
        mGoogleSignInClient: GoogleSignInClient
    ) {
        mGoogleSignInClient.signInIntent.also {
            loginGGRequest.launch(it)
        }
    }

    fun checkUser(): Boolean {
        if (mAuth.currentUser != null) {
            Log.d("CurrentUser", true.toString())
            return true
        } else {
            Log.d("CurrentUser", false.toString())
        }
        return false
    }
}