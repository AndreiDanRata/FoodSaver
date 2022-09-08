package com.example.finalyearproject.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import com.example.finalyearproject.MainActivity
import java.util.*


//this class listens to when the alarm happens(for notifications)
class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = context!!.getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis()+8*60*60*1000,  //Notification sent every 8 hours
            //AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
        Log.d("NOTIFICATIONRECIVER", "NOTIFICATION RECEIVED")
        val notificationHelper = NotificationHelper(context!!)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            notificationHelper.createNotification()
        }

    }
}