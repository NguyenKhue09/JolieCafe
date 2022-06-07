package com.nt118.joliecafe

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.messaging.FirebaseMessaging
import com.nt118.joliecafe.databinding.ActivityMainBinding
import com.nt118.joliecafe.util.ApiResult
import com.nt118.joliecafe.util.Constants
import com.nt118.joliecafe.viewmodels.MainActivityViewModel
import dagger.hilt.android.AndroidEntryPoint

const val TOPIC = Constants.TOPIC

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mNavController: NavController

    private val mainActivityViewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        subscribeNotificationTopic()
        observerUserNoticeToken()
        getUserNoticeToken()

        val navView: BottomNavigationView = binding.navView

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        mNavController = navHostFragment.navController
        navView.setupWithNavController(mNavController)
    }

    private fun getUserNoticeToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("NoticeToken", "Fetching FCM registration token failed", task.exception)
            }

            // Get new FCM registration token
            val token = task.result

            println("Notice token")
            println(token)
            if(mainActivityViewModel.userNoticeToken.isEmpty()) {
                updateUserNoticeToken(token = token)
                mainActivityViewModel.saveUserNoticeToken(token = token)
            }

            if (mainActivityViewModel.userNoticeToken.isNotEmpty() && mainActivityViewModel.userNoticeToken != token) {
                updateUserNoticeToken(token = mainActivityViewModel.userNoticeToken)
            }
        }
    }

    private fun updateUserNoticeToken(token: String) {
        mainActivityViewModel.updateUserNoticeToken(token)
    }

    private fun observerUserNoticeToken() {
        mainActivityViewModel.updateUserNoticeTokenResponse.observe(this) { result ->
            when(result) {
                is ApiResult.Success -> {
                    println("Notice token updated")
                }
                is ApiResult.Error -> {
                    println("Notice token update failed")
                }
                else -> {
                    println("Notice token update failed")
                }
            }
        }
    }

    private fun subscribeNotificationTopic() {
        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC)
    }
    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(mNavController, null)
    }
}