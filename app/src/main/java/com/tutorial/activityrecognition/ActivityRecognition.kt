package com.tutorial.activityrecognition

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransition.ACTIVITY_TRANSITION_ENTER
import com.google.android.gms.location.ActivityTransition.ACTIVITY_TRANSITION_EXIT
import com.google.android.gms.location.ActivityTransitionRequest
import com.google.android.gms.location.ActivityTransitionResult
import com.google.android.gms.location.DetectedActivity
import com.google.android.gms.location.DetectedActivity.IN_VEHICLE
import com.google.android.gms.location.DetectedActivity.RUNNING
import com.google.android.gms.location.DetectedActivity.STILL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val transitionTypeToString = mapOf(
    ACTIVITY_TRANSITION_ENTER to "Enter",
    ACTIVITY_TRANSITION_EXIT to "Exit"
)

val transitionActivityToString = mapOf(
    STILL to "Still",
    IN_VEHICLE to "In Vehicle",
    RUNNING to "Running"
)

@Composable
fun ActivityRecognitionComponent(){

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
    val context = LocalContext.current

    val request = ActivityTransitionRequest(transitions)
    val intent = Intent(context, ActivityTransitionBroadcastReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

    // when a transition is received, the pendingIntent executes intent to broadcast receiver
    // the broadcast receiver catches the intent with event details that can then be processed
    val task = ActivityRecognition.getClient(context)
        .requestActivityTransitionUpdates(request, pendingIntent)

    task.addOnSuccessListener {
        // Handle success
        Log.d(TAG, "successful activity recognition listener registration")
    }

    task.addOnFailureListener { e: Exception ->
        // Handle error
        Log.e(TAG,"Transitions could not be unregistered: $e")
    }

    // need to implement deregistration of client too

}


// definition of broadcast receiver
class ActivityTransitionBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (ActivityTransitionResult.hasResult(intent)) {
            val result = ActivityTransitionResult.extractResult(intent)

            for (event in result!!.transitionEvents) {
                val currTime = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
                // just console output for now
                Log.d(TAG, "Transition: ${transitionTypeToString[event.transitionType]} ${transitionActivityToString[event.activityType]} at Date: ${currTime}")
            }
        }
    }

    companion object {
        private const val TAG = "BroadcastReceiver"
    }
}