package com.example.finalyearproject.util

import com.example.finalyearproject.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.example.finalyearproject.MainActivity
import com.example.finalyearproject.models.NotificationsList


class NotificationHelper(private val mContext: Context) {
    fun createNotification() {

        val notList = NotificationsList()




        val intent = Intent(mContext, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val resultPendingIntent = PendingIntent.getActivity(
            mContext,
            0 , intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val mBuilder = NotificationCompat.Builder(mContext, NOTIFICATION_CHANNEL_ID)
        mBuilder.setSmallIcon(R.drawable.ic_app_icon)
        if(notList.getItemsInNotifications().isNotEmpty()) {
            mBuilder.setContentTitle("Close to expiration!")
                .setContentText("Some of the food items are about to expire: cook or donate them! +")
                .setAutoCancel(false)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setContentIntent(resultPendingIntent)
            //if there are some items show the first 2 in the notification
                if( NotificationsList.Singleton.notificationsList.size > 2) {
                    val item1 = NotificationsList.Singleton.notificationsList[0]
                    val item2 = NotificationsList.Singleton.notificationsList[1]
                    mBuilder.setStyle(NotificationCompat.BigTextStyle().bigText("You have ${item1.itemName} and ${item2.itemName}. You can find recipes in the app"))
                }
        } else {
            mBuilder.setContentTitle("Your list is empty!")
                .setContentText("Scan items in the app or using your Smart Kitchen Device")
                .setAutoCancel(false)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setContentIntent(resultPendingIntent)
        }

        val mNotificationManager =
            mContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "NOTIFICATION_CHANNEL_NAME",
                importance
            )
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.YELLOW
            notificationChannel.enableVibration(true)
            notificationChannel.vibrationPattern =
                longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            mBuilder.setChannelId(NOTIFICATION_CHANNEL_ID)
            mNotificationManager.createNotificationChannel(notificationChannel)
        }
        mNotificationManager.notify(0 , mBuilder.build())
    }

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "21011"
    }
}