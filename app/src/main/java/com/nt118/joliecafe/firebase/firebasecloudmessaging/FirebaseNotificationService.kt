package com.nt118.joliecafe.firebase.firebasecloudmessaging

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.nt118.joliecafe.R
import com.nt118.joliecafe.data.DataStoreRepository
import com.nt118.joliecafe.ui.activities.detail.DetailActivity
import com.nt118.joliecafe.ui.activities.notifications.NotificationActivity
import com.nt118.joliecafe.ui.activities.order_history.OrderHistoryActivity
import com.nt118.joliecafe.util.Constants.Companion.listNotificationType
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.random.Random

private const val CHANNEL_ID = "jolie_notice_channel"
const val channelName = "com.nt118.joliecafe"

class FirebaseNotificationService: FirebaseMessagingService() {

    @Inject
    lateinit var dataStoreRepository: DataStoreRepository

    lateinit var job: Job

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        saveNewTokenToDataStore(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val intent =  handleNotificationType(data = message.data)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationID = Random.nextInt()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        println("message: ${message.data}")

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0 , intent, PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(message.data["title"])
            .setContentText(message.data["message"])
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(notificationID, notification)
    }

    private fun handleNotificationType(data: Map<String, String>): Intent {
        when(data["type"]) {
            listNotificationType[0] -> {
                return Intent(this, NotificationActivity::class.java)
            }
            listNotificationType[1] -> {
                val intent = Intent(this, DetailActivity::class.java).apply {
                    putExtra("productId", data["productId"])
                }
                return intent
            }
            listNotificationType[2] -> {
                return Intent(this, NotificationActivity::class.java)
            }
            listNotificationType[3] -> {
                return Intent(this, OrderHistoryActivity::class.java)
            }
            else -> {
                return Intent(this, NotificationActivity::class.java)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channelName = channelName
        val channel = NotificationChannel(CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH).apply {
            description = "My channel description"
            enableLights(true)
            lightColor = Color.GREEN
        }

        notificationManager.createNotificationChannel(channel)
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun saveNewTokenToDataStore(token: String) {
        job = GlobalScope.launch {
            withContext(Dispatchers.IO) {
                dataStoreRepository.saveUserToken(token)
            }
        }

        job.invokeOnCompletion { println("Job1 completed") }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}