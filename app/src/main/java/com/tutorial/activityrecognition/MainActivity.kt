package com.tutorial.activityrecognition

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.PendingIntentCompat.send
import androidx.core.content.ContextCompat
import com.google.android.gms.location.ActivityRecognition
import java.util.Calendar

lateinit var activityContext: Context
class MainActivity : AppCompatActivity() {
//    private val transitionReciever = ActivityTransitionBroadcastReceiver()
    private val TRANSITIONS_RECEIVER_ACTION = BuildConfig.APPLICATION_ID + "TRANSITIONS_RECEIVER_ACTION"
    private val MY_PERMISSIONS_REQUEST_ACTIVITY_RECOGNITION = 50
    private val reciever = ActivityTransitionBroadcastReceiver()

    @SuppressLint("MutableImplicitPendingIntent")
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        reciever.setActivityContext(this)
        // registering receiver
//        registerReceiver(
//            reciever,
//            IntentFilter("TRANSITION_ACTION")
//        )

        val intent = Intent(this, ActivityTransitionBroadcastReceiver::class.java)

        activityContext = this
//        val intent = Intent("TRANSITION_ACTION")

        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_MUTABLE)
        val transitionRequest = getTransitionRequests()
//        pendingIntent.send()


        // check for permissions
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACTIVITY_RECOGNITION)
            != PackageManager.PERMISSION_GRANTED) {
            // ask for permission
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACTIVITY_RECOGNITION),
                MY_PERMISSIONS_REQUEST_ACTIVITY_RECOGNITION);
        }else{
//            pendingIntent.send()
            // registering client

//            ActivityRecognition.getClient(this).requestActivityUpdates(
//                3000,
//                pendingIntent
//            ).addOnSuccessListener {
//                Log.d(ContentValues.TAG, "successful activity recognition listener registration")
//            }.addOnFailureListener{ e: Exception ->
//                Log.e(ContentValues.TAG,"Transitions could not be unregistered: $e")
//            }
            ActivityRecognition.getClient(this)
                .requestActivityTransitionUpdates(transitionRequest, pendingIntent)
                .addOnSuccessListener {
                    // Handle success
                    Log.d(ContentValues.TAG, "successful activity recognition listener registration")
                }
                .addOnFailureListener { e: Exception ->
                    // Handle error
                    Log.e(ContentValues.TAG,"Transitions could not be unregistered: $e")
                }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }

//    override fun onDestroy() {
//        super.onDestroy()
//        // unregister on stop
//        unregisterReceiver(reciever)
//    }
}


