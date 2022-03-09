package com.nt118.joliecafe.firebase.firebaseauthentication

import android.app.Activity
import android.widget.Toast
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.nt118.joliecafe.ui.activities.login.LoginActivity


class FirebaseFacebookLogin {
    fun facebookLogin(activity: Activity, callbackManager: CallbackManager, auth: FirebaseAuth) {
        LoginManager.getInstance().logInWithReadPermissions(
            activity = activity,
            listOf("email", "public_profile")
        )
        println("Facebook Login")
        LoginManager.getInstance().registerCallback(callbackManager, object :
            FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                println("facebook:onSuccess:$result")
                handleFacebookAccessToken(result.accessToken, activity, auth)
            }

            override fun onCancel() {
                println("facebook:onCancel")
            }

            override fun onError(error: FacebookException) {
                println("facebook:onError")
            }
        })
    }

    private fun handleFacebookAccessToken(token: AccessToken, activity: Activity, auth: FirebaseAuth) {
        println("handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    println("signInWithCredential:success")
                    val user = auth.currentUser
                    println(user?.displayName)
                    when(activity) {
                        is LoginActivity -> {
                            activity.navigateToMainScreen()
                        }
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    println("signInWithCredential:failure  ${task.exception}")
                    Toast.makeText(
                        activity.baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    fun facebookLoginSignOut() {
        LoginManager.getInstance().logOut()
    }
}