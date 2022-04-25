package com.nt118.joliecafe.firebase.firebaseauthentication

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
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
    fun facebookLogin(
        callbackManager: CallbackManager,
        auth: FirebaseAuth,
        context: Context
    ) {
        LoginManager.getInstance().logInWithReadPermissions(
            context as ActivityResultRegistryOwner,
            callbackManager,
            listOf("email", "public_profile", "user_friends")
        )
        println("Facebook Login")
        LoginManager.getInstance().registerCallback(callbackManager, object :
            FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                println("facebook:onSuccess:$result")
                handleFacebookAccessToken(result.accessToken, context as Activity, auth)
            }

            override fun onCancel() {
                println("facebook:onCancel")
            }

            override fun onError(error: FacebookException) {
                println("facebook:onError: ${error.message}")
            }
        })
    }

    private fun handleFacebookAccessToken(token: AccessToken, activity: Activity, auth: FirebaseAuth) {
        println("handleFacebookAccessToken:$token")
        val credential = FacebookAuthProvider.getCredential(token.token)
        val data: HashMap<String, Any> = hashMapOf()

        auth.signInWithCredential(credential)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in User's information
                    println("signInWithCredential:success")
                    val user = auth.currentUser
                    println(user?.displayName)
                    when(activity) {
                        is LoginActivity -> {
                            data["_id"] = task.result.user!!.uid
                            data["fullname"] = task.result.user!!.displayName ?: ""
                            data["email"] = task.result.user!!.email ?: ""
                            activity.createUser(data = data)
                        }
                    }
                } else {
                    // If sign in fails, display a message to the User.
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