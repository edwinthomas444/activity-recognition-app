package com.tutorial.activityrecognition

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionRequest
import com.google.android.gms.location.ActivityTransitionResult
import com.google.android.gms.location.DetectedActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val transitionTypeToString = mapOf(
    ActivityTransition.ACTIVITY_TRANSITION_ENTER to "Enter",
    ActivityTransition.ACTIVITY_TRANSITION_EXIT to "Exit"
)

val transitionActivityToString = mapOf(
    DetectedActivity.STILL to "Still",
    DetectedActivity.IN_VEHICLE to "In Vehicle",
    DetectedActivity.RUNNING to "Running"
)

fun getTransitionRequests(): ActivityTransitionRequest{

    // defining transitions
    val transitions = mutableListOf<ActivityTransition>()

    // in vehicle
    transitions +=
        ActivityTransition.Builder()
            .setActivityType(DetectedActivity.IN_VEHICLE)
            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
            .build()

    transitions +=
        ActivityTransition.Builder()
            .setActivityType(DetectedActivity.IN_VEHICLE)
            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
            .build()

    // walking
    transitions +=
        ActivityTransition.Builder()
            .setActivityType(DetectedActivity.WALKING)
            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
            .build()

    transitions +=
        ActivityTransition.Builder()
            .setActivityType(DetectedActivity.WALKING)
            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
            .build()

    // still
    transitions +=
        ActivityTransition.Builder()
            .setActivityType(DetectedActivity.STILL)
            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
            .build()

    transitions +=
        ActivityTransition.Builder()
            .setActivityType(DetectedActivity.STILL)
            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
            .build()


    // define request object and pendingIntent wrapper around intent to broadcast receiver

    val request = ActivityTransitionRequest(transitions)
    return request
}
// definition of broadcast receiver
class ActivityTransitionBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // not even printing this log, so the activity is not getting recognition and the onReceive()
        // is not getting triggered
        Log.e(TAG, "Received an Intent..")
        // show a toast in the app to show the recognied activity
        Toast.makeText(activityContext, "Broadcast Receiver Triggered", Toast.LENGTH_LONG).show() // in Activity

        val result = ActivityTransitionResult.extractResult(intent)
        Log.e(TAG, result.toString())

        if (ActivityTransitionResult.hasResult(intent)) {
            val result = ActivityTransitionResult.extractResult(intent)
            Log.e(TAG, result.toString())
//            for (event in result!!.transitionEvents) {
//                val currTime = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
//                // just console output for now
//                Log.e(TAG, "Transition: ${transitionTypeToString[event.transitionType]} ${transitionActivityToString[event.activityType]} at Date: ${currTime}")
//            }
        }
    }

    companion object {
        private const val TAG = "BroadcastReceiver"
    }
}