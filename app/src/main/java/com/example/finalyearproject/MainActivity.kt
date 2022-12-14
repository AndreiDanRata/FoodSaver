package com.example.finalyearproject

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.finalyearproject.models.NotificationsList
import com.example.finalyearproject.util.NotificationReceiver
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.*

/**
 *  MainActivity hosts all fragments() after the user logs in
 *  foodListFragment,recipesFragment,mapFragment,settingsFragment
 */
class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    /**
     * Initializing the activity
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        navController = findNavController(R.id.navHostFragment)
        val appBarConfiguration = AppBarConfiguration(
                setOf(R.id.foodListFragment,
                R.id.recipesFragment,
                R.id.mapFragment,
                R.id.settingsFragment))

        bottomNavigationView.setupWithNavController(navController)
        setupActionBarWithNavController(navController, appBarConfiguration)

        myAlarm()
    }

    /**
     * Method hides soft keyboard after clicking outside an editText box
     */
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(this.currentFocus!!.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }

    /**
     * Method responsible for managing the times of the day when notifications are shown
     */
    private fun myAlarm() {
        val calendar = Calendar.getInstance()
        calendar[Calendar.HOUR_OF_DAY] = 22
        calendar[Calendar.MINUTE] = 15
        calendar[Calendar.SECOND] = 0
        calendar.set(Calendar.SECOND, 0)
        if (calendar.time < Date()){
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        val intent = Intent(applicationContext, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.setAndAllowWhileIdle(  //setRepeat
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }
}