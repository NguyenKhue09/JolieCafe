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
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirebaseGoogleAuthentication {

    var mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    fun signOut(activity: Activity, mGoogleSignInClient: GoogleSignInClient) {
        mAuth.signOut()
        mGoogleSignInClient.signOut().addOnCompleteListener(activity,
            OnCompleteListener<Void?> { })
        Toast.makeText(activity, "Google sign out: ${mAuth.currentUser}", Toast.LENGTH_LONG).show()
    }

    fun googleAuthForFirebase(account: GoogleSignInAccount, activity: Activity) {
        val credentials = GoogleAuthProvider.getCredential(account.idToken, null)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                mAuth.signInWithCredential(credentials).await()
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(activity, e.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun loginGoogle(loginGGRequest: ActivityResultLauncher<Intent>, mGoogleSignInClient: GoogleSignInClient) {
        mGoogleSignInClient.signInIntent.also {
            loginGGRequest.launch(it)
        }
    }

    fun checkUser(): Boolean {
        if (mAuth.currentUser != null) {
            Log.d("CurrentUser", true.toString())
            return true
        } else {
            Log.d("CurrentUser", true.toString())
        }
        return false
    }
}